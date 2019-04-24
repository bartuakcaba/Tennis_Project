import java.util.Objects;

/**
 * Created by bartu on 17/04/2019.
 */
public class Player {

    private int id;
    private String name;
    private String country;
    private Double[] rating;

    public Player(int id, String name, String country) {
        this.id = id;
        this.name = name;
        this.country = country;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Double[] getRating() {
        return rating;
    }

    public void setRating(Double[] rating) {
        this.rating = rating;
    }

    @Override
    public boolean equals(Object o) {

        if (o == this) return true;
        if (!(o instanceof Player)) {
            return false;
        }

        Player player = (Player) o;

        return id == player.id &&
                Objects.equals(name, player.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
