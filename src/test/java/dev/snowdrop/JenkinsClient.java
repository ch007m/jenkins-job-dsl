package dev.snowdrop;


import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.JenkinsTriggerHelper;
import com.offbytwo.jenkins.model.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class JenkinsClient {
    private final static String jobName = "mvn-spring-boot-rest-http";

    public static void main(String[] args) throws Exception {
        JenkinsServer cli = new JenkinsServer(new URI("http://localhost:8080"), "admin", "admin");
        System.out.println(("Job name: " + cli.getJob(jobName).getName()));
        System.out.println(("Job description: " + cli.getJob(jobName).getDescription()));

        // Trigger a build
        JenkinsTriggerHelper h = new JenkinsTriggerHelper(cli);

        Map<String, String> params = new HashMap<String, String>();
        params.put("SELECTED_TAG", "2.3.4_2");

        BuildWithDetails build = h.triggerJobAndWaitUntilFinished(jobName, params);
        System.out.println("Job result: " + build.getConsoleOutputText());
    }
}
