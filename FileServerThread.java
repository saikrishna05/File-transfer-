import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.Socket;
import java.security.Key;
import java.util.Arrays;

import static sun.security.x509.CertificateAlgorithmId.ALGORITHM;

public class FileServerThread implements Runnable {

    private Socket clientSocket;
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";
    private String byzantineFailureFlag;


    FileServerThread(Socket client, String byzantineFailureFlag) {
        this.clientSocket = client;
        // reads flag for byzantine failure
        this.byzantineFailureFlag = byzantineFailureFlag;
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    clientSocket.getInputStream()));
            // request sent by the client
            String clientSelection;
            while ((clientSelection = in.readLine()) != null) {
                // request = 1 then login, if request = 2 sendfile
                if(clientSelection.equalsIgnoreCase("1")){
                    login();
                    break;
                }else if(clientSelection.equalsIgnoreCase("2")){
                    String outGoingFileName;
                    while ((outGoingFileName = in.readLine()) != null) {
                        sendFile(outGoingFileName);
                    }
                    break;
                }else{
                    System.out.println("Incorrect command received.");
                }
                in.close();
                break;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void login(){
        try{

            DataInputStream clientData = new DataInputStream(clientSocket.getInputStream());
            // reads credentials sent from client, splits by ":", index 0 is username, index 1 is password
            String[] credentails = clientData.readUTF().split(":");

            // validates credentials
            if(credentails[0].equalsIgnoreCase("saikrishna")){
                if(credentails[1].equalsIgnoreCase("saikrishna")){
                    OutputStream os = clientSocket.getOutputStream();
                    DataOutputStream dos = new DataOutputStream(os);
                    // sends successful flag to client
                    dos.writeUTF("SUCCESSFUL");
                    dos.flush();
                }else{
                    OutputStream os = clientSocket.getOutputStream();
                    DataOutputStream dos = new DataOutputStream(os);
                    // sends login failed flag to client
                    dos.writeUTF("Login Failed");
                    dos.flush();
                }
            }else{
                OutputStream os = clientSocket.getOutputStream();
                DataOutputStream dos = new DataOutputStream(os);
                dos.writeUTF("Login Failed");
                dos.flush();
            }

        }catch (IOException ex){
            ex.printStackTrace();
        }

    }

    private void sendFile(String fileName) throws IOException {
        try {
            //handle file read
            File myFile = new File(fileName);
            byte[] mybytearray = new byte[(int) myFile.length()];

            FileInputStream fis = new FileInputStream(myFile);
            BufferedInputStream bis = new BufferedInputStream(fis);

            DataInputStream dis = new DataInputStream(bis);
            dis.readFully(mybytearray, 0, mybytearray.length);

            //handle file send over socket
            OutputStream os = clientSocket.getOutputStream();

            //Sending file name and file size to the server
            DataOutputStream dos = new DataOutputStream(os);
            dos.writeUTF(myFile.getName());
            dos.writeLong(mybytearray.length);

            // encrypts data before writnig to file
            String key = "1234567891234567";
            Key secretKey = new SecretKeySpec(key.getBytes("UTF-8"), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedData = cipher.doFinal(mybytearray);

            // if byzantineFailureFlag is "y" then writes this is a byzantine behavior other than writing file contents
            // hence file size won't be the same when file is downloaded on the client
            if(byzantineFailureFlag.equalsIgnoreCase("Y")){
                mybytearray = "this is a byzantine behavior".getBytes("UTF-8");
                dos.writeLong(mybytearray.length);
                dos.write(mybytearray, 0, mybytearray.length);
                dos.flush();

            // else byzantineFailureFlag is not "Y" then writes file data normally
            }else{
                dos.writeLong(mybytearray.length);
                dos.write(mybytearray, 0, mybytearray.length);
                dos.flush();
                System.out.println("File "+fileName+" sent to client.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            OutputStream os = clientSocket.getOutputStream();
            DataOutputStream dos = new DataOutputStream(os);
            dos.writeUTF("NOT_FOUND");
            dos.flush();
            System.err.println("File does not exist!");
        }
    }
}