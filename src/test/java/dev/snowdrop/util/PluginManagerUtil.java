package dev.snowdrop.util;

import org.jvnet.hudson.test.JenkinsRule;

public class PluginManagerUtil {

    public static JenkinsRule newJenkinsRule() {
        return new JenkinsRule() {
            @Override
            public void before() throws Throwable {
                setPluginManager(null);
                super.before();
            }
        };
    }
}