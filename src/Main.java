import weka.classifiers.Classifier;


/**
 * Created by bartu on 24/02/2019.
 */

public class Main {

    public static void main(String[] args) throws Exception {

//        Predictor p = new Predictor();
//        SetPredictor sp = new SetPredictor();
//        int year = 2015;
//
//        FileReader r = new FileReader(p, sp);
//
//        while (year < 2018) {
//            System.out.println(year);
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
////
//        System.out.print(p.calculateAccuracy());

        IndividualSetPred ip = new IndividualSetPred();
        ip.getPrediction("Jordan Thompson", "Francis Tiafoe");
        ip.getPrediction("Alex De Minaur", "Andreas Seppi");
        ip.getPrediction("Christian Garin", "Robin Haase");
        ip.getPrediction("Adrian Mannarino", "Fernando Verdasco");
        ip.getPrediction("David Goffin", "Pierre Hugues Herbert");
        ip.getPrediction("Mikhail Kukushkin", "Richard Gasquet");
        ip.getPrediction("Stefanos Tsitsipas", "Nicolas Jarry");

    }
}
