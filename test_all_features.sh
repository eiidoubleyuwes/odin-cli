#!/bin/bash

# Set up test directories
TEST_DIR="app_tests"
PYTHON_APP_DIR="${TEST_DIR}/python_app"
NODE_APP_DIR="${TEST_DIR}/node_app"
JAVA_APP_DIR="${TEST_DIR}/java_spring_app"


# Set up output directories
PYTHON_OUTPUT_DIR="${TEST_DIR}/python_app"
NODE_OUTPUT_DIR="${TEST_DIR}/node_app_output"
JAVA_OUTPUT_DIR="${TEST_DIR}/java_app_output"

## Clean up previous test artifacts
#echo "Cleaning up previous test artifacts..."
#rm -rf "${PYTHON_OUTPUT_DIR}" "${NODE_OUTPUT_DIR}" "${JAVA_OUTPUT_DIR}" "${RUST_OUTPUT_DIR}"

# Function to run tests with a specific provider
run_tests() {
    local provider=$1
    echo "Testing with ${provider} provider..."
    
    # Test Python app
    echo "Testing Python app..."
    java -jar target/odin-1.0-SNAPSHOT.jar all "${PYTHON_APP_DIR}" -o "${PYTHON_OUTPUT_DIR}" --provider "${provider}" --cloud aws

    # Test Node.js app
#    echo "Testing Node.js app..."
    java -jar target/odin-1.0-SNAPSHOT.jar all "${NODE_APP_DIR}" -o "${NODE_OUTPUT_DIR}_${provider}" --provider "${provider}" --cloud aws

#    # Test Java app
#    echo "Testing Java app..."
#    java -jar target/odin-1.0-SNAPSHOT.jar all "${JAVA_APP_DIR}" -o "${JAVA_OUTPUT_DIR}_${provider}" --provider "${provider}" --cloud aws
#

}

# Run tests with Ollama 
# run_tests "ollama"

# Run tests with Gemini
echo "Setting up Gemini API key..."
export GEMINI_API_KEY=AIzdfghjkjhgfdfghjhgfghjhgfghjhgfghjhfdfghgfdfgh
run_tests "gemini"

# Print test results
echo "Test execution completed. Check the output directories for results:"
# echo "- Python app outputs: ${PYTHON_OUTPUT_DIR}_ollama and ${PYTHON_OUTPUT_DIR}_gemini"
# echo "- Node.js app outputs: ${NODE_OUTPUT_DIR}_ollama and ${NODE_OUTPUT_DIR}_gemini"
# echo "- Java app outputs: ${JAVA_OUTPUT_DIR}_ollama and ${JAVA_OUTPUT_DIR}_gemini"
echo "- Python app output: ${PYTHON_OUTPUT_DIR}_gemini"
echo "- Node.js app output: ${NODE_OUTPUT_DIR}_gemini"
echo "- Java app output: ${JAVA_OUTPUT_DIR}_gemini"




#java -jar target/odin-1.0-SNAPSHOT.jar docker app_tests/node_app -o /app_test/node_app_output --provider ollama --cloud aws

