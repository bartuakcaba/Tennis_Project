import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.Logistic;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by bartu on 16/05/2019.
 */
public class SetPredictor {

    Instances dataset;
    Instances test;

    public SetPredictor() {
        ArrayList<Attribute> attrs = new ArrayList<>();

        Attribute att1 = new Attribute("higher_rating");
        attrs.add(att1);
        Attribute att2 = new Attribute("higher_rd");
        attrs.add(att2);
        Attribute att3 = new Attribute("higher_vol");
        attrs.add(att3);
        Attribute att4 = new Attribute("lower_rating");
        attrs.add(att4);
        Attribute att5 = new Attribute("lower_rd");
        attrs.add(att5);
        Attribute att6 = new Attribute("lower_vol");
        attrs.add(att6);
        Attribute att7 = new Attribute("h2h");
        attrs.add(att7);
        Attribute att8 = new Attribute("class", new ArrayList<>(Arrays.asList(new String[] {"2", "3"})));
        attrs.add(att8);

        dataset = new Instances("train", attrs, 10000);
        dataset.setClassIndex(dataset.numAttributes()-1);
        test = new Instances("test", attrs, 10000);
        test.setClassIndex(test.numAttributes()-1);
    }

    public void addToDataset(Double[] winnerRating, Double[] loserRating, Double[] winSurf, Double[] loseSurf, Double h2h, int score) {

        if (score != 2 && score != 3) {
            return;
        }

        Instance inst = new DenseInstance(8);
        inst.setDataset(dataset);

        inst.setValue(0, winnerRating[0]);
        inst.setValue(1, winSurf[0]);
        inst.setValue(2, winnerRating[2]);
        inst.setValue(3, loserRating[0]);
        inst.setValue(4, loseSurf[0]);
        inst.setValue(5, loserRating[2]);
//        inst.setValue(6, h2h);

        inst.setValue(7, Integer.toString(score));

        dataset.add(inst);
    }

    public void addToTest(Double[] winnerRating, Double[] loserRating, Double[] winSurf, Double[] loseSurf, Double h2h, int score) {

        if (score != 2 && score != 3) {
            return;
        }

        Instance inst = new DenseInstance(8);
        inst.setDataset(test);

        inst.setValue(0, winnerRating[0]);
        inst.setValue(1, winSurf[0]);
        inst.setValue(2, winnerRating[2]);
        inst.setValue(3, loserRating[0]);
        inst.setValue(4, loseSurf[0]);
        inst.setValue(5, loserRating[2]);
//        inst.setValue(6, h2h);

        inst.setValue(7, Integer.toString(score));

        test.add(inst);
    }

    public Classifier train() throws Exception {
        Logistic mlp = new Logistic();
//        mlp.setLearningRate(0.1);
//        mlp.setMomentum(0.2);
//        mlp.setTrainingTime(2000);
//        mlp.setHiddenLayers("3");
        mlp.buildClassifier(dataset);

        return mlp;
    }

    public void eval(Classifier cls) throws Exception {
        Evaluation eval1 = new Evaluation(dataset);

        eval1.evaluateModel(cls, test);
        System.out.println("Set Predictions");
        System.out.println(eval1.toSummaryString("\nResults\n======\n", false));
    }


}
