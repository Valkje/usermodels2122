additiveSim <- function(coef=NULL,n=10.1,t_max=930,f=1/(10^24),
                        n_measurements=10000,sd_dev=5,seed=0){
  # Simulates changes in the pupil size over time
  # based on the Hoeks & Levelt pupil response function.
  # Uses convolution steps described by Wierda et al. (2012)
  set.seed(seed)
  # Response function from Hoeks and Levelt
  # + scale parameter introduced by Wierda et al.
  h_pupil <- function(t,n,t_max,f)
    # n+1 = number of laters
    # t_max = response maximum
    # f = scaling factor
  {
    h<-f*(t^n)*exp(-n*t/t_max)
    h[0] <- 0
    h
  }
  
  # Convolution and spike setup code below taken from
  # supplements provided by:
  # https://www.pnas.org/content/109/22/8456
  
  # Simulation parameters
  time <- seq(0,n_measurements,by=20)
  last_pulse <- length(time) - (5 * round(t_max/100)) - 1
  
  # Define pulse location
  pulse_locations <- seq(1,last_pulse,5)
  real_locations <- time[pulse_locations]
  
  if(is.null(coef)){
    # Ground truth demand
    coef <- runif(length(pulse_locations),max=0.1) # Some demand everywhere
    
    # Add demanding episodes
    for (i in 1:15) {
      startInd <- sample(1:(length(coef)-min(101, length(coef))),1)
      endInd <- startInd + 100
      coef[startInd:endInd] <- coef[startInd:endInd] + 0.1
    }
    
  }
  
  peaks <- rep(0,length(time))
  peaks[pulse_locations] <- coef
  
  
  # Ground truth pupil response
  hc <- h_pupil(time,n,t_max,f)
  o <- convolve(peaks,rev(hc),type="open")
  o_restr <- o[1:length(time)]
  
  # Noise
  o_restr <- o_restr + rnorm(length(o_restr),sd=sd_dev)
  
  return(list("demand"=coef,"peaks"=peaks,"pupil"=o_restr,"time"=time))
}
