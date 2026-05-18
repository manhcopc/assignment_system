package vn.exam.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Scanner;

import vn.exam.util.AppLogger;
import vn.exam.util.FileTransferUtil;

public class ExamClient {
    private static final int SERVER_PORT = 9999;
    private static final int CONNECT_TIMEOUT_MILLIS = 10000;
    private static final String CLIENT_OUTPUT_FILE = "output/ket_qua_phan_cong.xlsx";

    private final AppLogger logger;

    public ExamClient() {
        this(new AppLogger() {
            public void log(String message) {
                System.out.println(message);
            }
        });
    }

    public ExamClient(AppLogger logger) {
        this.logger = logger == null ? new AppLogger() {
            public void log(String message) {
                System.out.println(message);
            }
        } : logger;
    }

    public static void main(String[] args) {
        new ExamClient().start();
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        try {
            String serverIp = readServerIp(scanner);
            int port = readPort(scanner);
            int soPhongThi = readNonNegativeInt(scanner, "Nhập số phòng thi cần sử dụng: ", true);
            int soGiamThi = readNonNegativeInt(scanner, "Nhập số giám thị: ", true);
            int soCaThi = readNonNegativeInt(scanner, "Nhập số ca thi: ", true);
            String outputPath = readOutputPath(scanner);

            sendRequest(serverIp, port, soPhongThi, soGiamThi, soCaThi, outputPath);
        } catch (Exception e) {
            // sendRequest logs connection and transfer errors clearly.
        } finally {
            scanner.close();
        }
    }

    public void sendRequest(String host, int port, int soPhongThi, int soGiamThi,
                            int soCaThi, String outputPath) throws Exception {
        Socket socket = null;
        DataOutputStream output = null;
        DataInputStream input = null;
        try {
            log("Đang kết nối tới server " + host + ":" + port + "...");
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MILLIS);
            output = new DataOutputStream(socket.getOutputStream());
            input = new DataInputStream(socket.getInputStream());

            log("Đã kết nối server. Đang gửi yêu cầu phân công...");
            output.writeInt(soPhongThi);
            output.writeInt(soGiamThi);
            output.writeInt(soCaThi);
            output.flush();

            log("Đang nhận file kết quả...");
            new FileTransferUtil().receiveFile(input, outputPath);
            log("Đã nhận và lưu file kết quả tại: " + outputPath);
        } catch (Exception e) {
            logConnectionError(e, port);
            throw e;
        } finally {
            try { if (input != null) input.close(); } catch (Exception ignored) { }
            try { if (output != null) output.close(); } catch (Exception ignored) { }
            try { if (socket != null) socket.close(); } catch (Exception ignored) { }
        }
    }

    private void logConnectionError(Exception e, int port) {
        if (e instanceof UnknownHostException) {
            log("Không tìm thấy server. Vui lòng kiểm tra lại IP server.");
        } else if (e instanceof SocketTimeoutException) {
            log("Không kết nối được tới server trong " + (CONNECT_TIMEOUT_MILLIS / 1000)
                    + " giây. Vui lòng kiểm tra IP, port " + port + " và firewall.");
        } else if (e instanceof ConnectException) {
            log("Không kết nối được tới server tại port " + port
                    + ". Hãy chắc chắn server đang chạy và hai máy cùng mạng LAN.");
        } else {
            log("Lỗi client: " + e.getMessage());
        }
    }

    private void log(String message) {
        logger.log(message);
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

    private int readPort(Scanner scanner) {
        while (true) {
            int port = readNonNegativeInt(scanner, "Nhập port server: ", true);
            if (port <= 65535) {
                return port;
            }
            System.out.println("Port phải nhỏ hơn hoặc bằng 65535.");
        }
    }

    private String readOutputPath(Scanner scanner) {
        System.out.print("Nhập đường dẫn lưu file kết quả (bỏ trống để dùng output/ket_qua_phan_cong.xlsx): ");
        String value = scanner.nextLine().trim();
        return value.length() == 0 ? CLIENT_OUTPUT_FILE : value;
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
