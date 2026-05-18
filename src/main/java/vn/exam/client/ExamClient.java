package vn.exam.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Scanner;

import vn.exam.util.FileTransferUtil;

public class ExamClient {
    private static final int SERVER_PORT = 9999;
    private static final int CONNECT_TIMEOUT_MILLIS = 10000;
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
            String serverIp = readServerIp(scanner);
            int soPhongThi = readNonNegativeInt(scanner, "Nhập số phòng thi cần sử dụng: ", true);
            int soCanBoGiamSat = readNonNegativeInt(scanner, "Nhập số cán bộ giám sát mỗi ca: ", false);
            int soCaThi = readNonNegativeInt(scanner, "Nhập số ca thi: ", true);

            socket = new Socket();
            socket.connect(new InetSocketAddress(serverIp, SERVER_PORT), CONNECT_TIMEOUT_MILLIS);
            output = new DataOutputStream(socket.getOutputStream());
            input = new DataInputStream(socket.getInputStream());

            output.writeInt(soPhongThi);
            output.writeInt(soCanBoGiamSat);
            output.writeInt(soCaThi);
            output.flush();

            new FileTransferUtil().receiveFile(input, CLIENT_OUTPUT_FILE);
            System.out.println("Đã nhận và lưu file kết quả tại: " + CLIENT_OUTPUT_FILE);
        } catch (UnknownHostException e) {
            System.err.println("Không tìm thấy server. Vui lòng kiểm tra lại IP server.");
        } catch (SocketTimeoutException e) {
            System.err.println("Không kết nối được tới server trong " + (CONNECT_TIMEOUT_MILLIS / 1000)
                    + " giây. Vui lòng kiểm tra IP, port " + SERVER_PORT + " và firewall.");
        } catch (ConnectException e) {
            System.err.println("Không kết nối được tới server tại port " + SERVER_PORT
                    + ". Hãy chắc chắn server đang chạy và hai máy cùng mạng LAN.");
        } catch (Exception e) {
            System.err.println("Lỗi client: " + e.getMessage());
        } finally {
            try { if (input != null) input.close(); } catch (Exception ignored) { }
            try { if (output != null) output.close(); } catch (Exception ignored) { }
            try { if (socket != null) socket.close(); } catch (Exception ignored) { }
            scanner.close();
        }
    }

    private String readServerIp(Scanner scanner) {
        while (true) {
            System.out.print("Nhập IP server: ");
            String value = scanner.nextLine().trim();
            if (value.length() > 0) {
                return value;
            }
            System.out.println("IP server không được để trống. Ví dụ: 192.168.1.10");
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
