/**
 * Created by bartu on 24/02/2019.
 */

public class Main {

    public static void main(String[] args) {

        DBHandler dbHandler = new DBHandler();

        int year = 1990;

        while (year < 2019) {
            System.out.println(year);
            dbHandler.fillH2HTable(year);
            year++;
        }


//        Predictor p = new Predictor();
//        int year = 1992;
//        String csvFile = "match_data/atp_matches_" + Integer.toString(year) + ".csv";
//
//        Rankings r = new Rankings(p);
//
//        while (year < 2018) {
//            r.readData(csvFile, false);
//            year++;
//        }
//
//        r.readData(csvFile, true);
//
//        System.out.print(p.calculateAccuracy());

    }
}
