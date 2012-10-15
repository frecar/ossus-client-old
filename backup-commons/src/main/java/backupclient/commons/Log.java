package backupclient.commons;

import java.util.HashMap;

public class Log {

	APIHandler apiHandler;
	String id;

    private static enum LogLevel {
        INFO("info"), ERROR("error"), WARNING("warning");
        String str;
        LogLevel(String str) {
            this.str = str;
        }
    }

	public Log(APIHandler apiHandler, String id) {
		this.apiHandler = apiHandler;
		this.id = id;
	}

    private void log_msg(String text, LogLevel level) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("id", ""+ id);
        map.put("type", ""+level.str);
        map.put("text", ""+text);
		this.apiHandler.set_api_data("machines/"+ id +"/create_log/", map);
    }

	public void log_info(String text) {
        log_msg(text, LogLevel.INFO);
	}

	public void log_error(String text) {
        log_msg(text, LogLevel.ERROR);
	}

	public void log_warning(String text) {
        log_msg(text, LogLevel.WARNING);
    }
}