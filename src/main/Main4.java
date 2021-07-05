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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;


/**
 * Created by ACER on 2020/5/18.
 */
public class Main4 {

    //1kb
    private static final int CAL_KB_UNIT = 1024;
    //1mb
    private static final int CAL_MB_UNIT = 1024 * 1024;

    public static void main(String[] args) throws IOException, InterruptedException {
        //初始化配置
        int originFileSize = 100 * CAL_MB_UNIT;// 100mb    初始文件大小
        int blockFileSize = 1 * CAL_MB_UNIT;// 切割后的文件块大小
        int pieceFileSize = 64 * CAL_KB_UNIT;// 切割后的文件片大小

        //转成后的时间的格式
        SimpleDateFormat sdff = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        FileUtil fileUtil = new FileUtil();
        String filePath = FileUtil.currentWorkDir + "\\cuts\\";

        long start = 0; //各阶段开始时间
        long end = 0; //各阶段结束时间
        long createFileTime = 0; //生成大文件时间
        long splitFileTime = 0; //拆分时间
        long signTime = 0; //签名时间
        long checkTime = 0; //查询时间
        long verifyTime = 0; //验证时间


        // 生成一个大文件
        start = System.nanoTime();
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < originFileSize; i++) {
            int number = random.nextInt(62);
            sb.append(str.charAt(number));
        }
        String fileName = filePath + "origin.myfile";
        FileUtil.write(fileName, sb.toString());
        end = System.nanoTime();
        createFileTime = (end - start) / 1000_000;


        //盲签名第三版
        /***************************************************************************************/
        //密码协议部分准备
        int rbits = 53;
        int qbits = 1024;
        TypeACurveGenerator pg = new TypeACurveGenerator(rbits, qbits);
        PairingParameters typeAParams = pg.generate();
        Pairing pairing = PairingFactory.getPairing(typeAParams);
        //初始化相关参数
        Element g = pairing.getG1().newRandomElement().getImmutable();     //生成生成元
        Element x = pairing.getZr().newRandomElement().getImmutable();
        Element v = g.powZn(x);
        //生成U
        ArrayList<ElementPowPreProcessing> uLists = new ArrayList<>();
        for (int i = 0; i < blockFileSize / pieceFileSize; i++) {
            ElementPowPreProcessing u = pairing.getG1().newRandomElement().getImmutable().getElementPowPreProcessing();
            uLists.add(u);
        }

        /***************************************************************************************/
        //签名阶段
        start = System.nanoTime();
        ArrayList<Element> signLists;
        Sign sign = new Sign();
        signLists = sign.sign(fileUtil, fileName, uLists, g, x, originFileSize, blockFileSize, pieceFileSize);
        end = System.nanoTime();
        signTime = (end - start) / 1000_000;


        /***************************************************************************************/
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
        miuLists = check.getMiuList(fileUtil, fileName, viLists, originFileSize, blockFileSize, pieceFileSize);
        end = System.nanoTime();
        checkTime = (end - start) / 1000_000;

        /***************************************************************************************/
        //开始验证
        start = System.nanoTime();
        Verify verify = new Verify();
        boolean result = verify.verifyResult(pairing, g, uLists, v, sigmasValues, viLists, signLists, miuLists);
        end = System.nanoTime();
        verifyTime = (end - start) / 1000_000;

        //********yqj*********
        //删除目录文件
//        FileUtil.deleteDir(filePath);

        //写入csv文件
        String csvPath = FileUtil.currentWorkDir + "\\data.csv";
        String data = originFileSize + "," + blockFileSize + "," + pieceFileSize + "," + createFileTime + "," + splitFileTime + "," + signTime + "," + checkTime + "," + verifyTime;
        FileUtil.writeToCsv(csvPath, data);
        //结果打印
        System.out.println("验证结果：" + result);
        System.out.println("初始文件大小：" + originFileSize + " 切割后的文件块大小：" + blockFileSize
                + " 切割后的文件片大小：" + pieceFileSize + " 生成大文件时间：" + createFileTime + " 拆分时间："
                + splitFileTime + " 签名时间：" + signTime + " 查询时间：" + checkTime + " 验证时间："
                + verifyTime + " 单位：ms");
    }
}
