package vn.exam.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileTransferUtil {
    public static final String STATUS_OK = "OK";
    public static final String STATUS_ERROR = "ERROR";
    private static final int BUFFER_SIZE = 8192;

    public void sendFile(DataOutputStream output, File file) throws IOException {
        output.writeUTF(STATUS_OK);
        output.writeLong(file.length());
        FileInputStream fileInputStream = new FileInputStream(file);
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
            output.flush();
        } finally {
            fileInputStream.close();
        }
    }

    public void sendError(DataOutputStream output, String message) throws IOException {
        output.writeUTF(STATUS_ERROR);
        output.writeUTF(message == null ? "Lỗi không xác định." : message);
        output.flush();
    }

    public void receiveFile(DataInputStream input, String outputPath) throws IOException {
        String status = input.readUTF();
        if (STATUS_ERROR.equals(status)) {
            throw new IOException(input.readUTF());
        }
        if (!STATUS_OK.equals(status)) {
            throw new IOException("Phản hồi từ server không hợp lệ: " + status);
        }

        long fileSize = input.readLong();
        File outputFile = new File(outputPath);
        File parent = outputFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            long remaining = fileSize;
            while (remaining > 0) {
                int bytesToRead = (int) Math.min(buffer.length, remaining);
                int bytesRead = input.read(buffer, 0, bytesToRead);
                if (bytesRead == -1) {
                    throw new IOException("Mất kết nối khi đang nhận file từ server.");
                }
                fileOutputStream.write(buffer, 0, bytesRead);
                remaining -= bytesRead;
            }
        } finally {
            fileOutputStream.close();
        }
    }
}
