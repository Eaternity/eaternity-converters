package ch.eaternity.converters.xls2json;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;


@JsonRootName("nutrition-data")
// actually JSON is not ordered by nature. Also not for testing needed. But looks nice.
// Be careful: the method corresponding to the "title" below must be getTitle etc.
@JsonPropertyOrder( {
	"name",
	"id",
	"country",
	"comment",
	"nutr-vals"
})

/**
 * Responsibility: <br/>
 * Converts {@link NutritionData} into JSON.
 *
 */
public class NutritionDataConverter {

	private NutritionData nutritionData;

	public NutritionDataConverter() {}

	public NutritionDataConverter(NutritionData nutritionData) {
		this.nutritionData = nutritionData;
	}

	public String getName() {
		return this.nutritionData.getName();
	}
	
	public String getCountry() {
		return this.nutritionData.getCountry();
	}
	
	public String getId() {
		return this.nutritionData.getId();
	}
	
	public String getComment() {
		return this.nutritionData.getComment();
	}
	
	@JsonProperty("nutr-vals")
	public List<NutrientConverter> getNutrients() {
		List<NutrientConverter> nutrientConverters = new ArrayList<NutrientConverter>();
		for (Nutrient nutrient : nutritionData.getNutrients()) {
			nutrientConverters.add(new NutrientConverter(nutrient));
		}
		return nutrientConverters;
	}
}
