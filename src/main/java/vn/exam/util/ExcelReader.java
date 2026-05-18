package vn.exam.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import vn.exam.model.CanBo;
import vn.exam.model.PhongThi;

public class ExcelReader {
    private final DataFormatter formatter = new DataFormatter();

    public List<CanBo> readCanBo(String filePath) throws IOException {
        File file = validateFile(filePath);
        List<CanBo> result = new ArrayList<CanBo>();
        FileInputStream inputStream = new FileInputStream(file);
        Workbook workbook = null;
        try {
            workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isBlankRow(row, 5)) {
                    continue;
                }
                result.add(new CanBo(
                        parseInt(getCellValue(row, 0), result.size() + 1),
                        getCellValue(row, 1),
                        getCellValue(row, 2),
                        getCellValue(row, 3),
                        getCellValue(row, 4)));
            }
        } finally {
            if (workbook != null) {
                workbook.close();
            }
            inputStream.close();
        }
        return result;
    }

    public List<PhongThi> readPhongThi(String filePath) throws IOException {
        File file = validateFile(filePath);
        List<PhongThi> result = new ArrayList<PhongThi>();
        FileInputStream inputStream = new FileInputStream(file);
        Workbook workbook = null;
        try {
            workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isBlankRow(row, 3)) {
                    continue;
                }
                result.add(new PhongThi(
                        parseInt(getCellValue(row, 0), result.size() + 1),
                        getCellValue(row, 1),
                        getCellValue(row, 2)));
            }
        } finally {
            if (workbook != null) {
                workbook.close();
            }
            inputStream.close();
        }
        return result;
    }

    private File validateFile(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            throw new IOException("Không tìm thấy file Excel: " + filePath);
        }
        return file;
    }

    private boolean isBlankRow(Row row, int numberOfColumns) {
        for (int i = 0; i < numberOfColumns; i++) {
            if (getCellValue(row, i).length() > 0) {
                return false;
            }
        }
        return true;
    }

    private String getCellValue(Row row, int index) {
        Cell cell = row.getCell(index);
        return cell == null ? "" : formatter.formatCellValue(cell).trim();
    }

    private int parseInt(String value, int defaultValue) {
        if (value == null || value.trim().length() == 0) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
