import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class DailyMarkovChain {

    public static void main(String[] args) {
        String path = "C:\\Users\\jared\\IdeaProjects\\DayTrading\\src\\CurrentHoldings.txt";
        String contents = "";

        try {
            contents = Files.readString(Paths.get(path));
        } catch (IOException e){
            System.out.println("No file");
        }
        String[] lines = contents.split("\n");
        double budget = Double.parseDouble(lines[0]);
        System.out.println("Initial Budget: " + budget);

        for(int i = 1 ; i < lines.length ; i++){
            String[] data = lines[i].split("\t");
            double boughtPrice = Double.parseDouble(data[1]);
            int quant = Integer.parseInt(data[2]);
            double currentPrice = getData(data[0]).get(0)[2];
            System.out.println("Sold " + quant + " " + data[0] + " for " + currentPrice + " each. Bought at " + boughtPrice + ". Change: " + (currentPrice - boughtPrice));
            budget += quant * currentPrice;
        }
        System.out.println("Total Budget: " + budget);

        String[] codes = new String[]{"WMT", "AZN", "AAPL", "GOOG", "AMZN", "XOM", "T", "CVX", "JNJ", "KR", "DELL", "UPS", "BAC", "UAL", "ANGPY", "DIS", "BBY", "DAL"};
        whatToBuy(codes, budget);
    }

    public static void whatToBuy(String[] codes, double budget){
        double[] currents = new double[codes.length];
        double[] difProbs = new double[codes.length];

        for(int i = 0 ; i < codes.length ; i++) {
            String code = codes[i];
            List<double[]> data = getData(code);

            currents[i] = data.get(0)[2];
            List<Integer> org = organizeData(data);
            Map<Integer, double[]> markovResults = markovChain(org);
            int index = org.size() - 1;
            int key = org.get(index) + org.get(index - 1) * 3 + org.get(index - 2) * 9;
            double[] probs = markovResults.get(key);
            difProbs[i] = probs[0] - probs[2];
        }
        int[] choices = new int[]{-1, -1, -1, -1};

        for(int i = 0 ; i < codes.length ; i++){

            if(choices[3] == -1){
                int counter = 2;

                while(counter != -1 && choices[counter] == -1){
                    counter--;
                }

                while(counter != -1 && difProbs[i] > difProbs[counter]){
                    counter--;
                }
                counter++;

                for(int k = 3 ; k > counter ; k--){
                    choices[k] = choices[k-1];
                }
                choices[counter] = i;
            }

            else{
                int counter = 3;

                while(counter != -1 && difProbs[i] > difProbs[counter]){
                    counter--;
                }

                if(counter != 3){
                    counter++;

                    for(int k = 3 ; k > counter ; k--){
                        choices[k] = choices[k-1];
                    }
                    choices[counter] = i;
                }
            }
        }
        double perc = 0.4;
        double left = budget;
        int[] shares = new int[4];

        for(int i = 0 ; i < 4 ; i++){
            int choice = choices[i];

            if(difProbs[choice] >= 0.20) {
                int share = (int) (budget * perc / currents[choice]);
                shares[i] = share;
                left -= share * currents[choice];
            }
            perc= 0.2;
        }
        String fileContents = "";

        for (int i = 0 ; i < 4 ; i++){
            int choice = choices[i];

            if(shares[i] != 0) {
                if (left >= currents[choice]) {
                    int toAdd = (int) (left / currents[choice]);
                    shares[i] += toAdd;
                    left -= toAdd * currents[choice];
                }
                fileContents += "\n" + codes[choice] + "\t" + currents[choice] + "\t" + shares[i];
                System.out.println("Bought " + shares[i] + " shares of " + codes[choice] + " at " + currents[choice]);
            }
        }
        fileContents = left + fileContents;
        System.out.println("\nBudget Left: " + left);

        try {
            FileWriter toWrite = new FileWriter("C:\\Users\\jared\\IdeaProjects\\DayTrading\\src\\CurrentHoldings.txt");
            toWrite.write(fileContents);
            toWrite.close();
        } catch (IOException e){
            System.out.println("No file");
        }
    }


    public static List<double[]> getData(String code) {
        URL url;
        InputStream is = null;
        BufferedReader br;
        String line;
        String html = "";

        try {
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

        for(int i = 2 ; i < days.length-4 ; i++){
            String[] dayData = days[i].split("Py\\(10px\\) Pstart\\(10px\\)");

            if(dayData.length > 5){
                String[] toCheck = new String[]{dayData[2], dayData[3], dayData[4]};
                double[] toAdd = new double[3];
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

    //0 - Greater than 1% increase
    //1 - Between 1 and -1% movement
    //2 - Decrease by at least 1%
    public static List<Integer> organizeData (List<double[]> data){
        List<Integer> toReturn = new ArrayList<>();
        data.get(0)[0] = data.get(0)[2];
        data.get(0)[1] = data.get(0)[2];

        for(int i = data.size()-1 ; i > 0 ; i--){
            double[] current = data.get(i);
            double[] next = data.get(i-1);
            double change = ((next[0] + next[1]) / 2 - (current[0] + current[1]) / 2) / ((current[0] + current[1])/2);

            if(change >= 0.01){
                toReturn.add(0);
            }
            else if(change <= -0.01){
                toReturn.add(2);
            }
            else{
                toReturn.add(1);
            }
        }
        return toReturn;
    }

    public static Map<Integer, double[]> markovChain(List<Integer> data){
        Map<Integer, int[]> toCollect = new HashMap<>();

        for(int i = 0 ; i < 27 ; i++){
            toCollect.put(i, new int[3]);
        }

        for(int i = 0 ; i < data.size()-3 ; i++){
            int val = data.get(i) * 9 + data.get(i+1) * 3 + data.get(i+2);
            toCollect.get(val)[data.get(i+3)]+= 1;
        }
        Map<Integer, double[]> toReturn = new HashMap<>();

        for(Integer key: toCollect.keySet()){
            int[] totals = toCollect.get(key);
            double sum = 0.0 + totals[0] + totals[1] + totals[2];
            double[] probs = new double[]{0.33, 0.33, 0.33};
            toReturn.put(key, probs);

            if(sum != 0){
                probs[0] = totals[0] / sum;
                probs[1] = totals[1] / sum;
                probs[2] = totals[2] / sum;
            }
        }
        return toReturn;
    }
}