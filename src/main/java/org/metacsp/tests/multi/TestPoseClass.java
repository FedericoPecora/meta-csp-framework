package org.metacsp.tests.multi;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Random;

import org.metacsp.multi.spatioTemporal.paths.Pose;

public class TestPoseClass {
	public static void main(String[] args) throws InterruptedException {
		
		int numTests = 1000;
		Random rand = new Random(Calendar.getInstance().getTimeInMillis());
			
		//2D compared to 2D
		for (int i = 0; i < numTests; i++) {
			double yaw = -Math.PI + 2*Math.PI * rand.nextDouble();
			Pose p1 = new Pose(rand.nextDouble(),rand.nextDouble(),yaw);
			Pose p2 = new Pose(p1.getX(),p1.getY(),p1.getTheta());
			double x = p1.getX() == 0 ? 1 : 2*p1.getX();
			Pose p3 = new Pose(x,0,0);
			assertTrue(p1.isPose2D());
			assertTrue(p2.isPose2D());
			assertTrue(p3.isPose2D());
			assertTrue(p1.equals(p2));
			assertFalse(p1.equals(p3));
		}
		
		//3D compared to 3D
		for (int i = 0; i < numTests; i++) {
			double roll1 = -Math.PI + 2*Math.PI * rand.nextDouble();
			double pitch1 = -Math.PI + 2*Math.PI * rand.nextDouble();
			double yaw1 = -Math.PI + 2*Math.PI * rand.nextDouble();
			Pose p1 = new Pose(rand.nextDouble(),rand.nextDouble(),rand.nextDouble(),roll1,pitch1,yaw1);
			Pose p2 = new Pose(p1.getX(),p1.getY(),p1.getZ(),p1.getRoll(),p1.getPitch(),p1.getYaw());
			double x3 = p1.getX() == 0 ? 1 : 2*p1.getX();
			Pose p3 = new Pose(x3,0,0,0,0,0);
			assertFalse(p1.isPose2D());
			assertFalse(p2.isPose2D());
			assertFalse(p3.isPose2D());
			assertTrue(p1.equals(p2));
			assertFalse(p1.equals(p3));
		}
		
		//2D compared to 3D: throw errors
		for (int i = 0; i < numTests; i++) {
			double roll = -Math.PI + 2*Math.PI * rand.nextDouble();
			double pitch = -Math.PI + 2*Math.PI * rand.nextDouble();
			double yaw = -Math.PI + 2*Math.PI * rand.nextDouble();
			Pose p1 = new Pose(rand.nextDouble(), rand.nextDouble(), rand.nextDouble(), roll, pitch, yaw);
			Pose p2 = new Pose(p1.getX(), p1.getY(), p1.getTheta());
			assertFalse(p1.isPose2D());
			assertTrue(p2.isPose2D());
			p1.equals(p2);
			/* For debugging. Tested.
			 * boolean gotExp = false;
			try { p1.equals(p2); }
			catch (Exception e) {
				System.out.println(e.toString());
				gotExp = true;
			}
			assertTrue(gotExp);*/
		}
	}

}
