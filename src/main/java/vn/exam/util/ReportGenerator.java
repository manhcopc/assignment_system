package vn.exam.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import vn.exam.model.AssignmentResult;
import vn.exam.model.GiamSat;
import vn.exam.model.PhanCong;

/**
 * Ghi báo cáo phân công cán bộ coi thi theo format Excel chuẩn
 * 
 * Tạo 3 file chính:
 * 1. DANHSACHPHANCONG_ca{N}.XLSX - Danh sách phân công giám thị
 * 2. DANHSACHGIAMSAT_ca{N}.XLSX - Danh sách giám sát
 * 3. THONGKE_ca{N}.XLSX - Thống kê tổng quát
 */
public class ReportGenerator {

    private static final int ROWS_PER_SHEET = 20;
    private static final int OUTPUT_DATA_START_ROW_INDEX = 4;
    private static final int HEADER_ROW_INDEX = 3;
    private static final int SXSSF_WINDOW_SIZE = 100;
    private static final int COLUMN_WIDTH = 18 * 256;

    /**
     * Ghi 3 file báo cáo Excel chuẩn với tên file bao gồm ca thi
     * @param caThi - Số ca thi (ví dụ: 1, 2, 3)
     */
    public List<File> generateReports(AssignmentResult result, String outputDirectoryPath,
            int soPhongThi, int soGiamThi, int soCaThi, int retryCount, int caThi) throws IOException {
        File outputDirectory = new File(outputDirectoryPath);
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }
        if (!outputDirectory.isDirectory()) {
            throw new IOException("Đường dẫn output không phải thư mục: " + outputDirectoryPath);
        }

        List<File> files = new ArrayList<>();

        File file1 = new File(outputDirectory, "DANHSACHPHANCONG_ca" + caThi + ".XLSX");
        writeDanhSachPhanCong(file1, result.getDanhSachPhanCong());
        files.add(file1);

        File file2 = new File(outputDirectory, "DANHSACHGIAMSAT_ca" + caThi + ".XLSX");
        writeDanhSachGiamSat(file2, result.getDanhSachGiamSat());
        files.add(file2);

        File file3 = new File(outputDirectory, "THONGKE_ca" + caThi + ".XLSX");
        writeThongKe(file3, result, soPhongThi, soGiamThi, soCaThi, retryCount);
        files.add(file3);

        return files;
    }

    private void writeDanhSachPhanCong(File outputFile, List<PhanCong> danhSachPhanCong) throws IOException {
        ensureParentDir(outputFile);

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(SXSSF_WINDOW_SIZE);
                FileOutputStream outputStream = new FileOutputStream(outputFile)) {

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle bodyStyleCenter = createBodyStyle(workbook, HorizontalAlignment.CENTER);
            CellStyle bodyStyleLeft = createBodyStyle(workbook, HorizontalAlignment.LEFT);

            int totalSheets = Math.max(1, (int) Math.ceil(danhSachPhanCong.size() / (double) ROWS_PER_SHEET));

            for (int sheetIndex = 0; sheetIndex < totalSheets; sheetIndex++) {
                SXSSFSheet sheet = workbook.createSheet("Phân công " + (sheetIndex + 1));

                createNationalHeader(sheet, headerStyle, 6);
                createHeaderRow(sheet, HEADER_ROW_INDEX, headerStyle,
                        "STT", "Mã GV", "Họ tên", "Giám thị 1", "Giám thị 2", "Phòng thi");

                int fromIndex = sheetIndex * ROWS_PER_SHEET;
                int toIndex = Math.min(fromIndex + ROWS_PER_SHEET, danhSachPhanCong.size());

                int rowIndex = OUTPUT_DATA_START_ROW_INDEX;
                for (int i = fromIndex; i < toIndex; i++) {
                    PhanCong phanCong = danhSachPhanCong.get(i);
                    Row row = sheet.createRow(rowIndex++);

                    createCell(row, 0, phanCong.getStt(), bodyStyleCenter);
                    createCell(row, 1, phanCong.getCanBo().getMaGv(), bodyStyleCenter);
                    createCell(row, 2, phanCong.getCanBo().getHoTen(), bodyStyleLeft);
                    String giamThi1 = "Giám thị 1".equals(phanCong.getVaiTro()) ? "X" : "";
                    createCell(row, 3, giamThi1, bodyStyleCenter);
                    String giamThi2 = "Giám thị 2".equals(phanCong.getVaiTro()) ? "X" : "";
                    createCell(row, 4, giamThi2, bodyStyleCenter);
                    createCell(row, 5, phanCong.getPhongThi().getTenPhong(), bodyStyleCenter);
                }

                applySheetLayout(sheet);
                setColumnWidths(sheet, 6);
            }

            workbook.write(outputStream);
            workbook.dispose();
        }
    }

    private void writeDanhSachGiamSat(File outputFile, List<GiamSat> danhSachGiamSat) throws IOException {
        ensureParentDir(outputFile);

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(SXSSF_WINDOW_SIZE);
                FileOutputStream outputStream = new FileOutputStream(outputFile)) {

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle bodyStyleCenter = createBodyStyle(workbook, HorizontalAlignment.CENTER);
            CellStyle bodyStyleLeft = createBodyStyle(workbook, HorizontalAlignment.LEFT);

            int totalSheets = Math.max(1, (int) Math.ceil(danhSachGiamSat.size() / (double) ROWS_PER_SHEET));

            for (int sheetIndex = 0; sheetIndex < totalSheets; sheetIndex++) {
                SXSSFSheet sheet = workbook.createSheet("Giám sát " + (sheetIndex + 1));

                createNationalHeader(sheet, headerStyle, 4);
                createHeaderRow(sheet, HEADER_ROW_INDEX, headerStyle,
                        "STT", "Mã GV", "Họ tên", "Phòng thi được giám sát");

                int fromIndex = sheetIndex * ROWS_PER_SHEET;
                int toIndex = Math.min(fromIndex + ROWS_PER_SHEET, danhSachGiamSat.size());

                int rowIndex = OUTPUT_DATA_START_ROW_INDEX;

                if (toIndex <= fromIndex && sheetIndex == 0) {
                    Row row = sheet.createRow(rowIndex);
                    createCell(row, 0, 1, bodyStyleCenter);
                    createCell(row, 1, "", bodyStyleCenter);
                    createCell(row, 2, "", bodyStyleLeft);
                    createCell(row, 3, "Không có cán bộ giám sát", bodyStyleLeft);
                } else {
                    for (int i = fromIndex; i < toIndex; i++) {
                        GiamSat giamSat = danhSachGiamSat.get(i);
                        Row row = sheet.createRow(rowIndex++);

                        createCell(row, 0, giamSat.getStt(), bodyStyleCenter);
                        createCell(row, 1, giamSat.getCanBo().getMaGv(), bodyStyleCenter);
                        createCell(row, 2, giamSat.getCanBo().getHoTen(), bodyStyleLeft);
                        createCell(row, 3, giamSat.getPhongThiDuocGiamSat().getTenPhong(), bodyStyleLeft);
                    }
                }

                applySheetLayout(sheet);
                setColumnWidths(sheet, 4);
            }

            workbook.write(outputStream);
            workbook.dispose();
        }
    }

    private void writeThongKe(File outputFile, AssignmentResult result,
            int soPhongThi, int soGiamThi, int soCaThi, int retryCount) throws IOException {
        ensureParentDir(outputFile);

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(SXSSF_WINDOW_SIZE);
                FileOutputStream outputStream = new FileOutputStream(outputFile)) {

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle bodyStyleLeft = createBodyStyle(workbook, HorizontalAlignment.LEFT);

            SXSSFSheet sheet = workbook.createSheet("Thống kê");

            createNationalHeader(sheet, headerStyle, 2);
            createHeaderRow(sheet, HEADER_ROW_INDEX, headerStyle, "Nội dung", "Giá trị");

            int rowIndex = OUTPUT_DATA_START_ROW_INDEX;
            int soGiamThiCan = soPhongThi * 2;
            int soCanBoGiamSat = soGiamThi - soGiamThiCan;

            rowIndex = addStatisticRow(sheet, rowIndex, "Số phòng thi sử dụng", String.valueOf(soPhongThi),
                    bodyStyleLeft);
            rowIndex = addStatisticRow(sheet, rowIndex, "Số giám thị nhập vào", String.valueOf(soGiamThi),
                    bodyStyleLeft);
            rowIndex = addStatisticRow(sheet, rowIndex, "Số giám thị cần mỗi ca", String.valueOf(soGiamThiCan),
                    bodyStyleLeft);
            rowIndex = addStatisticRow(sheet, rowIndex, "Số cán bộ giám sát mỗi ca", String.valueOf(soCanBoGiamSat),
                    bodyStyleLeft);
            rowIndex = addStatisticRow(sheet, rowIndex, "Số ca thi", String.valueOf(soCaThi), bodyStyleLeft);
            rowIndex = addStatisticRow(sheet, rowIndex, "Tổng số dòng phân công",
                    String.valueOf(result.getDanhSachPhanCong().size()), bodyStyleLeft);
            rowIndex = addStatisticRow(sheet, rowIndex, "Tổng số dòng giám sát",
                    String.valueOf(result.getDanhSachGiamSat().size()), bodyStyleLeft);
            rowIndex = addStatisticRow(sheet, rowIndex, "Số lần retry thuật toán", String.valueOf(retryCount),
                    bodyStyleLeft);

            applySheetLayout(sheet);
            setColumnWidths(sheet, 2);

            workbook.write(outputStream);
            workbook.dispose();
        }
    }

    private void createNationalHeader(Sheet sheet, CellStyle headerStyle, int columnCount) {
        Row row0 = sheet.createRow(0);
        Cell cell0 = row0.createCell(0);
        cell0.setCellValue("Cộng Hòa Xã Hội Chủ Nghĩa Việt Nam");
        cell0.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, columnCount - 1));

        Row row1 = sheet.createRow(1);
        Cell cell1 = row1.createCell(0);
        cell1.setCellValue("Độc Lập - Tự Do - Hạnh Phúc");
        cell1.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, columnCount - 1));

        sheet.createRow(2);
    }

    private void createHeaderRow(Sheet sheet, int rowIndex, CellStyle headerStyle, String... headers) {
        Row row = sheet.createRow(rowIndex);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private int addStatisticRow(Sheet sheet, int rowIndex, String name, String value, CellStyle bodyStyle) {
        Row row = sheet.createRow(rowIndex);
        createCell(row, 0, name, bodyStyle);
        createCell(row, 1, value, bodyStyle);
        return rowIndex + 1;
    }

    private CellStyle createHeaderStyle(SXSSFWorkbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        applyBorder(style);
        return style;
    }

    private CellStyle createBodyStyle(SXSSFWorkbook workbook, HorizontalAlignment alignment) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(alignment);
        applyBorder(style);
        return style;
    }

    private void applyBorder(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
    }

    private void createCell(Row row, int columnIndex, Object value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);

        if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else {
            cell.setCellValue(value == null ? "" : value.toString());
        }

        cell.setCellStyle(style);
    }

    private void applySheetLayout(Sheet sheet) {
        sheet.setAutobreaks(true);
        sheet.setFitToPage(true);
        sheet.setRepeatingRows(CellRangeAddress.valueOf("1:4"));

        PrintSetup printSetup = sheet.getPrintSetup();
        printSetup.setLandscape(false);
        printSetup.setFitWidth((short) 1);
        printSetup.setFitHeight((short) 0);
    }

    private void setColumnWidths(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.setColumnWidth(i, COLUMN_WIDTH);
        }
    }

    private void ensureParentDir(File file) throws IOException {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            if (!parent.mkdirs()) {
                throw new IOException("Không thể tạo thư mục: " + parent.getAbsolutePath());
            }
        }
    }
}
