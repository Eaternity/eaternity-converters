package ch.eaternity.converters.xls2json;

import java.io.BufferedWriter;
import java.io.File;
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

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ExcelConverter {
	
	private DateFormat dateFormat = DateFormat.getDateInstance();
	private final String LOG_FILE_NAME = dateFormat.format(new Date()) + "-converted-results.log";

	private final static int COLUMN_NUMBER_ID = 2;
	private final static int COLUMN_NUMBER_NAME = 5;
	private final static int COLUMN_NUMBER_COUNTRY = 1;
	private final static int COLUMN_NUMBER_NUTR_COMPONENT_ID = 8;
	private final static int COLUMN_NUMBER_NUTR_VALUE = 10;
	private final static int COLUMN_NUMBER_NUTR_UNIT = 11;
	
	private final static int ROW_NUMBER_CONTENT_START = 4;
	
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
			        //Create a workbook object from the file at specified location.
			        //Change the path of the file as per the location on your computer.
			        Workbook wrk1 =  Workbook.getWorkbook(listOfFiles[i]);
			         
			        //Obtain the reference to the first sheet in the workbook
			        Sheet sheet1 = wrk1.getSheet(0);
			        
			        nutritionDataList.addAll(extractNutritionDataFromSheet(sheet1, logger));
			        wrk1.close();
			    } catch (BiffException | IOException er) {
			        logger.append("ERROR opening Excel File with name: " + listOfFiles[i].getName() + System.lineSeparator() +
			        		"Cause: " + er.getMessage() + System.lineSeparator());
			    }
			}
		}
		return nutritionDataList;
	}

	public void writeJsonFiles(List<NutritionData> nutritionDataList, String outputFolder, StringBuilder logger) {
		ObjectMapper mapper = new ObjectMapper();
		
		File directory = new File(outputFolder);

        if (!directory.exists()) 
            directory.mkdir();
		
		for (NutritionData nutritionData : nutritionDataList) {
			String filename = outputFolder + nutritionData.getId() + "-" + nutritionData.getName() + "-nutr.json";
			
			File file = new File(filename);
			 
			try {
				// TODO check weather this works when file already exists
				// if file doesnt exists, then create it
				if (!file.exists()) {
					file.createNewFile();
				}
				mapper.writeValue(file, new NutritionDataConverter(nutritionData));
			} 
			catch (IOException e) {
				logger.append("Error converting/writing JSON File with name: " + filename + System.lineSeparator());
			}
		}
	}

	public List<NutritionData> extractNutritionDataFromSheet(Sheet sheet1, StringBuilder logger) {
		Map<String, NutritionData> nutritionDataMap = new HashMap<String, NutritionData>();
		
		for (int row = ROW_NUMBER_CONTENT_START; row < sheet1.getRows(); row ++) {
			String nutritionDataId = sheet1.getCell(COLUMN_NUMBER_ID, row).getContents();
			NutritionData nutritionData = nutritionDataMap.get(nutritionDataId);
			if (nutritionData == null) {
				nutritionData = new NutritionData();
				nutritionData.setId(nutritionDataId);
				nutritionData.setName(sheet1.getCell(COLUMN_NUMBER_NAME, row).getContents());
				nutritionData.setCountry(sheet1.getCell(COLUMN_NUMBER_COUNTRY, row).getContents());
				nutritionData.setComment("");
			}
			
			String componentId = sheet1.getCell(COLUMN_NUMBER_NUTR_COMPONENT_ID, row).getContents();
			String valueString = sheet1.getCell(COLUMN_NUMBER_NUTR_VALUE, row).getContents();
			try {
				Double value = Double.valueOf(valueString);
				String unit = sheet1.getCell(COLUMN_NUMBER_NUTR_UNIT, row).getContents(); 
				nutritionData.addNutrient(new Nutrient(componentId, value, unit));
			}
			catch (NumberFormatException nfe) {
				logger.append("Nutrient Value " + valueString + " of Nutrient with Component Id  " + componentId + 
						" and NutritionInfo with EurFIR Id " + nutritionDataId + " not able to convert to Double value." 
						+ System.lineSeparator());
			}
			nutritionDataMap.put(nutritionDataId, nutritionData);
		}
		 
		return new ArrayList<NutritionData>(nutritionDataMap.values());
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
	
	
}
