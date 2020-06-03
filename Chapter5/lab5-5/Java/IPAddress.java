package Chapter5.Lab5;

import java.util.BitSet;

public class IPAddress {
    String ipaddress;
    String netmask;
    String dest;
    BitSet ip;
    BitSet mask;
    long addr=0;
    int tmp=0;
    IPAddress(String info) throws Exception
    {
        dest=info.split("  ")[1];
        String IpInfo=info.split("  ")[0].split("/")[0];
        netmask=Integer.toBinaryString(Integer.parseInt(info.split("  ")[0].split("/")[1]));
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
        String binstr=Long.toBinaryString(addr);
        ipaddress=binstr;
        ip=binaryToBitSet(binstr);
        String maskinfo=info.split("  ")[0].split("/")[1];
        mask=new BitSet(32);
        for(int i=0;i<=32;++i)
        {
            if(i<Integer.parseInt(maskinfo))
            {
                mask.set(i,true);
            }
        }
//        System.out.println(binstr);
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
    private String binToIp(String bin)
    {
        String ret="";
        int tmp=0;
        int len=bin.length();
        if(len<8)
        {
             ret+=Integer.parseInt(bin,2);
             return ret;
        }
        for(int i=0;i<len/8;++i)
        {
            String sub=bin.substring(i*8,(i+1)*8);
            ret+=Integer.parseInt(sub,2);
            if(i!=len/8-1) ret+='.';
        }
//        ret+=((Integer)tmp).toString();
        return ret;
    }
    public void showInfo()
    {
        System.out.print("Address="+binToIp(ipaddress)+" Netmask="+binToIp(netmask)+" Dest="+dest);
    }
}
