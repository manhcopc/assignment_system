package vn.exam.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Scanner;

import vn.exam.server.ExamServer;
import vn.exam.util.FileTransferUtil;

public class ExamClient {
    private static final String SERVER_HOST = "localhost";
    private static final String CLIENT_OUTPUT_FILE = "output/ket_qua_phan_cong.xlsx";

    public static void main(String[] args) {
        new ExamClient().start();
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        Socket socket = null;
        DataOutputStream output = null;
        DataInputStream input = null;
        try {
            int soPhongThi = readNonNegativeInt(scanner, "Nhập số phòng thi cần sử dụng: ", true);
            int soCanBoGiamSat = readNonNegativeInt(scanner, "Nhập số cán bộ giám sát mỗi ca: ", false);
            int soCaThi = readNonNegativeInt(scanner, "Nhập số ca thi: ", true);

            socket = new Socket(SERVER_HOST, ExamServer.PORT);
            output = new DataOutputStream(socket.getOutputStream());
            input = new DataInputStream(socket.getInputStream());

            output.writeInt(soPhongThi);
            output.writeInt(soCanBoGiamSat);
            output.writeInt(soCaThi);
            output.flush();

            new FileTransferUtil().receiveFile(input, CLIENT_OUTPUT_FILE);
            System.out.println("Đã nhận và lưu file kết quả tại: " + CLIENT_OUTPUT_FILE);
        } catch (Exception e) {
            System.err.println("Lỗi client: " + e.getMessage());
        } finally {
            try { if (input != null) input.close(); } catch (Exception ignored) { }
            try { if (output != null) output.close(); } catch (Exception ignored) { }
            try { if (socket != null) socket.close(); } catch (Exception ignored) { }
            scanner.close();
        }
    }

    private int readNonNegativeInt(Scanner scanner, String message, boolean mustBePositive) {
        while (true) {
            System.out.print(message);
            String value = scanner.nextLine();
            try {
                int number = Integer.parseInt(value.trim());
                if (mustBePositive && number <= 0) {
                    System.out.println("Vui lòng nhập số nguyên lớn hơn 0.");
                    continue;
                }
                if (!mustBePositive && number < 0) {
                    System.out.println("Vui lòng nhập số nguyên không âm.");
                    continue;
                }
                return number;
            } catch (NumberFormatException e) {
                System.out.println("Giá trị không hợp lệ. Vui lòng nhập số nguyên.");
            }
        }
    }
}
