import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by bartu on 28/04/2019.
 */
public class Predictor {

    private int correctGuesses;
    private int allGuesses;

    public Predictor() {
        correctGuesses = 0;
        allGuesses = 0;
    }


    public void predictSingleMatch(Double[] winnerRatings, Double[] loserRatings) {

        if (winnerRatings[0] > loserRatings[0]) {
            correctGuesses++;
        }

        allGuesses++;
    }

    public double calculateAccuracy() {
        return ((double) correctGuesses/allGuesses) * 100;
    }
}
