
import java.util.ArrayList;
import java.util.List;

public class MoMetrics {

    /*** porciento de soluciones q no son miembros del frente de pareto verdadero **/
    public double ErroRate(List<Solution> solutionsFPcurrent, List<Solution> solutionsFPtrue){
        double tasaError = 0;
        for (Solution estadoM : solutionsFPcurrent) {
            boolean found = solutionsFPtrue.stream().anyMatch(x ->
                    x.getCost()==estadoM.getCost() &&
                            x.getCapMembership()==estadoM.getCapMembership() &&
                            x.getPackingMemebership()==estadoM.getPackingMemebership());
            if (!found) {
                tasaError++;
            }
        }
        return tasaError / solutionsFPcurrent.size();
    }

    /*** Indica  qui tan  lejos  estan  los  elementos  del frente  de  Pareto  actual  respecto  al  frente  de  Pareto  verdadero ****/
    public double GenerationalDistance(List<Solution> solutionsFPcurrent, List<Solution> solutionsFPtrue) {

        float cumulativeDistance = 0;
        for (Solution estadoM1 : solutionsFPcurrent) {
            double minimo = 999999999;
            for(Solution estadoM2 : solutionsFPtrue){
                double distancia = 0;
                distancia += Math.pow(estadoM1.getCost() - estadoM2.getCost(), 2);
                distancia += Math.pow(estadoM1.getCapMembership() - estadoM2.getCapMembership(), 2);
                distancia += Math.pow(estadoM1.getPackingMemebership() - estadoM2.getPackingMemebership(), 2);
                if (distancia < minimo) {
                    minimo = distancia;
                }
            }
            cumulativeDistance += minimo;
        }
        return Math.sqrt(cumulativeDistance) / solutionsFPcurrent.size();
    }

    public double Spread(List<Solution> estados){
        ArrayList<Double> distancias = new ArrayList<>();
        double min = 999999999;
        for (int i = 0; i < estados.size()-1; i++) {
            Solution estadoM1 = estados.get(i);
            double distancia = 0;
            for (int j = 1; j < estados.size(); j++) {
                Solution estadoM2 = estados.get(j);

                distancia += Math.abs(estadoM1.getCost() - estadoM2.getCost());
                distancia += Math.abs(estadoM1.getCapMembership() - estadoM2.getCapMembership());
                distancia += Math.abs(estadoM1.getPackingMemebership() - estadoM2.getPackingMemebership());

                if (distancia < min) {
                    min = distancia;
                }
                distancias.add(min);
            }
        }
        float sum = 0;
        for (Double dist : distancias) {
            sum += dist;
        }
        float media = sum / distancias.size();
        float sumDistancias = 0;
        for (Double dist : distancias) {
            sumDistancias += Math.pow((media - dist), 2);
        }
        double dispersion = 0;
        if (estados.size() > 1) {
            dispersion = Math.sqrt((1.0 / (estados.size() - 1)) * sumDistancias);
        }
        return dispersion;
    }
}
