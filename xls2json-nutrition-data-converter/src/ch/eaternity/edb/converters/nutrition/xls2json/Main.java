package ch.eaternity.edb.converters.nutrition.xls2json;



/**
 * MAKE SURE THE JSON FOLDER IS EMPTY - PROBABLY THE FILES DONT GET OVEWRITTEN!
 * @author aurelian_jaggi
 *
 */
public class Main {

	private static final String INPUT_FOLDER = "xls2json-nutrition-data-converter/res/";
	private static final String OUTPUT_FOLDER = "xls2json-nutrition-data-converter/out/";


	public static void main(String[] args) {

		ExcelConverter excelConverter = new ExcelConverter();
		excelConverter.convertAllFilesInFolder(INPUT_FOLDER , OUTPUT_FOLDER);

	}

}
