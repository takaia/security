import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DictAtk {

    public static void main(String[] args) {
        String hashes = "hashes.txt";

        try (BufferedReader br = new BufferedReader(new FileReader(hashes))) {
            String hash;
            File output = new File("cracked.output");

            try (FileWriter writer = new FileWriter(output, true)) {
                while ((hash = br.readLine()) != null) {
                    String begin = "The password for hash value " + hash;
                    String password = attack(hash);

                    // print results to console
                    System.out.print(begin);
                    System.out.print(password);

                    // save results to file
                    writer.write(begin);
                    writer.write(password);
                }
            }
            catch (IOException exception) {
                System.out.println("An IO error occurred when attempting to write to " + output);
            }
        }
        catch(FileNotFoundException exception){
            System.out.println("File " + hashes + " not found.");
        }
        catch(IOException exception) {
            System.out.println("An IO error occurred when attempting to read " + hashes);
        }
    }

    private static String attack(String hash){
        long atkStart = System.nanoTime();
        String passwords = "1000000passwords.txt";
        String rehash = "rehash not initialized";
        String password = null;

        try (BufferedReader br = new BufferedReader(new FileReader(passwords))) {
            // until the hashed value of the guessed password equals the hash being cracked...
            while (!rehash.equals(hash)) {
                //current pw try = next line in list
                password = br.readLine();

                // if program reached end of list, stop checking
                if (password == null){
                    password = "password not cracked";
                    break;
                }

                // hash the guessed password using the algorithm
                rehash = md5(password);
            }
        }
        catch(FileNotFoundException exception){
            System.out.println("File " + passwords + " not found.");
        }
        catch(IOException exception) {
            System.out.println("An IO error occurred when attempting to read " + passwords);
        }

        // record the amount of time it took to crack the password
        double atkEnd = System.nanoTime();
        double atkDuration = (atkEnd - atkStart)/1000000000.0;

        return " is " + password + ", it takes the program " + atkDuration + " sec to recover this password.\n";
    }

    // get the hashed version of the guessed password
    private static String md5(String password){
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(password.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            StringBuilder hashValue = new StringBuilder(no.toString(16));
            while (hashValue.length() < 32) {
                hashValue.insert(0, "0");
            }
            return hashValue.toString();
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "Your MD5 hashing algorithm was not found.";
        }
    }
}
