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
//        nbPart1.train(Att, training);
        PLabels = nbPart1.training(Att,training); //e.g. {"yes"=0.46, "no"=0.53}
        System.out.print(nbPart1.training(Att,training));
 //       System.out.print("\n"+testing);


        //test set
        List actual = nbPart1.labelsColumn(testing);
        List predicted = nbPart1.predicted(Att, testing);

        //output
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new File("NBPart1.csv"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        StringBuilder sb = new StringBuilder();

        sb.append(PLabels);
        sb.append(',');
        sb.append("Name");
        sb.append('\n');

        pw.write(sb.toString());
        pw.close();
        System.out.println("done!");
    }



    public List predicted(List Att, List testing){
        List predicted = new ArrayList();



        return predicted;
    }

    //e.g. key = yes, values = <Outlook,<Sunny,0.2222>>
    public HashMap training(List Att, List training){
        HashMap<String, HashMap> train = new HashMap<>();
        List labels =nonrepLabels(training);
        for (int i=0; i<labels.size();i++){
            train.put(labels.get(i).toString(), AttProb(labels.get(i).toString(), Att, training));
        }
        return train;
    }

    //key = att, value = prob (e.g. <Outlook,<Sunny,0.2222>> )
    public HashMap AttProb(String label, List Att, List training){
        HashMap<String, HashMap> AttProb = new HashMap<>();
        HashMap<String, Double> Prob = new HashMap<>();
        List subtraining = subtraining(label, training);
        for (int i=0; i<Att.size();i++){
            List attColumn = attColumn(i+1, subtraining);
            for (int j=0; j<attColumn.size();j++) {
                if (!Prob.containsKey(attColumn.get(j).toString())){
                    Prob.put(attColumn.get(j).toString(), 1d);
                } else {
                    Prob.put(attColumn.get(j).toString(), Prob.get(attColumn.get(j).toString()) + 1);
                }
            }
            Prob.replaceAll((k, v) -> v/attColumn.size());

//            System.out.print(Att.get(i).toString()+"\n");
            AttProb.put(Att.get(i).toString(),Prob);
        }
        return AttProb;
    }



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
        return pLabels;
    }




}
