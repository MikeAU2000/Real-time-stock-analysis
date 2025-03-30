# Real-time Stock Analysis System

A real-time stock analysis system based on a front-end and back-end separated architecture, providing real-time stock data visualization and analysis capabilities.

## Tech Stack

### Frontend Tech Stack
- Vue 3
- TypeScript
- Vite
- Chart.js (for data visualization)
- Pinia (state management)
- Vue Router
- Vitest (unit testing)
- Playwright (E2E testing)

### Backend Tech Stack
- Spring Boot 3.4.3
- Java 17
- PostgreSQL
- Redis
- Spring Data JPA
- Spring Data Redis
- Lombok
- Jackson

## Project Structure

```
.
├── stock_frontend_project_latest/    # Frontend project
│   ├── src/                         # Source code
│   ├── public/                      # Static assets
│   ├── dist/                        # Build output
│   └── e2e/                         # E2E tests
└── demo-bc-xfin-service-latest/     # Backend project
    └── src/                         # Source code
```

## Development Environment Requirements

- Node.js (Latest LTS version recommended)
- Java 17
- Maven
- PostgreSQL
- Redis

## Running Instructions

### Frontend Project

1. Navigate to the frontend project directory:
```bash
cd stock_frontend_project_latest
```

2. Install dependencies:
```bash
npm install
```

3. Start development server:
```bash
npm run dev
```

4. Build for production:
```bash
npm run build
```

### Backend Project

1. Navigate to the backend project directory:
```bash
cd demo-bc-xfin-service-latest
```

2. Build the project with Maven:
```bash
mvn clean install
```

3. Run the application:
```bash
mvn spring-boot:run
```

## Testing

### Frontend Testing
- Run unit tests:
```bash
npm run test:unit
```
- Run E2E tests:
```bash
npm run test:e2e
```

### Backend Testing
```bash
mvn test
```

## Features

- Real-time stock data visualization
- Stock chart analysis
- Data caching
- Responsive design
- Comprehensive test coverage

## Contributing

1. Fork the project
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

MIT License