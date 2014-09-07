package ch.eaternity.converters.xls2json;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ExcelConverter {
	
	private DateFormat dateFormat = DateFormat.getDateTimeInstance();
	private final String LOG_FILE_NAME = dateFormat.format(new Date()) + "-converted-results.log";

	private final static int COLUMN_NUMBER_ID = 4;
	private final static int COLUMN_NUMBER_NAME = 0;
	private final static int COLUMN_NUMBER_COUNTRY = 1;
	private final static int COLUMN_NUMBER_NUTR_COMPONENT_ID = 8;
	private final static int COLUMN_NUMBER_NUTR_VALUE = 3;
	private final static int COLUMN_NUMBER_NUTR_UNIT = 11;
	
	private final static int ROW_NUMBER_CONTENT_START = 1		;
	
	private static final Logger log = Logger.getLogger(ExcelConverter.class.getName());

	public void convertAllFilesInFolder(String inputFolder, String outputFolder) {
		List<NutritionData> nutritionDataList = new ArrayList<NutritionData>();
		StringBuilder logger = new StringBuilder();
		
		logger.append("Start converting all Excel Files in Folder + " + inputFolder + " to JSON output Folder " + outputFolder + System.lineSeparator());

		nutritionDataList.addAll(extractAllNutritionData(inputFolder, logger));
		writeJsonFiles(nutritionDataList, outputFolder, logger);
		
		logger.append("Finished converting Excel files. " + System.lineSeparator() + "Consult Console or Logfile " + 
				LOG_FILE_NAME + " for processing errors." + System.lineSeparator());
		log.log(Level.SEVERE, logger.toString());
		
		writeFile(LOG_FILE_NAME, logger.toString());
	}


	public List<NutritionData> extractAllNutritionData(String inputFolder, StringBuilder logger) {
		List<NutritionData> nutritionDataList = new ArrayList<NutritionData>();
		File folder = new File(inputFolder);
		File[] listOfFiles = folder.listFiles();
		
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile() && listOfFiles[i].getName().endsWith(".xls")) {
				try {
					
					FileInputStream fileInputStream = new FileInputStream(listOfFiles[i]);
					HSSFWorkbook workbook = new HSSFWorkbook(fileInputStream);
					HSSFSheet worksheet = workbook.getSheetAt(0);
					
			        nutritionDataList.addAll(extractNutritionDataFromSheet(worksheet, logger));
			        logger.append("Processed file: " + listOfFiles[i].getName() + System.lineSeparator());
			    } catch (IOException er) {
			        logger.append("ERROR opening Excel File with name: " + listOfFiles[i].getName() + System.lineSeparator() +
			        		"Cause: " + er.getMessage() + System.lineSeparator());
			    }
			}
		}
		return nutritionDataList;
	}

	public List<NutritionData> extractNutritionDataFromSheet(HSSFSheet worksheet, StringBuilder logger) {
		Map<String, NutritionData> nutritionDataMap = new HashMap<String, NutritionData>();
		
		for (int rowCounter = ROW_NUMBER_CONTENT_START; rowCounter < worksheet.getLastRowNum(); rowCounter ++) {
			HSSFRow row = worksheet.getRow(rowCounter);
			String nutritionDataId;
			//TODO This is a hack... The ids can be numbers or strings. 
			try {
				nutritionDataId = "" + (int) row.getCell(COLUMN_NUMBER_ID).getNumericCellValue();
			} catch (IllegalStateException e) {
				nutritionDataId = "" + (String) row.getCell(COLUMN_NUMBER_ID).getStringCellValue();
			}
			NutritionData nutritionData = nutritionDataMap.get(nutritionDataId);
			if (nutritionData == null) {
				nutritionData = new NutritionData();
				nutritionData.setId(nutritionDataId);
				nutritionData.setName(row.getCell(COLUMN_NUMBER_NAME).getStringCellValue());
				nutritionData.setCountry("");
				nutritionData.setComment("");
			}
			
//			String componentId = row.getCell(COLUMN_NUMBER_NUTR_COMPONENT_ID).getStringCellValue();
			Double value = row.getCell(COLUMN_NUMBER_NUTR_VALUE).getNumericCellValue();
//			String unit = row.getCell(COLUMN_NUMBER_NUTR_UNIT).getStringCellValue(); 
			nutritionData.addNutrient(new Nutrient("ENERC", value, "kJ"));

			nutritionDataMap.put(nutritionDataId, nutritionData);
		}
		 
		return new ArrayList<NutritionData>(nutritionDataMap.values());
	}

	public void writeJsonFiles(List<NutritionData> nutritionDataList, String outputFolder, StringBuilder logger) {
		ObjectMapper mapper = new ObjectMapper();
		
		File directory = new File(outputFolder);

        if (!directory.exists()) 
            directory.mkdir();
		
		for (NutritionData nutritionData : nutritionDataList) {
			String filename = outputFolder + replaceInvalidFilenameChars(nutritionData.getId() + "-" + nutritionData.getName()) + "-nutr.json";
			
			File file = new File(filename);
			 
			try {
				// TODO check weather this works when file already exists - probably not!
				if (!file.exists()) {
					file.createNewFile();
				}
				
				if (file.canWrite())
					mapper.writeValue(file, new NutritionDataConverter(nutritionData));
				else 
					logger.append("Error: cannot write file: " + filename + System.lineSeparator());
			} 
			catch (IOException e) {
				logger.append("Error converting/writing JSON File with name: " + filename + System.lineSeparator());
			}
		}
	}
	
	private void writeFile(String filename, String content) {
		try {
			File file = new File(filename);
 
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
 
			OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(file.getAbsoluteFile()), "UTF-8");
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();
 
			System.out.println("File successfully Written: " + filename);
 
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
	
	/**
	 * Replaces all sorts of problematic input file names such as German Umlaute, accentuated characters etc.
	 * @param fileName the file name containing problematic characters
	 * @return the file name in a canonic form
	 */
	private String replaceInvalidFilenameChars(String fileName) {
		// German Umlaute ae, oe, ue and French accented chars etc. For an overview,
		// see here: http://www.utf8-chartable.de/.
		fileName = fileName.replaceAll("[\u00C0\u00C1\u00C2\u00C3\u00C4\u00C5\u00C6\u00E0\u00E1\u00E2\u00E3\u00E4\u00E5\u00E6]", "a");
		fileName = fileName.replaceAll("[\u00D2\u00D3\u00D4\u00D5\u00D6\u00D8\u00F2\u00F3\u00F4\u00F5\u00F6\u00F8]", "o");
		fileName = fileName.replaceAll("[\u00D9\u00DA\u00DB\u00DC\u00F9\u00FA\u00FB\u00FC]", "u");
		fileName = fileName.replaceAll("[\u00C8\u00C9\u00CA\u00CB\u00E8\u00E9\u00EA\u00EB]", "e");
		fileName = fileName.replaceAll("[\u00CC\u00CD\u00CE\u00CF\u00EC\u00ED\u00EE\u00EF]", "i");
		fileName = fileName.replaceAll("[\u00DF]", "ss");
		fileName = fileName.replaceAll("[\u00C7\u00E7]", "c");
		fileName = fileName.replaceAll("[\u00D1\u00F1]", "n");
		
		// Remove all non-word characters (everything other than [a-zA-Z0-9_] or whitespaces
		fileName = fileName.replaceAll("[\\W|\\s]", "_");
		
		return fileName;
	}
	
}
