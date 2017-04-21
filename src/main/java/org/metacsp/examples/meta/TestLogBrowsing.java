package org.metacsp.examples.meta;

import java.io.File;
import java.util.logging.Level;

import org.metacsp.utility.logging.MetaCSPLogging;

public class TestLogBrowsing {
	
	public static void main(String[] args) {
		MetaCSPLogging.setLevel(Level.FINEST);
		File logDir = new File("logTest");
		if (logDir.exists()) logDir.delete();
		logDir.mkdir();
		MetaCSPLogging.setLogDir(logDir.getName());
		TestTrajectoryEnvelopeScheduler.main(null);
		MetaCSPLogging.showLogs(logDir.getName());
	}

}
