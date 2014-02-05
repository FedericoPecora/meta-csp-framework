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

(SimpleDomain TestSimpleHybridPlanningDomain)

#(Sensor RobotProprioception) #proprioception
#(Sensor atLocation) #tabletop perception

#(ContextVariable RobotProprioception) #proprioception
#(ContextVariable atLocation) #tabletop perception


#(Observable RobotProprioception) #proprioception
#(Observable atLocation) #tabletop perception

(Controllable RobotProprioception) #proprioception
(Controllable atLocation) #tabletop perception

(Resource arm 1)


(SimpleOperator
 (Head atLocation::at_cup1_table1())
 (RequiredState req1 RobotAction::place_cup1_table1())
 (Constraint StartedBy(Head,req1))
 (Constraint OverlappedBy(Head,req1))
 (Constraint Duration[5,INF](Head))
)


(SimpleOperator
 (Head RobotAction::place_cup1_table1())
 (RequiredState req1 RobotProprioception::holding_cup1())
 (Constraint MetBy(Head,req1))
 (Constraint Duration[5,INF](Head))
 (RequiredResource arm(1))
)


(SimpleOperator
 (Head RobotProprioception::holding_cup1())
 (RequiredState req1 RobotAction::pick_cup1_table1())
 (Constraint MetBy(Head,req1))
 (Constraint Duration[5,INF](Head))
 (RequiredResource arm(1))
)


(SimpleOperator
 (Head RobotAction::pick_cup1_table1())
 (RequiredState req1 atLocation::at_cup1_table1())
 (Constraint OverlappedBy(Head,req1))
 (Constraint Finishes(Head,req1))
 (Constraint Duration[5,INF](Head))
 (RequiredResource arm(1))
)


#tray

(SimpleOperator
 (Head atLocation::at_cup1_tray1())
 (RequiredState req1 RobotAction::place_cup1_tray1())
 (Constraint StartedBy(Head,req1))
 (Constraint OverlappedBy(Head,req1))
 (Constraint Duration[5,INF](Head))
)

(SimpleOperator
 (Head RobotAction::pick_cup1_tray1())
 (RequiredState req1 atLocation::at_cup1_tray1())
 (Constraint MetBy(Head,req1))
 (Constraint Duration[5,INF](Head))
 (RequiredResource arm(1))
)

(SimpleOperator
 (Head RobotProprioception::holding_cup1())
 (RequiredState req1 RobotAction::pick_cup1_tray1())
 (Constraint MetBy(Head,req1))
 (Constraint Duration[5,INF](Head))
 (RequiredResource arm(1))
)

(SimpleOperator
 (Head RobotAction::place_cup1_tray1())
 (RequiredState req1 RobotProprioception::holding_cup1())
 (Constraint MetBy(Head,req1))
 (Constraint Duration[5,INF](Head))
 (RequiredResource arm(1))
)

#table2

#(SimpleOperator
# (Head RobotProprioception::holding_cup1())
# (RequiredState req1 RobotAction::pick_cup1_table2())
# (Constraint MetBy(Head,req1))
# (Constraint Duration[5,INF](Head))
# (RequiredResource arm(1))
#)

#(SimpleOperator
# (Head RobotAction::pick_cup1_table2())
# (RequiredState req1 atLocation::at_cup1_table2())
# (Constraint OverlappedBy(Head,req1))
# (Constraint Finishes(Head,req1))
# (Constraint Duration[5,INF](Head))
# (RequiredResource arm(1))
#)

##################Knife##########################

#tray

(SimpleOperator
 (Head atLocation::at_knife1_tray1())
 (RequiredState req1 RobotAction::place_knife1_tray1())
 (Constraint StartedBy(Head,req1))
 (Constraint OverlappedBy(Head,req1))
 (Constraint Duration[5,INF](Head))
)


(SimpleOperator
 (Head RobotAction::place_knife1_tray1())
 (RequiredState req1 RobotProprioception::holding_knife1())
 (Constraint MetBy(Head,req1))
 (Constraint Duration[5,INF](Head))
 (RequiredResource arm(1))
)


(SimpleOperator
 (Head RobotProprioception::holding_knife1())
 (RequiredState req1 RobotAction::pick_knife1_tray1())
 (Constraint MetBy(Head,req1))
 (Constraint Duration[5,INF](Head))
 (RequiredResource arm(1))
)


(SimpleOperator
 (Head RobotAction::pick_knife1_tray1())
 (RequiredState req1 atLocation::at_knife1_tray1())
 (Constraint MetBy(Head,req1))
 (Constraint Duration[5,INF](Head))
 (RequiredResource arm(1))
)

#

(SimpleOperator
 (Head atLocation::at_knife1_table1())
 (RequiredState req1 RobotAction::place_knife1_table1())
 (Constraint StartedBy(Head,req1))
 (Constraint OverlappedBy(Head,req1))
 (Constraint Duration[5,INF](Head))
)

(SimpleOperator
 (Head RobotAction::place_knife1_table1())
 (RequiredState req1 RobotProprioception::holding_knife1())
 (Constraint MetBy(Head,req1))
 (Constraint Duration[5,INF](Head))
 (RequiredResource arm(1))
)

(SimpleOperator
 (Head RobotProprioception::holding_knife1())
 (RequiredState req1 RobotAction::pick_knife1_table1())
 (Constraint MetBy(Head,req1))
 (Constraint Duration[5,INF](Head))
 (RequiredResource arm(1))
)

(SimpleOperator
 (Head RobotAction::pick_knife1_table1())
 (RequiredState req1 atLocation::at_knife1_table1())
 (Constraint OverlappedBy(Head,req1))
 (Constraint Finishes(Head,req1))
 (Constraint Duration[5,INF](Head))
 (RequiredResource arm(1))
)




#table2



###############################fork#####################################



(SimpleOperator
 (Head atLocation::at_fork1_tray1())
 (RequiredState req1 RobotAction::place_fork1_tray1())
 (Constraint StartedBy(Head,req1))
 (Constraint OverlappedBy(Head,req1))
 (Constraint Duration[5,INF](Head))
)


(SimpleOperator
 (Head RobotAction::place_fork1_tray1())
 (RequiredState req1 RobotProprioception::holding_fork1())
 (Constraint MetBy(Head,req1))
 (Constraint Duration[5,INF](Head))
 (RequiredResource arm(1))
)

(SimpleOperator
 (Head RobotProprioception::holding_fork1())
 (RequiredState req1 RobotAction::pick_fork1_tray1())
 (Constraint MetBy(Head,req1))
 (Constraint Duration[5,INF](Head))
 (RequiredResource arm(1))
)


(SimpleOperator
 (Head RobotAction::pick_fork1_tray1())
 (RequiredState req1 atLocation::at_fork1_tray1())
 (Constraint MetBy(Head,req1))
 (Constraint Duration[5,INF](Head))
 (RequiredResource arm(1))
)


(SimpleOperator
 (Head atLocation::at_fork1_table1())
 (RequiredState req1 RobotAction::place_fork1_table1())
 (Constraint StartedBy(Head,req1))
 (Constraint OverlappedBy(Head,req1))
 (Constraint Duration[5,INF](Head))
)

(SimpleOperator
 (Head RobotAction::place_fork1_table1())
 (RequiredState req1 RobotProprioception::holding_fork1())
 (Constraint MetBy(Head,req1))
 (Constraint Duration[5,INF](Head))
 (RequiredResource arm(1))
)


(SimpleOperator
 (Head RobotProprioception::holding_fork1())
 (RequiredState req1 RobotAction::pick_fork1_table1())
 (Constraint MetBy(Head,req1))
 (Constraint Duration[5,INF](Head))
 (RequiredResource arm(1))
)




(SimpleOperator
 (Head RobotAction::pick_fork1_table1())
 (RequiredState req1 atLocation::at_fork1_table1())
 (Constraint OverlappedBy(Head,req1))
 (Constraint Finishes(Head,req1))
 (Constraint Duration[5,INF](Head))
 (RequiredResource arm(1))
)



#table2


