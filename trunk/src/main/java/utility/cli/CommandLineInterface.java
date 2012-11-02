package utility.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Vector;

import jline.ArgumentCompletor;
import jline.Completor;
import jline.ConsoleReader;
import jline.FileNameCompletor;
import jline.MultiCompletor;
import jline.NullCompletor;
import jline.SimpleCompletor;
import multi.fuzzyActivity.SimpleTimeline;
import onLineMonitoring.DomainDescription;
import onLineMonitoring.FuzzySensorEvent;
import onLineMonitoring.Hypothesis;
import onLineMonitoring.HypothesisListener;
import onLineMonitoring.MonitoredComponent;
import onLineMonitoring.PhysicalSensor;
import onLineMonitoring.Requirement;
import onLineMonitoring.Rule;

import org.naturalcli.Command;
import org.naturalcli.ExecutionException;
import org.naturalcli.ICommandExecutor;
import org.naturalcli.InvalidSyntaxException;
import org.naturalcli.NaturalCLI;
import org.naturalcli.ParseResult;
import org.naturalcli.commands.HelpCommand;

import symbols.SymbolicValueConstraint;
import framework.ConstraintNetwork;
import fuzzyAllenInterval.FuzzyAllenIntervalConstraint;

public class CommandLineInterface implements HypothesisListener {

	private HashSet<Command> cs = new HashSet<Command>();
	private LinkedHashMap<String,PhysicalSensor> sensors = new LinkedHashMap<String,PhysicalSensor>();
	private LinkedHashMap<String,MonitoredComponent> monitoredComponents = new LinkedHashMap<String,MonitoredComponent>();
	private LinkedHashMap<Integer,Requirement> requirements = new LinkedHashMap<Integer,Requirement>();
	private LinkedHashMap<Integer,FuzzySensorEvent> events = new LinkedHashMap<Integer,FuzzySensorEvent>();
	private Vector<Completor> completors = new Vector<Completor>();
	private Vector<Rule> rules = new Vector<Rule>();
	public SimpleCompletor sensorCompletions = new SimpleCompletor("");
	public SimpleCompletor monitoredCompletions = new SimpleCompletor("");
	public SimpleCompletor stateCompletions = new SimpleCompletor("");
	public SimpleCompletor requirementCompletions = new SimpleCompletor("");
	public FileNameCompletor filenameCompletions = new FileNameCompletor();
	public SimpleCompletor timeCompletions = new SimpleCompletor("<time>");
	public SimpleCompletor possibilityCompletions = new SimpleCompletor("<poss>");
	public SimpleCompletor thresholdCompletions = new SimpleCompletor("<threshold>");
	public SimpleCompletor maxHypothesesCompletions = new SimpleCompletor("<maxHypotheses>");
	public SimpleCompletor simulateEventsCompletions = new SimpleCompletor("<simulateEvents>");
	private DomainDescription dd = null;
	private boolean logging = false;
	private PrintWriter out = null;
	private PrintWriter outfile = null;
	private boolean success = false;
	
	private static boolean debug = true;
	
	public CommandLineInterface() {
		commandSetup();
		
		//Default commands
		createCompletor("help");
		cs.add(new HelpCommand(cs)); // help
		
		startPrompt();
	}

	private void createCompletor(String args) {
		String[] tokens = args.split(" ");
		Completor[] compl = new Completor[tokens.length+1];
		for (int i = 0; i < tokens.length; i++) {
			compl[i] = new SimpleCompletor(tokens[i]);
		}
		compl[tokens.length] = new NullCompletor();
		ArgumentCompletor c = new ArgumentCompletor(compl);
		c.setStrict(true);
		completors.add(c);			
	}
	
	private void createCompletor(String commandName, Object[]... args) {
		String[] tokens = commandName.split(" ");
		Completor[] compl = new Completor[tokens.length+args.length+1];
		for (int i = 0; i < tokens.length; i++) {
			compl[i] = new SimpleCompletor(tokens[i]);
		}
		for (int i = 0; i < args.length; i++) {
			//vector case
			if (args[i].length == 1 &&
					args[i][0] instanceof String &&
					((String)args[i][0]).startsWith("<") &&
					( ((String)args[i][0]).endsWith(">") || ((String)args[i][0]).endsWith("...") )) {
				String arg = ((String)args[i][0]).substring(1, ((String)args[i][0]).indexOf(':'));
				try {
					Completor completor = (Completor)this.getClass().getField(arg+"Completions").get(this);
					compl[i+tokens.length] = completor;
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchFieldException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else {
				//normal case
				String[] strings = new String[args[i].length];
				for (int j = 0; j < strings.length; j++) {
					strings[j] = args[i][j].toString();
				}
				compl[i+tokens.length] = new SimpleCompletor(strings);
			}
		}
		compl[tokens.length+args.length] = new NullCompletor();
		ArgumentCompletor  ac = new ArgumentCompletor(compl);
		ac.setStrict(true);
		completors.add(ac);
	}

	private void commandSetup() {
		Command command = null;
		String commandName = null;

		//Command: create sensor
		commandName = "create sensor";
		createCompletor(commandName);
		try {
			command = new Command(
					commandName + " <name:string> ...", 
					"Creates a sensor with given possible states.", 
					new ICommandExecutor () {
						public void execute(ParseResult pr) {
							String sensorName = "";
							String[] sensorStates = new String[pr.getParameterCount()-1];
							for (int i = 0 ; i < pr.getParameterCount() ; i++) {
								if (i == 0) sensorName = (String)pr.getParameterValue(i);
								else {
									sensorStates[i-1] = (String)pr.getParameterValue(i);
									stateCompletions.addCandidateString(sensorStates[i-1]);
								}
							}
							PhysicalSensor s = new PhysicalSensor(sensorName, sensorStates);
							sensors.put(sensorName, s);
							sensorCompletions.addCandidateString(sensorName);
							success = true;
						}
					}		
					);
			cs.add(command);
		} catch (InvalidSyntaxException e) { e.printStackTrace(); }


		//Command: create monitored
		commandName = "create monitored";
		createCompletor(commandName);
		try {
			command = new Command(
					commandName + " <name:string> ...", 
					"Creates a monitored component with given possible states.", 
					new ICommandExecutor () {
						public void execute(ParseResult pr) {
							String monName = "";
							String[] monStates = new String[pr.getParameterCount()-1];
							for (int i = 0 ; i < pr.getParameterCount() ; i++) {
								if (i == 0) monName = (String)pr.getParameterValue(i);
								else {
									monStates[i-1] = (String)pr.getParameterValue(i);
									stateCompletions.addCandidateString(monStates[i-1]);
								}
							}
							MonitoredComponent m = new MonitoredComponent(monName, monStates);
							monitoredComponents.put(monName, m);
							monitoredCompletions.addCandidateString(monName);
							success = true;
						}
					}		
					);
			cs.add(command);
		} catch (InvalidSyntaxException e) { e.printStackTrace(); }


		//Command: list sensors
		commandName = "list sensors";
		createCompletor(commandName);
		try {
			command = new Command(
					commandName, 
					"Lists existing sensors.", 
					new ICommandExecutor () {
						public void execute(ParseResult pr) {
							for (String s : sensors.keySet()) {
								System.out.println(sensors.get(s).getName() + " : " + Arrays.toString(sensors.get(s).getStates()));
							}
						}
					}		
					);
			cs.add(command);
		} catch (InvalidSyntaxException e) { e.printStackTrace(); }

		
		//Command: list monitored comps
		commandName = "list monitored";
		createCompletor(commandName);
		try {
			command = new Command(
					commandName, 
					"Lists existing sensors.", 
					new ICommandExecutor () {
						public void execute(ParseResult pr) {
							for (String s : monitoredComponents.keySet()) {
								System.out.println(monitoredComponents.get(s).getName() + " : " + Arrays.toString(monitoredComponents.get(s).getStates()));
							}
						}
					}		
					);
			cs.add(command);
		} catch (InvalidSyntaxException e) { e.printStackTrace(); }

		
		//Command: list requirements
		commandName = "list requirements";
		createCompletor(commandName);
		try {
			command = new Command(
					commandName, 
					"Lists created requirements.", 
					new ICommandExecutor () {
						public void execute(ParseResult pr) {
							for (Integer i : requirements.keySet()) {
								System.out.println(i + " : " + requirements.get(i));
							}
						}
					}		
					);
			cs.add(command);
		} catch (InvalidSyntaxException e) { e.printStackTrace(); }

		
		//Command: create requirement on sensor
		commandName = "create requirement";
		createCompletor(commandName, new String[] {"<sensor:string>"}, new String[] {"<state:string>"}, SymbolicValueConstraint.Type.values(), FuzzyAllenIntervalConstraint.Type.values());
		try {
			command = new Command(
					commandName + " <sensor:string> <state:string> <valueConstraint:string> <temporalConstraint:string>", 
					"Creates a requirement on a given sensor state with given constraints.", 
					new ICommandExecutor () {
						public void execute(ParseResult pr) {
							String sensorName = (String)pr.getParameterValue(0);
							PhysicalSensor sensor = sensors.get(sensorName);
							if (sensor == null) {
								//The head could be a monitored component - in this case,
								//make a ghost sensor that holds best scoring hypothesis
								String[] states = monitoredComponents.get(sensorName).getStates();
								String statesNew = "";
								for (String st : states) statesNew += (st + " ");
								CommandLineInterface.this.processCommand("create sensor _" + sensorName + " " + statesNew);
								sensor = sensors.get("_" + sensorName);
							}
							//else {
							if (sensor != null) {
								double[] poss = new double[sensor.getStates().length];
								for (int i = 0; i < poss.length; i++) {
									if (sensor.getStates()[i].equals((String)pr.getParameterValue(1))) poss[i] = 1.0;
									else poss[i] = 0.0;
								}
								SymbolicValueConstraint.Type vt = SymbolicValueConstraint.Type.valueOf((String)pr.getParameterValue(2));
								FuzzyAllenIntervalConstraint.Type tt = FuzzyAllenIntervalConstraint.Type.valueOf((String)pr.getParameterValue(3));
								Requirement req = new Requirement(sensor,poss,vt,tt);
								requirements.put(requirements.keySet().size(), req);
								requirementCompletions.addCandidateString("" + (requirements.keySet().size()-1));
								success = true;
							}
							else {
								System.out.println("Sensor/monitored " + sensorName + " not found");
							}
						}
					}		
					);
			cs.add(command);
		} catch (InvalidSyntaxException e) { e.printStackTrace(); }
		

		//Command: create rule
		commandName = "create rule";
		createCompletor(commandName, new String[] {"<monitored:string>"}, new String[] {"<state:string>"}, new String[] {"<requirement:integer> ..."});
		try {
			command = new Command(
					commandName + " <monitored:string> <state:string> <requirement:integer> ...", 
					"Creates a rule on a given monitored component state with given requirements.", 
					new ICommandExecutor () {
						public void execute(ParseResult pr) {
							String monName = (String)pr.getParameterValue(0);
							MonitoredComponent mon = monitoredComponents.get(monName);
							double[] poss = new double[mon.getStates().length];
							for (int i = 0; i < poss.length; i++) {
								if (mon.getStates()[i].equals((String)pr.getParameterValue(1))) poss[i] = 1.0;
								else poss[i] = 0.0;
							}
							Requirement[] reqs = new Requirement[pr.getParameterCount()-2];
							for (int i = 0; i < reqs.length; i++) {
								int index = (Integer)pr.getParameterValue(i+2);
								reqs[i] = requirements.get(index);
							}
							Rule r = new Rule(mon, poss, reqs);
							rules.add(r);
							success = true;
						}
					}		
					);
			cs.add(command);
		} catch (InvalidSyntaxException e) { e.printStackTrace(); }

		
		//Command: list rules
		commandName = "list rules";
		createCompletor(commandName);
		try {
			command = new Command(
					commandName, 
					"Lists created rules.", 
					new ICommandExecutor () {
						public void execute(ParseResult pr) {
							for (Rule r : rules) {
								System.out.println(rules.indexOf(r) + " : " + r);
							}
						}
					}		
					);
			cs.add(command);
		} catch (InvalidSyntaxException e) { e.printStackTrace(); }
		
		
		//Command: log start filename
		commandName = "log start";
		createCompletor(commandName, new String[] {"<filename:string>"});
		try {
			command = new Command(
					commandName + " <filename:string>", 
					"Start writing log to a file.", 
					new ICommandExecutor () {
						public void execute(ParseResult pr) {
							File file = new File((String)pr.getParameterValue(0));
						    try {
								out = new PrintWriter(file);
							} catch (FileNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							logging = true;
						}
					}		
					);
			cs.add(command);
		} catch (InvalidSyntaxException e) { e.printStackTrace(); }
		

		//Command: output filename
		commandName = "output";
		createCompletor(commandName, new String[] {"<filename:string>"});
		try {
			command = new Command(
					commandName + " <filename:string>", 
					"Write output of SAM to a file (when monitoring loop is active).", 
					new ICommandExecutor () {
						public void execute(ParseResult pr) {
							File file = new File((String)pr.getParameterValue(0));
						    try {
								outfile = new PrintWriter(file);
							} catch (FileNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}		
					);
			cs.add(command);
		} catch (InvalidSyntaxException e) { e.printStackTrace(); }
		

		//Command: log stop
		commandName = "log stop";
		createCompletor(commandName);
		try {
			command = new Command(
					commandName, 
					"Stop writing log.", 
					new ICommandExecutor () {
						public void execute(ParseResult pr) {
							if (out != null) out.close();
							logging = false;
						}
					}		
					);
			cs.add(command);
		} catch (InvalidSyntaxException e) { e.printStackTrace(); }

		
		//Command: load
		commandName = "load";
		createCompletor(commandName, new String[] {"<filename:string>"});
		try {
			command = new Command(
					commandName + " <filename:string>", 
					"Load the given log file.", 
					new ICommandExecutor () {
						public void execute(ParseResult pr) {
							BufferedReader in = null;
							boolean fileNotFound = false;
							try {
								in = new BufferedReader(new FileReader((String)pr.getParameterValue(0)));
							} catch (FileNotFoundException e) { fileNotFound = true; System.out.println((String)pr.getParameterValue(0) + " not found."); }
							if (!fileNotFound) {
							    String str;
							    try {
									while ((str = in.readLine()) != null) {
									    processCommand(str);
									}
								} catch (IOException e) { e.printStackTrace(); }
							    try {
									in.close();
								} catch (IOException e) { e.printStackTrace(); }
							}
						}
					}		
					);
			cs.add(command);
		} catch (InvalidSyntaxException e) { e.printStackTrace(); }
		
		
		//Command: create event
		commandName = "create event";
		createCompletor(commandName, new String[] {"<sensor:string>"}, new String[] {"<time:integer>"}, new String[] {"<possibility:double> ..."});
		try {
			command = new Command(
					commandName + " <sensor:string> <time:integer> <possibility:double> ...", 
					"Creates a sensor event on a given sensor with given possibilities.", 
					new ICommandExecutor () {
						public void execute(ParseResult pr) {
							double[] poss = new double[pr.getParameterCount()-2];
							for (int i = 0; i < pr.getParameterCount()-2; i++) {
								poss[i] = (Double)pr.getParameterValue(i+2);
							}
							FuzzySensorEvent fse = new FuzzySensorEvent(sensors.get((String)pr.getParameterValue(0)), poss, (Integer)pr.getParameterValue(1));
							events.put(new Integer(events.size()), fse);
							if (dd != null) {
								dd.addFuzzySensorEvents(fse);
							}
							success = true;
						}
					}		
					);
			cs.add(command);
		} catch (InvalidSyntaxException e) { e.printStackTrace(); }
		
		
		//Command: list events
		commandName = "list events";
		createCompletor(commandName);
		try {
			command = new Command(
					commandName, 
					"List all sensor events.", 
					new ICommandExecutor () {
						public void execute(ParseResult pr) {
							for (Integer i : events.keySet()) {
								System.out.println(i + " : " + events.get(i));
							}
						}
					}		
					);
			cs.add(command);
		} catch (InvalidSyntaxException e) { e.printStackTrace(); }
		
		
		//Command: monitoring start threshold (double)
		commandName = "monitoring start threshold";
		createCompletor(commandName, new String[] {"<threshold:double>"}, new String[] {"<simulateEvents:string>"});
		try {
			final CommandLineInterface enclosing = this;
			command = new Command(
					commandName + " <threshold:double> <simulateEvents:string>", 
					"Start monitoring loop with a given minimum possibility for the hypotheses to return.", 
					new ICommandExecutor () {
						public void execute(ParseResult pr) {
							dd = new DomainDescription();
							dd.addRules(rules.toArray(new Rule[rules.size()]));
							if (((String)pr.getParameterValue(1)).equals("true"))
								dd.setOptions(DomainDescription.OPTIONS.SIMULATE_SENSOR_DISPATCH);
							else
								dd.setOptions(DomainDescription.OPTIONS.NO_SENSOR_DISPATCH);
							dd.registerHypothesisListener(enclosing, (Double)pr.getParameterValue(0));
							dd.startMonitoring();
							dd.addFuzzySensorEvents(events.values().toArray(new FuzzySensorEvent[events.size()]));
						}
					}		
					);
			cs.add(command);
		} catch (InvalidSyntaxException e) { e.printStackTrace(); }


		//Command: monitoring start max int boolean
		commandName = "monitoring start max";
		createCompletor(commandName, new String[] {"<maxHypotheses:integer>"}, new String[] {"<simulateEvents:string>"});
		try {
			final CommandLineInterface enclosing = this;
			command = new Command(
					commandName + " <maxHypotheses:integer> <simulateEvents:string>", 
					"Start monitoring loop with a maximum number of hypotheses to return.", 
					new ICommandExecutor () {
						public void execute(ParseResult pr) {
							dd = new DomainDescription();
							dd.addRules(rules.toArray(new Rule[rules.size()]));
							if (((String)pr.getParameterValue(1)).equals("true"))
								dd.setOptions(DomainDescription.OPTIONS.SIMULATE_SENSOR_DISPATCH);
							else
								dd.setOptions(DomainDescription.OPTIONS.NO_SENSOR_DISPATCH);
							dd.registerHypothesisListener(enclosing, (Integer)pr.getParameterValue(0));
							dd.startMonitoring();
							dd.addFuzzySensorEvents(events.values().toArray(new FuzzySensorEvent[events.size()]));
						}
					}		
					);
			cs.add(command);
		} catch (InvalidSyntaxException e) { e.printStackTrace(); }
		
		
		//Command: monitoring stop
		commandName = "monitoring stop";
		createCompletor(commandName);
		try {
			command = new Command(
					commandName, 
					"Stop the monitoring loop.", 
					new ICommandExecutor () {
						public void execute(ParseResult pr) {try {
							dd.stopMonitoring();
							System.out.println("Monitoring stopped.");
						}
						catch (NullPointerException e1) { System.out.println("Monitoring not started."); }
						}
					}		
					);
			cs.add(command);
		} catch (InvalidSyntaxException e) { e.printStackTrace(); }
		
		
		//Command: monitoring pause
		commandName = "monitoring pause";
		createCompletor(commandName);
		try {
			command = new Command(
					commandName, 
					"Pause the monitoring loop.", 
					new ICommandExecutor () {
						public void execute(ParseResult pr) {try {
							dd.pauseMonitoring();
							System.out.println("Monitoring paused.");
						}
						catch (NullPointerException e1) { System.out.println("Monitoring not started."); }
						}
					}		
					);
			cs.add(command);
		} catch (InvalidSyntaxException e) { e.printStackTrace(); }
		
		
		//Command: monitoring resume
		commandName = "monitoring resume";
		createCompletor(commandName);
		try {
			command = new Command(
					commandName, 
					"Resume paused monitoring loop.", 
					new ICommandExecutor () {
						public void execute(ParseResult pr) {try {
							dd.resumeMonitoring();
							System.out.println("Monitoring resumed.");
						}
						catch (NullPointerException e1) { System.out.println("Monitoring not started."); }
						}
					}		
					);
			cs.add(command);
		} catch (InvalidSyntaxException e) { e.printStackTrace(); }
		
		
		//Command: time
		commandName = "time";
		createCompletor(commandName);
		try {
			command = new Command(
					commandName, 
					"Get time elapsed from monitoring start (in seconds).", 
					new ICommandExecutor () {
						public void execute(ParseResult pr) {
							try {
								System.out.println(dd.getTime());
							}
							catch (NullPointerException e1) { System.out.println("Monitoring not started."); }
						}
					}		
					);
			cs.add(command);
		} catch (InvalidSyntaxException e) { e.printStackTrace(); }
		
		
		//Command: show timelines
		commandName = "show timelines";
		createCompletor(commandName);
		try {
			command = new Command(
					commandName, 
					"Get the sensor timelines.", 
					new ICommandExecutor () {
						public void execute(ParseResult pr) {
							try {
								for (SimpleTimeline tl : dd.getTimelines())
								System.out.println(tl);
							}
							catch (NullPointerException e1) { System.out.println("Monitoring not started."); }
						}
					}		
					);
			cs.add(command);
		} catch (InvalidSyntaxException e) { e.printStackTrace(); }
		
		//Command: show network
		commandName = "show network";
		createCompletor(commandName);
		try {
			command = new Command(
					commandName, 
					"Get the sensor constraint network.", 
					new ICommandExecutor () {
						public void execute(ParseResult pr) {
							try {
								ConstraintNetwork.draw(dd.getConstraintNetwork(), "Sensor readings");
							}
							catch (NullPointerException e1) { System.out.println("Monitoring not started."); }
						}
					}		
					);
			cs.add(command);
		} catch (InvalidSyntaxException e) { e.printStackTrace(); }


	}


	private void processCommand(String input) {
		if (!input.startsWith("#") && !input.trim().equals("")) {
			try {
				NaturalCLI ncli = new NaturalCLI(cs);
				ncli.execute(input);
				if (logging && success) {
					success = false;
					out.println(input);
				}
			} catch (ExecutionException e) { System.out.println(e.getMessage()); }
		}
	}

	private void startPrompt() {
		try {
			ConsoleReader reader = new ConsoleReader();
			reader.setBellEnabled(true);
			MultiCompletor mc = new MultiCompletor(completors.toArray(new Completor[completors.size()]));
			reader.addCompletor(mc);

			if (!debug) {
				String line;
				reader.clearScreen();
				System.out.println("Welcome to SAM-shell");
				System.out.println("(type 'help' for a list of commands)");
				System.out.println();
				while ((line = reader.readLine("SAM-sh> ")) != null) {
					if (line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit")) {
						if (out != null) out.close();
						if (outfile != null) outfile.close();
						if (dd != null) dd.stopMonitoring();
						break;
					}
					if (!line.equalsIgnoreCase("")) processCommand(line);
				}
			}

		} catch (IOException e) { e.printStackTrace(); }			
	}

	public static void main(String[] args) {
		CommandLineInterface cli = new CommandLineInterface();		
		if (debug) debug(cli);
	}

	@Override
	public void processHypotheses(Hypothesis[] hypotheses) {
		
		if (outfile != null) {
			outfile.println("=====");
			for (SimpleTimeline stl : dd.getTimelines()) outfile.println(stl);
		}

		for (int i = 0; i < hypotheses.length; i++) {
			String outS = "Hyp " + i + " (pass " + hypotheses[i].getPass() + "): " + hypotheses[i] +
					"\n\tminInterval = " + dd.getMinInterval(hypotheses[i]); 
			System.out.println(outS);
			if (outfile != null) outfile.println(outS);
			//ConstraintNetwork.draw(hypotheses[i].getConstraintNetwork(), hypotheses[i].toCompactString()+" "+dd.getMinInterval(hypotheses[i]));//iran
		}
	}
	
	private static void debug(CommandLineInterface cli) {
		
		/*
		cli.processCommand("load /home/fpa/svnroot.aass/MetaCSPFramework/trunk/dist/martinTest1.log");
		cli.processCommand("create event containerSensor1 3 0.0 1.0");
		cli.processCommand("create event containerSensor1 9 1.0 0.0");
		cli.processCommand("create event containerSensor2 10 1.0 0.0");
		//cli.processCommand("create event containerSensor2 15 0.0 1.0");

		cli.processCommand("monitoring start max 10 false");
		cli.processCommand("show network");
		*/
		
		/*
		cli.processCommand("load /home/fpa/svnroot.aass/MetaCSPFramework/trunk/dist/simpleTest.log");
		cli.processCommand("monitoring start max 10 false");
		*/
		
		
		
//		cli.processCommand("load /home/fpa/svnroot.aass/MetaCSPFramework/trunk/dist/martinTest.log");
//		cli.processCommand("monitoring start max 1 false");
//		cli.processCommand("show network");
		

/*		cli.processCommand("load /home/iran/MetaCSPFramework/trunk/dist/martinTestNew.log");
		cli.processCommand("monitoring start max 1 false");
		//cli.processCommand("show network");
*/		
		
/*		cli.processCommand("load /home/iran/MetaCSPFramework/trunk/dist/iranTestTimeline.log");
		cli.processCommand("monitoring start max 1 true");
		//cli.processCommand("show network");
*/
		
		cli.processCommand("load /home/iran/svnroot/MetaCSPFramework/trunk/dist/simpleTestNew.log");
		//cli.processCommand("load /home/iran/MetaCSPFramework/trunk/dist/iranTestTimeline.log");
		cli.processCommand("monitoring start max 5 false");
		//cli.processCommand("show network");
		
	}

}
