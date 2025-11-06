package BTLClient;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class GameScreen extends JFrame {
    private String username;
    private NetworkHandler network;
    private boolean isHost;
    private String roomName; // ✅ Thêm biến tên phòng
    private JLabel[] cardLabels = new JLabel[3];
    private JButton btnDraw;
    private JButton btnStart;
    private boolean canDraw = false; // Biến kiểm soát có thể rút bài hay không

    public GameScreen(String username, NetworkHandler network, boolean isHost, String roomName) {
        this.username = username;
        this.network = network;
        this.isHost = isHost;
        this.roomName = roomName; // ✅ Gán tên phòng

        setTitle("Bàn chơi - " + username + (isHost ? " (Chủ phòng)" : ""));
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel gamePanel = new JPanel(new GridLayout(1, 3, 10, 10));
        for (int i = 0; i < 3; i++) {
            cardLabels[i] = new JLabel("Bài " + (i + 1) + ": [Chưa có]", SwingConstants.CENTER);
            cardLabels[i].setOpaque(true);
            cardLabels[i].setBackground(Color.LIGHT_GRAY);
            gamePanel.add(cardLabels[i]);
        }

        // Panel cho nút
        JPanel buttonPanel = new JPanel();
        btnStart = new JButton("Start");
        btnDraw = new JButton("Rút bài");
        btnDraw.setEnabled(false); // Chỉ cho phép rút bài sau khi nhấn start

        buttonPanel.add(btnStart);
        buttonPanel.add(btnDraw);

        add(gamePanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Nút Start
        btnStart.addActionListener(e -> {
            canDraw = true;
            btnDraw.setEnabled(true);
            btnStart.setEnabled(false); // Vô hiệu hóa nút Start sau khi nhấn

            try {
                // ✅ Gửi cả tên phòng kèm message START
                network.sendMsg(roomName + " START");
                System.out.println("▶️ Gửi lệnh bắt đầu: " + roomName + " START");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "❌ Lỗi gửi yêu cầu bắt đầu.");
            }
        });

        // Nút Rút bài
        btnDraw.addActionListener(e -> {
            if (canDraw) {
                try {
                    network.sendMsg("0:Draw");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "❌ Lỗi gửi yêu cầu rút bài.");
                }
            }
        });

        // Luồng lắng nghe dữ liệu từ server
        new Thread(() -> {
            try {
                while (true) {
                    String msg = network.readMsg();
                    if (msg.equalsIgnoreCase("END")) {
                        JOptionPane.showMessageDialog(this, "🃏 Kết thúc chia bài!");
                        break;
                    } else if (msg.startsWith("DRAW;")) {
                        // Dạng: DRAW;giá_trị
                        int value = Integer.parseInt(msg.split(";")[1]);
                        updateCard(value);
                    }
                }
            } catch (IOException e) {
                System.out.println("⚠️ Mất kết nối với server.");
            }
        }).start();
    }

    private void updateCard(int value) {
        SwingUtilities.invokeLater(() -> {
            for (JLabel label : cardLabels) {
                if (label.getText().contains("[Chưa có]")) {
                    label.setText("Bài: " + value);
                    break;
                }
            }
        });
    }
}
