package vn.exam.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import vn.exam.server.ExamServer;
import vn.exam.util.AppLogger;

public class ServerFrame extends JFrame {
    private JTextField portField;
    private JButton startButton;
    private JButton stopButton;
    private JTextArea logArea;
    private ExamServer server;
    private Thread serverThread;

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
        setTitle("Exam Assignment Server");
        setSize(700, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        portField = new JTextField(String.valueOf(ExamServer.PORT), 8);
        startButton = new JButton("Start Server");
        stopButton = new JButton("Stop Server");
        stopButton.setEnabled(false);
        logArea = new JTextArea();
        logArea.setEditable(false);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.add(new JLabel("Port:"));
        controlPanel.add(portField);
        controlPanel.add(startButton);
        controlPanel.add(stopButton);

        add(controlPanel, BorderLayout.NORTH);
        add(new JScrollPane(logArea), BorderLayout.CENTER);

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

    private AppLogger createSwingLogger() {
        return new AppLogger() {
            public void log(final String message) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        logArea.append(message + "\n");
                        logArea.setCaretPosition(logArea.getDocument().getLength());
                    }
                });
            }
        };
    }
}
