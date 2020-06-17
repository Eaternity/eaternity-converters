package ch.eaternity.edb.converters.old;

import ch.eaternity.edb.converters.Constants;
import ch.eaternity.edb.converters.JsonFilesUpdater;
import ch.eaternity.edb.converters.nutrition.xls2json.ExcelConverter;
import json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;


/**
 * Replaces the ID's of all nutrition json files found in /nutrs folder
 * with a new ID.
 */
public class JsonNutrFilesConverter extends JsonFilesUpdater {

	private Integer newNutrId = 1;
	private Map<String, String> nutrIdMap = new HashMap<>();
	private Map<String, String> nutrNameMap = new HashMap<>();

	public JsonNutrFilesConverter() throws IOException {
		inDirStr = "res/nutrs/";
		fileNameFilterPattern = "(.)+[-]nutr.json";
		outDirStr = "out/nutrs/";

		initializeDirs();
	}

	public static void main(String[] args) throws IOException {
		JsonNutrFilesConverter converter = new JsonNutrFilesConverter();
		converter.convertJSONFilesInDirectory();
		converter.writeCSVFile();
	}

	public void convertJSONFilesInDirectory() throws IOException {
		File[] inFiles = inDir.listFiles();
		for (File inFile : inFiles) {
			if (inFile.getName().matches(fileNameFilterPattern)) {
				convertFile(inFile, "" + newNutrId);
				newNutrId++;
			}
		}
	}

	private void writeCSVFile() {
		String fileContent = "";

		Iterator<String> iterator = nutrIdMap.keySet().iterator();
		while (iterator.hasNext()) {
			String next = iterator.next();
			fileContent = fileContent + next + "; " + nutrIdMap.get(next) + "; " + nutrNameMap.get(next) + System.lineSeparator();
		}

		writeFile("Nutrition-Id-Mapping.csv", fileContent);
	}

	private void convertFile(File inFile, String newNutrId) throws IOException {

			JSONObject json = readFile(inFile, inFileEncoding);

			BufferedWriter bw = null;
			try {
				if (json.has(Constants.ID)) {
					String nutrName = replaceNutrId(inFile.getName(), json, newNutrId);
					String filename = ExcelConverter.getNutrFilename("out/nutrs/", newNutrId, nutrName);
					File outFile = new File(filename);
					bw = new BufferedWriter(new FileWriter(outFile));
					json.write(bw, 2, 2);
				}
			}
			catch (IOException e) {

			}
			finally {
				bw.close();
			}
	}

	private String replaceNutrId(String currentFileName, JSONObject json, String newNutrId) {
		Object o = json.get(Constants.ID);
		if (o instanceof String) {
			LOGGER.log(Level.INFO, "Processing file: " + currentFileName + " [key: " + Constants.ID + "]");

			String oldId = (String) json.get(Constants.ID);
			json.put(Constants.ID, newNutrId);
			json.put(Constants.ORIGINAL_ID, oldId);

			nutrIdMap.put(oldId, newNutrId);

			Object ob = json.get("name");
			if (ob instanceof String) {
				LOGGER.log(Level.INFO, "Processing file: " + currentFileName + " [key: " + "name" + "]");

				String name = (String) json.get("name");
				nutrNameMap.put(oldId, name);
				return name;
			}
			else {
				throw new IllegalArgumentException("Unexpected type: " + o.getClass());
			}
		}
		else {
			throw new IllegalArgumentException("Unexpected type: " + o.getClass());
		}
	}
}
