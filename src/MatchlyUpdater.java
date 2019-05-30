import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 * Created by bartu on 22/05/2019.
 */
public class MatchlyUpdater {

    Ratings ratings;
    Ratings clayRatings;
    Ratings grassRatings;
    Ratings hardRatings;

    private Predictor predictor;
    private SetPredictor setPredictor;
    private ScoreCalculator scoreCalculator;
    private DBHandler dbhandler;

    Map<H2H, Integer[]> h2HMap = new HashMap<>();
    Map<Integer, String[]> currentTournyEntries = new HashMap<>();
    List<Player> playedThisWeek = new ArrayList<>();
    Map<Player, Integer> noOfTitles = new HashMap<>();

    Map<String, Double> matchLossWeights = getLossWeights();
    Map<String, Double> tournyWeigths = getTourneyWeights();

    public MatchlyUpdater(Predictor predictor, SetPredictor setPredictor) {
        ratings= new Ratings();
        clayRatings = new Ratings();
        grassRatings = new Ratings();
        hardRatings = new Ratings();

        this.predictor = predictor;
        this.setPredictor = setPredictor;
        this.scoreCalculator = new ScoreCalculator();
        dbhandler = new DBHandler();
    }

    public void readData(int year, boolean predictFlag) {

        BufferedReader br = null;
        String line;
        String cvsSplitBy = ",";
        String csvFile = "match_data/atp_matches_" + Integer.toString(year) + ".csv";

        try {
            br = new BufferedReader(new java.io.FileReader(csvFile));

            //Remove first line(headers of table)
            br.readLine();

            //We do not know the first tournament so we want to start with empty
            String currTournament = "";
            String currDate = "";
            String currCountry = "";
            String prevSurf = "";

            while ((line = br.readLine()) != null) {

                String[] entry = line.split(cvsSplitBy);
                if (entry.length == 0 || entry[4].equals("D") || entry[3].equals("9")) {
                    continue;
                }

                if (currTournament.equals("")) {
                    currTournament = entry[0];
                    currentTournyEntries.put(Integer.parseInt(entry[6]), entry);
                    prevSurf = entry[2];
                    currCountry = dbhandler.getCountry(entry[1]);
                } else if (!currTournament.equals(entry[0])) {
                    currTournament = entry[0];
                    updateRatings(predictFlag, prevSurf, currCountry);
                    currentTournyEntries.clear();
                    currentTournyEntries.put(Integer.parseInt(entry[6]), entry);
                    currCountry = dbhandler.getCountry(entry[1]);
                    prevSurf = entry[2];
                } else {
                    currentTournyEntries.put(Integer.parseInt(entry[6]), entry);
                }

                if (currDate.equals("")) {
                    currDate = entry[5];
                    Player winningPlayer = new Player(Integer.parseInt(entry[7]), entry[10], entry[13]);
                    Player losingPlayer = new Player(Integer.parseInt(entry[17]), entry[20], entry[23]);
                    if (!playedThisWeek.contains(winningPlayer)) {
                        playedThisWeek.add(winningPlayer);
                    }

                    if (!playedThisWeek.contains(losingPlayer)) {
                        playedThisWeek.add(losingPlayer);
                    }
                } else if (!currDate.equals(entry[5])) {
                    updateRD(entry[2]);
                    playedThisWeek.clear();
                    currDate=entry[5];
                    Player winningPlayer = new Player(Integer.parseInt(entry[7]), entry[10], entry[13]);
                    Player losingPlayer = new Player(Integer.parseInt(entry[17]), entry[20], entry[23]);
                    if (!playedThisWeek.contains(winningPlayer)) {
                        playedThisWeek.add(winningPlayer);
                    }

                    if (!playedThisWeek.contains(losingPlayer)) {
                        playedThisWeek.add(losingPlayer);
                    }
                } else {
                    Player winningPlayer = new Player(Integer.parseInt(entry[7]), entry[10], entry[13]);
                    Player losingPlayer = new Player(Integer.parseInt(entry[17]), entry[20], entry[23]);
                    if (!playedThisWeek.contains(winningPlayer)) {
                        playedThisWeek.add(winningPlayer);
                    }

                    if (!playedThisWeek.contains(losingPlayer)) {
                        playedThisWeek.add(losingPlayer);
                    }
                }

            }


            ratings.writeToExcel("ratings.xls");


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

    private void updateRD(String currSurf) {
        ratings.updateRD(playedThisWeek);

        if (currSurf.equals("Clay")) {
            clayRatings.updateRD(playedThisWeek);
        } else if (currSurf.equals("Grass")) {
            grassRatings.updateRD(playedThisWeek);
        } else {
            hardRatings.updateRD(playedThisWeek);
        }



    }

    private void updateRatings(boolean predictFlag, String prevSurf, String currCountry) {
        Map<Integer,String[]> sortedMap = new TreeMap<>(currentTournyEntries);

        for(Map.Entry<Integer, String[]> mapEntry : sortedMap.entrySet()) {
            String[] entry = mapEntry.getValue();

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

            double score = scoreCalculator.calcGameNormalised(entry[27]);
            double winnerScore = score/2 + tournyWeigths.get(entry[4])/3 + matchLossWeights.get(entry[29])/3;
            winnerScore = winnerScore > 1 ? 1 : winnerScore;
            double loserScore = (1-score)/2;
            loserScore = loserScore > 0.5 ? 0.5 : loserScore;

            double h2h = getH2H(winningPlayer, losingPlayer);

            //Can't use for ml model
            if (entry[14].equals("") || entry[24].equals("")) {
                continue;
            }

            if (entry[29].equals("F")) {
                putTitleWin(winningPlayer);
            }

            int higherTitles;
            int lowerTitles;
            double higherAge;
            double lowerAge;
            double higherMomentum;
            double lowerMomentum;
            int higherCountry;
            int lowerCountry;


            if(ratings.getRanking(winningPlayer)[0] > ratings.getRanking(losingPlayer)[0]) {
                higherTitles = noOfTitles.get(winningPlayer);
                lowerTitles = noOfTitles.get(losingPlayer);
                higherAge = Double.parseDouble(entry[14]);
                lowerAge = Double.parseDouble(entry[24]);
                higherMomentum = calculateMomentum(ratings.getMomentum(winningPlayer));
                lowerMomentum = calculateMomentum(ratings.getMomentum(losingPlayer));
                higherCountry = entry[13].equals(currCountry) ? 1 : 0;
                lowerCountry = entry[23].equals(currCountry) ? 1 : 0;
            } else {
                higherTitles = noOfTitles.get(losingPlayer);
                lowerTitles = noOfTitles.get(winningPlayer);
                higherAge = Double.parseDouble(entry[24]);
                lowerAge = Double.parseDouble(entry[14]);
                higherMomentum = calculateMomentum(ratings.getMomentum(losingPlayer));
                lowerMomentum = calculateMomentum(ratings.getMomentum(winningPlayer));
                higherCountry = entry[23].equals(currCountry) ? 1 : 0;
                lowerCountry = entry[13].equals(currCountry) ? 1 : 0;
            }

            if(predictFlag) {
//                    predictor.predictSingleMatch(ratings.getRanking(winningPlayer), ratings.getRanking(losingPlayer));

                predictor.predictWithMulRatings(ratings.getRanking(winningPlayer), ratings.getRanking(losingPlayer),
                        winnerSurfRatings,loserSurfRatings);
                predictor.addToTest(ratings.getRanking(winningPlayer), ratings.getRanking(losingPlayer),
                        winnerSurfRatings, loserSurfRatings, h2h, higherTitles, lowerTitles, higherAge,lowerAge,
                        higherMomentum,lowerMomentum, higherCountry, lowerCountry);

                //FOR SET PREDICTION
                if (!entry[4].equals("G")) {
                    setPredictor.addToTest(ratings.getRanking(winningPlayer), ratings.getRanking(losingPlayer),
                            winnerSurfRatings, loserSurfRatings, h2h, scoreCalculator.noOfSets(entry[27]));
                }
            } else {
                predictor.addToDataset(ratings.getRanking(winningPlayer), ratings.getRanking(losingPlayer),
                        winnerSurfRatings, loserSurfRatings, h2h, higherTitles, lowerTitles, higherAge,lowerAge,
                        higherMomentum,lowerMomentum, higherCountry, lowerCountry);

                //FOR SET PREDICTION
                if(!entry[4].equals("G")) {
                    setPredictor.addToDataset(ratings.getRanking(winningPlayer), ratings.getRanking(losingPlayer),
                            winnerSurfRatings, loserSurfRatings, h2h, scoreCalculator.noOfSets(entry[27]));
                }
            }

            ratings.fillMaps(winningPlayer, losingPlayer, winnerScore, loserScore);


            if (surface.equals("Clay")) {
                clayRatings.fillMaps(winningPlayer, losingPlayer, winnerScore, loserScore);
            } else if (surface.equals("Grass")) {
                grassRatings.fillMaps(winningPlayer, losingPlayer, winnerScore, loserScore);
            } else {
                //only other surface is hard
                hardRatings.fillMaps(winningPlayer, losingPlayer, winnerScore, loserScore);
            }

            ratings.updateRatingsMatchly();
            ratings.clearMaps();

            if (prevSurf.equals("Clay")) {
                clayRatings.updateRatingsMatchly();
                clayRatings.clearMaps();
            }

            if (prevSurf.equals("Grass")) {
                grassRatings.updateRatingsMatchly();
                grassRatings.clearMaps();
            }

            if (prevSurf.equals("Hard")) {
                hardRatings.updateRatingsMatchly();
                hardRatings.clearMaps();
            }
        }


    }

    private void putTitleWin(Player winningPlayer) {
        int titles = noOfTitles.get(winningPlayer);
        noOfTitles.put(winningPlayer, ++titles);
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
            double ratio =  p1wins / (p1wins + p2wins) ;

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

    private double calculateMomentum(Queue<Double> momentums) {
        double momentumSum = 0;
        Iterator<Double> platesListIterator = momentums.iterator();
        while (platesListIterator.hasNext()) {
            Double entry = platesListIterator.next();
            momentumSum += entry;
        }

        if (-100 <= momentumSum && momentumSum <= 100 ) {
            return 0;
        } else if ( momentumSum < -100) {
            return  -1;
        } else {
            return 1;
        }
    }
}
