package anastasiia.demo.enums;

import anastasiia.demo.dto.IngredientDTO;

import java.util.function.ToDoubleFunction;

public enum TargetType {
    INGREDIENT(null),
    PRICE(ingredient -> ingredient.price),
    CALORIES(ingredient -> ingredient.calories);

    private final ToDoubleFunction<IngredientDTO> extractor;

    TargetType(ToDoubleFunction<IngredientDTO> extractor) {
        this.extractor = extractor;
    }

    public ToDoubleFunction<IngredientDTO> getExtractor() {
        return extractor;
    }
}
