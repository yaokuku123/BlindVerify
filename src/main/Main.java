package main;

import BlindSignature.Sign;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.jpbc.PairingParameters;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.pairing.a.TypeACurveGenerator;
import util.FileUtil;
import util.HashUtil;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import static BlindSignature.Sign.miu;
import static BlindSignature.Sign.sigma;


/**
 * Created by ACER on 2020/5/18.
 */
public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
//        System.out.println(System.currentTimeMillis());
//        //调用cutFile()函数 传人参数分别为 （原大文件，切割后存放的小文件的路径，切割规定的内存大小）
//        cutFile("G:\\迅雷下载\\111.doc", "G:\\迅雷下载\\cuts", 1024);
//        System.out.println(System.currentTimeMillis());

        //转成后的时间的格式
        SimpleDateFormat sdff = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        StringBuilder sb = new StringBuilder();
        FileUtil fileUtil = new FileUtil();
        String filePath = FileUtil.currentWorkDir + "\\cuts\\";


        long originFileSize = 1024 * 25;// 50K    初始文件大小
        int blockFileSize = 1024 * 5;// 5K     切割后的文件块大小
        int pieceFileSize = 1024 * 1;// 1K     切割后的文件片大小



        System.out.println("开始生成大文件，时间："+sdff.format(new Date(System.currentTimeMillis())));
        // 生成一个大文件
        for (int i = 0; i < originFileSize; i++) {
            sb.append("A");
        }


        String fileName = filePath+"origin.myfile";
        System.out.println(FileUtil.write(fileName, sb.toString()));
        System.out.println("文件生成完毕，时间："+sdff.format(new Date(System.currentTimeMillis())));


        System.out.println("开始拆分文件，时间："+sdff.format(new Date(System.currentTimeMillis())));
        // 将origin.myfile拆分
        fileUtil.splitBySize(fileName, blockFileSize,".part");

        Thread.sleep(10000);// 稍等10秒，等前面的小文件全都写完
        System.out.println("文件拆分完毕，时间："+sdff.format(new Date(System.currentTimeMillis())));
//
//
//        //存放所有切割好的文件块
        ArrayList<File> partFiles = FileUtil.getDirFiles(filePath,
                ".part");
        Collections.sort(partFiles, new FileUtil.FileComparator());
//



        //盲签名第一版
        System.out.println("初始化密码协议，时间：" + sdff.format(new Date(System.currentTimeMillis())));
        long timeStamp = System.currentTimeMillis();


        //密码协议部分准备
        int rbits = 53;
        int qbits = 1024;
        TypeACurveGenerator pg = new TypeACurveGenerator(rbits, qbits);
        PairingParameters typeAParams = pg.generate();
        Pairing pairing = PairingFactory.getPairing(typeAParams);
        //初始化相关参数
        Element g = pairing.getG1().newRandomElement().getImmutable();     //生成生成元
        Element x = pairing.getZr().newRandomElement().getImmutable();
        Element u = pairing.getG1().newRandomElement().getImmutable();
        Element v = g.powZn(x);


        //签名阶段
        ArrayList<Element> signLists = new ArrayList<>();

        System.out.println("开始签名，时间：" + sdff.format(new Date(System.currentTimeMillis())));
        for (int i = 0; i < partFiles.size(); i++) {
            File partFile = partFiles.get(i);
            byte[] bytesByFile = fileUtil.getBytes(partFile);
            Element e = Sign.sign(g, u, x, bytesByFile, i + 1);
            signLists.add(e);
        }
        System.out.println("签名完毕，时间：" + sdff.format(new Date(System.currentTimeMillis())));

        //查询阶段
        System.out.println("开始查询，时间：" + sdff.format(new Date(System.currentTimeMillis())));
        ArrayList<Element> v_iLists = new ArrayList<>();
        Element v_i = pairing.getZr().newRandomElement().getImmutable();
        v_iLists.add(v_i);
        Element sigmasValue = sigma(signLists.get(0), v_i);
        Element mulsValue = miu(v_i, fileUtil.getBytes(partFiles.get(0)));
        for (int i = 1; i < signLists.size(); i++) {
            Element v_i_1 = pairing.getZr().newRandomElement().getImmutable();
            v_iLists.add(v_i_1);
            sigmasValue = sigmasValue.mul(sigma(signLists.get(i), v_i_1));
            mulsValue = mulsValue.add(miu(v_i_1, fileUtil.getBytes(partFiles.get(i))));
        }
        System.out.println("查询完毕，时间：" + sdff.format(new Date(System.currentTimeMillis())));


        //开始验证
        System.out.println("开始验证，时间：" + sdff.format(new Date(System.currentTimeMillis())));
        Element pairing1 = pairing.pairing(sigmasValue, g);   //e(u,v)^ab
        Element v_i_1 = v_iLists.get(0);
        Element hashValue = g.pow(BigInteger.valueOf(HashUtil.javaDefaultHash(String.valueOf(1)))).powZn(v_i_1);
        for (int i = 1; i < signLists.size(); i++) {
            Element v_i_i = v_iLists.get(i);
            hashValue = hashValue.mul(g.pow(BigInteger.valueOf(HashUtil.javaDefaultHash(String.valueOf(i + 1)))).powZn(v_i_i));
        }
        Element pairing2 = pairing.pairing(hashValue.mul(u.powZn(mulsValue)), v);   //e(u,v)^ab

        boolean result = false;
        if (pairing1.equals(pairing2)) {
            result = true;
        } else {
            result = false;
        }
        System.out.println("验证完毕，时间：" + sdff.format(new Date(System.currentTimeMillis())));
        System.out.println("验证结果：" + result);


    }


}
