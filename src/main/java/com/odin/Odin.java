package com.odin;

import picocli.CommandLine;
import com.odin.cli.OdinCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Odin - AI-powered DevOps automation tool
 * 
 * This is the main entry point for the Odin application. It initializes
 * the command-line interface and handles the execution of various DevOps
 * automation commands.
 * 
 * The application uses picocli for command-line argument parsing and
 * supports multiple subcommands for different DevOps tasks like:
 * - Container monitoring
 * - Infrastructure generation
 * - Stack detection
 * - Configuration management
 */
public class Odin {
    // Logger for tracking application startup and execution
    private static final Logger logger = LoggerFactory.getLogger(Odin.class);

    /**
     * Main entry point for the Odin application.
     * 
     * @param args Command line arguments passed to the application
     */
    public static void main(String[] args) {
        // Log application startup
        logger.info("Starting Odin - AI-powered DevOps automation tool");
        
        // Initialize and execute the command-line interface
        int exitCode = new CommandLine(new OdinCommand()).execute(args);
        
        // Exit with the appropriate status code
        System.exit(exitCode);
    }
} 