import java.util.HashMap;
import java.util.Map;

class FairShareScheduler {
    private HashMap<Integer, ScheduledProcess> processes;
    private HashMap<Integer, Integer> groupUtilization;

    FairShareScheduler() {
        processes  = new HashMap<>();
        groupUtilization = new HashMap<>();
    }

    void addProcess(ScheduledProcess process) {
        processes.put(process.getId(), process);

        if(!groupUtilization.containsKey(process.getGroupId())) {
            groupUtilization.put(process.getGroupId(), 1);
        }
    }

    void removeProcess(ScheduledProcess p) {
        processes.remove(p.getId());
    }

    /**
     * Fetches the next process.
     *
     * Blocked process will not be considered in the selection process. Uses
     * the following calculation to determine the next process:
     *
     *      i = CPU Utilization.
     *
     *      procUtil  = (i - 1) / 2.
     *      groupUtil = (i - 1) / 2.
     *      Priority  = BASE_PRIORITY + (procUtil / 2) + (groupUtil / 2) + weight.
     *
     * See Page 423 of textbook for more information.
     *
     * @return The process to be executed next.
     */
    ScheduledProcess getNextProcess() {
        ScheduledProcess minProcess = null;
        Integer minProcessPriority = null;

        for(Map.Entry<Integer, ScheduledProcess> map : processes.entrySet()) {
            //Fetch the process and calculate its priority
            ScheduledProcess p = map.getValue();

            //Skip blocked processes
            if(p.isBlocked()) continue;

            int pPriority = p.calculatePriority(groupUtilization.get(p.getGroupId()));

            //Set the process with the smallest priority value
            // (smaller values have a higher priority).
            if(minProcess == null) {
                minProcess = p;
                minProcessPriority = pPriority;
            } else {
                if(pPriority < minProcessPriority) {
                    minProcess = p;
                    minProcessPriority = pPriority;
                }
            }
        }

        return minProcess;
    }

    /**
     * Updates a process's utilization.
     * @param p
     */
    void updateProcessUtilization(ScheduledProcess p) {
        //Increment the process's utilization
        p.incrementUtilization();

        //Increment process group's utilization
        groupUtilization.replace(p.getGroupId(), groupUtilization.get(p.getGroupId()) + 1);
    }

    /**
     * Decrements the block time of each process in the queue.
     */
    void updateBlockedProcesses() {
        for(Map.Entry<Integer, ScheduledProcess> map : processes.entrySet()) {
            ScheduledProcess p = map.getValue();
            p.decrementBlockTime();
        }
    }

    /**
     * Determines if there are processes to be executed.
     *
     * @return True if processes are still in the queue.
     */
    boolean hasProcesses() {
        return processes.size() > 0;
    }
}
