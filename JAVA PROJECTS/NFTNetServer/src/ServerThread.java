import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.SocketTimeoutException;  //remove this if timeout ability is corrupted.

class ServerThread extends Thread
{
    private static final int DEFAULT_TIMEOUT_SET = 60000;
    protected BufferedReader is;
    protected PrintWriter os;
    protected Socket s;
    private String line = "";

    //private String lines = "";

    /**
     * Creates a server thread on the input socket
     *
     * @param s input socket to create a thread on
     */
    public ServerThread(Socket s)
    {
        this.s = s;
    }

    /**
     * The server thread, echos the client until it receives the QUIT string from the client
     */
    public void run()
    {
        try
        {
            is = new BufferedReader(new InputStreamReader(s.getInputStream()));
            os = new PrintWriter(s.getOutputStream());
            s.setSoTimeout(DEFAULT_TIMEOUT_SET);
            line = is.readLine();
            while (line.compareTo("QUIT") != 0)
            {
                System.out.println("Client " + s.getRemoteSocketAddress() + " sent :  " + line);
                s.setSoTimeout(8000);
		        ArrayList<String> outArray = getRequest(line);
                for (String data : outArray) {
                    os.println(data);
                    os.flush();
                }

                line = is.readLine();
            }
        }
        catch(SocketTimeoutException e)
        {
            String client = this.getName();
            os.println("timeout");
            os.flush();
            System.out.println("Client " + client + ", socket timed out!");
        }
        catch (IOException e)
        {
            line = this.getName(); //reused String line for getting thread name
            System.err.println("Server Thread. Run. IO Error/ Client " + line + " terminated abruptly");
        }
        catch (NullPointerException e)
        {
            line = this.getName(); //reused String line for getting thread name
            System.err.println("Server Thread. Run.Client " + line + " Closed");
        } finally
        {
            try
            {
                System.out.println("Closing the connection");
                if (is != null)
                {
                    is.close();
                    System.err.println(" Socket Input Stream Closed");
                }

                if (os != null)
                {
                    os.close();
                    System.err.println("Socket Out Closed");
                }
                if (s != null)
                {
                    s.close();
                    System.err.println("Socket Closed");
                }

            }
            catch (IOException ie)
            {
                System.err.println("Socket Close Error");
            }
        }//end finally
    }

    private ArrayList<String> getRequest(String inputLine) throws IOException {

        ArrayList<String> finalOut = new ArrayList<>();
        int listFlag = 10;
        String inline = "";
        int maxPage = 14;
        if ("list".equals(inputLine)) {
            listFlag = 1;
            String pageContent = "";
            String checkCondition = "";
            for (int i = 1; i <= maxPage; i++) {
                System.out.println(Integer.toString(i));
                pageContent = fullList(i);
                if("Error".equals(pageContent)) {
                    finalOut.add("Error while getting the data. Try again later.");
                    listFlag = 5;
                    break;
                }
                    if (i != 1) {
                        pageContent = pageContent.substring(1);
                    }
                    if (i != maxPage) {
                        pageContent = pageContent.substring(0, pageContent.length() - 1);
                    }
                    // Append a comma between pages if not the last page
                    if (i < maxPage) {
                        pageContent += ",";
                    }
                inline += pageContent;
            }
        }else{
            inline = getSearchedNFT(inputLine);
            listFlag = 0;
            //System.out.println(inline);
            if (inline.equals("Error")) {
                listFlag = 10;
            }
        }

        String symbol = null;
        String id = null;
        String name = null;
        String assetPlatformId = null;
        String floorPriceUSD = null;
        String contractAddress = null;


        if (listFlag == 0) {
            //System.out.println(inline);
            JSONObject jsonObject = new JSONObject(inline);
            name = jsonObject.getString("name");
            assetPlatformId = jsonObject.getString("asset_platform_id");
            floorPriceUSD = jsonObject.getJSONObject("floor_price").getBigInteger("usd").toString();
            finalOut.add(0,"Name: " +  name + " | Asset Platform ID: " + assetPlatformId + " | Floor Price USD$"  + floorPriceUSD);
        }

        if (listFlag == 1) {
            JSONArray jsonArray = new JSONArray(inline);
            for (int i = 0; i < jsonArray.length(); i++) {
                id = jsonArray.getJSONObject(i).getString("id");
                symbol = jsonArray.getJSONObject(i).getString("symbol");
                name = jsonArray.getJSONObject(i).getString("name");
                assetPlatformId = jsonArray.getJSONObject(i).getString("asset_platform_id");
                if (!jsonArray.getJSONObject(i).isNull("contract_address")) {
                    contractAddress = jsonArray.getJSONObject(i).getString("contract_address");
                    finalOut.add(i, "|Symbol: " + symbol + "  |Name: " + name + "  |ID: " + id + "  |Asset PlatformID: " + assetPlatformId + "  |Contract Address: " + contractAddress);
                } else {
                    finalOut.add(i, "|Symbol: " + symbol + "  |Name: " + name + "  |ID: " + id + "  |Asset PlatformID: " + assetPlatformId + "  |Contract Address: Null");
                }
            }
        }

        if (listFlag==10){
            finalOut.clear();
            finalOut.add("There is an error while getting the data. Check your command or wait for a while to restore requests.");
        }

        finalOut.add("done");

        //System.out.println(finalOut);

        return finalOut;
    }

    private String fullList(int page) throws IOException {
        try {
            String BASE_URL = "https://api.coingecko.com/api/v3/nfts/";
            String LIST_ENDPOINT = "list?per_page=250&page=" + Integer.toString(page);
            String urlString = BASE_URL + LIST_ENDPOINT;

            URL url = new URL(urlString);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            String inLine = "";
            Scanner scanner = new Scanner(url.openStream());

            while (scanner.hasNext()) {
                inLine += (scanner.nextLine());
            }

            scanner.close();
            System.out.println(inLine);
            return inLine;
        }catch (IOException e){
            return "Error";
        }
    }
    private String getSearchedNFT(String nftName) throws IOException{
        String BASE_URL = "https://api.coingecko.com/api/v3/nfts/";
        String urlString = BASE_URL + nftName;
        StringBuilder inLine = new StringBuilder();
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            int status = conn.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                return "Error";
            }

            Scanner scanner = new Scanner(url.openStream());
            while (scanner.hasNext()) {
                inLine.append(scanner.nextLine());
            }
            scanner.close();
            //System.out.println(inLine.toString());
        } catch (IOException e) {
            return "Error";
        }
        return inLine.toString();
    }
}
