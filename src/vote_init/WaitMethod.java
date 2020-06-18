package vote_init;

import org.junit.Test;
import person.Voter;
import person.WaitVoter;

import java.util.ArrayList;
import java.util.HashMap;

import static java.lang.Math.pow;

/**
 * Created by ACER on 2020/5/18.
 */
public class WaitMethod {
    public static int k;   //候选者个数
    public static ArrayList<Voter> voterList = new ArrayList<>();
    public static ArrayList<WaitVoter> waitVoterList = new ArrayList<>();
    public static HashMap<String,Integer> resultMap = new HashMap<>();
    public static int m;

    //    建立候选人名单
    @Test
    public  static void waitVoterForm(){
        VoteMethod.init();
        voterList = VoteMethod.list;
        int n = voterList.size();     //选举者个数
        m = (int)(Math.random()*n);
        int newm =(int) pow(2, m);
        while(newm<n){
            m = (int)(Math.random()*n);
            newm =(int) pow(2, m);
        }

        String[] wvoter = {"shkh","chks","ywdi"};
        for (int i = 0; i < wvoter.length; i++) {
            WaitVoter wv = new WaitVoter();
            wv.setWaitAddress(wvoter[i]);
            wv.setNumid(i);
            wv.setTotal(0);
            waitVoterList.add(wv);
            resultMap.put(wvoter[i],0);
        }
    }

}
