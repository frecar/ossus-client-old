
//import org.hyperic.sigar.Cpu;
//import org.hyperic.sigar.CpuInfo;
//import org.hyperic.sigar.CpuPerc;
//import org.hyperic.sigar.DiskUsage;
//import org.hyperic.sigar.FileSystem;
//import org.hyperic.sigar.Mem;
//import org.hyperic.sigar.NetInterfaceConfig;
//import org.hyperic.sigar.ProcCpu;
//import org.hyperic.sigar.Sigar;
//import org.hyperic.sigar.SigarException;
//import org.hyperic.sigar.cmd.Ps;

public class MachineStats {

	//private static Sigar sigar;

	Machine machine;
	float loadAverage;
	float cpuSystem;
	float cpuUser;
	float cpuStolen;
	float cpuTotal;

	float memFree;
	float memUsed;



	public MachineStats(Machine machine) {
//
//		this.sigar = new Sigar();
//		this.machine = machine;
//
//		System.out.println("************************************");
//		System.out.println("*** Informations about the CPUs: ***");
//		System.out.println("************************************\n");
//
//		CpuInfo[] cpuinfo = null;
//		Mem meminfo = null;
//		NetInterfaceConfig netinterface = null;
//
//		Ps ps = null;
//
//		try {
//			meminfo = sigar.getMem();
//			cpuinfo = sigar.getCpuInfoList();
//
//			double[] s = sigar.getLoadAverage();
//
//			long[] proclist = sigar.getProcList();
//
//			Cpu cpu = sigar.getCpu();
//
//			CpuPerc cpuperc = sigar.getCpuPerc();
//
//			System.out.println(cpuperc.getSys()*100);
//			System.out.println(cpuperc.getUser()*100);
//			System.out.println(cpuperc.getIdle()*100);
//
//
//			System.out.println(meminfo.getFree());
//			System.out.println(meminfo.getUsed());
//			System.out.println(meminfo.getActualUsed());
//
//			long[] procList = sigar.getProcList();
//
//			for(int k=0; k < proclist.length; k++) {
//
//				try {
//
//					//					ProcCpu proc = new ProcCpu();
//					//
//					//					proc.gather(sigar, proclist[k]);
//					//				
//					//					System.out.println(Ps.getInfo(sigar, proclist[k]));
//					//					
//					//					System.out.println(proc.getSys());
//					//					System.out.println(proc.getUser());
//					//					System.out.println(proc.getTotal());
//					//					
//					//					System.out.println(((float)proc.getSys()+(float)proc.getUser()) / (float)proc.getTotal());
//					//					
//					//					System.out.println("-----");
//					//					System.out.println(proclist[k]);
//					//					System.out.println(sigar.getProcCpu(proclist[k]));
//					//					System.out.println(sigar.getProcState(proclist[k]));
//					//					
//
//					System.out.println(proclist[k]);
//					System.out.println(sigar.getProcMem(proclist[k]).getResident());
//
//
//				} catch(Exception e) {
//					System.out.println(e.getMessage());
//				}
//
//			}
//
//
//		} catch (SigarException se) {
//			se.printStackTrace();
//		}

	}



	public void save() {
		// TODO Auto-generated method stub
		
	}




}
