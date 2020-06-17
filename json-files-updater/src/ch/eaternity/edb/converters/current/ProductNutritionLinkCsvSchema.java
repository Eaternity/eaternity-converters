package ch.eaternity.edb.converters.current;

import ch.eaternity.edb.converters.CsvSchema;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * TODO define responsibility of class
 */
@JsonPropertyOrder({ProductNutritionLinkCsvSchema.PRODUCT_ID, ProductNutritionLinkCsvSchema.NUTRITION_DATA_ID})
public class ProductNutritionLinkCsvSchema extends CsvSchema {

    public static final String PRODUCT_ID = "Product ID";
    public static final String NUTRITION_DATA_ID = "NutritionData ID";

    private String productId;
    private String nutritionDataId;

    @JsonProperty(PRODUCT_ID)
    public String getProductId() {
        return productId;
    }

    @JsonProperty(PRODUCT_ID)
    public void setProductId(String productId) {
        this.productId = productId;
    }

    @JsonProperty(NUTRITION_DATA_ID)
    public String getNutritionDataId() {
        return nutritionDataId;
    }

    @JsonProperty(NUTRITION_DATA_ID)
    public void setNutritionDataId(String nutritionDataId) {
        this.nutritionDataId = nutritionDataId;
    }
}
