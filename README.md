# Smart Recipe Optimizer

A Java + Spring Boot backend service to solve a linear programming dessert optimization problem.

## Project Overview
Smart Recipe Optimizer is an intelligent tool designed to optimize the composition of recipes, ingredient blends, and dietary plans.
It helps businesses find the best combination of ingredients based on specified goals and constraints, such as:

Budget
Calorie count
Weight
Aesthetic appeal of the dish (e.g., preserving key ingredients)
Individual customer preferences

The tool is built using optimization techniques (linear programming) and can be customized for use cases in the food industry, retail, restaurant management, and nutrition planning.

## Example Business Case
Challenge:
Pastry chef Gianna wants to reduce the calories in her signature dessert by 15% while preserving mint as a key visual element.

Solution:
Smart Recipe Optimizer automatically calculates the optimal balance of three ingredients, minimizing calories while ensuring that mint remains above the required threshold.
As a result, the chef receives an updated recipe that meets new health standards without compromising the dish's aesthetic quality.

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
```bash

src
├── main
│   └── java
│       └── anastasiia.demo
│           ├── controller
│           │   └── DemoApplication.java         # Main Spring Boot Controller
│           ├── dto
│           │   ├── DessertRequestDTO.java        # Request DTO (input format)
│           │   ├── DessertResultDTO.java         # Result DTO (output format)
│           │   └── IngredientDTO.java            # Ingredient model
│           ├── enums
│           │   ├── Direction.java                # Optimization direction (maximize/minimize)
│           │   ├── Operator.java                 # Constraint operators (>=, <=, ==)
│           │   └── TargetType.java               # Target optimization field (price, calories, etc.)
│           └── solver
│               └── DessertSolver.java            # Core optimization solver
├── resources
│   └── application.properties                    # Spring Boot config (empty for now)
└── test
    └── java
        └── anastasiia.demo
            ├── DemoApplicationTests.java         # Spring Boot application tests
            └── DessertSolverTest.java             # Full unit tests for solver logic
```
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
```
## Author

Created by **Anastasiia Desiateryk**  
Open for **collaboration and professional opportunities**.
