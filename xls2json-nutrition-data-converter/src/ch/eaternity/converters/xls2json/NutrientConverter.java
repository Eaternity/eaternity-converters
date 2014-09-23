package ch.eaternity.converters.xls2json;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;


@JsonRootName("nutrient")
// actually JSON is not ordered by nature. Also not for testing needed. But looks nice.
// Be careful: the method corresponding to the "title" below must be getTitle etc.
@JsonPropertyOrder( {
		"component-id",
		"value",
		"unit"
	})

/**
 * Responsibility:<br/>
 * Stores the information of a nutrient.
 * @author Andreas Scheuss
 *
 */
public class NutrientConverter {

	private Nutrient nutrient;
	
	private NutrientConverter() {}
	
	public NutrientConverter(Nutrient nutrient) {
		this.nutrient = nutrient;
	}
	
	@JsonProperty("component-id")
	public String getComponentId() {
		return nutrient.getComponentId();
	}
	
	public Double getValue() {
		return nutrient.getValue();
	}

	public String getUnit() {
		return nutrient.getUnit();
	}

}
