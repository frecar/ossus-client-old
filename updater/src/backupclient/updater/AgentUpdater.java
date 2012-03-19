package backupclient.updater;

import backupclient.commons.CrossProcessLock;
import backupclient.commons.Machine;
import backupclient.commons.GenericUpdater;
import backupclient.commons.XMLHandler;

public class AgentUpdater extends GenericUpdater {

    
    public AgentUpdater(Machine machine) {
        super(machine);
    }

    @Override
    protected String current_version() {
        return machine.current_agent_version;
    }

    @Override
    protected String selected_version() {
        return machine.selected_agent_version;
    }

    @Override
    protected String version_url() {
        return (machine.auto_update) ?
                "current_agent/" : selected_version();
    }

    public static void main(String... args) {

        boolean lock = CrossProcessLock.instance.tryLock(0);
        if (!lock) return; // TODO log?

        String settingsLocation = "settings.xml";
        Machine machine = Machine.buildFromXmlSettings(new XMLHandler(settingsLocation));
        new AgentUpdater(machine).run();
    }
}
