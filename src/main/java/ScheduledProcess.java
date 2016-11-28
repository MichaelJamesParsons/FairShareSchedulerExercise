public class ScheduledProcess {
    private int id;
    private double weight;
    private double priority;
    private int baseAddress;
    private int addressSize;
    private int blockTime;
    private int utilization;
    private int basePriority;
    private int groupId;

    ScheduledProcess(int id, int groupId, int baseAddress, int addressSize, double weight, int priority) {
        this.id          = id;
        this.weight      = weight;
        this.priority    = priority;
        this.basePriority = priority;
        this.addressSize = addressSize;
        this.baseAddress = baseAddress;
        this.blockTime   = 0;
        this.utilization = 0;
        this.groupId = groupId;
    }

    int calculatePriority(int groupUtilization) {
        int cpu  = (this.utilization - 1) / 2;
        int gcpu = (groupUtilization - 1) / 2;
        return (int)(basePriority + (cpu / 2) + (gcpu / 2) + weight);
    }

    int getId() {
        return id;
    }

    int getBaseAddress() {
        return baseAddress;
    }

    int getAddressSize() {
        return addressSize;
    }

    void setBlockTime(int blockTime) {
        this.blockTime = blockTime;
    }

    public int getGroupId() {
        return groupId;
    }

    void decrementBlockTime() {
        if(this.blockTime > 0) {
            this.blockTime--;
        }
    }

    boolean isBlocked() {
        return this.blockTime > 0;
    }

    public void incrementUtilization() {
        this.utilization++;
    }
}
