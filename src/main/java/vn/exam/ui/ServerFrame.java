package vn.exam.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.Enumeration;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import vn.exam.server.ExamServer;
import vn.exam.util.AppLogger;

/**
 * Giao diện Server với 4 phần chính:
 * 1. Thông tin Server (Port, IP, Status, Số client)
 * 2. Danh sách Client kết nối
 * 3. Điều khiển Server (Nhập port, Start/Stop)
 * 4. Phần Log
 */
public class ServerFrame extends JFrame {
    // Thông tin server
    private JLabel statusLabel;
    private JLabel portInfoLabel;
    private JLabel ipInfoLabel;
    private JLabel clientCountLabel;
    
    // Danh sách client
    private JTable clientTable;
    private DefaultTableModel clientTableModel;
    
    // Điều khiển
    private JTextField portField;
    private JButton startButton;
    private JButton stopButton;
    
    // Log
    private JTextArea logArea;
    
    // Server
    private ExamServer server;
    private Thread serverThread;
    private int connectedClientCount = 0;

    public ServerFrame() {
        initComponents();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ServerFrame().setVisible(true);
            }
        });
    }

    private void initComponents() {
        setTitle("Exam Assignment Server Management");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Main layout: GridBagLayout
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;

        // ============ PHẦN 1: THÔNG TIN SERVER ============
        JPanel serverInfoPanel = createServerInfoPanel();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0.15;
        mainPanel.add(serverInfoPanel, gbc);

        // ============ PHẦN 2: DANH SÁCH CLIENT ============
        JPanel clientListPanel = createClientListPanel();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.6;
        gbc.weighty = 0.35;
        mainPanel.add(clientListPanel, gbc);

        // ============ PHẦN 3: ĐIỀU KHIỂN SERVER ============
        JPanel controlPanel = createControlPanel();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.4;
        gbc.weighty = 0.35;
        mainPanel.add(controlPanel, gbc);

        // ============ PHẦN 4: PHẦN LOG ============
        JPanel logPanel = createLogPanel();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0.5;
        mainPanel.add(logPanel, gbc);

        add(mainPanel, BorderLayout.CENTER);
    }

    /**
     * Phần 1: Thông tin Server (Port, IP, Status, Số client)
     */
    private JPanel createServerInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                "📊 Thông Tin Server",
                0, 0,
                new Font("Arial", Font.BOLD, 12),
                Color.BLUE
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Status
        statusLabel = new JLabel("🔴 Status: Offline");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(statusLabel, gbc);

        // Port
        portInfoLabel = new JLabel("Port: --");
        portInfoLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(portInfoLabel, gbc);

        // IP
        ipInfoLabel = new JLabel("IP: --");
        ipInfoLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        gbc.gridx = 2;
        gbc.gridy = 0;
        panel.add(ipInfoLabel, gbc);

        // Client count
        clientCountLabel = new JLabel("Clients: 0");
        clientCountLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        gbc.gridx = 3;
        gbc.gridy = 0;
        panel.add(clientCountLabel, gbc);

        return panel;
    }

    /**
     * Phần 2: Danh sách Client kết nối
     */
    private JPanel createClientListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                "👥 Danh Sách Client Kết Nối",
                0, 0,
                new Font("Arial", Font.BOLD, 12),
                Color.BLUE
        ));

        // Tạo bảng client
        String[] columnNames = {"STT", "IP Address", "Trạng Thái", "Thời Gian Kết Nối"};
        clientTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        clientTable = new JTable(clientTableModel);
        clientTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        clientTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        clientTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        clientTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        
        JScrollPane scrollPane = new JScrollPane(clientTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Phần 3: Điều khiển Server (Nhập port, Start/Stop)
     */
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                "⚙️ Điều Khiển Server",
                0, 0,
                new Font("Arial", Font.BOLD, 12),
                Color.BLUE
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Label Port
        JLabel portLabel = new JLabel("Nhập Port:");
        portLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(portLabel, gbc);

        // TextField Port
        portField = new JTextField(String.valueOf(ExamServer.PORT), 10);
        portField.setFont(new Font("Arial", Font.PLAIN, 12));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        panel.add(portField, gbc);

        // Button Start
        startButton = new JButton("▶️ Khởi Động Server");
        startButton.setFont(new Font("Arial", Font.BOLD, 12));
        startButton.setBackground(new Color(76, 175, 80));
        startButton.setForeground(Color.WHITE);
        startButton.setOpaque(true);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        panel.add(startButton, gbc);

        // Button Stop
        stopButton = new JButton("⏹️ Dừng Server");
        stopButton.setFont(new Font("Arial", Font.BOLD, 12));
        stopButton.setBackground(new Color(244, 67, 54));
        stopButton.setForeground(Color.WHITE);
        stopButton.setOpaque(true);
        stopButton.setEnabled(false);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        panel.add(stopButton, gbc);

        // Khoảng trống
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        panel.add(new JPanel(), gbc);

        // Event listeners
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startServer();
            }
        });

        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stopServer();
            }
        });

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

    private void startServer() {
        final int port;
        try {
            port = Integer.parseInt(portField.getText().trim());
            if (port <= 0 || port > 65535) {
                throw new NumberFormatException("Port ngoài phạm vi hợp lệ");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Port phải là số nguyên từ 1 đến 65535.",
                    "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            return;
        }

        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        portField.setEnabled(false);
        
        // Cập nhật thông tin
        updateServerInfo(port, true);

        server = new ExamServer(createSwingLogger());
        serverThread = new Thread(new Runnable() {
            public void run() {
                try {
                    server.startServer(port);
                } finally {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            startButton.setEnabled(true);
                            stopButton.setEnabled(false);
                            portField.setEnabled(true);
                            updateServerInfo(port, false);
                            connectedClientCount = 0;
                            clientCountLabel.setText("Clients: 0");
                            clientTableModel.setRowCount(0);
                        }
                    });
                }
            }
        }, "exam-server-gui");
        serverThread.start();
    }

    private void stopServer() {
        if (server != null) {
            server.stopServer();
        }
        stopButton.setEnabled(false);
    }

    /**
     * Cập nhật thông tin Server
     * Lấy IP thực của mạng LAN (không phải localhost)
     */
    private void updateServerInfo(int port, boolean isRunning) {
        if (isRunning) {
            statusLabel.setText("🟢 Status: Online");
            statusLabel.setForeground(new Color(76, 175, 80));
            portInfoLabel.setText("Port: " + port);
            String networkIp = getNetworkIpAddress();
            ipInfoLabel.setText("IP: " + networkIp);
        } else {
            statusLabel.setText("🔴 Status: Offline");
            statusLabel.setForeground(new Color(244, 67, 54));
            portInfoLabel.setText("Port: --");
            ipInfoLabel.setText("IP: --");
        }
    }

    /**
     * Lấy IP thực của mạng LAN (không phải localhost)
     * Ưu tiên IPv4, bỏ qua loopback
     */
    private String getNetworkIpAddress() {
        try {
            // Phương pháp 1: Lặp qua tất cả NetworkInterface
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                
                // Bỏ qua loopback interface
                if (ni.isLoopback()) {
                    continue;
                }
                
                // Bỏ qua interface không active
                if (!ni.isUp()) {
                    continue;
                }
                
                // Lấy tất cả các InetAddress
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    
                    // Ưu tiên IPv4 (RFC 1918 - private IP)
                    if (!addr.isLoopbackAddress() && !addr.isLinkLocalAddress()) {
                        String hostAddress = addr.getHostAddress();
                        
                        // Kiểm tra nếu là IPv4 (không chứa dấu :)
                        if (!hostAddress.contains(":")) {
                            return hostAddress;
                        }
                    }
                }
            }
            
            // Phương pháp 2: Nếu không tìm được, dùng localhost
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "0.0.0.0";
        }
    }

    private AppLogger createSwingLogger() {
        return new AppLogger() {
            public void log(final String message) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        logArea.append("[" + getCurrentTime() + "] " + message + "\n");
                        logArea.setCaretPosition(logArea.getDocument().getLength());
                        
                        // Cập nhật số client nếu có trong log
                        if (message.contains("Client kết nối")) {
                            connectedClientCount++;
                            clientCountLabel.setText("Clients: " + connectedClientCount);
                            
                            // Thêm vào bảng
                            String[] parts = message.split("IP: ");
                            if (parts.length > 1) {
                                String ip = parts[1].trim();
                                clientTableModel.addRow(new Object[]{
                                    clientTableModel.getRowCount() + 1,
                                    ip,
                                    "Connected",
                                    getCurrentTime()
                                });
                            }
                        }
                    }
                });
            }
        };
    }

    private String getCurrentTime() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm:ss");
        return sdf.format(new java.util.Date());
    }
}
