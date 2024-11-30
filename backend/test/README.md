# Sports Center Test Suite

This directory contains the test suite for the Sports Center backend application.

## Prerequisites

Before running the tests, ensure you have the following installed:
- Clojure CLI tools
- Homebrew (for macOS)
- Java Development Kit (JDK) 11 or later

## Setup

1. Install Dependencies:
   ```bash
   # Install all required databases and services
   ./scripts/install_deps.sh
   ```

   This will install and start:
   - PostgreSQL
   - MongoDB
   - Redis
   - Elasticsearch
   - Neo4j
   - InfluxDB

2. Configure Services:
   - PostgreSQL: Create a user and database
   - MongoDB: Ensure the service is running on default port (27017)
   - Redis: Verify it's running on port 6379
   - Elasticsearch: Check it's accessible on http://localhost:9200
   - Neo4j: Set up initial password
   - InfluxDB: Create an organization and bucket

## Running Tests

1. Setup Test Environment:
   ```bash
   # Initialize test databases
   ./scripts/setup_test_dbs.sh
   ```

2. Run the Test Suite:
   ```bash
   # Run all tests
   ./scripts/run_tests.sh
   ```

## Test Structure

- `core_test.clj`: Core application tests
- `routes/`: API endpoint tests
  - `teams_test.clj`: Team management endpoints
  - `training_test.clj`: Training session endpoints
- `test_helpers.clj`: Common test utilities and fixtures

## Configuration

- Test configuration: `test/resources/config.edn`
  - Database connections
  - Authentication settings
  - Rate limiting parameters
  - Logging configuration

## Writing Tests

1. Use the provided test helpers in `test_helpers.clj`
2. Follow existing test patterns:
   ```clojure
   (deftest test-name
     (testing "description"
       (let [response (app (mock/request ...))]
         (is (= expected actual)))))
   ```
3. Include both success and error cases
4. Use fixtures appropriately
5. Clean up test data after each test

## Available Scripts

- `install_deps.sh`: Installs all required databases and services
- `setup_test_dbs.sh`: Initializes test databases
- `run_tests.sh`: Runs the test suite with proper configuration

## Troubleshooting

1. Database Connection Issues:
   - Check if services are running: `brew services list`
   - Verify port availability: `lsof -i :<port>`
   - Check logs: `brew services logs <service>`

2. Test Failures:
   - Check test output for specific failure messages
   - Verify database configurations in `config.edn`
   - Ensure all services are running and accessible

3. Common Issues:
   - Port conflicts: Check for other services using required ports
   - Permission issues: Verify database user permissions
   - Memory issues: Adjust service memory settings if needed

## Continuous Integration

The test suite runs automatically in CI for:
- Pull requests
- Merges to main branch
- Release tags

## Performance Considerations

- Tests use separate databases to avoid conflicts
- Database connections are pooled for efficiency
- Cleanup routines ensure test isolation
- Rate limiting is disabled in test environment

## Contributing

1. Write tests for new features
2. Update existing tests when modifying functionality
3. Ensure all tests pass locally before pushing
4. Add appropriate documentation for new test cases
