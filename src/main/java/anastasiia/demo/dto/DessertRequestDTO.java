package anastasiia.demo.dto;

import anastasiia.demo.enums.Direction;
import anastasiia.demo.enums.ConstraintOp;
import anastasiia.demo.enums.TargetType;

import java.util.List;

public class DessertRequestDTO {

    public List<IngredientDTO> ingredients;
    public AestheticConstraint aestheticConstraint;
    public GoalDTO goal;
    public ConstraintsBlock constraintsBlock;

    // Represents an aesthetic constraint on a specific ingredient
    public static class AestheticConstraint {
        public String ingredientName;   // Name of the ingredient
        public String ruleType;         // "min" or "max"
        public double percent;          // Required percentage
    }

    // Represents the optimization goal (what to maximize/minimize)
    public static class GoalDTO {
        public TargetType targetType;   // Type: INGREDIENT, PRICE, CALORIES
        public String targetName;       // Name if INGREDIENT
        public Direction direction;     // MINIMIZE or MAXIMIZE
    }

    // Represents all constraints grouped together
    public static class ConstraintsBlock {
        public double maxPrice;
        public double maxCalories;
        public double totalWeight;
        public List<ConstraintDTO> constraints;  // Custom user constraints
    }

    // Represents a single custom constraint
    public static class ConstraintDTO {
        public String left;            // Ingredient name or "price"/"calories"/"weight"
        public ConstraintOp op;        // Operator: EQUALS, GREATER_THAN, etc.
        public double right;           // Target value
        public boolean allowDeviation; // Whether small deviation is allowed
    }
}
