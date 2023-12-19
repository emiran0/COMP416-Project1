import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;


public class MultithreadClient {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        BufferedReader br= new BufferedReader(new InputStreamReader(System.in));
        int port;
        System.out.println("Enter the port for the server:");
        String strport= br.readLine();
        port = Integer.parseInt(strport);
        ConnectionToServer connectionToServer = new ConnectionToServer(ConnectionToServer.DEFAULT_SERVER_ADDRESS, port);
        connectionToServer.Connect();
        System.out.println("\n*************** WELCOME TO NFTNET ***************\n");
        Scanner scanner = new Scanner(System.in);
        connectionToServer.menuMessage();
        String message = scanner.nextLine();
        String out = null;
        connectionToServer.SendForAnswer(message);
        while (!message.equals("QUIT"))
        {
            out = connectionToServer.InputMessage();

                if (out != null) {
                    if (out.compareTo("timeout")==0) {
                        System.out.println("Session timed out! Please connect again!");
                        break;
                    }else{
                        System.out.println(out);
                        while (!out.equals("done")) {

                            out = connectionToServer.InputMessage();
                            if(!out.equals("done")) {
                                System.out.println(out);
                            }
                        }
                        connectionToServer.menuMessage();
                        message = scanner.nextLine();
                        connectionToServer.SendForAnswer(message);
                    }
                }

        }
        connectionToServer.Disconnect();
        }

    }

