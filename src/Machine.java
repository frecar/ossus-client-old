import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Machine {

	String server_ip;
	String machine_id;
	String username;
	String password;
	String local_temp_folder;
	String os_system;
	Boolean is_busy;
	String mysql_dump;
	String downloads_client;
	String get_next_backup_time;
	String agent_folder;
	Boolean force_action;
	APIHandler apiHandler; 
	Log log;

	List<Schedule> schedules = new ArrayList<Schedule>();

	public Machine(Map<String, String> settings) throws ParseException {
		this.server_ip = settings.get("server_ip");

		this.machine_id = settings.get("machine_id");
		this.username = settings.get("username");
		this.password = settings.get("password");
		this.os_system = settings.get("os_system");
		this.downloads_client = settings.get("downloads_client");
		this.mysql_dump = settings.get("mysql_dump");
		this.agent_folder = settings.get("agent_folder");
		this.local_temp_folder = settings.get("local_temp_folder");

		apiHandler = new APIHandler(this.server_ip + "/api/", this.username, this.password);


		if(! this.local_temp_folder.endsWith(System.getProperty("file.separator"))) {
			this.local_temp_folder += System.getProperty("file.separator");
		}
		if(! this.agent_folder.endsWith(System.getProperty("file.separator"))) {
			this.agent_folder += System.getProperty("file.separator");
		}
		
		download_lib_file("libsigar-amd64-freebsd-6.so");
		download_lib_file("libsigar-amd64-linux.so");
		download_lib_file("libsigar-amd64-solaris.so");
		download_lib_file("libsigar-ia64-hpux-11.sl");
		download_lib_file("libsigar-ia64-linux.so");
		download_lib_file("libsigar-pa-hpux-11.sl");
		download_lib_file("libsigar-ppc64-aix-5.so");
		download_lib_file("libsigar-ppc64-linux.so");
		download_lib_file("libsigar-ppc-aix-5.so");
		download_lib_file("libsigar-ppc-linux.so");
		download_lib_file("libsigar-s390x-linux.so");
		download_lib_file("libsigar-sparc64-solaris.so");
		download_lib_file("libsigar-sparc-solaris.so");
		download_lib_file("libsigar-universal64-macosx.dylib");
		download_lib_file("libsigar-universal-macosx.dylib");
		download_lib_file("libsigar-x86-freebsd-5.so");
		download_lib_file("libsigar-x86-freebsd-6.so");
		download_lib_file("libsigar-x86-linux.so");
		download_lib_file("libsigar-x86-solaris.so");
		download_lib_file("libsigar-x86-freebsd-6.so");
		download_lib_file("sigar-amd64-winnt.dll");
		download_lib_file("sigar-x86-winnt.lib");
		download_lib_file("sigar-x86-winnt.dll");
		download_lib_file("sigar.jar");

		this.force_action = settings.get("force_action").equals("1");

		this.log = new Log(apiHandler, this);


		String version = "FocusBackup 0.1";
		this.log_info("Connecting to " + server_ip + ". Current version: " + version);

		List<JSONObject> obj = apiHandler.get_api_data("machines/"+this.machine_id);
		JSONObject data = obj.get(0);

		this.is_busy = (Boolean) data.get("is_busy");

		this.addSchedules((JSONArray) data.get("schedules"));

	}

	private void download_lib_file(String string) {

		java.io.BufferedInputStream in;
		try {
					
			File f = new File(this.agent_folder+"libs");
			f.mkdirs();
			
			File lib_file = new File(this.agent_folder+"libs"+System.getProperty("file.separator")+string);
			
			if(lib_file.exists()) {
				return;
			}
			
			in = new java.io.BufferedInputStream(new java.net.URL(this.downloads_client+"/"+string).openStream());

			java.io.FileOutputStream fos = new java.io.FileOutputStream(this.agent_folder+"libs"+System.getProperty("file.separator")+string);
			java.io.BufferedOutputStream bout = new BufferedOutputStream(fos,1024);
			byte data[] = new byte[1024];
			while(in.read(data,0,1024)>=0)
			{
				bout.write(data);
			}
			bout.close();
			in.close();
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}


public void log_info(String text) {
	this.log.log_info(text);	
}

public void log_error(String text) {
	this.log.log_error(text);	
}

public void log_warning(String text) {
	this.log.log_warning(text);	
}

private void addSchedules(JSONArray schedules) throws ParseException {

	for (Object o : schedules) {
		JSONObject obj = (JSONObject) o;
		Schedule schedule = new Schedule();
		schedule.setId(obj.get("id").toString());
		schedule.setName(obj.get("name").toString());
		schedule.setCurrent_version_in_loop(obj.get("current_version_in_loop").toString());
		schedule.setMachine(this);


		String next_backup_time_string = obj.get("get_next_backup_time").toString();
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd H:m:s");
		Date next_backup_time = (Date)formatter.parse(next_backup_time_string);
		schedule.set_next_backup_time(next_backup_time);

		JSONObject storage= ((JSONObject) obj.get("storage"));

		schedule.setStorage(new FTPStorage(this, (String)storage.get("host"), (String)storage.get("username"),(String) storage.get("password"), (String) storage.get("folder")));
		schedule.setUpload_path((String)storage.get("folder") + "/" +  obj.get("current_day_folder_path").toString());

		schedule.setRunning_backup(obj.get("running_backup").toString().equals("true"));
		schedule.setRunning_restore(obj.get("running_restore").toString().equals("true"));

		JSONArray folderBackups = ((JSONArray) obj.get("folder_backups"));
		JSONArray sqlBackups = ((JSONArray) obj.get("sql_backups"));

		for (Object folderBackupJson : folderBackups) {
			FolderBackup folderBackup = new FolderBackup();
			folderBackup.setId((String) ((JSONObject) folderBackupJson).get("id").toString());
			folderBackup.setPath((String) ((JSONObject) folderBackupJson).get("local_folder_path").toString());
			schedule.addFolderBackup(folderBackup);
		}

		for (Object sqlBackupJson : sqlBackups) {
			SQLBackup sqlBackup = new SQLBackup();
			sqlBackup.setId((String) ((JSONObject) sqlBackupJson).get("id").toString());
			sqlBackup.setHost((String) ((JSONObject) sqlBackupJson).get("host").toString());
			sqlBackup.setUsername((String) ((JSONObject) sqlBackupJson).get("username").toString());
			sqlBackup.setPassword((String) ((JSONObject) sqlBackupJson).get("password").toString());
			sqlBackup.setDatabase((String) ((JSONObject) sqlBackupJson).get("database").toString());
			sqlBackup.setType((String) ((JSONObject) sqlBackupJson).get("type").toString());
			sqlBackup.setPort((String) ((JSONObject) sqlBackupJson).get("port").toString());
			schedule.addSqlBackup(sqlBackup);
		}

		this.schedules.add(schedule);	

	}
}

public void runBackup() {

	if(is_busy && ! force_action) {
		this.log_info("Machine busy");
	}
	else {
		for (Schedule schedule : this.schedules) {
			if(new Date().after(schedule.get_next_backup_time())) {
				this.log_info("Running schedule " + schedule.getName());
				schedule.runBackup();	
			}
		}	
	}
}
}
