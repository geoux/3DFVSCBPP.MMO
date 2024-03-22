import javafx.util.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

    static List<Solution> realPF;
    static List<List<Solution>> pfAlg;
    static ArrayList<String> instancesNames;
    static float cost = 0f;
    static float time = 0f;

    Pair<String,String> pathAlgorithm;
    static ArrayList<Pair<String,String>> resultFiles;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        realPF = new ArrayList<>();
        pfAlg = new ArrayList<>();
        instancesNames = new ArrayList<>();
        resultFiles = new ArrayList<>();

        for(int i = 1; i < 4; i++){
            String dirName = "results/G"+i+"/instances/";
            try {
                //Reading instances and filling names list
                Files.list(new File(dirName).toPath())
                        .forEach(path -> {
                            path.getFileName().toString().replace("..",".");
                            instancesNames.add(path.getFileName().toString());
                        });

                dirName = "results/G"+i+"/soluciones/";
                Files.list(new File(dirName).toPath())
                        .forEach(path -> {
                            int pos = path.getFileName().toString().indexOf("_");
                            String algName = path.getFileName().toString().substring(0,pos);
                            Pair<String,String> p = new Pair<>(algName,path.getFileName().toString());
                            resultFiles.add(p);
                        });
            } catch (IOException e) {
                e.printStackTrace();
            }

            for(String name : instancesNames){
                System.out.println(name);
                List<Pair<String,String>> sub = resultFiles.stream().filter(x -> x.getValue().contains(name)).collect(Collectors.toList());
                int index = 0;
                String fixedDirName = "results/G"+i+"/fixed/";
                for(Pair<String,String> instance : sub){
                    System.out.println(instance.getKey()+" ------> "+instance.getValue());
                    Tools.readResultFromFile(instance.getKey(),dirName+instance.getValue());
                    System.out.println("PF BEFORE fix: "+pfAlg.get(index).size());
                    Tools.extractNonDominated(pfAlg.get(index),false);
                    System.out.println("PF AFTER fix: "+pfAlg.get(index).size());
                    Tools.saveFixedPF(pfAlg.get(index),fixedDirName+instance.getValue());
                    realPF.addAll(pfAlg.get(index));
                    index++;
                }
                Tools.extractNonDominated(realPF,false);
                System.out.println("Real PF for instance: "+ realPF.size());
                fixedDirName = "results/G"+i+"/real/";
                Tools.saveRealPFByInstance(realPF,fixedDirName+name);

                System.out.println("Processing metrics for PF of "+name);
                //ToDo, metrics
                String metricFolder = "results/G"+i+"/metrics/";
                for(List<Solution> pf : pfAlg){
                    MoMetrics mo = new MoMetrics();
                    double errorRate = mo.ErroRate(pf,realPF);
                    double generationalDistance = mo.GenerationalDistance(pf,realPF);
                    double dispersion = mo.Spread(pf);
                }


                realPF.clear();
                //Tools.removeOldSolutionsDir();
                System.out.println("-------------------------------------------------");
            }
            instancesNames.clear();

        }
    }
}
