import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client {


    String Id;
    List<Node> allClientNodes = new LinkedList<>();
    List<Node> allServerNodes = new LinkedList<>();
    Integer logicalClock = 0;
    List<SocketConnection> socketConnectionList = new LinkedList<>();
    ServerSocket server;
    HashMap<String,SocketConnection> socketConnectionHashMap = new HashMap<>();
    Integer highestLogicalClockValue = 0;
    Integer outStandingReplyCount = 0;
    Boolean requestingCS = false;

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


    public List<Node> getAllServerNodes() {
        return allServerNodes;
    }

    public void setAllServerNodes(List<Node> allServerNodes) {
        this.allServerNodes = allServerNodes;
    }

    public Integer getLogicalClock() {
        return logicalClock;
    }

    public void setLogicalClock(Integer logicalClock) {
        this.logicalClock = logicalClock;
    }

    public class CommandParser extends Thread{

        Client current;

        public CommandParser(Client current){
            this.current = current;
        }

        Pattern SETUP = Pattern.compile("^SETUP$");
        Pattern START = Pattern.compile("^START$");
        Pattern CLOSE = Pattern.compile("^CLOSE$");
        Pattern REQUEST = Pattern.compile("^REQUEST$");
        int rx_cmd(Scanner cmd){
            String cmd_in = null;
            if (cmd.hasNext())
                cmd_in = cmd.nextLine();
            Matcher m_SETUP = SETUP.matcher(cmd_in);
            Matcher m_START = START.matcher(cmd_in);
            Matcher m_CLOSE = CLOSE.matcher(cmd_in);
            Matcher m_REQUEST = REQUEST.matcher(cmd_in);

            if(m_SETUP.find()){
                setupConnections(current);
            }

            else if(m_START.find()){
                sendP();

            }

            else if(m_REQUEST.find()){
                sendRequest();
            }

            else if(m_CLOSE.find()){
                System.out.println("Number of socket connection");
                System.out.println(socketConnectionList.size());
                Integer i = 0;
                for(i = 0; i < socketConnectionList.size(); i++){
                    System.out.println("IP: " + socketConnectionList.get(i).getOtherClient().getInetAddress() + "Port: " + socketConnectionList.get(i).getOtherClient().getPort() + "ID: " + socketConnectionList.get(i).getRemote_id());
                }

                for (String key: socketConnectionHashMap.keySet()){
                    System.out.println("ClientID: " + key + "Socket: " + socketConnectionHashMap.get(key).getOtherClient().getPort());
                }
            }
            return 1;
        }

        public void run() {
            System.out.println("Enter commands to set-up MESH Connection : START");
            Scanner input = new Scanner(System.in);
            while(rx_cmd(input) != 0) { }
        }
    }


    public void setupConnections(Client current){
        try {
            System.out.println("START THE CONNECTION TO OTHER CLIENTS");
            Integer clientId;
            for(clientId = Integer.valueOf(this.Id) + 1; clientId < allClientNodes.size(); clientId ++ ) {
                Socket clientConnection = new Socket("10.122.168.54", Integer.valueOf(allClientNodes.get(clientId).getPort()));
                SocketConnection socketConnection = new SocketConnection(clientConnection, this.getId(), true,current);
                if(socketConnection.getRemote_id() == null){
                    socketConnection.setRemote_id(Integer.toString(clientId));
                }
                socketConnectionList.add(socketConnection);
                socketConnectionHashMap.put(socketConnection.getRemote_id(),socketConnection);
            }
        }
        catch (Exception e){

        }
    }


    public void sendP(){
        System.out.println("Sending P");
        Integer i;
        for (i=0; i < this.socketConnectionList.size(); i++){
            socketConnectionList.get(i).publish();
        }
    }


    public void processRequest(String RequestingClientId, Integer RequestingClientLogicalClock){
        System.out.println("Inside Process Request for request Client: " + RequestingClientId + " which had logical clock value of: "+ RequestingClientLogicalClock);
    }


    public void processReply(String ReplyingClientId){
        System.out.println("Inside Process Reply for replying Client:  "+ ReplyingClientId);
        this.outStandingReplyCount = this.outStandingReplyCount -1;
        if(this.outStandingReplyCount == 0 ){
            enterCriticalSection();
        }
        this.requestingCS = false;
    }

    public void sendRequest(){
        this.requestingCS = true;
        this.outStandingReplyCount = 3;
        System.out.println("Sending Request");
        Integer i;
        for (i=0; i < this.socketConnectionList.size(); i++){
            socketConnectionList.get(i).request(logicalClock);
        }
    }

    public void enterCriticalSection(){
        System.out.println("Entering critical section READ/WRITE TO SEVER");
        try {
            System.out.println("System currently mocks CS execution by sleep - Started");
            TimeUnit.SECONDS.sleep(10);
            System.out.println("System currently mocks CS execution by sleep - Completed");
        }
        catch (Exception e){

        }
    }


    public void clientSocket(Integer ClientId, Client current){
        try
        {
            server = new ServerSocket(Integer.valueOf(this.allClientNodes.get(ClientId).port));
            Id = Integer.toString(ClientId);
            System.out.println("Client node running on port " + Integer.valueOf(this.allClientNodes.get(ClientId).port) +"," + " use ctrl-C to end");
            InetAddress myip = InetAddress.getLocalHost();
            String ip = myip.getHostAddress();
            String hostname = myip.getHostName();
            System.out.println("Your current IP address : " + ip);
            System.out.println("Your current Hostname : " + hostname);
        }
        catch (IOException e)
        {
            System.out.println("Error creating socket");
            System.exit(-1);
        }

        CommandParser cmdpsr = new CommandParser(current);
        cmdpsr.start();

        Thread current_node = new Thread() {
            public void run(){
                while(true){
                    try{
                        Socket s = server.accept();
                        SocketConnection socketConnection = new SocketConnection(s,Id,false, current);
                        socketConnectionList.add(socketConnection);
                        socketConnectionHashMap.put(socketConnection.getRemote_id(),socketConnection);
                    }
                    catch(IOException e){ e.printStackTrace(); }
                }
            }
        };

        current_node.setDaemon(true);
        current_node.start();
    }



    public void setClientList(){
        try {
            BufferedReader br = new BufferedReader(new FileReader("ClientAddressAndPorts.txt"));
            try {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    List<String> parsed_client = Arrays.asList(line.split(","));
                    Node n_client= new Node(parsed_client.get(0),parsed_client.get(1),parsed_client.get(2));
                    this.getAllClientNodes().add(n_client);
                    sb.append(System.lineSeparator());
                    line = br.readLine();
                }
                String everything = sb.toString();
                System.out.println(everything);
                System.out.println(this.getAllClientNodes().size());

            } finally {
                br.close();
            }
        }
        catch (Exception e) {
        }
    }


    public void setServerList(){
        try {
            BufferedReader br = new BufferedReader(new FileReader("ServerAddressAndPorts.txt"));
            try {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    List<String> parsed_server = Arrays.asList(line.split(","));
                    Node n_server = new Node(parsed_server.get(0),parsed_server.get(1),parsed_server.get(2));
                    this.getAllServerNodes().add(n_server);
                    sb.append(System.lineSeparator());
                    line = br.readLine();
                }
                String everything = sb.toString();
                System.out.println(everything);
                System.out.println(this.getAllServerNodes().size());

            } finally {
                br.close();
            }
        }
        catch (Exception e) {
        }

    }



    public static void main(String[] args) {

        if (args.length != 1)
        {
            System.out.println("Usage: java Client <port-number>");
            System.exit(1);
        }

        Client C1 = new Client(args[0]);
        C1.setClientList();
        C1.setServerList();
        C1.clientSocket(Integer.valueOf(args[0]),C1);

        System.out.println("Starting Client with ID: " + C1.getId());
    }
}
