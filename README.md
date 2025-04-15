# Assignment 3

This project contains JUnit 5 tests for the `org.apache.commons.mail.Email`
class. It verifies email functionality such as adding recipients, setting
headers, and building MIME messages.

## Requirements

Before running the tests, ensure you have the following installed:

- [JDK 11+](https://openjdk.org/projects/jdk/11/) for better compatibility with
  JUnit 5.
- [Apache Maven 3.6+](https://maven.apache.org/download.cgi) for dependency
  management and running tests.

## Setup Instructions

1. Extract the zip file to a directory of your choice.
2. Open a terminal and run these commands:

```sh
# Navigate to the extracted folder.
cd path/to/assignment_3

# Build the project.
mvn clean compile

# Run the test suite.
mvn test

# Generate a code coverage report.
# org.apache.commons.mail.Email should be at >70%.
mvn jacoco:prepare-agent test jacoco:report
```

The test coverage report will be available in `target/site/jacoco/index.html`

## Dependency Upgrades

The dependencies were updated from the original project files to ensure compatibility with JDK 11 and JUnit 5.
