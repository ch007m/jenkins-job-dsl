package dev.snowdrop;

import hudson.FilePath;
import hudson.maven.MavenModuleSet;
import hudson.maven.MavenModuleSetBuild;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.plugins.groovy.GroovyInstallation;
import hudson.tasks.Maven;
import javaposse.jobdsl.plugin.ExecuteDslScripts;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.ToolInstallations;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MavenCheckBOMJobDSLTest {
    @Rule public JenkinsRule j = new JenkinsRule();
    private ArrayList<String> LogResult;
    private String seedJobName = "maven-seed-job";
    private static String GROOVY_BINARY_ZIP_URL = "https://bintray.com/artifact/download/groovy/maven/apache-groovy-binary-3.0.7.zip";
    private static String GROOVY_BINARY_FILENAME = "groovy-binary-3.0.7.zip";
    private static String GROOVY_JENKINS_NAME = "groovy-3.0.7";

    // Download the Groovy Binary Zip as it needed by jenkins to run the Groovy script job
    @BeforeClass
    public static void init() throws IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                              .GET()
                              .uri(URI.create(GROOVY_BINARY_ZIP_URL))
                              .setHeader("User-Agent", "Java 11 HttpClient Bot") // add request header
                              .build();
        HttpResponse<Path> response = httpClient.send(request, HttpResponse.BodyHandlers.ofFile(Paths.get("target/test-classes/" + GROOVY_BINARY_FILENAME)));
    }

    @BeforeClass
    public static void setupProperty() {
        System.setProperty("jenkins.test.noSpaceInTmpDirs", "true");
    }

    @Before
    // Install Groovy to the Jenkins Global Configuration Tools as it is needed by the mavenJob
    // As no utility class exists to fetch groovy, then we will deploy it from a local resource zip file
    public void setUpGroovy() throws Exception {
        FilePath home = j.jenkins.getRootPath();
        home.unzipFrom(MavenCheckBOMJobDSLTest.class.getResourceAsStream("/" + GROOVY_BINARY_FILENAME));
        j.jenkins.getDescriptorByType(GroovyInstallation.DescriptorImpl.class)
                 .setInstallations(new GroovyInstallation(
                                    GROOVY_JENKINS_NAME,
                                    home.child(GROOVY_JENKINS_NAME).getRemote(),
                          null));
    }

    @Before
    public void setupMaven() throws Exception {
        // Add maven to the Jenkins Global Configuration Tools as it is needed by the mavenJob
        Maven.MavenInstallation mvn = ToolInstallations.configureDefaultMaven("apache-maven-3.6.3", Maven.MavenInstallation.MAVEN_30);
        Maven.MavenInstallation m3 = new Maven.MavenInstallation("apache-maven-3.6.3", mvn.getHome(), JenkinsRule.NO_PROPERTIES);
        j.getInstance().getDescriptorByType(Maven.DescriptorImpl.class).setInstallations(m3);
    }

    @Before
    public void copyGroovyFile() {
        String[] groovyFiles = {
                "backupPOM.groovy",
                "removeDependencyManagementTags.groovy",
                "restorePOM.groovy"
        };

        // Create under the temp jenkins directory, the workspace of the job
        File root = j.jenkins.root;
        File wksJobDir = new File(root.getAbsolutePath() + "/workspace/" + seedJobName);
        wksJobDir.mkdirs();

        // Create the destination file
        Arrays.stream(groovyFiles).forEach((f) -> {
            File groovyWksFile = new File(wksJobDir.getAbsolutePath() + "/" + f);

            // Copy the needed groovy file from local to the job workspace
            InputStream is = getClass().getResourceAsStream("/" + f);
            Path dest = Paths.get(groovyWksFile.getAbsolutePath());
            try {
                Files.copy(is, dest, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void useMavenDSLGroovyFileAsJob() throws Exception {
        FreeStyleProject job = j.createFreeStyleProject(seedJobName);

        // Setup the ExecuteDslScripts to load the content of the DSL groovy script = mavenJob.groovy
        ExecuteDslScripts e = new ExecuteDslScripts();
        String dslScript = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/checkBomDependenciesJob.groovy")))
                .lines().collect(Collectors.joining("\n"));
        e.setScriptText(dslScript);
        e.setUseScriptText(true);
        job.getBuildersList().add(e);

        // Execute the seed job to create the mavenJob
        FreeStyleBuild b = job.scheduleBuild2(0).get();

        assertEquals(1, b.number);
        assertEquals("#1", b.getDisplayName());
        if (b.getResult().toString() != "SUCCESS") {
            LogResult = (ArrayList<String>) b.getLog(200);
            LogResult.forEach((s) -> System.out.println(s));
        }

        // Check if the FreeStyleProject build reported that it generated the job: say-hello-world
        LogResult = (ArrayList<String>) b.getLog(100);
        //LogResult.forEach((s) -> System.out.println(s));
        assertTrue(b.getLog(100).stream().anyMatch(str -> str.contains("GeneratedJob{name='check-bom-dependencies'")));

        // Get the FreeStyleProject containing the Job DSL steps to be executed - "check-bom-dependencies"
        MavenModuleSet mavenJob = (MavenModuleSet) j.jenkins.getItem("check-bom-dependencies");
        MavenModuleSetBuild b2 = mavenJob.scheduleBuild2(0).get();

        assertEquals(1, b2.number);
        assertEquals("#1", b2.getDisplayName());
        if (b2.getResult().toString() != "SUCCESS") {
            LogResult = (ArrayList<String>) b2.getLog(200);
            LogResult.forEach((s) -> System.out.println(s));
        } else {
            System.out.println("Job build succeeded !");
            LogResult = (ArrayList<String>) b2.getLog(200);
            LogResult.forEach((s) -> System.out.println(s));
        }
    }
}
