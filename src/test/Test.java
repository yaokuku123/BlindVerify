package test;

import it.unisa.dia.gas.jpbc.*;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.pairing.a.TypeACurveGenerator;

import java.io.*;
import java.math.BigInteger;

/**
 * Created by admin on 2020/6/6.
 */
public class Test {




    public static void main(String[] args) throws IOException {

        //密码协议部分准备
        int rbits = 53;
        int qbits = 1024;
        TypeACurveGenerator pg = new TypeACurveGenerator(rbits, qbits);
        PairingParameters typeAParams = pg.generate();
        Pairing pairing = PairingFactory.getPairing(typeAParams);
        //初始化相关参数
        Element g = pairing.getG1().newRandomElement().getImmutable();     //生成生成元
        Element b = pairing.getG1().newRandomElement().getImmutable();     //生成生成元


        Field G1 = pairing.getG1();
        Element e = G1.newRandomElement();
//        ElementPowPreProcessing ppp = e.pow();
//        Element a = ppp.pow(BigInteger.valueOf(5));

       // System.out.println(g.isEqual(b));
//
//        System.out.println(g);
//
//        Element a= b.duplicate();

//        ObjectOutputStream oo = new ObjectOutputStream(new FileOutputStream(
//                new File("F:/g.txt")));
//        oo.writeObject(t);
//        System.out.println("Person对象序列化成功！");
//        oo.close();

//        byte[]c= g.toBytes();
//        System.out.println(b);
//        System.out.println(a.isImmutable());
//
//        int result=a.setFromBytes(c);

//        b.setFromBytes(a);
        System.out.println();
    }
}
