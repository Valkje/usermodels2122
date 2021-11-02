# Maintaining a steady driver workload with adaptive automation

## Table of contents

1. [Introduction](#introduction)
2. [Hypotheses](#hypotheses)
3. [Experiment](#experiment-test-of-system)
4. [Levels of automation](#levels-of-automation)
5. [Assessments](#assessments)
6. [Application modules](#application-modules)
7. [References](#references)
8. [Answers to questions](#answers-to-questions)

## Introduction

This project concerns itself with the adaptive automation of a simulated driving task. Relying on an existing driving model implemented in ACT-R, we will research and develop a real-time adaptive automation system that will determine when the model should take over partial or full control of the driving task and when control should be handed back to the human driver. The adaptive automation system should attempt to maintain a stable driver mental workload, increasing the level of automation when the driver's mental workload becomes too high and decreasing the level of automation again when the driver's mental workload has decreased sufficiently. The applicability of adaptive automation as a method for controlling excessive mental workload is that by taking away (sub)tasks we redistribute (or reduce) the workload imposed on the driver, thereby reducing mental workload (Vidulich and Chang (2012)). The advantage of an adaptive automation system over a non-adaptive automation system is that by maintaining a sufficiently high level of mental workload we can prevent error due to poor situation awareness, which could result in a driver not properly responding to unexpected events.

## Hypotheses

We hypothesize that increases in automation will reduce the driver’s workload. Based on this we expect that adaptive automation will lead to a more stable mental workload over time. Additionally, we hypothesize that we will be observing an improvement in the performance on a secondary task due to adaptive automation since subjects might be able to focus more on that secondary task when the model is in (partial) control of the driving task. We also hypothesized that we would be able to observe an inprovement in human driving performance measures due to adaptive automation. However, we did not investigate this hypothesis further because of two reasons. First, due to a recording error we did not collect driving performance data during the experiment. More importantly however, investigating this hypothesis would have been complicated by the fact that driving performance metrics would reflect a mixture of the autonomous driving model's performance and the driver's performance (see Assessment).

## Experiment (Test of System)

To test these hypotheses, our experimental setup needs to account for multiple aspects: the participants, experiment design, and the type of follow-up analyses. For this experiment, we aim at having two experimental within-subjects conditions (aided vs. unaided): in the aided condition the adaptive automation system will be enabled, while in the unaided condition the driver remains in full control for the entire session. Additionally, we need to manipulate the load on the driver in both conditions to test whether our adaptive automation system takes over control in appropriate moments and how the increase in automation will impact the driver. To manipulate the load on the driver we will let the participants complete a secondary task: Solving multiplication problems. These problems will be presented to the participants with a text-to-speech synthesizer, so participants will still be able to watch the road, which allows us to continue to monitor changes in the size of the pupil. Participants can respond to the problems using their voice, and we will record their responses manually.

To create concrete switches between light and heavy mental workload, within each aided/unaided condition, periods in which the participant will be asked to solve multiplication problems are alternated with periods in which the participant is not asked to solve problems. More specifically, the experiment schedule looks as follows:

1. 5-minute introduction session where the experimental information is presented

2. 10-minute driving session (Session 1)

    1. 2.5-minute without algebra problems 
    2. 2.5-minute with algebra problems 
    3. 2.5-minute without algebra problems 
    4. 2.5-minute with algebra problems

3. 5-minute break

4. 10-minute driving session (Session 2)

    1. 2.5-minute without algebra problems
    2. 2.5-minute with algebra problems
    3. 2.5-minute without algebra problems
    4. 2.5-minute with algebra problems




To prevent any effect of specific multiplication problems on pupil size and/or driving performance, two different lists of multiplication problems will be created and counterbalanced across the participants. Similarly, to prevent any learning effects, we will also have to counterbalance the aided/unaided conditions across participants. This yields 2*2=4 different experiment run configurations, as detailed in the table below. This means that we will recruit a number of participants dividable by four, so every configuration will be associated with the same number of participants.

| Session 1 (automation condition) | Session 2 (automation condition) | Session 1 (multiplication list) | Session 2 (multiplication list) |
|----------------------------------|----------------------------------|---------------------------------|---------------------------------|
| Aided                            | Unaided                          | List 1                          | List 2                          |
| Aided                            | Unaided                          | List 2                          | List 1                          |
| Unaided                          | Aided                            | List 1                          | List 2                          |
| Unaided                          | Aided                            | List 2                          | List 1                          |

## Levels of Automation

In order to successfully drive a car, two distinct types of control need to be performed. The first type, called lateral control, concerns itself with the sideways motion of the vehicle, and thus with how well a car can keep in its designated lane, or switch lanes when necessary. A driver performs lateral control by turning the steering wheel to influence the direction of the car. The second type, called longitudinal control, concerns itself with the velocity of the vehicle and thus also with the distance to other vehicles, either in front or behind. A driver performs longitudinal control by pressing their foot on the gas or brake pedal to influence the speed of the vehicle.

These distinct types of control allow for various levels of automation. The lowest level of automation is when the driver is responsible for both types of control and is thus in full control of the vehicle (from now on referred to as no—automation). When the vehicle, or more specifically the model, takes over longitudinal control, we speak of partial automation. The model takes over longitudinal control, and not lateral, as this type of driver assistance is well-known, under the name cruise control, and participants will not have to get used to it. The highest level of automation is when the vehicle is responsible for both lateral and longitudinal control, we call this full automation.

The terminology used does differ from a well-known taxonomy of levels of automation as determined by a committee of the Society of Automotive Engineers (SAE) (ORAD committee, 2018). This includes monitoring of the vehicle and the environment under automated activity as a driver task and takes this into account when determining the level of automation. As we, for this project/experiment, assume that the model performs correctly, this supervision while relying on the vehicle for either or both types of control does not play a part in our definition, and is not even considered a task of the driver. The SAE would classify our definition of partial automation as Driver Assistance (level 1) and our definition of full automation as either Partial Driving Automation (level 2), Conditional Driving Automation (level 3), High Driving Automation (level 4), or Full Driving Automation (level 5) dependent on whether the driver can overrule the car's decisions, needs to supervise or has to be able/ready to intervene.


Currently, we have implemented three different levels of automation:

1. No automation, driver has full control.
2. Partial automation in the form of cruise control: The driver can still steer the vehicle, but the speed is being controlled by the ACT-R model.
3. Full automation, ACT-R controls both steering and speed.

## Assessments

For investigating our hypotheses and for monitoring the load on the driver online we need to quantify cognitive load. We need an online measure because the adaptive automation system should have access to a direct measure of the mental/cognitive load currently experienced by the user when deciding whether to increase or decrease the level of automation. We will rely on Pupil dilation as such an indicator of cognitive load (Kahneman, 1973). Thus, we will continuously measure the size of the driver’s pupil. How these measures of the driver's pupil size are utilized to decide on the appropriate level of automation is detailed in the "Decision mechanism" section.

As part of the final statistical analysis, we will investigate how the size of the pupil changes over time, following an increase in the level of automation. This will allow us to assess whether an adaptive automation system might allow to manipulate the mental workload imposed on a driver. Additionally, we will investigate how the pupil changes over time following a multiplication problem. This will allow us to assess whether a) our manipulation was effective in influencing mental workload and b) whether the pupil size following a question changed differently in the two conditions.

We were also interested in the performance on a secondary task: solving multiplication problems. This task theoretically permits two performance metrics that could be taken into account: response time and response accuracy. Since we scored the answers participants gave manually we opted to only record the latter. As part of the statistical analysis we will investigate whether subjects performed better, according to this metric, in the aided condition compared to the unaided condition.

Initially we also wanted to investigate driving performance metrics. We could have considered multiple measures such as the time it takes to change lanes, the lateral deviations from the middle of the lane, deviations from speed limits (based on Savino, 2009), reaction times, and gap acceptance (Papantoniou, Papadimitriou, and Yannis, 2017). However, just comparing any of these metrics between the two conditions (aided and unaided) would not be very informative, since the performance metrics recorded during the aided condition would reflect not just the driver's performance but also the autonomous system's performance. While we could have compared performance measures unrelated to the actions taken over by the model during partial-automation periods in the aided condition with the same measures in the unaided condition it would have been difficult to experimentally control for the influence other variables have on driving performance (e.g. is the driver currently solving a multiplication problem or not) during these periods. This would confound the interpretation of differences in performance.

In addition, Vidulich and Chang (2012) outline a number of reasons why secondary task performance assessment of mental workload could be preferred over primary task performance (e.g. the driving performance measures outlined in the previous paragraph). For (semi-)automated systems it can be difficult, if not impossible, to acquire primary task performance measures. Also, primary task performance may be influenced by other factors than mental workload, for example inter-individual differences in skill and exerted mental effort, and may thus not be diagnostic of mental workload. Under the right instructions (i.e. to prioritize the main task) the secondary task performance should be reflective of the spare mental capacity that a subject has while performing the primary task, and thus the level of mental workload.


## Application modules

Our application can be divided up into four major parts:

1. Eye-tracking (for the purpose of measuring pupil dilation).
2. Simulation measures of driving performance.
3. Decision mechanism that combines all measures and decides on a level of automation.
4. The implementation of the levels of automation.

### Eye-tracking

The adaptive automation decision system will depend on changes in the pupil size. Thus, we repeatedly query the pupil dilation from the eye-tracking device (EyeLink Portable Duo from SR research), which records at a sampling rate of 500 HZ. We only record the left eye. The recorded samples are written to an .EDF file for offline analysis but also made available online through the eye-tracker's "link".

These online samples are collected by Python, which also keeps track of updating all the necessary parts of the adaptive automation system that are necessary for the decision (see "Decision mechanism" section).

### Decision mechanism

Since we want to compute automation decisions online, our system has to be of low computational complexity. Mindakis and Lohan (2018) suggested that monitoring both long-term changes and short-term changes in the size of the pupil would allow for an online investigation of changes in cognitive load. They rely on moving averages of different window size to estimate the aforementioned long-term and short-term changes. Unfortunately, their actual decision mechanism is not formulated very explicitly: they "assume that, when the size of the pupil is larger than 70% of the maximum the cognitive load is high." (Mindakis & Lohan, 2018) but it is not immediately clear how the aforementioned trend estimates relate to the terms "maximum" and "size" in this sentence.

Nevertheless, we interpreted this as suggesting that increases in cognitive load can be detected by monitoring short-term deviations from the long-term trend, which would align well with similar systems that have been utilized in the past (e.g. Katidioti et al., 2016). These kind of systems, essentially engage in "novelty" or "anomaly" detection by evaluating whether short term changes in the size of the pupil are signifcantly higher (Mindakis & Lohan, 2018) or lower (Katidioti et al., 2016) than some long-term base-line, which is taken as an indicator of an increase or decrease in cognitive load (Mindakis & Lohan, 2018; Katidioti et al., 2016).

An advantage of these systems is that moving averages can be computed efficiently (see Python implementation). Furthermore, relying on a moving average not just for long-term trend estimation but also for short-term trend estimation, as was done by Mindakis and Lohan (2018), reduces the impact possible outliers in the recorded pupil sizes have on the decision system. However, relying on a singular maximum value for the decision threshold is unlikely to be robust to the dynamic changes of the environment (e.g. light pollution) when driving a car.

Therefore, we changed the decision mechanism to utilize more information. We were inspired by the moving window system described in Novacic & Tokhi (n.d.), which calculates the moving variance around a moving average of a continuously measured signal to estimate the expected degree of variation around that signal. The variance can then be multiplied by a fixed weight to define at what point a realization of the signal counts as outlier. While the weight is fixed in this system the variance itself remains adaptive (since it is also calculated for a moving window), ensuring that the system is more suitable for implementation in a car.

We adapted this system by Novacic & Tokhi (n.d.) to utilize two moving averages based on the choices by Mindakis and Lohan (2018), resulting in the following algorithm:

- Define initial values for weight, window_size_short_term, window_size_long_term, window_size_variance, lock_duration
- set automationLevel to 1


- while recording:
    - get newest pupil size
    - if newest pupil size is not zero:
        - update short_term_trend
        - update long_term_trend
        - calculate difference between short_term_trend and long_term_trend
        - update variance with difference
    
    - if short_term_trend >= long_term_trend + weight * sqrt(variance):
        - schedule automation increase
    - if short_term_trend <= long_term_trend and automation_level > 1:
        - schedule automation increase

This routine will propose an increase in automation, should the short term trend exceed the decision boundary and will propose a decrease in automation as soon as the short term trend returns to the long term trend. In our implementation we further prevent this system from scheduling a change in automation for a fixed period (used to warn the driver about the upcoming change) after every change. This automatically ensures that, should any of the decision conditions again be met after this lockdown, the system will then propose to further increase or decrease the level of automation.


### Implementation of automation levels (perhaps name this Interface of the adaptive automation system?)

We use the existing ACT-R model to automate the driving process. Since we sometimes want to use no or only partial automation, certain parts of the ACT-R model are sometimes ignored when controlling the car. Previously, we worried that ACT-R might not be able to adapt to situations in which its output is ignored, but as it turns out, the ACT-R model has no problem recovering from the no and partial automation levels.

Information regarding the adaptive automation system is presented to the driver using a head-up display (HUD) on top of the dashboard. The head-up display presents information on a transparent material in the line of sight of the driver such that they do not have to take their eyes of the road to perceive the information. On the left and middle parts of the HUD the automation level is displayed by a text string stating _DRIVER CONTROL_ indicating no automation, _CRUISE CONTROL_ indicating partial automation, or _PASSENGER MODE_ indicating full automation. In addition to the level of automation, the HUD also displays the current speed limit if the driver deviates more from that limit by more than 10%. We added this because at higher speeds, we found that it is sometimes (near) impossible to read the speed signs.

![image](https://user-images.githubusercontent.com/45287198/139838439-35b31ffe-a4f6-4405-bf77-29f45b58d3f6.png)

## References

Papantoniou, P., Papadimitriou, E., & Yannis, G. (2017). Review of driving performance parameters critical for distracted driving research. Transportation Research Procedia, 25, 1796–1805.

On-Road Automated Driving (ORAD) committee (2018). Taxonomy and Definition for Terms Related to Driving Automation System for On-Road Motor Vehicles. Available from https://doi.org/10.4271/J3016_201609

Hoeks, B., Levelt, W. (1993) Pupillary dilation as a measure of attention: A quantitative system analysis

Mindakis, G., Lohan, K. (2018) Using Pupil Diameter to Measure Cognitive Load

Savino M. R. (2009) Standardized Names and Definitions for Driving Performance Measures

Kahneman, D. (1973) Attention and Effort

Vidulich, M.A. and Tsang, P.S. (2012). Mental Workload and Situation Awareness. In Handbook of Human Factors and Ergonomics, G. Salvendy (Ed.). https://doi.org/10.1002/9781118131350.ch8

Katidioti, I., Borst, J. P., Bierens de Haan, D. J., Pepping, T., van Vugt, M. K., & Taatgen, N. A. (2016). Interrupted by Your Pupil: An Interruption Management System Based on Pupil Dilation. International Journal of Human–Computer Interaction, 32(10), 791–801. https://doi.org/10.1080/10447318.2016.1198525

Novacic, J., & Tokhi, K. (n.d.). Implementation of Anomaly Detection on a Time-series Temperature Data set. 47.


## Answers to questions

**Gilles' question**:

Good question! We want to measure pupil dilation as a proxy for cognitive load, to determine whether the driver might be needing some driving assistance. However, the term ‘cognitive load’ is poorly defined, and could be interpreted as 1) increased competition for specific resources or 2) increased usage of cognitive resources overall (but without increased competition). Whatever the interpretation, we think we can ensure that pupil dilation changes with our secondary task. We will argue why we think so for both interpretations of cognitive load:

1) It is true that the secondary task we have planned (answering questions about an English listening test) uses a different perception modality (hearing) than the primary task (vision), but that does not mean that the different tasks do not draw on the same resources. While driving, participants need to focus on the approaching speed sign to read its contents (which is actually not that trivial) and then remember the speed limit they just saw. While listening to the audio file, they need to concentrate on its contents and remember it to properly answer the questions we will ask them. Attention and declarative/working memory should therefore be used in both tasks and cause some interference between the two, which should increase cognitive load according to definition 1) and therefore be reflected in the pupil size.

2) Increased demands on cognitive control alone, which includes task switching, already increases the pupil size (van der Wel & van Steenbergen, 2018). As long as the participants have not mastered either of the tasks to the degree that they can perform it fully automatically (which we believe they will not be able to do in the time frame of the experiment), doing the two tasks together should increase the pupil size because they will need to switch between them regularly.

Finally, if the current secondary task does turn out to be too easy and barely change pupil size, we will definitely consider using the n-back task (perhaps even in combination with the listening task).

PS Thanks for your offer to help! We will keep it in mind :)

*References*

van der Wel P, van Steenbergen H. Pupil dilation as an index of effort in cognitive control tasks: A review. Psychon Bull Rev. 2018 Dec;25(6):2005-2015. doi: 10.3758/s13423-018-1432-y. PMID: 29435963; PMCID: PMC6267528.

**Leander's question**:

Thank you for your questions! With regard to your first consideration: In an earlier draft of our experiment setup we, among ourselves, also discussed having a short practice session, and this indeed might still be a good idea. As of this week, we have actually played around with the driving simulator using a steering wheel and pedals and we found that performing a 0-back task (i.e. keeping the current speed limit) is not all that difficult. 2-back however was quite challenging. Considering that the 0-back task was not that difficult, and that most people of our age have some experience driving, we do not think that there will be a very large increase in skill between (and also within) sessions. More importantly, the ordering of the aided and unaided sessions will be opposite for half of the subjects, thereby we hope to control for any learning effect.

About your second concern: We are pretty certain that there is a relation between pupil dilation and driver workload (Marquart, Cabrall, & de Winter, 2015), as well as a relation between workload and performance (Bruggen, 2015). Workload and performance are not the same, however: Although performance can decrease with too much workload, it can also decrease with too little workload (again, see Bruggen (2015)). There indeed remains the question of how to set the sensitivity of the system. Following the method of working with short and long term moving averages, as described in our wiki, we change the level when the difference (either positively or negatively) between the short and long term trend is larger than K * (standard error of the long-term trend). We can use this scalar K to set the sensitivity of our system. This will probably be done by trial-and-error. We essentially do not want the system to continuously switch when the driver is not performing a secondary task.

*References*

Marquart G., Cabrall C., de Winter J. (2015), “Review of Eye-related Measures of Drivers’ Mental Workload”, Procedia Manufacturing. Vol 3, pp. 2854-2861. https://doi.org/10.1016/j.promfg.2015.07.783.

Bruggen, A. (2015), "An empirical investigation of the relationship between workload and performance", Management Decision, Vol. 53 No. 10, pp. 2377-2389. https://doi.org/10.1108/MD-02-2015-0063

