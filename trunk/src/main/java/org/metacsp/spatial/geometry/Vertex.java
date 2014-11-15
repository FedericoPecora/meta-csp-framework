package org.metacsp.spatial.geometry;

import org.metacsp.framework.Domain;
import org.metacsp.framework.Variable;

public class Vertex extends Domain{

	private static final long serialVersionUID = -672154595622071623L;
	private Vec2[] vertices; 
	protected Vertex(Variable v) {
		super(v);
		vertices = Vec2.arrayOf( Polygon.MAX_POLY_VERTEX_COUNT );
	}

	@Override
	public int compareTo(Object o) {
		return 0;
	}

	@Override
	public String toString() {
		String ret = "";
		for (int i = 0; i < vertices.length; i++) {
			ret += " (" +vertices[i].x + ", " +vertices[i].y +")";
		}
		return ret;
	}

	public void setVertices(Vec2[] vertices){
		this.vertices = vertices;
	}
	
	public Vec2[] getVertices(){
		return this.vertices;
	}
	
	public Vec2[] clone(){
		Vec2[] verticesCopy = new Vec2[vertices.length];
		for (int i = 0; i < vertices.length; i++) {
			verticesCopy[i] = new Vec2(vertices[i].x, vertices[i].y);
		}
		return verticesCopy;
	}
}
