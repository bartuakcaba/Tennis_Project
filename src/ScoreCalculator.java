import org.apache.commons.lang3.StringUtils;

/**
 * Created by bartu on 01/05/2019.
 */
public class ScoreCalculator {


    public double calculateScore(String s, String tournament) {
        int count = StringUtils.countMatches(s, "-");

        if (tournament == "G") {
            return count == 3 ? 1.0 : 0.75;
        } else {
            return count == 2 ? 1.0 : 0.75;
        }

    }

    public double[] calculateScoreGamewise(String scoreline) {
        String[] sets = scoreline.split("\\s+");

        double[] scores = new double[2];

        if (scoreline.equals("W/O")) {
            scores[0] = 1;
            return scores;
        }

        for (String set :  sets) {

            if (set.equals("RET")) {
                break;
            }

            String[] arr = set.split("-");

            scores[0] += Double.parseDouble(arr[0]);

            //Need to check if it is a tiebreak set
            scores[1] += Integer.parseInt(arr[1].split("\\(")[0]);

        }

        return scores;


    }

    public double calcGameNormalised(String scoreline) {
        String[] sets = scoreline.split("\\s+");

        double winnerScore = 0;
        double loserScore = 0;


        if (scoreline.equals("W/O")) {
            winnerScore = 1;
            return winnerScore;
        }

        for (String set :  sets) {

            if (set.equals("RET")) {
                break;
            }

            String[] arr = set.split("-");

            winnerScore += Double.parseDouble(arr[0]);

            //Need to check if it is a tiebreak set
            loserScore += Integer.parseInt(arr[1].split("\\(")[0]);

        }

        double toReturn = winnerScore / (winnerScore+loserScore);

        return (toReturn + 0.2) > 1.0 ? 1.0 : toReturn + 0.2;

    }
}
