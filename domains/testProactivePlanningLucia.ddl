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
#   AchievedState						#
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

(SimpleOperator
 (Head Human::Cooking())
 (RequiredState req1 Location::Kitchen())
 (RequiredState req2 Stove::On())
 (Constraint Overlaps(Head,req1))
 (Constraint Contains(Head,req2))
)

(SimpleOperator
 (Head Human::Eating())
 (RequiredState req1 Location::DiningRoom())
 (RequiredState req2 Human::Cooking())
 (RequiredState req3 Robot::SayWarning())
 (Constraint Finishes(Head,req1))		#Eating Finishes DiningRoom
 (Constraint After(Head,req2))			#Eating After Cooking
# (Constraint Contains(Head,req3))		#Eating Contains SayWarning -- WRONG
)

(SimpleOperator
 (Head Robot::SayWarning())
 (RequiredState req1 Robot::MoveTo())
 (Constraint MetBy(Head,req1))
 (Constraint Duration[2000,INF](Head))
)

(SimpleOperator
 (Head Robot::MoveTo())
 (Constraint Duration[2000,INF](Head))
)