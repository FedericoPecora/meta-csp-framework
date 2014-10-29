package org.metacsp.spatial.geometry;

import org.metacsp.framework.Domain;
import org.metacsp.framework.Variable;

public class Vertex extends Domain{

	private static final int MAX_POLY_VERTEX_COUNT = 20;
	private Vec2[] vertices = Vec2.arrayOf( MAX_POLY_VERTEX_COUNT );
	protected Vertex(Variable v) {
		super(v);
		// TODO Auto-generated constructor stub
	}

	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setVertices(Vec2[] vertices){
		this.vertices = vertices;
	}
	
	public Vec2[] getVertices(){
		return this.vertices;
	}
}
