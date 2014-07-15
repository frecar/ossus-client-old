package backupclient.agent;

import backupclient.commons.Machine;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

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
        List<JSONObject> json_list = machine.apiHandler.get_api_data("machines/" + machine.id + "/schedules/");
        JSONArray jsonArray = (JSONArray) json_list.get(0).get("schedules");
        List<JSONObject> schedules = new ArrayList<JSONObject>();

        if (json_list.isEmpty()) {
            machine.log.log_error("Could not get schedules");
            return;
        }

        try {

            for (Object jsonObject : jsonArray) {
                schedules.add((JSONObject) jsonObject);
            }

            addSchedules(schedules);
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
            schedule.setCurrent_version_in_loop(obj.get("current_version_in_loop").toString());
            schedule.setVersionsCount(obj.get("versions_count").toString());
            schedule.setMachine(machine);

            String next_backup_time_string = obj.get("get_next_backup_time").toString();

            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd H:m:s");
            Date next_backup_time = formatter.parse(next_backup_time_string);

            schedule.set_next_backup_time(next_backup_time);

            JSONObject storage = ((JSONObject) obj.get("storage"));
            schedule.setStorage(new FTPStorage(machine, (String) storage.get("host"), (String) storage.get("username"), (String) storage.get("password"), (String) storage.get("folder")));
            schedule.setUpload_path(storage.get("folder") + "/" + storage.get("current_day_folder_path"));

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
            } else machine.log_info("No folders to backup");

            if (sqlBackups != null) {
                for (Object sqlBackupJson : sqlBackups) {
                    SQLBackup sqlBackup = new SQLBackup();
                    sqlBackup.setId(((JSONObject) sqlBackupJson).get("id").toString());
                    sqlBackup.setHost(((JSONObject) sqlBackupJson).get("host").toString());
                    sqlBackup.setUsername(((JSONObject) sqlBackupJson).get("username").toString());
                    sqlBackup.setPassword(((JSONObject) sqlBackupJson).get("password").toString());
                    sqlBackup.setDatabase(((JSONObject) sqlBackupJson).get("database").toString());
                    sqlBackup.setType(((JSONObject) sqlBackupJson).get("type").toString());
                    sqlBackup.setPort(((JSONObject) sqlBackupJson).get("port").toString());
                    schedule.addSqlBackup(sqlBackup);
                }
            } else machine.log_info("No sql databases to backup");

            this.schedules.add(schedule);
        }
    }

    public void runBackup() {
        for (Schedule schedule : this.schedules) {
            if (new Date().after(schedule.get_next_backup_time())) {
                machine.log_info("Running schedule " + schedule.getName());

                if(!schedule.getRunning_backup()) {

                    schedule.setRunning_backup(true);
                    schedule.save();

                    try {
                        schedule.runBackup();
                    } catch (Exception e) {
                        this.machine.log_error(e.getMessage());
                    }

                    schedule.setRunning_backup(false);
                    schedule.save();

                }
            }
        }
    }
}