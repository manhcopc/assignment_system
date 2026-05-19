package vn.exam.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import vn.exam.client.ExamClient;
import vn.exam.server.ExamServer;
import vn.exam.util.AppLogger;

/**
 * Giao diện Client với các phần chính:
 * 1. Kết nối Server (IP, Port)
 * 2. Thông tin Phân Công (Số phòng, Số giám thị, Số ca thi)
 * 3. Nút Gửi Request
 * 4. Phần Log
 * 5. Nút Import Excel & Mở Thư Mục Output
 */
public class ClientFrame extends JFrame {
    // Phần 1: Kết nối Server
    private JTextField hostField;
    private JTextField portField;
    
    // Phần 2: Thông tin Phân Công
    private JSpinner soPhongThiSpinner;
    private JSpinner soGiamThiSpinner;
    private JSpinner soCaThiSpinner;
    
    // Phần 3: Nút gửi
    private JButton sendButton;
    
    // Phần 4: Log
    private JTextArea logArea;
    
    // Phần 5: Các nút chức năng
    private JButton importExcelButton;
    private JButton openOutputButton;
    
    // Output path
    private String outputPath = "output";

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
        setSize(1000, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Main layout: GridBagLayout
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;

        // ============ PHẦN 1: KẾT NỐI SERVER ============
        JPanel connectionPanel = createConnectionPanel();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0.15;
        mainPanel.add(connectionPanel, gbc);

        // ============ PHẦN 2: THÔNG TIN PHÂN CÔNG ============
        JPanel assignmentPanel = createAssignmentPanel();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        gbc.weighty = 0.2;
        mainPanel.add(assignmentPanel, gbc);

        // ============ PHẦN 5: NÚT CHỨC NĂNG (phía trên phải) ============
        JPanel functionsPanel = createFunctionsPanel();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        gbc.weighty = 0.2;
        mainPanel.add(functionsPanel, gbc);

        // ============ PHẦN 4: PHẦN LOG ============
        JPanel logPanel = createLogPanel();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0.65;
        mainPanel.add(logPanel, gbc);

        add(mainPanel, BorderLayout.CENTER);
    }

    /**
     * Phần 1: Kết nối Server
     */
    private JPanel createConnectionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                "🔗 Kết Nối Server",
                0, 0,
                new Font("Arial", Font.BOLD, 12),
                Color.BLUE
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // IP Server
        JLabel hostLabel = new JLabel("IP Server:");
        hostLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(hostLabel, gbc);

        hostField = new JTextField("localhost", 15);
        hostField.setFont(new Font("Arial", Font.PLAIN, 11));
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(hostField, gbc);

        // Port
        JLabel portLabel = new JLabel("Port:");
        portLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(portLabel, gbc);

        portField = new JTextField(String.valueOf(ExamServer.PORT), 8);
        portField.setFont(new Font("Arial", Font.PLAIN, 11));
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.weightx = 0.2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(portField, gbc);

        return panel;
    }

    /**
     * Phần 2: Thông tin Phân Công
     */
    private JPanel createAssignmentPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                "📋 Thông Tin Phân Công",
                0, 0,
                new Font("Arial", Font.BOLD, 12),
                Color.BLUE
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Số phòng thi
        JLabel soPhongLabel = new JLabel("Số Phòng Thi:");
        soPhongLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        panel.add(soPhongLabel, gbc);

        soPhongThiSpinner = new JSpinner(new SpinnerNumberModel(2, 1, 100, 1));
        soPhongThiSpinner.setFont(new Font("Arial", Font.PLAIN, 11));
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.5;
        panel.add(soPhongThiSpinner, gbc);

        // Số giám thị
        JLabel soGiamThiLabel = new JLabel("Số Giám Thị:");
        soGiamThiLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(soGiamThiLabel, gbc);

        soGiamThiSpinner = new JSpinner(new SpinnerNumberModel(4, 1, 200, 1));
        soGiamThiSpinner.setFont(new Font("Arial", Font.PLAIN, 11));
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.5;
        panel.add(soGiamThiSpinner, gbc);

        // Số ca thi
        JLabel soCaLabel = new JLabel("Số Ca Thi:");
        soCaLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        panel.add(soCaLabel, gbc);

        soCaThiSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
        soCaThiSpinner.setFont(new Font("Arial", Font.PLAIN, 11));
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 0.5;
        panel.add(soCaThiSpinner, gbc);

        // Nút Gửi Request
        sendButton = new JButton("📤 Gửi Request");
        sendButton.setFont(new Font("Arial", Font.BOLD, 12));
        sendButton.setBackground(new Color(33, 150, 243));
        sendButton.setForeground(Color.WHITE);
        sendButton.setOpaque(true);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.SOUTH;
        panel.add(sendButton, gbc);

        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendRequest();
            }
        });

        return panel;
    }

    /**
     * Phần 5: Nút Chức Năng (Import Excel, Mở Thư Mục)
     */
    private JPanel createFunctionsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                "🔧 Chức Năng",
                0, 0,
                new Font("Arial", Font.BOLD, 12),
                Color.BLUE
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Nút Import Excel
        importExcelButton = new JButton("📥 Import Excel");
        importExcelButton.setFont(new Font("Arial", Font.BOLD, 12));
        importExcelButton.setBackground(new Color(76, 175, 80));
        importExcelButton.setForeground(Color.WHITE);
        importExcelButton.setOpaque(true);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 0.5;
        panel.add(importExcelButton, gbc);

        importExcelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(ClientFrame.this, 
                    "Chức năng Import Excel sẽ được phát triển sau.", 
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // Nút Mở Thư Mục Output
        openOutputButton = new JButton("📁 Mở Thư Mục Output");
        openOutputButton.setFont(new Font("Arial", Font.BOLD, 12));
        openOutputButton.setBackground(new Color(255, 152, 0));
        openOutputButton.setForeground(Color.WHITE);
        openOutputButton.setOpaque(true);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weighty = 0.5;
        panel.add(openOutputButton, gbc);

        openOutputButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openOutputFolder();
            }
        });

        // Khoảng trống
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weighty = 1.0;
        panel.add(new JPanel(), gbc);

        return panel;
    }

    /**
     * Phần 4: Phần Log
     */
    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                "📝 Nhật Ký Sự Kiện",
                0, 0,
                new Font("Arial", Font.BOLD, 12),
                Color.BLUE
        ));

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Courier New", Font.PLAIN, 11));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(logArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void sendRequest() {
        final String host = hostField.getText().trim();
        final int port;
        final int soPhongThi = (int) soPhongThiSpinner.getValue();
        final int soGiamThi = (int) soGiamThiSpinner.getValue();
        final int soCaThi = (int) soCaThiSpinner.getValue();

        try {
            if (host.isEmpty()) {
                throw new IllegalArgumentException("IP server không được để trống.");
            }
            port = Integer.parseInt(portField.getText().trim());
            if (port <= 0 || port > 65535) {
                throw new IllegalArgumentException("Port phải từ 1 đến 65535.");
            }
            if (soPhongThi <= 0) {
                throw new IllegalArgumentException("Số phòng thi phải lớn hơn 0.");
            }
            if (soGiamThi <= 0) {
                throw new IllegalArgumentException("Số giám thị phải lớn hơn 0.");
            }
            if (soCaThi <= 0) {
                throw new IllegalArgumentException("Số ca thi phải lớn hơn 0.");
            }
            int soGiamThiCan = soPhongThi * 2;
            if (soGiamThi < soGiamThiCan) {
                throw new IllegalArgumentException("Số giám thị phải >= " + soGiamThiCan);
            }
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
                    // ExamClient already logs errors
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

    private void openOutputFolder() {
        try {
            File outputDir = new File(outputPath);
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(outputDir);
                appendLog("Đã mở thư mục: " + outputDir.getAbsolutePath());
            } else {
                JOptionPane.showMessageDialog(this, "Không thể mở thư mục trên hệ điều hành này.",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi mở thư mục: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
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
        logArea.append("[" + getCurrentTime() + "] " + message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private String getCurrentTime() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm:ss");
        return sdf.format(new java.util.Date());
    }
}
