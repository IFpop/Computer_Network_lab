package network.chapter3.lab3;

import java.io.Serializable;

//数据进行网络传输需要进行序列化
public class Frame implements Serializable{
    private int IdOfFrame;
    private String State="";
    public static int NumOfData=0;
    public int Length;
    private byte[] Msg;
//    private int FilterError=0;
//    private int FilterLost=0; 
    // private String CrcCode="";
    public Frame(int idOfFrame,int filterError,int filterLost)
    {
        this.IdOfFrame = idOfFrame;
        NumOfData++;
        //模拟设置状态，每10帧中一帧出错，每10帧中一帧丢失
        if(NumOfData%10==5) this.State="Lost";
        else if(NumOfData%10==7) this.State="Error";
        else this.State="Normal";
    }
    // public void SetCrc(String crc)
    // {
    //     this.CrcCode=crc;
    // }
    public void SetMsg(byte[] msg,int len)
    {
        Msg=new byte[len];
        Length=len;
        for(int i=0;i<len;++i) Msg[i]=msg[i];
    }
    public int GetId()
    {
        return this.IdOfFrame;
    }
    public void SetState(String s)
    {
        this.State=s;
    }
    public String GetState()
    {
        return this.State;
    }
    public byte[] GetMsg()
    {
        return this.Msg;
    }
    // public String GetCrc()
    // {
    //     return this.CrcCode;
    // }
    
}
