package Chapter5.Lab5;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Scanner;

public class routerTableLookUp {
    private static  String configPath="D:\\GoToCode\\Java\\ComputerNetworkLab\\src\\Chapter5\\Lab5\\routertable.properities";
    private  static  ArrayList<IPAddress> routerTable;
    private static String hexStr =  "0123456789ABCDEF";
    private  static  void displayRoute()
    {
        System.out.println("-------------------------Router Table Begin-----------------------------");

        for(IPAddress ipAddress:routerTable)
        {
//            System.out.println("Address="+byteToIp(ipAddress.ipaddress)+" Netmask="+byteToIp(ipAddress.netmask)+" Dest="+ipAddress.dest);
             ipAddress.showInfo();
             System.out.println();
        }
        System.out.println("-------------------------Router Table End-------------------------------");
    }
    public static String ipToBinary(String IpInfo)
    {
        long addr=0;
        int tmp=0;
        for(int i=0;i<IpInfo.length();++i)
        {
            char ch=IpInfo.charAt(i);
            if(ch=='.')
            {
                addr+=tmp;
                addr*=256;
                tmp=0;
            }
            else
            {
                tmp=tmp*10+ch-'0';
            }
        }
        return Long.toBinaryString(addr);
    }
    public static String ipToString(String ip)
    {
        String ret="";
        for(int i=0;i<ip.length();++i)
        {
            char ch=ip.charAt(i);
            if (ch >= '0' && ch <= '9')
               ret+=ch;
        }
        return ret;
    }
    public static String decimalToBinary(Long n) {
        String str = "";
        while (n != 0) {
            str = n % 2 + str;
            n = n / 2;
        }
        return str;
    }
    public static BitSet binaryToBitSet(String binary) {
        BitSet bitSet = new BitSet();
        for (int i = 0; i < binary.length(); i++) {
            String c = String.valueOf(binary.charAt(i));
            if ("1".equals(c)) {
                bitSet.set(i, true);
            }
        }

        return bitSet;
    }
    public static String match(String ip)
    {
        String ret="";
        int masklength=0;
        for(IPAddress ipAddress:routerTable)
        {
            BitSet curIp=binaryToBitSet(ipToBinary(ip));

            curIp.and(ipAddress.mask);
            boolean matched=(curIp.equals(ipAddress.ip));
//            System.out.println("");
            ipAddress.showInfo();
            if(matched) {
                System.out.println("    Matched!");
                if(ipAddress.netmask.length()>=masklength)
                {
                    masklength=ipAddress.netmask.length();
                    ret=ipAddress.dest;
                }
            }
            else System.out.println("   Not Matched!");
        }
        return ret;
    }
    public static void main(String[] args) throws Exception {
        routerTable=new ArrayList<>();
        File file=new File(configPath);
        BufferedReader bufferedReader=new BufferedReader(new FileReader(file));
        String fileString;
        //insert routeTable
        while((fileString=bufferedReader.readLine())!=null)
        {
            if(fileString.equals("RoutingTable="))
            continue;
            else
            {
                routerTable.add(new IPAddress(fileString));
            }
        }
        displayRoute();
        //read info
        System.out.print("Please input the IP address of the destination:");
//        System.out.println("Matching……");
        Scanner scanner = new Scanner(System.in);
        String ip=scanner.nextLine();
        System.out.println("--------------------------Start Matching--------------------------------");
        String des=match(ip);
        System.out.println("--------------------------Matching Done---------------------------------");
        System.out.println();
        System.out.println("The next destination is "+des);

    }
}
