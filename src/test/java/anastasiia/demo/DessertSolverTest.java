package anastasiia.demo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

@DisplayName("DessertSolver Unit Tests")
class DessertSolverTest {

    private final DessertSolver solver = new DessertSolver();

    @Nested
    @DisplayName("Successful Cases")
    class SuccessfulCases {

        @Test
        @DisplayName("Solve a basic optimization problem")
        void testSolveSimpleCase() {
            // Arrange
            IngredientDTO chocolate = new IngredientDTO();
            chocolate.name = "Chocolate";
            chocolate.price = 2.0;
            chocolate.calories = 500.0;

            IngredientDTO strawberry = new IngredientDTO();
            strawberry.name = "Strawberry";
            strawberry.price = 1.5;
            strawberry.calories = 100.0;

            DessertRequestDTO request = new DessertRequestDTO();
            request.ingredients = List.of(chocolate, strawberry);

            DessertRequestDTO.ConstraintsBlock constraintsBlock = new DessertRequestDTO.ConstraintsBlock();
            constraintsBlock.maxPrice = 10.0;
            constraintsBlock.maxCalories = 1500.0;
            constraintsBlock.totalWeight = 3.0;
            request.constraintsBlock = constraintsBlock;

            DessertRequestDTO.GoalDTO goal = new DessertRequestDTO.GoalDTO();
            goal.targetType = TargetType.PRICE;
            goal.direction = Direction.MINIMIZE;
            request.goal = goal;

            // Act
            DessertResultDTO result = solver.solve(request);

            // Assert
            assertEquals("OPTIMAL", result.status);
            assertEquals(3.0, result.totalWeight, 0.001);
            assertTrue(result.price > 0);
            assertEquals(2, result.ingredientsQuantities.size());
        }
    }

    @Nested
    @DisplayName("Advanced Constraints")
    class AdvancedCases {

        @Test
        @DisplayName("Respect aesthetic constraint: minimum chocolate percentage")
        void testAestheticConstraint_MinimumPercent() {
            // Arrange
            IngredientDTO chocolate = new IngredientDTO();
            chocolate.name = "Chocolate";
            chocolate.price = 2.0;
            chocolate.calories = 500.0;

            IngredientDTO cream = new IngredientDTO();
            cream.name = "Cream";
            cream.price = 1.0;
            cream.calories = 300.0;

            DessertRequestDTO request = new DessertRequestDTO();
            request.ingredients = List.of(chocolate, cream);

            DessertRequestDTO.ConstraintsBlock constraintsBlock = new DessertRequestDTO.ConstraintsBlock();
            constraintsBlock.maxPrice = 10.0;
            constraintsBlock.maxCalories = 1500.0;
            constraintsBlock.totalWeight = 3.0;
            request.constraintsBlock = constraintsBlock;

            DessertRequestDTO.AestheticConstraint aesthetic = new DessertRequestDTO.AestheticConstraint();
            aesthetic.ingredientName = "Chocolate";
            aesthetic.ruleType = "min";
            aesthetic.percent = 0.3; // at least 30%
            request.aestheticConstraint = aesthetic;

            DessertRequestDTO.GoalDTO goal = new DessertRequestDTO.GoalDTO();
            goal.targetType = TargetType.PRICE;
            goal.direction = Direction.MINIMIZE;
            request.goal = goal;

            // Act
            DessertResultDTO result = solver.solve(request);

            // Assert
            double chocolateQty = result.ingredientsQuantities.get("Chocolate");
            double totalQty = result.totalWeight;
            double chocolatePercent = chocolateQty / totalQty;
            assertTrue(chocolatePercent >= 0.3);
        }

        @Test
        @DisplayName("Respect custom constraint: minimum quantity of sugar")
        void testCustomConstraint_MinSugarQuantity() {
            // Arrange
            IngredientDTO sugar = new IngredientDTO();
            sugar.name = "Sugar";
            sugar.price = 0.5;
            sugar.calories = 400.0;

            IngredientDTO cream = new IngredientDTO();
            cream.name = "Cream";
            cream.price = 2.0;
            cream.calories = 600.0;

            DessertRequestDTO request = new DessertRequestDTO();
            request.ingredients = List.of(sugar, cream);

            DessertRequestDTO.ConstraintsBlock constraintsBlock = new DessertRequestDTO.ConstraintsBlock();
            constraintsBlock.maxPrice = 10.0;
            constraintsBlock.maxCalories = 1500.0;
            constraintsBlock.totalWeight = 5.0;
            request.constraintsBlock = constraintsBlock;

            DessertRequestDTO.ConstraintDTO customConstraint = new DessertRequestDTO.ConstraintDTO();
            customConstraint.left = "Sugar";
            customConstraint.op = Operator.GREATER_THAN_OR_EQUAL;
            customConstraint.right = 2.0;
            customConstraint.allowDeviation = false;
            constraintsBlock.constraints = List.of(customConstraint);

            DessertRequestDTO.GoalDTO goal = new DessertRequestDTO.GoalDTO();
            goal.targetType = TargetType.PRICE;
            goal.direction = Direction.MINIMIZE;
            request.goal = goal;

            // Act
            DessertResultDTO result = solver.solve(request);

            // Assert
            double sugarQty = result.ingredientsQuantities.get("Sugar");
            assertTrue(sugarQty >= 2.0);
        }
    }

    @Nested
    @DisplayName("Error Cases")
    class ErrorCases {

        @Test
        @DisplayName("Infeasible optimization problem due to strict constraints")
        void testSolveInfeasibleCase() {
            // Arrange
            IngredientDTO sugar = new IngredientDTO();
            sugar.name = "Sugar";
            sugar.price = 1.0;
            sugar.calories = 400.0;

            IngredientDTO cream = new IngredientDTO();
            cream.name = "Cream";
            cream.price = 2.0;
            cream.calories = 600.0;

            DessertRequestDTO request = new DessertRequestDTO();
            request.ingredients = List.of(sugar, cream);

            DessertRequestDTO.ConstraintsBlock constraintsBlock = new DessertRequestDTO.ConstraintsBlock();
            constraintsBlock.maxPrice = 1.0;
            constraintsBlock.maxCalories = 100.0;
            constraintsBlock.totalWeight = 3.0;
            request.constraintsBlock = constraintsBlock;

            DessertRequestDTO.GoalDTO goal = new DessertRequestDTO.GoalDTO();
            goal.targetType = TargetType.PRICE;
            goal.direction = Direction.MINIMIZE;
            request.goal = goal;

            // Act
            DessertResultDTO result = solver.solve(request);

            // Assert
            assertNotNull(result.status);
            assertTrue(result.status.equalsIgnoreCase("INFEASIBLE") || result.status.equalsIgnoreCase("INVALID"));
        }
    }
}
