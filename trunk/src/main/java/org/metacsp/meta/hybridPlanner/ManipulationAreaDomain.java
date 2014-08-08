package org.metacsp.meta.hybridPlanner;

import java.util.HashMap;
import java.util.Vector;

import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.spatial.rectangleAlgebra.RectangleConstraint;
import org.metacsp.multi.spatial.rectangleAlgebra.UnaryRectangleConstraint;
import org.metacsp.spatial.utility.SpatialRule;
import org.metacsp.time.Bounds;

public class ManipulationAreaDomain {

	
	Bounds manArea_size_x = new Bounds(60, 60);
	Bounds manArea_size_y = new Bounds(60, 60);	

	Bounds placingArea_size_max = new Bounds(40, 40);
	Bounds placingArea_size_min = new Bounds(34, 34);	
//	Bounds placingArea_size_min = new Bounds(40, 40);
	
	Bounds overlapped_max = new Bounds(30, 30);
//	Bounds overlapped_min = new Bounds(10, 10);
	
//	long min_distance = 30;
//	long max_distance = 35;
	
	HashMap<String, Vector<SpatialRule>> rulesHashMap = new HashMap<String, Vector<SpatialRule>>();
	
	public ManipulationAreaDomain(){

		rulesHashMap.put("RA_north", getSpatialKnowledge("RA_north"));
		rulesHashMap.put("RA_east", getSpatialKnowledge("RA_east"));
		rulesHashMap.put("RA_south", getSpatialKnowledge("RA_south"));
		rulesHashMap.put("RA_west", getSpatialKnowledge("RA_west"));

		rulesHashMap.put("LA_north", getSpatialKnowledge("LA_north"));
		rulesHashMap.put("LA_east", getSpatialKnowledge("LA_east"));
		rulesHashMap.put("LA_south", getSpatialKnowledge("LA_south"));
		rulesHashMap.put("LA_west", getSpatialKnowledge("LA_west"));

		
	}
	
	public Vector<SpatialRule> getSpatialRulesByRelation(String name){
		return rulesHashMap.get(name);
	}
	
	private Vector<SpatialRule> getSpatialKnowledge(String relation){

		Vector<SpatialRule> srules = new Vector<SpatialRule>();
		
		
		if(relation.contains("RA_south")){
			
			SpatialRule r1 = new SpatialRule("manipulationArea", "manipulationArea", 
					new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.Size, manArea_size_x, manArea_size_y));
			srules.add(r1);

			SpatialRule r0 = new SpatialRule("placingArea", "placingArea", 
					new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.Size, placingArea_size_max, placingArea_size_min));
			srules.add(r0);
			
			SpatialRule r2 = new SpatialRule("placingArea", "manipulationArea", 
					new RectangleConstraint(new AllenIntervalConstraint(AllenIntervalConstraint.Type.OverlappedBy , overlapped_max),
					new AllenIntervalConstraint(AllenIntervalConstraint.Type.MetBy, AllenIntervalConstraint.Type.MetBy.getDefaultBounds()))
					);
			srules.add(r2);

			SpatialRule r3 = new SpatialRule("object", "placingArea", 
					new RectangleConstraint(new AllenIntervalConstraint(AllenIntervalConstraint.Type.During , AllenIntervalConstraint.Type.During.getDefaultBounds()),
					new AllenIntervalConstraint(AllenIntervalConstraint.Type.During, AllenIntervalConstraint.Type.During.getDefaultBounds()))

					);
			srules.add(r3);

			
			SpatialRule r4 = new SpatialRule("manipulationArea", "table", 
					new RectangleConstraint(getConvexifyBeforeAndAfter(),
					new AllenIntervalConstraint(AllenIntervalConstraint.Type.Meets, AllenIntervalConstraint.Type.Meets.getDefaultBounds()))
					);
			srules.add(r4);

		}
		else if(relation.contains("LA_south")){
			
			SpatialRule r1 = new SpatialRule("manipulationArea", "manipulationArea", 
					new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.Size, manArea_size_x, manArea_size_y));
			srules.add(r1);

			SpatialRule r0 = new SpatialRule("placingArea", "placingArea", 
					new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.Size, placingArea_size_max, placingArea_size_min));
			srules.add(r0);

			
			SpatialRule r2 = new SpatialRule("placingArea", "manipulationArea", 
					new RectangleConstraint(new AllenIntervalConstraint(AllenIntervalConstraint.Type.Overlaps , overlapped_max),
					new AllenIntervalConstraint(AllenIntervalConstraint.Type.MetBy, AllenIntervalConstraint.Type.MetBy.getDefaultBounds()))
					);
			srules.add(r2);

			SpatialRule r3 = new SpatialRule("object", "placingArea", 
					new RectangleConstraint(new AllenIntervalConstraint(AllenIntervalConstraint.Type.During , AllenIntervalConstraint.Type.During.getDefaultBounds()),
					new AllenIntervalConstraint(AllenIntervalConstraint.Type.During, AllenIntervalConstraint.Type.During.getDefaultBounds()))

					);
			srules.add(r3);

			
			SpatialRule r4 = new SpatialRule("manipulationArea", "table", 
					new RectangleConstraint(getConvexifyBeforeAndAfter(),
					new AllenIntervalConstraint(AllenIntervalConstraint.Type.Meets, AllenIntervalConstraint.Type.Meets.getDefaultBounds()))
					);
			srules.add(r4);
		}		
		else if(relation.contains("RA_west")){
			
			SpatialRule r1 = new SpatialRule("manipulationArea", "manipulationArea", 
					new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.Size, manArea_size_x, manArea_size_y));
			srules.add(r1);

			
			SpatialRule r0 = new SpatialRule("placingArea", "placingArea", 
					new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.Size, placingArea_size_min, placingArea_size_max));
			srules.add(r0);

			
			SpatialRule r2 = new SpatialRule("placingArea", "manipulationArea", 
					new RectangleConstraint(new AllenIntervalConstraint(AllenIntervalConstraint.Type.MetBy , AllenIntervalConstraint.Type.MetBy.getDefaultBounds()),
					new AllenIntervalConstraint(AllenIntervalConstraint.Type.Overlaps, overlapped_max))
					);
			srules.add(r2);

			SpatialRule r3 = new SpatialRule("object", "placingArea", 
					new RectangleConstraint(new AllenIntervalConstraint(AllenIntervalConstraint.Type.During , AllenIntervalConstraint.Type.During.getDefaultBounds()),
					new AllenIntervalConstraint(AllenIntervalConstraint.Type.During, AllenIntervalConstraint.Type.During.getDefaultBounds()))

					);
			srules.add(r3);

			
//			SpatialRule r4 = new SpatialRule("manipulationArea", "table", 
//					new RectangleConstraint(new AllenIntervalConstraint(AllenIntervalConstraint.Type.Meets , AllenIntervalConstraint.Type.Meets.getDefaultBounds()),
//					new AllenIntervalConstraint(AllenIntervalConstraint.Type.During, AllenIntervalConstraint.Type.During.getDefaultBounds()))
//					);
//			srules.add(r4);
			
			
			SpatialRule r4 = new SpatialRule("manipulationArea", "table", 
					new RectangleConstraint(new AllenIntervalConstraint(AllenIntervalConstraint.Type.Meets , AllenIntervalConstraint.Type.Meets.getDefaultBounds()),
							getConvexifyBeforeAndAfter())
					);
			srules.add(r4);
						
		}
		else if(relation.contains("LA_west")){
			
			SpatialRule r1 = new SpatialRule("manipulationArea", "manipulationArea", 
					new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.Size, manArea_size_x, manArea_size_y));
			srules.add(r1);

			SpatialRule r0 = new SpatialRule("placingArea", "placingArea", 
					new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.Size, placingArea_size_min, placingArea_size_max));
			srules.add(r0);

			
			SpatialRule r2 = new SpatialRule("placingArea", "manipulationArea", 
					new RectangleConstraint(new AllenIntervalConstraint(AllenIntervalConstraint.Type.MetBy , AllenIntervalConstraint.Type.MetBy.getDefaultBounds()),
					new AllenIntervalConstraint(AllenIntervalConstraint.Type.OverlappedBy, overlapped_max))
					);
			srules.add(r2);

			SpatialRule r3 = new SpatialRule("object", "placingArea", 
					new RectangleConstraint(new AllenIntervalConstraint(AllenIntervalConstraint.Type.During , AllenIntervalConstraint.Type.During.getDefaultBounds()),
					new AllenIntervalConstraint(AllenIntervalConstraint.Type.During, AllenIntervalConstraint.Type.During.getDefaultBounds()))

					);
			srules.add(r3);

			
			SpatialRule r4 = new SpatialRule("manipulationArea", "table", 
					new RectangleConstraint(new AllenIntervalConstraint(AllenIntervalConstraint.Type.Meets , AllenIntervalConstraint.Type.Meets.getDefaultBounds()),
							getConvexifyBeforeAndAfter())
					);
			srules.add(r4);
		}
		else if(relation.contains("RA_north")){
			
			SpatialRule r1 = new SpatialRule("manipulationArea", "manipulationArea", 
					new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.Size, manArea_size_x, manArea_size_y));
			srules.add(r1);
			
			
			SpatialRule r0 = new SpatialRule("placingArea", "placingArea", 
					new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.Size, placingArea_size_max, placingArea_size_min));
			srules.add(r0);


			SpatialRule r2 = new SpatialRule("placingArea", "manipulationArea", 
					new RectangleConstraint(new AllenIntervalConstraint(AllenIntervalConstraint.Type.Overlaps , overlapped_max),
					new AllenIntervalConstraint(AllenIntervalConstraint.Type.Meets, AllenIntervalConstraint.Type.Meets.getDefaultBounds()))
					);
			srules.add(r2);

			SpatialRule r3 = new SpatialRule("object", "placingArea", 
					new RectangleConstraint(new AllenIntervalConstraint(AllenIntervalConstraint.Type.During , AllenIntervalConstraint.Type.During.getDefaultBounds()),
					new AllenIntervalConstraint(AllenIntervalConstraint.Type.During, AllenIntervalConstraint.Type.During.getDefaultBounds()))

					);
			srules.add(r3);
			
			SpatialRule r4 = new SpatialRule("manipulationArea", "table", 
					new RectangleConstraint(getConvexifyBeforeAndAfter(),
					new AllenIntervalConstraint(AllenIntervalConstraint.Type.MetBy, AllenIntervalConstraint.Type.MetBy.getDefaultBounds()))
					);
			srules.add(r4);
		}
		else if(relation.contains("LA_north")){
			
			SpatialRule r1 = new SpatialRule("manipulationArea", "manipulationArea", 
					new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.Size, manArea_size_x, manArea_size_y));
			srules.add(r1);

			SpatialRule r0 = new SpatialRule("placingArea", "placingArea", 
					new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.Size, placingArea_size_max, placingArea_size_min));
			srules.add(r0);
			
			
			SpatialRule r2 = new SpatialRule("placingArea", "manipulationArea", 
					new RectangleConstraint(new AllenIntervalConstraint(AllenIntervalConstraint.Type.OverlappedBy , overlapped_max),
					new AllenIntervalConstraint(AllenIntervalConstraint.Type.Meets, AllenIntervalConstraint.Type.Meets.getDefaultBounds()))
					);
			srules.add(r2);

			SpatialRule r3 = new SpatialRule("object", "placingArea", 
					new RectangleConstraint(new AllenIntervalConstraint(AllenIntervalConstraint.Type.During , AllenIntervalConstraint.Type.During.getDefaultBounds()),
					new AllenIntervalConstraint(AllenIntervalConstraint.Type.During, AllenIntervalConstraint.Type.During.getDefaultBounds()))

					);
			srules.add(r3);
			
			SpatialRule r4 = new SpatialRule("manipulationArea", "table", 
					new RectangleConstraint(getConvexifyBeforeAndAfter(),
					new AllenIntervalConstraint(AllenIntervalConstraint.Type.MetBy, AllenIntervalConstraint.Type.MetBy.getDefaultBounds()))
					);
			srules.add(r4);
		}
		else if(relation.contains("RA_east")){
			
			SpatialRule r1 = new SpatialRule("manipulationArea", "manipulationArea", 
					new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.Size, manArea_size_x, manArea_size_y));
			srules.add(r1);

			SpatialRule r0 = new SpatialRule("placingArea", "placingArea", 
					new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.Size, placingArea_size_min, placingArea_size_max));
			srules.add(r0);

			
			SpatialRule r2 = new SpatialRule("placingArea", "manipulationArea", 
					new RectangleConstraint(new AllenIntervalConstraint(AllenIntervalConstraint.Type.Meets , AllenIntervalConstraint.Type.Meets.getDefaultBounds()),
					new AllenIntervalConstraint(AllenIntervalConstraint.Type.OverlappedBy, overlapped_max))
					);
			srules.add(r2);

			SpatialRule r3 = new SpatialRule("object", "placingArea", 
					new RectangleConstraint(new AllenIntervalConstraint(AllenIntervalConstraint.Type.During , AllenIntervalConstraint.Type.During.getDefaultBounds()),
					new AllenIntervalConstraint(AllenIntervalConstraint.Type.During, AllenIntervalConstraint.Type.During.getDefaultBounds()))

					);
			srules.add(r3);
			
			SpatialRule r4 = new SpatialRule("manipulationArea", "table", 
					new RectangleConstraint(new AllenIntervalConstraint(AllenIntervalConstraint.Type.MetBy , AllenIntervalConstraint.Type.MetBy.getDefaultBounds()),
							getConvexifyBeforeAndAfter())
					);
			srules.add(r4);
		}
		else if(relation.contains("LA_east")){
			
			SpatialRule r1 = new SpatialRule("manipulationArea", "manipulationArea", 
					new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.Size, manArea_size_x, manArea_size_y));
			srules.add(r1);

			SpatialRule r0 = new SpatialRule("placingArea", "placingArea", 
					new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.Size, placingArea_size_min, placingArea_size_max));
			srules.add(r0);

			
			SpatialRule r2 = new SpatialRule("placingArea", "manipulationArea", 
					new RectangleConstraint(new AllenIntervalConstraint(AllenIntervalConstraint.Type.Meets , AllenIntervalConstraint.Type.Meets.getDefaultBounds()),
					new AllenIntervalConstraint(AllenIntervalConstraint.Type.Overlaps, overlapped_max))
					);
			srules.add(r2);

			SpatialRule r3 = new SpatialRule("object", "placingArea", 
					new RectangleConstraint(new AllenIntervalConstraint(AllenIntervalConstraint.Type.During , AllenIntervalConstraint.Type.During.getDefaultBounds()),
					new AllenIntervalConstraint(AllenIntervalConstraint.Type.During, AllenIntervalConstraint.Type.During.getDefaultBounds()))

					);
			srules.add(r3);
			
			SpatialRule r4 = new SpatialRule("manipulationArea", "table", 
					new RectangleConstraint(new AllenIntervalConstraint(AllenIntervalConstraint.Type.MetBy , AllenIntervalConstraint.Type.MetBy.getDefaultBounds()),
							getConvexifyBeforeAndAfter())
					);
			srules.add(r4);
		}

		
		return srules;

	}

	public AllenIntervalConstraint getConvexifyBeforeAndAfter(){
		return new AllenIntervalConstraint(AllenIntervalConstraint.Type.Before, AllenIntervalConstraint.Type.Meets, AllenIntervalConstraint.Type.Overlaps, AllenIntervalConstraint.Type.During, 
				AllenIntervalConstraint.Type.OverlappedBy, AllenIntervalConstraint.Type.MetBy, AllenIntervalConstraint.Type.After);

	}
	
	
}
