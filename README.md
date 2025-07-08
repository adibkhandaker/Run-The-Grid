# Run the Grid - NFL Data Application

A JavaFX desktop application for exploring NFL data including team statistics, schedules, draft information, trade simulations, and more.

## Features

- Team statistics and analysis
- NFL schedules and box scores
- Draft information by year
- Trade simulator with value calculator
- Depth charts for teams
- Matchup predictor
- Latest NFL news
- NFL records database

## Prerequisites

- Java 22 or higher
- Maven 3.6 or higher

## Installation

1. Clone the repository:
```bash
git clone https://github.com/adibkhandaker/Run-The-Grid.git
cd Run-The-Grid
```

2. Install dependencies:
```bash
mvn clean install
```

## Running the Application

### Option 1: Using Maven (Recommended)
```bash
mvn clean javafx:run
```

### Option 2: Using Maven Wrapper
```bash
./mvnw clean javafx:run
```

### Option 3: Compile and Run Manually
```bash
mvn clean compile
mvn javafx:run
```

## Project Structure

```
NFLData/
├── src/main/java/org/example/nfldata/     # Java source files
│   ├── HelloApplication.java             # Main application entry point
│   ├── *Controller.java                  # UI controllers for each feature
│   └── *.java                           # Data models and utilities
├── src/main/resources/                   # Resources directory
│   ├── data/drafts/                     # Draft data files
│   └── org/example/nfldata/             # FXML files and styles
│       ├── *.fxml                       # UI layout files
│       └── styles.css                   # Application styles
├── lib/                                 # External JAR dependencies
├── pom.xml                             # Maven configuration
└── README.md                           # This file
```

## Dependencies

- JavaFX 22.0.1 (Controls and FXML)
- Gson 2.10.1 (JSON processing)
- JSON Simple 1.1.1 (JSON utilities)
- Lombok 1.18.30 (Code generation)
- JUnit 5.10.2 (Testing)

## Development

### Building the Project
```bash
mvn clean compile
```

### Running Tests
```bash
mvn test
```

### Creating a Distribution
```bash
mvn clean package
```

## Troubleshooting

### Common Issues

1. **JavaFX Runtime Components Missing**
   - Ensure you have Java 22 installed
   - Use the Maven JavaFX plugin to run the application

2. **Module Path Issues**
   - The application uses Java modules, ensure your JDK supports modules
   - Run using the provided Maven commands

3. **Build Failures**
   - Verify Maven is installed and accessible
   - Check that JAVA_HOME points to Java 22 or higher

### System Requirements

- Operating System: Windows, macOS, or Linux
- RAM: Minimum 512MB, Recommended 1GB
- Disk Space: 100MB for application and dependencies

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details. 