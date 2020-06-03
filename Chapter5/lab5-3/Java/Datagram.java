package Chapter5.Lab3;

public class Datagram {
    int ID;
    int Offset;
    int MF;
    int DF;
    int Length;
    public Datagram()
    {
        MF=1;DF=1;
    }
    public void setID(int ID) {
        this.ID = ID;
    }

    public void setOffset(int offset) {
        Offset = offset;
    }

    public void setMF(int MF) {
        this.MF = MF;
    }

    public void setDF(int DF) {
        this.DF = DF;
    }

    public void setLength(int length) {
        Length = length;
    }

    public int getID() {
        return ID;
    }

    public int getOffset() {
        return Offset;
    }

    public int getMF() {
        return MF;
    }

    public int getDF() {
        return DF;
    }

    public int getLength() {
        return Length;
    }
}
