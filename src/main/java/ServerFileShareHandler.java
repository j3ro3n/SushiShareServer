import java.io.*;
import java.net.Socket;

public class ServerFileShareHandler implements Runnable {

    private Socket clientSocket;
    private BufferedReader in = null;

    public ServerFileShareHandler(Socket client) {
        this.clientSocket = client;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(
                    clientSocket.getInputStream()));
            String clientSelection;
            while ((clientSelection = in.readLine()) != null) {
                switch (clientSelection) {
                    case "1":
                        pullFile();
                        break;
                    case "2":
                        String outGoingFileName;
                        while ((outGoingFileName = in.readLine()) != null) {
                            pushFile(outGoingFileName);
                        }
                        break;
                    case "3":
                        String removeFileName;
                        while ((removeFileName = in.readLine()) != null) {
                            removeFile(removeFileName);
                        }
                        break;
                    case"4":
                        String syncFileName;
                        while ((syncFileName = in.readLine()) != null) {
                            syncFile(syncFileName);
                        }
                        break;
                    case "5":
                        System.exit(1);

                        break;
                    default:
                        System.out.println("Incorrect command received.");
                        break;
                }

            }

        } catch (IOException ex) {

        }
    }

    public void pullFile() {
        try {
            int bytesRead;

            DataInputStream clientData = new DataInputStream(clientSocket.getInputStream());

            String fileName = clientData.readUTF();
            OutputStream output = new FileOutputStream(fileName);
            long size = clientData.readLong();
            byte[] buffer = new byte[1024];
            while (size > 0 && (bytesRead = clientData.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
                output.write(buffer, 0, bytesRead);
                size -= bytesRead;
            }

            output.close();
            clientData.close();

            System.out.println("File "+fileName+" received from client.");
        } catch (IOException ex) {
            System.err.println("Client error. Connection closed.");
        }
    }

    public void pushFile(String fileName) {
        try {

            File myFile = new File(fileName);  //handle file reading
            byte[] mybytearray = new byte[(int) myFile.length()];

            FileInputStream fis = new FileInputStream(myFile);
            BufferedInputStream bis = new BufferedInputStream(fis);

            DataInputStream dis = new DataInputStream(bis);
            dis.readFully(mybytearray, 0, mybytearray.length);


            OutputStream os = clientSocket.getOutputStream();  //handle file send over socket

            DataOutputStream dos = new DataOutputStream(os); //Sending file name and file size to the server
            dos.writeUTF(myFile.getName());
            dos.writeLong(mybytearray.length);
            dos.write(mybytearray, 0, mybytearray.length);
            dos.flush();
            System.out.println("File "+fileName+" sent to client.");
        } catch (Exception e) {
            System.err.println("File does not exist!");
        }
    }
    public void removeFile(String fileName) {
        try {
            File myFile = new File(fileName);
            if(myFile.delete()){
                System.out.println(myFile.getName() + " is removed!");
            }else{
                System.out.println("Failed to delete "+fileName);
            }
        }catch(Exception e){
            //e.printStackTrace();
            System.out.println("File does not exist!");
        }
    }

    // Synchronise files to and from server
    public void syncFile(String fileName){
        FileInputStream fin;
        FileOutputStream fout;
        // Initializing a FileDescriptor
        FileDescriptor fd;
        File file = new File(fileName);
        try {
            fout= new FileOutputStream(file);
            // This getFD() method is called before closing the output stream
            fd= fout.getFD();
            //passing FileDescriptor to another  FileOutputStream
            FileOutputStream fout2= new FileOutputStream(fd);
            //Hier kan fout gaan
            //fout2.write("Hoi Sunny".getBytes());
            fout2.write(fileName.getBytes());
            // Use of sync() : to sync data to the source file
            fd.sync();
            System.out.println("Sync Successful");
            fin = new FileInputStream(file);
            fd=fin.getFD();
            System.out.print("String value has been changed in file -----> ");
            int i=0;
            while((i=fin.read())!=-1)
            {
                System.out.print(i);
            }
            fout2.close();
        }

        catch(Exception e)
        {
            System.out.println(e);
        }
    }


}
