package ch.eaternity.edb.converters.current;

import ch.eaternity.edb.converters.Constants;
import ch.eaternity.edb.converters.JsonFilesUpdater;
import json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/*
Batch processes all prod json files in a folder, overwriting their existing
nutrition-id links with new links loaded from the productToNutrition.csv.
 */
public class ProductNutritionLinkUpdater extends JsonFilesUpdater {

    public static void main(String[] args) throws Exception {

        ProductNutritionLinkUpdater nutritionLinkUpdater = new ProductNutritionLinkUpdater();

        inDirStr = "json-files-updater/res/prods/";
        fileNameFilterPattern = "(.)+[-]prod.json";
        outDirStr = "json-files-updater/out/prods/";

        //TODO this is a weird path and has to do with the project structure
        String csvFileStr = "json-files-updater/res/productToNutrition.csv";
        File csvFile = new File(csvFileStr);

        // loadCSV with Matching Items
        List<ProductNutritionLinkCsvSchema> productNutritionLinkCsvSchemas = JsonFilesUpdater.readCSVFile(csvFile, ProductNutritionLinkCsvSchema.class);
        Map<String, String> nutritionLinkMap = new HashMap<>();
        for (ProductNutritionLinkCsvSchema link : productNutritionLinkCsvSchemas) {
            nutritionLinkMap.put(link.getProductId(), link.getNutritionDataId());
        }


        // load all baseProducts
        File inDir = new File(inDirStr);

        if (!inDir.isDirectory()) {
            throw new IOException("This is not a directory: " + inDirStr);
        }

        File[] inFiles = inDir.listFiles();
        for (File inFile : inFiles) {
            if (inFile.getName().matches(fileNameFilterPattern)) {
                nutritionLinkUpdater.replaceNutrIdInFile(inFile, inFileEncoding, new File(outDirStr), outFileEncoding, nutritionLinkMap);
            }
        }
    }

    private void replaceNutrIdInFile(File inFile, String inFileEncoding, File outDir, String outFileEncoding, Map<String, String> nutritionLinkMap) throws IOException {
        JSONObject json = readFile(inFile, inFileEncoding);

        BufferedWriter bw = null;
        try {
            if (json.has(Constants.NUTRITION_ID)) {
                String filename = inFile.getName();
                replaceNutrId(filename, json, nutritionLinkMap);

                //Save File again
                String path = outDir.getAbsolutePath();
                File outFile = new File(path + "/" + filename);
                bw = new BufferedWriter(new FileWriter(outFile));
                json.write(bw, 2, 2);
            }
        }
        catch (IOException e) {

        }
        finally {
            if (bw != null) {
                bw.close();
            }
        }
    }

    private void replaceNutrId(String currentFileName, JSONObject json, Map<String, String> nutritionLinkMap) {
        Object productIdObj = json.get(Constants.ID);
        if (productIdObj instanceof Integer) {
            String productId = ((Integer) productIdObj).toString();
            LOGGER.log(Level.INFO, "Processing File " + currentFileName + " replacing nutritionId with " + nutritionLinkMap.get(productId));
            json.put(Constants.NUTRITION_ID, nutritionLinkMap.get(productId));
        }
        else {
            throw new IllegalArgumentException("Unexpected type: " + productIdObj.getClass());
        }

    }
}
