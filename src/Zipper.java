import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Zipper {

	Machine machine;
	
	static void zipDir(String zipFileName, String dir, Machine machine) throws Exception {
				
		File dirObj = new File(dir);
		try {
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName));
			addDir(dirObj, out, machine);
			out.close();	
		} catch(Exception e) {
			JFrame frame = new JFrame();
			JOptionPane.showMessageDialog(frame, e.getMessage() + ". Is settings.xml correct?");
		}
	}

	static void addDir(File dirObj, ZipOutputStream out, Machine machine) throws IOException {
		try {
			File[] files = dirObj.listFiles();
			byte[] tmpBuf = new byte[1024];
			for (int i = 0; i < files.length; i++) {
				try {
					if (files[i].isDirectory()) {
						
						if(machine.local_temp_folder.equals(files[i])) {
							continue;
						}
						
						addDir(files[i], out, machine);
						
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