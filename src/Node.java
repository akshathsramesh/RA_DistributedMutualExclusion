public class Node {

    String Id;
    String ipAddress;
    String port;

    public  Node(String Id, String ipAddress, String port){
        this.Id = Id;
        this.ipAddress = ipAddress;
        this.port = port;
    }


    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }
}
