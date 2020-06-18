package zero_protocol;

import org.junit.Test;
import person.Voter;
import person.WaitVoter;
import vote_init.VoteMethod;

import it.unisa.dia.gas.jpbc.Element;
import vote_init.WaitMethod;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import static java.lang.Math.pow;
import static vote_init.WaitMethod.m;


/**
 * Created by ACER on 2020/5/4.
 */
public class VKPProtocol {
    public static ArrayList<WaitVoter> waitVoterList = new ArrayList<>();
    public static Element x;
    public static Element y;
    public static Element h;
    public static Element w;
    public static Element[] alist;
    public static Element[] blist;
    public static Element[] dlist;
    public static Element[] rlist;
    public static Element[] mlist;
    public static Element[] Mlist;
    public static Element c;


    public static Element p = null;

//    i代表备选候选人的编号
    public static Voter excuteVKpp(String address,int num){
        Voter voter = new Voter();
        for (int i = 0; i < VoteMethod.list.size(); i++) {
            if (VoteMethod.list.get(i).getAddress().equals(address)){
                voter = VoteMethod.list.get(i);
                voter.setVoteNum(num);
                System.out.println("当前投票者是："+voter);
            }
        }
        return voter;
    }
    public static void initVkppProtocol(Voter voter, int i){
        waitVoterList = WaitMethod.waitVoterList;
        int n = waitVoterList.size();
//        int i = (int)(Math.random()*n);
        p = VoteMethod.g;
        x = p.powZn(voter.getSecretKey());
        h=voter.getPublicKey();
        Element e1 = h.powZn(voter.getSecretKey());
        int w_m = WaitMethod.m;
        int v =(int) pow(2, i * w_m);
//        int M_v = (int) pow(2, r * w_m);
        Element e2 = p.pow(BigInteger.valueOf(v));
        y=e1.mul(e2);
        w = VoteMethod.Z.newRandomElement().getImmutable();
        alist = new Element[n];
        blist = new Element[n];
        rlist = new Element[n];
        dlist = new Element[n];
        mlist = new Element[n];
        alist[i] = p.powZn(w);
        blist[i] = h.powZn(w);
        mlist[i] = e2;
        for(int j = 0;j<n;j++){
            if(j==i){
                continue;
            }
            dlist[j]=VoteMethod.Z.newRandomElement().getImmutable();
            rlist[j]=VoteMethod.Z.newRandomElement().getImmutable();
            alist[j]=(p.powZn(rlist[j])).mul(x.powZn(dlist[j]));
            int m_j =(int) pow(2, j * w_m);
            mlist[j]=p.pow(BigInteger.valueOf(m_j)) ;
            blist[j]= (h.powZn(rlist[j])).mul((y.div(mlist[j])).powZn(dlist[j]));
        }

        Element e = alist[0].add(blist[0]);
        for (int j = 1; j < n; j++) {
            e = e.add(alist[j]);
            e = e.add(blist[j]);
        }
        c = HashFunction.hash(e);
        dlist[i] = c;
        for (int j = 0; j < n; j++) {
            if(i==j){
                continue;
            }
            dlist[i] = dlist[i].sub(dlist[j]);

        }
        rlist[i] = w.sub(voter.getSecretKey().mul(dlist[i]));
    }

    public static boolean verifyprotocol(Element[] alist,Element[] blist,Element[] dlist,Element[] rlist){
        int n = waitVoterList.size();
        Element dele = dlist[0];
        for (int i = 1; i < n; i++) {
            dele = dele.add(dlist[i]);
        }
        Element[] mlist = new Element[n];
        for (int i = 0; i < n; i++) {
            int m_j =(int) pow(2, i * m);
            mlist[i]=p.pow(BigInteger.valueOf(m_j));
        }
        Element e1 = p.powZn(rlist[0]).mul(x.powZn(dlist[0]));
        Element e2 = h.powZn(rlist[0]).mul((y.div(mlist[0])).powZn(dlist[0]));
        Element e = e1.add(e2);
        for (int i = 1; i < n; i++) {
            Element element1 = p.powZn(rlist[i]).mul(x.powZn(dlist[i]));
            Element element2 = h.powZn(rlist[i]).mul((y.div(mlist[i])).powZn(dlist[i]));
            e = e.add(element1);
            e = e.add(element2);
        }
        Element hash = HashFunction.hash(e);
        System.out.println("验证者进行验证，最后验证的结果是：");
        return dele.isEqual(hash);
    }



    public static void main(String[] args) {
//        初始化椭圆曲线循环群
        VoteMethod.init();
//        选举者开始注册
        VoteMethod.regist("defr","fhgt","wert");
//        候选人表单生成
        WaitMethod.waitVoterForm();
//        为每个选举者生成私钥和对应的公钥
        VoteMethod.generateKey("defr");
        VoteMethod.generateKey("fhgt");
        VoteMethod.generateKey("wert");
//        验证选举是否全部注册
        VoteMethod.allGeneKey();
//        输出选举者的信息
        System.out.println("全部的选举者为：");
        ArrayList<Voter> votelist = VoteMethod.list;
        for (Voter voter1 : votelist) {
            System.out.println(voter1);
        }
//        输出候选者信息
        System.out.println("全部的候选者为：");
        ArrayList<WaitVoter> waitVoterList = WaitMethod.waitVoterList;
        for (WaitVoter waitVoter : waitVoterList) {
            System.out.println(waitVoter);
        }
        // 目前的投票者
        System.out.println("当前的投票者为");
        Voter voter = excuteVKpp("defr", 0);
        initVkppProtocol(voter,voter.getVoteNum());
        System.out.println("执行VKPP协议验证所需参数：alist,blist,dlist,rlist");
        System.out.println("alist:= ");
        for (Element element : alist) {
            System.out.println(element);
        }
        System.out.println("blist:= ");
        for (Element element : blist) {
            System.out.println(element);
        }
        System.out.println("dlist:= ");
        for (Element element : dlist) {
            System.out.println(element);
        }
        System.out.println("rlist:= ");
        for (Element element : rlist) {
            System.out.println(element);
        }
        boolean verifyresult = verifyprotocol(alist, blist, dlist, rlist);
        System.out.println(verifyresult);
        if(verifyresult==true){
            WaitVoter waitVoter = waitVoterList.get(voter.getVoteNum());
            String waitAddress = waitVoter.getWaitAddress();
            int total = waitVoter.getTotal()+1;
            WaitMethod.resultMap.put(waitAddress,total);
        }

        Set<String> set = WaitMethod.resultMap.keySet();
        for (String s : set) {
            System.out.println("候选人:"+s+"的票数为"+WaitMethod.resultMap.get(s));
        }

    }
}
