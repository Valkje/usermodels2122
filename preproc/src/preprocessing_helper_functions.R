require(PupilPre)
require(eyelinker)

asc_to_ppl_pre <- function(path_to_file,
                           label,
                           recorded_eye){
  # pupilPre's ppl_prep_data expects a sample report from the eye-tracker.
  # this function is designed to get the content of a .asc file into the
  # same format that ppl_prep_data would expect, so that ppl_prep_data and
  # all the other pupilPre functions can then be utilized as much as possible.
  
  # Thus this function is in large parts based on code from the ppl_prep_data
  # function available here:
  # https://github.com/cran/PupilPre/blob/master/R/process.R
  
  
  # Additional code parts were taken from the eyelinker vignette to align
  # blinks and saccaded. This vignette can be found here:
  # https://cran.r-project.org/web/packages/eyelinker/vignettes/basics.html
  
  # Required libraries:
  # eyelinker and PupilPre
  
  # First, read in .asc file and calculate length of samples
  asc_dat <- read.asc(path_to_file)
  n_samples <- length(asc_dat$raw$time)
  
  # Align blinks and saccaded to long format as described in the eyelinker
  # vignette. Basically, the stime and etime variables here contain
  # start and end time stamps for the corresponding events (saccade or blink)
  SAC <- cbind(asc_dat$sacc$stime,asc_dat$sacc$etime)
  asc_dat$raw <- mutate(asc_dat$raw, saccade = time %In% SAC)
  BLINK <- cbind(asc_dat$blinks$stime, asc_dat$blinks$etime)
  asc_dat$raw <- mutate(asc_dat$raw, blink = time %In% BLINK)
  
  # Remove by-products to keep memory footprint small
  rm(BLINK)
  rm(SAC)
  
  # Now align messages into long format. Since reception of messages is
  # not necessarily aligned with the sampling rate we might end up with
  # messages received at an odd number of millisecconds. These are here
  # always attributed to the next sample.
  
  asc_dat$raw$msg <- NA
  # get matching messages
  matched <- asc_dat$msg[asc_dat$msg$time %in% asc_dat$raw$time,]
  # get unmatched (odd ms reception stamp)
  unmatched <- asc_dat$msg[!(asc_dat$msg$time %in% asc_dat$raw$time),]
  # adjust unmatched stamps
  unmatched$time <- unmatched$time + 1
  
  if(length(unmatched$time[!(unmatched$time %in% asc_dat$raw$time)])) {
    stop("Problematic messages.\n")
  }
  
  # Now we write both, the matched and unmatched into the correct row.
  # Sometimes two messages end up with the same time-stamp, if that
  # happens they are merged with a ',' inbetween.
  combined_msgs_aligned <- rbind(matched,unmatched)
  
  # Remove by-products to keep memory footprint small
  rm(matched)
  rm(unmatched)
  
  for(msg_index in 1:nrow(combined_msgs_aligned)) {
    text_msg <- combined_msgs_aligned$text[msg_index]
    time_msg <- combined_msgs_aligned$time[msg_index]
    if(is.na(asc_dat$raw$msg[asc_dat$raw$time == time_msg])){
      asc_dat$raw$msg[asc_dat$raw$time == time_msg] <- text_msg
    } else {
      # already msg here so add with a , in between
      merged_msg <- paste0(asc_dat$raw$msg[asc_dat$raw$time == time_msg],
                           ",",
                           text_msg)
      asc_dat$raw$msg[asc_dat$raw$time == time_msg] <- merged_msg
    }
  }
  
  # Remove by-products to keep memory footprint small
  rm(combined_msgs_aligned)
  
  # Now we can get a data-frame with all the columns that ppl_prep_data
  # would expect. This needs to contain all the necessary columns
  # included in the original ppl_prep_data function. Here also two
  # optional ones (EYE_TRACKED and SAMPLE_MESSAGE) are added as well.
  # See: https://github.com/cran/PupilPre/blob/master/R/process.R
  
  data_ppl_pre <- data.frame(
    "RECORDING_SESSION_LABEL"= as.factor(rep(label,length.out=n_samples)),
    "LEFT_PUPIL_SIZE" = asc_dat$raw$ps,
    "LEFT_GAZE_X" = asc_dat$raw$xp,
    "LEFT_GAZE_Y" = asc_dat$raw$yp,
    "LEFT_IN_BLINK" = as.numeric(asc_dat$raw$blink),
    "LEFT_IN_SACCADE" = as.numeric(asc_dat$raw$saccade),
    "LEFT_ACCELERATION_X" = rep(NA,length.out=n_samples),
    "LEFT_ACCELERATION_Y" = rep(NA,length.out=n_samples),
    "LEFT_VELOCITY_X" = rep(NA,length.out=n_samples),
    "LEFT_VELOCITY_Y" = rep(NA,length.out=n_samples),
    "RIGHT_PUPIL_SIZE" = rep(NA,length.out=n_samples),
    "RIGHT_GAZE_X" = rep(NA,length.out=n_samples),
    "RIGHT_GAZE_Y" = rep(NA,length.out=n_samples),
    "RIGHT_IN_BLINK" = rep(NA,length.out=n_samples),
    "RIGHT_IN_SACCADE" = rep(NA,length.out=n_samples),
    "RIGHT_ACCELERATION_X" = rep(NA,length.out=n_samples),
    "RIGHT_ACCELERATION_Y" = rep(NA,length.out=n_samples),
    "RIGHT_VELOCITY_X" = rep(NA,length.out=n_samples),
    "RIGHT_VELOCITY_Y" = rep(NA,length.out=n_samples),
    "TIMESTAMP" = asc_dat$raw$time,
    "TRIAL_INDEX" = asc_dat$raw$block,
    "EYE_TRACKED" = rep("Left",length.out=n_samples),
    "SAMPLE_MESSAGE" = asc_dat$raw$msg
  )
  
  # And finally, ppl_prep_data can be called! The RECORDING_SESSION_LABEL
  # column will be renamed to subject. This column should then contain only
  # the label value passed as argument to this function.
  sample_ppl_prepped <- ppl_prep_data(data_ppl_pre, Subject = "RECORDING_SESSION_LABEL")
  
  return(sample_ppl_prepped)
}

safe_apply_manual_clean <- function(data_used_for_manual_clean,
                                    path_to_log) {
  
  
  # I encountered some bugs when using the clean app by pupilpre. Specifically,
  # the log file would not contain a value for every event in the data
  # passed to the user_cleanup_app() function. Thus, the apply_user_cleanup()
  # function from pupilpre crashed due to an out of bounds error.
  # This method checks whether the dimensions match between the
  # data_used_for_manual_clean and the stored log file. If everything is fine
  # it just uses apply_user_cleanup() otherwise it sets samples to NA manually.
  # The manual cleaning does not clean the gaze as done by apply_user_cleanup()
  # since we don't need that info. The columns are re-maned as is done by the
  # apply_user_cleanup() function in the manual case. So the code here
  # again is based a lot on pupilpre's original function to ensure that
  # functionality later in the pipeline remains unaffected.
  
  # see: https://github.com/cran/PupilPre/blob/master/R/process.R
  
  # Required libraries:
  # PupilPre
  
  
  # Load in log file
  logFile <- readRDS(path_to_log)
  
  # Compare length of log file to unique Events
  lengthLog <- length(names(logFile))
  lengthuniqueEvents <- length(unique(as.character(data_used_for_manual_clean$Event)))
  
  if(lengthLog == lengthuniqueEvents){
    manually_cleaned_data <- apply_user_cleanup(data=data_used_for_manual_clean,
                                                LogFile=path_to_log)
  } else {
    print("Events != length(logFile)")
    
    # Prepare manual clean up
    # basically replicate the steps taken in the apply_user_cleanup() function
    manually_cleaned_data <- data_for_manual_clean
    
    manually_cleaned_data$PupilClean <- data_used_for_manual_clean$Pupil
    
    for (event in names(logFile)) {
      # Get the value for an event. If it is NA just continue,
      # else replace all cleaned pupil values in manually_cleaned_data
      # at the time-stamps in to_be_replaced with NA
      to_be_replaced <- logFile[names(logFile) == event]
      
      if(!is.na(to_be_replaced)){
        # Plot for use to see that everything works as intended
        plot(data_used_for_manual_clean$Time[data_used_for_manual_clean$Event == as.numeric(event)],
             data_used_for_manual_clean$Pupil[data_used_for_manual_clean$Event == as.numeric(event)],
             type="l",
             ylab= "Pupil",
             xlab = "Time",
             main= event)
        
        manually_cleaned_data$PupilClean[manually_cleaned_data$Time %in%
                                           as.numeric(unlist(to_be_replaced))] <- NA
        
        lines(manually_cleaned_data$Time[manually_cleaned_data$Event == as.numeric(event)],
              manually_cleaned_data$PupilClean[manually_cleaned_data$Event == as.numeric(event)],
              col="red",lty=2)
      }
    }
    
    # Assign column names as done normally in apply_user_cleanup()
    manually_cleaned_data$PupilPrevious <- manually_cleaned_data$Pupil
    manually_cleaned_data$Pupil <- manually_cleaned_data$PupilClean
  }
  return(manually_cleaned_data)
}

extract_samples_before_after_msg <- function(data_from_final_clean,
                                             re_msg_for_split,
                                             pre_msg_sample_range,
                                             post_msg_sample_range,
                                             use_msg_for_label=T,
                                             split_msg_by_block=F,
                                             split_msg_decision=0,
                                             check_pre_msg=F,
                                             check_pre_msg_val=NULL,
                                             check_pre_msg_inval=NULL){
  
  # This function is designed to take the data-frame obtained from
  # safe_apply_manual_clean() or apply_user_cleanup() and to create a new
  # data-frame that contains new events, one for each msg that starts with the
  # string passed to re_msg_for_split. Each of those events
  # contains pre_msg_sample_range samples from before the message was received
  # and post_msg_sample_range samples from after the message was received.
  # Time-stamps are not manipulated here! This should be done by a follow up call
  # to align_msg() from the pupilpre package. Therefore, these columns are
  # also dropped.
  # Also allows to check whether the samples extracted like this happened
  # after some special event that was written to the tracker's messages if
  # check_pre_msg is set to True. In that case if prior to the first time-point
  # from which we collect samples no check_pre_msg_inval was sent and a 
  # check_pre_msg_val was received previously, this collection of samples will
  # be marked with val_check <- 1.
  
  # The aling_msg() function in pupilPre is  from VWPre
  # see: https://github.com/cran/VWPre/blob/master/R/formatting.R
  # so I will just link that repository here as well, if you want to check
  # that function.
  
  # The events are defined either as the cummulative number of messages detected
  # or if split_msg_by_block is true and split_msg_decision > 0 as:
  # "split_msg.msg_counter"
  # with split_msg increasing after every split_msg_decision messages have been
  # parsed.
  
  # Required libraries:
  # PupilPre
  
  #ToDO:
  # In the future checking for regular expressions might be better instead of
  # just accepting startswith.
  
  if(split_msg_by_block && (split_msg_decision <= 0)) {
    stop("If split_msg_by_block == TRUE, then split_msg_decision should be > 0.")
  }
  
  # Declare variable that will contain the new data-frame
  extracted_dat <- NULL
  
  # Initialize the msg counters optionally used for the split_msg_decision
  msg_counter <- 1
  split_msg <- 1
  
  # Collect all parsed messages in case a check_pre_msg should be performed
  message_history <- c()
  total_checks <- 0
  valid_checks <- 0
  
  # Extract data following a message starting with re_msg_for_split:
  for(row_index in 1:nrow(data_from_final_clean)){
    
    # Skip NA messages
    if(!is.na(data_from_final_clean$SAMPLE_MESSAGE[row_index])){
      
      msg_txt <- data_from_final_clean$SAMPLE_MESSAGE[row_index]
      msg_time <- data_from_final_clean$Time[row_index]
      
      # If there is a message, check whether it meets the pattern.
      if(startsWith(as.character(msg_txt),re_msg_for_split)) {
        
        # Extract the sample range around the message
        startR <- row_index - pre_msg_sample_range
        endR <- row_index + post_msg_sample_range
        msg_dat <- data_from_final_clean[seq(from=startR,to=endR,by=1),]
        
        # Print message that was used for cut
        print(msg_dat$SAMPLE_MESSAGE[(1 - pre_msg_sample_range)])
        
        # Rewrite the appropriate columns
        msg_dat$TRIAL_INDEX <- msg_counter
        
        # Event is a factor, so first set all to NA and then set to string
        msg_dat$Event <- NA
        
        if(split_msg_by_block){
          msg_dat$Event <- paste0(split_msg,".",msg_counter)
        } else {
          msg_dat$Event <- as.character(msg_counter)
        }
        
        # Optionally perform a check_pre_msg check
        if(check_pre_msg) {
          val_check <- 0
          total_checks <- total_checks + 1
          # Did we already encounter previous messages?
          if(length(message_history) > 0) {
            # Now iterate over messages backwards
            rev_msg_history <- rev(message_history)
            
            for(prev_msg in rev_msg_history) {
              if(startsWith(prev_msg,check_pre_msg_val)) {
                cat("Does meet check because of previous: ",prev_msg,"\n")
                valid_checks <- valid_checks + 1
                val_check <- 1
                break
              } else if(startsWith(prev_msg,check_pre_msg_inval)) {
                cat("Does not meet check because of previous: ",prev_msg,"\n")
                break
              }
            }
          }
          msg_dat$val_check <- val_check
        }
        
        # Attach current event to data-frame
        extracted_dat <- rbind(extracted_dat,msg_dat)
        
        # Update counters
        msg_counter <- msg_counter + 1
        if(split_msg_by_block){
          if(msg_counter > split_msg_decision) {
            msg_counter <- 1
            split_msg <- 2
          }
        }
      }
      
      # Collect last message
      message_history <- c(message_history,as.character(msg_txt))
    }
  }
  
  # Assign check columns
  extracted_dat$valid_checks <- valid_checks
  extracted_dat$total_checks <- total_checks
  # Set event data and sample messages to factor again
  extracted_dat$Event <- as.factor(extracted_dat$Event)
  extracted_dat$SAMPLE_MESSAGE <- factor(extracted_dat$SAMPLE_MESSAGE)
  # Drop align and Time columns, since these are no longer valid!
  extracted_dat <- extracted_dat[,!(colnames(extracted_dat) %in% c("Align","Time"))]
  # Drop all levels just in case we forgot something..
  extracted_dat <- droplevels(extracted_dat)
  return(extracted_dat)
}