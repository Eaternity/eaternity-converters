package ch.eaternity.converters.xls2json;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

	private List<NutrientConverter> nutrients;

	public NutritionDataConverter() {}

	public NutritionDataConverter(NutritionData nutritionData) {
		this.nutritionData = nutritionData;
	}


	/**
	 * @throws InvalidNutrientException when a wrong input was set in the converter
	 * @throws IllegalArgumentException when no nutrients are delivered
	 */
	@JsonIgnore
	public NutritionData getNutritionData() {
		return this.nutritionData;
	}

	//JSON fields
	public void setName(String name) {
		this.nutritionData.setName(name);
	}
	
	public void setCountry(String country) {
		this.nutritionData.setCountry(country);
	}
	
	/*
	@JsonProperty("id")
	public String getId() {
		return nutritionData.getId();
	}*/
	
	public void setId(String id) {
		this.nutritionData.setId(id);
	}
	
	public void setComment(String comment) {
		this.nutritionData.setComment(comment);
	}
	
	@JsonProperty("nutr-vals")
	public void setNutrient(List<NutrientConverter> nutrients) {
		this.nutrients = nutrients;
	}
}
