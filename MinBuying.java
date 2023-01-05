// Jared Boyd 1/5/2023
// Predicts stocks and when to buy based on average drops and standard deviation from the starting price

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MinBuying {

    public static void main(String[] args){
        //String[] codes = new String[]{"WMT", "AZN", "AAPL", "GOOG", "AMZN", "XOM", "T", "CVX", "JNJ", "KR", "DELL", "UPS", "BAC", "UAL", "ANGPY", "DIS", "BBY", "DAL"};
        String[] codes = new String[]{"AAPL", "MSFT", "AMZN", "GOOG", "JNJ", "XOM", "JPM", "PG", "NVDA", "V", "HD", "CVX", "MA", "LLY", "TSLA", "PFE", "ABBV", "MRK", "META", "PEP", "KO", "BAC", "AVGO", "TMO", "WMT", "COST", "CSCO", "MCD", "ABT", "DHR", "ACN", "VZ", "NEE", "DIS", "WFC", "LIN", "ADBE", "PM", "NKE", "T", "NFLX", "IBM", "LOW", "GS"};
        Map<String, List<double[]>> data = new HashMap<>();
        Map<String, double[]> statsToLow = new HashMap<>();

        //Gets data and calculates statistics of historical data
        for(String code : codes){
            List<double[]> temp = getData(code);
            data.put(code, temp);
            statsToLow.put(code, stats(temp));
        }
        double budget = 1000.0;
        double sum = 0.0;
        int found = 0;

        //Simulates four months worth of data
        for(int i = 96 ; i >= 0 ; i--){

            for(String code : codes) {

                if(found == 0) {
                    double[] dayData = data.get(code).get(i);
                    double[] dayStats = statsToLow.get(code);
                    //double limit = dayData[0] * 0.96;
                    double limit = dayData[0] - dayStats[0] - dayStats[1] * 1.5;

                    if (dayData[2] < limit) {
                        System.out.println("Bought " + code + " at " + limit);
                        double soldAt = dayData[3];

                        if (i != 0) {
                            soldAt = data.get(code).get(i - 1)[0];
                        }
                        System.out.println("Sold at " + soldAt);
                        int numBought = (int) (budget / limit);
                        budget -= numBought * limit;
                        budget += numBought * soldAt;
                        System.out.println(budget);
                        found = 1;
                    }
                }
            }

            if(found == 0){
                System.out.println("Did Not Buy");
            }
            found = 0;
        }

        System.out.println("Final Total: " + budget);
    }

    /*
    * Calculates the mean and standard deviation of the difference between the low point and the starting price
    * Input:
    *   data - the historical data gathered on a specific stock
    * Output: A list of doubles including the mean and standard deviation
    */
    public static double[] stats(List<double[]> data){
        double[] toReturn = new double[2]; //mean and sd
        double sum = 0.0;

        //Calculates mean
        for(int i = 0 ; i < data.size() ; i++){
            double[] dayData = data.get(i);
            sum += dayData[0] - dayData[2];
        }

        toReturn[0] = sum / data.size();
        double sdSum = 0.0;

        //Calculates standard deviation
        for(int i = 0 ; i < data.size() ; i++){
            double[] dayData = data.get(i);
            sdSum += Math.pow((toReturn[0] - (dayData[0] - dayData[2])), 2);
        }
        toReturn[1] = Math.sqrt(sdSum / data.size());

        return toReturn;
    }

    /*
    * Scrapes historical data from Yahoo Finance based on the given stock
    * Input:
    *   code - a String representing the ticker symbol of a stock
    * Output: A list of historical data by day for the given stock
    */
    public static List<double[]> getData(String code) {
        URL url;
        InputStream is = null;
        BufferedReader br;
        String line;
        String html = "";

        try {
            //url = new URL("https://finance.yahoo.com/quote/" + code + "/history?period1=1606780800&period2=1638316800");
            url = new URL("https://finance.yahoo.com/quote/" + code + "/history/");
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
        String[] days = html.split("Ta\\(start\\)");
        List<double[]> data = new ArrayList<>();

        for(int i = 2 ; i < days.length ; i++){
            String[] dayData = days[i].split("Py\\(10px\\) Pstart\\(10px\\)");

            if(dayData.length > 5){
                String[] toCheck = new String[]{dayData[1], dayData[2], dayData[3], dayData[4]};
                double[] toAdd = new double[4];
                int counter = 0;

                for(String elem : toCheck){
                    double num = Double.parseDouble(elem.substring(elem.indexOf("span")+5, elem.indexOf("/span")-1));
                    toAdd[counter] = num;
                    counter++;
                }
                data.add(toAdd);
            }
        }
        return data;
    }
}
