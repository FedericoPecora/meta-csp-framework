##################
# Reserved words #
#################################################################
#                                                               #
#   Head                                                        #
#   Resource                                                    #
#   Sensor                                                      #
#   ContextVariable                                             #
#   SimpleOperator                                              #
#   SimpleDomain                                                #
#   Constraint                                                  #
#   RequiredState						#
#   RequriedResoruce						#
#   All AllenIntervalConstraint types                           #
#   '[' and ']' should be used only for constraint bounds       #
#   '(' and ')' are used for parsing                            #
#                                                               #
#################################################################

(SimpleDomain TestProactivePlanning)

(Sensor Location)
(Sensor Stove)

(ContextVariable Human)

(Resource power 6)
(Resource usbport 6)
(Resource serialport 6)

(SimpleOperator
 (Head Human::Cooking())
 (RequiredState req1 Location::Kitchen())
 (RequiredState req2 Stove::On())
 (RequiredState req3 Robot::SayWarning())
 (Constraint During(Head,req1))
 (Constraint Contains(Head,req2))
 (Constraint Overlaps(Head,req3))
)

(SimpleOperator
 (Head Human::Eating())
 (RequiredState req1 Location::DiningRoom())
 (RequiredState req2 Human::Cooking())
 (Constraint Finishes(Head,req1))
 (Constraint After(Head,req2))
)

(SimpleOperator
 (Head Robot::SayWarning())
 (RequiredState req1 Robot::MoveTo())
 (Constraint MetBy(Head,req1))
 (Constraint Duration[5,INF](Head))
)

(SimpleOperator
 (Head Robot::MoveTo())
 (RequiredState req1 LocalizationService::Localization())
 (Constraint During(Head,req1))
 (Constraint Duration[5,INF](Head))
)

(SimpleOperator
 (Head LocalizationService::Localization())
 (RequiredState req1 RFIDReader::On(power,usbport))
 (Constraint During(Head,req1)) 
)

(SimpleOperator
 (Head LocalizationService::Localization())
 (RequiredState req1 LaserScanner::On(power,serialport))
 (Constraint During(Head,req1)) 
)

(SimpleOperator
 (Head RFIDReader::On(power,usbport))
 (RequiredResource power(5))
 (RequiredResource usbport(7))
)

(SimpleOperator
 (Head LaserScanner::On(power,serialport))
 (RequiredResource serialport(1))
 (RequiredResource power(5))
)
