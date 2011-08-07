import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPFile;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;

import java.io.IOException;

public class FTPStorage {
	FTPClient client = new FTPClient();

	public FTPStorage() {
		try {
			client.connect("backup.fncit.no");
			client.login("frecar", "76ahf6234a!!");
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FTPIllegalReplyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FTPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void deleteFolder(String folder) {
		try {
			this.client.changeDirectory(folder);
			
			FTPFile[] list = this.client.list();

			for(FTPFile file : list) {
								
				boolean isDirectory = file.getType() == 1;
				boolean isFile = file.getType() == 0;

				if(isDirectory) {
					this.deleteFolder(folder+file.getName());
					this.client.deleteDirectory(folder+file.getName());
				} else if(isFile) {
					this.client.deleteFile(file.getName());
				}
			}
		
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}

	public void createFolder(String folder) {

		try {
			this.client.changeDirectory("~/");
		} catch (Exception e2) {
			e2.printStackTrace();
		}	

		String[] path = folder.split("/");
		String mid_path = "";

		for(String p : path) {
			mid_path+=p+"/";
			
			try {
				this.client.changeDirectory(mid_path);	
			}
			catch(Exception e) {
				try {
					this.client.createDirectory(mid_path);
				} catch (Exception e1) {
					e1.printStackTrace();
				}			
			}
		}
	}

	public void upload(String destination, String local_file) {

		try {
			
			this.createFolder(destination);
			this.client.changeDirectory(destination);
			this.client.upload(new java.io.File(local_file));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}