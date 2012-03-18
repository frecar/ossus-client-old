package backupclient.agent;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class Schedule {

	private String id;
	private String name;
	private Boolean running_backup;
	private Boolean running_restore;
	private String current_version_in_loop;
	private String upload_path;
	private Date get_next_backup_time;

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

	public void setCurrent_version_in_loop(String string) {
		this.current_version_in_loop = string;
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

	private int find_next_current_version_in_loop() {

		if(Integer.parseInt(this.current_version_in_loop) >= 10) {
			return 1;
		}

		return Integer.parseInt(this.current_version_in_loop) + 1;

	}

	public void save() {
		HashMap<String, String> map = new HashMap<String, String>();	
		map.put("name", this.name);
		map.put("running_backup", "" + this.running_backup);
		map.put("running_restore", "" + this.running_restore);
		map.put("current_version_in_loop", ""+this.find_next_current_version_in_loop());
		this.machine.apiHandler.set_api_data("schedules/" + this.id + "/", map);	
	}

	private String getDateTime() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		return dateFormat.format(date);
	}
	
	public void createBackupEntry(String start, String end) {
		HashMap<String, String> map = new HashMap<String, String>();	
		map.put("machine_id", this.machine.machine_id);
		map.put("schedule_id", "" + this.id);
		map.put("time_started", start);
		map.put("time_ended", end);
			
		this.machine.apiHandler.set_api_data("backups/", map);	
		
	}
	
	public void runBackup() {

		String start = this.getDateTime();
		
		this.setRunning_backup(true);
		this.save();

		FTPStorage ftpStorage = this.storage;

		try {
			ftpStorage.client.changeDirectory(this.upload_path);			
			ftpStorage.deleteFolder(this.upload_path);
		} catch (Exception e) {

			this.machine.log_error("FTP | " + e.getMessage());

			try {
				ftpStorage.client.createDirectory(this.upload_path);			
			}
			catch (Exception es) {
				this.machine.log_error("FTP | " + es.getMessage());
			}
		}

		String file_separator = System.getProperty("file.separator");

		for (FolderBackup folderBackup : this.getFolderBackups()) {

			String filename_zip = folderBackup.getPath().replaceAll("\\" + file_separator,"_").replaceAll("\\:","_").replaceAll(" ","-")+".zip";

			try {
				this.machine.log_info("Zipping " + this.machine.local_temp_folder + filename_zip);
				Zipper.zipDir(this.machine.local_temp_folder + filename_zip, folderBackup.getPath(), this.machine);

				File file = new java.io.File(this.machine.local_temp_folder + filename_zip);

				this.machine.log_info("Done zipping " + (file.length()/1024/1024) +" MB");
				this.machine.log_info("Begun uploding " + this.machine.local_temp_folder + filename_zip + " to "+ this.upload_path + " server: " + ftpStorage.client.getHost());
				ftpStorage.upload(this.upload_path, this.machine.local_temp_folder + filename_zip, 0);
				this.machine.log_info("Upload of " + filename_zip + " done");

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		for (SQLBackup sqlBackup : this.getSqlBackups()) {
			this.machine.log_info("Performing " + sqlBackup.getType()+" backup of " + sqlBackup.getDatabase() + " at " + sqlBackup.getHost());
			String folder_zip = this.machine.local_temp_folder + sqlBackup.getDatabase() + file_separator;
			File f = new File(folder_zip);

			try{
				f.mkdirs();
				String filename_zip;
				if(sqlBackup.getType().equals("mysql")) {
					filename_zip = folder_zip + sqlBackup.getDatabase() + ".sql";
					String executeCmd = "";
					executeCmd =  this.machine.mysql_dump + " --user='" + sqlBackup.getUsername() + "' --host='" + sqlBackup.getHost()+ "' --password='" + sqlBackup.getPassword() + "' "+sqlBackup.getDatabase() + " > " + filename_zip;
					this.execShellCmd(executeCmd); 					
				}
				else {
					filename_zip = folder_zip + sqlBackup.getDatabase() + ".bak";
					Connection conn;	
					Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");	
					conn = DriverManager.getConnection("jdbc:sqlserver://" + sqlBackup.getHost() + "; portNumber=" + sqlBackup.getPort() +"; databaseName="+sqlBackup.getDatabase(), sqlBackup.getUsername(), sqlBackup.getPassword());
					conn.setAutoCommit(true);					
					Statement select = conn.createStatement();
					select.executeQuery("BACKUP DATABASE " + sqlBackup.getDatabase() + " TO DISK='" + filename_zip+"'");
					conn.close();					
				}

				String filename_zip_name = filename_zip.replaceAll(file_separator,"_")+".zip";
				Zipper.zipDir(this.machine.local_temp_folder+filename_zip_name, folder_zip, machine);
				ftpStorage.upload(this.upload_path,this.machine.local_temp_folder+filename_zip_name, 0);	
			}
			catch(Exception e)
			{
				this.machine.log_error(e.getMessage());
			}
		}

		this.setRunning_backup(false);
		this.save();
		
		this.createBackupEntry(start, this.getDateTime());
		
	}

	public void execShellCmd(String cmd) {
		try {
			Runtime runtime = Runtime.getRuntime();
			Process process = runtime.exec(new String[] { "/bin/bash", "-c", cmd });
			//int exitValue = process.waitFor();
			BufferedReader buf = new BufferedReader(new InputStreamReader(process.getInputStream()));
			while ((buf.readLine()) != null) {
			}
		} catch (Exception e) {
			this.machine.log_error(e.getMessage());
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

	public Date get_next_backup_time() {
		return get_next_backup_time;
	}

	public void set_next_backup_time(Date get_next_backup_time) {
		this.get_next_backup_time = get_next_backup_time;
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
	private String port;
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

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}



}