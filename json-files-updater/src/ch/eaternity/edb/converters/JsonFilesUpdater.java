package ch.eaternity.edb.converters;

import ch.eaternity.edb.converters.current.ProductNutritionLinkCsvSchema;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import json.JSONObject;
import json.JSONTokener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract class to collect commonly used variables and methods by all the different Converters / Updaters
 * Not yet very nicely implemented.
 *
 * See the packages old (most likely not reusable anymore) and current (most likely reusable) for implementations
 */
public abstract class JsonFilesUpdater {

    protected static final Logger LOGGER = Logger.getLogger(JsonFilesUpdater.class.getName());

    protected static String inDirStr;
    protected static String inFileEncoding = "UTF-8";
    protected static String fileNameFilterPattern;
    protected static String outDirStr;
    protected static String outFileEncoding = "UTF-8";

    protected static File inDir;
    protected static File outDir;

    protected static JSONObject readFile(File inFile, String encoding) throws IOException {
        // Not sure if we need to close this?
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inFile), encoding));
        JSONTokener jsonTok = new JSONTokener(br);
        JSONObject json = new JSONObject(jsonTok);
        return json;
    }

    public static void initializeDirs() throws IOException {
        inDir = new File(inDirStr);
        outDir = new File(outDirStr);

        if (!inDir.isDirectory()) {
            throw new IOException("JsonPropertyConverter.convert: This is not a directory: " + inDirStr);
        } else if (!outDirStr.isEmpty() && !outDir.isDirectory()) {
            throw new IOException("JsonPropertyConverter.convert: This is not a directory: " + outDirStr);
        }
    }

    public static void writeFile(String filename, String content) {
        try {
            File file = new File(filename);

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            String path = outDir.getAbsolutePath();
            File outFile = new File(path + "/" + filename);

            OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(outFile), outFileEncoding);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.close();

            LOGGER.log(Level.INFO, "File successfully Written: " + filename);

        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }
    }

    public static List<ProductNutritionLinkCsvSchema> readCSVFile(File csvFile, Class schemaClass) throws Exception {
        CsvSchema schema = new CsvMapper().schemaFor(schemaClass)
                .withColumnSeparator(Constants.DEFAULT_CSV_DELIMITER)
                .withHeader();

        MappingIterator<ProductNutritionLinkCsvSchema> csvIterator = new CsvMapper().readerFor(schemaClass).with(schema).readValues(csvFile);

        return csvIterator.readAll();
    }

    /*
    public static String writeSchemaListToString(final Class schemaClass, final List<> schemaList) {
        CsvMapper mapper = new CsvMapper();
        CsvSchema schema = mapper.schemaFor(schemaClass);
        schema = schema.withColumnSeparator(Constants.DEFAULT_CSV_DELIMITER);
        schema = schema.withHeader();
        try {
            return mapper.writer(schema).writeValueAsString(schemaList);
        } catch (JsonProcessingException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return e.getMessage();
        }
    }
     */
}
