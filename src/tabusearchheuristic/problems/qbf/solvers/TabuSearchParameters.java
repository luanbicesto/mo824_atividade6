package tabusearchheuristic.problems.qbf.solvers;

public class TabuSearchParameters {
    private int tenure;
    private int percentageFixeditems;
    private int interationsToStartIntensification;
    private int interationsOfIntensification;
    private String instanceName;
    
    public String getInstanceName() {
        return instanceName;
    }
    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }
    public int getTenure() {
        return tenure;
    }
    public void setTenure(int tenure) {
        this.tenure = tenure;
    }
    public int getPercentageFixeditems() {
        return percentageFixeditems;
    }
    public void setPercentageFixeditems(int percentageFixeditems) {
        this.percentageFixeditems = percentageFixeditems;
    }
    public int getInterationsToStartIntensification() {
        return interationsToStartIntensification;
    }
    public void setInterationsToStartIntensification(int interationsToStartIntensification) {
        this.interationsToStartIntensification = interationsToStartIntensification;
    }
    public int getInterationsOfIntensification() {
        return interationsOfIntensification;
    }
    public void setInterationsOfIntensification(int interationsOfIntensification) {
        this.interationsOfIntensification = interationsOfIntensification;
    }
    
    
}
