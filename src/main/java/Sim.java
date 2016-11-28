import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Random;

public class Sim {
    // # of clock cycles per timer interrupt
    private static int CLOCK_PER_TIMER = 100;

    // mininum # of cycles for an I/O event
    private static int CLOCK_IO_MIN = 50;

    // standard deviation of I/O times
    private static int CLOCK_IO_DEV = 100;

    // simulated memory (64K)
    private static final int MEM_SIZE = 65536;
    private static int[] memory = new int[MEM_SIZE];

    // next available address for a new program
    private static int loadAddr = 0;

    // next available process ID
    private static int nextProcId = 0;

    private static Random rng = new Random();

    private static final int INST_COMP  = 1;    // computation
    private static final int INST_BLOCK = 2;    // blocking (input/output)
    private static final int INST_EXIT  = 3;    // exit program
    private static final int INST_BR    = 128;  // Branch (128-228) with jump percentage

    // state when this instruction finishes
    private static final int STAT_GO    = 1;    // able to continue running
    private static final int STAT_BLOCK = 2;    // blocking on I/O
    private static final int STAT_EXIT  = 3;    // process is finished
    private static final int STAT_ERROR = 4;    // run-time error found

    // running process instruction pointer
    private static int CPU_instructionPointer;

    // running process base address
    private static int CPU_baseAddr;

    // running process address size
    private static int CPU_boundSize;

    /**
     * Fair share scheduler.
     *
     * Handles process queuing operations.
     */
    private static FairShareScheduler scheduler = new FairShareScheduler();

    public static void main(String[] args) {
        SchedulerCliConfigurationParser argumentParser = new SchedulerCliConfigurationParser();
        SchedulerProcessConfiguration config = argumentParser.parseConfigurationFromArgs(args);

        // clock cycles before next timer interrupt
        int timer_left = CLOCK_PER_TIMER;

        // load all the programs
        for(HashMap map : config.getFiles()) {
            @SuppressWarnings("unchecked")
            HashMap.Entry<String, Integer> entry = (HashMap.Entry<String, Integer>) map.entrySet().iterator().next();
            loadProgram(entry, config);
        }

        ScheduledProcess curProc = loadNextProcess();

        // @todo keep going as long as we have a process around
        while (scheduler.hasProcesses()){
            // what next?
            if(curProc != null) {
                // let it run for one cycle
                int status = runOneInst();

                //Update the utilization for this process and its group.
                scheduler.updateProcessUtilization(curProc);

                switch (status) {
                    case STAT_GO:   // able to continue running
                        break;

                    case STAT_BLOCK:                    // blocking on I/O
                        int delay = calcBlockWait();    // how long will this I/O take?

                        System.out.println("Process " + curProc.getId() + " blocked for " + delay + " cycles.");

                        // @todo block the current process and schedule another
                        curProc.setBlockTime(delay);
                        curProc = loadNextProcess();
                        break;

                    case STAT_EXIT:     // process is finished
                    case STAT_ERROR:    // run-time error found
                        System.out.println("Process " + curProc.getId() + " exiting");
                        // @todo stop the current process
                        scheduler.removeProcess(curProc);
                        curProc = loadNextProcess();
                        break;
                }
            }

            // timer interrupt?
            timer_left--;
            if (timer_left <= 0) {
                // @todo timer interrupt happened
				curProc = loadNextProcess();

                // reset timer
                timer_left = CLOCK_PER_TIMER;
            }

            // @todo handle processes whose events have occurred
            scheduler.updateBlockedProcesses();
        }

        System.out.println("Processing complete!");
    }

    // read one byte from memory
    private static int readByte(int addr) throws Exception {
        // check if the address is legal
        if (addr < 0 || addr >= CPU_boundSize) {
            System.out.println("Illegal address " + addr);
            throw new Exception("Address Error");
        }

        return memory[CPU_baseAddr + addr];
    }


    /**
     * Execute one instruction and return STAT_GO, STAT_BLOCK,
     * STAT_EXIT, or STAT_ERROR.
     *
     * @return int
     */
    private static int runOneInst() {
        try {
            // get the instruction opcode
            int opcode = readByte(CPU_instructionPointer);
            CPU_instructionPointer++;

            // branch?
            if (opcode >= INST_BR && opcode <= INST_BR + 100) {
                // calculate % of time we branch
                int percentageBranch = opcode - INST_BR;

                // read destination address (big-endian order)
                int destAddr =
                        (readByte(CPU_instructionPointer) << 8)
                                | readByte(CPU_instructionPointer + 1);
                CPU_instructionPointer += 2;

                // determine if we should jump
                if (rng.nextInt(100) < percentageBranch) {
                    // yes, are branching
                    CPU_instructionPointer = destAddr;
                }
                return STAT_GO;
            }

            // handle everything else
            switch (opcode) {
                case INST_COMP:     // computation
                    return STAT_GO;

                case INST_BLOCK:    // blocking (input/output)
                    return STAT_BLOCK;

                case INST_EXIT:     // exit program
                    return STAT_EXIT;

                default:            // illegal instruction
                    System.out.println("Illegal opcode: " + opcode);
                    return STAT_ERROR;
            }
        } catch (Exception e) {
            return STAT_ERROR;
        }
    }

    /**
     * Load one program, add it to the end of the ready queue.
     */
    private static void loadProgram(Map.Entry<String, Integer> map, SchedulerProcessConfiguration config) {
        try {
            // open the file
            File pfile = new File(map.getKey());
            Scanner in = new Scanner(pfile);

            // get size of the program
            int progLen = in.nextInt();

            // read the program
            for (int i = 0; i < progLen; i++) {
                memory[i + loadAddr] = in.nextInt();
            }
            in.close();

            // @todo create the process, add to the appropriate queue
			scheduler.addProcess(new ScheduledProcess(
                nextProcId,                         //Process ID
                map.getValue(),                     //Group ID
                loadAddr,                           //Base address
                progLen,                            //Process size
                config.getDefaultProcessWeight(),   //Weight weight
                config.getDefaultProcessPriority()  //Base priority
            ));

            // update values
            loadAddr += progLen;
            nextProcId++;
        } catch (Exception e) {
            System.out.println("Error loading program " + map.getKey());
        }
    }

    /**
     * Determine the number of clock cycles for this I/O operation.
     *
     * @return int
     */
    private static int calcBlockWait() {
        // calculate delay in cycles
        return (int) (CLOCK_IO_MIN + CLOCK_IO_DEV * Math.abs(rng.nextGaussian()));
    }

    /**
     * Load the next process into memory.
     *
     * @return The process to execute next.
     */
    private static ScheduledProcess loadNextProcess() {
        ScheduledProcess curProc = scheduler.getNextProcess();

        if(curProc != null) {
            // @todo move scheduling logic to separate method
            CPU_baseAddr  = curProc.getBaseAddress();
            CPU_boundSize = curProc.getAddressSize();

            System.out.println("Process loaded: " + curProc.getId());
        } else if(scheduler.hasProcesses()) {
            System.out.println("Waiting for processes to unblock.");
        }

        return curProc;
    }
}
