package com.odin.detection;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

public class Stack {
    private String language;
    private String framework;
    private String buildTool;
    private List<String> databases;
    private Map<String, Integer> ports;
    private List<String> cloudProviders;
    private List<String> testingFrameworks;

    public Stack() {
        this.databases = new ArrayList<>();
        this.ports = new HashMap<>();
        this.cloudProviders = new ArrayList<>();
        this.testingFrameworks = new ArrayList<>();
    }
    //If any parameter is null, it assigns an empty list or map instead (avoids NullPointerException).
    public Stack(String language, String framework, String buildTool, List<String> databases,
                Map<String, Integer> ports, List<String> cloudProviders, List<String> testingFrameworks) {
        this.language = language;
        this.framework = framework;
        this.buildTool = buildTool;
        this.databases = databases != null ? databases : new ArrayList<>();
        this.ports = ports != null ? ports : new HashMap<>();
        this.cloudProviders = cloudProviders != null ? cloudProviders : new ArrayList<>();
        this.testingFrameworks = testingFrameworks != null ? testingFrameworks : new ArrayList<>();
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getFramework() {
        return framework;
    }

    public void setFramework(String framework) {
        this.framework = framework;
    }

    public String getBuildTool() {
        return buildTool;
    }

    public void setBuildTool(String buildTool) {
        this.buildTool = buildTool;
    }

    public List<String> getDatabases() {
        return databases;
    }

    public void setDatabases(List<String> databases) {
        this.databases = databases != null ? databases : new ArrayList<>();
    }

    public Map<String, Integer> getPorts() {
        return ports;
    }

    public void setPorts(Map<String, Integer> ports) {
        this.ports = ports != null ? ports : new HashMap<>();
    }

    public int getAppPort() {
        return ports.getOrDefault("app", 8080);
    }

    public int getDatabasePort(String database) {
        return ports.getOrDefault(database, 0);
    }

    public List<String> getCloudProviders() {
        return cloudProviders;
    }

    public void setCloudProviders(List<String> cloudProviders) {
        this.cloudProviders = cloudProviders != null ? cloudProviders : new ArrayList<>();
    }

    public List<String> getTestingFrameworks() {
        return testingFrameworks;
    }

    public void setTestingFrameworks(List<String> testingFrameworks) {
        this.testingFrameworks = testingFrameworks != null ? testingFrameworks : new ArrayList<>();
    }

    @Override
    public String toString() {
        return String.format("Stack{language='%s', framework='%s', buildTool='%s', databases=%s, ports=%s, cloudProviders=%s, testingFrameworks=%s}",
            language, framework, buildTool, databases, ports, cloudProviders, testingFrameworks);
    }
}