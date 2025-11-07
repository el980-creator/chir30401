#!/bin/bash

echo "==================================================="
echo "           CHIRPY 2.0 COMPREHENSIVE TESTS"
echo "==================================================="
echo

# Compile all source files
echo "Compiling source files..."
find src/main/java -name "*.java" -type f > source_files.txt
javac -cp "lib/*" -d build/classes @source_files.txt
COMPILE_EXIT_CODE=$?

if [ $COMPILE_EXIT_CODE -ne 0 ]; then
    echo "Source compilation failed!"
    rm -f source_files.txt
    exit 1
fi
echo "Source compilation successful"

# Compile test files
echo
echo "Compiling test files..."
mkdir -p build/test-classes
find src/test/java -name "*.java" -type f > test_files.txt
javac -cp "lib/*:build/classes" -d build/test-classes @test_files.txt
TEST_COMPILE_EXIT_CODE=$?

if [ $TEST_COMPILE_EXIT_CODE -ne 0 ]; then
    echo "Test compilation failed!"
    rm -f source_files.txt test_files.txt
    exit 1
fi
echo "Test compilation successful"

# Run JUnit tests
echo
echo "Running JUnit test suite..."
java -cp "lib/*:build/classes:build/test-classes" edu.georgetown.AllTestsRunner
JUNIT_EXIT_CODE=$?

# Basic application startup test
echo
echo "Testing application startup..."
java -cp "lib/*:build/classes" edu.georgetown.Chirpy &
APP_PID=$!
sleep 3

if ps -p $APP_PID > /dev/null 2>&1; then
    echo "Application started successfully"
    
    # Test basic HTTP response
    echo "Testing HTTP endpoint..."
    if curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/ | grep -q "200"; then
        echo "HTTP endpoint responding correctly"
    else
        echo "HTTP endpoint may have issues (but app is running)"
    fi
    
    kill $APP_PID
    sleep 1
else
    echo "Application failed to start"
    JUNIT_EXIT_CODE=1
fi

# Cleanup
rm -f source_files.txt test_files.txt

echo
echo "==================================================="
if [ $JUNIT_EXIT_CODE -eq 0 ]; then
    echo "ALL TESTS PASSED! Chirpy 2.0 is ready!"
else
    echo "SOME TESTS FAILED - Check output above"
fi
echo "==================================================="

exit $JUNIT_EXIT_CODE