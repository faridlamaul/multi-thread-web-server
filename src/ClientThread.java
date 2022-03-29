import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;

public class ClientThread extends Thread {
    private Socket client;
    private String DocumentRoot = "/home/faridlamaul/Project/Kuliah/Progjar/multi-thread-web-server/DocumentRoot/";

    public ClientThread(Socket client, String DocumentRoot) {
        this.client = client;
        this.DocumentRoot = DocumentRoot;
    }

    public void run() {
        try  {
            BufferedReader reader = new BufferedReader(new InputStreamReader(this.client.getInputStream()));
            BufferedWriter output = new BufferedWriter(new OutputStreamWriter(this.client.getOutputStream()));
            
            // read request
            String message = reader.readLine();
            System.out.println("Request : " + message);
            String urn = message.split(" ")[1];
            urn = urn.substring(1);

            // get file name ex. file.pdf or file.html
            String fileName = urn.substring(urn.lastIndexOf("/") + 1);
            
            // get content type ex. text/html
            Path path = new File(fileName).toPath();
            String mimeType = Files.probeContentType(path);
            String contentType = mimeType;

            // get path of file
            String directoryPath = this.DocumentRoot + urn;
            
            // get path of current directory
            String currDirectoryPath = directoryPath.substring(directoryPath.lastIndexOf("/") + 1);

            FileInputStream fis;
            // byte[] fileContent12;
            String fileContent;
            String statusCode;
            String crlf = "\r\n";

            // request is a directory or file
            if (urn.contains(".")) {
                // check if file is html or not 
                if(contentType.equals("text/html")) {
                    try {
                        fis = new FileInputStream(directoryPath);
                        fileContent = new String(fis.readAllBytes());
                        
                        statusCode = "200 OK";

                    } catch (FileNotFoundException e) {
                        fileContent = "File not found";
                        statusCode = "404 Not Found";
                    }
   
                    output.write("HTTP/1.1 " + statusCode + crlf);
                    output.write("Content-Type: " + contentType + crlf);
                    output.write("Content-Length: " + fileContent.length() + crlf + crlf);
                    output.write(fileContent);
                    output.flush();

                } else {
                    try {
                        fis = new FileInputStream(directoryPath);
                        fileContent = new String(fis.readAllBytes());
                        
                        statusCode = "200 OK"; 

                        System.out.println("File found");
                        System.out.println("File Name: " + fileName);
                        System.out.println("Content-Type: " + contentType);
                        System.out.println("Content-Length: " + fileContent.length());
                        output.write("HTTP/1.1 " + statusCode + crlf);
                        output.write("Accept-Ranges: bytes" +  crlf);
                        output.write("Content-Type: " + contentType +  crlf);
                        output.write("Content-Length: " + fileContent.length() + crlf);
                        output.write("Content-Disposition: attachment; filename=" + "\"" + fileName + "\"" + crlf + crlf);
                        
                        output.write(fileContent);
                        output.flush();

                    } catch (FileNotFoundException e) {
                        fileContent = "File not found";
                        statusCode = "404 Not Found";
                        
                        System.out.println("File not found");
                        output.write("HTTP/1.1 " + statusCode + crlf + crlf);
                        output.write(fileContent);
                        output.flush();
                    }
                }
            } else {
                File dirPath = new File(directoryPath);
                
                //List of all files and directories
                File files[] = dirPath.listFiles();
                if (files != null) {
                    String fileContent2 = "";
                    fileContent2 += "<html>\r\n" 
                        + "<body>"
                        + "<table>"
                        + "<tr>"
                        + "<th>Nama File</th>"
                        + "<th>Last Modified</th>"
                        + "<th>Size</th>"
                        + "</tr>";

                    for (File file : files) {

                        contentType = Files.probeContentType(new File(file.getName()).toPath());
                        fileContent = file.getName();
                        Date lastModified = new Date(file.lastModified());
                        
                        if (file.getName().equals("index.html")) {
                            fis = new FileInputStream(file);
                            fileContent = new String(fis.readAllBytes());

                            statusCode = "200 OK";
                            
                            output.write("HTTP/1.1 " + statusCode + crlf);
                            output.write("Content-Type: " + contentType + crlf);
                            output.write("Content-Length: " + fileContent.length() + crlf + crlf);
                            output.write(fileContent);
                            output.flush();

                        } else {
                            fileContent2 += "<tr>";
                            fileContent2 += "<td><a href=\"" + currDirectoryPath + "/" + file.getName() + "\">" + file.getName() + "</a></td>";
                            fileContent2 += "<td>" + lastModified + "</td>";
                            fileContent2 += "<td>" + file.length() + " Bytes</td>";
                            fileContent2 += "</tr>";
                        }
                    }

                    fileContent2 += "</table>" 
                        + "</body>\r\n" 
                        + "</html>";
                    output.write("HTTP/1.1 200 OK" + crlf + crlf);
                    output.write(fileContent2);
                    output.flush();

                } else {

                    fileContent = "Directory not found";
                    statusCode = "404 Not Found";
                    
                    System.out.println("Directory not found");
                    output.write("HTTP/1.1 " + statusCode + crlf + crlf);
                    output.write(fileContent);
                    output.flush();

                }
            }

            while(message.isEmpty()) {
                message = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            this.client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
