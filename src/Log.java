import java.util.HashMap;

public class Log {

	APIHandler apiHandler;
	Machine machine;

	public Log(APIHandler apiHandler, Machine machine) {
		this.apiHandler = apiHandler;
		this.machine = machine;
	}

	public void log_info(String text) {
		HashMap<String, String> map = new HashMap<String, String>();	
		map.put("machine_id", this.machine.machine_id);
		map.put("type", "info");
		map.put("text", text);		
		this.apiHandler.set_api_data("machinelogs/", map);		

	}

	public void log_error(String text) {
		HashMap<String, String> map = new HashMap<String, String>();	
		map.put("machine_id", this.machine.machine_id);
		map.put("type", "error");
		map.put("text", text);		
		this.apiHandler.set_api_data("machinelogs/", map);		
	}

	public void log_warning(String text) {
		HashMap<String, String> map = new HashMap<String, String>();	
		map.put("machine_id", this.machine.machine_id);
		map.put("type", "warning");
		map.put("text", text);		
		this.apiHandler.set_api_data("machinelogs/", map);		
	}

}