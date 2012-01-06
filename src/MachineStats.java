import java.util.HashMap;
import java.util.Map;
import org.hyperic.sigar.Cpu;
import org.hyperic.sigar.CpuInfo;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.DiskUsage;
import org.hyperic.sigar.FileSystem;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.NetInterfaceConfig;
import org.hyperic.sigar.ProcCpu;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.cmd.Ps;

public class MachineStats {
	
	Machine machine;
	float loadAverage;
	float cpuSystem;
	float cpuUser;
	float cpuStolen;
	float cpuTotal;

	long memFree;
	long memUsed;

	double[] s;

	Sigar sigar;

	public MachineStats(Machine machine) {

		this.sigar = new Sigar();
		this.machine = machine;

		CpuInfo[] cpuinfo = null;
		Mem meminfo = null;
		NetInterfaceConfig netinterface = null;

		Ps ps = null;

		try {
			meminfo = sigar.getMem();
			cpuinfo = sigar.getCpuInfoList();

			s = sigar.getLoadAverage();

			long[] proclist = sigar.getProcList();

			Cpu cpu = sigar.getCpu();

			CpuPerc cpuperc = sigar.getCpuPerc();

			cpuSystem = (float) (cpuperc.getSys()*100);

			cpuUser = (float) (cpuperc.getUser()*100);

			memFree = meminfo.getActualFree();
			memUsed = meminfo.getActualUsed();
			
		} catch(Exception e) {}

	}
	public void save() {

		Map<String, String> map = new HashMap<String, String>();

		map.put("machine_id", this.machine.machine_id);
		
		map.put("cpu_system", ""+Math.round(cpuSystem*100)/100.0);
		map.put("cpu_user", ""+Math.round(cpuUser*100)/100.0);
		map.put("cpu_stolen","0.5");
		
		map.put("mem_free",""+memFree);
		map.put("mem_used",""+memUsed);
		
		System.out.println(s);
		
		map.put("load_average", ""+Math.round(s[0]*100)/100.0);

		this.machine.apiHandler.set_api_data("machinestats/", map);

		System.out.println("load " + this.s[0]);

	}
}

