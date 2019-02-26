import java.io.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Server {

    List<String> hostedFiles = new LinkedList<>();
    List<String> filesInWrite = new LinkedList<>();
    List<String> filesInRead = new LinkedList<>();
    Queue<WriteRequest> writeRequestQueue = new LinkedList<>();


    public List<String> getHostedFiles() {
        return hostedFiles;
    }

    public void setHostedFiles(List<String> hostedFiles) {
        this.hostedFiles = hostedFiles;
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



    public void setFiles(){
        try {
            BufferedReader br = new BufferedReader(new FileReader("hostedFiles.txt"));
            try {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    this.hostedFiles.add(line);
                    sb.append(System.lineSeparator());
                    line = br.readLine();
                }
                String everything = sb.toString();
                System.out.println(everything);
                System.out.println(this.getHostedFiles().size());

            } finally {
                br.close();
            }
        }
        catch (Exception e) {
        }
    }



    public static void main(String[] args) {
        System.out.println("Starting the server");
        Server s = new Server();
        s.setFiles();
        System.out.println(s.getHostedFiles());
        Message m1 = new Message("1","120");
        Message m2 = new Message("1","122");
        Message m3 = new Message("1","123");
        try {
            s.writeToFile("asr150330_1.txt", m1);
            s.writeToFile("asr150330_1.txt", m2);
            s.writeToFile("asr150330_1.txt", m3);
            Message ret = s.readLastFile("asr150330_1.txt");
        }
        catch (Exception e){}
        }

    }

