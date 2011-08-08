
public class Log {
	
    APIHandler apiHandler;
    Machine machine;
	
	public Log(APIHandler apiHandler, Machine machine) {
		this.apiHandler = apiHandler;
		this.machine = machine;
	}
	
	public void log_info(String text) {
		this.apiHandler.set_api_data("machinelogs/");		
	}

}
