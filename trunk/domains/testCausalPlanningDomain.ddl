##################
# Reserved words #
#################################################################
#                                                               #
#   Head                                                        #
#   Resource                                                    #
#   Sensor                                                      #
#   ContextVariable                                             #
#   SimpleOperator                                              #
#   PlanningOperator                                            #
#   SimpleDomain                                                #
#   Constraint                                                  #
#   RequiredState						#
#   AchievedState						#
#   RequriedResoruce						#
#   All AllenIntervalConstraint types                           #
#   '[' and ']' should be used only for constraint bounds       #
#   '(' and ')' are used for parsing                            #
#                                                               #
#################################################################

(PlanningDomain TestCausalPlanning)

(Resource power 6)
(Resource usbport 6)
(Resource serialport 6)

(PlanningOperator
 (Head Robot::SayWarning(?location))
 (RequiredState req1 Robot::At(?location))
 (Constraint During(Head,req1))
 (Constraint Duration[5000,INF](Head))
)

(PlanningOperator
 (Head Robot::MoveTo(?from,?to))
 (RequiredState req1 LocalizationService::Localization())
 (RequiredState req2 Robot::At(?from))
 (AchievedState ach1 Robot::At(?to))
 (Constraint During(Head,req1))
# (Constraint OverlappedBy(Head,req2))
 (Constraint Overlaps(req2,Head))
 (Constraint Overlaps(Head,ach1))
 (Constraint Duration[5000,INF](Head))
)

(PlanningOperator
 (Head LocalizationService::Localization())
 (RequiredState req1 RFIDReader::On())
 (Constraint During(Head,req1)) 
)

(PlanningOperator
 (Head LocalizationService::Localization())
 (RequiredState req1 LaserScanner::On())
 (Constraint During(Head,req1))
)

(PlanningOperator
 (Head RFIDReader::On())
 (RequiredResource power(5))
 (RequiredResource usbport(7))
)

(PlanningOperator
 (Head LaserScanner::On())
 (RequiredResource serialport(1))
 (RequiredResource power(5))
)
