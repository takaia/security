import java.io.*;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) {
        String fileName = "memorydump.dmp";
        StringBuilder sb = new StringBuilder();

        try (FileReader fr = new FileReader(fileName)) {
            int i;

            // make a stringBuilder out of the whole input file
            while ((i = fr.read()) != -1) {
                sb.append((char) i);
            }
        }
        catch(FileNotFoundException exception){
            System.out.println("File " + fileName + " not found.");
        }
        catch(IOException exception) {
            System.out.println("An IO error occurred when attempting to read " + fileName);
        }

        String fileText = sb.toString();
        findTrack1(fileText);

    }

    private static void findTrack1(String fileText){

        // match an unencrypted track 1 magstripe pattern from the input file
        Pattern pattern = Pattern.compile("%B\\d{13,19}\\^\\w{1,25}/\\w{1,25}\\^\\d{4}\\d{3}");
        Matcher matcher = pattern.matcher(fileText);

        String[] matches = matcher.results().map(MatchResult::group).toArray(String[]::new);
        int numRecords = matches.length; // number of times the regex was found in the input

        File output = new File("cards.output");

        try (FileWriter writer = new FileWriter(output, true)) {

            // introduce output with how many unencrypted track 1 records were found
            String intro = "There are " + numRecords + " track I records in the memory data";

            // in case of a singular record, correct grammar
            if (numRecords == 1){
                intro = "There is " + numRecords + " track I record in the memory data";
            }

            // write intro text to file, and console for convenience
            writer.write(intro);
            System.out.println(intro);

            // iterate through every record and print it out to file and console
            for(int i = 0; i < numRecords; i++) {
                // split by field separator TO DO: up to the end sentenal?
                String[] card = matches[i].split("\\^");
                String header = "\n\n<Information of " + numToOrdinal(i + 1) + " record>";
                String name = "\nCardholder's Name: " + card[1];
                String cardNum = "\nCard Number: " + card[0].substring(2);
                String expDate = "\nExpiration Date: " + formatDate(card[2].substring(0, 4));
                String cvc = "\nCVC Number: " + card[2].substring(4, 7);

                writer.write(header + name + cardNum + expDate + cvc);
                System.out.print(header + name + cardNum + expDate + cvc);
            }
        }
        catch (IOException exception) {
            System.out.println("An IO error occurred when attempting to write to " + output);
        }
    }

    // take a date formatted YYMM and convert it to MM/YYYY
    private static String formatDate(String date){
        String year = "20" + date.substring(0,2);
        String month = date.substring(2,4);

        date = month + "/" + year;
        return date;
    }

    // convert a number to an ordinal, like 1st, 2nd, 3rd, etc.
    private static String numToOrdinal(int num) {

        // special cases that don't fit the main ordinal pattern (11, 12, and 13)
        if ((num % 100) >= 11 && (num % 100) <= 13) {
            return num + "th";
        }

        // main ordinal pattern
        switch (num % 10) {
            case 1:
                return num + "st";
            case 2:
                return num + "nd";
            case 3:
                return num + "rd";
            default:
                return num + "th";
        }
    }
}
