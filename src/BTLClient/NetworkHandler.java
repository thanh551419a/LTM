package BTLClient;

import java.io.*;
import java.net.*;

public class NetworkHandler {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    public NetworkHandler(String host, int port) throws IOException {
        socket = new Socket(host, port);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
    }

    public boolean login(String username, String password) throws IOException {
        out.writeUTF("LOGIN;" + username + ";" + password);
        String response = in.readUTF();
        return response.equals("LOGIN_OK");
    }

    public void sendMsg(String msg) throws IOException {
        out.writeUTF(msg);
    }

    public String readMsg() throws IOException {
        return in.readUTF();
    }

    public void close() throws IOException {
        socket.close();
    }
}
