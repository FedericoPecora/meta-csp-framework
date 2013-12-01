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
#   Requirement                                                 #
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
 (Requirement req1 Location::Kitchen())
 (Requirement req2 Stove::On())
 (Requirement req3 Robot::SayWarning())
 (Constraint During(Head,req1))
 (Constraint Contains(Head,req2))
 (Constraint Overlaps(Head,req3))
)

(SimpleOperator
 (Head Human::Eating())
 (Requirement req1 Location::DiningRoom())
 (Requirement req2 Human::Cooking())
 (Constraint Finishes(Head,req1))
 (Constraint After(Head,req2))
)

(SimpleOperator
 (Head Robot::SayWarning())
 (Requirement req1 Robot::MoveTo())
 (Constraint MetBy(Head,req1))
 (Constraint Duration[5,INF](Head))
)

(SimpleOperator
 (Head Robot::MoveTo())
 (Requirement req1 LocalizationService::Localization())
 (Constraint During(Head,req1))
 (Constraint Duration[5,INF](Head))
)

(SimpleOperator
 (Head LocalizationService::Localization())
 (Requirement req1 RFIDReader::On(power,usbport))
 (Constraint During(Head,req1)) 
)

(SimpleOperator
 (Head LocalizationService::Localization())
 (Requirement req1 LaserScanner::On(power,serialport))
 (Constraint During(Head,req1)) 
)

(SimpleOperator
 (Head RFIDReader::On(power,usbport))
 (Requirement power(5))
 (Requirement usbport(7))
)

(SimpleOperator
 (Head LaserScanner::On())
 (RequiredResource serialport(1))
 (Requirement power(5))
)

=================
(Operator Robot::MoveTo(?from,?to)
 (Precondition req1 Robot::At(?from)) #copy4
 (Precondition req2 LocalizationService::On()) #copy1
 (Effect req3 Robot::At(?to)) #copy6
 (Effect req4 Robot::Ready()) #copy8
 (Constraint During(Head,req2)) #copy2
 (Constraint Duration[5,INF](Head)) #copy3
 (Constraint Overlaps(req1,Head)) #copy5
 (Constraint Overlaps(Head,req3)) #copy7
 (Constraint Meets(Head,req4)) #copy9
)

==>

(SimpleOperator
 (Head Robot::MoveTo(?from,?to))
 (Requirement req2 LocalizationService::Localization()) #copy1
 (Requirement req1 Robot::At(?from)) #copy4
 (Constraint During(Head,req2)) #copy2
 (Constraint Duration[5,INF](Head)) #copy3
 (Constraint Overlaps(req1,Head)) #copy5
)

(SimpleOperator
 (Head Robot::At(?to)) #copy6
 (Requirement req0 Robot::MoveTo(?from,?to)) #NEW
 (Constraint Overlaps(req0,Head)) #copy7
)

(SimpleOperator
 (Head Robot::Ready()) #copy8
 (Requirement req0 Robot::MoveTo(?from,?to)) #NEW
 (Constraint Meets(req0,Head)) #copy9
)
=================