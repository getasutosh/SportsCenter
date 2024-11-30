# SportsCenter Platform

A comprehensive sports training and performance tracking platform.

## Development Setup

### Prerequisites
- macOS
- Homebrew
- Java 17 (OpenJDK)
- Clojure
- Node.js and npm
- Docker (optional, for containerized development)

### Quick Start

1. Clone the repository:
```bash
git clone https://github.com/yourusername/sports-center.git
cd sports-center
```

2. Run the development setup script:
```bash
./backend/scripts/setup_dev.sh
```

This script will:
- Install required development tools
- Configure the development environment
- Set up all necessary databases
- Configure environment variables

3. Start the development servers:
```bash
# Start backend services
cd backend
clj -M:dev

# In another terminal, start frontend development server
cd frontend
npm install
npm start
```

### Development Environment

The development environment includes:

#### Backend
- Clojure-based REST API
- Multiple database support:
  - PostgreSQL (primary database)
  - MongoDB (document storage)
  - Redis (caching)
  - InfluxDB (time-series data)
- WebSocket support for real-time features
- JWT authentication
- Comprehensive test suite

#### Frontend
- React with TypeScript
- Modern UI components
- Real-time data updates
- Responsive design
- Unit and integration tests

### Configuration

Development configuration is stored in:
- `.env.development` - Environment variables
- `resources/config/development.edn` - Application configuration
- `docker-compose.yml` - Container configuration

### Testing

Run the test suite:
```bash
# Backend tests
cd backend
clj -M:test

# Frontend tests
cd frontend
npm test
```

### Database Management

Database scripts are available in `backend/scripts/`:
- `setup_databases.sh` - Set up all databases
- `setup_test_dbs.sh` - Set up test databases
- `setup_influxdb.sh` - Configure InfluxDB

### Contributing

1. Create a new branch for your feature
2. Write tests for your changes
3. Implement your changes following TDD
4. Submit a pull request

### License

[Your chosen license]
