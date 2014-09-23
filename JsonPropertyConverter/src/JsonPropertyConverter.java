import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import org.json.JSONObject;
import org.json.JSONTokener;

public class JsonPropertyConverter {
	
	public static enum ConversionType {
		INTEGER_TO_STRING;
		// Add other conversions here if needed
	}
	
	public void convertDirectory(String inDirStr, String inFileEncoding, String fileNameFilterPattern, ConversionParam[] convParams, String outDirStr, String outFileEncoding) throws IOException {
		File inDir = new File(inDirStr);
		File outDir = new File(outDirStr);
		
		if (!inDir.isDirectory() || !outDir.isDirectory()) {
			throw new IOException("JsonPropertyConverter.convert: This is not a directory: " + inDirStr);
		}
		
		File[] inFiles = inDir.listFiles();
		for (File inFile : inFiles) {
			if (inFile.getName().matches(fileNameFilterPattern)) {
				convertFile(inFile, inFileEncoding, convParams, outDir, outFileEncoding);
			}
		}
	}
	
	private void convertFile(File inFile, String inFileEncoding, ConversionParam[] convParams, File outDir, String outFileEncoding) throws IOException {
		
			JSONObject json = readFile(inFile, inFileEncoding);

			for (ConversionParam convParam : convParams) {
		
				BufferedWriter bw = null;
				try {
					switch (convParam.convType) {
					case INTEGER_TO_STRING:
						if (json.has(convParam.key)) convertIntegerToString(inFile.getName(), json, convParam);
						break;
						
					// Add other cases if needed here, e.g. case DOUBLE_TO_STRING or STRING_TO_INTEGER...
						
					default:
						throw new IllegalArgumentException("Unknown Conversion Type: " + convParam.convType);
						//break;
					}
					
					String path = outDir.getAbsolutePath();
					File outFile = new File(path + "/" + inFile.getName());
					bw = new BufferedWriter(new FileWriter(outFile));
					json.write(bw);
				}
				catch (IOException e) {
					
				}
				finally {
					bw.close();
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
	
	private void convertIntegerToString(String currentFileName, JSONObject json, ConversionParam convParam) {
		Object o = json.get(convParam.key);
		if (o instanceof Integer) {
			System.out.println("Processing file: " + currentFileName + " [key: " + convParam.key + "]");
			
			Integer integer = (Integer) json.get(convParam.key);
			json.put(convParam.key, integer.toString());
		}
		else if (o instanceof String) {
			// Do nothing
		}
		else {
			throw new IllegalArgumentException("Unexpected type: " + o.getClass());
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		ConversionParam[] convParams =  {
				new ConversionParam("nutrition-id", ConversionType.INTEGER_TO_STRING),
				new ConversionParam("group-id", ConversionType.INTEGER_TO_STRING),
				new ConversionParam("linked-id", ConversionType.INTEGER_TO_STRING)};
		
		JsonPropertyConverter converter = new JsonPropertyConverter();
		converter.convertDirectory(
				"./_data/prods/",
				"UTF-8",
				"(.)+[-]prod.json",
				convParams,
				"./out/prods/",
				"UTF-8");
	}
	
	
	private static class ConversionParam {		
		public ConversionType convType;
		public String key;
		public ConversionParam(String key, ConversionType type) {
			this.key = key;
			this.convType = type;
		}
	}

}
