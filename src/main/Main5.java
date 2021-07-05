package main;

import BlindVerify.Check;
import BlindVerify.Sign;
import BlindVerify.Verify;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.ElementPowPreProcessing;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.jpbc.PairingParameters;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.pairing.a.TypeACurveGenerator;
import util.FileUtil;

import java.io.File;
import java.util.ArrayList;

/**
 * Copyright(C),2019-2021,XXX公司
 * FileName: Main5
 * Author: yaoqijun
 * Date: 2021/6/22 9:49
 */
public class Main5 {


    public static void main(String[] args) {
        long start,end;
        //String filePath = "D:\\study\\code\\test\\zerotrust-demo\\uploadFile\\multichain-2.0-latest.tar.gz";
        String filePath = "D:\\Downloads\\JDK\\javafx-src.zip";
//        String filePath = "D:\\Downloads\\apache-jmeter-5.4.1\\apache-jmeter-5.2.1.rar";
        //初始化配置
        int originFileSize = (int) new File(filePath).length(); //获取文件大小
        int pieceFileSize = 64; //定义片大小
        int pieceFileCount = 128; //定义片数量
        int blockFileSize = pieceFileCount * pieceFileSize; //计算获取块大小
        int blockFileCount = originFileSize / blockFileSize; //获取块数量

        //密码协议部分准备
        int rbits = 53;
        int qbits = pieceFileSize * 8; //512 片 64B 128片
        TypeACurveGenerator pg = new TypeACurveGenerator(rbits, qbits);
        PairingParameters typeAParams = pg.generate();
        Pairing pairing = PairingFactory.getPairing(typeAParams);
        //初始化相关参数
        Element g = pairing.getG1().newRandomElement().getImmutable();     //生成生成元
        Element x = pairing.getZr().newRandomElement().getImmutable();
        Element v = g.powZn(x);
        //生成U
        ArrayList<ElementPowPreProcessing> uLists = new ArrayList<>();
        for (int i = 0; i < pieceFileCount; i++) {
            ElementPowPreProcessing u = pairing.getG1().newRandomElement().getImmutable().getElementPowPreProcessing();
            uLists.add(u);
        }
        //签名阶段
        start = System.currentTimeMillis();
        ArrayList<Element> signLists;
        Sign sign = new Sign();
        FileUtil fileUtil = new FileUtil();
        signLists = sign.signElm(pairing,fileUtil, filePath, uLists, g, x, originFileSize, blockFileSize, pieceFileSize);
        end = System.currentTimeMillis();
        System.out.println("签名时间："+ (end-start)/1000 + "s");

        //查询阶段
        start = System.currentTimeMillis();
        Check check = new Check();
        //求viLists
        ArrayList<Element> viLists;
        viLists = check.getViList(pairing, signLists);
        //求sigma
        Element sigmasValues = check.getSigh(pairing, signLists, viLists);
        //求miu
        ArrayList<Element> miuLists;
        miuLists = check.getMiuListElm(pairing,fileUtil, filePath, viLists, originFileSize, blockFileSize, pieceFileSize);
        end = System.currentTimeMillis();
        System.out.println("查询时间："+ (end-start)/1000 + "s");

        //开始验证
        start = System.currentTimeMillis();
        Verify verify = new Verify();
        boolean result = verify.verifyResult(pairing, g, uLists, v, sigmasValues, viLists, signLists, miuLists);
        end = System.currentTimeMillis();
        System.out.println("验证时间："+ (end-start)/1000 + "s");
        System.out.println(result);
    }
}
