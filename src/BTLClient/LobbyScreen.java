package BTLClient;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class LobbyScreen extends JFrame {
    private String username;
    private NetworkHandler network;
    private JTextArea playerListArea;
    private Thread listenThread;
    private BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();

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

        // ===== Gửi tên người dùng lên server =====
        try {
            network.sendMsg("LOGIN|" + username);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Không thể gửi tên người dùng đến server.");
        }

        // ===== Luồng nhận thông tin từ server =====
        listenThread = new Thread(this::listenServer);
        listenThread.start();

        // ===== Sự kiện nút =====
        btnCreate.addActionListener(e -> new Thread(this::sendCreateRoom).start());
        btnJoin.addActionListener(e -> new Thread(this::sendJoinRoom).start());
    }

    // 🔸 Gửi yêu cầu tạo phòng
    private void sendCreateRoom() {
        try {
            network.sendMsg("CREATE"); // Gửi CREATE cho server
            System.out.println("📤 Đã gửi yêu cầu CREATE");

            // ⬅️ Chờ tin phản hồi từ hàng đợi
            String response = messageQueue.take();
            System.out.println("📥 Nhận phản hồi: " + response);

            if (response.startsWith("ROOM_CREATED;")) {
                String roomName = response.split(";")[1];
                JOptionPane.showMessageDialog(this, "🏠 Phòng \"" + roomName + "\" đã tạo thành công!");
                new GameScreen(username, network, true, roomName).setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "❌ Không thể tạo phòng: " + response);
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Mất kết nối đến server.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 🔸 Gửi yêu cầu tham gia phòng
    private void sendJoinRoom() {
        String roomCode = JOptionPane.showInputDialog(this, "Nhập tên hoặc mã phòng cần tham gia:");
        if (roomCode == null || roomCode.isEmpty()) return;

        try {
            network.sendMsg("JOIN;" + roomCode);
            System.out.println("📤 Đã gửi yêu cầu JOIN");

            // ⬅️ Chờ phản hồi từ hàng đợi
            String response = messageQueue.take();
            System.out.println("📥 Nhận phản hồi: " + response);

            if (response.startsWith("JOIN_OK;")) {
                JOptionPane.showMessageDialog(this, "✅ Đã tham gia phòng thành công!");
                new GameScreen(username, network, false, roomCode).setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "❌ Tham gia thất bại: " + response);
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Mất kết nối đến server.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 🔸 Thread nhận dữ liệu duy nhất
    private void listenServer() {
        try {
            while (true) {
                String msg = network.readMsg();
                System.out.println("📨 Nhận từ server: " + msg);

                if (msg.startsWith("PLAYER_LIST|")) {
                    String players = msg.substring("PLAYER_LIST|".length());
                    SwingUtilities.invokeLater(() -> playerListArea.setText(players.replace("|", "\n")));
                } else {
                    // ⚡ Gửi các tin khác (như ROOM_CREATED, JOIN_OK, v.v.) vào hàng đợi
                    messageQueue.offer(msg);
                }
            }
        } catch (IOException e) {
            System.out.println("⚠️ Ngắt kết nối đến server.");
        }
    }
}
