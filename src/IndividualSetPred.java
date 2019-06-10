import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.NominalPrediction;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by bartu on 10/06/2019.
 */
public class IndividualSetPred {

    String ratingsFile = "grass_set/ratings.xls";
    String surfRatingsFile = "grass_set/grass_ratings.xls";
    Classifier mlp;
    Instances test;
    DBHandler dbhandler;

    public IndividualSetPred() throws Exception {
        mlp = (Classifier) weka.core.SerializationHelper.read("RG/RG_mlp.model");
        dbhandler = new DBHandler();

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
        Attribute att13 = new Attribute("class", new ArrayList<>(Arrays.asList(new String[] {"0", "1"})));
        attrs.add(att13);

        test = new Instances("test", attrs, 10000);
        test.setClassIndex(test.numAttributes()-1);
    }

    public void getPrediction(String player1, String player2) {
        FileInputStream fs = null;
        try {
            fs = new FileInputStream(ratingsFile);
            Workbook wb = Workbook.getWorkbook(fs);

            Sheet sh = wb.getSheet("new sheet");
            int totalNoOfRows = sh.getRows();

            double[] p1Rating = new double[]{1500.0, 350.0, 0.06};
            double[] p2Rating = new double[]{1500.0, 350.0, 0.06};
            int p1Momentum = 0;
            int p2Momentum = 0;
            int p1Titles = 0;
            int p2Titles = 0;
            int p1Age = dbhandler.getAge(player1);
            int p2Age = dbhandler.getAge(player2);
            double h2h = dbhandler.getH2H(player1, player2);

            for (int row = 0; row < totalNoOfRows; row++) {

                if(sh.getCell(1, row).getContents().equals(player1)) {
                    p1Rating[0] = Double.parseDouble(sh.getCell(1, row).getContents());
                    p1Rating[1] = Double.parseDouble(sh.getCell(2, row).getContents());
                    p1Rating[2] = Double.parseDouble(sh.getCell(3, row).getContents());
                    p1Momentum = Integer.parseInt(sh.getCell(4, row).getContents());
                    p1Titles = Integer.parseInt(sh.getCell(5, row).getContents());
                }

                if(sh.getCell(1, row).getContents().equals(player2)) {
                    p2Rating[0] = Double.parseDouble(sh.getCell(1, row).getContents());
                    p2Rating[1] = Double.parseDouble(sh.getCell(2, row).getContents());
                    p2Rating[2] = Double.parseDouble(sh.getCell(3, row).getContents());
                    p2Momentum = Integer.parseInt(sh.getCell(4, row).getContents());
                    p2Titles = Integer.parseInt(sh.getCell(5, row).getContents());
                }

            }

            int higherTitles;
            int lowerTitles;
            int higherAge;
            int lowerAge;
            int higherMomentum;
            int lowerMomentum;

            if (p1Rating[0] < p2Rating[0]) {
                higherTitles = p1Titles;
                lowerTitles = p2Titles;
                higherAge = p1Age;
                lowerAge = p2Age;
                higherMomentum = p1Momentum;
                lowerMomentum = p2Momentum;
            } else {
                higherTitles = p2Titles;
                lowerTitles = p1Titles;
                higherAge = p2Age;
                lowerAge = p1Age;
                higherMomentum = p2Momentum;
                lowerMomentum = p1Momentum;
                h2h = 1-h2h;
            }


            fs = new FileInputStream(surfRatingsFile);
            wb = Workbook.getWorkbook(fs);

            sh = wb.getSheet("new sheet");
            totalNoOfRows = sh.getRows();

            double[] surfp1Rating = new double[]{1500.0, 350.0, 0.06};
            double[] surfp2Rating = new double[]{1500.0, 350.0, 0.06};

            for (int row = 0; row < totalNoOfRows; row++) {

                if(sh.getCell(1, row).getContents().equals(player1)) {
                    surfp1Rating[0] = Double.parseDouble(sh.getCell(2, row).getContents());
                    surfp1Rating[1] = Double.parseDouble(sh.getCell(3, row).getContents());
                    surfp1Rating[2] = Double.parseDouble(sh.getCell(4, row).getContents());
                }

                if(sh.getCell(1, row).getContents().equals(player2)) {
                    surfp2Rating[0] = Double.parseDouble(sh.getCell(2, row).getContents());
                    surfp2Rating[1] = Double.parseDouble(sh.getCell(3, row).getContents());
                    surfp2Rating[2] = Double.parseDouble(sh.getCell(4, row).getContents());
                }

            }




            Evaluation evaluation = new Evaluation(test);
            evaluation.evaluateModelOnceAndRecordPrediction(mlp, createInstance(p1Rating, p2Rating, surfp1Rating, surfp2Rating,
                    h2h, higherAge, lowerAge, higherTitles, lowerTitles, higherMomentum, lowerMomentum));

            for (Object o : evaluation.predictions().toArray())
            {
                NominalPrediction prediction = (NominalPrediction) o;
                if (prediction != null)
                {
                    double[] distribution = prediction.distribution();
                    double predicted = prediction.predicted();

                    String higher;
                    String lower;

                    higher =  p1Rating[0] > p2Rating[0] ? player1 : player2;
                    lower =  p1Rating[0] > p2Rating[0] ? player2 : player1;

                    if (predicted == 1.0) {
                        System.out.println("Winner should be " + higher + " against " + lower);
                    } else {
                        System.out.println("Winner should be " + lower + " against " + higher);
                    }
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Instance createInstance(double[] p1, double[] p2, double[] p1Surf, double[] p2Surf, Double h2h,
                                    int higherTitles, int lowerTitles, int higherAge, int lowerAge,
                                    int higherMomentum, int lowerMomentum) {
        Instance inst = new DenseInstance(22);
        inst.setDataset(test);

        double[] higher = p1[0] > p2[0] ? p1 : p2;
        double[] lower = p1[0] > p2[0] ? p2 : p1;

        if (p1[0] > p2[0]) {
            inst.setValue(6, p1Surf[0]);
            inst.setValue(7, p1Surf[1]);
            inst.setValue(8, p1Surf[2]);
            inst.setValue(9, p2Surf[0]);
            inst.setValue(10, p2Surf[1]);
            inst.setValue(11, p2Surf[2]);
        } else {
            inst.setValue(6, p2Surf[0]);
            inst.setValue(7, p2Surf[1]);
            inst.setValue(8, p2Surf[2]);
            inst.setValue(9, p1Surf[0]);
            inst.setValue(10, p1Surf[1]);
            inst.setValue(11, p1Surf[2]);
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


        test.add(inst);

        return inst;
    }
}
