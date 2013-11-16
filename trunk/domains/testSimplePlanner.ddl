##################
# Reserved words #
#################################################################
#                                                               #
#   Head                                                        #
#   Resource                                                    #
#   SimpleOperator                                              #
#   SimpleDomain                                                #
#   Constraint                                                  #
#   Requirement                                                 #
#   All AllenIntervalConstraint types                           #
#   '[' and ']' should be used only for constraint bounds       #
#   '(' and ')' are used for parsing                            #
#                                                               #
#################################################################

(SimpleDomain TestDomain)

(Resource power 6)
(Resource usbport 6)
(Resource serialport 6)

(SimpleOperator
 (Head Robot1::MoveTo())
 (Requirement req1 LocalizationService::Localization())
 (Constraint During(Head,req1))
 (Constraint Duration[5,INF](Head))
)

(SimpleOperator
 (Head Robot2::MoveTo())
 (Requirement req1 LocalizationService::Localization())
 (Constraint Duration[5,INF](Head))
 (Constraint During(Head,req1))
)

(SimpleOperator
 (Head LocalizationService::Localization())
 (Requirement req1 RFIDReader1::On(power,usbport))
 (Constraint During(Head,req1)) 
)

(SimpleOperator
 (Head LocalizationService::Localization())
 (Requirement req1 LaserScanner1::On(power,serialport))
 (Constraint During(Head,req1)) 
)

(SimpleOperator
 (Head RFIDReader1::On(power,usbport))
 (Requirement power(5))
 (Requirement usbport(7))
)

(SimpleOperator
 (Head LaserScanner1::On(power,serialport))
 (Requirement serialport(1))
 (Requirement power(5))
)
