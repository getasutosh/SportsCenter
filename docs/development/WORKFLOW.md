# Local Development Workflow

## Development Environment

### Directory Structure
```
sports-center/
├── backend/           # Clojure backend
│   ├── src/          # Source code
│   ├── test/         # Test files
│   ├── resources/    # Configuration and resources
│   └── scripts/      # Development scripts
├── frontend/         # React frontend (to be added)
└── docs/            # Documentation
```

### Development Scripts
All development scripts are in `backend/scripts/`:
- `dev.sh`: Start development environment
- `setup_dev.sh`: Initial setup
- `setup_databases.sh`: Database setup
- `test.sh`: Run tests
- `repl.sh`: Start REPL with dev profile

## Development Workflow

### 1. Daily Development Setup
```bash
# Start all required services
./backend/scripts/dev.sh start

# Stop services when done
./backend/scripts/dev.sh stop
```

### 2. Development Process
1. Start REPL for interactive development:
   ```bash
   ./backend/scripts/repl.sh
   ```

2. Run tests while developing:
   ```bash
   ./backend/scripts/test.sh watch
   ```

3. Check database status:
   ```bash
   ./backend/scripts/dev.sh status
   ```

### 3. Testing
- Write tests in `backend/test/`
- Run specific test: `./backend/scripts/test.sh <test-name>`
- Run all tests: `./backend/scripts/test.sh`
- Watch mode: `./backend/scripts/test.sh watch`

### 4. Database Management
- Reset development database: `./backend/scripts/dev.sh reset-db`
- Reset test database: `./backend/scripts/test.sh reset-db`
- View database logs: `./backend/scripts/dev.sh logs <database-name>`

### 5. Code Quality
- Run before committing:
  ```bash
  ./backend/scripts/quality.sh
  ```
  This will:
  - Format code
  - Run linter
  - Run tests
  - Check for outdated dependencies

### 6. Debugging
- View logs: `./backend/scripts/dev.sh logs`
- Check service status: `./backend/scripts/dev.sh status`
- Reset environment: `./backend/scripts/dev.sh reset`

## Best Practices

1. **REPL-Driven Development**
   - Keep a REPL running during development
   - Test functions interactively
   - Reload changed namespaces frequently

2. **Testing**
   - Write tests before implementing features
   - Run tests frequently
   - Use test watch mode during development

3. **Database**
   - Use migrations for schema changes
   - Keep test database in sync with development
   - Clean up test data after tests

4. **Code Organization**
   - Follow Clojure naming conventions
   - Keep functions small and focused
   - Document public functions and complex logic

5. **Version Control**
   - Make small, focused commits
   - Write descriptive commit messages
   - Keep feature branches up to date with main

6. **Dependencies**
   - Check for updates regularly
   - Test thoroughly after updating dependencies
   - Document dependency changes
