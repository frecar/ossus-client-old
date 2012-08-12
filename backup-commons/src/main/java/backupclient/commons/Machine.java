package backupclient.commons;

import org.json.simple.JSONObject;
import java.io.File;
import java.text.ParseException;
import java.util.*;

public class Machine {

    private Version current_agent_version;
    private Version current_updater_version;
    public final Version selected_updater_version;
    public final Version selected_agent_version;
    public final boolean auto_update;
    
    
	public final String server_ip;
	public final String machine_id;
    //public final String get_next_backup_time;
    public final String api_user;
    public final String api_token;
    public String agent_folder;
    public String local_temp_folder;
    public final String os_system;
    public final boolean is_busy;
    public final String mysql_dump;
    public final String downloads_client;
    public final Boolean force_action;
    public final Boolean run_install;

    public final Log log;

    public final APIHandler apiHandler;

	public Machine(Map<String, String> settings) throws ParseException {
		this.server_ip = settings.get("server_ip");
		this.machine_id = settings.get("machine_id");
        this.api_user = settings.get("api_user");
        this.api_token = settings.get("api_token");
		this.os_system = settings.get("os_system");
		this.downloads_client = settings.get("downloads_client");
		this.mysql_dump = settings.get("mysql_dump");
		this.agent_folder = settings.get("agent_folder");
		this.local_temp_folder = settings.get("local_temp_folder");

		apiHandler = new APIHandler(this.server_ip + "/api/", api_user, api_token);

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

		this.log_info("Connecting to " + server_ip);
		
		List<JSONObject> obj = apiHandler.get_api_data("machines/"+this.machine_id);
		JSONObject data = (JSONObject)obj.get(0).get("machine");

        this.current_agent_version = Version.buildFromJson((JSONObject)  data.get("current_agent_version"), this); //(String) ((JSONObject) data.get("current_agent_version")).get("name");
        this.selected_agent_version = Version.buildFromJson((JSONObject)  data.get("selected_agent_version"), this);
        this.current_updater_version = Version.buildFromJson((JSONObject)  data.get("current_updater_version"), this);
        this.selected_updater_version = Version.buildFromJson((JSONObject)  data.get("selected_updater_version"), this);

        this.auto_update = (Boolean) data.get("auto_update");
		this.is_busy = (Boolean) data.get("is_busy");
		this.run_install = (Boolean) data.get("run_install");

        log_info("Current agent version: FocusBackup " + current_agent_version);
        log_info("Current updater version: FocusBackup " + current_updater_version);
	}
    
    public static Machine buildFromXmlSettings(XMLHandler xmlHandler) {
        Map<String, String> settings = new HashMap<String, String>();

        settings.put("machine_id", xmlHandler.get_value("machine", "machine_id"));
        settings.put("server_ip", xmlHandler.get_value("machine", "server_ip"));
        settings.put("api_user", xmlHandler.get_value("machine", "api_user"));
        settings.put("api_token", xmlHandler.get_value("machine", "api_token"));
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


    public Version get_current_agent_version() {
        return current_agent_version;
    }

    public Version get_current_updater_version() {
        return current_updater_version;
    }

    public void set_current_agent_version(Version current_agent_version) {
        this.current_agent_version = current_agent_version;
        final String agent_url = "machines/" + machine_id + "/set_agent_version/" + current_agent_version.id;
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiHandler.get_api_data(agent_url);
            }
        }).start();
    }

    public void set_current_updater_version(Version current_updater_version) {
        this.current_updater_version = current_updater_version;
        final String updater_url = "machines/" + machine_id + "/set_updater_version/" + current_updater_version.id;
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiHandler.get_api_data(updater_url);
            }
        }).start();
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
