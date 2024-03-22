import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

class Tools {

    static void readResultFromFile(String algorithm, String path){
        List<Solution> aproxPF = new ArrayList<>();
        try {
            File instanceFile = new File(path);
            FileInputStream ficheroXlsx = new FileInputStream(instanceFile);
            Workbook ficheroWb = new HSSFWorkbook(ficheroXlsx);
            Sheet sheet = ficheroWb.getSheetAt(0);
            int i = 1;
            //for the items
            Row row = sheet.getRow(i);
            Main.cost = (float) row.getCell(4).getNumericCellValue();
            Main.time = (float) row.getCell(5).getNumericCellValue();
            while(row != null && row.getCell(0) != null){
                Solution solution = new Solution();
                solution.setAlgorithm(algorithm);
                solution.setCost((float) row.getCell(0).getNumericCellValue());
                solution.setCapMembership((float) row.getCell(1).getNumericCellValue());
                solution.setPackingMemebership((float) row.getCell(2).getNumericCellValue());
                aproxPF.add(solution);
                i++;
                row = sheet.getRow(i);
            }
            ficheroXlsx.close();
            Main.pfAlg.add(aproxPF);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void saveFixedPF(List<Solution> data, String instanceName){
        try {
            FileOutputStream fileout = new FileOutputStream(new File(instanceName));
            Workbook ficheroWb = new HSSFWorkbook();
            Sheet sheet = ficheroWb.createSheet("Real Pareto Front");
            Row row = sheet.createRow(0);
            row.createCell(0).setCellValue("Cost");
            row.createCell(1).setCellValue("Overload");
            row.createCell(2).setCellValue("Packing");
            row.createCell(3).setCellValue("Size");
            row.createCell(4).setCellValue("Ave. Cost");
            row.createCell(5).setCellValue("Ave. Time");
            int rowIndex = 1;
            row = sheet.createRow(rowIndex);
            row.createCell(3).setCellValue(data.size());
            row.createCell(4).setCellValue(Main.cost);
            row.createCell(5).setCellValue(Main.time);
            for(Solution s : data){
                row.createCell(0).setCellValue(s.getCost());
                row.createCell(1).setCellValue(s.getCapMembership());
                row.createCell(2).setCellValue(s.getPackingMemebership());
                rowIndex++;
                row = sheet.createRow(rowIndex);
            }
            ficheroWb.write(fileout);
            fileout.flush();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void saveRealPFByInstance(List<Solution> data, String instanceName){
        try {
            FileOutputStream fileout = new FileOutputStream(new File(instanceName+"_realPF.xls"));
            Workbook ficheroWb = new HSSFWorkbook();
            Sheet sheet = ficheroWb.createSheet("Real Pareto Front");
            Row row = sheet.createRow(0);
            row.createCell(0).setCellValue("Cost");
            row.createCell(1).setCellValue("Overload");
            row.createCell(2).setCellValue("Packing");
            row.createCell(3).setCellValue("Algorithms");
            int rowIndex = 1;
            for(Solution s : data){
                row = sheet.createRow(rowIndex);
                row.createCell(0).setCellValue(s.getCost());
                row.createCell(1).setCellValue(s.getCapMembership());
                row.createCell(2).setCellValue(s.getPackingMemebership());
                row.createCell(3).setCellValue(s.getAlgorithm());
                rowIndex++;
            }
            ficheroWb.write(fileout);
            fileout.flush();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void extractNonDominated(List<Solution> fullPF, boolean normalize)
    {
        if(normalize)
            normalize();
        ArrayList<Solution> result = new ArrayList<>();

        fullPF.sort(Comparator.comparing(Solution::getCost)
                .thenComparing(Solution::getCapMembership)
                .thenComparing(Solution::getPackingMemebership).reversed()
        );
        for(int i = 0; i < fullPF.size(); i++){
            Solution candidateSolution = fullPF.get(i);
            ArrayList<ArrayList<String>> improvements = new ArrayList<>();
            if(candidateSolution.getCost() > 0){
                if(result.isEmpty()){
                    result.add(candidateSolution);
                }else{
                    int checkExistingIndex = checkIfExists(candidateSolution,result);
                    boolean checkMembership = checkGeneralMembership(candidateSolution);
                    if(checkExistingIndex == -1 && !checkMembership){
                        int j = 0;
                        boolean found = false;
                        while (j < result.size() && !found){
                            ArrayList<String> candidateCmp = new ArrayList<>();
                            Solution referenceSolution = result.get(j);
                            //Improve at least one without worsen any other
                            if(candidateSolution.getCost() < referenceSolution.getCost()){
                                candidateCmp.add("Better");
                            }else if (candidateSolution.getCost() == referenceSolution.getCost()) {
                                candidateCmp.add("Equal");
                            }else {
                                candidateCmp.add("Worst");
                            }
                            if(candidateSolution.getCapMembership() > referenceSolution.getCapMembership()){
                                candidateCmp.add("Better");
                            }else if (candidateSolution.getCapMembership() == referenceSolution.getCapMembership()) {
                                candidateCmp.add("Equal");
                            }else{
                                candidateCmp.add("Worst");
                            }
                            if(candidateSolution.getPackingMemebership() > referenceSolution.getPackingMemebership()){
                                candidateCmp.add("Better");
                            }else if (candidateSolution.getPackingMemebership() == referenceSolution.getPackingMemebership()) {
                                candidateCmp.add("Equal");
                            }else{
                                candidateCmp.add("Worst");
                            }
                            if(candidateCmp.stream().anyMatch(x -> x.equalsIgnoreCase("Better"))){
                                j++;
                                improvements.add(candidateCmp);
                            }else{
                                found = true;
                            }
                        }
                        if(!found){
                            ArrayList<Solution> indexes = new ArrayList<>();
                            int cursor = 0;
                            for(ArrayList<String> r : improvements){
                                /*
                                if((r.stream().filter(x -> x.equalsIgnoreCase("Equal")).count() == 2 &&
                                        r.stream().filter(x -> x.equalsIgnoreCase("Better")).count() == 1) ||
                                        (r.stream().filter(x -> x.equalsIgnoreCase("Better")).count() == 2 &&
                                                r.stream().filter(x -> x.equalsIgnoreCase("Equal")).count() == 1) ||
                                        r.stream().filter(x -> x.equalsIgnoreCase("Better")).count() == 3){
                                    indexes.add(result.get(cursor));
                                }
                                */
                                if(r.stream().noneMatch(x -> x.equalsIgnoreCase("Worst"))){
                                    indexes.add(result.get(cursor));
                                }
                                cursor++;
                            }
                            if(!indexes.isEmpty())
                                result.removeAll(indexes);
                            result.add(candidateSolution);
                        }
                    }else{
                        if(checkMembership){
                            addNewCrispSolution(candidateSolution,result);
                        }else{
                            if(checkExistingIndex > -1 && !result.get(checkExistingIndex-1).getAlgorithm().contains(candidateSolution.getAlgorithm())){
                                result.get(checkExistingIndex-1).setAlgorithm(result.get(checkExistingIndex-1).getAlgorithm()+","+candidateSolution.getAlgorithm());
                            }
                        }
                    }
                }
            }
        }
        fullPF.clear();
        fullPF.addAll(result);
    }

    private static int checkIfExists(Solution s, ArrayList<Solution> cmp){
        boolean found = false;
        int index = 0;
        while(!found && index < cmp.size()){
            if(cmp.get(index).getCost() == s.getCost() &&
                    cmp.get(index).getCapMembership() == s.getCapMembership() &&
                        cmp.get(index).getPackingMemebership() == s.getPackingMemebership()){
                found = true;
            }else{
                index++;
            }
        }
        return (found)?index:-1;
    }

    private static boolean checkGeneralMembership(Solution s){
        return (s.getCapMembership()==1 && s.getPackingMemebership()==1);
    }

    private static void addNewCrispSolution(Solution s, ArrayList<Solution> result){
        ArrayList<Solution> tmpToDelete = new ArrayList<>();
        boolean[] flag = {false};
        result.stream().filter(x -> (x.getPackingMemebership()==1 && x.getCapMembership()==1) || (x.getCost() > s.getCost())).forEach(y -> {
            flag[0] = true;
            if(y.getCost() > s.getCost()){
                tmpToDelete.add(y);
            }
        });
        if(flag[0]){
            if(!tmpToDelete.isEmpty()){
                result.removeAll(tmpToDelete);
                result.add(s);
            }
        }else{
            result.add(s);
        }
    }

    static void normalize(){
        for(List<Solution> pf : Main.pfAlg){
            Main.realPF.addAll(pf);
        }
        double[] max = {0};
        max[0] = Main.realPF.stream().max(Comparator.comparing(Solution::getCost)).get().cost;

        Main.realPF.forEach(x -> {
            float currentCost = x.getCost();
            x.setCost((float) (1 - (currentCost / max[0])));
        });
    }

    static void removeOldSolutionsDir(){
        File index = new File("results/G1/soluciones");
        if(index.delete()){
            File old = new File("results/G1/fixed");
            File newFile = new File("results/G1/solutions");
            old.renameTo(newFile);
        }
    }
}
