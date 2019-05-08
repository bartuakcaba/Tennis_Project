/**
 * Created by bartu on 28/04/2019.
 */

import com.sun.tools.doclint.HtmlTag;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.Logistic;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Predictor {

    private int correctGuesses;
    private int allGuesses;
    private DBHandler dbHandler;
    List<Double[]> predList;
    Instances dataset;
    Instances test;


    public Predictor() {
        correctGuesses = 0;
        allGuesses = 0;
        dbHandler = new DBHandler();
        predList = new ArrayList<>();

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
        Attribute att7 = new Attribute("class", new ArrayList<String>(Arrays.asList(new String[] {"0", "1"})));
        attrs.add(att7);

        dataset = new Instances("train", attrs, 10000);
        dataset.setClassIndex(dataset.numAttributes()-1);
        test = new Instances("test", attrs, 10000);
        test.setClassIndex(test.numAttributes()-1);

    }

    public void addToDataset(Double[] winningPlayer, Double[] losingPlayer) {
        Instance inst = new DenseInstance(7);
        inst.setDataset(dataset);

        Double[] higher = winningPlayer[0] > losingPlayer[0] ? winningPlayer : losingPlayer;
        Double[] lower = winningPlayer[0] > losingPlayer[0] ? losingPlayer : winningPlayer;

        inst.setValue(0, higher[0]);
        inst.setValue(1, higher[1]);
        inst.setValue(2, higher[2]);
        inst.setValue(3, lower[0]);
        inst.setValue(4, lower[1]);
        inst.setValue(5, lower[2]);
        inst.setValue(6, higher.equals(winningPlayer) ? "1" : "0");


        dataset.add(inst);
    }

    public void addToTest(Double[] winningPlayer, Double[] losingPlayer) {
        Instance inst = new DenseInstance(7);
        inst.setDataset(test);

        Double[] higher = winningPlayer[0] > losingPlayer[0] ? winningPlayer : losingPlayer;
        Double[] lower = winningPlayer[0] > losingPlayer[0] ? losingPlayer : winningPlayer;

        inst.setValue(0, higher[0]);
        inst.setValue(1, higher[1]);
        inst.setValue(2, higher[2]);
        inst.setValue(3, lower[0]);
        inst.setValue(4, lower[1]);
        inst.setValue(5, lower[2]);
        inst.setValue(6, higher.equals(winningPlayer) ? "1" : "0");

        test.add(inst);
    }

    public Classifier trainClassifier() throws Exception {
        String[] options = new String[1];
        Logistic tree = new Logistic();         // new instance of tree
        tree.buildClassifier(dataset);

        return tree;
    }

    public void eval(Classifier cls) throws Exception {
        Evaluation eval = new Evaluation(dataset);
        eval.evaluateModel(cls, test);
        System.out.println(eval.toSummaryString("\nResults\n======\n", false));
    }


    public void predictSingleMatch(Double[] winnerRatings, Double[] loserRatings) {
        Double[] toWritePred = new Double[5];

        Double[] higher = winnerRatings[0] > loserRatings[0] ? winnerRatings : loserRatings;
        Double[] lower = winnerRatings[0] > loserRatings[0] ? loserRatings : winnerRatings;

        toWritePred[0] = winnerRatings[0];
        toWritePred[1] = winnerRatings[1];
        toWritePred[2] = loserRatings[0];
        toWritePred[3] =loserRatings[1];
        toWritePred[4] = 0.0;

        predList.add(toWritePred);

//        if (higher[0] - lower[0] <= 100) {
//            if (higher[0] - higher[1] > lower[0] - lower[1]) {
//                if (higher.equals(winnerRatings)) {
//                    correctGuesses++;
//                    toWritePred[4] = 1.0;
//                }
//            } else {
//                if (lower.equals(winnerRatings)) {
//                    correctGuesses++;
//                    toWritePred[4] = 1.0;
//                }
//            }
//        } else {
//            if (higher.equals(winnerRatings)) {
//                correctGuesses++;
//                toWritePred[4] = 1.0;
//            }
//        }

        if (higher.equals(winnerRatings)) {
            correctGuesses++;
            toWritePred[4] = 1.0;
        }



        allGuesses++;
    }


    public void predictWithMulRatings(Double[] winnerRatings, Double[] loserRatings, Double[] winnerSurf, Double[] loserSurf) {
        Double[] toWritePred = new Double[5];

        Double[] higher = winnerRatings[0] > loserRatings[0] ? winnerRatings : loserRatings;
        Double[] lower = winnerRatings[0] > loserRatings[0] ? loserRatings : winnerRatings;
        Double[] predicted;


        toWritePred[0] = winnerRatings[0];
        toWritePred[1] = winnerRatings[1];
        toWritePred[2] = loserRatings[0];
        toWritePred[3] =loserRatings[1];
        toWritePred[4] = 0.0;

        predList.add(toWritePred);

        if (higher[0] - lower[0] > 200) {
            predicted = higher;
        } else {
            if (higher[0] + higher[1] > lower[0] +lower[1] && higher[0] - higher[1] > lower[0] - lower[1]) {
                predicted = higher;
            } else {
                if (higher[2] > lower[2]) {
                    predicted = lower;
                } else {
                    predicted = higher;
                }
            }

        }

        if (predicted.equals(winnerRatings)) {
            correctGuesses++;
            toWritePred[4] = 1.0;
        }

//        if (higher[0] - lower[0] <= 100) {
//            if (higher[0] - higher[1] > lower[0] - lower[1]) {
//                if (higher.equals(winnerRatings)) {
//                    correctGuesses++;
//                    toWritePred[4] = 1.0;
//                }
//            } else {
//                if (lower.equals(winnerRatings)) {
//                    correctGuesses++;
//                    toWritePred[4] = 1.0;
//                }
//            }
//        } else {
//            if (higher.equals(winnerRatings)) {
//                correctGuesses++;
//                toWritePred[4] = 1.0;
//            }
//        }

        allGuesses++;
    }

    public double calculateAccuracy() {
        return ((double) correctGuesses/allGuesses) * 100;
    }
}
