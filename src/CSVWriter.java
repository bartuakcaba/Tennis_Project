import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by bartu on 08/05/2019.
 */
public class CSVWriter {

    public void writeRatings(Map<Player, Double[]> rankings) {

        Set<Player> players = rankings.keySet();
        try {
            Workbook workbook = new HSSFWorkbook();
            Sheet sheet = workbook.createSheet("new sheet");

            int i = 0;
            for (Player player : players) {
                Double[] ranking = rankings.get(player);
                Row row = sheet.createRow(i);
                row.createCell(1).setCellValue(player.getName());
                row.createCell(2).setCellValue(ranking[0]);
                row.createCell(3).setCellValue(ranking[1]);
                row.createCell(4).setCellValue(ranking[2]);
                i++;
            }

            FileOutputStream fileOut = new FileOutputStream("ratings.xls");
            workbook.write(fileOut);
            fileOut.close();
        } catch (Exception ioe) {
            ioe.printStackTrace();
        }

    }

    public void writeOpponents(Map<Player, List<Double[]>> opponents) {
        try {
            Workbook workbook = new HSSFWorkbook();
            Sheet sheet = workbook.createSheet("new sheet");

            int i = 0;
            for (Map.Entry<Player, List<Double[]>> entry : opponents.entrySet()) {
                Row row = sheet.createRow(i);
                row.createCell(1).setCellValue(entry.getKey().getName());
                int j = 2;
                for (Double[] player : entry.getValue()) {
                    row.createCell(j).setCellValue(player[0]);
                    j++;
                }
                i++;
            }

            FileOutputStream fileOut = new FileOutputStream("opponents.xls");
            workbook.write(fileOut);
            fileOut.close();
        } catch (Exception ioe) {
            ioe.printStackTrace();
        }

    }

    public void writeAllPredictions(List<Double[]> predList) {
        try {
            Workbook workbook = new HSSFWorkbook();
            Sheet sheet = workbook.createSheet("new sheet");

            Row row = sheet.createRow(0);
            row.createCell(0).setCellValue("Winner R");
            row.createCell(1).setCellValue("Winner RD");
            row.createCell(2).setCellValue("Loser R");
            row.createCell(3).setCellValue("Loser RD");
            row.createCell(4).setCellValue("Correct Prediction");
            row.createCell(5).setCellValue("Difference in R");



            int i = 1;
            for (Double[] pred : predList) {
                Row row1 = sheet.createRow(i);
                row1.createCell(0).setCellValue(pred[0]);
                row1.createCell(1).setCellValue(pred[1]);
                row1.createCell(2).setCellValue(pred[2]);
                row1.createCell(3).setCellValue(pred[3]);
                row1.createCell(4).setCellValue(pred[4]);
                row1.createCell(5).setCellValue(pred[0] - pred[2]);
                i++;
            }

            FileOutputStream fileOut = new FileOutputStream("predictions.xls");
            workbook.write(fileOut);
            fileOut.close();
        } catch (Exception ioe) {
            ioe.printStackTrace();
        }
    }
}
