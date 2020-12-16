## Jenkins MavenJob

This project has been designed to test a `Maven Job DSL` using a Junit java class designed using as `@Rule` a `Jenkinsrule

To test the `MavenJob`, execute the following gradle command:
```
gradle test --tests MavenJobDSLTest
```
and next view the report populated under the folder `build/reports/tests/test/index.html`