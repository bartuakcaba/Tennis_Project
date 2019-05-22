import java.util.Objects;

/**
 * Created by bartu on 21/05/2019.
 */
public class H2H {

    String player1;
    String player2;

    public H2H(String player1, String player2) {
        this.player1 = player1;
        this.player2 = player2;

    }

    @Override
    public boolean equals(Object o) {

        if (o == this) return true;
        if (!(o instanceof H2H)) {
            return false;
        }

        H2H h2h = (H2H) o;

        return h2h.player1.equals(this.player1) &&
                Objects.equals(h2h.player2, this.player2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(player1) + Objects.hash(player2);
    }
}
