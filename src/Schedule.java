import java.util.ArrayList;
import java.util.List;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.sql.*;

public class Schedule {

	private String id;
	private String name;
	private Boolean running_backup;
	private Boolean running_restore;
	private Integer current_version_in_loop;
	private String upload_path;

	private FTPStorage storage;
	private Machine machine;

	private List<FolderBackup> folderBackups = new ArrayList<FolderBackup>();
	private List<SQLBackup> sqlBackups = new ArrayList<SQLBackup>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getRunning_backup() {
		return running_backup;
	}

	public void setRunning_backup(Boolean running_backup) {
		this.running_backup = running_backup;
	}

	public Boolean getRunning_restore() {
		return running_restore;
	}

	public void setRunning_restore(Boolean running_restore) {
		this.running_restore = running_restore;
	}

	public Integer getCurrent_version_in_loop() {
		return current_version_in_loop;
	}

	public void setCurrent_version_in_loop(Integer current_version_in_loop) {
		this.current_version_in_loop = current_version_in_loop;
	}

	public String getUpload_path() {
		return upload_path;
	}

	public void setUpload_path(String upload_path) {
		this.upload_path = upload_path;
	}

	public FTPStorage getStorage() {
		return storage;
	}

	public void setStorage(FTPStorage storage) {
		this.storage = storage;
	}

	public Machine getMachine() {
		return machine;
	}

	public void setMachine(Machine machine) {
		this.machine = machine;
	}

	public void runBackup() {

		FTPStorage ftpStorage = new FTPStorage();


		try {
			ftpStorage.client.changeDirectory(this.upload_path);			
			ftpStorage.deleteFolder(this.upload_path);

		} catch (Exception e) {

			try {
				ftpStorage.client.createDirectory(this.upload_path);			
			}
			catch (Exception es) {
			}
		}

		String file_separator = System.getProperty("file.separator");

		for (FolderBackup folderBackup : this.getFolderBackups()) {

			String filename_zip = folderBackup.getPath().replaceAll(file_separator,"_")+".zip";

			try {
				//Zipper.zipDir(this.machine.local_temp_folder + filename_zip, folderBackup.getPath());				
				//ftpStorage.upload(this.upload_path, this.machine.local_temp_folder + filename_zip);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}


		for (SQLBackup sqlBackup : this.getSqlBackups()) {

			String folder_zip = this.machine.local_temp_folder + sqlBackup.getDatabase() + file_separator;

			File f = new File(folder_zip);

			try{
				f.mkdirs();

				String filename_zip = folder_zip + sqlBackup.getDatabase() + ".sql";

				if(sqlBackup.getType().equals("mysql")) {
					String executeCmd = "";
					executeCmd = this.machine.mysql_dump + " --user='" + sqlBackup.getUsername() + "' --host='" + sqlBackup.getHost()+ "' --password='" + sqlBackup.getPassword() + "' "+sqlBackup.getDatabase()+"> "+filename_zip;
					this.execShellCmd(executeCmd); 
				}
				else {

					
				}
				
				String filename_zip_name = filename_zip.replaceAll(file_separator,"_")+".zip";
				Zipper.zipDir(this.machine.local_temp_folder+filename_zip_name, folder_zip);
				ftpStorage.upload(this.upload_path,filename_zip_name);	
				
			}
			catch(Exception d)
			{
				System.out.println(d.getCause());
			}
		}
	}

	public void execShellCmd(String cmd) {
		try {
			Runtime runtime = Runtime.getRuntime();
			Process process = runtime.exec(new String[] { "/bin/bash", "-c", cmd });
			int exitValue = process.waitFor();
			BufferedReader buf = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = "";
			while ((line = buf.readLine()) != null) {
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}


	public List<FolderBackup> getFolderBackups() {
		return folderBackups;
	}

	public void addFolderBackup(FolderBackup folderBackup) {
		this.folderBackups.add(folderBackup);
	}

	public List<SQLBackup> getSqlBackups() {
		return sqlBackups;
	}

	public void addSqlBackup(SQLBackup sqlBackup) {
		this.sqlBackups.add(sqlBackup);
	}
}

class FolderBackup {
	private String id;
	private String path;
	private Schedule schedule;

	public Schedule getSchedule() {
		return this.schedule;
	}

	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
}

class SQLBackup {
	private String id;
	private String database;
	private String username;
	private String password;
	private String host;
	private String type;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}



}