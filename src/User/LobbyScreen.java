package User;

import BTLClient.*;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class LobbyScreen extends JFrame {
    private String username;
    private NetworkHandler network;

    private JButton btnCreate, btnJoin;
    private JTextArea onlineList;
    private JLabel lblPlayerInfo;

    public LobbyScreen(String username, NetworkHandler network) {
        this.username = username;
        this.network = network;

        setTitle("Phòng chờ - " + username);
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Panel chính chia tỷ lệ 7:3
        JPanel mainPanel = new JPanel(new GridLayout(1, 2));

        // === Bên trái (7 phần): Nút Create + Join ===
        JPanel leftPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        btnCreate = new JButton("Create Room");
        btnJoin = new JButton("Join Room");

        leftPanel.add(btnCreate);
        leftPanel.add(btnJoin);
        leftPanel.setBorder(BorderFactory.createTitledBorder("Phòng"));

        // === Bên phải (3 phần): Danh sách + thông tin ===
        JPanel rightPanel = new JPanel(new BorderLayout());
        onlineList = new JTextArea();
        onlineList.setEditable(false);
        onlineList.setText("🟢 Danh sách người chơi online:\n(Chưa có dữ liệu)");

        lblPlayerInfo = new JLabel("👤 Người chơi: " + username, SwingConstants.CENTER);

        rightPanel.add(new JScrollPane(onlineList), BorderLayout.CENTER);
        rightPanel.add(lblPlayerInfo, BorderLayout.SOUTH);
        rightPanel.setBorder(BorderFactory.createTitledBorder("Thông tin"));

        // Gộp lại 7:3 (tương đối)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(450);
        add(splitPane, BorderLayout.CENTER);

        // === Sự kiện ===
        btnCreate.addActionListener(e -> {
            try {
                network.sendMsg("CREATE");
                new GameScreen(username, network, true).setVisible(true);
                dispose();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Không thể gửi yêu cầu tạo phòng!");
            }
        });

        btnJoin.addActionListener(e -> {
            try {
                network.sendMsg("JOIN");
                new GameScreen(username, network, false).setVisible(true);
                dispose();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Không thể gửi yêu cầu tham gia phòng!");
            }
        });
    }
}
