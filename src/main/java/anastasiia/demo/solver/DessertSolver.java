package anastasiia.demo.solver;

import anastasiia.demo.dto.DessertRequestDTO;
import anastasiia.demo.dto.DessertResultDTO;
import anastasiia.demo.dto.IngredientDTO;
import anastasiia.demo.enums.Direction;
import anastasiia.demo.enums.ConstraintType;
import anastasiia.demo.enums.TargetType;
import anastasiia.demo.enums.ConstraintOp;

import org.ojalgo.optimisation.*;
import org.springframework.stereotype.Service;
import java.util.function.ToDoubleFunction;

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
            if (request.constraintsBlock.maxPrice > 0)
                addSimpleConstraint(model, variables, ingredients, "MaxPrice", i -> i.price, ConstraintType.UPPER, request.constraintsBlock.maxPrice);
            if (request.constraintsBlock.maxCalories > 0)
                addSimpleConstraint(model, variables, ingredients, "MaxCalories", i -> i.calories, ConstraintType.UPPER, request.constraintsBlock.maxCalories);
            if (request.constraintsBlock.totalWeight > 0)
                addSimpleConstraint(model, variables, null, "TotalWeight", i -> DEFAULT_WEIGHT_COEFFICIENT, ConstraintType.LEVEL, request.constraintsBlock.totalWeight);
            if (request.constraintsBlock.constraints != null) addCustomConstraints(model, variables, ingredients, request);
        }
        if (request.aestheticConstraint != null && request.aestheticConstraint.ingredientName != null && !request.aestheticConstraint.ingredientName.isEmpty()) {
            addAestheticConstraint(model, variables, ingredients, request);
        }
    }

    private void addSimpleConstraint(
            ExpressionsBasedModel model,
            Variable[] variables,
            List<IngredientDTO> ingredients,
            String name,
            ToDoubleFunction<IngredientDTO> coefficientExtractor,
            ConstraintType type,
            double value
    ) {
        addLinearConstraint(model, name, variables, ingredients, coefficientExtractor, type, value);
    }

    private void addLinearConstraint(
            ExpressionsBasedModel model,
            String name,
            Variable[] variables,
            List<IngredientDTO> ingredients,
            ToDoubleFunction<IngredientDTO> coefficientExtractor,
            ConstraintType type,
            double value
    ) {
        Expression expr = model.addExpression(name);
        fillLinearExpression(expr, variables, ingredients, coefficientExtractor);

        switch (type) {
            case UPPER -> expr.upper(value);
            case LOWER -> expr.lower(value);
            case LEVEL -> expr.level(value);
        }
    }

    private void addAestheticConstraint(ExpressionsBasedModel model, Variable[] variables, List<IngredientDTO> ingredients, DessertRequestDTO request) {
        int targetIndex = findIngredientIndexByName(ingredients, request.aestheticConstraint.ingredientName);
        double percent = request.aestheticConstraint.percent;
        String ruleType = request.aestheticConstraint.ruleType;

        if (targetIndex == -1 || percent <= 0) return;

        Expression expr = model.addExpression("AestheticConstraint");

        for (int i = 0; i < variables.length; i++) {
            double coefficient = (i == targetIndex)
                    ? DEFAULT_WEIGHT_COEFFICIENT - percent
                    : -percent;
            expr.set(variables[i], coefficient);
        }

        if ("min".equalsIgnoreCase(ruleType)) {
            expr.lower(0);
        } else if ("max".equalsIgnoreCase(ruleType)) {
            expr.upper(0);
        }
    }


    private void addCustomConstraints(ExpressionsBasedModel model, Variable[] variables, List<IngredientDTO> ingredients, DessertRequestDTO request) {
        for (DessertRequestDTO.ConstraintDTO constraint : request.constraintsBlock.constraints) {
            Expression expr = model.addExpression("Custom_" + constraint.left + "_" + constraint.op + "_" + constraint.right);

            int index = findIngredientIndexByName(ingredients, constraint.left.toLowerCase());

            if (index != -1) {
                expr.set(variables[index], DEFAULT_WEIGHT_COEFFICIENT);
            } else {
                ToDoubleFunction<IngredientDTO> extractor = switch (constraint.left.toLowerCase()) {
                    case "price" -> i -> i.price;
                    case "calories" -> i -> i.calories;
                    case "weight" -> i -> DEFAULT_WEIGHT_COEFFICIENT;
                    default -> null;
                };

                if (extractor != null) {
                    for (int i = 0; i < variables.length; i++) {
                        expr.set(variables[i], extractor.applyAsDouble(ingredients.get(i)));
                    }
                }
            }

            double tolerance = constraint.allowDeviation ? constraint.right * 0.10 : 0;
            constraint.op.applyTo(expr, constraint.right, tolerance); // 💥 единая логика
        }
    }
    /**
     * Define the optimization objective (maximize/minimize according to the goal).
     */
    private void setObjective(ExpressionsBasedModel model, Variable[] variables, List<IngredientDTO> ingredients, DessertRequestDTO request) {
        Expression objective = model.addExpression("Objective");

        if (request.goal == null || request.goal.targetType == null) {
            applyLinearExpression(objective, variables, ingredients, i -> i.price);
            objective.weight(DEFAULT_WEIGHT_COEFFICIENT);
            return;
        }

        if (request.goal.targetType == TargetType.INGREDIENT) {
            int index = findIngredientIndexByName(ingredients, request.goal.targetName);
            if (index != -1) {
                objective.set(variables[index], DEFAULT_WEIGHT_COEFFICIENT);
            }
        } else {
            ToDoubleFunction<IngredientDTO> extractor = request.goal.targetType.getExtractor();
            if (extractor != null) {
                applyLinearExpression(objective, variables, ingredients, extractor);
            }
        }

        double weight = request.goal.direction == Direction.MAXIMIZE
                ? DEFAULT_WEIGHT_COEFFICIENT
                : -DEFAULT_WEIGHT_COEFFICIENT;

        objective.weight(weight);
    }

    private void applyLinearExpression(
            Expression expression,
            Variable[] variables,
            List<IngredientDTO> ingredients,
            ToDoubleFunction<IngredientDTO> coefficientExtractor
    ) {
        fillLinearExpression(expression, variables, ingredients, coefficientExtractor);
    }

    private void fillLinearExpression(
            Expression expression,
            Variable[] variables,
            List<IngredientDTO> ingredients,
            ToDoubleFunction<IngredientDTO> coefficientExtractor
    ) {
        for (int i = 0; i < variables.length; i++) {
            double coefficient = (ingredients != null)
                    ? coefficientExtractor.applyAsDouble(ingredients.get(i))
                    : coefficientExtractor.applyAsDouble(null);
            expression.set(variables[i], coefficient);
        }
    }


    private int findIngredientIndexByName(List<IngredientDTO> ingredients, String name) {
        for (int i = 0; i < ingredients.size(); i++) {
            if (ingredients.get(i).name.equalsIgnoreCase(name)) {
                return i;
            }
        }
        return -1; // not found
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
