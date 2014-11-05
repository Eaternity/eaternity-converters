import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;

import org.json.JSONObject;
import org.json.JSONTokener;


public class JsonPropertyTranslator {

	public static enum ConversionType {
		INTEGER_TO_STRING;
		// Add other conversions here if needed
	}
	
	public void convertDirectory(String inDirStr, String inFileEncoding, String fileNameFilterPattern, TranslationParam[] translationParams, String outDirStr, String outFileEncoding) throws IOException {
	File inDir = new File(inDirStr);
		File outDir = new File(outDirStr);
		
		if (!inDir.isDirectory() || !outDir.isDirectory()) {
			throw new IOException("JsonPropertyConverter.convert: This is not a directory: " + inDirStr);
		}
		
		File[] inFiles = inDir.listFiles();
		for (File inFile : inFiles) {
			if (inFile.getName().matches(fileNameFilterPattern)) {
				convertFile(inFile, inFileEncoding, translationParams, outDir, outFileEncoding);
			}
		}
	}
	
	private void translatePropertyValue(String currentFileName, JSONObject json, String propertyKey, String oldValue, String newValue) {
		Object o = json.get(propertyKey);
		if (o instanceof String) {			
			String oldPropertyValue = (String) o;
			if (oldPropertyValue.contains(oldValue)) {
				System.out.println("Translating in file " + currentFileName + " property " + propertyKey + " from " + oldValue + " to " + newValue);
				String newPropertyValue = oldPropertyValue.toLowerCase().replaceAll(oldValue.toLowerCase(), newValue);
				json.put(propertyKey, newPropertyValue);
			}
		}
		else {
			throw new IllegalArgumentException("Unexpected property value type: " + o.getClass());
		}	
	}
	
	private void convertFile(File inFile, String inFileEncoding, TranslationParam[] translationParams, File outDir, String outFileEncoding) throws IOException {
		
			JSONObject json = readFile(inFile, inFileEncoding);

			for (TranslationParam translationParam : translationParams) {
		
				BufferedWriter bw = null;
				Writer writer = null;
				try {
					if (json.has(translationParam.key)) {
						translatePropertyValue(inFile.getName(), json, translationParam.key, translationParam.oldPropertyValue, translationParam.newPropertyValue);
					}
										
					String path = outDir.getAbsolutePath();
					File outFile = new File(path + "/" + inFile.getName());
					bw = new BufferedWriter(new FileWriter(outFile));
					writer = json.write(bw);
				}
				catch (IOException e) {
					e.printStackTrace(System.err);
				}
				finally {
					//bw.close();
					
					if (writer != null) {
						writer.close();
					}
				}
			}
	}
	
	private JSONObject readFile(File inFile, String encoding) throws IOException {
		
		// Not sure if we need to close this?
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inFile), encoding));
		JSONTokener jsonTok = new JSONTokener(br);
		JSONObject json = new JSONObject(jsonTok);
		return json;
	}
		
	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {		
		TranslationParam[] translationParams = {
				new TranslationParam("production-names", "bio", "organic"),
				new TranslationParam("production-names", "gewächshaus", "greenhouse"),
				new TranslationParam("production-names", "gh", "greenhouse"),
				new TranslationParam("production-names", "konventionell", "standard"),
				new TranslationParam("production-names", "aquakultur", "farm"),
				new TranslationParam("production-names", "fang", "wild-caught"),
				new TranslationParam("production-names", "frisch", "fresh"),
				new TranslationParam("conservation-names", "tiefgekühlt", "frozen"),
				new TranslationParam("conservation-names", "tk", "frozen"),
				new TranslationParam("conservation-names", "getrocknet", "dried"),
				new TranslationParam("conservation-names", "konserviert", "conserved"),
				new TranslationParam("conservation-names", "dose", "canned")
		};
		
		JsonPropertyTranslator converter = new JsonPropertyTranslator();
		converter.convertDirectory(
				"./in/_data/prods/",
				"UTF-8",
				"(.)+[-]prod.json",
				translationParams,
				"./out/_data/prods/",
				"UTF-8");
	}
	
	
	private static class TranslationParam {		
		public String key;
		public String oldPropertyValue; // is case ignorant!
		public String newPropertyValue;
		public TranslationParam(String key, String oldPropertyValue, String newPropertyValue) {
			this.key = key;
			this.oldPropertyValue = oldPropertyValue;
			this.newPropertyValue = newPropertyValue;
		}
	}

}
