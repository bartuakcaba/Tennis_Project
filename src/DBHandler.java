/**
 * Created by bartu on 29/04/2019.
 */

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;

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

    public void fillH2HTable() {
        int year = 1992;
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

                    String winningPlayer = entry[10];
                    String losingPlayer = entry[20];


                    if (winningPlayer.compareTo(losingPlayer) > 0) {
                        String searchQuery = "SELECT P1_Score, P2_Score " +
                                "from " + "Head_To_Head " +
                                "where " + "Player1 = ? AND Player2 = ?";
                        PreparedStatement pstmt  = conn.prepareStatement(searchQuery);

                        pstmt.setString(1, winningPlayer.replaceAll("\\s+",""));
                        pstmt.setString(2, losingPlayer.replaceAll("\\s+",""));

                        System.out.println(pstmt.toString());

                        ResultSet rs = pstmt.executeQuery();

                        //Means they haven't played before so we need to insert
                        if (!rs.isBeforeFirst()) {
                            stmt.executeUpdate("INSERT INTO Head_To_Head " + "VALUES (" + winningPlayer + ", " + losingPlayer + ", 1, 0);");
                        } else {
                            int p1Score = rs.getInt("P1Score");
                            int p2Score = rs.getInt("P2Score");
                            stmt.executeUpdate("INSERT INTO Head_To_Head " + "VALUES (" + winningPlayer + ", " + losingPlayer + ", "
                                    + ++p1Score + ", " + p2Score + ");");
                        }
                    } else {
                        String searchQuery = "SELECT P1_Score, P2_Score " +
                                "from " + "Head_To_Head " +
                                "where " + "Player1 = ? AND Player2 = ?";
                        PreparedStatement pstmt  = conn.prepareStatement(searchQuery);

                        pstmt.setString(1, losingPlayer.replaceAll("\\s+",""));
                        pstmt.setString(2, winningPlayer.replaceAll("\\s+",""));

                        System.out.println(pstmt.toString());

                        ResultSet rs = pstmt.executeQuery();

                        //Means they haven't played before so we need to insert
                        if (!rs.isBeforeFirst()) {
                            stmt.executeUpdate("INSERT INTO Head_To_Head " + "VALUES (" + losingPlayer + ", " + winningPlayer + ", 0, 1);");
                        } else {
                            int p1Score = rs.getInt("P1Score");
                            int p2Score = rs.getInt("P2Score");
                            stmt.executeUpdate("INSERT INTO Head_To_Head " + "VALUES (" + winningPlayer + ", " + losingPlayer + ", "
                                    + p1Score + ", " + ++p2Score + ");");
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
}
