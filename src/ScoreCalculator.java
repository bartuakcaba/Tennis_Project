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

    public int noOfSets(String s) {
         return  StringUtils.countMatches(s, "-");
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


        if (scoreline.contains("W/O") || scoreline.equals(" RET") || scoreline.contains("Jun") || scoreline.isEmpty()) {
            winnerScore = 1;
            return winnerScore;
        }

        for (String set :  sets) {

            if (set.contains("RET") || set.contains("DEF")) {
                break;
            }

            if (set.contains("Played and abandoned")) {
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

    public double calcScoreGameDivivded(String sw1, String sl1, String sw2, String sl2, String sw3, String sl3,
                                        String sw4, String sl4, String sw5, String sl5) {
        int winner;
        int total;


        if (sw1.equals("")) {
            winner =1;
            total=1;
        } else if (sw2.equals("") || sw2.equals(" ")) {
            winner = Integer.parseInt(sw1);
            total = winner + Integer.parseInt(sl1);
        } else if (sw3.equals("")|| sw3.equals(" ")) {
            winner = Integer.parseInt(sw1) + Integer.parseInt(sw2);
            total = winner + Integer.parseInt(sl1) + Integer.parseInt(sl2);
        } else if (sw4.equals("") || sw4.equals(" ")) {
            winner = Integer.parseInt(sw1) + Integer.parseInt(sw2) + Integer.parseInt(sw3);
            total = winner + Integer.parseInt(sl1) + Integer.parseInt(sl2) + Integer.parseInt(sl3);
        } else if (sw5.equals("") || sw5.equals("")) {
            winner = Integer.parseInt(sw1) + Integer.parseInt(sw2) + Integer.parseInt(sw3) + Integer.parseInt(sw4);
            total = winner + Integer.parseInt(sl1) + Integer.parseInt(sl2) + Integer.parseInt(sl3)
                    + Integer.parseInt(sl4);

        } else {
            winner = Integer.parseInt(sw1) + Integer.parseInt(sw2) + Integer.parseInt(sw3) + Integer.parseInt(sw4) + Integer.parseInt(sw5);
            total = winner + Integer.parseInt(sl1) + Integer.parseInt(sl2) + Integer.parseInt(sl3)
                    + Integer.parseInt(sl4) + Integer.parseInt(sl5);
        }

        if (total == 0) {
            return  1;
        }

        double toReturn = (double) winner /  (double) total;

        return (toReturn + 0.2) > 1.0 ? 1.0 : toReturn + 0.2;
    }
}
