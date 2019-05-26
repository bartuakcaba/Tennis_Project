import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

import java.io.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by bartu on 24/05/2019.
 */
public class BetFileReader {

    Ratings ratings;
    Ratings clayRatings;
    Ratings grassRatings;
    Ratings hardRatings;

    private Predictor predictor;
    private SetPredictor setPredictor;
    private ScoreCalculator scoreCalculator;

    Map<String, Double> matchWinWeights = getWinWeigths();
    Map<String, Double> tournyWeigths = getTourneyWeights();
    Map<String, Double> tournySize = getTournySize();

    Map<H2H, Integer[]> h2HMap = new HashMap<>();
    Map<Player, Integer> noOfTitles = new HashMap<>();

    public BetFileReader(Predictor predictor, SetPredictor setPredictor) {
        ratings= new Ratings();
        clayRatings = new Ratings();
        grassRatings = new Ratings();
        hardRatings = new Ratings();

        this.predictor = predictor;
        this.setPredictor = setPredictor;
        this.scoreCalculator = new ScoreCalculator();
    }

    //Here we need to read and create ranking for all players
    //At some point you need to add code to be able to create new entries for players
    public void readData(int year, boolean predictFlag) throws Exception {


        BufferedReader br = null;
        String line;
        String cvsSplitBy = ",";
        String xlsxFile = "match_data_bet/" + Integer.toString(year) + ".xls";

        try {

            File excelFile = new File(xlsxFile);
            FileInputStream fs = new FileInputStream(excelFile);
            Workbook wb = Workbook.getWorkbook(fs);

            Sheet sh = wb.getSheet(Integer.toString(year));
            int totalNoOfRows = sh.getRows();

            br = new BufferedReader(new java.io.FileReader(xlsxFile));
            //Remove first line(headers of table)
            br.readLine();
            //We do not know the first week so we want to start with empty
            LocalDate currLD = LocalDate.MAX;
            String prevSurf = "";


            for (int rowNo = 1; rowNo < totalNoOfRows; rowNo++) {

                // use comma as separator
                Cell[] row = sh.getRow(rowNo) ;
                //Davis Cup, ATP Tour Challanger Skip

                if (row.length == 0) {
                    continue;
                }

                //In bet files dates are given per day, we need to find the monday
                String[] dateString = row[3].getContents().split("/");
                LocalDate ld = LocalDate.of(Integer.parseInt(dateString[2]), Integer.parseInt(dateString[1]), Integer.parseInt(dateString[0]));
                ld = ld.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

                //If date has changed we need to update ratings
                //We need to also reset opponents & scores tables
                if (currLD.equals(LocalDate.MAX)) {
                    currLD = ld;
                    prevSurf = row[6].getContents();
                } else if (!ld.equals(currLD)) {
                    ratings.updateRatings();
                    ratings.clearMaps();

                    if (prevSurf.equals("Clay")) {
                        clayRatings.updateRatings();
                        clayRatings.clearMaps();
                    }

                    if (prevSurf.equals("Grass")) {
                        grassRatings.updateRatings();
                        grassRatings.clearMaps();
                    }

                    if (prevSurf.equals("Hard")) {
                        hardRatings.updateRatings();
                        hardRatings.clearMaps();
                    }

                    currLD = ld;
                    prevSurf = row[6].getContents();
                }

                Player winningPlayer = new Player(row[9].getContents());
                Player losingPlayer = new Player(row[10].getContents());
                Double[] winnerSurfRatings;
                Double[] loserSurfRatings;
                String surface = row[6].getContents();

                checkRatingExists(winningPlayer, losingPlayer, ratings);

                if (surface.equals("Clay")) {
                    checkRatingExists(winningPlayer, losingPlayer, clayRatings);
                    winnerSurfRatings = clayRatings.getRanking(winningPlayer);
                    loserSurfRatings = clayRatings.getRanking(losingPlayer);

                } else if (surface.equals("Grass")) {
                    checkRatingExists(winningPlayer, losingPlayer, grassRatings);
                    winnerSurfRatings = grassRatings.getRanking(winningPlayer);
                    loserSurfRatings = grassRatings.getRanking(losingPlayer);
                } else {
                    //only other surface is hard
                    checkRatingExists(winningPlayer, losingPlayer, hardRatings);
                    winnerSurfRatings = hardRatings.getRanking(winningPlayer);
                    loserSurfRatings = hardRatings.getRanking(losingPlayer);
                }

                if (row[7].getContents().equals("F")) {
                    putTitleWin(winningPlayer);
                }

                int higherTitles;
                int lowerTitles;

                if(ratings.getRanking(winningPlayer)[0] > ratings.getRanking(losingPlayer)[0]) {
                    higherTitles = noOfTitles.get(winningPlayer);
                    lowerTitles = noOfTitles.get(losingPlayer);
                } else {
                    higherTitles = noOfTitles.get(losingPlayer);
                    lowerTitles = noOfTitles.get(winningPlayer);
                }

                double score = scoreCalculator.calcScoreGameDivivded(row[15].getContents(), row[16].getContents(), row[17].getContents(),
                        row[18].getContents(), row[19].getContents(), row[20].getContents(), row[21].getContents(), row[22].getContents(),
                        row[23].getContents(), row[24].getContents());
                double winnerScore = score/2 + tournyWeigths.get(row[4].getContents())/3 + matchWinWeights.get(row[7].getContents())/3;
                winnerScore = winnerScore > 1 ? 1 : winnerScore;
                double loserScore = (1-score)/2;
                loserScore = loserScore > 0.5 ? 0.5 : loserScore;
//                loserScore += tournyWeigths.get(entry[4]);

                double h2h = getH2H(winningPlayer, losingPlayer);

//                double h2h = dbhandler.getH2H(winningPlayer.getName(), losingPlayer.getName());
//
//                //The cases where these two palyers have met before
//                if (h2h != -1) {
//                    h2h = ratings.getRanking(winningPlayer)[0] > ratings.getRanking(losingPlayer)[0] ? h2h : 1 - h2h;
//                }

                ratings.fillMaps(winningPlayer, losingPlayer, winnerScore, loserScore);


                if (surface.equals("Clay")) {
                    clayRatings.fillMaps(winningPlayer, losingPlayer, winnerScore, loserScore);
                } else if (surface.equals("Grass")) {
                    grassRatings.fillMaps(winningPlayer, losingPlayer, winnerScore, loserScore);
                } else {
                    //only other surface is hard
                    hardRatings.fillMaps(winningPlayer, losingPlayer, winnerScore, loserScore);
                }


                if(predictFlag) {
//                    predictor.predictSingleMatch(ratings.getRanking(winningPlayer), ratings.getRanking(losingPlayer));

//                    if (!row[2].getContents().equals("Australian Open")) {
//                        continue;
//                    }

                    predictor.predictWithMulRatings(ratings.getRanking(winningPlayer), ratings.getRanking(losingPlayer),
                            winnerSurfRatings,loserSurfRatings);
                    predictor.addToTest(ratings.getRanking(winningPlayer), ratings.getRanking(losingPlayer),
                            winnerSurfRatings, loserSurfRatings, h2h, higherTitles, lowerTitles, 0);

                    //FOR SET PREDICTION
                    if (!row[4].getContents().equals("Grand Slam") && !row[25].getContents().equals("") && !row[26].getContents().equals("")) {
                        setPredictor.addToTest(ratings.getRanking(winningPlayer), ratings.getRanking(losingPlayer),
                                winnerSurfRatings, loserSurfRatings, h2h, Integer.parseInt(row[25].getContents()) + Integer.parseInt(row[26].getContents()));
                    }
                } else {
                    predictor.addToDataset(ratings.getRanking(winningPlayer), ratings.getRanking(losingPlayer),
                            winnerSurfRatings, loserSurfRatings, h2h, higherTitles, lowerTitles, 0);

                    //FOR SET PREDICTION
                    if(!row[4].getContents().equals("Grand Slam") && !row[25].getContents().equals("") && !row[26].getContents().equals("")) {
                        setPredictor.addToDataset(ratings.getRanking(winningPlayer), ratings.getRanking(losingPlayer),
                                winnerSurfRatings, loserSurfRatings, h2h, Integer.parseInt(row[25].getContents()) + Integer.parseInt(row[26].getContents()));
                    }
                }
            }

            //Final ratings update
            ratings.updateRatings();
            hardRatings.updateRatings();

            ratings.writeToExcel("ratings.xls");
            clayRatings.writeToExcel("clay_ratings.xls");
            grassRatings.writeToExcel("grass_ratings.xls");
            hardRatings.writeToExcel("hard_ratings.xls");


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

    private void putTitleWin(Player winningPlayer) {
        int titles = noOfTitles.get(winningPlayer);
        noOfTitles.put(winningPlayer, ++titles);
    }

    private double getH2H(Player winner, Player loser) {

        boolean winnerAlph = winner.getName().compareTo(loser.getName()) < 0;
        Player alpHigher = winner.getName().compareTo(loser.getName()) < 0 ? winner : loser;
        Player ratingHigher = ratings.getRanking(winner)[0] >= ratings.getRanking(loser)[0] ?winner : loser;
        boolean higherAlph = alpHigher.equals(ratingHigher);

        H2H h2h;

        if (winnerAlph) {
            h2h = new H2H(winner.getName(), loser.getName());
        } else {
            h2h = new H2H(loser.getName(), winner.getName());
        }

        if (h2HMap.containsKey(h2h)) {
            int p1wins = h2HMap.get(h2h)[0];
            int p2wins = h2HMap.get(h2h)[1];
            double ratio =  p1wins / (p1wins + p2wins);

            if (winnerAlph) {
                h2HMap.put(h2h, new Integer[]{p1wins+1, p2wins});
            } else {
                h2HMap.put(h2h, new Integer[]{p1wins, p2wins+1});
            }
            return higherAlph ? ratio : 1-ratio;
        } else {
            if (winnerAlph) {
                h2HMap.put(h2h, new Integer[]{1, 0});
            } else {
                h2HMap.put(h2h, new Integer[]{0, 1});
            }
            return higherAlph ? -1 : -2;
        }

    }

    private void checkRatingExists(Player winningPlayer, Player losingPlayer, Ratings ratings) {
        if (!ratings.rakingContains(winningPlayer)) {
            Double[] playerRating = {1500.0, 350.0, 0.06};
            winningPlayer.setRating(playerRating);
            ratings.addNewPlayer(winningPlayer, playerRating);
            noOfTitles.put(winningPlayer, 0);
        }

        if (!ratings.rakingContains(losingPlayer)) {
            Double[] playerRating = {1500.0, 350.0, 0.06};
            losingPlayer.setRating(playerRating);
            ratings.addNewPlayer(losingPlayer, playerRating);
            noOfTitles.put(losingPlayer, 0);
        }
    }


    private Map<String, Double> getWinWeigths() {
        Map<String, Double> map = new HashMap<>();

        map.put("Round Robin", 0.7);
        map.put("1st Round", 0.4);
        map.put("2nd Round", 0.4);
        map.put("3rd Round", 0.6);
        map.put("4th Round", 0.7);
        map.put("Quarterfinals", 0.80);
        map.put("Semifinals", 0.90);
        map.put("The Final", 1.0);

        return map;
    }

    private Map<String, Double> getTourneyWeights() {
        Map<String, Double> map = new HashMap<>();

        map.put("ATP250", 0.7);
        map.put("ATP500", 0.75);
        map.put("Masters 1000", 0.8);
        map.put("Masters Cup", 0.9);
        map.put("Grand Slam", 1.0);

        map.put("International", 0.7);
        map.put("International Gold", 0.75);
        map.put("Masters", 0.8);
        map.put("Masters Cup", 0.9);
        map.put("Grand Slam", 1.0);
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
