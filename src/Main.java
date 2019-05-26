import weka.classifiers.Classifier;

import java.io.File;

/**
 * Created by bartu on 24/02/2019.
 */

public class Main {

    public static void main(String[] args) throws Exception {

        Predictor p = new Predictor();
        SetPredictor sp = new SetPredictor();
        int year = 2014;

        FileReader r = new FileReader(p, sp);

        while (year < 2018) {
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

        System.out.print(p.calculateAccuracy());

//        IndividualPredictor ip = new IndividualPredictor();
//        ip.getPrediction("De Minaur A.", "Klahn B.");
//        ip.getPrediction("Paire B.", "Copil M.");
//        ip.getPrediction("Krajinovic F.", "Tiafoe F.");
//        ip.getPrediction("Andreozzi G.", "Pella G.");
//        ip.getPrediction("Tsonga J.W.", "Gojowczyk P.");
//        ip.getPrediction("Kecmanovic M.", "Kudla D.");
//        ip.getPrediction("Zverev M.", "Gasquet R.");
//        ip.getPrediction("Basilashvili N.", "Londero J.I.");
//        ip.getPrediction("Carreno-Busta P.", "Sousa J.");
//        ip.getPrediction("Herbert P.H.", "Medvedev D.");
//        ip.getPrediction("Opelka R.", "Garin C.");
//        ip.getPrediction("Wawrinka S.", "Kovalik J.");
//        ip.getPrediction("Vatutin A.", "Moutet C.");
//        ip.getPrediction("Carballes Baena R.", "Muller A.");
//        ip.getPrediction("Hanfmann Y.", "Nadal R.");
//        ip.getPrediction("Maden Y.", "Coppejans K.");
//        ip.getPrediction("Bedene A.", "Coric B.");
//        ip.getPrediction("Hoang A.", "Dzumhur D.");
//        ip.getPrediction("Norrie C.", "Benchetrit E.");
//        ip.getPrediction("Shapovalov D.", "Struff J.L.");
//        ip.getPrediction("Thiem D.", "Paul T.");
//        ip.getPrediction("Fognini F.", "Seppi A.");
//        ip.getPrediction("Verdasco F.", "Evans D.");
//        ip.getPrediction("Barrere G.", "Ebden M.");
//        ip.getPrediction("Karlovic I.", "Lopez F.");
//        ip.getPrediction("Chardy J.", "Edmund K.");
//        ip.getPrediction("Zverev A.", "Millman J.");
//        ip.getPrediction("Khachanov K.", "Stebe C.M.");
//        ip.getPrediction("Klizan M.", "Kukushkin M.");
//        ip.getPrediction("Janvier M.", "Cuevas P.");
//        ip.getPrediction("Jarry N.", "Del Potro J.M.");
//        ip.getPrediction("Djokovic N.", "Hurkacz H.");
//        ip.getPrediction("Johnson S.", "Bautista Agut R.");
//        ip.getPrediction("Daniel T.", "Monfils G.");
//        ip.getPrediction("Fritz T.", "Tomic B.");
//        ip.getPrediction("Nishioka Y.", "Mcdonald M.");
//        ip.getPrediction("Lajovic D.", "Monteiro T.");
//        ip.getPrediction("Garcia-Lopez G.", "Delbonis F.");
//        ip.getPrediction("Martinez P.", "Laaksonen H.");
//        ip.getPrediction("Munar J.", "Caruso S.");
//        ip.getPrediction("Harris L.", "Rosol L.");
//        ip.getPrediction("Ymer M.", "Rola B.");
//        ip.getPrediction("Albot R.", "Sandgren T.");
//        ip.getPrediction("Molleker R.", "Bublik A.");
//        ip.getPrediction("Stakhovsky S.", "Simon G.");
//        ip.getPrediction("Bolelli S.", "Pouille L.");
//        ip.getPrediction("Travaglia S.", "Mannarino A.");

    }
}
