package org.metacsp.examples.meta;
import org.metacsp.meta.spatioTemporal.paths.Map;
import org.metacsp.meta.spatioTemporal.paths.TrajectoryEnvelopeScheduler;
import org.metacsp.multi.spatioTemporal.paths.TrajectoryEnvelopeSolver;
import org.metacsp.utility.UI.TrajectoryEnvelopeAnimator;

public class VolvoCEDynamicCoordinationTest8 {
	
	public static void main(String[] args) {
		
		TrajectoryEnvelopeScheduler metaSolver = new TrajectoryEnvelopeScheduler(0, 100000);
		TrajectoryEnvelopeSolver solver = (TrajectoryEnvelopeSolver)metaSolver.getConstraintSolvers()[0];
				
		Map map = new Map(null, null);
		metaSolver.addMetaConstraint(map);
		
		TrajectoryEnvelopeAnimator tea = new TrajectoryEnvelopeAnimator("Coordinated trajecotries of several XA15s");
		tea.setTrajectoryEnvelopeScheduler(metaSolver);
		tea.setTrajectoryEnvelopes(solver.getConstraintNetwork());

	}
	
}
