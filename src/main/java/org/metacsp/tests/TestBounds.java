package org.metacsp.tests;

import java.util.logging.Level;

import junit.framework.TestCase;
import org.metacsp.meta.symbolsAndTime.Scheduler;
import org.metacsp.time.Bounds;
import org.metacsp.utility.logging.MetaCSPLogging;

public class TestBounds extends TestCase {
	
	@Override
	public void setUp() throws Exception {
		MetaCSPLogging.setLevel(Level.OFF);
	}

	@Override
	public void tearDown() throws Exception {
	}

	public void testIntersection() {
		Bounds b1 = new Bounds(0, 10);
		Bounds b2 = new Bounds(5, 10);
		
		Bounds i1 = b1.intersectStrict(b2);
		Bounds i2 = b2.intersectStrict(b1);
		
		assertTrue( i1.min == 5 && i1.max == 10 );
		assertTrue( i2.min == 5 && i2.max == 10 );
		
		assertTrue( b1.intersect(b1).equals(b1));
		assertTrue( b2.intersect(b2).equals(b2));
	}
	
	public void testEmptyIntersection() {
		Bounds b1 = new Bounds(0, 4);
		Bounds b2 = new Bounds(5, 10);

		assertTrue( b1.intersectStrict(b2) == null );
		assertTrue( b2.intersectStrict(b1) == null );
	}
	
	public void testMeetsIntersection() {
		Bounds b1 = new Bounds(0, 5);
		Bounds b2 = new Bounds(5, 10);

		assertTrue( b1.intersectStrict(b2) == null );
		assertTrue( b2.intersectStrict(b1) == null );
	}
}