package server;

import java.io.*;
import java.net.*;
import java.util.*;

public class BTLTM {
    private static Map<String, String> accounts = new HashMap<>();
    private static Map<String, RoomThread> rooms = new HashMap<>();

    static {
        accounts.put("admin", "123");
        accounts.put("user1", "abc");
        accounts.put("user2", "xyz");
    }

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(5000)) {

            InetAddress localHost = InetAddress.getLocalHost();
            System.out.println("🟢 Server đang chạy trên cổng 5000");
            System.out.println("📡 IP: " + localHost.getHostAddress());

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("👤 Người dùng mới kết nối.");
                new ClientHandler(socket).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ==========================================
    // 🔹 Thread xử lý mỗi client
    // ==========================================
    static class ClientHandler extends Thread {
        private Socket socket;
        private DataInputStream in;
        private DataOutputStream out;
        private String username;
        private String currentRoom;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());

                // Đăng nhập
                String loginMsg = in.readUTF();
                if (loginMsg.startsWith("LOGIN;")) {
                    String[] parts = loginMsg.split(";");
                    String user = parts[1];
                    String pass = parts[2];
                    if (accounts.containsKey(user) && accounts.get(user).equals(pass)) {
                        out.writeUTF("LOGIN_OK");
                        username = user;
                        System.out.println("✅ " + user + " đăng nhập thành công.");
                    } else {
                        out.writeUTF("LOGIN_FAIL");
                        socket.close();
                        return;
                    }
                }

                while (true) {
                    String msg = in.readUTF();
                    if (msg.equalsIgnoreCase("exit")) break;

                    // Tạo phòng
                    if (msg.equalsIgnoreCase("CREATE")) {
                        handleCreateRoom(username);
                        continue;
                    }

                    // Tham gia phòng
                    if (msg.startsWith("JOIN;")) {
                        String roomName = msg.split(";")[1];
                        handleJoinRoom(roomName);
                        continue;
                    }

                    // Bắt đầu game (START;Room_x)
                    if (msg.startsWith("START;")) {
                        String roomName = msg.split(";")[1];
                        RoomThread r = rooms.get(roomName);
                        if (r != null) {
                            r.startGame();
                            // Gửi phản hồi cho client để biết phòng nào bắt đầu
                            out.writeUTF("START_OK;" + roomName);
                        }
                        continue;
                    }

                    // Rút bài
                    if (msg.contains(":Draw")) {
                        System.out.println(msg);
                        String[] parts = msg.split(":");
                        int playerID = Integer.parseInt(parts[0]);
                        if (currentRoom != null && rooms.containsKey(currentRoom)) {
                            rooms.get(currentRoom).playerDrawCard(playerID);
                        }
                        continue;
                    }

                    // Nhận bài đã chọn
                    if (msg.matches("\\d+:.*")) {
                        String[] parts = msg.split(":");
                        System.out.println("🃏 Người chơi " + parts[0] + ", bài là:" + parts[1]);
                    }
                }

                socket.close();
            } catch (IOException e) {
                System.out.println("⚠️ Client ngắt kết nối: " + username);
            }
        }

        // 🔸 Khi tạo phòng, gửi lại số phòng cho client
        private void handleCreateRoom(String user) throws IOException {
            String roomName = "Room_" + (rooms.size() + 1);
            if (!rooms.containsKey(roomName)) {
                RoomThread newRoom = new RoomThread(roomName);
                rooms.put(roomName, newRoom);
                newRoom.start(); // chạy thread phòng riêng
                currentRoom = roomName;
                newRoom.addPlayer(this);
                // gửi lại cho client biết đã tạo phòng và số phòng
                out.writeUTF("ROOM_CREATED;" + roomName);
                System.out.println("🏠 " + user + " đã tạo phòng: " + roomName);
            }
        }

        private void handleJoinRoom(String roomName) throws IOException {
            if (rooms.containsKey(roomName)) {
                currentRoom = roomName;
                rooms.get(roomName).addPlayer(this);
                out.writeUTF("JOIN_OK;" + roomName);
                System.out.println("👥 " + username + " tham gia phòng " + roomName);
            } else {
                out.writeUTF("JOIN_FAIL");
            }
        }

        public void sendMessage(String msg) {
            try {
                out.writeUTF(msg);
            } catch (IOException e) {
                System.out.println("❌ Gửi thất bại tới " + username);
            }
        }
    }

    // ==========================================
    // 🔸 Thread cho từng phòng chơi
    // ==========================================
    static class RoomThread extends Thread {
        private String roomName;
        private List<ClientHandler> players = new ArrayList<>();
        private int[] cards = new int[52];
        private int drawCount = 0; // đếm số lần Draw

        public RoomThread(String name) {
            this.roomName = name;
            for (int i = 0; i < 52; i++) cards[i] = i;
        }

        public synchronized void addPlayer(ClientHandler p) {
            players.add(p);
        }

        public synchronized void removePlayer(ClientHandler p) {
            players.remove(p);
        }

        public void run() {
            System.out.println("🧩 Phòng " + roomName + " đã sẵn sàng.");
            // Chờ tín hiệu Start
        }

        public void startGame() {
            shuffleCards();
            broadcast("READY;" + roomName);
            System.out.println("🎮 " + roomName + " bắt đầu, bài đã được tráo!");
        }

        private void shuffleCards() {
            List<Integer> list = new ArrayList<>();
            for (int i = 0; i < 52; i++) list.add(i);
            Collections.shuffle(list);
            for (int i = 0; i < 52; i++) cards[i] = list.get(i);
        }

        public synchronized void playerDrawCard(int playerID) {
            for (int i = 0; i < 52; i++) {
                if (cards[i] != -1) {
                    int cardValue = cards[i];
                    cards[i] = -1;
                    drawCount++;
                    players.get(playerID - 1).sendMessage("DRAW;" + cardValue);
                    System.out.println("🂠 Player " + playerID + " rút bài: " + cardValue);
                    break;
                }
            }

            // Sau khi tất cả đã rút 3 lần → gửi "END"
            if (drawCount >= players.size() * 3) {
                broadcast("END;" + roomName);
                drawCount = 0; // reset để có thể chơi tiếp
                System.out.println("🏁 Vòng rút bài kết thúc trong " + roomName);
            }
        }

        private void broadcast(String msg) {
            for (ClientHandler p : players) p.sendMessage(msg);
        }
    }
}
