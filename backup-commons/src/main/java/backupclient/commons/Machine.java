package backupclient.commons;

import backupclient.commons.APIHandler;
import backupclient.commons.Log;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Machine {

    public final String current_agent_version;
    public final String current_updater_version;
    public final String selected_updater_version;
    public final String selected_agent_version;
    public final boolean auto_update;
    
    
	public final String server_ip;
	public final String machine_id;
    //public final String get_next_backup_time;
    public final String username;
    public final String password;
    public String agent_folder;
    public String local_temp_folder;
    public final String os_system;
    public final boolean is_busy;
    public final String mysql_dump;
    public final String downloads_client;
    public final Boolean force_action;

    public final Log log;

    public final APIHandler apiHandler;

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

		apiHandler = new APIHandler(this.server_ip + "/api/", username, password);


		if(! this.local_temp_folder.endsWith(System.getProperty("file.separator"))) {
			this.local_temp_folder += System.getProperty("file.separator");
		}

		File f_temp = new File(this.local_temp_folder);
		f_temp.mkdirs();

		if(! this.agent_folder.endsWith(System.getProperty("file.separator"))) {
			this.agent_folder += System.getProperty("file.separator");
		}

		File f_agent = new File(this.agent_folder);
		f_agent.mkdirs();

		this.force_action = settings.get("force_action").equals("1");

		this.log = new Log(apiHandler, this.machine_id);

		String version = "FocusBackup 0.1";
		this.log_info("Connecting to " + server_ip + ". Current version: " + version);
		
		List<JSONObject> obj = apiHandler.get_api_data("machines/"+this.machine_id);
		JSONObject data = obj.get(0);

        this.current_agent_version = (String) data.get("current_agent_version");
        this.current_updater_version = (String) data.get("current_updater_version");
        this.selected_agent_version = (String) data.get("selected_agent_version");
        this.selected_updater_version = (String) data.get("selected_updater_version");
        this.auto_update = (Boolean) data.get("auto_version");
		this.is_busy = (Boolean) data.get("is_busy");
	}
    
    public static Machine buildFromXmlSettings(XMLHandler xmlHandler) {
        Map<String, String> settings = new HashMap<String, String>();

        settings.put("machine_id", xmlHandler.get_value("machine", "machine_id"));
        settings.put("server_ip", xmlHandler.get_value("machine", "server_ip"));
        settings.put("username", xmlHandler.get_value("machine", "username"));
        settings.put("password", xmlHandler.get_value("machine", "password"));
        settings.put("force_action", xmlHandler.get_value("machine", "force_action"));
        settings.put("local_temp_folder", xmlHandler.get_value("machine", "local_temp_folder"));
        settings.put("os_system", xmlHandler.get_value("machine", "os_system"));
        settings.put("mysql_dump", xmlHandler.get_value("machine", "mysql_dump"));
        settings.put("downloads_client", xmlHandler.get_value("machine", "downloads_client"));
        settings.put("agent_folder", xmlHandler.get_value("machine", "agent_folder"));
        
        try {
            return new Machine(settings);
        } catch (ParseException e) {
            e.printStackTrace(); // TODO use a real local logger, like util.Logger
            return null; // TODO dont return null here, throw it
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

    public String get_local_temp_folder() {
        return local_temp_folder;
    }
    
    public String get_agent_folder() {
        return agent_folder;
    }

}
