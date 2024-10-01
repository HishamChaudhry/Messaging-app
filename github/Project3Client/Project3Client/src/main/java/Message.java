import java.io.Serializable;
import java.util.Date;

public class Message implements Serializable {
    private static final long serialVersionUID = 42L;

    public enum MessageType {
        PRIVATE, GROUP, BROADCAST, SYSTEM, USER_LIST
    } 

    private MessageType messageType;
    private String senderId;
    private String receiverId; 
    private Serializable  content;
    private Date timestamp;
    private String groupId; 

    public Message(MessageType messageType, String senderId, String receiverId, Serializable content, Date timestamp) {
        this.messageType = messageType;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.timestamp = timestamp;
    }


    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public Serializable  getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @Override
    public String toString() {
        return "Message{" +
               "type=" + messageType +
               ", from='" + senderId + '\'' +
               ", to='" + receiverId + '\'' +
               ", group='" + groupId + '\'' +
               ", time='" + timestamp + '\'' +
               ", content='" + content + '\'' +
               '}';
    }
}

