import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Client {


    String Id;
    List<Node> allClientNodes = new LinkedList<>();
    List<Node> allServerList = new LinkedList<>();

    Integer logicalClock = 0;

    public Client(String id) {
        this.Id = id;
    }

    public String getId() {
        return this.Id;
    }

    public void setId(String id) {
        this.Id = id;
    }


    public List<Node> getAllClientNodes() {
        return allClientNodes;
    }

    public void setAllClientNodes(List<Node> allClientNodes) {
        this.allClientNodes = allClientNodes;
    }

    public Integer getLogicalClock() {
        return logicalClock;
    }

    public void setLogicalClock(Integer logicalClock) {
        this.logicalClock = logicalClock;
    }







    public static void main(String[] args) {


//        check for arguments and use the client id to start the client node


        Client C1 = new Client("1");


        System.out.println("Starting Client with ID: " + C1.getId());

        try {
            BufferedReader br = new BufferedReader(new FileReader("ClientAddressAndPorts.txt"));
            try {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    List<String> parsed = Arrays.asList(line.split(","));
                    System.out.println(parsed);
                    Node n1 = new Node(parsed.get(0),parsed.get(1),parsed.get(2));
                    C1.getAllClientNodes().add(n1);
                    sb.append(System.lineSeparator());
                    line = br.readLine();
                }
                String everything = sb.toString();
                System.out.println(everything);
                System.out.println(C1.getAllClientNodes().size());

            } finally {
                br.close();
            }
        }
        catch (Exception e){

        }
    }
}
