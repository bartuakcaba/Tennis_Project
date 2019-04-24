/**
 * Created by bartu on 24/02/2019.
 */

import java.util.*;

public class Main {

    public static void main(String[] args) {

        Rankings r = new Rankings();

        r.readFile();

//        String csvFile = "../tennis_atp/atp_matches_2018.csv";
//        BufferedReader br = null;
//        String line = "";
//        String cvsSplitBy = ",";
//        Map<String, Integer> players = new HashMap<>();
//        Map<String, Integer> matchWinWeights = getWinWeights();
//        Map<String, Integer> tournyWeigths = getTourneyWeights();
//
//
//
//
//        try {
//
//            br = new BufferedReader(new FileReader(csvFile));
//            //Remove first line(headers of table)
//            br.readLine();
//            while ((line = br.readLine()) != null) {
//
//                // use comma as separator
//                String[] entry = line.split(cvsSplitBy);
//
//                //If we havent seen the name before start with a rating of 1400
//                if (!players.containsKey(entry[10])) {
//                    players.put(entry[10], 1400);
//                }
//
//                if (!players.containsKey(entry[20])) {
//                    players.put(entry[20], 1400);
//                }
//
//                //winner earns points
//                int winnerPoints = players.get(entry[10]);
//                winnerPoints += (matchWinWeights.get(entry[29])*tournyWeigths.get(entry[4]));
//                players.put(entry[10], winnerPoints);
//
//                //If loser is ranked higher loses more points
//                int loserSeed = entry[18].equals("") ? Integer.MAX_VALUE : Integer.parseInt(entry[18]);
//                int winnerSeed = entry[8].equals("") ? Integer.MAX_VALUE : Integer.parseInt(entry[8]);
//                double discountFactor = loserSeed < winnerSeed ? 1.25 : 1;
//
//                int loserPoints = players.get(entry[20]);
//                loserPoints -= (matchWinWeights.get(entry[29])*tournyWeigths.get(entry[4])*discountFactor);
//                players.put(entry[20], loserPoints);
//
//            }
//
//            Map<String, Integer> treeWinners = new TreeMap<>(players);
//
//            System.out.println(entriesSortedByValues(treeWinners));
//
//
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (br != null) {
//                try {
//                    br.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
    }

    static <K,V extends Comparable<? super V>>
    SortedSet<Map.Entry<K,V>> entriesSortedByValues(Map<K,V> map) {
        SortedSet<Map.Entry<K,V>> sortedEntries = new TreeSet<Map.Entry<K,V>>(
                new Comparator<Map.Entry<K,V>>() {
                    @Override public int compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2) {
                        int res = e1.getValue().compareTo(e2.getValue());
                        return res != 0 ? res : 1;
                    }
                }
        );
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }

    static Map<String, Integer> getWinWeights() {
        Map<String, Integer> map = new HashMap<>();

        map.put("RR", 5);
        map.put("R128", 5);
        map.put("R64", 10);
        map.put("R32", 15);
        map.put("R16", 30);
        map.put("QF", 50);
        map.put("SF", 75);
        map.put("F", 100);

        return map;
    }

    static Map<String, Integer> getTourneyWeights() {
        Map<String, Integer> map = new HashMap<>();

        map.put("D", 1);
        map.put("A", 2);
        map.put("M", 4);
        map.put("G", 8);
        return map;
    }

//To rank them
/*    if (!winners.containsKey(entry[10])) {
        winners.put(entry[10], 0);
    }

    //If they win we add points
    int point = winners.get(entry[10]);
    point += (matchWinWeights.get(entry[29])*tournyWeigths.get(entry[4]));
    winners.put(entry[10], point);*/
}
