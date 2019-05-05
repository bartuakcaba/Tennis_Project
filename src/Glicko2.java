import java.util.LinkedList;
import java.util.List;

/**
 * Created by bartu on 17/04/2019.
 * This is the Glicko-2 Calculator
 */
public class Glicko2 {


    private static double SYS_CONSTANT  = 0.3; //Between 0.3 and 1.2
    private static double CONV_TOLERANCE = 0.000001;

    public Double[] convertGlicko2(Double[] rating) {

        double r = rating[0];
        double RD = rating[1];

        rating[0] = (r-1500)/173.7178;
        rating[1] = RD/173.7178;

        return rating;
    }

    public List<Double[]> convertGlicko2List(List<Double[]> ratings) {

        List<Double[]> newRatings = new LinkedList<>();

        for (Double[] rating : ratings) {
            newRatings.add(convertGlicko2(rating));
        }

        return newRatings;
    }

    public double calcV(Double[] rating, List<Double[]> rivalRatings) {

        double sum = 0;

        for (Double[] arr : rivalRatings) {
            double rivalG = Math.pow(calcG(arr[1]), 2);
            double E = calcE(rating[0],arr[0],arr[1]);

            sum += Math.pow((rivalG * E * (1-E)), -1);
        }

        return sum;
    }

    private double calcG(double der) {
        return (1 / Math.sqrt((1 + 3*Math.pow(der, 2)/Math.pow(Math.PI, 2))));
    }

    private double calcE(double rating, double rivalRating, double rivalDer) {

        return 1 / (1 + Math.exp(-calcG(rivalDer) * (rating - rivalRating)));
    }


    public double calcEstImprovement(Double V, List<Double> results, Double[] rating, List<Double[]> rivalRatings) {

        double sum = 0;
        int i = 0;

        for (Double[] arr : rivalRatings) {
            sum += (calcG(arr[1]) * (results.get(i) - calcE(rating[0],arr[0],arr[1])));
            i++;
        }

        return (V * sum);
    }

    public double calcNewVol(double RD, double estImprovement, double V, double vol) {

        double a = Math.log(Math.pow(vol,2));
        double A = a;
        double B;

        if (Math.pow(estImprovement, 2) > Math.pow(RD, 2) + V) {
            B = Math.log(Math.pow(estImprovement, 2) - Math.pow(RD, 2) - V);
        } else  {
            int k = 1;
            while (volatilityUtility(a, (a-k*SYS_CONSTANT), RD, estImprovement, V) < 0) {
                k++;
            }
            B = a - k*SYS_CONSTANT;
        }

        double fA = volatilityUtility(a, A, RD, estImprovement, V);
        double fB = volatilityUtility(a, B, RD, estImprovement, V);

        while (Math.abs(B-A) > CONV_TOLERANCE) {
            double C  = A + (((A-B) * fA) / (fB - fA));
            double fC = volatilityUtility(a, C, RD, estImprovement, V);

            if (fC * fB < 0) {
                A = B;
                fA = fB;
            } else {
                fA = fA/2;
            }

            B = C;
            fB = fC;
        }

        return Math.exp(A/2); // This is the new volatility
    }

    private double volatilityUtility(double a, double input, double RD, double estImprovement, double V) {
        double firstPart =  (Math.exp(input) * (Math.pow(estImprovement, 2) - Math.pow(RD, 2) - V - Math.exp(input)))
                / (2 * Math.pow(Math.pow(RD, 2) + V + Math.exp(input),2));

        double secondPart = (input-a) / Math.pow(SYS_CONSTANT, 2);

        return firstPart - secondPart;
    }

    public double updateRD(double RD, double newVol) {

        return Math.sqrt(Math.pow(RD,2) + Math.pow(newVol,2));

    }

    public Double[] updateRating(Double V, List<Double> results, Double[] rating, List<Double[]> rivalRatings, double updatedRD, double newVol) {

        rating[1] = 1 / Math.sqrt((1/Math.pow(updatedRD, 2)) + (1/V));

        double oldRating = rating[0];

        double sum = 0;
        int i = 0;

        for (Double[] arr : rivalRatings) {

            sum += (calcG(arr[1]) * (results.get(i) - calcE(oldRating,arr[0],arr[1])));
            i++;
        }

        rating[0] = oldRating + Math.pow(rating[1],2) * sum;
        rating[2] = newVol;

        return rating;
    }

    public Double[] convertBack(Double[] rating) {

        double r = rating[0];
        double RD = rating[1];

        rating[0] = 173.7178*r + 1500;
        rating[1] = 173.7178*RD;

        return rating;
    }


    //If player hasn't played this week then we only update RD

    public Double[] updateUnplayedPlayer(Double[] rating) {

        double oldRD = rating[1];
        double oldRating = rating[0];

        rating[1] = Math.sqrt(Math.pow(oldRD,2) + Math.pow(rating[2],2));
        //Need to decay ratings
//        rating[0] = oldRating *0.99;

        return rating;

    }
}
