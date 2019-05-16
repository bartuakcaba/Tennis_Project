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

    public void writeRatings(Map<Player, Double[]> rankings, String filename) {

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

            FileOutputStream fileOut = new FileOutputStream(filename);
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

    public void writeAllPredictions(List<double[]> predList, List<String[]> names) {
        try {
            Workbook workbook = new HSSFWorkbook();
            Sheet sheet = workbook.createSheet("new sheet");

            Row row = sheet.createRow(0);
            row.createCell(0).setCellValue("Higher");
            row.createCell(1).setCellValue("Lower");
            row.createCell(2).setCellValue("Tournament");
            row.createCell(3).setCellValue("Higher R");
            row.createCell(4).setCellValue("Lower R");
            row.createCell(5).setCellValue("High Surf R");
            row.createCell(6).setCellValue("Low Surf R");
            row.createCell(7).setCellValue("H2H");
            row.createCell(8).setCellValue("Winner");
            row.createCell(9).setCellValue("Loser");
            row.createCell(10).setCellValue("Higher Surf");



            int i = 1;
            for (double[] pred : predList) {

                    Row row1 = sheet.createRow(i);
                    row1.createCell(0).setCellValue(names.get(i - 1)[0]);
                    row1.createCell(1).setCellValue(names.get(i - 1)[1]);
                    row1.createCell(2).setCellValue(names.get(i - 1)[2]);
                    row1.createCell(3).setCellValue(pred[0]);
                    row1.createCell(4).setCellValue(pred[1]);
                    row1.createCell(5).setCellValue(pred[2]);
                    row1.createCell(6).setCellValue(pred[3]);
                    row1.createCell(7).setCellValue(pred[4]);
                    row1.createCell(8).setCellValue(names.get(i - 1)[3]);
                    row1.createCell(9).setCellValue(names.get(i - 1)[4]);
                    row1.createCell(10).setCellValue(names.get(i - 1)[5]);

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
