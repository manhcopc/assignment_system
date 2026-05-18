package vn.exam.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import vn.exam.model.AssignmentResult;
import vn.exam.model.CanBo;
import vn.exam.model.PhongThi;
import vn.exam.service.AssignmentService;
import vn.exam.util.ExcelReader;
import vn.exam.util.ExcelWriter;
import vn.exam.util.FileTransferUtil;

public class ExamServer {
    public static final int PORT = 9999;
    private static final String CAN_BO_FILE = "data/CANBOCOITHI.xlsx";
    private static final String PHONG_THI_FILE = "data/PHONGTHI.xlsx";
    private static final String SERVER_OUTPUT_FILE = "output/server_ket_qua_phan_cong.xlsx";

    public static void main(String[] args) {
        new ExamServer().start();
    }

    public void start() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server đang chạy tại port " + PORT + ". Nhấn Ctrl+C để dừng.");
            while (true) {
                Socket socket = serverSocket.accept();
                handleClient(socket);
            }
        } catch (Exception e) {
            System.err.println("Không thể khởi động server: " + e.getMessage());
        } finally {
            if (serverSocket != null) {
                try { serverSocket.close(); } catch (Exception ignored) { }
            }
        }
    }

    private void handleClient(Socket socket) {
        DataInputStream input = null;
        DataOutputStream output = null;
        try {
            System.out.println("Client kết nối từ " + socket.getInetAddress());
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());

            int soPhongThi = input.readInt();
            int soCanBoGiamSat = input.readInt();
            int soCaThi = input.readInt();
            System.out.println("Yêu cầu: " + soPhongThi + " phòng thi, " + soCanBoGiamSat
                    + " cán bộ giám sát mỗi ca, " + soCaThi + " ca thi.");

            ExcelReader reader = new ExcelReader();
            List<CanBo> canBoList = reader.readCanBo(CAN_BO_FILE);
            List<PhongThi> phongThiList = reader.readPhongThi(PHONG_THI_FILE);

            AssignmentResult result = new AssignmentService().phanCongNhieuCa(canBoList, phongThiList,
                    soPhongThi, soCanBoGiamSat, soCaThi);
            new ExcelWriter().writeAssignmentResult(result, SERVER_OUTPUT_FILE, soPhongThi, soCanBoGiamSat, soCaThi);
            new FileTransferUtil().sendFile(output, new File(SERVER_OUTPUT_FILE));
            System.out.println("Đã gửi file kết quả cho client.");
        } catch (Exception e) {
            System.err.println("Lỗi xử lý client: " + e.getMessage());
            if (output != null) {
                try {
                    new FileTransferUtil().sendError(output, e.getMessage());
                } catch (Exception sendErrorException) {
                    System.err.println("Không thể gửi lỗi về client: " + sendErrorException.getMessage());
                }
            }
        } finally {
            try { if (input != null) input.close(); } catch (Exception ignored) { }
            try { if (output != null) output.close(); } catch (Exception ignored) { }
            try { socket.close(); } catch (Exception ignored) { }
        }
    }
}
