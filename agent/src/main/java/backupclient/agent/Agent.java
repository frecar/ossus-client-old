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


        boolean update_lock = CrossProcessLock.instance.tryLock(0);
        if (!update_lock) {
            machine.log_warning("Machine busy, update lock!");
            return; // TODO log this?
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