package com.odin.monitoring;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.Statistics;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import com.odin.llm.LLMClient;
import com.odin.llm.LLMClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Handles Docker container monitoring and metrics collection.
 * This class keeps track of running containers, their stats, and analyzes their logs
 * for potential issues. It's designed to be lightweight and efficient.
 */
public class DockerMonitor {
    private static final Logger logger = LoggerFactory.getLogger(DockerMonitor.class);
    private final DockerClient dockerClient;
    private final LLMClient llmClient;
    private final ScheduledExecutorService scheduler;
    private final Map<String, ContainerStats> containerStats;
    private final Map<String, List<String>> containerLogs;
    private final Map<String, List<String>> failurePatterns;

    /**
     * Sets up the monitor with Docker client and LLM for log analysis.
     * Configures timeouts and connection settings for Docker operations.
     */
    public DockerMonitor() {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
            .dockerHost(config.getDockerHost())
            .maxConnections(100)
            .connectionTimeout(Duration.ofSeconds(30))
            .responseTimeout(Duration.ofSeconds(45))
            .build();
        this.dockerClient = DockerClientImpl.getInstance(config, httpClient);
        this.llmClient = LLMClientFactory.createClient();
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.containerStats = new ConcurrentHashMap<>();
        this.containerLogs = new ConcurrentHashMap<>();
        this.failurePatterns = new ConcurrentHashMap<>();
    }

    /**
     * Starts the monitoring process with different intervals for various tasks:
     * - Container info display: every 5 seconds
     * - Stats collection: every 30 seconds
     * - Log collection: every minute
     * - Log analysis: every 5 minutes
     */
    public void startMonitoring() {
        // Initial display
        displayContainerInfo();
        
        // Schedule only essential monitoring tasks
        scheduler.scheduleAtFixedRate(this::displayContainerInfo, 5, 5, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(this::collectContainerStats, 30, 30, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(this::collectContainerLogs, 60, 60, TimeUnit.MINUTES);
        scheduler.scheduleAtFixedRate(this::analyzeLogs, 5, 5, TimeUnit.MINUTES);
    }

    /**
     * Safely stops all monitoring tasks and cleans up resources.
     * Waits up to 60 seconds for tasks to complete before forcing shutdown.
     */
    public void stopMonitoring() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Collects real-time stats for all running containers.
     * Tracks CPU usage, memory consumption, and network I/O.
     * Uses Docker's stats API with a 5-second timeout per container.
     */
    private void collectContainerStats() {
        try {
            List<Container> containers = dockerClient.listContainersCmd().exec();
            for (Container container : containers) {
                String containerId = container.getId();
                CompletableFuture<Statistics> statsFuture = new CompletableFuture<>();
                
                dockerClient.statsCmd(containerId).exec(new ResultCallback<Statistics>() {
                    @Override
                    public void onStart(Closeable closeable) {}

                    @Override
                    public void onNext(Statistics stats) {
                        statsFuture.complete(stats);
                        try {
                            close();
                        } catch (IOException e) {
                            logger.error("Failed to close stats stream", e);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        statsFuture.completeExceptionally(throwable);
                    }

                    @Override
                    public void onComplete() {}

                    @Override
                    public void close() throws IOException {}
                });
                
                try {
                    Statistics stats = statsFuture.get(5, TimeUnit.SECONDS);
                    ContainerStats containerStats = new ContainerStats(
                        stats.getCpuStats().getCpuUsage().getTotalUsage(),
                        stats.getMemoryStats().getUsage(),
                        stats.getMemoryStats().getLimit(),
                        stats.getNetworks().get("eth0").getRxBytes(),
                        stats.getNetworks().get("eth0").getTxBytes()
                    );
                    this.containerStats.put(containerId, containerStats);
                } catch (Exception e) {
                    logger.error("Failed to get stats for container {}", containerId, e);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to collect container stats", e);
        }
    }

    /**
     * Gathers recent logs from all containers.
     * Keeps the last 100 lines per container to avoid memory issues.
     * Logs are stored for later analysis by the AI.
     */
    private void collectContainerLogs() {
        try {
            List<Container> containers = dockerClient.listContainersCmd().exec();
            for (Container container : containers) {
                String containerId = container.getId();
                List<String> logs = new ArrayList<>();
                
                CompletableFuture<Void> future = new CompletableFuture<>();
                
                dockerClient.logContainerCmd(containerId)
                    .withTail(100)
                    .withTimestamps(true)
                    .withFollowStream(false)
                    .exec(new ResultCallback<Frame>() {
                        @Override
                        public void onStart(Closeable closeable) {}

                        @Override
                        public void onNext(Frame frame) {
                            logs.add(new String(frame.getPayload()));
                            if (logs.size() >= 100) {
                                try {
                                    close();
                                } catch (IOException e) {
                                    logger.error("Error closing log stream", e);
                                }
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            logger.error("Error collecting logs for container {}", containerId, throwable);
                            future.completeExceptionally(throwable);
                        }

                        @Override
                        public void onComplete() {
                            future.complete(null);
                        }

                        @Override
                        public void close() throws IOException {
                            future.complete(null);
                        }
                    });
                
                try {
                    future.get(5, TimeUnit.SECONDS);
                    containerLogs.put(containerId, logs);
                } catch (Exception e) {
                    logger.error("Failed to collect logs for container {}", containerId, e);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to collect container logs", e);
        }
    }

    /**
     * Uses AI to analyze container logs for potential issues.
     * Looks for error patterns, resource exhaustion, and common failure modes.
     * Stores detected issues for later reporting.
     */
    private void analyzeLogs() {
        for (Map.Entry<String, List<String>> entry : containerLogs.entrySet()) {
            String containerId = entry.getKey();
            List<String> logs = entry.getValue();
            
            if (logs.isEmpty()) continue;
            
            // Prepare log context for AI analysis
            String logContext = String.join("\n", logs);
            String prompt = "Analyze these Docker container logs and identify potential issues or failures:\n\n" + logContext;
            
            try {
                String analysis = llmClient.generateText(prompt);
                List<String> detectedFailures = parseFailureAnalysis(analysis);
                
                if (!detectedFailures.isEmpty()) {
                    failurePatterns.put(containerId, detectedFailures);
                    logger.warn("Detected failures in container {}: {}", containerId, detectedFailures);
                }
            } catch (Exception e) {
                logger.error("Failed to analyze logs for container {}", containerId, e);
            }
        }
    }

    /**
     * Parses the AI's analysis of container logs to extract failure patterns.
     * Currently looks for keywords like 'error', 'failure', 'exception'.
     * Could be enhanced with more sophisticated pattern matching.
     */
    private List<String> parseFailureAnalysis(String analysis) {
        // Simple parsing of AI analysis - can be enhanced based on AI response format
        return Arrays.stream(analysis.split("\n"))
            .filter(line -> line.toLowerCase().contains("error") || 
                          line.toLowerCase().contains("failure") ||
                          line.toLowerCase().contains("exception"))
            .collect(Collectors.toList());
    }

    public Map<String, ContainerStats> getContainerStats() {
        return Collections.unmodifiableMap(containerStats);
    }

    public Map<String, List<String>> getContainerLogs() {
        return Collections.unmodifiableMap(containerLogs);
    }

    public Map<String, List<String>> getFailurePatterns() {
        return Collections.unmodifiableMap(failurePatterns);
    }

    /**
     * Displays a clean, formatted table of all containers.
     * Updates every 5 seconds with fresh data.
     * Shows container ID, name, status, and port mappings.
     * Truncates long values to keep the table readable.
     */
    private void displayContainerInfo() {
        try {
            // Clear console for better visibility
            System.out.print("\033[H\033[2J");
            System.out.flush();

            List<Container> containers = dockerClient.listContainersCmd()
                .withShowAll(true)  // Show all containers including stopped ones
                .exec();

            if (containers.isEmpty()) {
                System.out.println("\nNo containers found.");
                return;
            }

            // Print table header
            System.out.println("\nDocker Containers:");
            System.out.println("+------------------+------------------+------------------+------------------+");
            System.out.println("| Container ID     | Name             | Status           | Ports            |");
            System.out.println("+------------------+------------------+------------------+------------------+");

            for (Container container : containers) {
                String containerId = container.getId().substring(0, 12);
                String name = container.getNames()[0].replace("/", "");
                String status = container.getStatus();
                String ports = container.getPorts() == null ? "No ports" :
                    Arrays.stream(container.getPorts())
                        .filter(Objects::nonNull)
                        .map(port -> String.format("%d->%d",
                            port.getPrivatePort(),
                            port.getPublicPort() == null ? port.getPrivatePort() : port.getPublicPort()))
                        .collect(Collectors.joining(", "));

                // Truncate long strings to fit in table
                name = name.length() > 16 ? name.substring(0, 13) + "..." : name;
                status = status.length() > 16 ? status.substring(0, 13) + "..." : status;
                ports = ports.length() > 16 ? ports.substring(0, 13) + "..." : ports;

                System.out.printf("| %-16s | %-16s | %-16s | %-16s |%n",
                    containerId, name, status, ports);
            }
            System.out.println("+------------------+------------------+------------------+------------------+");
            System.out.println("Press Ctrl+C to exit");
        } catch (Exception e) {
            // Minimize logging for better performance
            logger.error("Display error: {}", e.getMessage());
        }
    }

    /**
     * Internal class to hold container statistics.
     * Tracks CPU, memory, and network metrics for each container.
     * Used for trend analysis and resource monitoring.
     */
    public static class ContainerStats {
        private final long cpuUsage;
        private final long memoryUsage;
        private final long memoryLimit;
        private final long networkRx;
        private final long networkTx;

        public ContainerStats(long cpuUsage, long memoryUsage, long memoryLimit, 
                            long networkRx, long networkTx) {
            this.cpuUsage = cpuUsage;
            this.memoryUsage = memoryUsage;
            this.memoryLimit = memoryLimit;
            this.networkRx = networkRx;
            this.networkTx = networkTx;
        }

        // Getters
        public long getCpuUsage() { return cpuUsage; }
        public long getMemoryUsage() { return memoryUsage; }
        public long getMemoryLimit() { return memoryLimit; }
        public long getNetworkRx() { return networkRx; }
        public long getNetworkTx() { return networkTx; }
    }
} 