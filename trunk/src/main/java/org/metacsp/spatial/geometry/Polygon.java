package org.metacsp.spatial.geometry;

import java.util.Vector;

import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Domain;
import org.metacsp.framework.Variable;

public class Polygon extends Variable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3385252449426376306L;
	private Domain dom;
	
	public static final int MAX_POLY_VERTEX_COUNT = 10;
	private int vertexCount;
//	private Vec2[] vertices = Vec2.arrayOf( MAX_POLY_VERTEX_COUNT );
	public Vec2[] normals = Vec2.arrayOf( MAX_POLY_VERTEX_COUNT );
	public Mat2 u = new Mat2();
	
	
	private Vec2 position = new Vec2();
	public boolean ref = false;
	public boolean incident = false;
	
	
	
	protected Polygon(ConstraintSolver cs, int id) {
		super(cs, id);
		
	}

	
	
	public int getVertexCount(){
		return vertexCount;
	}
	
	@Override
	public int compareTo(Variable arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Domain getDomain() {
		// TODO Auto-generated method stub
		return dom;
	}

	@Override
	public void setDomain(Domain d) {
		
	}
	
	public void setDomain(Vec2 ... verts){
		dom = new Vertex(this);
		vertexCount = verts.length;
		this.setOrient(0.0f);
		getPosition().set( getCentroid(orderVertex(verts)).x, getCentroid(orderVertex(verts)).y );
		Vec2[] halfPoly = new Vec2[verts.length];
		for (int i = 0; i < verts.length; i++) {
			halfPoly[i] = new Vec2(verts[i].x - getPosition().x, verts[i].y - getPosition().y);
		}
		set( halfPoly );
		initialize();
		
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return this.getClass().getSimpleName() + " " + this.id + " " + this.getDomain();
	}

	public void initialize()
	{
		computeMass( 1.0f );
	}
	
	private void computeMass( float density )
	{
		// Calculate centroid and moment of inertia
		
		float area = 0.0f;
		float I = 0.0f;
		final float k_inv3 = 1.0f / 3.0f;
		Vec2 c = new Vec2( 0.0f, 0.0f ); // centroid;

		for (int i = 0; i < vertexCount; ++i)
		{
			// Triangle vertices, third vertex implied as (0, 0)
			Vec2 p1 = ((Vertex)this.dom).getVertices()[i];
			Vec2 p2 = ((Vertex)this.dom).getVertices()[(i + 1) % vertexCount];

			float D = Vec2.cross( p1, p2 );
			float triangleArea = 0.5f * D;

			area += triangleArea;

			// Use area to weight the centroid average, not just vertex position
			float weight = triangleArea * k_inv3;
			c.addsi( p1, weight );
			c.addsi( p2, weight );

			float intx2 = p1.x * p1.x + p2.x * p1.x + p2.x * p2.x;
			float inty2 = p1.y * p1.y + p2.y * p1.y + p2.y * p2.y;
			I += (0.25f * k_inv3 * D) * (intx2 + inty2);
		}

		c.muli( 1.0f / area );

		// Translate vertices to centroid (make the centroid (0, 0)
		// for the polygon in model space)
		// Not really necessary, but I like doing this anyway
		for (int i = 0; i < vertexCount; ++i)
		{
			((Vertex)this.dom).getVertices()[i].subi( c );
		}
		
//		this.mass = density * area;
//		this.invMass = (this.mass != 0.0f) ? 1.0f / this.mass : 0.0f;
//		this.inertia = I * density;
//		this.invInertia = (this.inertia != 0.0f) ? 1.0f / this.inertia : 0.0f;
	}

	private Vec2 getCentroid(Vec2 ...vers)
	{
		// Calculate centroid and moment of inertia
		
		Vec2[] verticesCopy = new Vec2[vers.length];
		for (int i = 0; i < vers.length; i++) {
			verticesCopy[i] = new Vec2(vers[i].x, vers[i].y);
		}
		
		float area = 0.0f;
		float I = 0.0f;
		final float k_inv3 = 1.0f / 3.0f;
		Vec2 c = new Vec2( 0.0f, 0.0f ); // centroid;

		for (int i = 0; i < vertexCount; ++i)
		{
			// Triangle vertices, third vertex implied as (0, 0)
			Vec2 p1 = verticesCopy[i];
			Vec2 p2 = verticesCopy[(i + 1) % vertexCount];

			float D = Vec2.cross( p1, p2 );
			float triangleArea = 0.5f * D;

			area += triangleArea;

			// Use area to weight the centroid average, not just vertex position
			float weight = triangleArea * k_inv3;
			c.addsi( p1, weight );
			c.addsi( p2, weight );

			float intx2 = p1.x * p1.x + p2.x * p1.x + p2.x * p2.x;
			float inty2 = p1.y * p1.y + p2.y * p1.y + p2.y * p2.y;
			I += (0.25f * k_inv3 * D) * (intx2 + inty2);
		}

		c.muli( 1.0f / area );

		// Translate vertices to centroid (make the centroid (0, 0)
		// for the polygon in model space)
		// Not really necessary, but I like doing this anyway
		for (int i = 0; i < vertexCount; ++i)
		{
			verticesCopy[i].subi( c );
		}		
		return c;
	
	}

	private void setOrient( float radians )
	{
		u.set( radians );
	}

	private void setBox( float hw, float hh )
	{
		Vec2[] vertices = Vec2.arrayOf( MAX_POLY_VERTEX_COUNT );
		vertexCount = 4;
		vertices[0].set( -hw, -hh );
		vertices[1].set( hw, -hh );
		vertices[2].set( hw, hh );
		vertices[3].set( -hw, hh );
		
		((Vertex)this.dom).setVertices(vertices);
		
		normals[0].set( 0.0f, -1.0f );
		normals[1].set( 1.0f, 0.0f );
		normals[2].set( 0.0f, 1.0f );
		normals[3].set( -1.0f, 0.0f );
	}
	
	private Vec2[] orderVertex(Vec2 ...verts){
		
		Vec2[] vertices = Vec2.arrayOf( MAX_POLY_VERTEX_COUNT );
		Vec2[] verticesCopy = new Vec2[verts.length];
		for (int i = 0; i < verts.length; i++) {
			verticesCopy[i] = new Vec2(verts[i].x, verts[i].y);
		}
		
		
		
		int rightMost = 0;
		float highestXCoord = verticesCopy[0].x;
		for (int i = 1; i < verticesCopy.length; ++i)
		{
			float x = verticesCopy[i].x;

			if (x > highestXCoord)
			{
				highestXCoord = x;
				rightMost = i;
			}
			// If matching x then take farthest negative y
			else if (x == highestXCoord)
			{
				if (verticesCopy[i].y < verticesCopy[rightMost].y)
				{
					rightMost = i;
				}
			}
		}

		int[] hull = new int[MAX_POLY_VERTEX_COUNT];
		int outCount = 0;
		int indexHull = rightMost;

		for (;;)
		{
			hull[outCount] = indexHull;

			// Search for next index that wraps around the hull
			// by computing cross products to find the most counter-clockwise
			// vertex in the set, given the previos hull index
			int nextHullIndex = 0;
			for (int i = 1; i < verticesCopy.length; ++i)
			{
				// Skip if same coordinate as we need three unique
				// points in the set to perform a cross product
				if (nextHullIndex == indexHull)
				{
					nextHullIndex = i;
					continue;
				}

				// Cross every set of three unique vertices
				// Record each counter clockwise third vertex and add
				// to the output hull
				// See : http://www.oocities.org/pcgpe/math2d.html
				Vec2 e1 = verticesCopy[nextHullIndex].sub( verticesCopy[hull[outCount]] );
				Vec2 e2 = verticesCopy[i].sub( verticesCopy[hull[outCount]] );
				float c = Vec2.cross( e1, e2 );
				if (c < 0.0f)
				{
					nextHullIndex = i;
				}

				// Cross product is zero then e vectors are on same line
				// therefore want to record vertex farthest along that line
				if (c == 0.0f && e2.lengthSq() > e1.lengthSq())
				{
					nextHullIndex = i;
				}
			}

			++outCount;
			indexHull = nextHullIndex;

			// Conclude algorithm upon wrap-around
			if (nextHullIndex == rightMost)
			{
				vertexCount = outCount;
				break;
			}
		}

		// Copy vertices into shape's vertices
		for (int i = 0; i < vertexCount; ++i)
		{
			vertices[i].set( verticesCopy[hull[i]] );
		}
		return vertices;
	}
	
	private void set( Vec2... verts )
	{
		// Find the right most point on the hull
		Vec2[] vertices = Vec2.arrayOf( MAX_POLY_VERTEX_COUNT );
		int rightMost = 0;
		float highestXCoord = verts[0].x;
		for (int i = 1; i < verts.length; ++i)
		{
			float x = verts[i].x;

			if (x > highestXCoord)
			{
				highestXCoord = x;
				rightMost = i;
			}
			// If matching x then take farthest negative y
			else if (x == highestXCoord)
			{
				if (verts[i].y < verts[rightMost].y)
				{
					rightMost = i;
				}
			}
		}

		int[] hull = new int[MAX_POLY_VERTEX_COUNT];
		int outCount = 0;
		int indexHull = rightMost;

		for (;;)
		{
			hull[outCount] = indexHull;

			// Search for next index that wraps around the hull
			// by computing cross products to find the most counter-clockwise
			// vertex in the set, given the previos hull index
			int nextHullIndex = 0;
			for (int i = 1; i < verts.length; ++i)
			{
				// Skip if same coordinate as we need three unique
				// points in the set to perform a cross product
				if (nextHullIndex == indexHull)
				{
					nextHullIndex = i;
					continue;
				}

				// Cross every set of three unique vertices
				// Record each counter clockwise third vertex and add
				// to the output hull
				// See : http://www.oocities.org/pcgpe/math2d.html
				Vec2 e1 = verts[nextHullIndex].sub( verts[hull[outCount]] );
				Vec2 e2 = verts[i].sub( verts[hull[outCount]] );
				float c = Vec2.cross( e1, e2 );
				if (c < 0.0f)
				{
					nextHullIndex = i;
				}

				// Cross product is zero then e vectors are on same line
				// therefore want to record vertex farthest along that line
				if (c == 0.0f && e2.lengthSq() > e1.lengthSq())
				{
					nextHullIndex = i;
				}
			}

			++outCount;
			indexHull = nextHullIndex;

			// Conclude algorithm upon wrap-around
			if (nextHullIndex == rightMost)
			{
				vertexCount = outCount;
				break;
			}
		}

		// Copy vertices into shape's vertices
		for (int i = 0; i < vertexCount; ++i)
		{
			vertices[i].set( verts[hull[i]] );
		}

		// Compute face normals
		for (int i = 0; i < vertexCount; ++i)
		{
			Vec2 face = vertices[(i + 1) % vertexCount].sub( vertices[i] );

			// Calculate normal with 2D cross product between vector and scalar
			normals[i].set( face.y, -face.x );
			normals[i].normalize();
		}
		((Vertex)this.dom).setVertices(vertices);
	}

	public Vec2 getSupport( Vec2 dir )
	{
		float bestProjection = -Float.MAX_VALUE;
		Vec2 bestVertex = null;

		for (int i = 0; i < vertexCount; ++i)
		{
			Vec2 v = ((Vertex)this.dom).getVertices()[i];
			float projection = Vec2.dot( v, dir );

			if (projection > bestProjection)
			{
				bestVertex = v;
				bestProjection = projection;
			}
		}
		return bestVertex;
	}

	Vec2 getPosition() {
		return position;
	}

	void setPosition(Vec2 position) {
		this.position = position;
	}
	
//	public Vector<Vec2> getShiftedPolygon(){
//		
//		Vector<Vec2> vecs = new Vector<Vec2>();
////		System.out.println(position.x);
////		System.out.println(position.y);
//		System.out.println("-----------------------------------------");		
//		for (int i = 0; i < vertexCount; i++){
//			Vec2 v = new Vec2( ((Vertex)this.dom).getVertices()[i] );
//			this.u.muli( v );
//			v.addi( position );
//			((Vertex)this.dom).getVertices()[i] = v;
//			vecs.add(v);
//			System.out.println("--> " +v.x + " " + v.y);
//		}
//		System.out.println("-----------------------------------------");
//		return vecs;
//	}
	
	public Vector<Vec2> getFullSpaceRepresentation(){
		
		Vector<Vec2> vecs = new Vector<Vec2>();
//		System.out.println(position.x);
//		System.out.println(position.y);
//		System.out.println("-----------------------------------------");		
		for (int i = 0; i < vertexCount; i++){
			Vec2 v = new Vec2( ((Vertex)this.dom).getVertices()[i] );
			this.u.muli( v );
			v.addi( position );
//			((Vertex)this.dom).getVertices()[i] = v;
			vecs.add(v);
//			System.out.println("--> " +v.x + " " + v.y);
		}
//		System.out.println("-----------------------------------------");
		return vecs;
	}
	
}
