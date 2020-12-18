## Jenkins MavenJob

### Prerequisite

- Have gradle 6.7.1 installed and groovy 2.5.12
- JDK 8 !!  
- Run the following script in the Script Console of a Jenkins instance to generate
the above testPlugins list.
```
Jenkins.instance.pluginManager.plugins
  .findAll { !(it.shortName in ['job-dsl', 'structs']) }
  .collect { "testPlugins '${it.manifest.mainAttributes.getValue("Group-Id")}:${it.shortName}:${it.version}'" }
  .sort()
  .each { println it }
```
and testCompile dependencies
```
Jenkins.instance.pluginManager.plugins
  .findAll { !(it.shortName in ['job-dsl', 'structs']) }
  .collect { "testCompile '${it.manifest.mainAttributes.getValue("Group-Id")}:${it.shortName}:${it.version}@jar'" }
  .sort()
  .each { println it }
```
**Remark**: TODO. Not all the `testCompile` deps are needed within the list populated. To be checked

### HowTo test

This project has been designed to test a `Maven Job DSL` using a Junit java class designed using as `@Rule` a `Jenkinsrule

To test the `MavenJob`, execute the following gradle command:
```
gradle test --tests MavenJobDSLTest
```
and next view the report populated under the folder `build/reports/tests/test/index.html`