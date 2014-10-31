package org.metacsp.spatial.geometry;
import java.util.*;

 
public class SutherlandHodgman{

	List<double[]> subject, clipper, result; 
    public SutherlandHodgman(Polygon p1, Polygon p2) {
//    	double[][] clipPoints = {{100, 100}, {150, 80}, {300, 130}, {320, 300},
//        {200, 220}};
//        double[][] subjPoints = {{80, 80}, {360, 80}, {360, 360}, {80, 360}};
        
        //clipper is p1
        //subject is p2
    	double[][] clipPoints = new double[p1.getVertexCount()][2];
//        Vec2[] p1vec = ((Vertex)p1.getDomain()).getVertices();
    	Vec2[] p1vec = p1.getFullSpaceRepresentation().toArray(new Vec2[p1.getFullSpaceRepresentation().size()]);
    	for (int i = 0; i < clipPoints.length; i++) {
        	clipPoints[i][0] = p1vec[i].x;
        	clipPoints[i][1] = p1vec[i].y;
		}
    	double[][] subjPoints = new double[p2.getVertexCount()][2];
//        Vec2[] p2vec = ((Vertex)p2.getDomain()).getVertices();
    	Vec2[] p2vec = p2.getFullSpaceRepresentation().toArray(new Vec2[p2.getFullSpaceRepresentation().size()]);
        for (int i = 0; i < subjPoints.length; i++) {
        	subjPoints[i][0] = p2vec[i].x;
        	subjPoints[i][1] = p2vec[i].y;
		}
        
        subject = new ArrayList<double[]>(Arrays.asList(subjPoints));
        result  = new ArrayList<double[]>(subject);
        clipper = new ArrayList<double[]>(Arrays.asList(clipPoints));
 
        clipPolygon();
        
//        for (int i = 0; i < subject.size(); i++) {
//				System.out.println(subject.get(i)[0] + " " + subject.get(i)[1]);
//		}
//        System.out.println();
//        for (int i = 0; i < clipper.size(); i++) {
//				System.out.println(clipper.get(i)[0] + " " + clipper.get(i)[1]);
//		}        
//        System.out.println();
//        for (int i = 0; i < result.size(); i++) {
//				System.out.println(result.get(i)[0] + " " + result.get(i)[1]);
//		}
    }
 
    public Vec2[] getClippedResult(){
    	Vec2[] ret = new Vec2[result.size()];
    	for (int i = 0; i < result.size(); i++) {
    		ret[i] = new Vec2((float)result.get(i)[0], (float)result.get(i)[1]);
//			System.out.println(result.get(i)[0] + " " + result.get(i)[1]);
    	}
    	return ret;
    }
    
    private void clipPolygon() {
        int len = clipper.size();
        for (int i = 0; i < len; i++) {
 
            int len2 = result.size();
            List<double[]> input = result;
            result = new ArrayList<double[]>(len2);
 
            double[] A = clipper.get((i + len - 1) % len);
            double[] B = clipper.get(i);
 
            for (int j = 0; j < len2; j++) {
 
                double[] P = input.get((j + len2 - 1) % len2);
                double[] Q = input.get(j);
 
                if (isInside(A, B, Q)) {
                    if (!isInside(A, B, P))
                        result.add(intersection(A, B, P, Q));
                    result.add(Q);
                } else if (isInside(A, B, P))
                    result.add(intersection(A, B, P, Q));
            }
        }
    }
 
    private boolean isInside(double[] a, double[] b, double[] c) {
        return (a[0] - c[0]) * (b[1] - c[1]) > (a[1] - c[1]) * (b[0] - c[0]);
    }
 
    private double[] intersection(double[] a, double[] b, double[] p, double[] q) {
        double A1 = b[1] - a[1];
        double B1 = a[0] - b[0];
        double C1 = A1 * a[0] + B1 * a[1];
 
        double A2 = q[1] - p[1];
        double B2 = p[0] - q[0];
        double C2 = A2 * p[0] + B2 * p[1];
 
        double det = A1 * B2 - A2 * B1;
        double x = (B2 * C1 - B1 * C2) / det;
        double y = (A1 * C2 - A2 * C1) / det;
 
        return new double[]{x, y};
    }

}