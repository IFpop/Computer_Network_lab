package network.chapter3.lab3;

import java.io.*;
import java.nio.file.DirectoryStream.Filter;
import java.util.Properties;

public class Main {
    static String ConfigPath = "E:\\University\\Study\\ThreeDown\\计算机网络\\网络编程小组实验\\lab3\\lab3-3.cfg";
    static int UDPPort;
    static int FilterError;
    static int FilterLost;
    static String FilePath="E:\\GoToCode\\Java\\WorkSpace\\ComputerNetworkLab3\\src\\network\\chapter3\\lab3\\testfile.txt";
    public static void main(String[] args) throws IOException {
        Properties pps = new Properties();
        try {
            InputStream in = new BufferedInputStream(new FileInputStream(ConfigPath));
            pps.load(in);
            UDPPort=Integer.parseInt(pps.getProperty("UDPPort"),10);
            FilterError=Integer.parseInt(pps.getProperty("FilterError"),10);
            FilterLost=Integer.parseInt(pps.getProperty("FilterError"),10);
            (new Client(UDPPort,"")).start();
            new Server(UDPPort,FilterError,FilterLost,FilePath);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
