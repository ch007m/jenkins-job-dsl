package dev.snowdrop;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.tasks.Shell;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.hamcrest.MatcherAssert.assertThat;

public class HelloFreeStyleJobTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void sayHelloJobUsingShellScript() throws Exception {
        FreeStyleProject project = j.createFreeStyleProject();
        project.getBuildersList().add(new Shell("echo hello"));
        FreeStyleBuild build = project.scheduleBuild2(0).get();

        System.out.println("Build name: " + build.getDisplayName());
        //assertThat(build.getDisplayName(),build.getDisplayName().contains(" completed"));

        String s = FileUtils.readFileToString(build.getLogFile(),"UTF-8");
        assertThat(s, s.contains("+ echo hello"));
    }
}