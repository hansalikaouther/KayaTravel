import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ChatServer {
    private final int port;
    private ServerSocket serverSocket;
    private final ConcurrentMap<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Set<String>> rooms = new ConcurrentHashMap<>();

    public ChatServer(int port) { this.port = port; }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("ChatServer running on port " + port);
        while (true) {
            Socket socket = serverSocket.accept();
            new Thread(() -> {
                try { handleNewSocket(socket); }
                catch (IOException e) { e.printStackTrace(); }
            }).start();
        }
    }

    private void handleNewSocket(Socket socket) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        try {
            Object obj = in.readObject();
            if (!(obj instanceof Message)) { socket.close(); return; }
            Message connectMsg = (Message) obj;
            if (connectMsg.getType() != Message.Type.CONNECT) { socket.close(); return; }
            String username = connectMsg.getSender().trim();
            if (username.isEmpty() || clients.containsKey(username)) {
                Message sys = Message.system("Username invalide or Used .");
                out.writeObject(sys); out.flush(); socket.close(); return;
            }
            ClientHandler handler = new ClientHandler(username, socket, in, out);
            clients.put(username, handler);
            handler.start();
            broadcastSystem(username + " Joined chat !.");
            sendUserList(); sendRoomList();
        } catch (Exception e) { socket.close(); }
    }

    private void broadcast(Message m) { for (ClientHandler ch : clients.values()) ch.send(m); }
    private void broadcastSystem(String txt) { broadcast(Message.system(txt)); }
    private void sendUserList() {
        broadcast(Message.userList(new ArrayList<>(clients.keySet())));
    }
    private void sendRoomList() {
        Map<String, List<String>> map = new HashMap<>();
        for (String r : rooms.keySet())
            map.put(r, new ArrayList<>(rooms.get(r)));
        broadcast(Message.roomList(map));
    }
    private void handleRoomCommand(String sender, String cmd) {
        if (cmd.startsWith("CREATE_ROOM:")) {
            String room = cmd.substring(12).trim();
            rooms.computeIfAbsent(room, k -> ConcurrentHashMap.newKeySet()).add(sender);
            broadcastSystem(sender + " created and joined '" + room + "'.");
            sendRoomList();
        }
        else if (cmd.startsWith("JOIN_ROOM:")) {
            String room = cmd.substring(10).trim();
            rooms.computeIfAbsent(room, k -> ConcurrentHashMap.newKeySet()).add(sender);
                broadcastSystem(sender + " joined '" + room + "'.");
            sendRoomList();
        }
        else if (cmd.startsWith("LEAVE_ROOM:")) {
            String room = cmd.substring(11).trim();
            Set<String> members = rooms.get(room);
            if (members != null) {
                members.remove(sender);
                if (members.isEmpty()) rooms.remove(room);
                broadcastSystem(sender + " left '" + room + "'.");
                sendRoomList();
            }
        }
    }
    private class ClientHandler extends Thread {
        private final String username;
        private final Socket socket;
        private final ObjectInputStream in;
        private final ObjectOutputStream out;
        private volatile boolean running = true;
        public ClientHandler(String username, Socket socket, ObjectInputStream in, ObjectOutputStream out) {
            this.username = username; this.socket = socket; this.in = in; this.out = out;
        }
        public void send(Message m) {
            try { out.writeObject(m); out.flush(); }
            catch (IOException e) { shutdown(); }
        }
        public void run() {
            try {
                while (running) {
                    Object obj = in.readObject();
                    if (!(obj instanceof Message)) continue;
                    Message m = (Message)obj;
                    switch (m.getType()) {
                        case CONNECT:
                        case ROOM_LIST:
                        case USER_LIST: break;
                        case BROADCAST:
                            broadcast(new Message(Message.Type.BROADCAST, username, m.getContent())); break;
                        case PRIVATE:
                            ClientHandler dest = clients.get(m.getRecipient());
                            if (dest != null) {
                                dest.send(new Message(Message.Type.PRIVATE, username, m.getContent()));
                                send(new Message(Message.Type.PRIVATE, username, m.getContent()));
                            } break;
                        case ROOM:
                            String room = m.getRoom();
                            if (rooms.containsKey(room) && rooms.get(room).contains(username)) {
                                for (String user : rooms.get(room)) {
                                    ClientHandler ch = clients.get(user);
                                    if (ch != null)
                                        ch.send(new Message(Message.Type.ROOM, username, m.getContent()));
                                }
                            } break;
                        case SYSTEM: handleRoomCommand(username, m.getContent()); break;
                        case DISCONNECT: shutdown(); break;
                    }
                }
            } catch (Exception e) { shutdown(); }
        }
        private void shutdown() {
            if (!running) return;
            running = false;
            clients.remove(username);
            for (Set<String> set : rooms.values()) set.remove(username);
            rooms.entrySet().removeIf(e -> e.getValue().isEmpty());
            broadcastSystem(username + " left the chat .");
            sendUserList(); sendRoomList();
            try { socket.close(); } catch (IOException ignored) {}
        }
    }
    public static void main(String[] args) {
        try { new ChatServer(12345).start(); }
        catch (Exception e) { e.printStackTrace(); }
    }
}