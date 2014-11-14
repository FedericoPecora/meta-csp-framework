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
#   RequiredState												#
#   RequriedResoruce											#
#   All AllenIntervalConstraint types                           #
#   '[' and ']' should be used only for constraint bounds       #
#   '(' and ')' are used for parsing                            #
#                                                               #
#################################################################

(Domain TestDom)

(Resource power 5)
(Resource usbport 6)
(Resource serialport 1)

(Actuator Robot1)
(Actuator Robot2)
(Actuator LocalizationService)
(Actuator RFIDReader1)
(Actuator LaserScanner1)

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
 (RequiredState req1 RFIDReader1::On())
 (Constraint During(Head,req1)) 
)

(SimpleOperator
 (Head LocalizationService::Localization())
 (RequiredState req1 LaserScanner1::On())
 (Constraint During(Head,req1)) 
)

(SimpleOperator
 (Head RFIDReader1::On())
 (RequiredResource power(5))
 (RequiredResource usbport(7))
)

(SimpleOperator
 (Head LaserScanner1::On())
 (RequiredResource serialport(1))
 (RequiredResource power(5))
)
