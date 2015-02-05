package org.metacsp.spatial.geometry;

import cern.colt.Arrays;


@SuppressWarnings("javadoc")
public class Vec2
{

	public float x, y;
	public static float EPSILON = 0.0001f;
	public static float EPSILON_SQ = EPSILON * EPSILON;

	public Vec2()
	{
	}

	public Vec2( float x, float y )
	{
		set( x, y );
	}

	public Vec2( Vec2 v )
	{
		set( v );
	}

	public void set( float x, float y )
	{
		this.x = x;
		this.y = y;
	}

	public Vec2 set( Vec2 v )
	{
		x = v.x;
		y = v.y;
		return this;
	}

	@SuppressWarnings("javadoc")
	/**
	 * Negates this vector and returns this.
	 */
	public Vec2 negi()
	{
		return neg( this );
	}

	@SuppressWarnings("javadoc")
	/**
	 * Sets out to the negation of this vector and returns out.
	 */
	public Vec2 neg( Vec2 out )
	{
		out.x = -x;
		out.y = -y;
		return out;
	}

	@SuppressWarnings("javadoc")
	/**
	 * Returns a new vector that is the negation to this vector.
	 */
	public Vec2 neg()
	{
		return neg( new Vec2() );
	}

	@SuppressWarnings("javadoc")
	/**
	 * Multiplies this vector by s and returns this.
	 */
	public Vec2 muli( float s )
	{
		return mul( s, this );
	}

	@SuppressWarnings("javadoc")
	/**
	 * Sets out to this vector multiplied by s and returns out.
	 */
	public Vec2 mul( float s, Vec2 out )
	{
		out.x = s * x;
		out.y = s * y;
		return out;
	}

	@SuppressWarnings("javadoc")
	/**
	 * Returns a new vector that is a multiplication of this vector and s.
	 */
	public Vec2 mul( float s )
	{
		return mul( s, new Vec2() );
	}

	@SuppressWarnings("javadoc")
	/**
	 * Divides this vector by s and returns this.
	 */
	public Vec2 divi( float s )
	{
		return div( s, this );
	}

	@SuppressWarnings("javadoc")
	/**
	 * Sets out to the division of this vector and s and returns out.
	 */
	public Vec2 div( float s, Vec2 out )
	{
		out.x = x / s;
		out.y = y / s;
		return out;
	}

	@SuppressWarnings("javadoc")
	/**
	 * Returns a new vector that is a division between this vector and s.
	 */
	public Vec2 div( float s )
	{
		return div( s, new Vec2() );
	}

	@SuppressWarnings("javadoc")
	/**
	 * Adds s to this vector and returns this. 
	 */
	public Vec2 addi( float s )
	{
		return add( s, this );
	}

	@SuppressWarnings("javadoc")
	/**
	 * Sets out to the sum of this vector and s and returns out.
	 */
	public Vec2 add( float s, Vec2 out )
	{
		out.x = x + s;
		out.y = y + s;
		return out;
	}

	@SuppressWarnings("javadoc")
	/**
	 * Returns a new vector that is the sum between this vector and s.
	 */
	public Vec2 add( float s )
	{
		return add( s, new Vec2() );
	}

	@SuppressWarnings("javadoc")
	/**
	 * Multiplies this vector by v and returns this.
	 */
	public Vec2 muli( Vec2 v )
	{
		return mul( v, this );
	}

	@SuppressWarnings("javadoc")
	/**
	 * Sets out to the product of this vector and v and returns out.
	 */
	public Vec2 mul( Vec2 v, Vec2 out )
	{
		out.x = x * v.x;
		out.y = y * v.y;
		return out;
	}

	@SuppressWarnings("javadoc")
	/**
	 * Returns a new vector that is the product of this vector and v.
	 */
	public Vec2 mul( Vec2 v )
	{
		return mul( v, new Vec2() );
	}

	@SuppressWarnings("javadoc")
	/**
	 * Divides this vector by v and returns this.
	 */
	public Vec2 divi( Vec2 v )
	{
		return div( v, this );
	}

	@SuppressWarnings("javadoc")
	/**
	 * Sets out to the division of this vector and v and returns out.
	 */
	public Vec2 div( Vec2 v, Vec2 out )
	{
		out.x = x / v.x;
		out.y = y / v.y;
		return out;
	}

	@SuppressWarnings("javadoc")
	/**
	 * Returns a new vector that is the division of this vector by v.
	 */
	public Vec2 div( Vec2 v )
	{
		return div( v, new Vec2() );
	}

	@SuppressWarnings("javadoc")
	/**
	 * Adds v to this vector and returns this.
	 */
	public Vec2 addi( Vec2 v )
	{
		return add( v, this );
	}

	@SuppressWarnings("javadoc")
	/**
	 * Sets out to the addition of this vector and v and returns out.
	 */
	public Vec2 add( Vec2 v, Vec2 out )
	{
		out.x = x + v.x;
		out.y = y + v.y;
		return out;
	}

	@SuppressWarnings("javadoc")
	/**
	 * Returns a new vector that is the addition of this vector and v.
	 */
	public Vec2 add( Vec2 v )
	{
		return add( v, new Vec2() );
	}

	@SuppressWarnings("javadoc")
	/**
	 * Adds v * s to this vector and returns this.
	 */
	public Vec2 addsi( Vec2 v, float s )
	{
		return adds( v, s, this );
	}

	@SuppressWarnings("javadoc")
	/**
	 * Sets out to the addition of this vector and v * s and returns out.
	 */
	public Vec2 adds( Vec2 v, float s, Vec2 out )
	{
		out.x = x + v.x * s;
		out.y = y + v.y * s;
		return out;
	}

	@SuppressWarnings("javadoc")
	/**
	 * Returns a new vector that is the addition of this vector and v * s.
	 */
	public Vec2 adds( Vec2 v, float s )
	{
		return adds( v, s, new Vec2() );
	}

	@SuppressWarnings("javadoc")
	/**
	 * Subtracts v from this vector and returns this.
	 */
	public Vec2 subi( Vec2 v )
	{
		return sub( v, this );
	}

	@SuppressWarnings("javadoc")
	/**
	 * Sets out to the subtraction of v from this vector and returns out.
	 */
	public Vec2 sub( Vec2 v, Vec2 out )
	{
		out.x = x - v.x;
		out.y = y - v.y;
		return out;
	}

	@SuppressWarnings("javadoc")
	/**
	 * Returns a new vector that is the subtraction of v from this vector.
	 */
	public Vec2 sub( Vec2 v )
	{
		return sub( v, new Vec2() );
	}

	@SuppressWarnings("javadoc")
	/**
	 * Returns the squared length of this vector.
	 */
	public float lengthSq()
	{
		return x * x + y * y;
	}

	@SuppressWarnings("javadoc")
	/**
	 * Returns the length of this vector.
	 */
	public float length()
	{
		return (float)StrictMath.sqrt( x * x + y * y );
	}

	@SuppressWarnings("javadoc")
	/**
	 * Rotates this vector by the given radians.
	 */
	public void rotate( float radians )
	{
		float c = (float)StrictMath.cos( radians );
		float s = (float)StrictMath.sin( radians );

		float xp = x * c - y * s;
		float yp = x * s + y * c;

		x = xp;
		y = yp;
	}

	@SuppressWarnings("javadoc")
	/**
	 * Normalizes this vector, making it a unit vector. A unit vector has a length of 1.0.
	 */
	public void normalize()
	{
		float lenSq = lengthSq();

		if (lenSq > EPSILON_SQ)
		{
			float invLen = 1.0f / (float)StrictMath.sqrt( lenSq );
			x *= invLen;
			y *= invLen;
		}
	}

	@SuppressWarnings("javadoc")
	/**
	 * Sets this vector to the minimum between a and b.
	 */
	public Vec2 mini( Vec2 a, Vec2 b )
	{
		return min( a, b, this );
	}

	@SuppressWarnings("javadoc")
	/**
	 * Sets this vector to the maximum between a and b.
	 */
	public Vec2 maxi( Vec2 a, Vec2 b )
	{
		return max( a, b, this );
	}

	@SuppressWarnings("javadoc")
	/**
	 * Returns the dot product between this vector and v.
	 */
	public float dot( Vec2 v )
	{
		return dot( this, v );
	}

	@SuppressWarnings("javadoc")
	/**
	 * Returns the squared distance between this vector and v.
	 */
	public float distanceSq( Vec2 v )
	{
		return distanceSq( this, v );
	}

	@SuppressWarnings("javadoc")
	/**
	 * Returns the distance between this vector and v.
	 */
	public float distance( Vec2 v )
	{
		return distance( this, v );
	}

	@SuppressWarnings("javadoc")
	/**
	 * Sets this vector to the cross between v and a and returns this.
	 */
	public Vec2 cross( Vec2 v, float a )
	{
		return cross( v, a, this );
	}
	
	@SuppressWarnings("javadoc")
	/**
	 * Sets this vector to the cross between a and v and returns this.
	 */
	public Vec2 cross( float a, Vec2 v )
	{
		return cross( a, v, this );
	}

	@SuppressWarnings("javadoc")
	/**
	 * Returns the scalar cross between this vector and v. This is essentially
	 * the length of the cross product if this vector were 3d. This can also
	 * indicate which way v is facing relative to this vector.
	 */
	public float cross( Vec2 v )
	{
		return cross( this, v );
	}

	public static Vec2 min( Vec2 a, Vec2 b, Vec2 out )
	{
		out.x = (float)StrictMath.min( a.x, b.x );
		out.y = (float)StrictMath.min( a.y, b.y );
		return out;
	}

	public static Vec2 max( Vec2 a, Vec2 b, Vec2 out )
	{
		out.x = (float)StrictMath.max( a.x, b.x );
		out.y = (float)StrictMath.max( a.y, b.y );
		return out;
	}

	public static float dot( Vec2 a, Vec2 b )
	{
		return a.x * b.x + a.y * b.y;
	}

	public static float distanceSq( Vec2 a, Vec2 b )
	{
		float dx = a.x - b.x;
		float dy = a.y - b.y;

		return dx * dx + dy * dy;
	}

	public static float distance( Vec2 a, Vec2 b )
	{
		float dx = a.x - b.x;
		float dy = a.y - b.y;

		return (float)StrictMath.sqrt( dx * dx + dy * dy );
	}

	public static Vec2 cross( Vec2 v, float a, Vec2 out )
	{
		out.x = v.y * a;
		out.y = v.x * -a;
		return out;
	}

	public static Vec2 cross( float a, Vec2 v, Vec2 out )
	{
		out.x = v.y * -a;
		out.y = v.x * a;
		return out;
	}

	public static float cross( Vec2 a, Vec2 b )
	{
		return a.x * b.y - a.y * b.x;
	}

	@SuppressWarnings("javadoc")
	/**
	 * Returns an array of allocated Vec2 of the requested length.
	 */
	public static Vec2[] arrayOf( int length )
	{
		Vec2[] array = new Vec2[length];

		while (--length >= 0)
		{
			array[length] = new Vec2();
		}

		return array;
	}
	
	public String toString() {
		return Arrays.toString(new float[] {this.x, this.y});
	}

}

