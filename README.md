# The Meta-CSP Framework
a Java API for meta-constraint reasoning

## What is meta-constarint reasoning?
Constraint Satisfaction Problems (CSP) consist of a finite set of variables, each associated with a finite domain, and a set of constraints which restrict simultaneous assignments to variables. A Meta-CSP is CSP formulation of a combinatorial problem which builds on lower-level CSPs.

## What does this API do?

This software framework provides tools for developing solvers for problems that can be cast as Meta-CSPs. The framework includes several built-in CSP and Meta-CSP problem solvers which can be used as "ingredients" for defining more sophisticated solvers. Among these, hybrid problem solvers which exemplify the natural predisposition of Meta-CSPs for solving hybrid reasoning problems.

## Documentation

Developers can refer to the API documentation (Javadoc) for reference, and to the examples below for getting started.

## Using the framework as a library

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

Alternatively, get the <a href="http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.metacsp%22%20AND%20a%3A%22meta-csp-framework%22">latest binary, source and Javadoc JARs from the Maven Central website</a>.

## Compiling from source

The framework as well as the API documentation (Javadoc) can be built through <a href="http://www.gradle.org/">Gradle</a>. A Gradle redistributable is included in the master branch, so there is no need to install Gradle on your machine.

### Gradle build instructions

Enter the directory with the file build.gradle and issue the command:
```
./gradlew install    #(on Unix-based systems)
gradlew.bat install  #(on Windows-based systems)
```

To test the build, issue the following:
```
./gradlew run        #(on Unix-based systems)
gradlew.bat run      #(on Windows-based systems)
```

The ```clean``` target will clean up the build directory. The target ```javadoc``` can be used to generate the API documentation (Javadoc), which will be placed in ```build/docs/javadoc```.

### Preparing an Eclipse project

If developing in Eclipse, consider using the eclipse target:
```
./gradlew eclipse    #(on Unix-based systems)
gradlew.bat eclipse  #(on Windows-based systems)
```

This will prepare the directory with ```.classpath```, ```.settings``` and ```.project``` files. The directory can then be used as source for a new Eclipse project which will have all dependencies properly set.

### Using the compiled library in other Gradle projects

The install target builds the artifact ```meta-csp-framework-0.0.0-SNAPSHOT.jar```, and places it into your local Maven repository (the location is ```~/.m2/repository/org/metacsp/meta-csp-framework/``` on a Unix-based system). You can use the newly compiled version of the framework in another Gradle-based project by including the following in the project's ```build.gradle```:

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

## Sponsors

* <a href="http://www.oru.se/">&Ouml;rebro University</a>
* <a href="http://project-race.eu/">FP7 Project RACE (Robustness by Autonomous Competence Enhancement)</a>
* <a href="http://cordis.europa.eu/fp7/home_en.html">The 7<sup>th</sup> EU Framework Programme</a>
* <a href="http://www.kk-stiftelsen.org/">The Swedish Knowledge Foundation</a>
