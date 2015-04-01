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

(Domain TestContextInference)

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
 (Constraint Finishes(Head,req1))		#Eating Finishes DiningRoom
 (Constraint After(Head,req2))			#Eating After Cooking
)
