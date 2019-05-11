/**
 * Created by bartu on 28/04/2019.
 */

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import weka.classifiers.*;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.SMO;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.*;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Predictor {

    private int correctGuesses;
    private int allGuesses;
    List<double[]> predList;
    List<String[]> names;
    Instances dataset;
    Instances dataset2;
    Instances test;
    Instances test2;
    private CSVWriter writer;
    private Classifier classifier;


    public Predictor() {
        correctGuesses = 0;
        allGuesses = 0;
        predList = new ArrayList<>();
        names = new ArrayList<>();

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
        Attribute att14 = new Attribute("class", new ArrayList<String>(Arrays.asList(new String[] {"0", "1"})));
        attrs.add(att14);

        dataset = new Instances("train", attrs, 10000);
        dataset.setClassIndex(dataset.numAttributes()-1);
        dataset2 = new Instances("train", attrs, 10000);
        dataset2.setClassIndex(dataset.numAttributes()-1);
        test = new Instances("test", attrs, 10000);
        test.setClassIndex(test.numAttributes()-1);
        test2 = new Instances("test", attrs, 10000);
        test2.setClassIndex(test.numAttributes()-1);

        writer = new CSVWriter();

    }

    public void addToDataset(Double[] winningPlayer, Double[] losingPlayer, Double[] winSurface, Double[] loseSurface, double h2h) {
        Instance inst = new DenseInstance(14);
        Instance inst2 = new DenseInstance(14);
        inst.setDataset(dataset);
        inst2.setDataset(dataset2);

        Double[] higher = winningPlayer[0] > losingPlayer[0] ? winningPlayer : losingPlayer;
        Double[] lower = winningPlayer[0] > losingPlayer[0] ? losingPlayer : winningPlayer;

        if (winningPlayer[0] > losingPlayer[0]) {
            inst.setValue(6, winSurface[0]);
//            inst.setValue(7, winSurface[1]);
            inst.setValue(8, winSurface[2]);
            inst.setValue(9, loseSurface[0]);
//            inst.setValue(10, loseSurface[1]);
            inst.setValue(11, loseSurface[2]);
            inst2.setValue(6, winSurface[0]);
            inst2.setValue(7, winSurface[1]);
            inst2.setValue(8, winSurface[2]);
            inst2.setValue(9, loseSurface[0]);
            inst2.setValue(10, loseSurface[1]);
            inst2.setValue(11, loseSurface[2]);
        } else {
            inst.setValue(6, loseSurface[0]);
//            inst.setValue(7, loseSurface[1]);
            inst.setValue(8, loseSurface[2]);
            inst.setValue(9, winSurface[0]);
//            inst.setValue(10, winSurface[1]);
            inst.setValue(11, winSurface[2]);
            inst2.setValue(6, loseSurface[0]);
            inst2.setValue(7, loseSurface[1]);
            inst2.setValue(8, loseSurface[2]);
            inst2.setValue(9, winSurface[0]);
            inst2.setValue(10, winSurface[1]);
            inst2.setValue(11, winSurface[2]);
        }

        inst.setValue(0, higher[0]);
//        inst.setValue(1, higher[1]);
        inst.setValue(2, higher[2]);
        inst.setValue(3, lower[0]);
//        inst.setValue(4, lower[1]);
        inst.setValue(5, lower[2]);
        inst.setValue(12, h2h);
        inst2.setValue(0, higher[0]);
        inst2.setValue(1, higher[1]);
        inst2.setValue(2, higher[2]);
        inst2.setValue(3, lower[0]);
        inst2.setValue(4, lower[1]);
        inst2.setValue(5, lower[2]);

        inst.setValue(13, higher.equals(winningPlayer) ? "1" : "0");
        inst2.setValue(13, higher.equals(winningPlayer) ? "1" : "0");


        dataset.add(inst);
        dataset2.add(inst2);
    }

    public void addToTest(Double[] winningPlayer, Double[] losingPlayer, Double[] winSurface, Double[] loseSurface, Double h2h) {
        Instance inst = new DenseInstance(14);
        inst.setDataset(test);
        Instance inst2 = new DenseInstance(14);
        inst2.setDataset(test2);

        Double[] higher = winningPlayer[0] > losingPlayer[0] ? winningPlayer : losingPlayer;
        Double[] lower = winningPlayer[0] > losingPlayer[0] ? losingPlayer : winningPlayer;

        if (winningPlayer[0] > losingPlayer[0]) {
            inst.setValue(6, winSurface[0]);
//            inst.setValue(7, winSurface[1]);
            inst.setValue(8, winSurface[2]);
            inst.setValue(9, loseSurface[0]);
//            inst.setValue(10, loseSurface[1]);
            inst.setValue(11, loseSurface[2]);
            inst2.setValue(6, winSurface[0]);
            inst2.setValue(7, winSurface[1]);
            inst2.setValue(8, winSurface[2]);
            inst2.setValue(9, loseSurface[0]);
            inst2.setValue(10, loseSurface[1]);
            inst2.setValue(11, loseSurface[2]);
        } else {
            inst.setValue(6, loseSurface[0]);
//            inst.setValue(7, loseSurface[1]);
            inst.setValue(8, loseSurface[2]);
            inst.setValue(9, winSurface[0]);
//            inst.setValue(10, winSurface[1]);
            inst.setValue(11, winSurface[2]);
            inst2.setValue(6, loseSurface[0]);
            inst2.setValue(7, loseSurface[1]);
            inst2.setValue(8, loseSurface[2]);
            inst2.setValue(9, winSurface[0]);
            inst2.setValue(10, winSurface[1]);
            inst2.setValue(11, winSurface[2]);
        }

        inst.setValue(0, higher[0]);
//        inst.setValue(1, higher[1]);
        inst.setValue(2, higher[2]);
        inst.setValue(3, lower[0]);
//        inst.setValue(4, lower[1]);
        inst.setValue(5, lower[2]);
        inst.setValue(12, h2h);
        inst2.setValue(0, higher[0]);
        inst2.setValue(1, higher[1]);
        inst2.setValue(2, higher[2]);
        inst2.setValue(3, lower[0]);
        inst2.setValue(4, lower[1]);
        inst2.setValue(5, lower[2]);

        inst.setValue(13, higher.equals(winningPlayer) ? "1" : "0");
        inst2.setValue(13, higher.equals(winningPlayer) ? "1" : "0");

        test.add(inst);
        test2.add(inst2);
    }

    public Classifier trainClassifier() throws Exception {
        Logistic classifier = new Logistic();
        classifier.buildClassifier(dataset);

        return classifier;
    }

    public void eval(Classifier cls) throws Exception {
        Evaluation eval1 = new Evaluation(dataset);
        Evaluation eval2 = new Evaluation(dataset2);

        eval1.evaluateModel(cls, test);
        System.out.println(eval1.toSummaryString("\nResults\n======\n", false));

        eval2.evaluateModel(cls, test2);
        System.out.println(eval2.toSummaryString("\nResults\n======\n", false));
    }

    public void predOneByOne(Double[] winningPlayer, Double[] losingPlayer, Double[] winSurface, Double[] loseSurface,
                             Double h2h, String winner, String loser, String tourny) throws Exception {
        Instance inst = new DenseInstance(14);
        inst.setDataset(test);
        Instance inst2 = new DenseInstance(14);
        inst2.setDataset(test2);

        boolean high = winningPlayer[0] > losingPlayer[0];
        boolean high_surf = winSurface[0] > loseSurface[0];

        Double[] higher = high ? winningPlayer : losingPlayer;
        Double[] lower = high ? losingPlayer : winningPlayer;

        if (high) {
            inst.setValue(6, winSurface[0]);
            inst.setValue(7, winSurface[1]);
            inst.setValue(8, winSurface[2]);
            inst.setValue(9, loseSurface[0]);
            inst.setValue(10, loseSurface[1]);
            inst.setValue(11, loseSurface[2]);
            inst2.setValue(6, winSurface[0]);
            inst2.setValue(7, winSurface[1]);
            inst2.setValue(8, winSurface[2]);
            inst2.setValue(9, loseSurface[0]);
            inst2.setValue(10, loseSurface[1]);
            inst2.setValue(11, loseSurface[2]);
        } else {
            inst.setValue(6, loseSurface[0]);
            inst.setValue(7, loseSurface[1]);
            inst.setValue(8, loseSurface[2]);
            inst.setValue(9, winSurface[0]);
            inst.setValue(10, winSurface[1]);
            inst.setValue(11, winSurface[2]);
            inst2.setValue(6, loseSurface[0]);
            inst2.setValue(7, loseSurface[1]);
            inst2.setValue(8, loseSurface[2]);
            inst2.setValue(9, winSurface[0]);
            inst2.setValue(10, winSurface[1]);
            inst2.setValue(11, winSurface[2]);
        }

        inst.setValue(0, higher[0]);
        inst.setValue(1, higher[1]);
        inst.setValue(2, higher[2]);
        inst.setValue(3, lower[0]);
        inst.setValue(4, lower[1]);
        inst.setValue(5, lower[2]);
        inst.setValue(12, h2h);
        inst2.setValue(0, higher[0]);
        inst2.setValue(1, higher[1]);
        inst2.setValue(2, higher[2]);
        inst2.setValue(3, lower[0]);
        inst2.setValue(4, lower[1]);
        inst2.setValue(5, lower[2]);

        inst.setValue(13, higher.equals(winningPlayer) ? "1" : "0");
        inst2.setValue(13, higher.equals(winningPlayer) ? "1" : "0");

        test.add(inst);
        test2.add(inst2);

        Evaluation eval1 = new Evaluation(dataset);
        Evaluation eval2 = new Evaluation(dataset2);

            double pred1 = eval1.evaluateModelOnce(classifier, inst);
            double pred2 = eval2.evaluateModelOnce(classifier, inst2);
        if (pred1 != inst.value(13)) {
            predList.add(new double[]{inst.value(0), inst.value(3), inst.value(6), inst.value(9), inst.value(12)});
            if (high) {
                if (high_surf) {
                    names.add(new String[]{winner, loser, tourny, winner, loser, winner});
                } else {
                    names.add(new String[]{winner, loser, tourny, winner, loser, loser});
                }
            } else {
                if (high_surf) {
                    names.add(new String[]{loser, winner, tourny, winner, loser, winner});
                } else {
                    names.add(new String[]{loser, winner, tourny, winner, loser, loser});
                }
            }
        }

    }

    public void writepredictions() {
        writer.writeAllPredictions(predList, names);
    }

    public void setClassifier(Classifier classifier) {
        this.classifier = classifier;
    }



    public void predictSingleMatch(Double[] winnerRatings, Double[] loserRatings) {
        double[] toWritePred = new double[5];

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
