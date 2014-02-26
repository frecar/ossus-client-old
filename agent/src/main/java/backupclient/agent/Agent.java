package backupclient.agent;

import backupclient.commons.CrossProcessLock;
import backupclient.commons.Machine;
import backupclient.install.Installer;

import java.io.IOException;
import java.text.ParseException;

public class Agent {

    public static void main(String[] args) throws ParseException {

        String settingsLocation = args.length > 0 ? args[0] : "settings.json";
        Machine machine = Machine.buildFromSettings(settingsLocation);

        if (machine.is_busy) {
            machine.log_warning("Machine busy, wait until next run!");
            return;
        }

        if (machine.run_install) {
            try {
                new Installer().runInstall();
            } catch (IOException e) {
                e.printStackTrace();
                machine.log_error("Error running install");
            }
        }

        new Thread(new Updater(machine)).start();
        new BackupJob(machine).runBackup();

        MachineStats machinestats = new MachineStats(machine);
        machinestats.save();
    }
}