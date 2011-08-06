import it.sauronsoftware.ftp4j.FTPAbortedException;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferException;
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
				System.out.print(file);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}	
	}

	public void createFolder(String folder) {

		try {
			this.client.changeDirectory(folder);
			FTPFile[] list = this.client.list();

			for(FTPFile file : list) {
				System.out.print(file);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}



	public void upload(String destination, String local_file) {

		try {
			this.client.createDirectory(destination);
			this.client.changeDirectory(destination);
			this.client.upload(new java.io.File(local_file));

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
		} catch (FTPDataTransferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FTPAbortedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}