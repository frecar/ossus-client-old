package backupclient.agent;

import backupclient.commons.Machine;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class BackupJob {
    
    private final Machine machine;
    private final List<Schedule> schedules;
    
    public BackupJob(Machine machine) throws ParseException {
        this.machine = machine;
        schedules = new ArrayList<Schedule>();
        getSchedules();
    }


    private void getSchedules() {
        List<JSONObject> json_list = machine.apiHandler.get_api_data("machines/"+machine.machine_id+"/schedules/");
        if (json_list.isEmpty()) {
            machine.log.log_error("Could not get schedules");
            return;
        }

        try {
            addSchedules(json_list);
        } catch (ParseException e) {
            machine.log_error("Error getting schedules:\n" + e.toString());
        }
    }


    private void addSchedules(List<JSONObject> schedules) throws ParseException {
        for (Object o : schedules) {
            JSONObject obj = (JSONObject) o;
            Schedule schedule = new Schedule();
            schedule.setId(obj.get("id").toString());
            schedule.setName(obj.get("name").toString());
            schedule.setCurrent_version_in_loop("10");//obj.get("current_version_in_loop").toString());
            schedule.setMachine(machine);


            String next_backup_time_string = obj.get("get_next_backup_time").toString();

            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd H:m:s");
            next_backup_time_string = "2012-04-22 23:48:56";
            Date next_backup_time = formatter.parse(next_backup_time_string);

            schedule.set_next_backup_time(next_backup_time);

            JSONObject storage= ((JSONObject) obj.get("storage"));

            schedule.setStorage(new FTPStorage(machine, (String)storage.get("host"), (String)storage.get("username"),(String) storage.get("password"), (String) storage.get("folder")));
            schedule.setUpload_path(storage.get("folder") + "/" +  obj.get("current_day_folder_path").toString());

            schedule.setRunning_backup(obj.get("running_backup").toString().equals("true"));
            schedule.setRunning_restore(obj.get("running_restore").toString().equals("true"));

            JSONArray folderBackups = ((JSONArray) obj.get("folder_backups"));
            JSONArray sqlBackups = ((JSONArray) obj.get("sql_backups"));

            if (folderBackups != null) {
                for (Object folderBackupJson : folderBackups) {
                    FolderBackup folderBackup = new FolderBackup();
                    folderBackup.setId(((JSONObject) folderBackupJson).get("id").toString());
                    folderBackup.setPath(((JSONObject) folderBackupJson).get("local_folder_path").toString());
                    schedule.addFolderBackup(folderBackup);
                }
            } else machine.log_info("No folder backups...");

            if (sqlBackups != null) {
                for (Object sqlBackupJson : sqlBackups) {
                    SQLBackup sqlBackup = new SQLBackup();
                    sqlBackup.setId( ((JSONObject) sqlBackupJson).get("id").toString());
                    sqlBackup.setHost(((JSONObject) sqlBackupJson).get("host").toString());
                    sqlBackup.setUsername(((JSONObject) sqlBackupJson).get("username").toString());
                    sqlBackup.setPassword(((JSONObject) sqlBackupJson).get("password").toString());
                    sqlBackup.setDatabase(((JSONObject) sqlBackupJson).get("database").toString());
                    sqlBackup.setType(((JSONObject) sqlBackupJson).get("type").toString());
                    sqlBackup.setPort(((JSONObject) sqlBackupJson).get("port").toString());
                    schedule.addSqlBackup(sqlBackup);
                }
            } else machine.log_info("No sql backups");

            this.schedules.add(schedule);

        }
    }

    public void runBackup() {

        if(machine.is_busy && ! machine.force_action) {
            machine.log_info("Machine busy");
        }
        else {
            for (Schedule schedule : this.schedules) {
                if(new Date().after(schedule.get_next_backup_time())) {
                    machine.log_info("Running schedule " + schedule.getName());
                    schedule.runBackup();
                }
            }
        }
    }
    
}