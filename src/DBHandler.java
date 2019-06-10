/**
 * Created by bartu on 29/04/2019.
 */

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;

public class DBHandler {

    public void createDatabse(String fileName) {

        String url = "jdbc:sqlite:sqlite/" + fileName;

        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("A new database has been created.");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    public void createTable(String query) {

        String url = "jdbc:sqlite:sqlite/tennis.db";
        Connection conn = null;
        Statement stmt = null;

        try  {
            conn = DriverManager.getConnection(url);
            stmt = conn.createStatement();

            stmt.executeUpdate(query);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            try{
                if(stmt!=null)
                    conn.close();
            }catch(SQLException se){
            }// do nothing
            try{
                if(conn!=null)
                    conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }//end finally try
        }
    }

    public void fillH2HTable(int year) {
        String csvFile = "match_data/atp_matches_" + Integer.toString(year) + ".csv";
        BufferedReader br = null;
        String line;
        String cvsSplitBy = ",";
        String url = "jdbc:sqlite:sqlite/tennis.db";
        Connection conn = null;
        Statement stmt = null;

            try {
                br = new BufferedReader(new FileReader(csvFile));
                //Remove first line(headers of table)
                br.readLine();

                conn = DriverManager.getConnection(url);
                stmt = conn.createStatement();


                while ((line = br.readLine()) != null) {
                    String[] entry = line.split(cvsSplitBy);

                    //At the olympics there is an entry every other line
                    if (entry.length == 0) {
                        continue;
                    }

                    String winningPlayer = entry[10].replaceAll("\\s+", "");
                    String losingPlayer = entry[20].replaceAll("\\s+", "");


                    if (winningPlayer.compareTo(losingPlayer) < 0) {
                        String searchQuery = "SELECT P1_Score, P2_Score " +
                                "from " + "Head_To_Head " +
                                "where " + "Player1 = ? AND Player2 = ?";
                        PreparedStatement pstmt = conn.prepareStatement(searchQuery);

                        pstmt.setString(1, winningPlayer);
                        pstmt.setString(2, losingPlayer);

                        ResultSet rs = pstmt.executeQuery();

                        //Means they haven't played before so we need to insert
                        if (!rs.isBeforeFirst()) {
                            stmt.executeUpdate("INSERT INTO Head_To_Head " + "VALUES ('" + winningPlayer + "', '" + losingPlayer + "', 1, 0);");
                        } else {
                            int p1Score = rs.getInt("P1_Score") + 1;
                            int p2Score = rs.getInt("P2_Score");
                            String updateQuery = "UPDATE Head_To_Head SET P1_Score = ?, P2_Score = ? WHERE Player1 = ? AND Player2 = ?";
                            PreparedStatement preparedStmt = conn.prepareStatement(updateQuery);
                            preparedStmt.setInt(1, p1Score);
                            preparedStmt.setInt(2, p2Score);
                            preparedStmt.setString(3, winningPlayer);
                            preparedStmt.setString(4, losingPlayer);

                            preparedStmt.executeUpdate();
                        }
                    } else {
                        String searchQuery = "SELECT P1_Score, P2_Score " +
                                "from " + "Head_To_Head " +
                                "where " + "Player1 = ? AND Player2 = ?";
                        PreparedStatement pstmt = conn.prepareStatement(searchQuery);

                        pstmt.setString(1, losingPlayer);
                        pstmt.setString(2, winningPlayer);

                        ResultSet rs = pstmt.executeQuery();

                        //Means they haven't played before so we need to insert
                        if (!rs.isBeforeFirst()) {
                            stmt.executeUpdate("INSERT INTO Head_To_Head " + "VALUES ('" + losingPlayer + "', '" + winningPlayer + "', 0, 1);");
                        } else {
                            int p1Score = rs.getInt("P1_Score");
                            int p2Score = rs.getInt("P2_Score") + 1;
                            String updateQuery = "UPDATE Head_To_Head SET P1_Score = ?, P2_Score = ? WHERE Player1 = ? AND Player2 = ?";
                            PreparedStatement preparedStmt = conn.prepareStatement(updateQuery);
                            preparedStmt.setInt(1, p1Score);
                            preparedStmt.setInt(2, p2Score);
                            preparedStmt.setString(3, losingPlayer);
                            preparedStmt.setString(4, winningPlayer);

                            preparedStmt.executeUpdate();
                        }
                    }
                }


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    if (stmt != null)
                        conn.close();
                } catch (SQLException se) {
                }// do nothing
                try {
                    if (conn != null)
                        conn.close();
                } catch (SQLException se) {
                    se.printStackTrace();
                }
            }

    }

    public double getH2H(String winningPlayer, String losingPlayer) {
        String url = "jdbc:sqlite:sqlite/tennis.db";
        Connection conn = null;
        Statement stmt = null;

        String winner = winningPlayer.replaceAll("\\s+", "");
        String loser = losingPlayer.replaceAll("\\s+", "");

        String searchQuery = "SELECT P1_Score, P2_Score " +
                "from " + "Head_To_Head " +
                "where " + "Player1 = ? AND Player2 = ?";

        try  {
            conn = DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(searchQuery);
            boolean winnerAlph = winner.compareTo(loser) < 0;

            if (winnerAlph) {
                pstmt.setString(1, winner);
                pstmt.setString(2, loser);
            } else {
                pstmt.setString(1, loser);
                pstmt.setString(2, winner);
            }

            ResultSet rs = pstmt.executeQuery();

            int p1Score = rs.getInt("P1_Score");
            int p2Score = rs.getInt("P2_Score");

            double ratio = p1Score / (p1Score+p2Score);

            return winnerAlph ? ratio : 1-ratio;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return -1;
        } finally {
            try{
                if(stmt!=null)
                    conn.close();
            }catch(SQLException se){
            }// do nothing
            try{
                if(conn!=null)
                    conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }//end finally try
        }
    }

    public String getCountry(String city) {
        String url = "jdbc:sqlite:sqlite/tennis.db";
        Connection conn = null;
        Statement stmt = null;

        String searchQuery = "SELECT Country " +
                "from " + "Countries " +
                "where " + "City = ?";

        try {

            conn = DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(searchQuery);
            pstmt.setString(1, city);
            ResultSet rs = pstmt.executeQuery();
            return rs.getString("Country");

        }catch (SQLException e) {
            System.out.println(e.getMessage());
            return "No Country";
        } finally {
            try{
                if(stmt!=null)
                    conn.close();
            }catch(SQLException se){
            }// do nothing
            try{
                if(conn!=null)
                    conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }//end finally try
        }

    }

    public void fillAgeTable(int year) {
        String csvFile = "match_data/atp_matches_" + Integer.toString(year) + ".csv";
        BufferedReader br = null;
        String line;
        String cvsSplitBy = ",";
        String url = "jdbc:sqlite:sqlite/tennis.db";
        Connection conn = null;
        Statement stmt = null;

        try {
            br = new BufferedReader(new FileReader(csvFile));
            //Remove first line(headers of table)
            br.readLine();

            conn = DriverManager.getConnection(url);
            stmt = conn.createStatement();


            while ((line = br.readLine()) != null) {
                String[] entry = line.split(cvsSplitBy);

                //At the olympics there is an entry every other line
                if (entry.length == 0) {
                    continue;
                }

                String winningPlayer = entry[10].replaceAll("\\s+", "");
                String losingPlayer = entry[20].replaceAll("\\s+", "");
                if (entry[24].isEmpty() || entry[14].isEmpty()) {
                    continue;
                }
                int winningAge = (int) Double.parseDouble(entry[14]);
                int losingAge = (int) Double.parseDouble(entry[24]);


                String searchQuery = "SELECT Age " +
                        "from " + "Age " +
                        "where " + "Player = ?";
                PreparedStatement pstmt = conn.prepareStatement(searchQuery);

                pstmt.setString(1, winningPlayer);

                ResultSet rs = pstmt.executeQuery();

                //Means they haven't played before so we need to insert
                if (!rs.isBeforeFirst()) {
                    stmt.executeUpdate("INSERT INTO Age " + "VALUES ('" + winningPlayer + "', " + winningAge + ");");
                } else {
                    String updateQuery = "UPDATE Age SET Age = ? WHERE Player = ?";
                    PreparedStatement preparedStmt = conn.prepareStatement(updateQuery);
                    preparedStmt.setInt(1, winningAge);
                    preparedStmt.setString(2, winningPlayer);
                    preparedStmt.executeUpdate();
                }

                pstmt.setString(1, losingPlayer);

                ResultSet rs1 = pstmt.executeQuery();

                if (!rs1.isBeforeFirst()) {
                    stmt.executeUpdate("INSERT INTO Age " + "VALUES ('" + losingPlayer + "', " + losingAge + ");");
                } else {
                    String updateQuery = "UPDATE Age SET Age = ? WHERE Player = ?";
                    PreparedStatement preparedStmt = conn.prepareStatement(updateQuery);
                    preparedStmt.setInt(1, losingAge);
                    preparedStmt.setString(2, losingPlayer);
                    preparedStmt.executeUpdate();
                }

            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                if (stmt != null)
                    conn.close();
            } catch (SQLException se) {
            }// do nothing
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }

    }

    public int getAge(String player) {
        String url = "jdbc:sqlite:sqlite/tennis.db";
        Connection conn = null;
        Statement stmt = null;

        String p = player.replaceAll("\\s+", "");

        String searchQuery = "SELECT Age " +
                "from " + "Age " +
                "where " + "Player = ?";

        try  {
            conn = DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(searchQuery);

            pstmt.setString(1, p);

            ResultSet rs = pstmt.executeQuery();

            int age = rs.getInt("Age");

            return age;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return 0;
        } finally {
            try{
                if(stmt!=null)
                    conn.close();
            }catch(SQLException se){
            }// do nothing
            try{
                if(conn!=null)
                    conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }//end finally try
        }
    }

}
