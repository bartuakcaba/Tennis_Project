/**
 * Created by bartu on 17/04/2019.
 */
public class Tournament {

    private int id;
    private String name;
    private int draw_size;
    private int points;
    private String country;

    public Tournament(int id, String name, int draw_size, int points, String country) {
        this.id = id;
        this.name = name;
        this.draw_size = draw_size;
        this.points = points;
        this.country = country;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDraw_size() {
        return draw_size;
    }

    public void setDraw_size(int draw_size) {
        this.draw_size = draw_size;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
