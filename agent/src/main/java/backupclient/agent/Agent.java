package backupclient.agent;

import backupclient.commons.CrossProcessLock;
import backupclient.commons.Machine;
import backupclient.commons.XMLHandler;
import backupclient.install.Installer;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public class Agent {

	public static void main(String[]args) throws ParseException {

        /* try {
            new Installer().runInstall();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error running install");
        } */

          boolean update_lock = CrossProcessLock.instance.tryLock(0);
          if (!update_lock) {
              System.out.println("!update_lock");
              return; // TODO log this?
          }

          String settingsLocation = args.length > 0 ? args[0] : "settings.xml";
          Machine machine =  Machine.buildFromXmlSettings(new XMLHandler(settingsLocation));

          new Thread(new Updater(machine)).start();
          new BackupJob(machine).runBackup();

          MachineStats machinestats = new MachineStats(machine);
          machinestats.save();
	}
}
