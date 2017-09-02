
package com.mapr;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.mapr.fs.MapRFileAce;
import com.mapr.fs.MapRFileSystem;

public class MapRAceTest {

	public static final String MAPRFS_URI = "maprfs:///";

	public static void main(String[] args) throws IOException {

		String aceTestPathStr = "/tmp/ace_test_file";

		Configuration conf = new Configuration();
		conf.set("fs.default.name", MAPRFS_URI);
		conf.set("fs.maprfs.impl", "com.mapr.fs.MapRFileSystem");

		FileSystem fs = null;

		try {

			fs = FileSystem.get(URI.create(MAPRFS_URI), conf);
			Path path = new Path(aceTestPathStr);

			// Creating test file if not exits.
			if (!fs.exists(path)) {
				fs.createNewFile(path);
			}

			// Setting AccessType and expression. Refer to http://maprdocs.mapr.com/apidocs/fileace_javadocs/ 
			// for different AccessType.

			ArrayList<MapRFileAce> faces = new ArrayList<MapRFileAce>();

			MapRFileAce ace = new MapRFileAce(MapRFileAce.AccessType.READFILE);
			ace.setBooleanExpression("p");
			faces.add(ace);

			ace = new MapRFileAce(MapRFileAce.AccessType.WRITEFILE);
			ace.setBooleanExpression("u:mapr|g:mapr|u:arjun");
			faces.add(ace);

			// Setting ACE for path with expressions built in previous step.

			((MapRFileSystem) fs).setAces(path, faces);
			System.out.println("\nAce for path:" + aceTestPathStr + " has been set successfully!");

			List<MapRFileAce> aceList = ((MapRFileSystem) fs).getAces(path);
			System.out.println("\nCurrent Ace values for path - " + aceTestPathStr);

			for (MapRFileAce aceValue : aceList) {

				System.out.println(
						"AccessType:" + aceValue.getAccessType().name() + " : " + aceValue.getBooleanExpression());
			}

			// Deleting ACE for path.
			((MapRFileSystem) fs).deleteAces(path);
			System.out.println("\nAce for path:" + aceTestPathStr + " has been deleted successfully!");

			aceList = ((MapRFileSystem) fs).getAces(path);
			System.out.println("\nCurrent Ace values for path - " + aceTestPathStr);

			for (MapRFileAce aceValue : aceList) {

				System.out.println(
						"AccessType:" + aceValue.getAccessType().name() + " : " + aceValue.getBooleanExpression());
			}

		} catch (IOException e) {
			System.out.println("Fs get failed(), Error" + e.getMessage());
		} catch (Exception e) {
			System.out.println("Set/Del Ace Failed, Error: " + e.getMessage());
		}
	}
}
