package ch.eaternity.converters.xls2json;




public class Main {
	
	private static final String INPUT_FOLDER = "excel/";
	private static final String OUTPUT_FOLDER = "nutrition-json/";
	
	
	public static void main(String[] args) {
	
		ExcelConverter excelConverter = new ExcelConverter();
		excelConverter.convertAllFilesInFolder(INPUT_FOLDER , OUTPUT_FOLDER);
		
	}
	
}
