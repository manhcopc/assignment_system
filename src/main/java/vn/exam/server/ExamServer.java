package vn.exam.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.UUID;

import vn.exam.model.AssignmentResult;
import vn.exam.model.CanBo;
import vn.exam.model.PhongThi;
import vn.exam.service.AssignmentService;
import vn.exam.util.AppLogger;
import vn.exam.util.ExcelReader;
import vn.exam.util.ExcelWriter;
import vn.exam.util.FileTransferUtil;

public class ExamServer {
    public static final int PORT = 9999;
    private static final String BIND_ADDRESS = "0.0.0.0";
    private static final String CAN_BO_FILE = "data/CANBOCOITHI.xlsx";
    private static final String PHONG_THI_FILE = "data/PHONGTHI.xlsx";
    private static final String SERVER_OUTPUT_DIR = "output";

    private final AppLogger logger;
    private volatile boolean running;
    private ServerSocket serverSocket;

    public ExamServer() {
        this(new AppLogger() {
            public void log(String message) {
                System.out.println(message);
            }
        });
    }

    public ExamServer(AppLogger logger) {
        this.logger = logger == null ? new AppLogger() {
            public void log(String message) {
                System.out.println(message);
            }
        } : logger;
    }

    public static void main(String[] args) {
        new ExamServer().start();
    }

    public void start() {
        startServer(PORT);
    }

    public void startServer(int port) {
        synchronized (this) {
            if (running) {
                log("Server đang chạy, không khởi động lại.");
                return;
            }
            running = true;
        }

        try {
            InetAddress bindAddress = InetAddress.getByName(BIND_ADDRESS);
            serverSocket = new ServerSocket(port, 50, bindAddress);
            log("Server đang chạy tại " + BIND_ADDRESS + ":" + port
                    + ". Các máy trong cùng LAN có thể kết nối bằng IP của máy server.");
            log("Nhấn Stop Server hoặc Ctrl+C để dừng server.");
            while (running) {
                try {
                    Socket socket = serverSocket.accept();
                    String clientIp = socket.getInetAddress().getHostAddress();
                    log("Client kết nối từ IP: " + clientIp);
                    Thread clientThread = new Thread(new ClientHandler(socket),
                            "exam-client-" + clientIp + "-" + System.nanoTime());
                    clientThread.start();
                } catch (SocketException e) {
                    if (running) {
                        log("Lỗi socket khi chờ client: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            if (running) {
                log("Không thể khởi động server tại " + BIND_ADDRESS + ":" + port + ": " + e.getMessage());
            }
        } finally {
            closeServerSocket();
            running = false;
            log("Server đã dừng.");
        }
    }

    public void stopServer() {
        running = false;
        closeServerSocket();
        log("Đã gửi yêu cầu dừng server.");
    }

    private void closeServerSocket() {
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (Exception e) {
                log("Lỗi khi đóng server socket: " + e.getMessage());
            }
        }
    }

    private void log(String message) {
        logger.log(message);
    }

    private class ClientHandler implements Runnable {
        private Socket socket;

        private ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            handleClient(socket);
        }

        private void handleClient(Socket socket) {
            DataInputStream input = null;
            DataOutputStream output = null;
            File outputFile = null;
            String clientIp = socket.getInetAddress().getHostAddress();
            try {
                input = new DataInputStream(socket.getInputStream());
                output = new DataOutputStream(socket.getOutputStream());

                int soPhongThi = input.readInt();
                int soCanBoGiamSat = input.readInt();
                int soCaThi = input.readInt();
                log("[" + clientIp + "] Yêu cầu: " + soPhongThi + " phòng thi, "
                        + soCanBoGiamSat + " cán bộ giám sát mỗi ca, " + soCaThi + " ca thi.");

                ExcelReader reader = new ExcelReader();
                List<CanBo> canBoList = reader.readCanBo(CAN_BO_FILE);
                List<PhongThi> phongThiList = reader.readPhongThi(PHONG_THI_FILE);

                AssignmentResult result = new AssignmentService().phanCongNhieuCa(canBoList, phongThiList,
                        soPhongThi, soCanBoGiamSat, soCaThi);
                outputFile = createOutputFile(clientIp);
                new ExcelWriter().writeAssignmentResult(result, outputFile.getPath(), soPhongThi, soCanBoGiamSat, soCaThi);
                new FileTransferUtil().sendFile(output, outputFile);
                log("[" + clientIp + "] Đã gửi file kết quả cho client.");
            } catch (Exception e) {
                log("[" + clientIp + "] Lỗi xử lý client: " + e.getMessage());
                if (output != null) {
                    try {
                        new FileTransferUtil().sendError(output, e.getMessage());
                    } catch (Exception sendErrorException) {
                        log("[" + clientIp + "] Không thể gửi lỗi về client: " + sendErrorException.getMessage());
                    }
                }
            } finally {
                if (outputFile != null && outputFile.exists() && !outputFile.delete()) {
                    log("[" + clientIp + "] Không thể xóa file tạm: " + outputFile.getPath());
                }
                try { if (input != null) input.close(); } catch (Exception ignored) { }
                try { if (output != null) output.close(); } catch (Exception ignored) { }
                try { socket.close(); } catch (Exception ignored) { }
                log("[" + clientIp + "] Đã đóng kết nối.");
            }
        }

        private File createOutputFile(String clientIp) {
            File outputDir = new File(SERVER_OUTPUT_DIR);
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            String safeClientIp = clientIp.replaceAll("[^0-9A-Za-z._-]", "_");
            return new File(outputDir, "server_ket_qua_phan_cong_" + safeClientIp + "_"
                    + UUID.randomUUID().toString() + ".xlsx");
        }
    }
}
