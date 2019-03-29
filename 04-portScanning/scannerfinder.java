import org.jnetpcap.Pcap;
import org.jnetpcap.packet.JPacket;
import org.jnetpcap.packet.JPacketHandler;
import org.jnetpcap.protocol.network.Ip4;
import org.jnetpcap.protocol.tcpip.Tcp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

public class scannerfinder {

    public static void main(String[] args) {

        // if user doesn't give an argument, tell them they did it wrong
        if(args.length == 0){
            System.out.println("One argument is required. Please try again.");
        }
        else {
            String pcapFile = args[0];
            detectSYNs(pcapFile);
        }
    }

    private static void detectSYNs(String pcapFilename) {
        StringBuilder errorMsg = new StringBuilder();
        Pcap capture = Pcap.openOffline(pcapFilename, errorMsg);

        // if there is an error retrieving the .pcap file
        if (capture == null){
            System.err.println(errorMsg.toString());
            return;
        }

        System.out.println("begin analyzing file " + pcapFilename + "...");

        // a hash map to store all of the IP addresses and their counts
        HashMap<Integer, IPActivityCount> countingIPs = new HashMap<>();

        capture.loop(Pcap.LOOP_INFINITE, new PacketHandler(), countingIPs);

        // close the .pcap file
        capture.close();

        // create output file for suspicious IP addresses
        File output = new File("output.txt");

        try (FileWriter writer = new FileWriter(output, true)) {
            // loop through all the ip data and see which ones were sending 3x + more SYNS than SYN+ACKs received
            for (IPActivityCount count : countingIPs.values()){
                if(count.SYNsReceived > (count.SYNACKsSent * 3)){
                    System.out.println(count.ipAddress.getHostAddress());
                    writer.write(count.ipAddress.getHostAddress());
                    writer.write("\n");
                }
            }
        } catch (IOException exception) {
            System.out.println("An IO error occurred when attempting to write to " + output);
        }
    }

    private static class IPActivityCount {
        InetAddress ipAddress;
        int SYNsReceived = 0;
        int SYNACKsSent = 0;
    }

    private static class PacketHandler implements JPacketHandler<HashMap<Integer, IPActivityCount>> {
        Tcp tcpPkt = new Tcp();
        Ip4 ipAddress = new Ip4();

        public void nextPacket(JPacket pkt, HashMap<Integer, IPActivityCount> countIPs) {

            // If the current packet is not a TCP packet, skip
            if (!pkt.hasHeader(tcpPkt)) {
                return;
            }

            // If the current packet is not an IPv4 packet, skip
            if (!pkt.hasHeader(ipAddress)) {
                return;
            }

            // If the current packet is a SYN+ACK
            if (tcpPkt.flags_SYN() && tcpPkt.flags_ACK()) {
                // Get destination IP info
                int dest = ipAddress.destinationToInt();
                IPActivityCount ipRecord = countIPs.get(dest);

                // If a new IP record needs to be made for the destination
                if (ipRecord == null) {
                    ipRecord = new IPActivityCount(); // make the new record
                    try {
                        ipRecord.ipAddress = InetAddress.getByAddress(ipAddress.destination());
                    } catch (UnknownHostException exception) {
                        exception.printStackTrace();
                    }
                    countIPs.put(dest, ipRecord);
                }
                // add 1 to the count of SYN+ACK packets sent from the current IP address
                ipRecord.SYNACKsSent += 1;
                return;
            }

            // If the current packet is a SYN packet
            if (tcpPkt.flags_SYN()) {
                // Get IP info of the source
                int source = ipAddress.sourceToInt();
                IPActivityCount ipRecord = countIPs.get(source);

                // If a new IP record needs to be made for the source
                if (ipRecord == null) {
                    ipRecord = new IPActivityCount(); // make the new record
                    try {
                        ipRecord.ipAddress = InetAddress.getByAddress(ipAddress.source());
                    } catch (UnknownHostException exception) {
                        exception.printStackTrace();
                    }
                    countIPs.put(source, ipRecord);
                }
                // add 1 to the count of SYN packets received by the current IP address
                ipRecord.SYNsReceived += 1;
            }
        }
    }

}
