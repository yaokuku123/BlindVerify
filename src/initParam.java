import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.jpbc.PairingParameters;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.pairing.a.TypeACurveGenerator;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.Queue;


/**
 * Created by ACER on 2020/5/3.
 */
public class initParam {
    @Test
    public void init(){
        //文件读取生成TypeA类型的，基于椭圆曲线的对称质数阶双线性群（G1=G2）,G和G1是乘法循环群
        //Pairing pairing = PairingFactory.getPairing("a.properties");
        //PairingFactory.getInstance().setUsePBCWhenPossible(true);

        //动态代码生成
        //rBit是Zp中阶数p的比特长度；qBit是G中阶数的比特长度
        int rbits = 117;
        int qbits = 1024;
        TypeACurveGenerator pg = new TypeACurveGenerator(rbits,qbits);
        PairingParameters typeAParams = pg.generate();
        Pairing pairing = PairingFactory.getPairing(typeAParams);

        //初始化相关参数
        Element G1 = pairing.getG1().newRandomElement().getImmutable();     //生成生成元
        System.out.println("G1的随机元素：" + G1);

        Element G2 = pairing.getG2().newRandomElement().getImmutable();
        System.out.println("G2的随机元素：" + G2);

        Element Z = pairing.getZr().newRandomElement().getImmutable();
        System.out.println("Z的随机元素：" + Z);

        Element GT = pairing.getGT().newRandomElement().getImmutable();
        System.out.println("G_T中的随机元素：" + GT);

        Element G11 = pairing.getG1().newRandomElement().getImmutable();
        Element G22 = pairing.getG2().newRandomElement().getImmutable();
        Element Z2 = pairing.getZr().newRandomElement().getImmutable();
        Element GT2= pairing.getGT().newRandomElement().getImmutable();

        //G_1的相关运算
        //G_1 multiply G_1
        Element G_1_m_G_1 = G1.mul(G11);
        System.out.println("G1*G1=："+G_1_m_G_1);

            //G_1 power Z
        Element G_1_e_Z = G1.powZn(Z);
        System.out.println("G1^Z=："+G_1_e_Z);

        //G_2的相关运算
        //G_2 multiply G_2
        Element G_2_m_G_2 = G2.mul(G22);
        //G_2 power Z
        Element G_2_e_Z = G2.powZn(Z2);

        //G_T的相关运算
        //G_T multiply G_T
        Element G_T_m_G_T = GT.mul(GT2);
        //G_T power Z
        Element G_T_e_Z = GT.powZn(Z);

        //Z的相关运算
        //Z add Z

        Element Z_a_Z = Z.add(Z2);
        System.out.println("Z+Z2=：" + Z_a_Z);
        //Z multiply Z
        Element Z_m_Z = Z.mul(Z2);
        System.out.println("Z*Z2=：" + Z_m_Z);


        System.out.println("Z:"+Z);
        System.out.println("Z2:"+Z2);

        Element Z3 = pairing.getZr().newRandomElement().getImmutable();
        Element G3 = G1.powZn(Z3);
        System.out.println("------------------");
        System.out.println(G1.powZn(Z.sub(Z3.mul(Z2))).mul(G3.powZn(Z2)));
        System.out.println(G1.powZn(Z));

        System.out.println("-----------");
        System.out.println(G1.mul(G2).div(G2));
        System.out.println(G1);

        System.out.println(G1.powZn(Z).powZn(Z2));
        System.out.println(G1.powZn(Z.mul(Z2)));

        //Pairing运算
        Element pairing1 = pairing.pairing(G1, G2).powZn(Z).powZn(Z2);   //e(u,v)^ab
        Element pairing2 = pairing.pairing(G_1_e_Z, G_2_e_Z);      //e(u^a,v^b)
        boolean result=false;
        if(pairing1.equals(pairing2)){
            result=true;
        }else {
            result = false;
        }
        System.out.println(result);
    }

    @Test
    public void checkSymmetric(){
        Pairing pairing = PairingFactory.getPairing("a.properties");
        if (!pairing.isSymmetric()) {
            throw new RuntimeException("双线性映射是不对称的!");
        }
        else
            System.out.println("双线性映射是对称的");

    }




}
