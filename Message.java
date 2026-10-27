import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Message implements Serializable {
    public enum Type {
        CONNECT, DISCONNECT,
        BROADCAST, PRIVATE, ROOM, SYSTEM,
        USER_LIST, ROOM_LIST
    }
    private Type type;
    private String sender, recipient, content, room;
    private List<String> users;
    private Map<String, List<String>> rooms;

    public Message() {}
    public Message(Type type, String sender, String content) {
        this.type = type; this.sender = sender; this.content = content;
    }
    public static Message system(String text) { return new Message(Type.SYSTEM, "SERVER", text); }
    public static Message userList(List<String> users) { Message m = new Message(); m.type = Type.USER_LIST; m.users = users; return m; }
    public static Message roomList(Map<String, List<String>> rooms) { Message m = new Message(); m.type = Type.ROOM_LIST; m.rooms = rooms; return m; }

    public Type getType() { return type; }
    public String getSender() { return sender; }
    public String getRecipient() { return recipient; }
    public String getContent() { return content; }
    public String getRoom() { return room; }
    public List<String> getUsers() { return users; }
    public Map<String, List<String>> getRooms() { return rooms; }
    public void setType(Type t) { this.type=t; }
    public void setSender(String s) { this.sender=s; }
    public void setRecipient(String r) { this.recipient=r; }
    public void setContent(String c) { this.content=c; }
    public void setRoom(String r) { this.room=r; }
    public void setUsers(List<String> u) { this.users=u; }
    public void setRooms(Map<String, List<String>> rooms) { this.rooms=rooms; }
}