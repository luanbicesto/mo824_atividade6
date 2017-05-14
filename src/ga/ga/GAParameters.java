package ga.ga;

public class GAParameters {
    private String instanceName;
    private String constructionType;
    private int populationSize;
    private Integer mutationTax;
    private Double fitRate;
    private Double locusTax;
    
    public String getInstanceName() {
        return instanceName;
    }
    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }
    public String getConstructionType() {
        return constructionType;
    }
    public void setConstructionType(String constructionType) {
        this.constructionType = constructionType;
    }
    public int getPopulationSize() {
        return populationSize;
    }
    public void setPopulationSize(int populationSize) {
        this.populationSize = populationSize;
    }
    public Integer getMutationTax() {
        return mutationTax;
    }
    public void setMutationTax(Integer mutationTax) {
        this.mutationTax = mutationTax;
    }
    public Double getFitRate() {
        return fitRate;
    }
    public void setFitRate(Double fitRate) {
        this.fitRate = fitRate;
    }
    public Double getLocusTax() {
        return locusTax;
    }
    public void setLocusTax(Double locusTax) {
        this.locusTax = locusTax;
    }
    
}
