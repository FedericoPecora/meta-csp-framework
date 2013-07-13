/*******************************************************************************
 * Copyright (c) 2010-2013 Federico Pecora <federico.pecora@oru.se>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package time.qualitative;
import framework.Domain;
import framework.ValueChoiceFunction;

/**
 * Represents intervals of time without metric extension. 
 * Used as a domain for the {@link SimpleAllenInterval}.  
 * @author Federico Pecora
 */
public class SimpleInterval extends Domain {

	
	private String intervalName;
	
    /**
     * Builds a zero-length time interval (start = stop = 0).
     */
    public SimpleInterval(SimpleAllenInterval sai) {
    	super(sai);
    	intervalName = "Interval" + sai.getID();
    }
    
//	@Override
//	protected void registerValueChoiceFunctions() {
//		this.registerValueChoiceFunction(new ValueChoiceFunction(){
//			@Override
//			public Object getValue(Domain dom) {
//				return intervalName;
//			}}, "ID");
//	}
	
	public String getIntervalName() { return intervalName; }
	
	/**
     * Compares two time intervals, returning true iff their start and end times coincide.
     * @return True iff the two intervals coincide and the argument is also an {@link SimpleInterval}.
     */
    public boolean equals (Object obj) {
    	return (obj instanceof SimpleInterval) && (intervalName.equals(((SimpleInterval)obj).getIntervalName()));
    }

    /**
     * Returns A String representation of the {@link SimpleInterval}.
     * @return A String describing the {@link SimpleInterval}.
     */
	public String toString () {
		return intervalName;
	}

	/**
	 * Comapres this to another Interval
	 * @return {@code 1} if this Interval's lower bound is lower than the argument's, {@code 0} if they are the same, {@code 1} otherwise  
	 */
	@Override
	public int compareTo(Object arg0) {
		if (arg0 instanceof SimpleInterval) {
			SimpleInterval that = (SimpleInterval)arg0;
			return intervalName.compareTo(that.getIntervalName());
		}
		return 0;
	}
	
	@Override
	public int hashCode() {
		return intervalName.hashCode();
	}
	


}

