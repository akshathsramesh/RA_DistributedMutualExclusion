import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketConnection {


    Socket otherClient;
    String my_id;
    String remote_id;
    BufferedReader in;
    PrintWriter out;
    Boolean Initiator;
    Client my_master;

    public String getRemote_id() {
        return remote_id;
    }

    public void setRemote_id(String remote_id) {
        this.remote_id = remote_id;
    }

    public SocketConnection(Socket otherClient, String myId, Boolean Initiator, Client my_master) {
        this.otherClient = otherClient;
        this.my_id = myId;
        this.my_master = my_master;
        try{
           in = new BufferedReader(new InputStreamReader(this.otherClient.getInputStream()));
           out = new PrintWriter(this.otherClient.getOutputStream(), true);
        }
        catch (Exception e){

        }

        try {
            if(!Initiator) {
                out.println("SEND_ID");
                System.out.println("SEND_ID request sent");
                remote_id = in.readLine();
                System.out.println("SEND_ID request response received with ID: " + remote_id);
            }
        }

        catch (Exception e){

        }
        Thread read = new Thread(){
            public void run(){
                while(rx_cmd(in,out) != 0) { }
            }
        };
        read.setDaemon(true); 	// terminate when main ends
        read.start();
    }


    public int rx_cmd(BufferedReader cmd,PrintWriter out) {
        try {
            String cmd_in = cmd.readLine();
            if(cmd_in.equals("P")){
                System.out.println("P recieved from sender");
            }

            else if(cmd_in.equals("SEND_ID")){
                out.println(this.my_id);
            }
            else if(cmd_in.equals("REQ")){
                System.out.println("Received Request and Hence calling sendP on all available connection");
                my_master.sendP();
            }
        }
        catch (Exception e){}
        return 1;
    }


    public void publish() {
        out.println("P");
    }


    public void request(){
        System.out.println("SENDING REQ FROM CLIENT FROM MACHINE WITH CLIENT ID" + this.my_id);
        out.println("REQ");
    }

    public void release(){
        System.out.println("SENDING REQ FROM CLIENT FROM MACHINE WITH CLIENT ID" + this.my_id);
        out.println("REL");
    }

    public Socket getOtherClient() {
        return otherClient;
    }

    public void setOtherClient(Socket otherClient) {
        this.otherClient = otherClient;
    }
}


