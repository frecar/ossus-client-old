package backupclient.agent;

import backupclient.commons.APIHandler;
import backupclient.commons.GenericUpdater;
import backupclient.commons.Log;
import backupclient.commons.Machine;
import org.json.simple.JSONObject;

import java.util.List;

public class Updater extends GenericUpdater {

    private Machine machine;
    private APIHandler api_handler;
    private Log log;

    public Updater(Machine machine) {
        super(machine);
    }


    @Override
    protected String current_version() {
        return machine.current_updater_version;
    }

    @Override
    protected String selected_version() {
        return machine.selected_updater_version;
    }

    @Override
    protected String version_url() {
        return (machine.auto_update) ?
                "current_updater/" : selected_version();
    }



}
