package person;

/**
 * Created by ACER on 2020/5/12.
 */
public class WaitVoter {
    private String waitAddress;
    private int numid;
    private int total;

    public String getWaitAddress() {
        return waitAddress;
    }

    public void setWaitAddress(String waitAddress) {
        this.waitAddress = waitAddress;
    }

    public int getNumid() {
        return numid;
    }

    public void setNumid(int numid) {
        this.numid = numid;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    @Override
    public String toString() {
        return "WaitVoter{" +
                "waitAddress='" + waitAddress + '\'' +
                ", numid=" + numid +
                ", total=" + total +
                '}';
    }
}
