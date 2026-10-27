import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

public class ChatClient {
    
    private static final Color PRIMARY_GREEN = new Color(16, 185, 129);
    private static final Color DARK_GREEN = new Color(5, 150, 105);
    private static final Color ACCENT_GREEN = new Color(52, 211, 153);
    private static final Color BG_DARK = new Color(17, 24, 39);
    private static final Color BG_CARD = new Color(31, 41, 55);
    private static final Color BG_INPUT = new Color(55, 65, 81);
    private static final Color TEXT_PRIMARY = new Color(243, 244, 246);
    private static final Color TEXT_SECONDARY = new Color(156, 163, 175);
    private static final Color BORDER_COLOR = new Color(75, 85, 99);
    private static final Color BUTTON_RED = new Color(239, 68, 68);
    private JFrame frame;
    private JTextPane taMessages;
    private JTextField tfInput;
    private JButton btnSend;
    private JList<String> userList, roomList;
    private DefaultListModel<String> userListModel, roomListModel;
    private JComboBox<String> cbTarget;
    private String username;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Socket socket;
    private volatile boolean running = false;

    public ChatClient(String host, int port, String username) throws IOException {
        this.username = username;
        socket = new Socket(host, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());
        buildGui();
        connectToServer();
        startReader();
    }

    private void buildGui() {
        frame = new JFrame("Kaya Chat");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1150, 720);
        frame.setLayout(new BorderLayout(0, 0));
        frame.getContentPane().setBackground(BG_DARK);
        
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(BG_DARK);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel header = createHeader();
        mainPanel.add(header, BorderLayout.NORTH);

        JPanel centerPanel = createMessagesPanel();
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        JPanel rightPanel = createRightPanel();
        mainPanel.add(rightPanel, BorderLayout.EAST);

        JPanel bottomPanel = createBottomPanel();
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.addWindowListener(new WindowAdapter() { 
            public void windowClosing(WindowEvent e) { disconnect(); } 
        });

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_CARD);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_GREEN, 2, true),
            BorderFactory.createEmptyBorder(18, 25, 18, 25)
        ));
        
        JLabel title = new JLabel("Kaya Chat");
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(PRIMARY_GREEN);
        
        JLabel userBadge = new JLabel("User: " + username);
        userBadge.setFont(new Font("Segoe UI", Font.BOLD, 15));
        userBadge.setForeground(TEXT_PRIMARY);
        userBadge.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_GREEN, 2, true),
            BorderFactory.createEmptyBorder(8, 18, 8, 18)
        ));
        userBadge.setOpaque(true);
        userBadge.setBackground(DARK_GREEN);
        
        header.add(title, BorderLayout.WEST);
        header.add(userBadge, BorderLayout.EAST);
        
        return header;
    }

    private JPanel createMessagesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_DARK);
        
        taMessages = new JTextPane();
        taMessages.setEditable(false);
        taMessages.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        taMessages.setBackground(BG_CARD);
        taMessages.setForeground(TEXT_PRIMARY);
        taMessages.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        taMessages.setCaretColor(PRIMARY_GREEN);
        
        JScrollPane scrollPane = new JScrollPane(taMessages);
        scrollPane.setBorder(createModernBorder("Messages"));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setBackground(BG_INPUT);
        scrollPane.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = PRIMARY_GREEN;
                this.trackColor = BG_INPUT;
            }
        });
        
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 20));
        panel.setBackground(BG_DARK);
        panel.setPreferredSize(new Dimension(290, 0));

        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        styleList(userList);
        
        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.setBackground(BG_DARK);
        userPanel.setBorder(createModernBorder("Online Users"));
        
        JScrollPane userScroll = new JScrollPane(userList);
        userScroll.setBorder(null);
        userScroll.getVerticalScrollBar().setBackground(BG_INPUT);
        userPanel.add(userScroll, BorderLayout.CENTER);

        roomListModel = new DefaultListModel<>();
        roomList = new JList<>(roomListModel);
        styleList(roomList);
        
        JPanel roomPanel = new JPanel(new BorderLayout(0, 10));
        roomPanel.setBackground(BG_DARK);
        roomPanel.setBorder(createModernBorder("Chat Rooms"));
        
        JScrollPane roomScroll = new JScrollPane(roomList);
        roomScroll.setBorder(null);
        roomScroll.getVerticalScrollBar().setBackground(BG_INPUT);
        
        JPanel roomButtons = new JPanel(new GridLayout(1, 3, 10, 0));
        roomButtons.setBackground(BG_CARD);
        roomButtons.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton btnCreate = createModernButtonSimple("Create", ACCENT_GREEN);
        JButton btnJoin = createModernButtonSimple("Join", ACCENT_GREEN);
        JButton btnLeave = createModernButtonSimple("Leave", BUTTON_RED);
        
        btnCreate.addActionListener(e -> createRoomDialog());
        btnJoin.addActionListener(e -> joinSelectedRoom());
        btnLeave.addActionListener(e -> leaveSelectedRoom());
        
        roomButtons.add(btnCreate);
        roomButtons.add(btnJoin);
        roomButtons.add(btnLeave);
        
        roomPanel.add(roomScroll, BorderLayout.CENTER);
        roomPanel.add(roomButtons, BorderLayout.SOUTH);

        userList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String sel = userList.getSelectedValue();
                    if (sel != null) setTargetToUser(sel);
                }
            }
        });
        
        roomList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String sel = roomList.getSelectedValue();
                    if (sel != null) setTargetToRoom(sel);
                }
            }
        });

        panel.add(userPanel);
        panel.add(roomPanel);
        
        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_DARK);
        
        JPanel targetPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        targetPanel.setBackground(BG_DARK);
        
        JLabel lblTarget = new JLabel("Send to:");
        lblTarget.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTarget.setForeground(TEXT_PRIMARY);
        
        cbTarget = new JComboBox<>();
        cbTarget.addItem("All");
        cbTarget.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbTarget.setBackground(BG_INPUT);
        cbTarget.setForeground(TEXT_PRIMARY);
        cbTarget.setPreferredSize(new Dimension(220, 38));
        cbTarget.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        
        targetPanel.add(lblTarget);
        targetPanel.add(cbTarget);
        
        panel.add(targetPanel, BorderLayout.NORTH);
        
        JPanel inputPanel = new JPanel(new BorderLayout(15, 0));
        inputPanel.setBackground(BG_DARK);
        
        tfInput = new JTextField();
        tfInput.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        tfInput.setBackground(BG_INPUT);
        tfInput.setForeground(TEXT_PRIMARY);
        tfInput.setCaretColor(PRIMARY_GREEN);
        tfInput.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 2, true),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        tfInput.addActionListener(e -> sendMessageFromInput());
        
        btnSend = new JButton("SEND");
        btnSend.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnSend.setBackground(ACCENT_GREEN);
        btnSend.setForeground(Color.BLACK);
        btnSend.setFocusPainted(false);
        btnSend.setBorderPainted(false);
        btnSend.setPreferredSize(new Dimension(140, 52));
        btnSend.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSend.addActionListener(e -> sendMessageFromInput());
        

        
        inputPanel.add(tfInput, BorderLayout.CENTER);
        inputPanel.add(btnSend, BorderLayout.EAST);
        
        panel.add(inputPanel, BorderLayout.CENTER);
        
        return panel;
    }

    private void styleList(JList<String> list) {
        list.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        list.setBackground(BG_CARD);
        list.setForeground(TEXT_PRIMARY);
        list.setSelectionBackground(PRIMARY_GREEN);
        list.setSelectionForeground(Color.WHITE);
        list.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        list.setFixedCellHeight(38);
    }

    private JButton createModernButton(String text, Color normalColor, Color hoverColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(normalColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(hoverColor);
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(normalColor);
            }
        });
        
        return btn;
    }

    private JButton createModernButtonSimple(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(color);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private Border createModernBorder(String title) {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PRIMARY_GREEN, 2, true),
                title,
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14),
                PRIMARY_GREEN
            ),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        );
    }

    private void setTargetToUser(String user) {
        DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) cbTarget.getModel();
        if (model.getIndexOf(user) == -1) model.addElement(user);
        cbTarget.setSelectedItem(user);
    }

    private void setTargetToRoom(String room) {
        String label = "Room:" + room;
        DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) cbTarget.getModel();
        if (model.getIndexOf(label) == -1) model.addElement(label);
        cbTarget.setSelectedItem(label);
    }

    private void createRoomDialog() {
        JPanel dialogPanel = new JPanel(new BorderLayout(10, 10));
        dialogPanel.setBackground(BG_CARD);
        dialogPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel label = new JLabel("Room name:");
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(TEXT_PRIMARY);
        
        JTextField tfRoom = new JTextField(20);
        tfRoom.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tfRoom.setBackground(BG_INPUT);
        tfRoom.setForeground(TEXT_PRIMARY);
        tfRoom.setCaretColor(PRIMARY_GREEN);
        tfRoom.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        
        dialogPanel.add(label, BorderLayout.NORTH);
        dialogPanel.add(tfRoom, BorderLayout.CENTER);
        
        int result = JOptionPane.showConfirmDialog(frame, dialogPanel, "Create Room", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String room = tfRoom.getText().trim();
            if (room.isEmpty()) return;
            
            Message m = new Message();
            m.setType(Message.Type.SYSTEM);
            m.setSender(username);
            m.setContent("CREATE_ROOM:" + room);
            sendMessageObject(m);
        }
    }

    private void joinSelectedRoom() {
        String room = roomList.getSelectedValue();
        if (room == null) {
            JOptionPane.showMessageDialog(frame, "Please select a room from the list.");
            return;
        }
        Message m = new Message();
        m.setType(Message.Type.SYSTEM);
        m.setSender(username);
        m.setContent("JOIN_ROOM:" + room);
        sendMessageObject(m);
    }

    private void leaveSelectedRoom() {
        String room = roomList.getSelectedValue();
        if (room == null) {
            JOptionPane.showMessageDialog(frame, "Please select a room from the list.");
            return;
        }
        Message m = new Message();
        m.setType(Message.Type.SYSTEM);
        m.setSender(username);
        m.setContent("LEAVE_ROOM:" + room);
        sendMessageObject(m);
    }

    private void sendMessageFromInput() {
        String text = tfInput.getText().trim();
        if (text.isEmpty()) return;
        
        Object sel = cbTarget.getSelectedItem();
        String target = sel == null ? "All" : sel.toString();
        
        Message m = new Message();
        if ("All".equals(target)) {
            m.setType(Message.Type.BROADCAST);
            m.setContent(text);
            m.setSender(username);
        } else if (target.startsWith("Room:")) {
            m.setType(Message.Type.ROOM);
            m.setRoom(target.substring("Room:".length()));
            m.setContent(text);
            m.setSender(username);
        } else {
            m.setType(Message.Type.PRIVATE);
            m.setRecipient(target);
            m.setContent(text);
            m.setSender(username);
        }
        
        sendMessageObject(m);
        tfInput.setText("");
    }

    private void connectToServer() {
        try {
            Message connect = new Message(Message.Type.CONNECT, username, "connect");
            out.writeObject(connect);
            out.flush();
            running = true;
        } catch (IOException e) {
            appendStyled("Connection failed: " + e.getMessage(), "system");
        }
    }

    private void startReader() {
        Thread reader = new Thread(() -> {
            try {
                while (running) {
                    Object obj = in.readObject();
                    if (!(obj instanceof Message)) continue;
                    Message m = (Message) obj;
                    handleIncoming(m);
                }
            } catch (EOFException | SocketException eof) {
                appendStyled("Disconnected from server.", "system");
                running = false;
            } catch (Exception e) {
                e.printStackTrace();
                appendStyled("Error: " + e.getMessage(), "system");
            } finally { disconnect(); }
        });
        reader.setDaemon(true);
        reader.start();
    }

    private void handleIncoming(Message m) {
        if (m == null) return;
        switch (m.getType()) {
            case BROADCAST:
                appendStyled(String.format("[All] %s: %s", m.getSender(), m.getContent()), "broadcast");
                break;
            case PRIVATE:
                appendStyled(String.format("[Private] %s: %s", m.getSender(), m.getContent()), "private");
                break;
            case ROOM:
                appendStyled(String.format("[%s] %s: %s", m.getRoom(), m.getSender(), m.getContent()), "room");
                break;
            case SYSTEM:
                appendStyled(m.getContent(), "system");
                break;
            case USER_LIST:
                SwingUtilities.invokeLater(() -> {
                    userListModel.clear();
                    if (m.getUsers() != null) {
                        for (String u : m.getUsers()) {
                            if (!u.equals(username)) userListModel.addElement(u);
                        }
                        updateTargetsWithUsers(m.getUsers());
                    }
                });
                break;
            case ROOM_LIST:
                SwingUtilities.invokeLater(() -> {
                    roomListModel.clear();
                    if (m.getRooms() != null) {
                        for (String r : m.getRooms().keySet()) roomListModel.addElement(r);
                        updateTargetsWithRooms(new ArrayList<>(m.getRooms().keySet()));
                    }
                });
                break;
            default:
                appendStyled(m.getContent(), "default");
        }
    }

    private void appendStyled(String text, String type) {
        SwingUtilities.invokeLater(() -> {
            javax.swing.text.StyledDocument doc = taMessages.getStyledDocument();
            javax.swing.text.Style style = taMessages.addStyle("Style", null);
            
            switch (type) {
                case "broadcast":
                    javax.swing.text.StyleConstants.setForeground(style, TEXT_PRIMARY);
                    javax.swing.text.StyleConstants.setBold(style, false);
                    break;
                case "private":
                    javax.swing.text.StyleConstants.setForeground(style, ACCENT_GREEN);
                    javax.swing.text.StyleConstants.setBold(style, true);
                    break;
                case "room":
                    javax.swing.text.StyleConstants.setForeground(style, new Color(96, 165, 250));
                    javax.swing.text.StyleConstants.setBold(style, false);
                    break;
                case "system":
                    javax.swing.text.StyleConstants.setForeground(style, TEXT_SECONDARY);
                    javax.swing.text.StyleConstants.setItalic(style, true);
                    break;
                default:
                    javax.swing.text.StyleConstants.setForeground(style, TEXT_PRIMARY);
            }
            
            try {
                doc.insertString(doc.getLength(), text + "\n", style);
                taMessages.setCaretPosition(doc.getLength());
            } catch (javax.swing.text.BadLocationException e) {
                e.printStackTrace();
            }
        });
    }

    private void updateTargetsWithUsers(java.util.List<String> users) {
        if (users == null) users = Collections.<String>emptyList();
        Object previousSelection = cbTarget.getSelectedItem();
        DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) cbTarget.getModel();
        model.removeAllElements();
        model.addElement("All");
        for (String u : users) if (!u.equals(username)) model.addElement(u);
        if (previousSelection != null) {
            for (int i = 0; i < model.getSize(); i++) {
                if (model.getElementAt(i).equals(previousSelection)) {
                    cbTarget.setSelectedItem(previousSelection); 
                    return;
                }
            }
        }
        cbTarget.setSelectedIndex(0);
    }

    private void updateTargetsWithRooms(java.util.List<String> rooms) {
        Object previousSelection = cbTarget.getSelectedItem();
        DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) cbTarget.getModel();
        for (String r : rooms) {
            String label = "Room:" + r;
            if (model.getIndexOf(label) == -1) model.addElement(label);
        }
        for (int i = model.getSize() - 1; i >= 0; i--) {
            String item = model.getElementAt(i);
            if (item.startsWith("Room:")) {
                String raw = item.substring(5);
                if (!rooms.contains(raw)) model.removeElementAt(i);
            }
        }
        if (previousSelection != null) {
            for (int i = 0; i < model.getSize(); i++) {
                if (model.getElementAt(i).equals(previousSelection)) {
                    cbTarget.setSelectedItem(previousSelection); 
                    return;
                }
            }
        }
        cbTarget.setSelectedIndex(0);
    }

    private synchronized void sendMessageObject(Message m) {
        try { 
            out.writeObject(m); 
            out.flush(); 
        } catch (IOException e) {
            appendStyled("Send error: " + e.getMessage(), "system");
        }
    }

    private void disconnect() {
        if (!running) {
            try { socket.close(); } catch (Exception ignored) {}
            return;
        }
        running = false;
        try {
            Message disc = new Message(Message.Type.DISCONNECT, username, "bye");
            out.writeObject(disc); 
            out.flush();
        } catch (Exception e) {}
        try { socket.close(); } catch (Exception ignored) {}
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            JPanel loginPanel = new JPanel(new GridBagLayout());
            loginPanel.setBackground(BG_DARK);
            loginPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_GREEN, 2, true),
                BorderFactory.createEmptyBorder(25, 25, 25, 25)
            ));
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(10, 10, 10, 10);
            
            JTextField tfHost = new JTextField("localhost", 20);
            JTextField tfPort = new JTextField("12345", 20);
            JTextField tfUser = new JTextField(20);
            
            Font labelFont = new Font("Segoe UI", Font.BOLD, 14);
            Font fieldFont = new Font("Segoe UI", Font.PLAIN, 14);
            
            for (JTextField tf : Arrays.asList(tfHost, tfPort, tfUser)) {
                tf.setFont(fieldFont);
                tf.setBackground(BG_INPUT);
                tf.setForeground(TEXT_PRIMARY);
                tf.setCaretColor(PRIMARY_GREEN);
                tf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR, 1),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
                ));
            }
            
            JLabel lblHost = new JLabel("Server Host:");
            JLabel lblPort = new JLabel("Server Port:");
            JLabel lblUser = new JLabel("Username:");
            
            for (JLabel lbl : Arrays.asList(lblHost, lblPort, lblUser)) {
                lbl.setFont(labelFont);
                lbl.setForeground(TEXT_PRIMARY);
            }
            
            gbc.gridx = 0; gbc.gridy = 0;
            loginPanel.add(lblHost, gbc);
            gbc.gridx = 1;
            loginPanel.add(tfHost, gbc);
            
            gbc.gridx = 0; gbc.gridy = 1;
            loginPanel.add(lblPort, gbc);
            gbc.gridx = 1;
            loginPanel.add(tfPort, gbc);
            
            gbc.gridx = 0; gbc.gridy = 2;
            loginPanel.add(lblUser, gbc);
            gbc.gridx = 1;
            loginPanel.add(tfUser, gbc);
            
            int result = JOptionPane.showConfirmDialog(null, loginPanel, 
                "Connect to Chat Server", JOptionPane.OK_CANCEL_OPTION, 
                JOptionPane.PLAIN_MESSAGE);
            
            if (result == JOptionPane.OK_OPTION) {
                String host = tfHost.getText().trim();
                int port = Integer.parseInt(tfPort.getText().trim());
                String user = tfUser.getText().trim();
                
                if (user.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Username is required.");
                    return;
                }
                
                try { 
                    new ChatClient(host, port, user); 
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, "Connection failed: " + e.getMessage());
                }
            }
        });
    }
}