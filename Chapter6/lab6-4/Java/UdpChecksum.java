package Chapter6.lab4;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class UdpChecksum {
    public static int Checksum(ArrayList<Integer> data) {
        int length = data.size();
        int sum = 0;
        System.out.println("length1:"+length);
        if (length % 2 == 1) {
            data.add(0);
            length++;
        }
        System.out.println("length:"+length);
        for (int i = 0; i < length; i += 2) {
            int nowAdd = data.get(i);
            nowAdd <<= 8;
            nowAdd += data.get(i + 1);
            // System.out.println(nowAdd);

            while (nowAdd != 0) {
                sum += nowAdd;
                nowAdd = sum >> 16;
                sum = sum & 0x0000ffff;
            }
        }
        sum = sum ^ 0x0000ffff;
        return sum;
    }
    public static void main(String[] args) throws Exception
    {
        String configPath="D:\\GoToCode\\Java\\ComputerNetworkLab\\src\\Chapter6\\lab4\\udp.properities";
        ArrayList<Integer> udpData=new ArrayList<>();
        FileInputStream is=new FileInputStream(configPath);
        Properties props=new Properties();
        props.load(is);
        String udpSegment=props.getProperty("udpsegment");
        String srcIp=props.getProperty("sourceip");
        String desIp=props.getProperty("destip");
        int udpLength=udpSegment.length()/2;
        //pseudo header
        System.out.println("------------Information of pseudo header---------------");
        System.out.println("Source IP: "+srcIp);
        System.out.println("Destination IP: "+desIp);
        System.out.println("Protocal: 17");
        System.out.println("UDP length: "+udpSegment.length()/2);
        //add pseudo
        String[] src=srcIp.split("\\.");
        String[] des=desIp.split("\\.");
        for(int i=0;i<src.length;++i)
        {
            udpData.add(Integer.parseInt(src[i]));
        }
        for(int i=0;i<des.length;++i)
        {
            udpData.add(Integer.parseInt(des[i]));
        }
        udpData.add(0);
        udpData.add(17);
        udpData.add(udpLength>>8);
        udpData.add(udpLength&0x000000ff);

        //udp header
        System.out.println("----------------Information of UDP header--------------");
        System.out.println("Source port: "+Integer.parseInt(udpSegment.substring(0,4),16));
        System.out.println("Destination port: "+Integer.parseInt(udpSegment.substring(4,8),16));
        System.out.println("UDP length: "+Integer.parseInt(udpSegment.substring(8,12),16));
        System.out.println("Checksum: 0x"+udpSegment.substring(12,16));
        //add udpsegment
        for(int i=0;i<udpLength*2;i+=2)
        {
            if(i==12||i==14) continue;
            udpData.add(Integer.parseInt(udpSegment.substring(i,i+2),16));
        }
        System.out.println("length:"+udpData.size());
        if(udpData.size()%2!=0)
        {
            udpData.add(0);
        }
        System.out.println("chesum="+Checksum(udpData));
        //get checksum
        int checksum=0;
        System.out.println("length:"+udpData.size());
        for(int i=0;i<udpData.size();i+=2)
        {
            int curNum=(udpData.get(i)<<8)+udpData.get(i+1);
            while(curNum!=0)
            {
                checksum += curNum;
                curNum = checksum >> 16;
                checksum &= 0x0000ffff;
            }
        }
        checksum^=0x0000ffff;
        System.out.println("Calacuted checksum is:"+Integer.toHexString(checksum));
    }
}
