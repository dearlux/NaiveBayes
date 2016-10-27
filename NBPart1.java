import java.io.*;
import java.util.*;

/**
 * Created by lux on 10/21/2016.
 */
public class NBPart1 {
    public static void main(String[] args) {
        List training;
        List testing;
        List Att;
        HashMap PLabels;
        Long seed = System.nanoTime();   //random seed
        Random rng = new Random(seed);
        List shuffled = new ArrayList();
        Integer PercentageSplit = 66;   // percentage of training set
//        File csvFile = new File("C:/2016 Fall/ML/OpticalDigit.csv");  // file path
        File csvFile = new File("C:/2016 Fall/ML/monks.csv");  // file path

        //Scan data
        BufferedReader br = null;
        String line;
        try {
            br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {
                shuffled.add(line.replace("\"",""));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // split train/test sets
        String names = shuffled.get(0).toString();

        List<String> name = Arrays.asList(names.split(","));
        Att = name.subList(1, name.size());
        shuffled.remove(0);
        Collections.shuffle(shuffled, rng);
        training = shuffled.subList(0, java.lang.Math.round(shuffled.size() * PercentageSplit / 100));
        testing = shuffled.subList(java.lang.Math.round(shuffled.size() * PercentageSplit / 100), shuffled.size());


       //run algorithm
        NBPart1 nbPart1= new NBPart1();
        PLabels = nbPart1.PLabels(nbPart1.labelsColumn(training)); //e.g. {"yes"=0.46, "no"=0.53}
//        System.out.println(nbPart1.training(Att, training));
//        nbPart1.predicted(nbPart1.nonrepLabels(training), Att, testing, training);
//        nbPart1.calcProb(Att, testing.get(0).toString(), training);
//        System.out.print("testing"+testing.get(0).toString());


        //test set
        List actual = nbPart1.labelsColumn(testing);
        List predicted = nbPart1.predicted(nbPart1.nonrepLabels(testing),Att, testing,training);
        System.out.print(predicted);

        int[][] matrix = new int[nbPart1.nonrepLabels(testing).size()][nbPart1.nonrepLabels(testing).size()];
        for (int m = 0; m < matrix.length; m++) {
            for (int n = 0; n < matrix.length; n++) {
                matrix[m][n]=0;
            }
        }


        for(int i=0; i<testing.size();i++){
            int x=0,y=0;
            for(int j=0; j<nbPart1.nonrepLabels(testing).size();j++) {
                if (actual.get(i) == nbPart1.nonrepLabels(testing).get(j)) {
                    y = j;
                }
            }
            for(int j=0; j<nbPart1.nonrepLabels(testing).size();j++) {
                if (predicted.get(i) == nbPart1.nonrepLabels(testing).get(j)) {
                    x = j;
                }
            }
            matrix[x][y]+=1;
        }

        //output
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new File("NBPart1.csv"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        StringBuilder sb = new StringBuilder();

        sb.append(PLabels);

        pw.write(sb.toString());
        pw.close();
    }



    public List predicted(List labels, List Att, List testing, List training){
        List predicted = new ArrayList();

        for (int i=0;i<testing.size();i++){
            String label = labels.get(0).toString();
            Double max = (Double)calcProb(Att, testing.get(i).toString(), training).get(0);
            for (int j=1;j<labels.size();j++) {
                if((Double)calcProb(Att, testing.get(i).toString(), training).get(j) > max){
                    max = (Double)calcProb(Att, testing.get(i).toString(), training).get(j);
                    label = labels.get(j).toString();
                }
            }
        predicted.add(i,label);
        }

        System.out.println("predicted"+predicted);
        return predicted;
    }

    //e.g. [0.0106,0.0274], Prob when label = YES / No
        public ArrayList calcProb(List Att, String testing, List training){
            ArrayList<Double> Sum = new ArrayList<>();
        for (int i=0;i<nonrepLabels(training).size();i++){
            ArrayList<Double> calcProbi = new ArrayList<>();
            Double PLabel = PLabels(labelsColumn(training)).get(nonrepLabels(training).get(i));  //P(l)
            for (int j=0;j<Att.size();j++){
                HashMap<String, HashMap<String, Double>> temp = (HashMap)training(Att, training).get(nonrepLabels(training).get(i).toString());
                HashMap<String, Double> temp2 = (HashMap)temp.get(Att.get(j).toString());
                List<String> test = Arrays.asList(testing.split(","));
                Double prob = temp2.get(test.get(j+1).toString());
                calcProbi.add(prob);
            }
            Double sum = Math.log(PLabel);
            for (int k=0; k<calcProbi.size();k++){
                if (calcProbi.get(k)!=null) {
                    sum += Math.log(calcProbi.get(k));
                }
            }
            Sum.add(i,sum);
        }
        System.out.println("Sum "+Sum);
        return Sum;
    }


    //e.g. key = yes, values = <Outlook,<Sunny,0.2222>>
    public HashMap training(List Att, List training){
        HashMap<String, HashMap<String,HashMap<String,Double>>> train = new HashMap<>();
        List labels =nonrepLabels(training);
        for (int i=0; i<labels.size();i++){
            train.put(labels.get(i).toString(), AttProb(labels.get(i).toString(), Att, training));
        }
        return train;
    }

    //key = att, value = prob (e.g. <Outlook,<Sunny,0.2222>> )
    public HashMap AttProb(String label, List Att, List training){

        HashMap<String,HashMap<String,Double>> AttProb = new HashMap<>();
        List subtraining = subtraining(label, training);
        for (int i=0; i<Att.size();i++){
            HashMap<String, Double> Prob = new HashMap<>();
            List attColumn = attColumn(i+1, subtraining);
            for (int j=0; j<attColumn.size();j++) {
                if (!Prob.containsKey(attColumn.get(j).toString())){
                    Prob.put(attColumn.get(j).toString(), 1d);
                } else {
                    Prob.put(attColumn.get(j).toString(), Prob.get(attColumn.get(j).toString()) + 1);
                }
            }
            Prob.replaceAll((k, v) -> (v+1)/(attColumn.size()+Prob.size()));
            AttProb.put(Att.get(i).toString(),Prob);
        }
        return AttProb;
    }


    //get sub data set
    public List subtraining(String label, List training){
//        System.out.print("\n"+"label"+label);
        List subtraining=new ArrayList();
        List labelsColumn = labelsColumn(training);
//        System.out.print("\n"+"labelsColumn"+labelsColumn);
        for (int i=0; i<labelsColumn.size();i++){
            if(labelsColumn.get(i).toString().equals(label)){
                subtraining.add(training.get(i));
            }
        }
        return subtraining;
    }

    //column of ith att
    public List attColumn(int i, List dataset){
        List attColumn=new ArrayList(); //labels in S
        for (int j=0; j<dataset.size();j++){
            List<String> s = Arrays.asList((dataset.get(j)).toString().split(","));
            attColumn.add(s.get(i));
        }
        return attColumn;
    }


    public List<String> labelsColumn(List dataset){
        List<String> labels = new ArrayList<>(); //labels in S
        for (int i=0; i<dataset.size();i++){
            List<String> s = Arrays.asList((dataset.get(i)).toString().split(","));

            labels.add(s.get(0));
        }
        return labels;
    }

        //unique labels
        public List nonrepLabels(List training){
        List nonrepLabels = new ArrayList();
        HashMap<String, Double> pLabels = PLabels(labelsColumn(training));
        for ( String key : pLabels.keySet() ) {
             nonrepLabels.add(key);
        }
        return nonrepLabels;
    }

    public HashMap<String,Double> PLabels(List labels){
        HashMap<String, Double> pLabels = new HashMap<String, Double>();
        for(int i=0;i<labels.size();i++){
            if (!pLabels.containsKey(labels.get(i).toString())){
                pLabels.put(labels.get(i).toString(),1d);
            } else {
                pLabels.put(labels.get(i).toString(),pLabels.get(labels.get(i).toString())+1);
            }
        }
        pLabels.replaceAll((k, v) -> v/labels.size());
//       System.out.println("pLabels"+pLabels);
        return pLabels;
    }




}
