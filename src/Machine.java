import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Machine {

	String server_ip;
	String machine_id;
	String username;
	String password;
	String local_temp_folder;
	String os_system;
	String mysql_dump;
	String get_next_backup_time;
	Boolean force_action;
	Log log;
	
	List<Schedule> schedules = new ArrayList<Schedule>();

	public Machine(Map<String, String> settings) {
		this.server_ip = settings.get("server_ip");

		APIHandler apiHandler = new APIHandler(this.server_ip + "/api/");

		this.machine_id = settings.get("machine_id");
		this.username = settings.get("username");
		this.password = settings.get("password");
		this.os_system = settings.get("os_system");
		this.mysql_dump = settings.get("mysql_dump");
		this.local_temp_folder = settings.get("local_temp_folder");

		if(! this.local_temp_folder.endsWith(System.getProperty("file.separator"))) {
			this.local_temp_folder += System.getProperty("file.separator");
		}

		this.force_action = settings.get("force_action").equals("1");

		log = new Log(apiHandler, this);
		
		List<JSONObject> obj = apiHandler.get_api_data("machines/"+this.machine_id);
		JSONObject data = obj.get(0);

		this.addSchedules((JSONArray) data.get("schedules"));

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
	
	private void addSchedules(JSONArray schedules) {

		for (Object o : schedules) {
			JSONObject obj = (JSONObject) o;
			Schedule schedule = new Schedule();
			schedule.setId(obj.get("id").toString());
			schedule.setName(obj.get("name").toString());
			schedule.setMachine(this);

			JSONObject storage= ((JSONObject) obj.get("storage"));

			schedule.setStorage(new FTPStorage(this, (String)storage.get("host"), (String)storage.get("username"),(String) storage.get("password"), (String) storage.get("folder")));
			schedule.setUpload_path((String)storage.get("folder") + "/" +  obj.get("current_day_folder_path").toString());
			
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
				schedule.addSqlBackup(sqlBackup);
			}
			
			this.schedules.add(schedule);
			
		}
	}

	public void runBackup() {
		this.log_info("Running backup");
		for (Schedule schedule : this.schedules) {
			this.log_info("Running schedule " + schedule.getName());
			schedule.runBackup();
		}
	}
}
