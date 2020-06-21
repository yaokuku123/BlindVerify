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
        Element G1 = pairing.getG1().newRandomElement().getImmutable();     //生成生成元
        Element b = pairing.getG1().newRandomElement();     //生成生成元


        Element in1 = pairing.getG1().newRandomElement();



        ElementPowPreProcessing ppp = G1.getElementPowPreProcessing();
        Element pow1 = ppp.pow(BigInteger.valueOf(1));
        System.out.println(pow1);


        ElementPow elementPow=  b;


/* Now let's raise using ppp which returns a new element which contains the result of the operation. */

        Element out1 = ppp.pow(BigInteger.valueOf(5));

        Element out2 = ppp.pow((BigInteger) pairing.getZr().newRandomElement());



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
