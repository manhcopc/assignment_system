package vn.exam.ui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import vn.exam.client.ExamClient;
import vn.exam.server.ExamServer;
import vn.exam.util.AppLogger;

public class ClientFrame extends JFrame {
    private JTextField hostField;
    private JTextField portField;
    private JTextField soPhongThiField;
    private JTextField soGiamThiField;
    private JTextField soCaThiField;
    private JTextField outputPathField;
    private JButton chooseOutputButton;
    private JButton sendButton;
    private JTextArea logArea;

    public ClientFrame() {
        initComponents();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ClientFrame().setVisible(true);
            }
        });
    }

    private void initComponents() {
        setTitle("Exam Assignment Client");
        setSize(760, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        hostField = new JTextField("localhost", 25);
        portField = new JTextField(String.valueOf(ExamServer.PORT), 10);
        soPhongThiField = new JTextField(10);
        soGiamThiField = new JTextField(10);
        soCaThiField = new JTextField(10);
        outputPathField = new JTextField("output/ket_qua_phan_cong.xlsx", 35);
        chooseOutputButton = new JButton("Chọn nơi lưu file");
        sendButton = new JButton("Gửi yêu cầu phân công");
        logArea = new JTextArea();
        logArea.setEditable(false);

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addRow(formPanel, gbc, 0, "IP server:", hostField, null);
        addRow(formPanel, gbc, 1, "Port:", portField, null);
        addRow(formPanel, gbc, 2, "Số phòng thi:", soPhongThiField, null);
        addRow(formPanel, gbc, 3, "Số giám thị:", soGiamThiField, null);
        addRow(formPanel, gbc, 4, "Số ca thi:", soCaThiField, null);
        addRow(formPanel, gbc, 5, "File output:", outputPathField, chooseOutputButton);

        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        formPanel.add(sendButton, gbc);

        add(formPanel, BorderLayout.NORTH);
        add(new JScrollPane(logArea), BorderLayout.CENTER);

        chooseOutputButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                chooseOutputFile();
            }
        });
        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendRequest();
            }
        });
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, JTextField field, JButton button) {
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(field, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        if (button != null) {
            panel.add(button, gbc);
        } else {
            panel.add(new JLabel(""), gbc);
        }
    }

    private void chooseOutputFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn nơi lưu file kết quả");
        fileChooser.setSelectedFile(new File(outputPathField.getText().trim()));
        fileChooser.setFileFilter(new FileNameExtensionFilter("Excel Workbook (*.xlsx)", "xlsx"));
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String path = file.getPath();
            if (!path.toLowerCase().endsWith(".xlsx")) {
                path = path + ".xlsx";
            }
            outputPathField.setText(path);
        }
    }

    private void sendRequest() {
        final String host = hostField.getText().trim();
        final String outputPath = outputPathField.getText().trim();
        final int port;
        final int soPhongThi;
        final int soGiamThi;
        final int soCaThi;
        try {
            if (host.length() == 0) {
                throw new IllegalArgumentException("IP server không được để trống.");
            }
            if (outputPath.length() == 0) {
                throw new IllegalArgumentException("Đường dẫn file output không được để trống.");
            }
            port = parsePositiveInt(portField.getText(), "Port");
            if (port > 65535) {
                throw new IllegalArgumentException("Port phải nhỏ hơn hoặc bằng 65535.");
            }
            soPhongThi = parsePositiveInt(soPhongThiField.getText(), "Số phòng thi");
            soGiamThi = parsePositiveInt(soGiamThiField.getText(), "Số giám thị");
            soCaThi = parsePositiveInt(soCaThiField.getText(), "Số ca thi");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            return;
        }

        sendButton.setEnabled(false);
        appendLog("Bắt đầu gửi yêu cầu phân công...");
        Thread clientThread = new Thread(new Runnable() {
            public void run() {
                try {
                    new ExamClient(createSwingLogger()).sendRequest(host, port, soPhongThi,
                            soGiamThi, soCaThi, outputPath);
                } catch (Exception ignored) {
                    // ExamClient already logs a clear error message.
                } finally {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            sendButton.setEnabled(true);
                        }
                    });
                }
            }
        }, "exam-client-gui");
        clientThread.start();
    }

    private int parsePositiveInt(String value, String fieldName) {
        int number = parseInteger(value, fieldName);
        if (number <= 0) {
            throw new IllegalArgumentException(fieldName + " phải là số nguyên lớn hơn 0.");
        }
        return number;
    }

    private int parseNonNegativeInt(String value, String fieldName) {
        int number = parseInteger(value, fieldName);
        if (number < 0) {
            throw new IllegalArgumentException(fieldName + " phải là số nguyên không âm.");
        }
        return number;
    }

    private int parseInteger(String value, String fieldName) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " phải là số nguyên hợp lệ.");
        }
    }

    private AppLogger createSwingLogger() {
        return new AppLogger() {
            public void log(final String message) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        appendLog(message);
                    }
                });
            }
        };
    }

    private void appendLog(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}
