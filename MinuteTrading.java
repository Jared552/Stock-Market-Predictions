import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

public class MinuteTrading {

    public static void main(String[] args){
        String time = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
        double pastPrice = 0.0;
        int currentStocks = 0;
        double boughtPrice = 0;
        double budget = 1000;
        String code = "AAPL";
        int counter = 0;
        int num = 0;

        while(!time.startsWith("5")){
            double currentPrice = getCurrentPrice(code);
            System.out.print(currentPrice + " ");
            num++;

            if(num % 10 == 0){
                System.out.println();
            }
            time = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());

            if (pastPrice != 0.0){

                if(currentStocks == 0 && currentPrice > pastPrice){
                    double[] info = buy(code, currentPrice, budget, time);
                    budget = info[0];
                    currentStocks = (int)info[1];
                    boughtPrice = currentPrice;
                }

                else if (currentStocks != 0 && currentPrice < pastPrice){
                    budget = sell(code, currentStocks, currentPrice, boughtPrice, budget, time);
                    currentStocks = 0;
                    boughtPrice = 0;
                }
            }
            pastPrice = currentPrice;
            counter++;

            try {
                Thread.sleep(30 * 1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        time = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());

        if(currentStocks != 0){
            sell(code, currentStocks, getCurrentPrice(code), boughtPrice, budget, time);
        }
    }

    public static double[] buy(String code, double price, double budget, String time){
        int numToBuy = (int)(budget / price);
        String str = "Buy " + numToBuy + " " + code + " at " + price + ". Time: " + time;
        document(str);
        return new double[]{(budget - price * numToBuy), (double)numToBuy};
    }

    public static double sell(String code, int numStock, double sellPrice, double buyPrice, double budget, String time){
        String str = "Sell " + numStock + " " + code + " at " + sellPrice + ". Bought at " + buyPrice + ". Change " + (sellPrice - buyPrice) + ". Time: " + time;
        document(str);
        return budget + numStock * sellPrice;
    }

    public static void document(String toDocument){
        try {
            FileWriter toWrite = new FileWriter("C:\\Users\\jared\\IdeaProjects\\MinuteTrading\\src\\Documentation.txt", true);
            toWrite.append(toDocument + "\n");
            toWrite.close();
        } catch (IOException e){
            System.out.println("No file");
        }
    }

    public static double getCurrentPrice(String code) {
        URL url;
        InputStream is = null;
        BufferedReader br;
        String line;
        String html = "";

        try {
            url = new URL("https://finance.yahoo.com/quote/" + code + "/");
            is = url.openStream();  // throws an IOException
            br = new BufferedReader(new InputStreamReader(is));

            while ((line = br.readLine()) != null) {
                html += line;
            }
        } catch (MalformedURLException mue) {
            mue.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException ioe) {
                System.out.println("Error");
            }
        }
        html = html.substring(html.indexOf("regularMarketPrice"));
        html = html.substring(html.indexOf(">")+1, html.indexOf("<"));
        return Double.parseDouble(html);
    }
}
