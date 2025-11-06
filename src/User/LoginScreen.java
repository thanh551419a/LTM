package User;

import BTLClient.*;
import javax.swing.*;
import java.awt.*;

public class LoginScreen extends JFrame {
    private JTextField txtUser;
    private JPasswordField txtPass;
    private JButton btnLogin, btnRegister;
    private JLabel lblStatus;

    public LoginScreen() {
        setTitle("Đăng nhập / Đăng ký");
        setSize(350, 220);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridLayout(3, 2, 5, 5));
        txtUser = new JTextField();
        txtPass = new JPasswordField();

        form.add(new JLabel("Tên đăng nhập:"));
        form.add(txtUser);
        form.add(new JLabel("Mật khẩu:"));
        form.add(txtPass);

        btnLogin = new JButton("Đăng nhập");
        btnRegister = new JButton("Đăng ký");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnLogin);
        buttonPanel.add(btnRegister);

        lblStatus = new JLabel(" ", SwingConstants.CENTER);

        add(form, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        add(lblStatus, BorderLayout.NORTH);

        btnLogin.addActionListener(e -> connectAndLogin());
        btnRegister.addActionListener(e -> JOptionPane.showMessageDialog(this, "Tính năng đang phát triển"));
    }

    private void connectAndLogin() {
        String user = txtUser.getText().trim();
        String pass = new String(txtPass.getPassword()).trim();

        if (user.isEmpty() || pass.isEmpty()) {
            lblStatus.setText("⚠️ Nhập đầy đủ thông tin!");
            return;
        }

        new Thread(() -> {
            try {
                NetworkHandler net = new NetworkHandler("26.205.157.69", 5000);
                if (net.login(user, pass)) {
                    SwingUtilities.invokeLater(() -> {
                        new LobbyScreen(user, net).setVisible(true);
                        dispose();
                    });
                } else {
                    lblStatus.setText("❌ Sai tài khoản hoặc mật khẩu!");
                }
            } catch (Exception ex) {
                lblStatus.setText("❌ Không thể kết nối tới server!");
            }
        }).start();
    }
}
