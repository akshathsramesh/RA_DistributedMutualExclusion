public class Message {

    String clientId;
    String timeStamp;

    public Message(String clientId, String timeStamp) {
        this.clientId = clientId;
        this.timeStamp = timeStamp;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
