package org.metacsp.spatial.geometry;

public class Mat2
{

	public float m00, m01;
	public float m10, m11;

	/**
	 * Creates a new 2D transformation matrix.
	 */
	public Mat2()
	{
	}

	/**
	 * Creates a new 2D transformation matrix with given rotation.
	 * @param radians Rotation to set.
	 */
	public Mat2( float radians )
	{
		set( radians );
	}

	/**
	 * Creates a new transfomration matrix 
	 * @param a
	 * @param b
	 * @param c
	 * @param d
	 */
	public Mat2( float a, float b, float c, float d )
	{
		set( a, b, c, d );
	}

	/**
	 * Sets this matrix to a rotation matrix with the given radians.
	 * @param radians Rotation in radians.
	 */
	public void set( float radians )
	{
		float c = (float)StrictMath.cos( radians );
		float s = (float)StrictMath.sin( radians );

		m00 = c;
		m01 = -s;
		m10 = s;
		m11 = c;
	}

	@SuppressWarnings("javadoc")
	/**
	 * Sets the values of this matrix.
	 */
	public void set( float a, float b, float c, float d )
	{
		m00 = a;
		m01 = b;
		m10 = c;
		m11 = d;
	}

	@SuppressWarnings("javadoc")
	/**
	 * Sets this matrix to have the same values as the given matrix.
	 */
	public void set( Mat2 m )
	{
		m00 = m.m00;
		m01 = m.m01;
		m10 = m.m10;
		m11 = m.m11;
	}

	@SuppressWarnings("javadoc")
	/**
	 * Sets the values of this matrix to their absolute value.
	 */
	public void absi()
	{
		abs( this );
	}

	@SuppressWarnings("javadoc")
	/**
	 * Returns a new matrix that is the absolute value of this matrix.
	 */
	public Mat2 abs()
	{
		return abs( new Mat2() );
	}

	@SuppressWarnings("javadoc")
	/**
	 * Sets out to the absolute value of this matrix.
	 */
	public Mat2 abs( Mat2 out )
	{
		out.m00 = StrictMath.abs( m00 );
		out.m01 = StrictMath.abs( m01 );
		out.m10 = StrictMath.abs( m10 );
		out.m11 = StrictMath.abs( m11 );
		return out;
	}

	@SuppressWarnings("javadoc")
	/**
	 * Sets out to the x-axis (1st column) of this matrix.
	 */
	public Vec2 getAxisX( Vec2 out )
	{
		out.x = m00;
		out.y = m10;
		return out;
	}

	@SuppressWarnings("javadoc")
	/**
	 * Returns a new vector that is the x-axis (1st column) of this matrix.
	 */
	public Vec2 getAxisX()
	{
		return getAxisX( new Vec2() );
	}

	@SuppressWarnings("javadoc")
	/**
	 * Sets out to the y-axis (2nd column) of this matrix.
	 */
	public Vec2 getAxisY( Vec2 out )
	{
		out.x = m01;
		out.y = m11;
		return out;
	}

	@SuppressWarnings("javadoc")
	/**
	 * Returns a new vector that is the y-axis (2nd column) of this matrix.
	 */
	public Vec2 getAxisY()
	{
		return getAxisY( new Vec2() );
	}

	@SuppressWarnings("javadoc")
	/**
	 * Sets the matrix to it's transpose.
	 */
	public void transposei()
	{
		float t = m01;
		m01 = m10;
		m10 = t;
	}

	@SuppressWarnings("javadoc")
	/**
	 * Sets out to the transpose of this matrix.
	 */
	public Mat2 transpose( Mat2 out )
	{
		out.m00 = m00;
		out.m01 = m10;
		out.m10 = m01;
		out.m11 = m11;
		return out;
	}

	@SuppressWarnings("javadoc")
	/**
	 * Returns a new matrix that is the transpose of this matrix.
	 */
	public Mat2 transpose()
	{
		return transpose( new Mat2() );
	}

	@SuppressWarnings("javadoc")
	/**
	 * Transforms v by this matrix.
	 */
	public Vec2 muli( Vec2 v )
	{
		return mul( v.x, v.y, v );
	}

	@SuppressWarnings("javadoc")
	/**
	 * Sets out to the transformation of v by this matrix.
	 */
	public Vec2 mul( Vec2 v, Vec2 out )
	{
		return mul( v.x, v.y, out );
	}

	@SuppressWarnings("javadoc")
	/**
	 * Returns a new vector that is the transformation of v by this matrix.
	 */
	public Vec2 mul( Vec2 v )
	{
		return mul( v.x, v.y, new Vec2() );
	}

	@SuppressWarnings("javadoc")
	/**
	 * Sets out the to transformation of {x,y} by this matrix.
	 */
	public Vec2 mul( float x, float y, Vec2 out )
	{
		out.x = m00 * x + m01 * y;
		out.y = m10 * x + m11 * y;
		return out;
	}

	@SuppressWarnings("javadoc")
	/**
	 * Multiplies this matrix by x.
	 */
	public void muli( Mat2 x )
	{
		set(
			m00 * x.m00 + m01 * x.m10,
			m00 * x.m01 + m01 * x.m11,
			m10 * x.m00 + m11 * x.m10,
			m10 * x.m01 + m11 * x.m11 );
	}

	@SuppressWarnings("javadoc")
	/**
	 * Sets out to the multiplication of this matrix and x.
	 */
	public Mat2 mul( Mat2 x, Mat2 out )
	{
		out.m00 = m00 * x.m00 + m01 * x.m10;
		out.m01 = m00 * x.m01 + m01 * x.m11;
		out.m10 = m10 * x.m00 + m11 * x.m10;
		out.m11 = m10 * x.m01 + m11 * x.m11;
		return out;
	}

	@SuppressWarnings("javadoc")
	/**
	 * Returns a new matrix that is the multiplication of this and x.
	 */
	public Mat2 mul( Mat2 x )
	{
		return mul( x, new Mat2() );
	}

}

