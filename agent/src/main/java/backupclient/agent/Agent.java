package backupclient.agent;

import backupclient.commons.CrossProcessLock;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public class Agent {
	String name;
	Map<String, String> settings;
	
	public Agent() {}
	
	public static void main(String[]args) throws ParseException {

        System.out.println("A new version!");
        if (1==1) return;
        boolean update_lock = CrossProcessLock.instance.tryLock(0);
        if (!update_lock) return; // TODO log this?
        
		String settingsLocation = "settings.xml";
		try {
			settingsLocation = args[0];
		}
		catch(Exception e) {}
		
		Agent agent = new Agent();
		agent.buildSettings(settingsLocation);
				
		Machine machine = new Machine(agent.settings);
        if (update_lock) new Thread(new Updater(machine)).start();
		machine.runBackup();

		MachineStats machinestats = new MachineStats(machine);
		machinestats.save();
		
	}
	
	
	public void buildSettings(String location) {
		
		XMLHandler xmlHandler = new XMLHandler(location);
		
		settings = new HashMap<String, String>();
		
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
		
	}	
}
