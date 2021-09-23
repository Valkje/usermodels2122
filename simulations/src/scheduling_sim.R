source("src/sim_dat.R")

schedulingSim <- function(path_out,short_term_window=150,
                          long_term_window=2500,
                          auto_1_effect=0.015,
                          auto_2_effect=0.03,
                          sd_decision=0.5) {
  
  # First simulates ground-truth changes in the pupil size.
  # This reflects how the pupil size would change if we do not adapt automation.
  # Uses the model by Hoeks and Levelt to simulate changes in the
  # size of the pupil for given changes in demand.
  # Then uses two moving averages inspired by Minadakis & Lohan (2018)
  # to keep track of long-term changes in the pupil size and short-term
  # changes in the pupil size.
  # The decision to increase/decrease the level of automation is based
  # on the deviation of the short-term trend from the long-term trend.
  # It is then assumed that an increase in automation
  # lowers the next spike in demand by a fixed constant for every iteration
  # during which the automation level exceeds 1. (the auto_1_effect and
  # auto_2_effect parameters control how much the next spike in demand is
  # lowered).
  # A new simulation is then scheduled taking into account the assumed effect on
  # the next spike in demand. This reflects what we would observe "online".
  
  # Simulate ground truth
  groundSim <- additiveSim(n_measurements = 100000)
  goundPeak <- groundSim$peaks
  groundDemand <- groundSim$demand
  groundPupil <- groundSim$pupil
  
  time <- groundSim$time
  
  
  # Stat collection for the averages (predictions are not really used)
  preds_short <- c()
  x_short <- c()
  
  preds_long <- c()
  x_long <- c()
  sd_long <- NULL
  
  # Variables hopefully changed by the automation system
  Y <- groundPupil
  current_demand <- groundDemand
  current_peak <- goundPeak
  next_demand_index <- 2
  
  # Changes in automation level
  inter_1_2 <- c()
  inter_2_3 <- c()
  inter_3_2 <- c()
  inter_2_1 <- c()
  
  # online automation level stats
  automation_level <- 1
  baseline_2 <- NULL
  
  for (i in 2:length(Y)) {
    
    # Calculate window averages 
    # Minadakis & Lohan (2018) do not use a windowed average
    # for the long trend. That assumes that the mean of
    # the pupil size remains stationary, which is unlikely. Thus
    # we use two window averages here and for the long-term just use
    # a very large window.
    x_short <- c(x_short,Y[i - 1])
    
    if (length(x_short) > short_term_window) {
      x_short <- x_short[2:(short_term_window + 1)]
    }
    
    x_long <- c(x_long,Y[i - 1])
    
    if (length(x_long) > long_term_window) {
      x_long <- x_long[2:(long_term_window + 1)]
    }
    
    # Perform prediction
    pred_short <- mean(x_short)
    pred_long <- mean(x_long)
    
    # Collect predictions
    preds_short <- c(preds_short,pred_short)
    preds_long <- c(preds_long,pred_long)
    
    # Keep track of what is the next spike in demand!
    if(time[i] %% 100 == 0) {
      next_demand_index <- next_demand_index + 1
    }
    
    # Calculate SD (the control flow here basically prevents
    # any intervention during the first 10 samples)
    if(length(x_long) > 10) {
      sd_long <- sd(x_long)
    } else {
      sd_long <- 1000
    }
    
    
    # Check if short-term demand exceeds long-term trend
    # Or: if automation level was already increased to 2
    # whether the short-term demand is still exceeding the
    # level at which the second level was activated.
    # Then go to level 3.
    
    # The third and fourth control statements handle decreases.
    # Basically, as soon as short-term demand is no longer significantly
    # exceeding any of the baselines (i.e. long-term demand or automation
    # level 2 baseline) decrease the automation level by 1.
    
    ## Handle increases
    if((automation_level == 1) &&
       (pred_short > (pred_long + (sd_decision * sd_long)))) {
      # increase automation level to two
      cat("increased from 1 to 2 at ",time[i],"\n")
      automation_level <- 2
      baseline_2 <- pred_short
      inter_1_2 <- c(inter_1_2,time[i])
    } else if((automation_level == 2) &&
              (pred_short > (baseline_2 + (sd_decision * sd_long)))) {
      # increase automation level to 3
      cat("increased from 2 to 3 at ",time[i],"\n")
      automation_level <- 3
      inter_2_3 <- c(inter_2_3,time[i])
    } else if((automation_level == 3) &&
              !(pred_short > (baseline_2 + (sd_decision * sd_long)))) {
      ## Handle decreases
      # decrease automation level to 2
      cat("decreased from 3 to 2 at ",time[i],"\n")
      automation_level <- 2
      inter_3_2 <- c(inter_3_2,time[i])
    } else if((automation_level == 2) &&
              !(pred_short > (pred_long + (sd_decision * sd_long)))) {
      # decrease automation level to 2
      cat("decreased from 2 to 1 at ",time[i],"\n")
      automation_level <- 1
      inter_2_1 <- c(inter_2_1,time[i])
    }
    
    # Affect of automation levels on demand
    # This is the biggest assumption:
    # an increase in automation lowers the next spike in demand
    # for each iteration during which the automation level is active
    # by a fixed constant.
    if (automation_level == 2) {
      current_demand[next_demand_index] <- current_demand[next_demand_index] -
                                            auto_1_effect
      current_demand[next_demand_index] <- max(0,current_demand[next_demand_index])
    } else if (automation_level == 3) {
      current_demand[next_demand_index] <- current_demand[next_demand_index] -
                                            auto_2_effect
      current_demand[next_demand_index] <- max(0,current_demand[next_demand_index])
    }
    
    # Re-calculate the simulation based on the assumed effect of the automation
    # level on the changes in demand.
    # These are basically the "online" measures.
    nextSim <- additiveSim(coef = current_demand,n_measurements = 100000)
    Y <- nextSim$pupil
    current_demand <- nextSim$demand
    current_peak <- nextSim$peaks
    
    # Save output
    png(filename = paste0(path_out,i,".png"),
        width = 960,height = 960)
    par(mfrow=c(2,1))
    plot(time,groundPupil,col="black",lwd=2,type="l",
         xlab="Time",ylab="Pupil size")
    lines(time[1:(i-1)],Y[1:(i-1)],col="blue",lwd=2)
    lines(time[1:(i-1)],preds_short,col="green",lwd=2,lty=2)
    lines(time[1:(i-1)],preds_long,col="red",lwd=2,lty=2)
    
    for (tp in inter_1_2) {
      abline(v=tp,col="pink",lty=2,lwd=2)
    }
    
    for (tp in inter_2_3) {
      abline(v=tp,col="purple",lty=2,lwd=2)
    }
    
    for (tp in inter_3_2) {
      abline(v=tp,col="orange",lty=2,lwd=2)
    }
    
    for (tp in inter_2_1) {
      abline(v=tp,col="yellow",lty=2,lwd=2)
    }
    
    plot(time,goundPeak,type="l",
         xlab="Time",ylab="Demand")
    lines(time[1:(i-1)],current_peak[1:(i-1)],col="red")
    
    for (tp in inter_1_2) {
      abline(v=tp,col="pink",lty=2,lwd=2)
    }
    
    for (tp in inter_2_3) {
      abline(v=tp,col="purple",lty=2,lwd=2)
    }
    
    for (tp in inter_3_2) {
      abline(v=tp,col="orange",lty=2,lwd=2)
    }
    
    for (tp in inter_2_1) {
      abline(v=tp,col="yellow",lty=2,lwd=2)
    }
    
    dev.off()
  }
  
  
}