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

(SimpleDomain TestDomain)

(Resource power 6)
(Resource usbport 6)
(Resource serialport 6)

(SimpleOperator
 (Head Robot1::MoveTo())
 (RequiredState req1 LocalizationService::Localization())
 (Constraint During(Head,req1))
 (Constraint Duration[5,INF](Head))
)

(SimpleOperator
 (Head Robot2::MoveTo())
 (RequiredState req1 LocalizationService::Localization())
 (Constraint Duration[5,INF](Head))
 (Constraint During(Head,req1))
)

(SimpleOperator
 (Head LocalizationService::Localization())
 (RequiredState req1 RFIDReader1::On(power,usbport))
 (Constraint During(Head,req1)) 
)

(SimpleOperator
 (Head LocalizationService::Localization())
 (RequiredState req1 LaserScanner1::On(power,serialport))
 (Constraint During(Head,req1)) 
)

(SimpleOperator
 (Head RFIDReader1::On(power,usbport))
 (RequiredResource power(5))
 (RequiredResource usbport(7))
)

(SimpleOperator
 (Head LaserScanner1::On(power,serialport))
 (RequiredResource serialport(1))
 (RequiredResource power(5))
)
