package spatial.RCC;

import framework.Domain;


public class Rectangle extends Domain {

	private int width = 0;
	private int height = 0;
	private String[] intervalName = new String[2]; 
	
	public Rectangle(Region v) {
		super(v);
		intervalName[0] = "XInterval" + v.getID();
		intervalName[1] = "YInterval" + v.getID();
	}

	public String getIntervalsName() { return intervalName[0] + " " + intervalName[1]; }
	
	public String getXInterval() { return intervalName[0]; }
	public String getYInterval() { return intervalName[1]; }
	
	@Override
	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected void registerValueChoiceFunctions() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String toString() {
		return intervalName[0] +  " " + intervalName[1];
	}
	
	public int getHeight() {
		return height;
	}
	
	public int getWidth() {
		return width;
	}
	
	
	
}