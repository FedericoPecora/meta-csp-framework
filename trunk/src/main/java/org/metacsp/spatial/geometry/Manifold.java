package org.metacsp.spatial.geometry;


public class Manifold {



		public Polygon A;
		public Polygon B;
		public float penetration;
		public final Vec2 normal = new Vec2();
		public final Vec2[] contacts = { new Vec2(), new Vec2() };
		public int contactCount;


		public Manifold( Polygon a, Polygon b )
		{
			A = a;
			B = b;
		}

		public boolean  solve()
		{
			CollisionPolygonPolygon cpp = new CollisionPolygonPolygon();
			return cpp.handleCollision( this, A, B );			
		}

		public boolean isCollided() {			
//			return new CollisionPolygonPolygon().handleCollision( this, A, B );		
			return new CollisionPolygonPolygon().verifyCollision(this, A, B);		
		}
		
		public void positionalCorrection()
		{
			// const real k_slop = 0.05f; // Penetration allowance
			// const real percent = 0.4f; // Penetration percentage to correct
			// Vec2 correction = (std::max( penetration - k_slop, 0.0f ) / (A->im +
			// B->im)) * normal * percent;
			// A->position -= correction * A->im;
			// B->position += correction * B->im;

//			float correction = StrictMath.max( penetration - ImpulseMath.PENETRATION_ALLOWANCE, 0.0f ) / (A.invMass + B.invMass) * ImpulseMath.PENETRATION_CORRETION;
//			float correction = StrictMath.max( penetration - ImpulseMath.PENETRATION_ALLOWANCE, 0.0f );
			float correction = penetration;
//			if(A.incident) 
				A.getPosition().addsi( normal, -(correction * (float)1.3)  );
//			if(B.incident) 
//				B.position.addsi( normal, correction * ((float)1.0) );			
//			A.position.addsi( normal, -A.invMass * correction );
//			B.position.addsi( normal, B.invMass * correction );
		}



}
