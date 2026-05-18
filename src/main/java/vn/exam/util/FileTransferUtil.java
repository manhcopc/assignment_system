package vn.exam.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileTransferUtil {
    public static final String STATUS_OK = "OK";
    public static final String STATUS_ERROR = "ERROR";
    private static final int BUFFER_SIZE = 8192;

    public void sendFile(DataOutputStream output, File file) throws IOException {
        output.writeUTF(STATUS_OK);
        output.writeLong(file.length());
        writeFileBytes(output, file);
        output.flush();
    }

    public void sendFiles(DataOutputStream output, List<File> files) throws IOException {
        output.writeUTF(STATUS_OK);
        output.writeInt(files.size());
        for (File file : files) {
            output.writeUTF(file.getName());
            output.writeLong(file.length());
            writeFileBytes(output, file);
        }
        output.flush();
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

        readFileBytes(input, outputFile, fileSize);
    }

    public List<File> receiveFiles(DataInputStream input, String outputDirectoryPath) throws IOException {
        String status = input.readUTF();
        if (STATUS_ERROR.equals(status)) {
            throw new IOException(input.readUTF());
        }
        if (!STATUS_OK.equals(status)) {
            throw new IOException("Phản hồi từ server không hợp lệ: " + status);
        }

        File outputDirectory = resolveOutputDirectory(outputDirectoryPath);
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }
        if (!outputDirectory.isDirectory()) {
            throw new IOException("Đường dẫn lưu kết quả không phải thư mục: " + outputDirectory.getPath());
        }

        int fileCount = input.readInt();
        List<File> receivedFiles = new ArrayList<File>();
        for (int i = 0; i < fileCount; i++) {
            String fileName = input.readUTF();
            long fileSize = input.readLong();
            File outputFile = new File(outputDirectory, sanitizeFileName(fileName));
            readFileBytes(input, outputFile, fileSize);
            receivedFiles.add(outputFile);
        }
        return receivedFiles;
    }

    private void writeFileBytes(DataOutputStream output, File file) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        } finally {
            fileInputStream.close();
        }
    }

    private void readFileBytes(DataInputStream input, File outputFile, long fileSize) throws IOException {
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

    private File resolveOutputDirectory(String outputDirectoryPath) {
        File outputPath = new File(outputDirectoryPath);
        if (outputDirectoryPath.toLowerCase().endsWith(".xlsx")) {
            File parent = outputPath.getParentFile();
            return parent == null ? new File(".") : parent;
        }
        return outputPath;
    }

    private String sanitizeFileName(String fileName) {
        return new File(fileName).getName();
    }
}
