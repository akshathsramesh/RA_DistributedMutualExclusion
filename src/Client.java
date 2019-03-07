import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
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
    HashMap<String,Boolean> clientPermissionRequired = new HashMap<>();
    Integer highestLogicalClockValue = 0;
    Integer outStandingReplyCount = 0;
    Boolean requestedCS = false;
    Boolean usingCS = false;
    List<String> deferredReplyList = new LinkedList<>();
    String RequestedFile;
    Integer minimumDelay = 5000;

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
        Pattern CONNECTION_DETAIL = Pattern.compile("^CONNECTION_DETAIL$");
        Pattern REQUEST = Pattern.compile("^REQUEST$");
        Pattern AUTO_REQUEST = Pattern.compile("^AUTO_REQUEST$");
        int rx_cmd(Scanner cmd){
            String cmd_in = null;
            if (cmd.hasNext())
                cmd_in = cmd.nextLine();
            Matcher m_SETUP = SETUP.matcher(cmd_in);
            Matcher m_START = START.matcher(cmd_in);
            Matcher m_CONNECTION_DETAIL = CONNECTION_DETAIL.matcher(cmd_in);
            Matcher m_REQUEST = REQUEST.matcher(cmd_in);
            Matcher m_AUTO_REQUEST = AUTO_REQUEST.matcher(cmd_in);
            if(m_SETUP.find()){
                setupConnections(current);
            }

            else if(m_START.find()){
                sendP();

            }

            else if(m_REQUEST.find()){
                sendRequest();
            }

            else if(m_CONNECTION_DETAIL.find()){
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

            else if(m_AUTO_REQUEST.find()){
                sendAutoRequest();
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
            System.out.println("CONNECTING CLIENTS");
            Integer clientId;
            for(clientId = Integer.valueOf(this.Id) + 1; clientId < allClientNodes.size(); clientId ++ ) {
                Socket clientConnection = new Socket("10.122.168.54", Integer.valueOf(allClientNodes.get(clientId).getPort()));
                SocketConnection socketConnection = new SocketConnection(clientConnection, this.getId(), true,current);
                if(socketConnection.getRemote_id() == null){
                    socketConnection.setRemote_id(Integer.toString(clientId));
                }
                socketConnectionList.add(socketConnection);
                socketConnectionHashMap.put(socketConnection.getRemote_id(),socketConnection);
                clientPermissionRequired.put(socketConnection.getRemote_id(),true);
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

    public void sendAutoRequest(){

        Thread sendAuto = new Thread(){
            public void run(){
                try {
                    while(true) {
                        System.out.println("Auto - Generating request");
                        sendRequest();
                        double randFraction = Math.random() * 1000;
                        Integer delay = (int) Math.floor(randFraction) + minimumDelay;
                        System.out.println("The AUTO REQUEST THREAD thread will sleep for " + delay +" seconds");
                        Thread.sleep(delay);
                    }
                }
                catch (Exception e){}
            }
        };
        sendAuto.setDaemon(true); 	// terminate when main ends
        sendAuto.start();
        }



    public synchronized void processRequest(String RequestingClientId, Integer RequestingClientLogicalClock){
        System.out.println("Inside Process Request for request Client: " + RequestingClientId + " which had logical clock value of: "+ RequestingClientLogicalClock);
        this.highestLogicalClockValue = Math.max(this.highestLogicalClockValue, RequestingClientLogicalClock);
        if (this.usingCS || this.requestedCS){
            if(RequestingClientLogicalClock > this.logicalClock){
                System.out.println("USING OR REUQESTED CS");
                System.out.println("Highest Logical Clock Value: " +  this.highestLogicalClockValue);
                System.out.println("Current Logical Clock Value:" + this.logicalClock);
                System.out.println("************** SHOULD DEFER *********** CONDITION 1*****************");
            }
            else if(RequestingClientLogicalClock == this.logicalClock){
                System.out.println("USING OR REUQESTED CS");
                System.out.println("Highest Logical Clock Value: " +  this.highestLogicalClockValue);
                System.out.println("Current Logical Clock Value:" + this.logicalClock);
                System.out.println("************** SHOULD DEFER *********** CONDITION 2*****************");
            }

        }
        if(( (this.usingCS || this.requestedCS) && (RequestingClientLogicalClock > this.logicalClock)) || ((this.usingCS || this.requestedCS) && RequestingClientLogicalClock == this.logicalClock && Integer.valueOf(RequestingClientId) > Integer.valueOf(this.getId()))){
            System.out.println("____________________________________________________________________________________________________");
            System.out.println("Deferred Reply for request Client: " + RequestingClientId + " which had logical clock value of: "+ RequestingClientLogicalClock);
            System.out.println("Critical Section Access from this node had CLIENT ID" + this.getId() +"and last updated logical clock is: " + this.logicalClock );
            System.out.println("_____________________________________________________________________________________________________");
            this.clientPermissionRequired.replace(RequestingClientId,true);
            this.deferredReplyList.add(RequestingClientId);
        }
        else {
            System.out.println("Initiating SEND REPLY without block");
            this.clientPermissionRequired.replace(RequestingClientId,true);
            SocketConnection requestingSocketConnection = socketConnectionHashMap.get(RequestingClientId);
            requestingSocketConnection.reply();
        }

    }

    public synchronized void processReply(String ReplyingClientId){
        System.out.println("Inside Process Reply for replying Client:  "+ ReplyingClientId);
        this.clientPermissionRequired.replace(ReplyingClientId,false);
        this.outStandingReplyCount = this.outStandingReplyCount -1;
        if(this.outStandingReplyCount == 0 ){
            enterCriticalSection();
            releaseCSCleanUp();
        }
    }

    public synchronized void sendRequest(){
        if(!(this.requestedCS || this.usingCS)) {
            this.requestedCS = true;
            this.logicalClock = this.highestLogicalClockValue + 1;
            System.out.println("Sending Request with logical clock: " + this.logicalClock);
            Integer i;
            for (i = 0; i < this.socketConnectionList.size(); i++) {
                if (clientPermissionRequired.get(socketConnectionList.get(i).getRemote_id()) == true) {
                    this.outStandingReplyCount = this.outStandingReplyCount + 1;
                    socketConnectionList.get(i).request(logicalClock);
                }
            }

            if(this.outStandingReplyCount == 0){
                enterCriticalSection();
                releaseCSCleanUp();
            }
        }
        else{
            System.out.println("Currently in CS or already requested for CS");
        }
    }

    public void enterCriticalSection(){
        System.out.println("Entering critical section READ/WRITE TO SERVER");
        this.usingCS = true;
        this.requestedCS = false;
        try {
            System.out.println("Writing Client Id of requesting node to file and logical clock in critical section");
            Server server = new Server();
            server.writeToFile("asr150330_1.txt", new Message(this.Id,Integer.toString(this.logicalClock)));
            TimeUnit.SECONDS.sleep(10);
            System.out.println("Exciting Critical Section");
        }
        catch (Exception e){

        }
    }

    public void releaseCSCleanUp(){
        System.out.println("Entering Clean UP");
        this.usingCS = false;
        this.requestedCS = false;
        Iterator<String> deferredReplyClientId = deferredReplyList.iterator();
        while(deferredReplyClientId.hasNext()){
            socketConnectionHashMap.get(deferredReplyClientId.next()).reply();
        }
        deferredReplyList.clear();
        System.out.println("Exiting Clean UP");
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
                        clientPermissionRequired.put(socketConnection.getRemote_id(),true);
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
