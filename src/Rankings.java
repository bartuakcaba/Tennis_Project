import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by bartu on 17/04/2019.
 */
public class Rankings {

    private HashMap<Player, Double[]> rankings;


    public Rankings() {
        rankings = new HashMap<>();

    }

    //Here we need to read and create ranking for all players
    //At some point you need to add code to be ablet o create new entries for players

    public void readFile() {
        String csvFile = "../tennis_atp/atp_matches_2018.csv";
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";

        HashMap<Player, List<Double[]>> opponents = new HashMap<>();
        HashMap<Player, List<Integer>> scores = new HashMap<>();

        try {

            br = new BufferedReader(new FileReader(csvFile));
            //Remove first line(headers of table)
            br.readLine();
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] entry = line.split(cvsSplitBy);

                String currDate = entry[5];

                Player winningPlayer = new Player(Integer.parseInt(entry[7]),entry[10], entry[13]);
                Player losingPlayer = new Player(Integer.parseInt(entry[17]),entry[20], entry[23]);

                if (!rankings.containsKey(winningPlayer)) {
                    //CREATE RANKING ARRAY FOR PLAYER
                }

                if (!rankings.containsKey(losingPlayer)) {
                    //CREATE RANKING ARRAY FOR PLAYER
                }


                Double[] winningRankings = rankings.get(winningPlayer);
                Double[] losingRankings = rankings.get(losingPlayer);

            }

            Map<String, Integer> treeWinners = new TreeMap<>(players);

            System.out.println(entriesSortedByValues(treeWinners));


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
    }
}
