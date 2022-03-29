import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Properties;

public class MultiThread {
    public static void main(String[] args) throws Exception {
        try {
            // initialize variables
            int port;
            String ip;
            String hostName;
            String DocumentRoot = "";
            
            Properties prop = new Properties();
            String configFile = "/home/faridlamaul/Project/Kuliah/Progjar/multi-thread-web-server/src/2022-progjarc.conf";
            
            try {
                FileInputStream in = new FileInputStream(configFile);
                prop.load(in);
            } catch (FileNotFoundException e) {
                System.out.println("File not found");
            }
        
            ip = prop.getProperty("ip");
            System.out.println("IP : " + ip);
            port = Integer.parseInt(prop.getProperty("port"));
            
            // directory root 
            // String DocumentRoot = "/home/faridlamaul/Project/Kuliah/Progjar/single-thread-web-server/DocumentRoot/";
            
            

            InetAddress addr = InetAddress.getByName(ip);
            System.out.println(addr);
            
            // create server socket
            ServerSocket server = new ServerSocket(port, 0, addr);
            // ServerSocket server = new ServerSocket(port);
            while(true) {
                
                System.out.println("******* Server started in port " + port + " *******");
                
                // listen for client
                Socket client = server.accept();

                // get hostname from request client
                hostName = client.getInetAddress().getHostName();
                System.out.println("Hostname : " + hostName);

                if (hostName.equals(prop.getProperty("servername1"))) {
                    DocumentRoot = new String(prop.getProperty("documentroot1"));
                    System.out.println("DocumentRoot : " + DocumentRoot);
                } else if (hostName.equals(prop.getProperty("servername2"))) {
                    DocumentRoot = new String(prop.getProperty("documentroot2"));
                    System.out.println("DocumentRoot : " + DocumentRoot);
                }

                System.out.println("*******      Client connected       *******");
                
                ClientThread clientThread = new ClientThread(client, DocumentRoot);
                clientThread.start();
                
            }
            // server.close();
        } catch (Exception e) {
            Logger.getLogger(MultiThread.class.getName()).log(Level.SEVERE, null, e);
        }
    }
}

