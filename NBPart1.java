import java.io.*;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by luxing and Yujia on 10/21/2016.
 * This is a program that uses Naive Bayes to classify labels.
 */
public class NBPart1 {
    public static void main(String[] args) {
        List training;
        List testing;
        List Att;
        HashMap PLabels;
        String file;
        String seed_val;
        seed_val = args[1];
//        seed_val = "30";
        Long seed = Long.parseLong(seed_val);                  //System.nanoTime(); random seed
        Random rng = new Random(seed);
        List shuffled = new ArrayList();
        Integer PercentageSplit = 66;   // percentage of training set
        file = args[0];
//        file = "C:/2016 Fall/ML/opticalDigit.csv";
        String file_name = file.substring(0, file.lastIndexOf('.'));
        File f = new File(file);
        String path = f.getAbsolutePath();
        File csvFile = new File(path);  // file path

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
        List unqshuffled = nonrepLabels(shuffled);
        List unqtraining = nonrepLabels(training);
        List unqtesting = nonrepLabels(testing);
        //run algorithm
        NBPart1 nbPart1= new NBPart1();
//        PLabels = nbPart1.PLabels(unqtraining); //e.g. {"yes"=0.46, "no"=0.53}
//        nbPart1.predicted(nbPart1.nonrepLabels(training), Att, testing, training);
//        nbPart1.calcProb(Att, testing.get(0).toString(), training);


        //test set
        List actual = nbPart1.labelsColumn(testing);
        List predicted = nbPart1.predicted(nonrepattcolumn(shuffled,Att),unqtesting,Att, testing,training,shuffled,unqtraining,training(Att, training,unqtraining),PLabels(labelsColumn(training)));

        //System.out.println(actual);
        //System.out.println(predicted);

        int[][] matrix = new int[unqshuffled.size()][unqshuffled.size()];
        for (int m = 0; m < matrix.length; m++) {
            for (int n = 0; n < matrix.length; n++) {
                matrix[m][n]=0;
            }
        }


        for(int i=0; i<testing.size();i++){
            int x=0,y=0;
            for(int j=0; j<unqshuffled.size();j++) {
                if (actual.get(i).equals(unqshuffled.get(j))) {
                    x = j;
                }
            }
            for(int j=0; j<unqshuffled.size();j++) {
                if (predicted.get(i).equals(unqshuffled.get(j))) {
                    y = j;
                }
            }
            matrix[x][y]+=1;
        }
//        for (int m = 0; m < matrix.length; m++) {
//            for (int n = 0; n < matrix.length; n++) {
//                System.out.print("m "+m+" n "+n+"  "+matrix[m][n]+"; ");
//            }
//        }
        //System.out.println(matrix);
        //output

        PrintWriter pw = null;
        try {
//            pw = new PrintWriter(new File("results_"+file_name+"_NaiveBayes_"+seed_val+".csv"));
            pw = new PrintWriter(new File("output.csv"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        StringBuilder sb = new StringBuilder();
        for(int j=0; j<unqshuffled.size();j++) {
            sb.append(unqshuffled.get(j));
            sb.append(",");
        }
        sb.append("\n");
        for(int i=0; i<unqshuffled.size();i++){
            for(int j=0; j<unqshuffled.size();j++) {
                sb.append(matrix[i][j]);
                sb.append(",");
            }
            sb.append(unqshuffled.get(i));
            sb.append("\n");
        }

        pw.write(sb.toString());
        pw.close();
    }



    public List predicted(ArrayList<Integer> bs, List labels, List Att, List testing, List training, List shuffled, List unqtraining, HashMap trainingAtttrainingunqtraining,HashMap<String,Double> PLabels){
        List predicted = new ArrayList();

        for (int i=0;i<testing.size();i++){
            String label = labels.get(0).toString();
            ArrayList calcprob = calcProb(bs,Att, Arrays.asList((testing.get(i).toString()).split(",")), training, shuffled,unqtraining,trainingAtttrainingunqtraining,PLabels);
            Double max = (Double)calcprob.get(0);
            for (int j=1;j<labels.size();j++) {
                if((Double)calcprob.get(j) > max){
                    max = (Double)calcprob.get(j);
                    label = labels.get(j).toString();
                }
            }
            predicted.add(i,label);
        }

        return predicted;
    }

    //e.g. [0.0106,0.0274], Prob when label = YES / No
    public ArrayList calcProb(ArrayList<Integer> bs, List Att, List<String> test, List training, List shuffled, List unqtraining, HashMap trainingAtttrainingunqtraining, HashMap<String,Double> PLabels){
        ArrayList<Double> Sum = new ArrayList<>();
        for (int i=0;i<unqtraining.size();i++){
            ArrayList<Double> calcProbi = new ArrayList<>();
            Double PLabel = PLabels.get(unqtraining.get(i));  //P(l)
            HashMap<String, HashMap<String, Double>> temp = (HashMap)trainingAtttrainingunqtraining.get(unqtraining.get(i).toString());
            for (int j=0;j<Att.size();j++){
                HashMap<String, Double> temp2 = (HashMap)temp.get(Att.get(j).toString());
                Double prob = temp2.get(test.get(j+1).toString());
                if(prob==null){
                    prob = 0d;
                }
                prob = (prob+1)/(temp2.size()+bs.get(j));
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
        return Sum;
    }


    //e.g. key = yes, values = <Outlook,<Sunny,0.2222>>
    public static HashMap training(List Att, List training, List unqtraining){
        HashMap<String, HashMap<String,HashMap<String,Double>>> train = new HashMap<>();
        List labels =unqtraining;
        for (int i=0; i<labels.size();i++){
            train.put(labels.get(i).toString(), AttProb(Att, subtraining(labels.get(i).toString(), training)));
        }
        return train;
    }

    //key = att, value = prob (e.g. <Outlook,<Sunny,0.2222>> )
    public static HashMap AttProb( List Att, List subtraining){
        HashMap<String,HashMap<String,Double>> AttProb = new HashMap<>();
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
//            Prob.replaceAll((k, v) -> (v)/(attColumn.size()));
            AttProb.put(Att.get(i).toString(),Prob);
        }
        return AttProb;
    }


    //get sub data set
    public static List subtraining(String label, List training){
        List subtraining=new ArrayList();
        List labelsColumn = labelsColumn(training);
        for (int i=0; i<labelsColumn.size();i++){
            if(labelsColumn.get(i).toString().equals(label)){
                subtraining.add(training.get(i));
            }
        }
        return subtraining;
    }

    //column of ith att  (i=0, label column)
    public static List attColumn(int i, List dataset){
        List attColumn=new ArrayList(); //labels in S
        for (int j=0; j<dataset.size();j++){
            List<String> s = Arrays.asList((dataset.get(j)).toString().split(","));
            attColumn.add(s.get(i));
        }
        return attColumn;
    }


    public static List<String> labelsColumn(List dataset){
        List<String> labels = new ArrayList<>(); //labels in S
        for (int i=0; i<dataset.size();i++){
            List<String> s = Arrays.asList((dataset.get(i)).toString().split(","));

            labels.add(s.get(0));
        }
        return labels;
    }

    //unique labels
    public static List nonrepLabels(List training){
        List nonrepLabels = new ArrayList();
        HashMap<String, Double> pLabels = PLabels(labelsColumn(training));
        for ( String key : pLabels.keySet() ) {
            nonrepLabels.add(key);
        }
        return nonrepLabels;
    }

    //unique attcolumn
    public static ArrayList<Integer> nonrepattcolumn(List shuffled, List Att){   //ith att\
        ArrayList<Integer> bs = new ArrayList<>();
        for (int i=0;i<Att.size();i++) {
            HashSet nonrepattcolumn = new HashSet();
            List attcol = attColumn(i + 1, shuffled);
            for (int j = 0; j < attcol.size(); j++) {
                nonrepattcolumn.add(attcol.get(j));
            }
            bs.add(i,nonrepattcolumn.size());
        }
        return bs;
    }


    public static HashMap<String,Double> PLabels(List labels){
        HashMap<String, Double> pLabels = new HashMap<String, Double>();
        for(int i=0;i<labels.size();i++){
            if (!pLabels.containsKey(labels.get(i).toString())){
                pLabels.put(labels.get(i).toString(),1d);
            } else {
                pLabels.put(labels.get(i).toString(),pLabels.get(labels.get(i).toString())+1);
            }
        }
        for (String key : pLabels.keySet()) {
            Double v = pLabels.get(key);
            v = v/labels.size();
            pLabels.put(key, v);
        }
        return pLabels;
    }




}
