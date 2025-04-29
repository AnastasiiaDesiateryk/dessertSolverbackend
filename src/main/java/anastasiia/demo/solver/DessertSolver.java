package anastasiia.demo.solver;

import anastasiia.demo.dto.DessertRequestDTO;
import anastasiia.demo.dto.DessertResultDTO;
import anastasiia.demo.dto.IngredientDTO;
import anastasiia.demo.enums.Direction;
import org.ojalgo.optimisation.*;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Solver for the dessert optimization problem based on provided constraints and goals.
 */
@Service
public class DessertSolver {

    private static final double DEFAULT_WEIGHT_COEFFICIENT = 1.0;
    private static final double EPSILON = 1e-6;

    /**
     * Main entry point to solve the optimization request.
     */
    public DessertResultDTO solve(DessertRequestDTO request) {
        List<IngredientDTO> ingredients = request.ingredients;
        Variable[] variables = createVariables(ingredients);
        ExpressionsBasedModel model = new ExpressionsBasedModel();

        addVariablesToModel(model, variables);
        addConstraints(model, variables, ingredients, request);
        setObjective(model, variables, ingredients, request);

        Optimisation.Result result = model.maximise();
        return buildResult(result, variables, ingredients);
    }

    /**
     * Create optimization variables for each ingredient.
     */
    private Variable[] createVariables(List<IngredientDTO> ingredients) {
        Variable[] variables = new Variable[ingredients.size()];
        for (int i = 0; i < ingredients.size(); i++) {
            variables[i] = Variable.make(ingredients.get(i).name).lower(0);
        }
        return variables;
    }

    /**
     * Add all created variables into the optimization model.
     */
    private void addVariablesToModel(ExpressionsBasedModel model, Variable[] variables) {
        for (Variable variable : variables) {
            model.addVariable(variable);
        }
    }

    /**
     * Add constraints (maximum price, calories, weight, aesthetic, and custom).
     */
    private void addConstraints(ExpressionsBasedModel model, Variable[] variables, List<IngredientDTO> ingredients, DessertRequestDTO request) {
        if (request.constraintsBlock != null) {
            if (request.constraintsBlock.maxPrice > 0) addMaxPriceConstraint(model, variables, ingredients, request);
            if (request.constraintsBlock.maxCalories > 0) addMaxCaloriesConstraint(model, variables, ingredients, request);
            if (request.constraintsBlock.totalWeight > 0) addTotalWeightConstraint(model, variables, request);
            if (request.constraintsBlock.constraints != null) addCustomConstraints(model, variables, ingredients, request);
        }
        if (request.aestheticConstraint != null && request.aestheticConstraint.ingredientName != null && !request.aestheticConstraint.ingredientName.isEmpty()) {
            addAestheticConstraint(model, variables, ingredients, request);
        }
    }

    private void addMaxPriceConstraint(ExpressionsBasedModel model, Variable[] variables, List<IngredientDTO> ingredients, DessertRequestDTO request) {
        Expression expr = model.addExpression("MaxPrice").upper(request.constraintsBlock.maxPrice);
        for (int i = 0; i < variables.length; i++) {
            expr.set(variables[i], ingredients.get(i).price);
        }
    }

    private void addMaxCaloriesConstraint(ExpressionsBasedModel model, Variable[] variables, List<IngredientDTO> ingredients, DessertRequestDTO request) {
        Expression expr = model.addExpression("MaxCalories").upper(request.constraintsBlock.maxCalories);
        for (int i = 0; i < variables.length; i++) {
            expr.set(variables[i], ingredients.get(i).calories);
        }
    }

    private void addTotalWeightConstraint(ExpressionsBasedModel model, Variable[] variables, DessertRequestDTO request) {
        Expression expr = model.addExpression("TotalWeight").level(request.constraintsBlock.totalWeight);
        for (Variable variable : variables) {
            expr.set(variable, DEFAULT_WEIGHT_COEFFICIENT);
        }
    }

    private void addAestheticConstraint(ExpressionsBasedModel model, Variable[] variables, List<IngredientDTO> ingredients, DessertRequestDTO request) {
        int targetIndex = -1;
        for (int i = 0; i < ingredients.size(); i++) {
            if (ingredients.get(i).name.equalsIgnoreCase(request.aestheticConstraint.ingredientName)) {
                targetIndex = i;
                break;
            }
        }

        if (targetIndex != -1 && request.aestheticConstraint.percent > 0) {
            Expression expr = model.addExpression("AestheticConstraint");
            for (int i = 0; i < variables.length; i++) {
                if (i == targetIndex) {
                    expr.set(variables[i], DEFAULT_WEIGHT_COEFFICIENT - request.aestheticConstraint.percent);
                } else {
                    expr.set(variables[i], -request.aestheticConstraint.percent);
                }
            }
            if ("min".equalsIgnoreCase(request.aestheticConstraint.ruleType)) {
                expr.lower(0);
            } else if ("max".equalsIgnoreCase(request.aestheticConstraint.ruleType)) {
                expr.upper(0);
            }
        }
    }

    private void addCustomConstraints(ExpressionsBasedModel model, Variable[] variables, List<IngredientDTO> ingredients, DessertRequestDTO request) {
        for (DessertRequestDTO.ConstraintDTO constraint : request.constraintsBlock.constraints) {
            Expression expr = model.addExpression("Custom_" + constraint.left + "_" + constraint.op + "_" + constraint.right);
            boolean matched = false;

            for (int i = 0; i < ingredients.size(); i++) {
                if (ingredients.get(i).name.equalsIgnoreCase(constraint.left)) {
                    expr.set(variables[i], DEFAULT_WEIGHT_COEFFICIENT);
                    matched = true;
                    break;
                }
            }

            if (!matched) {
                switch (constraint.left.toLowerCase()) {
                    case "price" -> { for (int i = 0; i < variables.length; i++) expr.set(variables[i], ingredients.get(i).price); }
                    case "calories" -> { for (int i = 0; i < variables.length; i++) expr.set(variables[i], ingredients.get(i).calories); }
                    case "weight" -> { for (int i = 0; i < variables.length; i++) expr.set(variables[i], DEFAULT_WEIGHT_COEFFICIENT); }
                }
            }

            double tolerance = constraint.allowDeviation ? constraint.right * 0.10 : 0;
            switch (constraint.op) {
                case EQUALS -> { expr.lower(constraint.right - tolerance); expr.upper(constraint.right + tolerance); }
                case LESS_THAN -> expr.upper(constraint.right - EPSILON + tolerance);
                case LESS_THAN_OR_EQUAL -> expr.upper(constraint.right + tolerance);
                case GREATER_THAN -> expr.lower(constraint.right + EPSILON - tolerance);
                case GREATER_THAN_OR_EQUAL -> expr.lower(constraint.right - tolerance);
            }
        }
    }

    /**
     * Define the optimization objective (maximize/minimize according to the goal).
     */
    private void setObjective(ExpressionsBasedModel model, Variable[] variables, List<IngredientDTO> ingredients, DessertRequestDTO request) {
        Expression objective = model.addExpression("Objective");

        // If goal or targetType is missing, fallback to default
        if (request.goal == null || request.goal.targetType == null) {
            for (int i = 0; i < variables.length; i++) {
                objective.set(variables[i], ingredients.get(i).price);
            }
            objective.weight(DEFAULT_WEIGHT_COEFFICIENT);
            return; // Important! Exit the method early
        }

        // Goal is present and valid
        switch (request.goal.targetType) {
            case INGREDIENT -> {
                for (int i = 0; i < ingredients.size(); i++) {
                    if (ingredients.get(i).name.equalsIgnoreCase(request.goal.targetName)) {
                        objective.set(variables[i], DEFAULT_WEIGHT_COEFFICIENT);
                    }
                }
            }
            case PRICE -> {
                for (int i = 0; i < variables.length; i++) {
                    objective.set(variables[i], ingredients.get(i).price);
                }
            }
            case CALORIES -> {
                for (int i = 0; i < variables.length; i++) {
                    objective.set(variables[i], ingredients.get(i).calories);
                }
            }
        }

        objective.weight(request.goal.direction == Direction.MAXIMIZE ? DEFAULT_WEIGHT_COEFFICIENT : -DEFAULT_WEIGHT_COEFFICIENT);
    }

    /**
     * Builds the DessertResultDTO from the optimization result.
     */
    private DessertResultDTO buildResult(Optimisation.Result result, Variable[] variables, List<IngredientDTO> ingredients) {
        DessertResultDTO output = new DessertResultDTO();
        output.ingredientsQuantities = new LinkedHashMap<>();

        double totalWeight = 0;
        double totalPrice = 0;
        double totalCalories = 0;

        for (int i = 0; i < variables.length; i++) {
            double qty = result.get(i).doubleValue();
            output.ingredientsQuantities.put(ingredients.get(i).name, qty);
            totalWeight += qty;
            totalPrice += qty * ingredients.get(i).price;
            totalCalories += qty * ingredients.get(i).calories;
        }

        output.status = result.getState().toString();
        output.price = totalPrice;
        output.totalWeight = totalWeight;
        output.totalCalories = totalCalories;

        return output;
    }
}
