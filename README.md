# Dessert Solver

A Java + Spring Boot backend service to solve a linear programming dessert optimization problem.

## Features
- Ingredient selection based on optimization goals
- Constraints on total price, calories, and weight
- Support for custom user constraints
- Aesthetic rules (minimum/maximum proportion of ingredients)
- Built with ojAlgo optimization engine
- Full Unit Test Coverage (Junit5)

## Technologies
- Java 21
- Spring Boot 3
- Maven
- ojAlgo 48.3.0 (Optimization library)
- JUnit 5 (Testing)


## Project Structure

src
├── main
│   └── java
│       └── anastasiia.demo
│           ├── controller
│           │   └── DemoApplication.java        # Main Spring Boot Controller
│           ├── dto
│           │   ├── DessertRequestDTO.java       # Request DTO
│           │   ├── DessertResultDTO.java        # Result DTO
│           │   └── IngredientDTO.java           # Ingredient DTO
│           ├── enums
│           │   ├── Direction.java               # Enum: Direction
│           │   ├── Operator.java                # Enum: Operator
│           │   └── TargetType.java              # Enum: TargetType
│           └── solver
│               └── DessertSolver.java           # Optimization Solver
├── resources
│   
└── test
    └── java
        └── anastasiia.demo
            ├── DemoApplicationTests.java        # Spring Boot Application Tests
            └── DessertSolverTest.java            # Full unit tests for solver


## API Endpoints

| Method | URL | Description |
|:------:|:---:|:------------:|
| POST   | `/solve-dessert` | Solves dessert optimization problem |

## Run Locally

```bash
mvn spring-boot:run
Service will be available at: http://localhost:8080/solve-dessert

Testing
Run all tests:
bash
mvn test




Author
Created by Anastasiia Desiateryk
Open for collaboration and professional opportunities

