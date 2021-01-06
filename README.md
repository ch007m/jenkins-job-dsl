## Jenkins Project to test Jobs

The purpose of this project is to setup the needed environment in order to test/check/qualify
`FreeStyle Project`, `Job` or `Pipeline` DSL where the business logic has been developed using `Groovy scripts`.

All the Junit test classes include a `@Rule` annotation to instantiate the `JenkinsRule.class`. This class is responsible to
install locally a Jenkins Server (from a war archive) as declared within the pom.

All the needed `hpi or jpi` plugins (e.g. maven-plugin, pipeline, ...) will be deployed using the maven `hpi` plugin and goal `resolve-test-dependencies`.
They will be deployed under the folder `target/test-classes/test-dependencies`. The different versions of the plugins are resolved from the `io.jenkins.tools.bom/bom-2.263.x` bom file.

The making off of a test case is quite simple as you must create a `FreeStyle project` able to load a `Groovy DSL Script`
```java
FreeStyleProject job = j.createFreeStyleProject("simple-seed-job");

// Setup the ExecuteDslScripts to load the content of the DSL groovy script = mavenJob.groovy
ExecuteDslScripts e = new ExecuteDslScripts();
String dslScript = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/simpleJob.groovy")))
        .lines().collect(Collectors.joining("\n"));
e.setScriptText(dslScript);
e.setUseScriptText(true);
job.getBuildersList().add(e);

// Execute the seed job to create the mavenJob
FreeStyleBuild b = job.scheduleBuild2(0).get();
```
and next launch the `Job` created previously.
```java
FreeStyleProject freeStyleProjectGeneratedJob = (FreeStyleProject) j.jenkins.getItem("hello-world");
FreeStyleBuild b2 = freeStyleProjectGeneratedJob.scheduleBuild2(0).get();

assertEquals(1, b2.number);
assertEquals("#1",b2.getDisplayName());
assertEquals("SUCCESS",b2.getResult().toString());
```

**Important**: If the `Groovy DSL syntax` of the script(s) loaded by the `FreeStyle project` is not correct, if some plugins or dependencies are missing, then errors will be raised. This is
why having Junit test is very helpful :-)

**Remark**: Instead of loading the groovy script from a local project, git could be used to fetch from a repository the syntax of the jobs.

### Prerequisite

- JDK 11 is needed

### How to test

To test the different jobs created (simple, maven, pipeline, ...), execute the following maven command:
```
mvn clean test
```