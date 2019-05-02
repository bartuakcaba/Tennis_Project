/**
 * Created by bartu on 24/02/2019.
 */

public class Main {

    public static void main(String[] args) {

        Predictor p = new Predictor();
        int year = 2011;

        Rankings r = new Rankings(p);

        while (year < 2018) {
            r.readData(year, false);
            year++;
        }

        r.readData(year, true);

        System.out.print(p.calculateAccuracy());

    }
}
