import java.io.*;
import java.util.*;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;


/**
 * Created by bartu on 17/04/2019.
 */

public class FileReader {


    Ratings ratings;
    Ratings clayRatings;
    Ratings grassRatings;
    Ratings hardRatings;

    private Predictor predictor;
    private ScoreCalculator scoreCalculator;

    Map<String, Double> matchWinWeights = getWinWeights();
    Map<String, Double> matchLossWeights = getLossWeights();
    Map<String, Double> tournyWeigths = getTourneyWeights();
    Map<String, Double> tournySize = getTournySize();



    public FileReader(Predictor predictor) {
        ratings= new Ratings();
        clayRatings = new Ratings();
        grassRatings = new Ratings();
        hardRatings = new Ratings();

        this.predictor = predictor;
        this.scoreCalculator = new ScoreCalculator();
    }

    //Here we need to read and create ranking for all players
    //At some point you need to add code to be able to create new entries for players

    public void readData(int year, boolean predictFlag) {

        BufferedReader br = null;
        String line;
        String cvsSplitBy = ",";
        String csvFile = "match_data/atp_matches_" + Integer.toString(year) + ".csv";

        try {

            br = new BufferedReader(new java.io.FileReader(csvFile));
            //Remove first line(headers of table)
            br.readLine();
            //We do not know the first week so we want to start with empty
            String currDate = "";

            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] entry = line.split(cvsSplitBy);
                //Davis Cup, ATP Tour Challanger Skip
                if (entry.length == 0 || entry[4].equals("D") || entry[3].equals("9")) {
                    continue;
                }

                //If date has changed we need to update ratings
                //We need to also reset opponents & scores tables
                if (currDate.equals("")) {
                    currDate = entry[5];
                } else if (!currDate.equals(entry[5])) {
                    ratings.updateRatings();
                    ratings.clearMaps();
                    currDate = entry[5];
                }

                Player winningPlayer = new Player(Integer.parseInt(entry[7]), entry[10], entry[13]);
                Player losingPlayer = new Player(Integer.parseInt(entry[17]), entry[20], entry[23]);
                String surface = entry[2];

                checkRatingExists(winningPlayer, losingPlayer, ratings);

                if (surface.equals("Clay")) {
                    checkRatingExists(winningPlayer, losingPlayer, clayRatings);
                } else if (surface.equals("Grass")) {
                    checkRatingExists(winningPlayer, losingPlayer, grassRatings);
                } else {
                    //only other surface is hard
                    checkRatingExists(winningPlayer, losingPlayer, hardRatings);
                }

                double score = scoreCalculator.calcGameNormalised(entry[27]);
                double winnerScore = score;
                double loserScore = (1-score);

//                winnerScore = (winnerScore / (loserScore+winnerScore));
//                loserScore =1 - winnerScore;


                //Increment the predict counter if flag is in
                if(predictFlag) {
                    predictor.predictSingleMatch(ratings.getRanking(winningPlayer), ratings.getRanking(losingPlayer));


//                    if (surface.equals("Clay")) {
//                        predictor.predictSingleMatch(clayRatings.getRanking(winningPlayer), clayRatings.getRanking(losingPlayer));
//                    } else if (surface.equals("Grass")) {
//                        predictor.predictSingleMatch(grassRatings.getRanking(winningPlayer), grassRatings.getRanking(losingPlayer));
//                    } else {
//                        //only other surface is hard
//                        predictor.predictSingleMatch(hardRatings.getRanking(winningPlayer), hardRatings.getRanking(losingPlayer));
//                    }
                }

                ratings.fillMaps(winningPlayer, losingPlayer, winnerScore, loserScore);
            }

            //Final ratings update
            ratings.updateRatings();

            ratings.writeToExcel();

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

    private void checkRatingExists(Player winningPlayer, Player losingPlayer, Ratings ratings) {
        if (!ratings.rakingContains(winningPlayer)) {
            Double[] playerRating = {1500.0, 350.0, 0.06};
            winningPlayer.setRating(playerRating);
            ratings.addNewPlayer(winningPlayer, playerRating);
        }

        if (!ratings.rakingContains(losingPlayer)) {
            Double[] playerRating = {1500.0, 350.0, 0.06};
            losingPlayer.setRating(playerRating);
            ratings.addNewPlayer(losingPlayer, playerRating);
        }
    }

    private Map<String, Double> getWinWeights() {
        Map<String, Double> map = new HashMap<>();

        map.put("RR", 1.0);
        map.put("R128", 1.0);
        map.put("R64", 1.20);
        map.put("R32", 1.50);
        map.put("R16", 2.0);
        map.put("QF", 2.50);
        map.put("SF", 3.0);
        map.put("F", 4.0);

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
        map.put("A", 1.50);
        map.put("M", 2.0);
        map.put("C", 2.0);
        map.put("F", 2.0);
        map.put("G", 4.0);
        return map;
    }

    private Map<String, Double> getTournySize() {
        Map<String, Double> map = new HashMap<>();

        map.put("4", 1.0);
        //Special case for ATP Finals
        map.put("8", 4.0);
        map.put("16", 3.0);
        map.put("28", 1.0);
        map.put("32", 1.50);
        map.put("48", 2.0);
        map.put("56", 2.0);
        map.put("64", 2.0);
        map.put("96", 2.0);
        map.put("128", 3.0);
        return map;
    }

}
