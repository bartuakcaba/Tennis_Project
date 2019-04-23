import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import org.apache.commons.lang3.StringUtils;


/**
 * Created by bartu on 17/04/2019.
 */
public class Rankings {

    private HashMap<Player, Double[]> rankings;
    private Glicko2 rater;


    public Rankings() {
        rankings = new HashMap<>();
        rater = new Glicko2();
    }

    //Here we need to read and create ranking for all players
    //At some point you need to add code to be ablet o create new entries for players

    public void readFile() {
        String csvFile = "match_data/atp_matches_2018.csv";
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";

        HashMap<Player, List<Double[]>> opponents = new HashMap<>();
        HashMap<Player, List<Double>> scores = new HashMap<>();

        try {

            br = new BufferedReader(new FileReader(csvFile));
            //Remove first line(headers of table)
            br.readLine();
            //We do not know the first week so we want to start with empty
            String currDate = "";
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] entry = line.split(cvsSplitBy);

                //If date has changed we need to update ratings
                //We need to also reset opponents & scores tables

                if (currDate.equals("")) {
                    currDate = entry[5];
                } else if (!currDate.equals(entry[5])) {
                    updateRatings();
                    for (Map.Entry<Player, Double[]> playerEntry : rankings.entrySet()) {
                        Player player = playerEntry.getKey();
                        opponents.get(player).clear();
                        scores.get(player).clear();

                    }
                    currDate = entry[5];
                }

                Player winningPlayer = new Player(Integer.parseInt(entry[7]), entry[10], entry[13]);
                Player losingPlayer = new Player(Integer.parseInt(entry[17]), entry[20], entry[23]);

                if (!rankings.containsKey(winningPlayer)) {
                    //CREATE RANKING ARRAY FOR PLAYER
                    rankings.put(winningPlayer, new Double[]{1500.0, 350.0, 0.06});

                    //Add player to hashmap of scores and opponents
                    opponents.put(winningPlayer, new LinkedList<Double[]>());
                    scores.put(winningPlayer, new LinkedList<Double>());
                }

                if (!rankings.containsKey(losingPlayer)) {
                    //CREATE RANKING ARRAY FOR PLAYER
                    rankings.put(losingPlayer, new Double[]{1500.0, 350.0, 0.06});

                    //Add player to hashmap of scores and opponents
                    opponents.put(losingPlayer, new LinkedList<Double[]>());
                    scores.put(losingPlayer, new LinkedList<Double>());
                }

                double score = calculateScore(entry[27]);

                //Fill hashmaps of winning player
                List<Double[]> winningPlayerOpponents = opponents.get(winningPlayer);
                winningPlayerOpponents.add(rankings.get(losingPlayer));
                opponents.put(winningPlayer, winningPlayerOpponents);
                List<Double> winningPlayerScores = scores.get(winningPlayer);
                winningPlayerScores.add(score);
                scores.put(winningPlayer, winningPlayerScores);


                //Fill hashmaps of losing player
                List<Double[]> losingPlayerOpponents = opponents.get(losingPlayer);
                losingPlayerOpponents.add(rankings.get(winningPlayer));
                opponents.put(losingPlayer, losingPlayerOpponents);
                List<Double> losingPlayerScores = scores.get(losingPlayer);
                losingPlayerScores.add(1 - score);
                scores.put(losingPlayer, losingPlayerScores);

            }

//            Map<String, Integer> treeWinners = new TreeMap<>(players);
//
//           System.out.println(entriesSortedByValues(treeWinners));


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //TODO This scoring is going to be the main part
    private double calculateScore(String s) {
        int count = StringUtils.countMatches(s, "-");
        return count == 2 ? 1.0 : 0.75;
    }

    private void updateRatings() {

    }
}
