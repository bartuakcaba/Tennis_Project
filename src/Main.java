import weka.classifiers.Classifier;

/**
 * Created by bartu on 24/02/2019.
 */

public class Main {

    public static void main(String[] args) throws Exception {

        Predictor p = new Predictor();
        int year = 2017;

        FileReader r = new FileReader(p);

        while (year < 2018) {
            r.readData(year, false);
            year++;
        }

        Classifier cls = p.trainClassifier();
        p.setClassifier(cls);

        r.readData(year, true);
        p.writepredictions();
        p.eval(cls);


        System.out.print(p.calculateAccuracy());

    }
}
