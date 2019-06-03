import weka.classifiers.Classifier;


/**
 * Created by bartu on 24/02/2019.
 */

public class Main {

    public static void main(String[] args) throws Exception {

        Predictor p = new Predictor();
        SetPredictor sp = new SetPredictor();
        int year = 2013;

        FileReader r = new FileReader(p, sp);

        while (year < 2017) {
            System.out.println(year);
            r.readData(year, false);
            year++;
        }

        Classifier winCls = p.trainClassifier();
        p.setClassifier(winCls);

        Classifier setCls =  sp.train();

        r.readData(year, true);
        p.writepredictions();
        p.eval(winCls);
        sp.eval(setCls);
        r.writeTitleWInners();

        System.out.print(p.calculateAccuracy());

//        IndividualPredictor ip = new IndividualPredictor();
//        ip.getPrediction("Djokovic N.", "Struff J.L.");
//        ip.getPrediction("Fognini F.", "Zverev A.");
//        ip.getPrediction("Thiem D.", "Monfils G.");
//        ip.getPrediction("Khachanov K.", "Del Potro J.M.");
    }
}
