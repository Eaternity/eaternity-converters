package ch.eaternity.edb.converters.nutrition.xls2json;

public class Nutrient {

	private String componentId;
	private Double value;
	private String unit;

	public Nutrient(String componentId, Double value, String unit) {
		this.componentId = componentId;
		this.value = value;
		this.unit = unit;

	}

	public String getComponentId() {
		return componentId;
	}
	public void setComponentId(String componentId) {
		this.componentId = componentId;
	}
	public Double getValue() {
		return value;
	}
	public void setValue(Double value) {
		this.value = value;
	}
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
}
