import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.trees.RandomForest;
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
        Attribute att7 = new Attribute("higher_surface_r");
        attrs.add(att7);
        Attribute att8 = new Attribute("higher_surface_rd");
        attrs.add(att8);
        Attribute att9 = new Attribute("higher_surface_vol");
        attrs.add(att9);
        Attribute att10 = new Attribute("lower_surface_rating");
        attrs.add(att10);
        Attribute att11 = new Attribute("lower_surface_rd");
        attrs.add(att11);
        Attribute att12 = new Attribute("lower_surface_vol");
        attrs.add(att12);
        Attribute att13 = new Attribute("h2h");
        attrs.add(att13);
        Attribute att14 = new Attribute("higherTitles");
        attrs.add(att14);
        Attribute att15 = new Attribute("lowerTitles");
        attrs.add(att15);
        Attribute att16 = new Attribute("higherAge");
        attrs.add(att16);
        Attribute att17 = new Attribute("lowerAge");
        attrs.add(att17);
        Attribute att18 = new Attribute("higherMomentum");
        attrs.add(att18);
        Attribute att19 = new Attribute("lowerMomentum");
        attrs.add(att19);
        Attribute att20 = new Attribute("higherCountry");
        attrs.add(att20);
        Attribute att21 = new Attribute("lowerCountry");
        attrs.add(att21);
        Attribute att22 = new Attribute("class", new ArrayList<>(Arrays.asList(new String[] {"-3", "-2", "2", "3"})));
        attrs.add(att22);

        dataset = new Instances("train", attrs, 10000);
        dataset.setClassIndex(dataset.numAttributes()-1);
        test = new Instances("test", attrs, 10000);
        test.setClassIndex(test.numAttributes()-1);
    }

    public void addToDataset(Double[] winningPlayer, Double[] losingPlayer, Double[] winSurface, Double[] loseSurface, Double h2h,
                             int higherTitles, int lowerTitles, double higherAge, double lowerAge,
                             double higherMomentum, double lowerMomentum, int higherCountry, int lowerCountry, int score) {

        if (score != 2 && score != 3 && score != -2 && score != -3) {
            return;
        }

        Instance inst = new DenseInstance(22);
        inst.setDataset(dataset);

        Double[] higher = winningPlayer[0]  > losingPlayer[0] ? winningPlayer : losingPlayer;
        Double[] lower = winningPlayer[0] > losingPlayer[0] ? losingPlayer : winningPlayer;

        if (winningPlayer[0] > losingPlayer[0]) {
            inst.setValue(6, winSurface[0]);
            inst.setValue(7, winSurface[1]);
            inst.setValue(8, winSurface[2]);
            inst.setValue(9, loseSurface[0]);
            inst.setValue(10, loseSurface[1]);
            inst.setValue(11, loseSurface[2]);
        } else {
            inst.setValue(6, loseSurface[0]);
            inst.setValue(7, loseSurface[1]);
            inst.setValue(8, loseSurface[2]);
            inst.setValue(9, winSurface[0]);
            inst.setValue(10, winSurface[1]);
            inst.setValue(11, winSurface[2]);
        }

        inst.setValue(0, higher[0]);
        inst.setValue(1, higher[1]);
        inst.setValue(2, higher[2]);
        inst.setValue(3, lower[0]);
        inst.setValue(4, lower[1]);
        inst.setValue(5, lower[2]);
        inst.setValue(12, h2h);
        inst.setValue(13, higherTitles);
        inst.setValue(14, lowerTitles);
        inst.setValue(15, higherAge);
        inst.setValue(16, lowerAge);
        inst.setValue(17, higherMomentum);
        inst.setValue(18, lowerMomentum);
//        inst.setValue(19, higherCountry);
//        inst.setValue(20, lowerCountry);

        inst.setValue(21, Integer.toString(score));

        dataset.add(inst);
    }

    public void addToTest(Double[] winningPlayer, Double[] losingPlayer, Double[] winSurface, Double[] loseSurface, Double h2h,
                          int higherTitles, int lowerTitles, double higherAge, double lowerAge,
                          double higherMomentum, double lowerMomentum, int higherCountry, int lowerCountry, int score) {

        if (score != 2 && score != 3 && score != -2 && score != -3) {
            return;
        }


        Instance inst = new DenseInstance(22);
        inst.setDataset(test);

        Double[] higher = winningPlayer[0]  > losingPlayer[0] ? winningPlayer : losingPlayer;
        Double[] lower = winningPlayer[0] > losingPlayer[0] ? losingPlayer : winningPlayer;

        if (winningPlayer[0] > losingPlayer[0]) {
            inst.setValue(1, winSurface[0]);
            inst.setValue(7, winSurface[1]);
            inst.setValue(8, winSurface[2]);
            inst.setValue(4, loseSurface[0]);
            inst.setValue(10, loseSurface[1]);
            inst.setValue(11, loseSurface[2]);
        } else {
            inst.setValue(1, loseSurface[0]);
            inst.setValue(7, loseSurface[1]);
            inst.setValue(8, loseSurface[2]);
            inst.setValue(4, winSurface[0]);
            inst.setValue(10, winSurface[1]);
            inst.setValue(11, winSurface[2]);
        }

        inst.setValue(0, higher[0]);
        inst.setValue(1, higher[1]);
        inst.setValue(2, higher[2]);
        inst.setValue(3, lower[0]);
        inst.setValue(4, lower[1]);
        inst.setValue(5, lower[2]);
        inst.setValue(12, h2h);
        inst.setValue(13, higherTitles);
        inst.setValue(14, lowerTitles);
        inst.setValue(15, higherAge);
        inst.setValue(16, lowerAge);
        inst.setValue(17, higherMomentum);
        inst.setValue(18, lowerMomentum);
//        inst.setValue(19, higherCountry);
//        inst.setValue(20, lowerCountry);

        inst.setValue(21, Integer.toString(score));

        test.add(inst);
    }

    public Classifier train() throws Exception {
        RandomForest classifier = new RandomForest();
        classifier.setMaxDepth(10);
        classifier.setNumFeatures(10);
        classifier.setBreakTiesRandomly(false);
        classifier.setNumIterations(100);
        classifier.buildClassifier(dataset);
//        MultilayerPerceptron classifier = new MultilayerPerceptron();
//        classifier.setLearningRate(0.1);
//        classifier.setMomentum(0.2);
//        classifier.setTrainingTime(1500);
//        classifier.setHiddenLayers("1");
//        classifier.buildClassifier(dataset);
        weka.core.SerializationHelper.write("grass.model", classifier);

        return classifier;
    }

    public void eval(Classifier cls) throws Exception {
        Evaluation eval1 = new Evaluation(dataset);

        eval1.evaluateModel(cls, test);
        System.out.println("Set Predictions");
        System.out.println(eval1.toSummaryString("\nResults\n======\n", false));
    }


}
