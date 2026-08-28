#!/bin/bash
echo "Running all tests (Unit Tests for Logic)..."
gradle :app:testDebugUnitTest

if [ $? -eq 0 ]; then
    echo "✅ All tests passed successfully."
else
    echo "❌ Tests failed! Please check the report."
    exit 1
fi
