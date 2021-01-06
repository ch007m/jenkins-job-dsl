## Jenkins Project to test Jobs

The purpose of this project is to setup the needed environment in order to test/check/qualify
`FreeStyle Project`, `Job` or `Pipeline` DSL where the business logic has been developed using `Groovy scripts`.

All the Junit test classes include a `@Rule` annotation to instantiate the `JenkinsRule.class`. This class is responsible to
install locally a Jenkins Server (from a war archive) as declared within the pom.

All the needed `hpi or jpi` plugins (e.g. maven-plugin, pipeline, ...) will be deployed using the maven `hpi` plugin and goal `resolve-test-dependencies`.
They will be deployed under the folder `target/test-classes/test-dependencies`. The different versions of the plugins are resolved from the `io.jenkins.tools.bom/bom-2.263.x` bom file.

### Prerequisite

- JDK 11 is needed

### How to test

To test the different jobs created (simple, maven, pipeline, ...), execute the following maven command:
```
mvn clean test
```