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

    List<Schedule> schedules = new ArrayList<Schedule>();

    public Machine(Map<String, String> settings) {


        APIHandler apiHandler = new APIHandler("http://backup.fncit.no/api/");

        this.server_ip = settings.get("server_ip");
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


        List<JSONObject> obj = apiHandler.get_api_data("machines/"+this.machine_id);
        JSONObject data = obj.get(0);

        this.addSchedules((JSONArray) data.get("schedules"));
    }

    private void addSchedules(JSONArray schedules) {

        for (Object o : schedules) {
            JSONObject obj = (JSONObject) o;
            Schedule schedule = new Schedule();
            schedule.setId(obj.get("id").toString());
            schedule.setName(obj.get("name").toString());
            schedule.setUpload_path("~/backup/" + obj.get("current_day_folder_path").toString());
            schedule.setMachine(this);

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
        for (Schedule schedule : this.schedules) {
            schedule.runBackup();
        }
    }
}