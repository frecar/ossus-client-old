package backupclient.commons;

import java.util.HashMap;

public class Log {

	APIHandler apiHandler;
	String machine_id;

	public Log(APIHandler apiHandler, String machine_id) {
		this.apiHandler = apiHandler;
		this.machine_id = machine_id;
	}

	public void log_info(String text) {
		HashMap<String, String> map = new HashMap<String, String>();	
		map.put("machine_id", machine_id);
		map.put("type", "info");
		map.put("text", text);		
		System.out.println(text);
		this.apiHandler.set_api_data("machinelogs/", map);		
	}

	public void log_error(String text) {
		HashMap<String, String> map = new HashMap<String, String>();	
		map.put("machine_id", machine_id);
		map.put("type", "error");
		map.put("text", text);		
		System.out.println(text);
	    this.apiHandler.set_api_data("machinelogs/", map);
	}

	public void log_warning(String text) {
		HashMap<String, String> map = new HashMap<String, String>();	
		map.put("machine_id", machine_id);
		map.put("type", "warning");
		map.put("text", text);		
		System.out.println(text);
	this.apiHandler.set_api_data("machinelogs/", map);		
	}

}