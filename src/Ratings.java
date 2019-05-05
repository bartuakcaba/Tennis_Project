import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.FileOutputStream;
import java.util.*;

/**
 * Created by bartu on 05/05/2019.
 */

public class Ratings {

    private HashMap<Player, Double[]> rankings;
    private HashMap<Player, List<Double[]>> opponents;
    private HashMap<Player, List<Double>> scores;

    private Glicko2 rater;

    public Ratings() {
        rankings = new HashMap<>();
        opponents = new HashMap<>();
        scores = new HashMap<>();
        rater = new Glicko2();
    }

    public void updateRatings() {
        writeToExcel2(opponents);
//        scores = normaliseRankings(scores);

        for(Map.Entry<Player, Double[]> rivals : rankings.entrySet()) {
            Double[] convertedRatings = rater.convertGlicko2(rivals.getValue());
            rankings.put(rivals.getKey(), convertedRatings);
        }

        //turn Rankings into Glicko-2 scale
        for (Map.Entry<Player, Double[]> ranking : rankings.entrySet()) {
            Double[] glicko2Rating = ranking.getValue();
            List<Double[]> rivalRatings = opponents.get(ranking.getKey());

            if (rivalRatings.isEmpty()) {
                Double[] newRating = rater.updateUnplayedPlayer(glicko2Rating);
                Double[] convertedBack = rater.convertBack(newRating);
                rankings.put(ranking.getKey(), convertedBack);
                continue;
            }

            Double v = rater.calcV(glicko2Rating, rivalRatings);

            Double diff = rater.calcEstImprovement(v, scores.get(ranking.getKey()), glicko2Rating, rivalRatings);

            Double newVol = rater.calcNewVol(glicko2Rating[1], diff, v, glicko2Rating[2]);

            Double newRD = rater.updateRD(glicko2Rating[1], newVol);

            Double[] newRatings = rater.updateRating(v, scores.get(ranking.getKey()), glicko2Rating, rivalRatings, newRD, newVol);

            Double[] convertedBack = rater.convertBack(newRatings);

            rankings.put(ranking.getKey(), convertedBack);
        }

    }

    public void writeToExcel() {

        Set<Player> players = rankings.keySet();
        try {
            Workbook workbook = new HSSFWorkbook();
            Sheet sheet = workbook.createSheet("new sheet");

            int i = 0;
            for (Player player : players) {
                Double[] ranking = rankings.get(player);
                Row row = sheet.createRow(i);
                row.createCell(1).setCellValue(player.getName());
                row.createCell(2).setCellValue(ranking[0]);
                row.createCell(3).setCellValue(ranking[1]);
                row.createCell(4).setCellValue(ranking[2]);
                i++;
            }

            FileOutputStream fileOut = new FileOutputStream("ratings.xls");
            workbook.write(fileOut);
            fileOut.close();
        } catch (Exception ioe) {
            ioe.printStackTrace();
        }

    }

    private void writeToExcel2(Map<Player, List<Double[]>> opponents) {
        try {
            Workbook workbook = new HSSFWorkbook();
            Sheet sheet = workbook.createSheet("new sheet");

            int i = 0;
            for (Map.Entry<Player, List<Double[]>> entry : opponents.entrySet()) {
                Row row = sheet.createRow(i);
                row.createCell(1).setCellValue(entry.getKey().getName());
                int j = 2;
                for (Double[] player : entry.getValue()) {
                    row.createCell(j).setCellValue(player[0]);
                    j++;
                }
                i++;
            }

            FileOutputStream fileOut = new FileOutputStream("opponents.xls");
            workbook.write(fileOut);
            fileOut.close();
        } catch (Exception ioe) {
            ioe.printStackTrace();
        }

    }

    public void clearMaps() {
        for (Map.Entry<Player, Double[]> playerEntry : rankings.entrySet()) {
            Player player = playerEntry.getKey();
            opponents.get(player).clear();
            scores.get(player).clear();
        }
    }


    public boolean rakingContains(Player player) {
        return rankings.containsKey(player);
    }

    public void addNewPlayer(Player player, Double[] playerRating) {
        rankings.put(player, playerRating);

        //Add player to hashmap of scores and opponents
        opponents.put(player, new LinkedList<>());
        scores.put(player, new LinkedList<>());
    }

    public Double[] getRanking(Player player) {
        return rankings.get(player);
    }

    public void fillMaps(Player winningPlayer, Player losingPlayer, Double winnerScore, Double loserScore) {
        //Fill hashmaps of winning player
        List<Double[]> winningPlayerOpponents = opponents.get(winningPlayer);
        Double[] loserRating = new Double[3];
        loserRating = rater.convertGlicko2( Arrays.copyOf(rankings.get(losingPlayer), rankings.get(losingPlayer).length));
        winningPlayerOpponents.add(loserRating);
        opponents.put(winningPlayer, winningPlayerOpponents);
        List<Double> winningPlayerScores = scores.get(winningPlayer);
        winningPlayerScores.add(winnerScore);
        scores.put(winningPlayer, winningPlayerScores);

        //Fill hashmaps of losing player
        List<Double[]> losingPlayerOpponents = opponents.get(losingPlayer);
        Double[] winnerRating = new Double[3];
        winnerRating = rater.convertGlicko2(Arrays.copyOf(rankings.get(winningPlayer), rankings.get(winningPlayer).length));
        losingPlayerOpponents.add(winnerRating);
        opponents.put(losingPlayer, losingPlayerOpponents);
        List<Double> losingPlayerScores = scores.get(losingPlayer);
        losingPlayerScores.add(loserScore);
        scores.put(losingPlayer, losingPlayerScores);
    }

    private HashMap<Player, List<Double>> normaliseRankings(HashMap<Player, List<Double>> scores) {

        Double maxScore = 0.0;
        Double minScore = Double.MAX_VALUE;

        for(Map.Entry<Player, List<Double>> entry : scores.entrySet()) {

            if (entry.getValue().isEmpty()) {
                continue;
            }

            if (Collections.max(entry.getValue()) > maxScore) {
                maxScore = Collections.max(entry.getValue());
            }

            if (Collections.min(entry.getValue()) < minScore) {
                minScore = Collections.min(entry.getValue());
            }
        }

        for(Map.Entry<Player, List<Double>> entry : scores.entrySet()) {

            for (int i =0; i < entry.getValue().size(); i++) {
                Double d = entry.getValue().get(i);
                d = (d-minScore) / (maxScore-minScore);
                entry.getValue().set(i, d);
            }
        }

        return  scores;

    }
}
