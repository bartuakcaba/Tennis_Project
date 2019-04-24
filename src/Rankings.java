import java.io.*;
import java.util.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hpbf.dev.PLCDumper;
import org.apache.poi.hssf.*;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;


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
                    updateRatings(opponents, scores);
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
                    winningPlayer.setRating(new Double[]{1500.0, 350.0, 0.06});
                    rankings.put(winningPlayer, new Double[]{1500.0, 350.0, 0.06});

                    //Add player to hashmap of scores and opponents
                    opponents.put(winningPlayer, new LinkedList<Double[]>());
                    scores.put(winningPlayer, new LinkedList<Double>());
                }

                if (!rankings.containsKey(losingPlayer)) {
                    //CREATE RANKING ARRAY FOR PLAYER
                    losingPlayer.setRating(new Double[]{1500.0, 350.0, 0.06});
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

            List<Player> players = getPlayers();

//            Collections.sort(players, new SortByRating());
            writeToExcel(players);

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

    private void updateRatings(HashMap<Player, List<Double[]>> opponents, HashMap<Player, List<Double>> scores) {

        for(Map.Entry<Player, List<Double[]>> rivals : opponents.entrySet()) {
            opponents.put(rivals.getKey(), rater.convertGlicko2List(rivals.getValue()));
        }

        //turn Rankings into Glicko-2 scale
        for (Map.Entry<Player, Double[]> ranking : rankings.entrySet()) {
            Double[] glicko2Rating = rater.convertGlicko2(ranking.getValue());
            List<Double[]> rivalRatings = opponents.get(ranking.getKey());

            if (rivalRatings.isEmpty()) {
                Double[] newRating = rater.updateUnplayedPlayer(glicko2Rating);
                ranking.getKey().setRating(rater.convertBack(newRating));
                rankings.put(ranking.getKey(), rater.convertBack(newRating));
                continue;
            }

            Double v = rater.calcV(glicko2Rating, rivalRatings);

            Double diff = rater.calcEstImprovement(v, scores.get(ranking.getKey()), glicko2Rating, rivalRatings);

            Double newVol = rater.calcNewVol(glicko2Rating[1], diff, v);

            Double newRD = rater.updateRD(glicko2Rating[1], newVol);

            Double[] newRatings = rater.updateRating(v, scores.get(ranking.getKey()), glicko2Rating, rivalRatings, newRD);

            ranking.getKey().setRating(rater.convertBack(newRatings));
            rankings.put(ranking.getKey(), rater.convertBack(newRatings));
        }


    }

    private List<Player> getPlayers() {
        return new ArrayList<>(rankings.keySet());
    }

    private void writeToExcel(List<Player> players) {
        try {
            Workbook workbook = new HSSFWorkbook();
            CreationHelper createHelper = workbook.getCreationHelper();
            Sheet sheet = workbook.createSheet("new sheet");

            int i = 0;
            for (Player player : players) {
                Row row = sheet.createRow(i);
                row.createCell(1).setCellValue(player.getName());
                row.createCell(2).setCellValue(player.getRating()[0]);
                row.createCell(3).setCellValue(player.getRating()[1]);
                row.createCell(4).setCellValue(player.getRating()[2]);
                i++;
            }

            FileOutputStream fileOut = new FileOutputStream("ratings.xls");
            workbook.write(fileOut);
            fileOut.close();
        } catch (Exception ioe) {
            ioe.printStackTrace();
        }

    }

    class SortByRating implements Comparator<Player>
    {
        // Used for sorting in ascending order of
        // roll number
        public int compare(Player a, Player b)
        {
            return Double.compare(a.getRating()[0], b.getRating()[0]);
        }
    }

}
