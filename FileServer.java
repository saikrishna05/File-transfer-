import java.net.ServerSocket;
import java.net.Socket;

public class FileServer {

    private static ServerSocket serverSocket;

    public static void main(String[] args){

        try {
            //server runs on port 2018
            serverSocket = new ServerSocket(2018);
            System.out.println("Server started.");
        } catch (Exception e) {
            System.err.println("Port already in use.");
            System.exit(1);
        }

        while (true) {
            try {
                Socket socket = serverSocket.accept();
                System.out.println("Incoming connection from : " + socket);

                Thread t = new Thread(new FileServerThread(socket, "Y"));
                t.start();

            } catch (Exception e) {
                System.err.println("Error in connection attempt.");
            }
        }
    }
}