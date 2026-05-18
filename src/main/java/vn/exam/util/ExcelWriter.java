package vn.exam.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import vn.exam.model.AssignmentResult;
import vn.exam.model.GiamSat;
import vn.exam.model.PhanCong;

public class ExcelWriter {
    public void writeAssignmentResult(AssignmentResult result, String outputPath) throws IOException {
        File outputFile = new File(outputPath);
        File parent = outputFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        Workbook workbook = new XSSFWorkbook();
        try {
            CellStyle headerStyle = createHeaderStyle(workbook);
            writePhanCongSheet(workbook, result, headerStyle);
            writeGiamSatSheet(workbook, result, headerStyle);
            writeThongKeSheet(workbook, result, headerStyle);

            FileOutputStream outputStream = new FileOutputStream(outputFile);
            try {
                workbook.write(outputStream);
            } finally {
                outputStream.close();
            }
        } finally {
            workbook.close();
        }
    }

    private void writePhanCongSheet(Workbook workbook, AssignmentResult result, CellStyle headerStyle) {
        Sheet sheet = workbook.createSheet("Danh sách phân công");
        createHeader(sheet, headerStyle, new String[] {"STT", "Mã GV", "Họ và tên", "Giám thị 1", "Giám thị 2", "Phòng thi"});
        int rowIndex = 1;
        for (PhanCong phanCong : result.getDanhSachPhanCong()) {
            Row row = sheet.createRow(rowIndex++);
            setCell(row, 0, phanCong.getStt());
            setCell(row, 1, phanCong.getCanBo().getMaGv());
            setCell(row, 2, phanCong.getCanBo().getHoTen());
            setCell(row, 3, phanCong.isGiamThi1() ? "X" : "");
            setCell(row, 4, phanCong.isGiamThi2() ? "X" : "");
            setCell(row, 5, phanCong.getPhongThi().getTenPhong());
        }
        autoSize(sheet, 6);
    }

    private void writeGiamSatSheet(Workbook workbook, AssignmentResult result, CellStyle headerStyle) {
        Sheet sheet = workbook.createSheet("Danh sách giám sát");
        createHeader(sheet, headerStyle, new String[] {"STT", "Mã GV", "Họ và tên", "Phòng thi được giám sát"});
        int rowIndex = 1;
        for (GiamSat giamSat : result.getDanhSachGiamSat()) {
            Row row = sheet.createRow(rowIndex++);
            setCell(row, 0, giamSat.getStt());
            setCell(row, 1, giamSat.getCanBo().getMaGv());
            setCell(row, 2, giamSat.getCanBo().getHoTen());
            setCell(row, 3, giamSat.getPhongThiDuocGiamSat().getTenPhong());
        }
        autoSize(sheet, 4);
    }

    private void writeThongKeSheet(Workbook workbook, AssignmentResult result, CellStyle headerStyle) {
        Sheet sheet = workbook.createSheet("Thống kê");
        createHeader(sheet, headerStyle, new String[] {"Nội dung", "Giá trị"});
        int rowIndex = 1;
        rowIndex = addStatistic(sheet, rowIndex, "Số phòng thi sử dụng", result.getSoPhongThiSuDung());
        rowIndex = addStatistic(sheet, rowIndex, "Số cán bộ giám sát", result.getSoCanBoGiamSat());
        rowIndex = addStatistic(sheet, rowIndex, "Tổng cán bộ cần dùng", result.getTongCanBoCanDung());
        rowIndex = addStatistic(sheet, rowIndex, "Tổng phòng trong file", result.getTongPhongTrongFile());
        addStatistic(sheet, rowIndex, "Tổng cán bộ trong file", result.getTongCanBoTrongFile());
        autoSize(sheet, 2);
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private void createHeader(Sheet sheet, CellStyle headerStyle, String[] headers) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private int addStatistic(Sheet sheet, int rowIndex, String name, int value) {
        Row row = sheet.createRow(rowIndex);
        setCell(row, 0, name);
        setCell(row, 1, value);
        return rowIndex + 1;
    }

    private void setCell(Row row, int index, String value) {
        row.createCell(index).setCellValue(value == null ? "" : value);
    }

    private void setCell(Row row, int index, int value) {
        row.createCell(index).setCellValue(value);
    }

    private void autoSize(Sheet sheet, int numberOfColumns) {
        for (int i = 0; i < numberOfColumns; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}
