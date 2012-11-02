package utility.UI;

import java.text.FieldPosition;



	public class PlotDecimalFormat extends java.text.DecimalFormat 
	{ /**
		 * 
		 */
	private static final long serialVersionUID = 1L;
	private long k;

	public PlotDecimalFormat (long k) 
	{ this.k = k;
	} 



	public StringBuffer format ( double number, StringBuffer result, 
	FieldPosition fieldPosition ) 
	{ 
//	 A constant value of k is added befor the transformation 
	return super.format ( number + k, result, fieldPosition ); 
	} 

	} 

