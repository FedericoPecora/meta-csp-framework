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
#   Domain                                                      #
#   Constraint                                                  #
#   RequiredState												#
#   AchievedState                                               #
#   RequiredResource											#
#   All AllenIntervalConstraint types                           #
#   '[' and ']' should be used only for constraint bounds       #
#   '(' and ')' are used for parsing                            #
#                                                               #
#################################################################

(Domain TestDom)

(Resource arm 1)

(Actuator Robot1)

#(Sensor OnTable)

(PlanningOperator
 (Head Robot1::Place())
 (RequiredState req1 OnTable::false)
 (AchievedState req2 OnTable::true)
 (RequiredState req3 Arm::Used())
 (Constraint Overlaps(req1,Head))
 (Constraint Meets(req1,req2))
 (Constraint Overlaps(Head,req2))
 (Constraint Duration[5,INF](Head))
# (RequiredResource arm(1))
 (Constraint During(Head,req3))
)

(PlanningOperator
 (Head Robot1::Pick())
# (RequiredState req1 OnTable::true)
 (AchievedState req2 OnTable::false)
 (RequiredState req3 Arm::Used())
# (Constraint Overlaps(req1,Head))
# (Constraint Meets(req1,req2))
 (Constraint Overlaps(Head,req2))
 (Constraint Duration[5,INF](Head))
# (RequiredResource arm(1))
 (Constraint During(Head,req3))
)

(PlanningOperator
 (Head Arm::Used())
 (RequiredResource arm(1))
)