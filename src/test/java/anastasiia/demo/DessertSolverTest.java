package anastasiia.demo;

import anastasiia.demo.dto.DessertRequestDTO;
import anastasiia.demo.dto.DessertResultDTO;
import anastasiia.demo.dto.IngredientDTO;
import anastasiia.demo.enums.ConstraintOp;
import anastasiia.demo.enums.Direction;
import anastasiia.demo.enums.TargetType;
import anastasiia.demo.solver.DessertSolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import org.junit.jupiter.params.provider.Arguments;

@DisplayName("DessertSolver Documentation-Oriented Tests")
class DessertSolverParameterizedTest {

    private final DessertSolver solver = new DessertSolver();

    @ParameterizedTest
    @MethodSource("createTestParameters")
    @DisplayName("Given complete input when solved then exact expected result is returned")
    void givenCompleteInput_whenSolved_thenReturnsExpected(DessertRequestDTO input, DessertResultDTO expected) {
        DessertResultDTO actual = solver.solve(input);

        assertEquals(expected.status, actual.status, "Status mismatch");
        assertEquals(expected.totalWeight, actual.totalWeight, 0.0001, "Total weight mismatch");
        assertEquals(expected.price, actual.price, 0.0001, "Price mismatch");
        assertEquals(expected.totalCalories, actual.totalCalories, 0.0001, "Calories mismatch");
        assertEquals(expected.ingredientsQuantities, actual.ingredientsQuantities, "Quantities mismatch");
    }

    private static Stream<Arguments> createTestParameters() {
        return Stream.of(
                arguments(createSimpleRequest(), createExpectedSimpleResult()),
                arguments(createAestheticRequest(), createExpectedAestheticResult()),
                arguments(createCustomConstraintRequest(), createExpectedCustomResult())
        );
    }

    private static IngredientDTO ingredient(String name, double price, double calories) {
        IngredientDTO i = new IngredientDTO();
        i.name = name;
        i.price = price;
        i.calories = calories;
        return i;
    }

    private static DessertRequestDTO.GoalDTO goal(TargetType type, Direction direction) {
        DessertRequestDTO.GoalDTO goal = new DessertRequestDTO.GoalDTO();
        goal.targetType = type;
        goal.direction = direction;
        return goal;
    }

    private static DessertRequestDTO.ConstraintsBlock constraints(double price, double calories, double weight) {
        DessertRequestDTO.ConstraintsBlock block = new DessertRequestDTO.ConstraintsBlock();
        block.maxPrice = price;
        block.maxCalories = calories;
        block.totalWeight = weight;
        return block;
    }

    private static DessertResultDTO expectedResult(String status, double weight, double price, double calories, Map<String, Double> quantities) {
        DessertResultDTO result = new DessertResultDTO();
        result.status = status;
        result.totalWeight = weight;
        result.price = price;
        result.totalCalories = calories;
        result.ingredientsQuantities = new LinkedHashMap<>(quantities);
        return result;
    }

    private static DessertRequestDTO createSimpleRequest() {
        DessertRequestDTO request = new DessertRequestDTO();
        request.ingredients = List.of(
                ingredient("Chocolate", 2.0, 500.0),
                ingredient("Strawberry", 1.5, 100.0)
        );
        request.constraintsBlock = constraints(10.0, 1500.0, 3.0);
        request.goal = goal(TargetType.PRICE, Direction.MINIMIZE);
        return request;
    }

    private static DessertResultDTO createExpectedSimpleResult() {
        return expectedResult(
                "OPTIMAL",
                3.0,
                4.5,
                300.0,
                Map.of("Strawberry", 3.0, "Chocolate", 0.0)
        );
    }

    private static DessertRequestDTO createAestheticRequest() {
        DessertRequestDTO request = new DessertRequestDTO();
        request.ingredients = List.of(
                ingredient("Chocolate", 2.0, 500.0),
                ingredient("Cream", 1.0, 300.0)
        );
        request.constraintsBlock = constraints(10.0, 1500.0, 3.0);

        DessertRequestDTO.AestheticConstraint aesthetic = new DessertRequestDTO.AestheticConstraint();
        aesthetic.ingredientName = "Chocolate";
        aesthetic.ruleType = "min";
        aesthetic.percent = 0.3;
        request.aestheticConstraint = aesthetic;

        request.goal = goal(TargetType.PRICE, Direction.MINIMIZE);
        return request;
    }

    private static DessertResultDTO createExpectedAestheticResult() {
        return expectedResult(
                "OPTIMAL",
                3.0,
                3.9,
                1080.0,
                Map.of("Chocolate", 0.9, "Cream", 2.1)
        );
    }

    private static DessertRequestDTO createCustomConstraintRequest() {
        DessertRequestDTO request = new DessertRequestDTO();
        request.ingredients = List.of(
                ingredient("Sugar", 0.5, 400.0),
                ingredient("Cream", 2.0, 600.0)
        );
        DessertRequestDTO.ConstraintsBlock block = constraints(10.0, 2000.0, 5.0); // bumped calories

        DessertRequestDTO.ConstraintDTO constraint = new DessertRequestDTO.ConstraintDTO();
        constraint.left = "Sugar";
        constraint.op = ConstraintOp.GREATER_THAN_OR_EQUAL;
        constraint.right = 2.0;
        constraint.allowDeviation = false;
        block.constraints = List.of(constraint);

        request.constraintsBlock = block;
        request.goal = goal(TargetType.PRICE, Direction.MINIMIZE);
        return request;
    }

    private static DessertResultDTO createExpectedCustomResult() {
        return expectedResult(
                "OPTIMAL",
                5.0,
                2.5,
                2000.0,
                Map.of("Sugar", 5.0, "Cream", 0.0)
        );
    }
}
