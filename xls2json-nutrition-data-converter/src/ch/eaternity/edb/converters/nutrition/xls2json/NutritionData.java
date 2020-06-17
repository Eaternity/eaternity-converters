package ch.eaternity.edb.converters.nutrition.xls2json;

import java.util.ArrayList;
import java.util.List;

public class NutritionData {

	private String name;
	private String id; // Eaternity given ID
	private String originalId; // ID given from Source
	private String country;
	private String comment;
	private List<Nutrient> nutrients = new ArrayList<Nutrient>();


	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public List<Nutrient> getNutrients() {
		return nutrients;
	}
	public void setNutrients(List<Nutrient> nutrient) {
		this.nutrients = nutrient;
	}
	public void addNutrient(Nutrient nutrient) {
		this.nutrients.add(nutrient);
	}

	public String getOriginalId() {
		return originalId;
	}

	public void setOriginalId(String originalId) {
		this.originalId = originalId;
	}
}
