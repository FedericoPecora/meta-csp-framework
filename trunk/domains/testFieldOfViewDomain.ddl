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

(SimpleDomain TestSensingDomain)

#(Sensor RobotProprioception) #proprioception
#(Sensor atLocation) #tabletop perception

#(ContextVariable RobotProprioception) #proprioception
#(ContextVariable atLocation) #tabletop perception


#(Observable RobotProprioception) #proprioception
#(Observable atLocation) #tabletop perception

(Controllable RobotProprioception) #proprioception
(Controllable atLocation) #tabletop perception

(Resource arm 1)
#(Resource kinect 1)
(Resource fieldOfView 200)




(SimpleOperator
 (Head RobotSense::sensing_before_placing_cup1_table1())
 (RequiredState req1 atLocation::at_robot1_table1())
 (Constraint During(Head,req1))
 (Constraint Duration[2000,INF](Head))
# (RequiredResource kinect(1))
 (RequiredResource fieldOfView(200))
)




(SimpleOperator
 (Head RobotSense::sensing_before_picking_cup1_table1())
 (RequiredState req1 atLocation::at_robot1_table1())
 (Constraint During(Head,req1))
 (Constraint Duration[2000,INF](Head))
# (RequiredResource kinect(1))
 (RequiredResource fieldOfView(200))
)




(SimpleOperator
 (Head RobotSense::sensing_before_picking_cup1_tray1())
 (RequiredState req1 atLocation::at_robot1_table1())
 (Constraint During(Head,req1))
 (Constraint Duration[2000,INF](Head))
# (RequiredResource kinect(1))
 (RequiredResource fieldOfView(200))
)




(SimpleOperator
 (Head RobotSense::sensing_before_placing_cup1_tray1())
 (RequiredState req1 atLocation::at_robot1_table1())
 (Constraint During(Head,req1))
 (Constraint Duration[2000,INF](Head))
# (RequiredResource kinect(1))
 (RequiredResource fieldOfView(200))
)




(SimpleOperator
 (Head RobotSense::sensing_before_placing_knife1_tray1())
 (RequiredState req1 atLocation::at_robot1_table1())
 (Constraint During(Head,req1))
 (Constraint Duration[2000,INF](Head))
# (RequiredResource kinect(1))
 (RequiredResource fieldOfView(200))
)



(SimpleOperator
 (Head RobotSense::sensing_before_picking_knife1_tray1())
 (RequiredState req1 atLocation::at_robot1_table1())
 (Constraint During(Head,req1))
 (Constraint Duration[2000,INF](Head))
# (RequiredResource kinect(1))
 (RequiredResource fieldOfView(200))
)




(SimpleOperator
 (Head RobotSense::sensing_before_placing_knife1_table1())
 (RequiredState req1 atLocation::at_robot1_table1())
 (Constraint During(Head,req1))
 (Constraint Duration[2000,INF](Head))
# (RequiredResource kinect(1))
 (RequiredResource fieldOfView(200))
)



(SimpleOperator
 (Head RobotSense::sensing_before_picking_knife1_table1())
 (RequiredState req1 atLocation::at_robot1_table1())
 (Constraint During(Head,req1))
 (Constraint Duration[2000,INF](Head))
# (RequiredResource kinect(1))
 (RequiredResource fieldOfView(200))
)



(SimpleOperator
 (Head RobotSense::sensing_before_placing_fork1_tray1())
 (RequiredState req1 atLocation::at_robot1_table1())
 (Constraint During(Head,req1))
 (Constraint Duration[2000,INF](Head))
# (RequiredResource kinect(1))
 (RequiredResource fieldOfView(200))
)


(SimpleOperator
 (Head RobotSense::sensing_before_picking_fork1_tray1())
 (RequiredState req1 atLocation::at_robot1_table1())
 (Constraint During(Head,req1))
 (Constraint Duration[2000,INF](Head))
# (RequiredResource kinect(1))
 (RequiredResource fieldOfView(200))
)


(SimpleOperator
 (Head RobotSense::sensing_before_placing_fork1_table1())
 (RequiredState req1 atLocation::at_robot1_table1())
 (Constraint During(Head,req1))
 (Constraint Duration[2000,INF](Head))
# (RequiredResource kinect(1))
 (RequiredResource fieldOfView(200))
)



(SimpleOperator
 (Head RobotSense::sensing_before_picking_fork1_table1())
 (RequiredState req1 atLocation::at_robot1_table1())
 (Constraint During(Head,req1))
 (Constraint Duration[2000,INF](Head))
# (RequiredResource kinect(1))
 (RequiredResource fieldOfView(200))
)



(SimpleOperator
 (Head RobotSense::sensing_before_picking_cup1_counter1())
 (RequiredState req1 atLocation::at_robot1_counter1())
 (Constraint During(Head,req1))
 (Constraint Duration[2000,INF](Head))
# (RequiredResource kinect(1))
 (RequiredResource fieldOfView(200))
)

#########################move######################################

#Move

(SimpleOperator
 (Head RobotAction::move_counter1_table1())
 (RequiredState req1 atLocation::at_robot1_counter1())
 (Constraint MetBy(Head,req1))
 (Constraint Duration[2000,INF](Head))
)

(SimpleOperator
 (Head RobotAction::move_table1_counter1())
 (RequiredState req1 atLocation::at_robot1_table1())
 (Constraint MetBy(Head,req1))
 (Constraint Duration[2000,INF](Head))
)


(SimpleOperator
 (Head atLocation::at_robot1_table1())
 (RequiredState req1 RobotAction::move_counter1_table1())
 (Constraint MetBy(Head,req1))
 (Constraint Duration[2000,INF](Head))
)

(SimpleOperator
 (Head atLocation::at_robot1_counter1())
 (RequiredState req1 RobotAction::move_table1_counter1())
 (Constraint MetBy(Head,req1))
 (Constraint Duration[2000,INF](Head))
)



##############################cup#########################################

#tray

(SimpleOperator
 (Head atLocation::at_cup1_tray1())
 (RequiredState req1 RobotAction::place_cup1_tray1())
 (Constraint StartedBy(Head,req1))
 (Constraint OverlappedBy(Head,req1))
 (Constraint Duration[2000,INF](Head))
)

(SimpleOperator
 (Head RobotAction::pick_cup1_tray1())
 (RequiredState req1 atLocation::at_cup1_tray1())
 (RequiredState req2 RobotSense::sensing_before_picking_cup1_tray1())
# (RequiredState req3 atLocation::at_robot1_table1())
# (Constraint During(Head,req3))
 (Constraint MetBy(Head,req2))
 (Constraint MetBy(Head,req1))
 (Constraint Duration[2000,INF](Head))
 (RequiredResource arm(1))
 (RequiredResource fieldOfView(1))
)

(SimpleOperator
 (Head RobotProprioception::holding_cup1())
 (RequiredState req1 RobotAction::pick_cup1_tray1())
 (Constraint MetBy(Head,req1))
 (Constraint Duration[2000,INF](Head))
 (RequiredResource arm(1))
)

(SimpleOperator
 (Head RobotAction::place_cup1_tray1())
 (RequiredState req1 RobotProprioception::holding_cup1())
 (RequiredState req2 RobotSense::sensing_before_placing_cup1_tray1())
# (RequiredState req3 atLocation::at_robot1_table1())
# (Constraint During(Head,req3))
 (Constraint MetBy(Head,req2))
 (Constraint MetBy(Head,req1))
 (Constraint Duration[2000,INF](Head))
 (RequiredResource arm(1))
 (RequiredResource fieldOfView(1))
)

#counter

(SimpleOperator
 (Head RobotProprioception::holding_cup1())
 (RequiredState req1 RobotAction::pick_cup1_counter1())
 (Constraint MetBy(Head,req1))
 (Constraint Duration[2000,INF](Head))
 (RequiredResource arm(1))
)

(SimpleOperator
 (Head RobotAction::pick_cup1_counter1())
 (RequiredState req1 atLocation::at_cup1_counter1())
(RequiredState req2 RobotSense::sensing_before_picking_cup1_counter1())
 (RequiredState req3 atLocation::at_robot1_counter1())
# (Constraint During(req2,req3))
 (Constraint During(Head,req3))
 (Constraint MetBy(Head,req2))
 (Constraint OverlappedBy(Head,req1))
 (Constraint Finishes(Head,req1))
 (Constraint Duration[2000,INF](Head))
 (RequiredResource arm(1))
 (RequiredResource fieldOfView(1))
)

#table


(SimpleOperator
 (Head atLocation::at_cup1_table1())
 (RequiredState req1 RobotAction::place_cup1_table1())
 (Constraint StartedBy(Head,req1))
 (Constraint OverlappedBy(Head,req1))
 (Constraint Duration[2000,INF](Head))
)


(SimpleOperator
 (Head RobotAction::place_cup1_table1())
 (RequiredState req1 RobotProprioception::holding_cup1())
 (RequiredState req2 RobotSense::sensing_before_placing_cup1_table1())
 (RequiredState req3 atLocation::at_robot1_table1())
# (Constraint During(req2,req3))
 (Constraint During(Head,req3))
 (Constraint MetBy(Head,req2))
 (Constraint MetBy(Head,req1))
 (Constraint Duration[2000,INF](Head))
 (RequiredResource arm(1))
 (RequiredResource fieldOfView(1))
)


(SimpleOperator
 (Head RobotProprioception::holding_cup1())
 (RequiredState req1 RobotAction::pick_cup1_table1())
 (Constraint MetBy(Head,req1))
 (Constraint Duration[2000,INF](Head))
 (RequiredResource arm(1))
)


(SimpleOperator
 (Head RobotAction::pick_cup1_table1())
 (RequiredState req1 atLocation::at_cup1_table1())
 (RequiredState req2 RobotSense::sensing_before_picking_cup1_table1())
 (RequiredState req3 atLocation::at_robot1_table1())
# (Constraint During(req2,req3))
 (Constraint During(Head,req3))
 (Constraint MetBy(Head,req2))
 (Constraint OverlappedBy(Head,req1))
 (Constraint Finishes(Head,req1))
 (Constraint Duration[2000,INF](Head))
 (RequiredResource arm(1))
 (RequiredResource fieldOfView(1))
)






##################Knife##########################

#tray

(SimpleOperator
 (Head atLocation::at_knife1_tray1())
 (RequiredState req1 RobotAction::place_knife1_tray1())
 (Constraint StartedBy(Head,req1))
 (Constraint OverlappedBy(Head,req1))
 (Constraint Duration[2000,INF](Head))
)


(SimpleOperator
 (Head RobotAction::place_knife1_tray1())
 (RequiredState req1 RobotProprioception::holding_knife1())
(RequiredState req2 RobotSense::sensing_before_placing_knife1_tray1())
 (Constraint MetBy(Head,req2))
 (Constraint MetBy(Head,req1))
 (Constraint Duration[2000,INF](Head))
 (RequiredResource arm(1))
 (RequiredResource fieldOfView(1))
)


(SimpleOperator
 (Head RobotProprioception::holding_knife1())
 (RequiredState req1 RobotAction::pick_knife1_tray1())
 (Constraint MetBy(Head,req1))
 (Constraint Duration[2000,INF](Head))
 (RequiredResource arm(1))
)


(SimpleOperator
 (Head RobotAction::pick_knife1_tray1())
 (RequiredState req1 atLocation::at_knife1_tray1())
 (RequiredState req2 RobotSense::sensing_before_picking_knife1_tray1())
 (Constraint MetBy(Head,req2))
 (Constraint MetBy(Head,req1))
 (Constraint Duration[2000,INF](Head))
 (RequiredResource arm(1))
 (RequiredResource fieldOfView(1))
)



#table

(SimpleOperator
 (Head atLocation::at_knife1_table1())
 (RequiredState req1 RobotAction::place_knife1_table1())
 (Constraint StartedBy(Head,req1))
 (Constraint OverlappedBy(Head,req1))
 (Constraint Duration[2000,INF](Head))
)

(SimpleOperator
 (Head RobotAction::place_knife1_table1())
 (RequiredState req1 RobotProprioception::holding_knife1())
 (RequiredState req2 RobotSense::sensing_before_placing_knife1_table1())
 (RequiredState req3 atLocation::at_robot1_table1())
# (Constraint During(req2,req3))
 (Constraint During(Head,req3))
 (Constraint MetBy(Head,req2))
 (Constraint MetBy(Head,req1))
 (Constraint Duration[2000,INF](Head))
 (RequiredResource arm(1))
 (RequiredResource fieldOfView(1))
)

(SimpleOperator
 (Head RobotProprioception::holding_knife1())
 (RequiredState req1 RobotAction::pick_knife1_table1())
 (Constraint MetBy(Head,req1))
 (Constraint Duration[2000,INF](Head))
 (RequiredResource arm(1))
)

(SimpleOperator
 (Head RobotAction::pick_knife1_table1())
 (RequiredState req1 atLocation::at_knife1_table1())
 (RequiredState req2 RobotSense::sensing_before_picking_knife1_table1())
 (RequiredState req3 atLocation::at_robot1_table1())
# (Constraint During(req2,req3))
 (Constraint During(Head,req3))
 (Constraint MetBy(Head,req2))
 (Constraint OverlappedBy(Head,req1))
 (Constraint Finishes(Head,req1))
 (Constraint Duration[2000,INF](Head))
 (RequiredResource arm(1))
 (RequiredResource fieldOfView(1))
)




###############################fork#####################################

#tray

(SimpleOperator
 (Head atLocation::at_fork1_tray1())
 (RequiredState req1 RobotAction::place_fork1_tray1())
 (Constraint StartedBy(Head,req1))
 (Constraint OverlappedBy(Head,req1))
 (Constraint Duration[2000,INF](Head))
)


(SimpleOperator
 (Head RobotAction::place_fork1_tray1())
 (RequiredState req1 RobotProprioception::holding_fork1())
(RequiredState req2 RobotSense::sensing_before_placing_fork1_tray1())
 (Constraint MetBy(Head,req2))
 (Constraint MetBy(Head,req1))
 (Constraint Duration[2000,INF](Head))
 (RequiredResource arm(1))
 (RequiredResource fieldOfView(1))
)

(SimpleOperator
 (Head RobotProprioception::holding_fork1())
 (RequiredState req1 RobotAction::pick_fork1_tray1())
 (Constraint MetBy(Head,req1))
 (Constraint Duration[2000,INF](Head))
 (RequiredResource arm(1))
)


(SimpleOperator
 (Head RobotAction::pick_fork1_tray1())
 (RequiredState req1 atLocation::at_fork1_tray1())
(RequiredState req2 RobotSense::sensing_before_picking_fork1_tray1())
 (Constraint MetBy(Head,req2))
 (Constraint MetBy(Head,req1))
 (Constraint Duration[2000,INF](Head))
 (RequiredResource arm(1))
 (RequiredResource fieldOfView(1))
)

#table

(SimpleOperator
 (Head atLocation::at_fork1_table1())
 (RequiredState req1 RobotAction::place_fork1_table1())
 (Constraint StartedBy(Head,req1))
 (Constraint OverlappedBy(Head,req1))
 (Constraint Duration[2000,INF](Head))
)

(SimpleOperator
 (Head RobotAction::place_fork1_table1())
 (RequiredState req1 RobotProprioception::holding_fork1())
 (RequiredState req2 RobotSense::sensing_before_placing_fork1_table1())
 (RequiredState req3 atLocation::at_robot1_table1())
# (Constraint During(req2,req3))
 (Constraint During(Head,req3))
 (Constraint MetBy(Head,req2))
 (Constraint MetBy(Head,req1))
 (Constraint Duration[2000,INF](Head))
 (RequiredResource arm(1))
 (RequiredResource fieldOfView(1))
)


(SimpleOperator
 (Head RobotProprioception::holding_fork1())
 (RequiredState req1 RobotAction::pick_fork1_table1())
 (Constraint MetBy(Head,req1))
 (Constraint Duration[2000,INF](Head))
 (RequiredResource arm(1))
)




(SimpleOperator
 (Head RobotAction::pick_fork1_table1())
 (RequiredState req1 atLocation::at_fork1_table1())
 (RequiredState req2 RobotSense::sensing_before_picking_fork1_table1())
 (RequiredState req3 atLocation::at_robot1_table1())
# (Constraint During(req2,req3))
 (Constraint During(Head,req3))
 (Constraint MetBy(Head,req2))
 (Constraint OverlappedBy(Head,req1))
 (Constraint Finishes(Head,req1))
 (Constraint Duration[2000,INF](Head))
 (RequiredResource arm(1))
 (RequiredResource fieldOfView(1))
)


