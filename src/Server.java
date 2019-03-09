import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Server {

    List<Node> allServerNodes = new LinkedList<>();
    List<ServerSocketConnection> serverSocketConnectionList = new LinkedList<>();
    ServerSocket server;
    String Id;
    HashMap<String,ServerSocketConnection> serverSocketConnectionHashMap = new HashMap<>();

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public List<Node> getAllServerNodes() {
        return allServerNodes;
    }

    public void setAllServerNodes(List<Node> allServerNodes) {
        this.allServerNodes = allServerNodes;
    }

    public class CommandParser extends Thread{

        Server currentServer;

        public CommandParser(Server currentServer){
            this.currentServer = currentServer;
        }

        Pattern START = Pattern.compile("^START$");

        int rx_cmd(Scanner cmd){
            String cmd_in = null;
            if (cmd.hasNext())
                cmd_in = cmd.nextLine();
            Matcher m_START = START.matcher(cmd_in);

            if(m_START.find()){
                System.out.println("Socket connection test function");
//                sendP();

            }

            return 1;
        }

        public void run() {
            System.out.println("Enter commands to set-up MESH Connection : START");
            Scanner input = new Scanner(System.in);
            while(rx_cmd(input) != 0) { }
        }
    }


    public void writeToFile( String fileName, Message message) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
        writer.append(message.getClientId()+","+message.getTimeStamp()+"\n");
        writer.close();
    }



    public Message readLastFile(String fileName) {
        String sCurrentLine;
        String lastLine= "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            while ((sCurrentLine = br.readLine()) != null)
            {
                System.out.println(sCurrentLine);
                lastLine = sCurrentLine;
            }

        }
        catch (Exception e){

        }
        List<String> message = Arrays.asList(lastLine.split(","));
        System.out.println(message.get(1)+"Value of last line");
        Message returnMessage = new Message(message.get(0),message.get(1));
        return returnMessage;
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

    public void serverSocket(Integer serverId, Server currentServer){
        try
        {
            server = new ServerSocket(Integer.valueOf(this.allServerNodes.get(serverId).port));
            Id = Integer.toString(serverId);
            System.out.println("Server node running on port " + Integer.valueOf(this.allServerNodes.get(serverId).port) +"," + " use ctrl-C to end");
            InetAddress myServerIp = InetAddress.getLocalHost();
            String ip = myServerIp.getHostAddress();
            String hostname = myServerIp.getHostName();
            System.out.println("Your current Server IP address : " + ip);
            System.out.println("Your current Server Hostname : " + hostname);
        }
        catch (IOException e)
        {
            System.out.println("Error creating socket");
            System.exit(-1);
        }

        Server.CommandParser cmdpsr = new Server.CommandParser(currentServer);
        cmdpsr.start();

        Thread current_node = new Thread() {
            public void run(){
                while(true){
                    try{
                        Socket s = server.accept();
                        ServerSocketConnection serverSocketConnection = new ServerSocketConnection(s,Id, false,currentServer);
                        serverSocketConnectionList.add(serverSocketConnection);
                        serverSocketConnectionHashMap.put(serverSocketConnection.getRemote_id(),serverSocketConnection);
                    }
                    catch(IOException e){ e.printStackTrace(); }
                }
            }
        };

        current_node.setDaemon(true);
        current_node.start();
    }



    public static void main(String[] args) {


        if (args.length != 1) {
            System.out.println("Usage: java Server <server-number>");
            System.exit(1);
        }

        System.out.println("Starting the server");

        Server server = new Server();
        server.setServerList();
        server.serverSocket(Integer.valueOf(args[0]),server);

        System.out.println("Started Client with ID: " + server.getId());

    }
}

