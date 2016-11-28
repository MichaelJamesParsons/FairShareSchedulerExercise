import java.util.HashMap;

class SchedulerProcessConfiguration {
    private double defaultProcessWeight;
    private int defaultProcessPriority;
    private HashMap<String, Integer>[] files;

    double getDefaultProcessWeight() {
        return defaultProcessWeight;
    }

    void setDefaultProcessWeight(double defaultProcessWeight) {
        this.defaultProcessWeight = defaultProcessWeight;
    }

    int getDefaultProcessPriority() {
        return defaultProcessPriority;
    }

    void setDefaultProcessPriority(int defaultProcessPriority) {
        this.defaultProcessPriority = defaultProcessPriority;
    }

    HashMap[] getFiles() {
        return files;
    }

    void setFiles(HashMap<String, Integer>[] files) {
        this.files = files;
    }
}
