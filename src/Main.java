import weka.classifiers.Classifier;

/**
 * Created by bartu on 24/02/2019.
 */

public class Main {

    public static void main(String[] args) throws Exception {

//        Predictor p = new Predictor();
//        SetPredictor sp = new SetPredictor();
//        int year = 1990;
//
//        FileReader r = new FileReader(p, sp);
//
//        while (year < 2019) {
//            r.readData(year, false);
//            year++;
//        }
//
//        Classifier winCls = p.trainClassifier();
//        p.setClassifier(winCls);
//
//        Classifier setCls =  sp.train();
//
//        r.readData(year, true);
//        p.writepredictions();
//        p.eval(winCls);
//        sp.eval(setCls);
//
//        System.out.print(p.calculateAccuracy());

        IndividualPredictor ip = new IndividualPredictor();

//        ip.getPrediction("Grigor Dimitrov", "Federico Delbonis");
//        ip.getPrediction("Albert Ramos", "Adrian Mannarino");
//        ip.getPrediction("Feliciano Lopez", "Damir Dzumhur");
//        ip.getPrediction("Joao Sousa", "Leonardo Mayer");
//        ip.getPrediction("Alexander Zverev", "Ernests Gulbis");
        ip.getPrediction("Taro Daniel", "Christian Garin");
    }
}
