package anastasiia.demo;

import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.optimisation.Expression;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DessertSolver {

    public static Map<String, Object> solve(DessertRequestDTO request) {
        List<DessertRequestDTO.IngredientDTO> ingredients = request.ingredients;

        Variable[] x = new Variable[ingredients.size()];
        for (int i = 0; i < ingredients.size(); i++) {
            x[i] = Variable.make(ingredients.get(i).name).lower(0);
        }

        ExpressionsBasedModel model = new ExpressionsBasedModel();
        for (Variable v : x) model.addVariable(v);

        double maxPrice = request.max_price;
        double maxCalories = request.max_calories;
        int aestheticIndex = request.aesthetic_ingredient_index;
        double minAestheticPercent = request.min_aesthetic_percent;

        if (maxPrice > 0) {
            Expression priceExpr = model.addExpression("Price").upper(maxPrice);
            for (int i = 0; i < x.length; i++) priceExpr.set(x[i], ingredients.get(i).price);
        }

        if (maxCalories > 0) {
            Expression calExpr = model.addExpression("Calories").upper(maxCalories);
            for (int i = 0; i < x.length; i++) calExpr.set(x[i], ingredients.get(i).calories);
        }

        Expression aestheticExpr = model.addExpression("Aesthetic").lower(0);
        for (int i = 0; i < x.length; i++) {
            if (i == aestheticIndex) aestheticExpr.set(x[i], 1.0);
            else aestheticExpr.set(x[i], -minAestheticPercent);
        }

        if (ingredients.size() >= 2) {
            model.addExpression("SweetDiffPos").upper(request.max_sweet_diff)
                    .set(x[0], 1).set(x[1], -1);
            model.addExpression("SweetDiffNeg").upper(request.max_sweet_diff)
                    .set(x[0], -1).set(x[1], 1);
        }

        if (request.constraints != null) {
            for (DessertRequestDTO.ConstraintDTO c : request.constraints) {
                Expression customExpr = model.addExpression("Custom_" + c.left);
                boolean matched = false;

                for (int i = 0; i < ingredients.size(); i++) {
                    if (ingredients.get(i).name.equalsIgnoreCase(c.left)) {
                        customExpr.set(x[i], 1.0);
                        matched = true;
                        break;
                    }
                }

                if (!matched) {
                    switch (c.left.toLowerCase()) {
                        case "price":
                            for (int i = 0; i < x.length; i++)
                                customExpr.set(x[i], ingredients.get(i).price);
                            break;
                        case "calories":
                            for (int i = 0; i < x.length; i++)
                                customExpr.set(x[i], ingredients.get(i).calories);
                            break;
                        case "weight":
                            for (int i = 0; i < x.length; i++)
                                customExpr.set(x[i], 1.0);
                            break;
                    }
                }

                switch (c.op) {
                    case "==": customExpr.level(c.right); break;
                    case "<":  customExpr.upper(c.right - 1e-6); break;
                    case "<=": customExpr.upper(c.right); break;
                    case ">":  customExpr.lower(c.right + 1e-6); break;
                    case ">=": customExpr.lower(c.right); break;
                }
            }
        }

        Expression objective = model.addExpression("Objective");

        if (request.goal != null) {
            String type = request.goal.target_type;
            String name = request.goal.target_name;
            String direction = request.goal.direction;

            if ("ingredient".equals(type)) {
                for (int i = 0; i < ingredients.size(); i++) {
                    if (ingredients.get(i).name.equals(name)) {
                        objective.set(x[i], 1.0);
                    }
                }
            } else if ("price".equals(type)) {
                for (int i = 0; i < ingredients.size(); i++) {
                    objective.set(x[i], ingredients.get(i).price);
                }
            } else if ("calories".equals(type)) {
                for (int i = 0; i < ingredients.size(); i++) {
                    objective.set(x[i], ingredients.get(i).calories);
                }
            }

            if ("maximize".equalsIgnoreCase(direction)) {
                objective.weight(1.0);
            } else {
                objective.weight(-1.0);
            }
        } else {
            for (int i = 0; i < x.length; i++) {
                objective.set(x[i], ingredients.get(i).price);
            }
            objective.weight(1.0);
        }

        Optimisation.Result result = model.maximise();

        Map<String, Object> output = new HashMap<>();
        for (int i = 0; i < x.length; i++) {
            output.put(ingredients.get(i).name, result.get(i).doubleValue());
        }
        output.put("status", result.getState().toString());
        output.put("objective_value", result.getValue());

        return output;
    }
}
