package org.metacsp.framework;

import java.awt.Color;

public class DummyVariable extends Variable {

	private String label;
	
	private static int dummyIDs = 0;
	
	private class DummyDomain extends Domain {

		private static final long serialVersionUID = 4097176900671983140L;

		protected DummyDomain(Variable v) {
			super(v);
			// TODO Auto-generated constructor stub
		}

		@Override
		public int compareTo(Object o) {
			// TODO Auto-generated method stub
			return 0;
		}

//		@Override
//		protected void registerValueChoiceFunctions() {
//			// TODO Auto-generated method stub
//			
//		}

		@Override
		public String toString() {
			return "";
		}
		
	}
	
	protected DummyVariable(ConstraintSolver cs, String label) {
		super(cs, dummyIDs++);
		this.setColor(Color.LIGHT_GRAY);
		this.label = label;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 8072923751888165313L;

	@Override
	public int compareTo(Variable arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Domain getDomain() {
		return new DummyDomain(this);
	}

	@Override
	public void setDomain(Domain d) {
		// TODO Auto-generated method stub
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return label;
	}

}
