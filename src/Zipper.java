import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Zipper {

	static void zipDir(String zipFileName, String dir) throws Exception {
		File dirObj = new File(dir);
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName));
		addDir(dirObj, out);
		out.close();
	}

	static void addDir(File dirObj, ZipOutputStream out) throws IOException {
		try {
			File[] files = dirObj.listFiles();
			byte[] tmpBuf = new byte[1024];
			for (int i = 0; i < files.length; i++) {
				try {
					if (files[i].isDirectory()) {
						addDir(files[i], out);
						continue;
					}
					FileInputStream in = new FileInputStream(files[i].getAbsolutePath());
					out.putNextEntry(new ZipEntry(files[i].getAbsolutePath().replace(File.separatorChar,'/')));
					int len;
					while ((len = in.read(tmpBuf)) > 0) {
						try {

							out.write(tmpBuf, 0, len);
						}
						catch(Exception e) {
							System.out.println(e.getMessage());
						}
					}	
					out.closeEntry();
					in.close();
				}
				catch(Exception e) {
					System.out.println(e.getMessage());
				}
			}
		}
		catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}
}