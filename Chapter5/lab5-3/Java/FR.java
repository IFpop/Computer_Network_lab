package Chapter5.Lab3;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Properties;
import java.util.ResourceBundle;

public class FR {
    //读取属性、进行分片、进行整合
    public static void main(String[] args) throws Exception{
        ArrayList<Datagram> Fragments=new ArrayList<>();
        String configPath="D:\\GoToCode\\Java\\ComputerNetworkLab\\src\\Chapter5\\Lab3\\ipv4.properities";
        FileInputStream is=new FileInputStream(configPath);
        Properties props=new Properties();
        props.load(is);
        int BigIPTotalLen=Integer.parseInt(props.getProperty("BigIPTotalLen"));
        int IPID=Integer.parseInt(props.getProperty("ID"));
        int MTU=Integer.parseInt(props.getProperty("MTU"));
        int FragNum=Integer.parseInt(props.getProperty("FragNum"));
        String[] TotalLen=props.getProperty("TotalLen").split(",");
        String[] newID=props.getProperty("ID1").split(",");
        String[] FragMF=props.getProperty("FragMF").split(",");
        String[] FragOffset=props.getProperty("FragOffset").split(",");
        int DF=0,MF=0;
        System.out.println("------Original Data Packet---------------------------");
        System.out.println("---Total Length----ID----DF------MF-------Offset-----");
        System.out.println("      "+BigIPTotalLen+"        "+IPID+"    "+DF+"       "+MF+"           0");
        System.out.println("MTU:"+MTU);
        int numOfFrag=BigIPTotalLen/IPID;
        if(BigIPTotalLen%IPID!=0) numOfFrag++;
        System.out.println("Number of fragments: "+FragNum);
        //do fragment
        System.out.println("-----------Fragment Infomation------------------------");
        System.out.println("----ID----Length------Flags-------FragmentOffset------");
        for(int i=0;i<FragNum;++i)
        {
            Datagram datagram=new Datagram();
            datagram.setID(Integer.parseInt(newID[i]));
            datagram.setLength(MTU);
            if(i==FragNum-1) {
                datagram.setMF(0);
                datagram.setLength(BigIPTotalLen);
            }
            BigIPTotalLen-=MTU;
            datagram.setOffset(MTU*i/8);
            Fragments.add(datagram);
            System.out.println("   "+datagram.getID()+"    "+datagram.getLength()+"     "+"DF="+datagram.getDF()+",MF="+datagram.getMF()+"      "+datagram.getOffset());
        }
        System.out.println("Fragments are about to be reassembled...");
        System.out.println("Number of Fragments: "+Fragments.size());
        System.out.println("-----------Fragment Infomation------------------------");
//        System.out.println("---ID---Length---Flags-------FragmentOffset------");
        System.out.println("----ID----Length------Flags-------FragmentOffset------");
        for(int i=0;i<Fragments.size();++i)
        {
            Datagram datagram=Fragments.get(i);
            System.out.println("   "+datagram.getID()+"    "+datagram.getLength()+"     "+"DF="+datagram.getDF()+",MF="+datagram.getMF()+"      "+datagram.getOffset());
//            System.out.println(datagram.getID()+"    "+datagram.getLength()+"     "+"DF="+datagram.getDF()+",MF="+datagram.getMF()+"    "+datagram.getOffset());
        }
        System.out.println("-------------Datagram after reassembled---------------");
        Datagram ReassembledDatagram=new Datagram();
        ReassembledDatagram.setMF(0);
        ReassembledDatagram.setDF(0);
        ReassembledDatagram.setID(Fragments.get(0).getID());
        ReassembledDatagram.setOffset(0);
        int newLen=0;
        for(int i=0;i<Fragments.size();++i)
        {
            newLen+=Fragments.get(i).getLength();
        }
        ReassembledDatagram.setLength(newLen);
        System.out.println("----ID----Length------Flags-------FragmentOffset------");
//        System.out.println("---ID---Length---Flags-------FragmentOffset------");
        System.out.println("   "+ReassembledDatagram.getID()+"     "+ReassembledDatagram.getLength()+"     DF="+ReassembledDatagram.getDF()+",MF="+ReassembledDatagram.getMF()+"     "+ReassembledDatagram.getOffset());
//        System.out.println();
    }
}
