import java.io.*;
import java.util.*;
import java.util.function.DoubleBinaryOperator;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;


/**
 * Created by bartu on 17/04/2019.
 */

public class Rankings {

    private HashMap<Player, Double[]> rankings;
    private HashMap<Player, List<Double[]>> opponents = new HashMap<>();
    private HashMap<Player, List<Double>> scores = new HashMap<>();
    private Glicko2 rater;
    private Predictor predictor;
    Map<String, Double> matchWinWeights = getWinWeights();
    Map<String, Double> matchLossWeights = getLossWeights();
    Map<String, Double> tournyWeigths = getTourneyWeights();
    Map<String, Double> tournySize = getTournySize();



    public Rankings(Predictor predictor) {
        rankings = new HashMap<>();
        rater = new Glicko2();
        this.predictor = predictor;
    }

    //Here we need to read and create ranking for all players
    //At some point you need to add code to be able to create new entries for players

    public void readData(String csvFile, boolean predictFlag) {

        BufferedReader br = null;
        String line;
        String cvsSplitBy = ",";

        try {

            br = new BufferedReader(new FileReader(csvFile));
            //Remove first line(headers of table)
            br.readLine();
            //We do not know the first week so we want to start with empty
            String currDate = "";


            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] entry = line.split(cvsSplitBy);

                if (entry[4].equals("D")) {
                    continue;
                }


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
                    Double[] playerRating = {1500.0, 350.0, 0.06};
                    winningPlayer.setRating(playerRating);
                    rankings.put(winningPlayer, playerRating);

                    //Add player to hashmap of scores and opponents
                    opponents.put(winningPlayer, new LinkedList<>());
                    scores.put(winningPlayer, new LinkedList<>());
                }

                if (!rankings.containsKey(losingPlayer)) {
                    //CREATE RANKING ARRAY FOR PLAYER
                    Double[] playerRating = {1500.0, 350.0, 0.06};
                    losingPlayer.setRating(playerRating);
                    rankings.put(losingPlayer, playerRating);

                    //Add player to hashmap of scores and opponents
                    opponents.put(losingPlayer, new LinkedList<>());
                    scores.put(losingPlayer, new LinkedList<>());
                }

                double score = calculateScore(entry[27], entry[4]);
                double winnerScore = score*tournyWeigths.get(entry[4])*tournySize.get(entry[3]);
                double loserScore = (1-score)*tournyWeigths.get(entry[4])*tournySize.get(entry[3]);

                //Increment the predict counter if flag is in
                if(predictFlag) {
                    predictor.predictSingleMatch(rankings.get(winningPlayer), rankings.get(losingPlayer));
                }

                //Fill hashmaps of winning player
                List<Double[]> winningPlayerOpponents = opponents.get(winningPlayer);
                winningPlayerOpponents.add(rankings.get(losingPlayer));
                opponents.put(winningPlayer, winningPlayerOpponents);
                List<Double> winningPlayerScores = scores.get(winningPlayer);
                winningPlayerScores.add(winnerScore);
                scores.put(winningPlayer, winningPlayerScores);


                //Fill hashmaps of losing player
                List<Double[]> losingPlayerOpponents = opponents.get(losingPlayer);
                losingPlayerOpponents.add(rankings.get(winningPlayer));
                opponents.put(losingPlayer, losingPlayerOpponents);
                List<Double> losingPlayerScores = scores.get(losingPlayer);
                losingPlayerScores.add(loserScore);
                scores.put(losingPlayer, losingPlayerScores);

            }

            List<Player> players = getPlayers();

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
    private double calculateScore(String s, String tournament) {
        int count = StringUtils.countMatches(s, "-");

        if (tournament == "G") {
            return count == 3 ? 1.0 : 0.75;
        } else {
            return count == 2 ? 1.0 : 0.75;
        }

    }

    private void updateRatings(HashMap<Player, List<Double[]>> opponents, HashMap<Player, List<Double>> scores) {
        writeToExcel2(opponents);
        scores = normaliseRankings(scores);

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

    private List<Player> getPlayers() {
        return new ArrayList<>(rankings.keySet());
    }

    private void writeToExcel(List<Player> players) {
        try {
            Workbook workbook = new HSSFWorkbook();
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

    private Map<String, Double> getWinWeights() {
        Map<String, Double> map = new HashMap<>();

        map.put("RR", 1.0);
        map.put("R128", 1.0);
        map.put("R64", 1.1);
        map.put("R32", 1.2);
        map.put("R16", 1.3);
        map.put("QF", 1.5);
        map.put("SF", 1.75);
        map.put("F", 2.0);

        return map;
    }

    private Map<String, Double> getLossWeights() {
        Map<String, Double> map = new HashMap<>();

        map.put("RR", 0.5);
        map.put("R128", 0.5);
        map.put("R64", 0.5);
        map.put("R32", 0.6);
        map.put("R16", 0.70);
        map.put("QF", 0.80);
        map.put("SF", 0.90);
        map.put("F", 1.0);

        return map;
    }

    private Map<String, Double> getTourneyWeights() {
        Map<String, Double> map = new HashMap<>();

        map.put("D", 1.0);
        map.put("A", 1.25);
        map.put("M", 1.50);
        map.put("C", 1.50);
        map.put("F", 1.50);
        map.put("G", 2.0);
        return map;
    }

    private Map<String, Double> getTournySize() {
        Map<String, Double> map = new HashMap<>();

        map.put("4", 1.0);
        //Special case for ATP Finals
        map.put("8", 2.0);
        map.put("16", 2.0);
        map.put("28", 1.0);
        map.put("32", 1.0);
        map.put("48", 1.0);
        map.put("56", 1.25);
        map.put("64", 1.25);
        map.put("96", 1.45);
        map.put("128", 1.50);
        return map;
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

    public HashMap<Player, Double[]> getRankings() {
        return rankings;
    }
}
