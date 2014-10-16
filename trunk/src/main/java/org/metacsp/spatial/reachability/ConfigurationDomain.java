package org.metacsp.spatial.reachability;

import org.metacsp.framework.Domain;
import org.metacsp.time.qualitative.SimpleInterval;


public class ConfigurationDomain  extends Domain {

	private static final long serialVersionUID = -6231986282649879013L;
	private String configurationName;
	
	public ConfigurationDomain(ConfigurationVariable cv) {
		super(cv);
		configurationName = "Interval" + cv.getID();
	}

	@Override
	public int compareTo(Object arg0) {
		if (arg0 instanceof ConfigurationDomain) {
			ConfigurationDomain that = (ConfigurationDomain)arg0;
			return configurationName.compareTo(that.configurationName);
		}
		return 0;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return configurationName;
	}

	
	public String getConfigurationName() { return configurationName; }
	
	/**
     * Compares two time intervals, returning true iff their start and end times coincide.
     * @return True iff the two intervals coincide and the argument is also an {@link SimpleInterval}.
     */
    public boolean equals (Object obj) {
    	return (obj instanceof ConfigurationDomain) && (configurationName.equals(((ConfigurationDomain)obj).getConfigurationName()));
    }
	
}
