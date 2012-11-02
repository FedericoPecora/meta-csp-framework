package spatial.rectangleAlgebra;


public class TwoDimensionsAllenConstraint extends QualitativeAllenIntervalConstraint {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8168190298662846847L;
	private QualitativeAllenIntervalConstraint.Type[] con = new QualitativeAllenIntervalConstraint.Type[2];
	
	public TwoDimensionsAllenConstraint(QualitativeAllenIntervalConstraint.Type xcon, QualitativeAllenIntervalConstraint.Type ycon){
		this.con[0] = xcon;
		this.con[1] = ycon;
	}
	
	public QualitativeAllenIntervalConstraint.Type[] getAllenType() {
		return con;
	}
	
	@Override
	public String toString() {
		String ret = "[";
		for (int i = 0; i < types.length; i++) {
			ret +="(" + this.getFrom() + ") --" + "(" + this.con[0] + ", " + this.con[1] + ")" +"--> (" + this.getTo() + ")"; 
		}
		ret += "]";
		return ret;

	}
}




