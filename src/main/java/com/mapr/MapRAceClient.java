
package com.mapr;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.google.common.collect.ImmutableMap;
import com.google.protobuf.ByteString;
import com.mapr.fs.AceHelper;
import com.mapr.fs.MapRFileAce;
import com.mapr.fs.MapRFileSystem;
import com.mapr.fs.proto.Common.FSAccessType;
import com.mapr.fs.proto.Common.FileACE;

public class MapRAceClient {

	public static final Map<MultiAceType, FSAccessType> aceTypeMapping = new ImmutableMap.Builder<MultiAceType, FSAccessType>()
			.put(MultiAceType.FILEREAD_ACCESS, FSAccessType.AceRead)
			.put(MultiAceType.FILEWRITE_ACCESS, FSAccessType.AceWrite)
			.put(MultiAceType.FILEEXECUTE_ACCESS, FSAccessType.AceExecute)
			.put(MultiAceType.READDIR_ACCESS, FSAccessType.AceReadDir)
			.put(MultiAceType.ADDCHILD_ACCESS, FSAccessType.AceAddChild)
			.put(MultiAceType.DELETECHILD_ACCESS, FSAccessType.AceDeleteChild)
			.put(MultiAceType.LOOKUPDIR_ACCESS, FSAccessType.AceLookupDir).build();

	private FileSystem fs;

	public MapRAceClient(String maprFsURI) throws IOException {

		setFS(maprFsURI);

	}

	/**
	 * Setting MapR FS object
	 * @param maprFsURI
	 * @throws IOException
	 */
	private void setFS(String maprFsURI) throws IOException {

		Configuration conf = new Configuration();
		conf.set("fs.default.name", maprFsURI);
		conf.set("fs.maprfs.impl", "com.mapr.fs.MapRFileSystem");

		fs = FileSystem.get(URI.create(maprFsURI), conf);

	}

	public static void main(String[] args) throws IOException {

		Options options = new Options();

		Option maprFsUri = new Option("maprfsuri", "maprfsuri", true, "MapR FS URI");
		maprFsUri.setRequired(true);
		options.addOption(maprFsUri);

		Option aceOp = new Option("aceOp", "aceOp", true, "Ace Operations permitted: setace/getace/delace");
		aceOp.setRequired(true);
		options.addOption(aceOp);

		Option path = new Option("path", "path", true, "Path");
		path.setRequired(true);
		options.addOption(path);

		Option aceExpr = new Option("aceExpr", "aceExpr", true, "Ace Expression");
		options.addOption(aceExpr);

		Option setInherit = new Option("setInherit", "setInherit", true, "Enable set Inherit value for sub directories :<true/false>");
		options.addOption(setInherit);

		Option preserveModeBits = new Option("preserveModeBits", "preserveModeBits", true, "Enable preserve Mode bits : <true/false>");
		options.addOption(preserveModeBits);

		Option recursive = new Option("recursive", "recursive", true, "set Ace recursively : <true/false>");
		options.addOption(recursive);

		CommandLineParser parser = new BasicParser();
		HelpFormatter formatter = new HelpFormatter();

		try {
			// parse the command line arguments
			CommandLine line = parser.parse(options, args);
			String maprFsURI = line.getOptionValue("maprfsuri");
			String operation = line.getOptionValue("aceOp");
			String pathStr = line.getOptionValue("path");

			MapRAceClient aceClient = new MapRAceClient(maprFsURI);
			aceClient.setFS(maprFsURI);

			if (!aceClient.isPathExists(new Path(pathStr))) {

				System.err.println("Invalid path !!" + pathStr);

			}

			if (operation.equalsIgnoreCase("setace")) {

				String aceExpression = line.getOptionValue("aceExpr");
				String preserveModeBitsStr = line.getOptionValue("preserveModeBits");
				String setInheritStr = line.getOptionValue("setInherit");
				String recursiveStr = line.getOptionValue("recursive");

				aceClient.validateAceOptions(aceExpression, preserveModeBitsStr, setInheritStr, recursiveStr);

				aceClient.setAce(operation, pathStr, aceExpression, preserveModeBitsStr, setInheritStr, recursiveStr);
			} else if (operation.equalsIgnoreCase("delace")) {

				aceClient.delAce(pathStr);

			} else if (operation.equalsIgnoreCase("getace")) {

				aceClient.getAce(pathStr);

			}else {
				
				System.err.println("Invalid ace operations!");
				formatter.printHelp("MapRAceClient", options);
				
			}

		} catch (ParseException exp) {
			// oops, something went wrong
			System.err.println("Parsing failed.  Reason: " + exp.getMessage());
			formatter.printHelp("MapRAceClient", options);

		}

	}

	private boolean validateAceOptions(String aceExpr, String preserveModeBitsStr, String setInheritStr,
			String recursiveStr) {

		boolean isValid = true;
		if (StringUtils.isBlank(aceExpr)) {

			isValid = false;

		}
		if (preserveModeBitsStr != null && !"true".equalsIgnoreCase(preserveModeBitsStr)
				&& !"false".equalsIgnoreCase(preserveModeBitsStr)) {

			isValid = false;

		}

		if (setInheritStr != null && !"true".equalsIgnoreCase(setInheritStr)
				&& !"false".equalsIgnoreCase(setInheritStr)) {
			isValid = false;

		}

		if (recursiveStr != null && !"true".equalsIgnoreCase(recursiveStr) && !"false".equalsIgnoreCase(recursiveStr)) {

			isValid = false;

		}

		return isValid;

	}

	private void getAce(String pathStr) throws IOException {

		Path path = new Path(pathStr);

		List<MapRFileAce> aceList = ((MapRFileSystem) fs).getAces(path);
		System.out.println("\nAce settings for path - " + pathStr);

		for (MapRFileAce aceValue : aceList) {

			System.out.println(aceValue.getAccessType().name().toLowerCase() + " : " + aceValue.getBooleanExpression());
		}

	}

	private void delAce(String pathStr) throws IOException {

		Path path = new Path(pathStr);

		((MapRFileSystem) fs).deleteAces(path);

	}

	private void setAce(String operation, String pathStr, String aceExpression, String preserveModeBitsStr,
			String setInheritStr, String recursiveStr) throws IOException {

		Path path = new Path(pathStr);

		ArrayList<FileACE> faces = parseAceOptions(aceExpression);

		// preserve mode 1 = true ,0 = false and false by default.
		int preservemodebits = preserveModeBitsStr == null ? 0 : preserveModeBitsStr.equals("true") ? 1 : 0;
		// 1 for inherit = false and 0 = true and true by default
		int noinherit = setInheritStr == null ? 0 : setInheritStr.equals("true") ? 0 : 1;

		boolean recursive = "true".equalsIgnoreCase(recursiveStr) ? true : false;

		int status = ((MapRFileSystem) fs).setAces(path, faces, false, noinherit, preservemodebits, recursive, null);

		if (status == 0) {
			System.out.println("\nAce for path:" + pathStr + " has been set successfully!" + " status :" + status);

			List<MapRFileAce> aceList = ((MapRFileSystem) fs).getAces(path);
			System.out.println("\nCurrent Ace values for path - " + pathStr);

			for (MapRFileAce aceValue : aceList) {

				System.out.println(
						aceValue.getAccessType().name().toLowerCase() + " : " + aceValue.getBooleanExpression());
			}
		} else {

			System.out.println("\nAce set for path:" + pathStr + " has been failed with" + " status :" + status);

		}
	}

	private boolean isPathExists(Path path) throws IOException {

		return fs.exists(path);
	}

	private ArrayList<FileACE> parseAceOptions(String multiAceStr) throws IOException {

		ArrayList<FileACE> faces = new ArrayList<FileACE>();
		String[] aceStrArray = multiAceStr.split(",");
		for (String aceStr : aceStrArray) {
			String[] pair = aceStr.split(":", 2);
			String aceType = pair[0];
			String aceExpr = pair[1];
			FSAccessType acessType = getAccessTypeMapping(aceType);
			if (acessType == null) {
				throw new IllegalArgumentException("Invalid Ace option -" + aceType);
			}
			faces.add(FileACE.newBuilder().setAccessType(acessType)
					.setBoolExp(ByteString.copyFromUtf8(AceHelper.toPostfix(aceExpr))).build());

		}

		return faces;

	}

	private FSAccessType getAccessTypeMapping(String aceType) {

		return aceTypeMapping.get(MultiAceType.get(aceType));
	}
}