import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.Socket;
import java.security.Key;

public class FileClient {

    private static Socket socket;
    private static String fileName;
    private static BufferedReader stdin;
    private static PrintStream printStream;
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";
    private static int retryCount = 0;

    public static void main(String[] args) throws IOException {
        try {
            // connects to server on socket 2018 at given ip
            socket = new Socket("10.234.136.55", 2018);
            stdin = new BufferedReader(new InputStreamReader(System.in));
        } catch (Exception e) {
            System.err.println("Cannot connect to the server, try again later.");
            System.exit(1);
        }

        printStream = new PrintStream(socket.getOutputStream());

        try {
            // gets user choice
            if(Integer.parseInt(userMenu()) == 1){
                printStream.println("1");
                login();
            }else{
                System.exit(1);
            }
        } catch (Exception e) {
            System.err.println("not valid input");
        }

        // closes socket
        socket.close();
    }

    // user menu
    private static String userMenu() throws IOException {
        System.out.println("1. Login ");
        System.out.println("2. Exit");
        System.out.print("\n Make selection: ");
        return stdin.readLine();
    }

    //login
    private static void login(){
        try {
            InputStream in = socket.getInputStream();
            DataInputStream clientData = new DataInputStream(in);

            System.err.print("username: ");
            String username = stdin.readLine();

            System.err.print("password: ");
            String password = stdin.readLine();

            OutputStream outputStream = socket.getOutputStream();
            DataOutputStream dos = new DataOutputStream(outputStream);
            dos.writeUTF(username+ ":" + password);
            dos.flush();

            // reads login status from server
            String loginStatus = clientData.readUTF();
            // if successful tells server to it wants to download a file, trigger send file function on server
            if(loginStatus.equalsIgnoreCase("SUCCESSFUL")){
                System.out.println("Login successful \n");
                socket.close();
                socket = new Socket("10.234.136.55", 2018);
                printStream = new PrintStream(socket.getOutputStream());
                //tells server to call sendfile function
                printStream.println("2");
                // reads file name from user
                System.err.print("Enter file name: \n");
                fileName = stdin.readLine();
                // sends file name to server
                printStream.println(fileName);
                // calls receive file to download the file
                receiveFile();
            }else{
                System.out.println("Login Failed, reconnect");
                socket.close();
            }

        }catch (IOException ex){
            ex.printStackTrace();
        }

    }


    private static void receiveFile() {
        try {
            int bytesRead;
            // to store input stream received from the server
            InputStream in = socket.getInputStream();

            DataInputStream clientData = new DataInputStream(in);
            fileName = clientData.readUTF();

            // gets if the stream send is "NOT_FOUND"
            // if yes prints an error and exits
            if(fileName.equalsIgnoreCase("NOT_FOUND")){
                System.out.println("File not found on server");
                socket.close();
                System.exit(1);
            }else{
                FileOutputStream output = new FileOutputStream(("received_from_server_" + fileName));

                // stores all the sizes sent by server
                long actualSize = clientData.readLong();
                long sizeReceived = clientData.readLong();
                long size = sizeReceived;

                byte[] buffer = new byte[2048];

                // key for encryption
                String key = "1234567891234567";

                // decrypts stream before writing to file
                Key secretKey = new SecretKeySpec(key.getBytes("UTF-8"), ALGORITHM);
                Cipher cipher = Cipher.getInstance(TRANSFORMATION);
                cipher.init(Cipher.DECRYPT_MODE, secretKey);
                int i =0;
                while (size > 0 && (bytesRead = clientData.read(buffer, 0, (int)  size)) != -1) {
                    output.write(buffer, 0, bytesRead);
                    size -= bytesRead;
                }

                System.out.println("File "+fileName+" received from Server.");


                // if file size is not equal then download failed retries again
                if((long)actualSize != (long)sizeReceived){
                    retryCount++;
                    // if retried 5 ties close the connection to server
                    if(retryCount == 6){
                        System.out.println("5 retries finished. closing connection to server. Please contact server admin");
                        socket.close();
                        output.close();
                        in.close();
                        System.exit(1);
                    }else{
                        System.out.println("Error downloading the file, file size does not match up. retrying...");
                        socket.close();
                        socket = new Socket("10.234.136.55", 2018);
                        printStream = new PrintStream(socket.getOutputStream());
                        printStream.println("2");
                        printStream.println(fileName);
                        receiveFile();
                    }
                }else{
                    System.out.println("File successfully downloaded");
                }

                output.close();
                in.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}