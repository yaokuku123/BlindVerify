package person;

import it.unisa.dia.gas.jpbc.Element;

/**
 * Created by ACER on 2020/5/4.
 */
public class Voter {
    private String address;
    private Element secretKey;
    private Element publicKey;
    private int voteNum;


    public Voter() {
    }

    public Voter(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Element getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(Element secretKey) {
        this.secretKey = secretKey;
    }

    public Element getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(Element publicKey) {
        this.publicKey = publicKey;
    }

    public int getVoteNum() {
        return voteNum;
    }

    public void setVoteNum(int voteNum) {
        this.voteNum = voteNum;
    }

    @Override
    public String toString() {
        return "Voter{" +
                "address='" + address + '\'' +
                ", secretKey=" + secretKey +
                ", publicKey=" + publicKey +
                ", voteNum=" + voteNum +
                '}';
    }
}
