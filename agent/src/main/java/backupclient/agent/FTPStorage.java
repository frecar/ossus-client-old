package backupclient.agent;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPFile;
import java.io.File;

public class FTPStorage {
	FTPClient client;
	String homeFolder;
	Machine machine;

	String host;
	String username;
	String password;

	public FTPStorage(Machine machine, String host, String username, String password, String folder) {
		this.host = host;
		this.username = username;
		this.password = password;
		this.homeFolder = folder;
		this.machine = machine;
		this.reconnect();	
	}

	public void reconnect() {
		try {			
			this.client = new FTPClient();
			this.client.connect(this.host);
			this.client.login(this.username, this.password);
			
		} catch (Exception e) {
			this.machine.log_error(e.getMessage());
		}
	}

	public void deleteFolder(String folder) {
		try {
			this.client.changeDirectory(this.homeFolder);
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
			this.client.changeDirectory(this.homeFolder);
		} catch (Exception e2) {
			this.machine.log_error(e2.getMessage());
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
					this.machine.log_error(e1.getMessage());
				}		
				this.machine.log_error(e.getMessage());
			}
		}
	}

	public void upload(String destination, String local_file, int restart) {
		try {
			this.createFolder(destination);
			this.client.changeDirectory(destination);

			
			File file = new java.io.File(local_file);
			MyTransferListener listener = new MyTransferListener(this.machine, this.client, file.length());
			
			if (restart>10) {
				this.machine.log_warning("Tried to upload 10 times, aborting");
				return;
			}
			else if(restart >= 1 && this.client.isResumeSupported()) {
				try {
					
					//Switch to binary mode..
					this.client.sendCustomCommand("type i");
					//this.client.sendCustomCommand("binary");
					//this.client.setType(FTPClient.TYPE_BINARY);
					
					Long uploaded_size = this.client.fileSize(destination+"/"+file.getName());
					
					this.machine.log_info("Resuming from byte number: " + uploaded_size);
					this.client.upload(file, uploaded_size, listener);
				}
				catch (Exception e) {
					this.machine.log_warning("Can not resume, restart upload: " + e.getMessage() );
					this.client.upload(file, listener);
				}
			}
			else
				this.client.upload(file, listener);

		} catch (Exception e) {

			e.getStackTrace();
			
			this.machine.log_error(e.getMessage());
			this.machine.log_warning("Restarting upload in 10 seconds");
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			
			this.reconnect();
			this.upload(destination, local_file, restart+=1);
		}
	}
}
