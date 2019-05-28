import com.sun.org.apache.xpath.internal.operations.Bool;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.*;
import java.util.*;


/**
 * Created by bartu on 17/04/2019.
 */

public class FileReader {

    Ratings ratings;
    Ratings clayRatings;
    Ratings grassRatings;
    Ratings hardRatings;

    private Predictor predictor;
    private SetPredictor setPredictor;
    private ScoreCalculator scoreCalculator;
    private DBHandler dbhandler;

    Map<String, Double> matchWinWeights = getWinWeights();
    Map<String, Double> matchLossWeights = getLossWeights();
    Map<String, Double> tournyWeigths = getTourneyWeights();
    Map<String, Double> tournySize = getTournySize();

    Map<H2H, Integer[]> h2HMap = new HashMap<>();
    Map<Player, Integer> noOfTitles = new HashMap<>();
    List<Double[]> titleWinners = new ArrayList<>();
    List<String> witleNames = new ArrayList<>();
    Map<Double, Double> avgTitle = new HashMap<>();
    Map<Double, Integer> titleNo = new HashMap<>();


    public FileReader(Predictor predictor, SetPredictor setPredictor) {
        ratings= new Ratings();
        clayRatings = new Ratings();
        grassRatings = new Ratings();
        hardRatings = new Ratings();

        this.predictor = predictor;
        this.setPredictor = setPredictor;
        this.scoreCalculator = new ScoreCalculator();
        this.dbhandler = new DBHandler();
    }

    //Here we need to read and create ranking for all players
    //At some point you need to add code to be able to create new entries for players
    public void readData(int year, boolean predictFlag) throws Exception {


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
            String prevSurf = "";


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
                    prevSurf = entry[2];
                } else if (!currDate.equals(entry[5])) {
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

                    currDate = entry[5];
                    prevSurf = entry[2];
                }

                Player winningPlayer = new Player(Integer.parseInt(entry[7]), entry[10], entry[13]);
                Player losingPlayer = new Player(Integer.parseInt(entry[17]), entry[20], entry[23]);
                Double[] winnerSurfRatings;
                Double[] loserSurfRatings;
                String surface = entry[2];

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

                if (entry[29].equals("F")) {
                    putTitleWin(winningPlayer);
                }

                double score = scoreCalculator.calcGameNormalised(entry[27]);
                double winnerScore = score/2 + tournyWeigths.get(entry[4])/3 + matchLossWeights.get(entry[29])/3;
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

                //Can't use for ml model
                if (entry[14].equals("") || entry[24].equals("")) {
                    continue;
                }

                int higherTitles;
                int lowerTitles;
                double higherAge;
                double lowerAge;
                double higherMomentum;
                double lowerMomentum;


                if(ratings.getRanking(winningPlayer)[0] > ratings.getRanking(losingPlayer)[0]) {
                    higherTitles = noOfTitles.get(winningPlayer);
                    lowerTitles = noOfTitles.get(losingPlayer);
                    higherAge = Double.parseDouble(entry[14]);
                    lowerAge = Double.parseDouble(entry[24]);
                    higherMomentum = calculateMomentum(ratings.getMomentum(winningPlayer));
                    lowerMomentum = calculateMomentum(ratings.getMomentum(losingPlayer));
                } else {
                    higherTitles = noOfTitles.get(losingPlayer);
                    lowerTitles = noOfTitles.get(winningPlayer);
                    higherAge = Double.parseDouble(entry[24]);
                    lowerAge = Double.parseDouble(entry[14]);
                    higherMomentum = calculateMomentum(ratings.getMomentum(losingPlayer));
                    lowerMomentum = calculateMomentum(ratings.getMomentum(winningPlayer));
                }


                if(predictFlag) {

//                    if (!entry[1].equals("Us Open")) {
//                        continue;
//                    }

                    predictor.predictWithMulRatings(ratings.getRanking(winningPlayer), ratings.getRanking(losingPlayer),
                            winnerSurfRatings,loserSurfRatings);
                    predictor.addToTest(ratings.getRanking(winningPlayer), ratings.getRanking(losingPlayer),
                            winnerSurfRatings, loserSurfRatings, h2h, higherTitles, lowerTitles, higherAge, lowerAge,
                            higherMomentum, lowerMomentum);


                    //FOR SET PREDICTION
                    if (!entry[4].equals("G")) {
                        setPredictor.addToTest(ratings.getRanking(winningPlayer), ratings.getRanking(losingPlayer),
                                winnerSurfRatings, loserSurfRatings, h2h, scoreCalculator.noOfSets(entry[27]));
                    }
                } else {
                    predictor.addToDataset(ratings.getRanking(winningPlayer), ratings.getRanking(losingPlayer),
                            winnerSurfRatings, loserSurfRatings, h2h, higherTitles, lowerTitles, higherAge, lowerAge,
                            higherMomentum, lowerMomentum);


                    //FOR SET PREDICTION
                    if(!entry[4].equals("G")) {
                        setPredictor.addToDataset(ratings.getRanking(winningPlayer), ratings.getRanking(losingPlayer),
                                winnerSurfRatings, loserSurfRatings, h2h, scoreCalculator.noOfSets(entry[27]));
                    }
                }

//                    double age = Math.floor(Double.parseDouble(entry[14]));
//
//                    if (avgTitle.containsKey(age)) {
//                        double avg = avgTitle.get(age);
//                        int no = titleNo.get(age);
//                        avg *= no;
//                        no++;
//                        avg += ratings.getRanking(winningPlayer)[0];
//                        avg = avg / no;
//                        avgTitle.put(age, avg);
//                        titleNo.put(age, no);
//                    } else {
//                        avgTitle.put(age, ratings.getRanking(winningPlayer)[0]);
//                        titleNo.put(age, 1);
//
//                }
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

    private Map<String, Double> getWinWeights() {
        Map<String, Double> map = new HashMap<>();

//        map.put("RR", 1.0);
//        map.put("R128", 1.0);
//        map.put("R64", 1.20);
//        map.put("R32", 1.50);
//        map.put("R16", 2.0);
//        map.put("QF", 2.50);
//        map.put("SF", 3.0);
//        map.put("F", 4.0);

        map.put("RR", 0.40);
        map.put("R128", 0.30);
        map.put("R64", 0.30);
        map.put("R32", 0.35);
        map.put("R16", 0.35);
        map.put("QF", 0.40);
        map.put("SF", 0.45);
        map.put("F", 0.50);

        return map;
    }

    private Map<String, Double> getLossWeights() {
        Map<String, Double> map = new HashMap<>();

        map.put("RR", 0.7);
        map.put("BR", 0.7);
        map.put("R128", 0.4);
        map.put("R64", 0.5);
        map.put("R32", 0.6);
        map.put("R16", 0.7);
        map.put("QF", 0.80);
        map.put("SF", 0.90);
        map.put("F", 1.0);

        return map;
    }

    private Map<String, Double> getTourneyWeights() {
        Map<String, Double> map = new HashMap<>();

//        map.put("D", 1.0);
//        map.put("A", 1.50);
//        map.put("M", 2.0);
//        map.put("C", 2.0);
//        map.put("F", 2.0);
//        map.put("G", 4.0);

        map.put("D", 0.7);
        map.put("A", 0.7);
        map.put("M", 0.8);
        map.put("C", 0.9);
        map.put("F", 0.9);
        map.put("G", 1.0);
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

    public void writeTitleWInners() {
        try {
            Workbook workbook = new HSSFWorkbook();
            Sheet sheet = workbook.createSheet("new sheet");

            int i = 0;
            for (Map.Entry<Double, Double> entry : avgTitle.entrySet()) {
                Row row = sheet.createRow(i);
                row.createCell(0).setCellValue(entry.getKey());
                row.createCell(1).setCellValue(entry.getValue());
                i++;
            }

            FileOutputStream fileOut = new FileOutputStream("titles.csv");
            workbook.write(fileOut);
            fileOut.close();
        } catch (Exception ioe) {
            ioe.printStackTrace();
        }
    }

    private double calculateMomentum(Queue<Double> momentums) {
        double momentumSum = 0;
        Iterator<Double> platesListIterator = momentums.iterator();
        while (platesListIterator.hasNext()) {
            Double entry = platesListIterator.next();
            momentumSum += entry;
        }

        return momentumSum;
    }

}
