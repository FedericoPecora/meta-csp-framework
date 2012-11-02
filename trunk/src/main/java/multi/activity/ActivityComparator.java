package multi.activity;

import java.util.Comparator;

public class ActivityComparator implements Comparator<Activity> {
	
	private boolean EST;
	public ActivityComparator(boolean EST) {
		super();
		this.EST = EST;
	}
	
	@Override
	public int compare(Activity o1, Activity o2) {
		if (EST) return (int)(o1.getTemporalVariable().getEST()-o2.getTemporalVariable().getEST());
		return (int)(o1.getTemporalVariable().getEET()-o2.getTemporalVariable().getEET());
	}
}
