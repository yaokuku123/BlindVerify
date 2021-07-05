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
 * FileName: MainForDraw
 * Author: yaoqijun
 * Date: 2021/6/23 16:19
 */
public class MainForDraw {
    public static void main(String[] args) {

        long start = 0; //各阶段开始时间
        long end = 0; //各阶段结束时间
        long signTime = 0; //签名时间
        long checkTime = 0; //查询时间
        long verifyTime = 0; //验证时间

        String filePath = "D:\\study\\code\\test\\zerotrust-demo\\uploadFile\\multichain-2.0-latest.tar.gz";
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
        start = System.nanoTime();
        ArrayList<Element> signLists;
        Sign sign = new Sign();
        FileUtil fileUtil = new FileUtil();
        signLists = sign.sign(fileUtil, filePath, uLists, g, x, originFileSize, blockFileSize, pieceFileSize);
        end = System.nanoTime();
        signTime = end - start;

        //查询阶段
        start = System.nanoTime();
        Check check = new Check();
        //求viLists
        ArrayList<Element> viLists;
        viLists = check.getViList(pairing, signLists);
        //求sigma
        Element sigmasValues = check.getSigh(pairing, signLists, viLists);
        //求miu
        ArrayList<Element> miuLists;
        miuLists = check.getMiuList(fileUtil, filePath, viLists, originFileSize, blockFileSize, pieceFileSize);
        end = System.nanoTime();
        checkTime = end - start;

        //开始验证
        start = System.nanoTime();
        Verify verify = new Verify();
        boolean result = verify.verifyResult(pairing, g, uLists, v, sigmasValues, viLists, signLists, miuLists);
        end = System.currentTimeMillis();
        verifyTime = end - start;

        //写入csv文件
        String csvPath = FileUtil.currentWorkDir + "\\data.csv";
        String data = originFileSize + "," + blockFileSize + "," + pieceFileSize + "," + signTime + "," + checkTime + "," + verifyTime;
        FileUtil.writeToCsv(csvPath, data);
        //结果打印
        System.out.println("验证结果：" + result);
        System.out.println("初始文件大小：" + originFileSize + " 切割后的文件块大小：" + blockFileSize
                + " 切割后的文件片大小：" + pieceFileSize  + " 签名时间：" + signTime + " 查询时间：" + checkTime + " 验证时间："
                + verifyTime + " 单位：ms");
    }
}
