package Chapter6.lab5;

import java.io.Serializable;

public class Packet implements Serializable {

  private long sendTime;
  private int seq;

  public Packet(long sendTime, int seq) {
    this.sendTime = sendTime;
    this.seq = seq;
  }

  public long getRTT(long receiveTime) {
    return receiveTime - sendTime;
  }

  public int getSeq() {
    return seq;
  }
}
