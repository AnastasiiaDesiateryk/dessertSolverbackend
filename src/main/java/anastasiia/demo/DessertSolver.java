
package anastasiia.demo;

import anastasiia.demo.IngredientDTO;
import org.ojalgo.optimisation.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DessertSolver {

    public static Map<String, Object> solve(DessertRequestDTO request) {
        List<IngredientDTO> ingredients = request.ingredients;

        Variable[] x = new Variable[ingredients.size()];
        for (int i = 0; i < ingredients.size(); i++) {
            x[i] = Variable.make(ingredients.get(i).name).lower(0);
        }

        ExpressionsBasedModel model = new ExpressionsBasedModel();
        for (Variable v : x) model.addVariable(v);

        if (request.max_price > 0) {
            Expression priceExpr = model.addExpression("Price").upper(request.max_price);
            for (int i = 0; i < x.length; i++) priceExpr.set(x[i], ingredients.get(i).price);
        }

        if (request.max_calories > 0) {
            Expression calExpr = model.addExpression("Calories").upper(request.max_calories);
            for (int i = 0; i < x.length; i++) calExpr.set(x[i], ingredients.get(i).calories);
        }

        if (request.total_weight > 0) {
            Expression weightExpr = model.addExpression("TotalWeight").level(request.total_weight);
            for (int i = 0; i < x.length; i++) weightExpr.set(x[i], 1.0);
        }

        Expression aestheticExpr = model.addExpression("Aesthetic").lower(0);
        for (int i = 0; i < x.length; i++) {
            if (i == request.aesthetic_ingredient_index) aestheticExpr.set(x[i], 1.0);
            else aestheticExpr.set(x[i], -request.min_aesthetic_percent);
        }

        if (ingredients.size() >= 2) {
            model.addExpression("SweetDiffPos").upper(request.max_sweet_diff)
                    .set(x[0], 1).set(x[1], -1);
            model.addExpression("SweetDiffNeg").upper(request.max_sweet_diff)
                    .set(x[0], -1).set(x[1], 1);
        }

        if (request.constraints != null) {
            for (DessertRequestDTO.ConstraintDTO c : request.constraints) {
                Expression customExpr = model.addExpression("Custom_" + c.left + "_" + c.op + "_" + c.right);
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
                            for (int i = 0; i < x.length; i++) customExpr.set(x[i], ingredients.get(i).price);
                            break;
                        case "calories":
                            for (int i = 0; i < x.length; i++) customExpr.set(x[i], ingredients.get(i).calories);
                            break;
                        case "weight":
                            for (int i = 0; i < x.length; i++) customExpr.set(x[i], 1.0);
                            break;
                    }
                }

                double tolerance = c.allow_deviation ? c.right * 0.10 : 0;
                switch (c.op) {
                    case "==":
                        customExpr.lower(c.right - tolerance);
                        customExpr.upper(c.right + tolerance);
                        break;
                    case "<":
                        customExpr.upper(c.right - 1e-6 + tolerance);
                        break;
                    case "<=":
                        customExpr.upper(c.right + tolerance);
                        break;
                    case ">":
                        customExpr.lower(c.right + 1e-6 - tolerance);
                        break;
                    case ">=":
                        customExpr.lower(c.right - tolerance);
                        break;
                }
            }
        }

        Expression objective = model.addExpression("Objective");
        boolean isMinimize = false;

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
                isMinimize = true;
            }
        } else {
            for (int i = 0; i < x.length; i++) {
                objective.set(x[i], ingredients.get(i).price);
            }
            objective.weight(1.0);
        }

        Optimisation.Result result = model.maximise();

        Map<String, Object> output = new LinkedHashMap<>();
        double totalWeight = 0;
        double totalPrice = 0;
        double totalCalories = 0;

        for (int i = 0; i < x.length; i++) {
            double qty = result.get(i).doubleValue();
            output.put(ingredients.get(i).name, qty);
            totalWeight += qty;
            totalPrice += qty * ingredients.get(i).price;
            totalCalories += qty * ingredients.get(i).calories;
        }

        output.put("status", result.getState().toString());
        output.put("price", totalPrice);
        output.put("total_weight", totalWeight);
        output.put("total_calories", totalCalories);

        return output;
    }
}

