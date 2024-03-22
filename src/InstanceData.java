public class InstanceData {
    private String instanceName;
    private String algorithm;
    private int nonDominatedSolutions;
    private double errorRate;
    private double generationalDistance;
    private double dispersion;
    private double time;

    public InstanceData(String instanceName, String algorithm, int nonDominatedSolutions, double errorRate, double generationalDistance, double dispersion, double time) {
        this.instanceName = instanceName;
        this.algorithm = algorithm;
        this.nonDominatedSolutions = nonDominatedSolutions;
        this.errorRate = errorRate;
        this.generationalDistance = generationalDistance;
        this.dispersion = dispersion;
        this.time = time;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public int getNonDominatedSolutions() {
        return nonDominatedSolutions;
    }

    public double getErrorRate() {
        return errorRate;
    }

    public double getGenerationalDistance() {
        return generationalDistance;
    }

    public double getDispersion() {
        return dispersion;
    }

    public double getTime() {
        return time;
    }
}
