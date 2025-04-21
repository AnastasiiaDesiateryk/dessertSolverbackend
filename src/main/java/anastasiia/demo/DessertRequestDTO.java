package anastasiia.demo;

import java.util.List;

public class DessertRequestDTO {

    public List<IngredientDTO> ingredients;
    public double total_weight;
    public double max_price;
    public double max_calories;
    public double max_sweet_diff;
    public int aesthetic_ingredient_index;
    public double min_aesthetic_percent;
    public GoalDTO goal;
    public List<ConstraintDTO> constraints;
    public boolean allow_deviation;

//    public static class IngredientDTO {
//        public String name;
//        public double price;
//        public double calories;
//    }

    public static class GoalDTO {
        public String target_type; // "ingredient", "price", "calories"
        public String target_name; // name of the ingredient or "price" etc.
        public String direction;   // "maximize" or "minimize"
    }

    public static class ConstraintDTO {
        public String left;   // name of ingredient or "price", "calories", "weight"
        public String op;     // one of: ">", ">=", "<", "<=", "=="
        public double right;// constraint value
        public boolean allow_deviation;
    }
}