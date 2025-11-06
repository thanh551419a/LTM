package BTLClient;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class LobbyScreen extends JFrame {
    private String username;
    private NetworkHandler network;
    private JTextArea playerListArea;
    private Thread listenThread;

    public LobbyScreen(String username, NetworkHandler network) {
        this.username = username;
        this.network = network;

        setTitle("Sảnh chờ - " + username);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ===== Panel bên trái: tạo / tham gia phòng =====
        JPanel leftPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        JButton btnCreate = new JButton("Tạo phòng");
        JButton btnJoin = new JButton("Tham gia phòng");
        leftPanel.add(btnCreate);
        leftPanel.add(btnJoin);

        // ===== Panel bên phải: danh sách người chơi =====
        JPanel rightPanel = new JPanel(new BorderLayout());
        playerListArea = new JTextArea();
        playerListArea.setEditable(false);
        rightPanel.add(new JLabel("👥 Người chơi online:"), BorderLayout.NORTH);
        rightPanel.add(new JScrollPane(playerListArea), BorderLayout.CENTER);

        add(leftPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        // ===== Sự kiện nút =====
        btnCreate.addActionListener(e -> sendCreateRoom());
        btnJoin.addActionListener(e -> sendJoinRoom());

        // ===== Gửi tên người dùng lên server =====
        try {
            network.sendMsg("LOGIN|" + username);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Không thể gửi tên người dùng đến server.");
        }

        // ===== Luồng nhận thông tin từ server =====
        listenThread = new Thread(this::listenServer);
        listenThread.start();
    }

    private void sendCreateRoom() {
        try {
            network.sendMsg("CREATE_ROOM|" + username);
            String response = network.readMsg();

            if (response.startsWith("ROOM_CREATED|")) {
                // Server gửi dạng: ROOM_CREATED|roomName
                String roomName = response.split("\\|")[1];
                JOptionPane.showMessageDialog(this, "🏠 Phòng \"" + roomName + "\" đã tạo thành công!");
                new GameScreen(username, network, true, roomName).setVisible(true);
                dispose();
            } else if (response.equals("ROOM_EXISTED")) {
                JOptionPane.showMessageDialog(this, "⚠️ Bạn đã có phòng đang tồn tại!");
            } else {
                JOptionPane.showMessageDialog(this, "❌ Không thể tạo phòng: " + response);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Mất kết nối đến server.");
        }
    }

    private void sendJoinRoom() {
        String roomCode = JOptionPane.showInputDialog(this, "Nhập tên hoặc mã phòng cần tham gia:");
        if (roomCode == null || roomCode.isEmpty()) return;

        try {
            network.sendMsg("JOIN_ROOM|" + username + "|" + roomCode);
            String response = network.readMsg();

            if (response.equals("ROOM_JOINED")) {
                JOptionPane.showMessageDialog(this, "✅ Đã tham gia phòng thành công!");
                new GameScreen(username, network, false, roomCode).setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "❌ Tham gia thất bại: " + response);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Mất kết nối đến server.");
        }
    }

    // Nhận cập nhật danh sách người chơi
    private void listenServer() {
        try {
            while (true) {
                String msg = network.readMsg();
                if (msg.startsWith("PLAYER_LIST|")) {
                    String players = msg.substring("PLAYER_LIST|".length());
                    SwingUtilities.invokeLater(() -> playerListArea.setText(players.replace("|", "\n")));
                }
            }
        } catch (IOException e) {
            System.out.println("⚠️ Ngắt kết nối đến server.");
        }
    }
}
