package vote_init;

import org.junit.Before;
import org.junit.Test;
import person.Voter;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.jpbc.PairingParameters;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.pairing.a.TypeACurveGenerator;
import person.WaitVoter;
import zero_protocol.VKPProtocol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Created by ACER on 2020/5/3.
 */
public class VoteMethod {
    public static int n;   //投票者个数
    public static ArrayList<Voter> list = new ArrayList<>();
    public static Element g = null;
    public static Element z = null;
    public static Field G=null;
    public static Field Z=null;
    public static Element e1= null;
    public static Element e2 = null;

    public static void init() {
        int pbits = 1021;
        TypeACurveGenerator pg = new TypeACurveGenerator(pbits, pbits);
        PairingParameters typeAParams = pg.generate();
        Pairing pairing = PairingFactory.getPairing(typeAParams);
        //生成阶数q的G1群
        G = pairing.getG1();
        //生成G的生成元
        g = G.newRandomElement().getImmutable();
        Z = pairing.getZr();
        z = Z.newRandomElement().getImmutable();
        e1=G.newRandomElement().getImmutable();
        e2=G.newRandomElement().getImmutable();
    }

//    投票者注册
    public static void regist(String...params) {
        n = params.length;
//       为投票者生成对应的公钥以及私钥
        for (int i = 0; i < n; i++) {
            Voter vt = new Voter();
            vt.setAddress(params[i]);
            vt.setVoteNum(-1);
            list.add(vt);
        }
    }

//    投票者生成公私钥
    public static void generateKey (String address) {
        for (int i = 0; i < n; i++) {
            if (list.get(i).getAddress().equals(address)) {;
                list.get(i).setSecretKey(Z.newRandomElement().getImmutable());
                list.get(i).setPublicKey(g.powZn(list.get(i).getSecretKey()));
            }
        }
    }
//    检验是否全部生成完毕
    public static boolean allGeneKey () {
        for (int i = 0; i < n; i++) {
            if(list.get(i).getPublicKey()==null){
                System.out.println("选举者还未完全注册！！！");
                return false;
            }
        }
        System.out.println("选举者已经完全注册！！！");
        return true;
    }
}






