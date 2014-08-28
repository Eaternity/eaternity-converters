import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;

public class XmlToMultipleJsonConverter3 {

	/** This is the newline character used by JSONForm in textareas. */
	private static final String NEWLINE = "\r\n";

	/** Illegal "null characters" (0x0) resulting from the FileMaker export are 
	    replaced with empty strings. */
	private static final String NULL_CHAR_REPLACEMENT = "";

	private static final String JSON_ATTR_ROW = "ROW";

	private static final String JSON_ATTR_ID = "ID";
	private static final String JSON_ATTR_RECORDID = "@RECORDID";
	private static final String JSON_ATTR_MODID = "@MODID";
	private static final String JSON_ATTR_NAMEDEUTSCH = "Name_Deutsch";

	private static final String OUT_JSON_FILE_POSTAMBLE = "-edb.json";
	
	
	/**
	 * Main method to read the FileMaker exported xml file and create a bunch of json output file.
	 * @param inXmlFileStr the xml file to read
	 * @param inXmlFileEncoding the xml file's encoding (usually "UTF-8")
	 * @param outDir the output directory to write the json files to
	 * @param outJsonFileEncoding the output files' encoding (usually "UTF-8")
	 * @param overwrite if true then existing output files with the same name will be 
	 * 		overwritten, if false an Exception is thrown
	 * @param prettyPrintJson if true then the json written to the output file will 
	 * 		be "pretty printed", i.e. include indentation and line breaks
	 * @throws IOException
	 */
	private void split(String inXmlFileStr, String inXmlFileEncoding,
			String outDir, String outJsonFileEncoding, boolean overwrite,
			boolean prettyPrintJson) throws IOException {
		try {
			String xml = readXmlFile(inXmlFileStr, inXmlFileEncoding);
			JSONObject json = (JSONObject) (new XMLSerializer().read(xml));
			JSONArray rows = (JSONArray) json.get(JSON_ATTR_ROW);

			for (int i = 0; i < rows.size(); i++) {
				
				JSONObject row = rows.getJSONObject(i);
				String fileName = getOutJsonFileName(outDir, row, OUT_JSON_FILE_POSTAMBLE);
				replaceInvalidProseKeys(row);
				String content = prettyPrintJson ? row.toString(2) : row.toString();
				writeJsonFile(fileName, content, outJsonFileEncoding, true);

			}
			
		} catch (SAXException e) {
			e.printStackTrace(System.err);
		} catch (ParserConfigurationException e) {
			e.printStackTrace(System.err);
		}
	}
	
	/**
	 * Prose does not accept JSON attributes starting with '@' characters such as "@RECORDID".
	 * This method simply removes these characters.
	 * 
	 * @param json
	 */
	private void replaceInvalidProseKeys(JSONObject json) {
		// @RECORDID -> RECORDID
		Object v = json.remove(JSON_ATTR_RECORDID);
		json.put(JSON_ATTR_RECORDID.substring(1, JSON_ATTR_RECORDID.length()), v);
		
		// @MODID -> MODID
		v = json.remove(JSON_ATTR_MODID);
		json.put(JSON_ATTR_MODID.substring(1, JSON_ATTR_MODID.length()), v);
	}

	/**
	 * Reads the FileMaker export xml file
	 * @param inXmlFileStr the xml file to read
	 * @param inXmlFileEncoding the xml file's encoding (usually "UTF-8")
	 * @return the xml in a single string
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	private String readXmlFile(String inXmlFileStr, String inXmlFileEncoding)
			throws IOException, SAXException, ParserConfigurationException {
		File inXmlFile = new File(inXmlFileStr);

		BufferedReader reader = null;

		try {

			// We need to replace invalid 0x0 characters in the input string.
			// For this purpose, we first simply read the whole xml file.
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(inXmlFile), inXmlFileEncoding));
			String line;
			StringBuilder sb = new StringBuilder();
			while ((line = reader.readLine()) != null) {
				sb.append(line).append(NEWLINE);
			}

			String xml = replaceInvalid0x0Chars(sb.toString());
			return xml;
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	/**
	 * Searches a dirty xml input string for null-characters (0x0), which are
	 * not allowed in json. These are then replaced with empty strings ("").
	 * 
	 * @param dirtyXMLString
	 *            the dirty xml
	 * @return the cleaned xml
	 */
	private String replaceInvalid0x0Chars(String dirtyXMLString) {
		String cleanXMLString = null;
		Pattern pattern = null;
		Matcher matcher = null;

		// Regex matching null characters
		pattern = Pattern.compile("[\\000]+");
		matcher = pattern.matcher(dirtyXMLString);
		if (matcher.find()) {
			cleanXMLString = matcher.replaceAll(NULL_CHAR_REPLACEMENT);
		}
		return cleanXMLString;
	}
	
	/**
	 * Creates the JSON file output name in the form of: <id>-<name>-edb.json
	 * @param outDir the output directory
	 * @param json the json object
	 * @param fileNamePostAmble is equal to "-edb.json" for edb files
	 * @return the output file name
	 */
	private String getOutJsonFileName(String outDir, JSONObject json, String fileNamePostAmble) {
		StringBuilder sb = new StringBuilder(outDir);
		if (!outDir.endsWith("/"))
			sb.append("/");
		
		String id = json.getString(JSON_ATTR_ID);
		sb.append(id).append("-");
		
		String nameDeutsch = json.getString(JSON_ATTR_NAMEDEUTSCH).toLowerCase().trim();
		nameDeutsch = replaceInvalidFilenameChars(nameDeutsch);
		sb.append(nameDeutsch);
		
		sb.append(fileNamePostAmble);
		return sb.toString();
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
	
	/**
	 * Write the collected json to the output file
	 * @param outFileStr the name of the file to write to
	 * @param content the file content to write
	 * @param outFileEncoding the output file encoding - usually "UTF-8"
	 * @param overwriteExisting if true then existing files with the same name will be overwritten, if false an Exception is thrown
	 * @throws IOException
	 */
	private void writeJsonFile(String outFileStr, String content, String outFileEncoding, boolean overwriteExisting) throws IOException {
		File outFile = new File(outFileStr);
		if (outFile.exists() && !overwriteExisting) {
			throw new IOException("SplitIntoManyFiles.writeJsonFile: Not allowed to overwrite the existing file " + outFile);
		}
		if (outFile.exists() && overwriteExisting) {
			outFile.delete();
		}
		
		outFile.createNewFile();
		
		BufferedWriter writer = null;
		
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), outFileEncoding));
			writer.write(content);
		}
		finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		new XmlToMultipleJsonConverter3().split(
				"./xml/filemaker-export-kostadinov-komplett-utf8.xml",
				"UTF-8",
				"./json/",
				"UTF-8",
				false,
				true);
	}

}
