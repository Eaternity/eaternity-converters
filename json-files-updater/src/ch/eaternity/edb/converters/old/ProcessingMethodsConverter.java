package ch.eaternity.edb.converters.old;

import json.JSONArray;
import json.JSONObject;
import json.JSONTokener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Just here as a reference, most likely not needed anymore.
 */
public class ProcessingMethodsConverter {

	public void convertDirectory(String inDirStr, String inFileEncoding, String fileNameFilterPattern, Map<String, ProcParam> processes, String outDirStr, String outFileEncoding) throws IOException {
	File inDir = new File(inDirStr);
		File outDir = new File(outDirStr);

		if (!inDir.isDirectory() || !outDir.isDirectory()) {
			throw new IOException("ProcessingMethodsConverter.convert: This is not a directory: " + inDirStr);
		}

		File[] inFiles = inDir.listFiles();
		for (File inFile : inFiles) {
			if (inFile.getName().matches(fileNameFilterPattern)) {
				convertFile(inFile, inFileEncoding, processes, outDir, outFileEncoding);
			}
		}
	}


	private void convertFile(File inFile, String inFileEncoding, Map<String, ProcParam> processes, File outDir, String outFileEncoding) throws IOException {

		JSONObject json = readFile(inFile, inFileEncoding);
		String currentFileName = inFile.getName();

		processProcess(currentFileName, json, "production-names", "production-values", "production-methods", processes);
		processProcess(currentFileName, json, "processing-names", "processing-values", "processing-methods", processes);
		processProcess(currentFileName, json, "conservation-names", "conservation-values", "preservation-methods", processes);
		processProcess(currentFileName, json, "packaging-names", "packaging-values", "packaging-methods", processes);

		BufferedWriter bw = null;
		//Writer writer = null;
		try {
			String path = outDir.getAbsolutePath();
			File outFile = new File(path + "/" + inFile.getName());
			bw = new BufferedWriter(new FileWriter(outFile));
			bw.write(json.toString(2));
		}
		catch (IOException e) {
			e.printStackTrace(System.err);
		}
		finally {
			if (bw != null) {
				bw.close();
			}
		}
	}

	private void processProcess(String currentFileName, JSONObject json, String procArrayNameStr, String procArrayValueStr, String procArrayIdStr, Map<String, ProcParam> processes) {

		//System.out.println("Processing file: " + currentFileName);

		if (json.has(procArrayNameStr)) {

			if (json.has(procArrayValueStr)) {

				String procNames = (String) json.get(procArrayNameStr);
				String procValues = (String) json.get(procArrayValueStr);

				Map<String, String> m = parseProcessStrings(currentFileName, procNames, procValues);
				for (Map.Entry<String, String> entry : m.entrySet()) {
					String key = entry.getKey();
					String val = entry.getValue();

					// Some processes must be ignored
					if (!processes.get(key).ignore) {

						//System.out.println("Key: " + key + " value: " + val);
						if (val.equalsIgnoreCase("S")) {

							// It's a standard process
							if (!processes.containsKey(key)) {
								throw new IllegalArgumentException("Unknown process " + key + " in file " + currentFileName);
							}
							Integer procId = processes.get(key.toLowerCase()).id;
							storeId(currentFileName, json, procArrayIdStr, procId);
						}
						else {
							// It's a non-standard process
							System.out.println("File " + currentFileName + " has non-standard process " + key + " with value " + val);
						}

					}
					else {
						System.out.println("Ignoring process " + key + " in file " + currentFileName);
					}

				}
			}
			else {
				throw new IllegalArgumentException("File " + currentFileName + " has " + procArrayNameStr + " but has not " + procArrayValueStr);
			}
		}
	}

	private void storeId(String currentFileName, JSONObject json, String procArrayIdStr, Integer procIdToStore) {

		JSONArray procIds = null;
		if (json.has(procArrayIdStr)) {
			procIds = json.getJSONArray(procArrayIdStr);
		}
		else {
			procIds = new JSONArray();
			json.put(procArrayIdStr, procIds);
		}

		// Attention: We must store the ID as a string! JSONForm expects this.
		procIds.put(procIdToStore.toString());
	}


	private Map<String, String> parseProcessStrings(String currentFileName, String nameArrayStr, String valueArrayStr) {
		Map<String, String> ret;

		// Check to see whether there is is an inconsistency between e.g. production-names and production-values
		String[] nameArray = nameArrayStr.trim().split(",(\\s)*");
		String[] valueArray = valueArrayStr.trim().split(",(\\s)*");

		if (nameArray.length == valueArray.length) {
			ret = new HashMap<String, String>();

			for (int i = 0; i < nameArray.length; i++) {
				ret.put(nameArray[i], valueArray[i]);
			}
			return ret;
		}
		else {
			// production-names and production-values have distinct number of tokens
			throw new IllegalArgumentException("Inconsistent number tokens in " + nameArrayStr + " and " + valueArrayStr + " in file " + currentFileName);
		}
	}

	private JSONObject readFile(File inFile, String encoding) throws IOException {

		// Not sure if we need to close this?
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inFile), encoding));
		JSONTokener jsonTok = new JSONTokener(br);
		JSONObject json = new JSONObject(jsonTok);
		return json;
	}


	public static void main(String[] args) throws IOException {

		// Only processes registered in this map will be stored as IDs. Others not included
		// such as "frisch" will throw an error.
		Map<String, ProcParam> processes = new HashMap<String, ProcParam>();
		processes.put("conserved", new ProcParam(1, false));
		processes.put("frozen", new ProcParam(2, false));
		processes.put("dried", new ProcParam(3, false));
		processes.put("organic", new ProcParam(4, false));
		processes.put("greenhouse", new ProcParam(5, false));
		processes.put("farm", new ProcParam(6, false));
		processes.put("wild-caught", new ProcParam(7, false));

		// Ignore these processes
		processes.put("PET", new ProcParam(-9999, true));
		processes.put("Dose", new ProcParam(-9999, true));
		processes.put("Glas", new ProcParam(-9999, true));
		processes.put("Plastik", new ProcParam(-9999, true));
		processes.put("Pappe", new ProcParam(-9999, true));
		processes.put("filetiert", new ProcParam(-9999, true));
		processes.put("verarbeitet", new ProcParam(-9999, true));
		processes.put("frisch", new ProcParam(-9999, true));

		System.out.println("--------> Begin");
		ProcessingMethodsConverter converter = new ProcessingMethodsConverter();
		converter.convertDirectory(
				"./in/_data/prods/",
				"UTF-8",
				"(.)+[-]prod.json",
				processes,
				"./out/_data/prods/",
				"UTF-8");
		System.out.println("--------> End");
	}


	private static class ProcParam {
		private Integer id;
		private boolean ignore;
		public ProcParam(Integer id, boolean ignore) {
			this.id = id;
			this.ignore = ignore;
		}
	}

}
