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
 * Created by bartu on 20/05/2019.
 */
public class IndividualPredictor {

    String ratingsFile = "GenevaRatings.xls";
    String surfRatingsFile = "GenevaClayRatings.xls";
    Classifier mlp;
    Instances test;
    DBHandler dbhandler;

    public IndividualPredictor() throws Exception {
        mlp = (Classifier) weka.core.SerializationHelper.read("geneva_mlp.model");
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
        Attribute att13 = new Attribute("h2h");
        attrs.add(att13);
        Attribute att14 = new Attribute("class", new ArrayList<>(Arrays.asList(new String[] {"0", "1"})));
        attrs.add(att14);

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

            for (int row = 0; row < totalNoOfRows; row++) {

                if(sh.getCell(1, row).getContents().equals(player1)) {
                    p1Rating[0] = Double.parseDouble(sh.getCell(2, row).getContents());
                    p1Rating[1] = Double.parseDouble(sh.getCell(3, row).getContents());
                    p1Rating[2] = Double.parseDouble(sh.getCell(4, row).getContents());
                }

                if(sh.getCell(1, row).getContents().equals(player2)) {
                    p2Rating[0] = Double.parseDouble(sh.getCell(2, row).getContents());
                    p2Rating[1] = Double.parseDouble(sh.getCell(3, row).getContents());
                    p2Rating[2] = Double.parseDouble(sh.getCell(4, row).getContents());
                }

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

            double h2h = dbhandler.getH2H(player1, player2);
            h2h = p1Rating[0] > p2Rating[0] ? h2h : 1-h2h;


            Evaluation evaluation = new Evaluation(test);
            evaluation.evaluateModelOnceAndRecordPrediction(mlp, createInstance(p1Rating, p2Rating, surfp1Rating, surfp2Rating, h2h));

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
                        System.out.println("Winner should be " + higher);
                    } else {
                        System.out.println("Winner should be " + lower);
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

    private Instance createInstance(double[] p1, double[] p2, double[] p1Surf, double[] p2Surf, double h2h) {
        Instance inst = new DenseInstance(14);
        inst.setDataset(test);

        double[] higher = p1[0] > p2[0] ? p1 : p2;
        double[] lower = p1[0] > p2[0] ? p2 : p1;

        if (p1[0] > p2[0]) {
            inst.setValue(6, p1Surf[0]);
            inst.setValue(9, p2Surf[0]);
        } else {
            inst.setValue(6, p2Surf[0]);
            inst.setValue(9, p1Surf[0]);
        }

        inst.setValue(0, higher[0]);
        inst.setValue(1, higher[1]);

        inst.setValue(3, lower[0]);
        inst.setValue(4, lower[1]);

        inst.setValue(12, h2h);

        test.add(inst);

        return inst;
    }
}
