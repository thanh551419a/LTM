package User;

import BTLClient.*;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class GameScreen extends JFrame {
    private String username;
    private NetworkHandler network;
    private boolean isHost;

    public GameScreen(String username, NetworkHandler network, boolean isHost) {
        this.username = username;
        this.network = network;
        this.isHost = isHost;

        setTitle("Bàn chơi - " + username + (isHost ? " (Chủ phòng)" : ""));
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        GamePanel panel = new GamePanel();
        add(panel);
    }

    // Panel để bạn dễ vẽ sprite sau này
    class GamePanel extends JPanel {
        public GamePanel() {
            setBackground(Color.darkGray);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.white);
            g.drawString("🎮 Đây là bàn chơi (scene game)", 300, 100);
            g.drawString("Người chơi: " + username, 320, 130);
        }
    }
}
