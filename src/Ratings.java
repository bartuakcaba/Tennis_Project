import java.util.*;

/**
 * Created by bartu on 05/05/2019.
 */

public class Ratings {

    private HashMap<Player, Double[]> rankings;
    private HashMap<Player, List<Double[]>> opponents;
    private HashMap<Player, List<Double>> scores;
    private HashMap<Player, Integer> timeNotPlayed;

    private Glicko2 rater;
    private CSVWriter writer;

    public Ratings() {
        rankings = new HashMap<>();
        opponents = new HashMap<>();
        scores = new HashMap<>();
        timeNotPlayed = new HashMap<>();
        rater = new Glicko2();
        writer = new CSVWriter();
    }

    public void updateRatings() {
        writeToExcel2(opponents);
//        scores = normaliseRankings(scores);

        for(Map.Entry<Player, Double[]> rating : rankings.entrySet()) {
            Double[] convertedRatings = rater.convertGlicko2(rating.getValue());
            rankings.put(rating.getKey(), convertedRatings);
        }

        //turn Rankings into Glicko-2 scale
        for (Map.Entry<Player, Double[]> ranking : rankings.entrySet()) {
            Double[] glicko2Rating = ranking.getValue();
            List<Double[]> rivalRatings = opponents.get(ranking.getKey());

            if (rivalRatings.isEmpty()) {
                Double[] newRating = rater.updateUnplayedPlayer(glicko2Rating);
                Double[] convertedBack = rater.convertBack(newRating);
                rankings.put(ranking.getKey(), convertedBack);
                if (!timeNotPlayed.containsKey(ranking.getKey())) {
                    timeNotPlayed.put(ranking.getKey(), 1);
                } else {
                    int times = timeNotPlayed.get(ranking.getKey());
                    times++;
                    if (times >= 52) {
                        Double[] playerRating = ranking.getValue();
                        playerRating[0] = playerRating[0] * 0.97;
                        rankings.put(ranking.getKey(), playerRating);
                    }
                    timeNotPlayed.put(ranking.getKey(), times);
                }
                continue;
            }


            timeNotPlayed.put(ranking.getKey(), 0);

            Double v = rater.calcV(glicko2Rating, rivalRatings);

            Double diff = rater.calcEstImprovement(v, scores.get(ranking.getKey()), glicko2Rating, rivalRatings);

            Double newVol = rater.calcNewVol(glicko2Rating[1], diff, v, glicko2Rating[2]);

            Double newRD = rater.updateRD(glicko2Rating[1], newVol);

            Double[] newRatings = rater.updateRating(v, scores.get(ranking.getKey()), glicko2Rating, rivalRatings, newRD, newVol);

            Double[] convertedBack = rater.convertBack(newRatings);

            rankings.put(ranking.getKey(), convertedBack);
        }

    }

    public void updateRatingsMatchly() {
        writeToExcel2(opponents);
//        scores = normaliseRankings(scores);

        for(Map.Entry<Player, Double[]> rating : rankings.entrySet()) {
            Double[] convertedRatings = rater.convertGlicko2(rating.getValue());
            rankings.put(rating.getKey(), convertedRatings);
        }

        //turn Rankings into Glicko-2 scale
        for (Map.Entry<Player, Double[]> ranking : rankings.entrySet()) {
            Double[] glicko2Rating = ranking.getValue();
            List<Double[]> rivalRatings = opponents.get(ranking.getKey());

            if (rivalRatings.isEmpty()) {
                rankings.put(ranking.getKey(), rater.convertBack(glicko2Rating));
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

    public void writeToExcel(String filename) {
        writer.writeRatings(rankings, filename);
    }

    private void writeToExcel2(Map<Player, List<Double[]>> opponents) {
        writer.writeOpponents(opponents);
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

    public boolean areMapsEmpty() {
        return scores.isEmpty();
    }

    public void updateRD(List<Player> playedThisWeek) {

        for (Map.Entry<Player, Double[]> ranking : rankings.entrySet()) {
            if (!playedThisWeek.contains(ranking.getKey())) {
                Double[] glicko2Rating = ranking.getValue();
                 glicko2Rating = rater.convertGlicko2(ranking.getValue());
                Double[] newRating = rater.updateUnplayedPlayer(glicko2Rating);
                Double[] convertedBack = rater.convertBack(newRating);
                rankings.put(ranking.getKey(), convertedBack);
            }
        }
    }


}
