# The Meta-CSP Framework:<br>

<I>

a Java API for Meta-CSP based reasoning<br>
<br>
</I><br>
<br>
</h1>

## What is Meta-CSP reasoning? ##
Constraint Satisfaction Problems (CSP) consist of a finite set of variables, each associated with a finite domain, and a set of constraints which restrict simultaneous assignments to variables. A Meta-CSP is CSP formulation of a combinatorial problem which builds on lower-level CSPs.

## What does this API do? ##
This software framework provides tools for developing solvers for problems that can be cast as Meta-CSPs.  The framework includes several built-in CSP and Meta-CSP problem solvers which can be used as "ingredients" for defining more sophisticated solvers. Among these, hybrid problem solvers which exemplify the natural predisposition of Meta-CSPs for solving hybrid reasoning problems.

## Documentation ##
Developers can refer to the <a href='http://meta-csp-framework.googlecode.com/svn/javadoc/index.html'>API documentation (Javadoc)</a> for reference, and to the examples below for getting started.

## Examples to get started ##
Several examples are provided to get started with the framework.  They are organized into three groups:

  * Ground CSPs: these are the ground CSP implementations that are used to build higher level CSPs - e.g., a STP solver, which is used by the Meta-CSP implementation of a TCSP solver.

  * Multi-CSPs: there are CSPs whose variables/constraints are composed of multiple variables/constraints in ground CSPs - e.g., a temporal constraint solver whose variables are intervals, composed of two timepoints (maintained by an underlying STP solver).

  * Meta-CSPs: these are high-level CSPs which leverage ground CSPs (or Multi-CSPs) to solve a problem - e.g., a resource scheduler which uses a temporal constraint solver to search for a resource-feasible schedule.

### Ground CSPs ###

  * `examples.TestAPSPSolver` shows how to use an implementation of a STP solver (uses Floyd-Warshall for temporal constraint propagation).

  * `examples.TestFuzzyAllenIntervalNetworkSolver` shows how to use an Allen Interval Constraint solver which performs fuzzy constraint evaluation (i.e., returning the possibility degree of temporal constraint satisfaction).

  * `examples.TestSymbolicVariableConstraintSolver` shows the use of a symbolic variable constraint solver, where variable domains are possible symbols and constraints are EQUALS and DIFFERENT.

  * `examples.TestFuzzySymbolicVariableConstraintSolver` shows the use of symbolic variables where symbols have an associated possibility degree and constraints are subject to fuzzy AC propagation.

### Multi-CSPs ###

  * `examples.multi.TestAllenIntervalNetworkSolver` shows how to propagate a subset of augmented Allen's Interval Algebra, i.e., temporal relations with bounds.  The variables are flexible temporal intervals (represented by pairs of timepoints maintained in a STP).

  * `examples.multi.TestActivityNetworkSolver` shows how to work with Activities, which are variables whose domain is both temporal and symbolic.  The temporal part is a flexible temporal interval (see `examples.TestAllenIntervalNetworkSolver`), while the symbolic part is a set of possible symbols (see `examples.TestSymbolicVariableConstraintSolver`).

  * `examples.multi.TestDistanceConstraintSolver` shows how to work with disjunctive temporal constraints (prescribing alternative bounds on the distance between timepoints).  The solver shown here is equivalent to a TCSP solver which only propagates constraints with one disjunct (it is used as part of the TCSP solver `meta.TCSPSolver`).

  * `examples.multi.TestFuzzyActivityNetworkSolver` shows how to use Activities in conjunction with temporal constraints which are evaluated through fuzzy temporal inference, and in which symbolic values have an associated possibility degree.

  * `examples.multi.TestTimelinePlotting` shows how to visualize a temporal constraint network of Activities as timelines.

### Meta-CSPs ###

  * `examples.meta.TestReusableResourceScheduler` shows the use of a meta-CSP based renewable resource scheduler.  The algorithm posts temporal constraints to the ground-CSP to resolve resource over-consumption.

  * `examples.meta.TestSimplePlanner` shows how to use a simple yet expressive planner which can deal with explicit time and renewable resources.

  * `examples.meta.TestStateVariableScheduler` and `examples.meta.TestStateVariableSchedulerSimple` exemplify a similar concept to reusable resource scheduling, but where scheduling conflicts arise due to conflicting symbols on a timeline rather than resource over-consumption.

  * `examples.meta.TestTCSPSolver` shows the use of a meta-CSP based TCSP solver.

## Using the framework as a library ##

The Meta-CSP Framework is available on Maven Central.

Maven dependency declaration:

```
<dependency>
  <groupId>org.metacsp</groupId>
  <artifactId>meta-csp-framework</artifactId>
  <version>1.0.394</version>
</dependency>
```

Gradle dependency declaration:

```
compile 'org.metacsp:meta-csp-framework:1.0.394'
```

A project template for use with Gradle is available on the SVN. Check it out as follows:

```
$ svn checkout http://meta-csp-framework.googlecode.com/svn/project-template project-template
```

Alternatively, get the <a href='http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.metacsp%22%20AND%20a%3A%22meta-csp-framework%22'>latest binary, source and Javadoc JARs</a> from the Maven Central website.


## Compiling from source ##

Check out the framework from the <a href='http://code.google.com/p/meta-csp-framework/source/checkout'>SVN repository</a>.  To do so from the command line, issue the following:

```
$ svn checkout http://meta-csp-framework.googlecode.com/svn/trunk/ meta-csp-framework-read-only
```

The framework as well as the <a href='http://meta-csp-framework.googlecode.com/svn/javadoc/index.html'>API documentation (Javadoc)</a> can be built through <a href='http://www.gradle.org/'>Gradle</a>.

### Gradle build instructions ###
Enter the directory with the file `build.gradle` and issue the command:

```
$ gradle install
```

To test the build, issue the following:

```
$ gradle run
```

The `clean` target will clean up the `build` directory.  The target `javadoc` can be used to generate the <a href='http://meta-csp-framework.googlecode.com/svn/javadoc/index.html'>API documentation (Javadoc)</a>, which will be placed in `build/docs/javadoc`.

### Preparing an Eclipse project ###
If developing in Eclipse, consider using the `eclipse` target:

```
$ gradle eclipse
```

This will prepare the directory with `.classpath`, `.settings` and `.project` files.  The directory can then be used as source for a new Eclipse project which will have all dependencies properly set.

### Using the compiled library in other Gradle projects ###

The `install` target builds the artifact `meta-csp-framework-0.0.0-SNAPSHOT.jar`, and places it into your local Maven repository (the location is `~/.m2/repository/org/metacsp/meta-csp-framework/`).  You can use the newly compiled version of the framework in another Gradle-based project by including the following in the project's `build.gradle`:

```
repositories {
   mavenLocal()
   //any other repo you may need for your project
}

dependencies {
   compile 'org.metacsp:meta-csp-framework:0.0.0-SNAPSHOT'
   //any other dependency you may have
}
```

## Sponsors ##

This project is supported by:

<table border='0'>
<tr>
<td align='center'><a href='http://aass.oru.se'><img src='http://meta-csp-framework.googlecode.com/svn/wiki/Logo_txt_runt_farg_ENG.png' /></a></td>
<td><pre>            </pre></td>
<td align='center'><a href='http://project-race.eu'><img src='http://meta-csp-framework.googlecode.com/svn/wiki/race-logo.png' /></a></td>
<td><pre>            </pre></td>
<td align='center'><a href='http://cordis.europa.eu/fp7/home_en.html'><img src='http://meta-csp-framework.googlecode.com/svn/wiki/FP7-gen-RGB.jpg' /></a><a href='http://cordis.europa.eu/fp7/home_en.html'><img src='http://meta-csp-framework.googlecode.com/svn/wiki/euflag.jpg' /></a></td>
</tr>
<tr>
<td align='center'><a href='http://www.oru.se'>Örebro University</a></td>
<td><pre>            </pre></td>
<td align='center'><a href='http://project-race.eu'>The RACE Project</a></td>
<td><pre>            </pre></td>
<td align='center'><a href='http://cordis.europa.eu/fp7/home_en.html'>The 7th EU Framework Programme</a></td>
</tr>
</table>