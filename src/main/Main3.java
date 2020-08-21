package main;

import BlindSignature.Check;
import BlindSignature.Sign;
import BlindSignature.Verify;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.ElementPowPreProcessing;
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
public class Main3 {

    //1kb
    private static final int CAL_KB_UNIT = 1024;
    //1mb
    private static final int CAL_MB_UNIT = 1024 * 1024;

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


        //long originFileSize = 1024 * 1024*100;// 100mb    初始文件大小
        //int blockFileSize = 1024 * 1024;// 1mb     切割后的文件块大小
        //int pieceFileSize = 1024 * 64;// 64K     切割后的文件片大小

        long originFileSize = 100 * CAL_MB_UNIT;// 100mb    初始文件大小
        int blockFileSize = 0;// 切割后的文件块大小
        int pieceFileSize = 0;// 切割后的文件片大小

        long start = 0; //各阶段开始时间
        long end = 0; //各阶段结束时间
        long createFileTime = 0; //生成大文件时间
        long splitFileTime = 0; //拆分时间
        long signTime = 0; //签名时间
        long checkTime = 0; //查询时间
        long verifyTime = 0; //验证时间

        for (int blockSpace = 1; blockSpace <= 1; blockSpace++) {
            blockFileSize = CAL_MB_UNIT * blockSpace;
            for (int pieceSpace = 64; pieceSpace <= 64; pieceSpace++) {
                pieceFileSize = CAL_KB_UNIT * pieceSpace;
                // 生成一个大文件
                //System.out.println("开始生成大文件，时间："+sdff.format(new Date(System.currentTimeMillis())));
                start = System.nanoTime();
                for (int i = 0; i < originFileSize; i++) {
                    sb.append("A");
                }
                String fileName = filePath + "origin.myfile";
                FileUtil.write(fileName, sb.toString());
                //System.out.println("文件生成完毕，时间："+sdff.format(new Date(System.currentTimeMillis())));
                end = System.nanoTime();
                createFileTime = (end - start) / 1000_000;


                // 将origin.myfile拆分
                //System.out.println("开始拆分文件，时间："+sdff.format(new Date(System.currentTimeMillis())));
                start = System.nanoTime();
                fileUtil.splitBySize(fileName, blockFileSize, ".part");
                //Thread.sleep(10000);// 稍等10秒，等前面的小文件全都写完
                //System.out.println("文件拆分完毕，时间："+sdff.format(new Date(System.currentTimeMillis())));
                end = System.nanoTime();
                splitFileTime = (end - start) / 1000_000;


                //存放所有切割好的文件块
                ArrayList<File> partFiles = FileUtil.getDirFiles(filePath,
                        ".part");
                Collections.sort(partFiles, new FileUtil.FileComparator());
                //将文件块在切分为文件片
                for (int i = 0; i < partFiles.size(); i++) {
                    File partFile = partFiles.get(i);
                    partFile.length();
                    fileUtil.splitBySize(partFile, pieceFileSize, "." + (i + 1) + "_piece");

                }
                //Thread.sleep(10000);// 稍等10秒，等前面的小文件全都写完
                ArrayList<ArrayList<File>> pieceFilesAll = new ArrayList<ArrayList<File>>();
                for (int i = 0; i < partFiles.size(); i++) {
                    ArrayList<File> pieceFiles = FileUtil.getDirFiles(filePath,
                            "." + (i + 1) + "_piece");
                    Collections.sort(partFiles, new FileUtil.FileComparator());
                    pieceFilesAll.add(pieceFiles);

                }


                //盲签名第三版
                //System.out.println("初始化密码协议，时间："+sdff.format(new Date(System.currentTimeMillis())));
                //long timeStamp = System.currentTimeMillis();
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
                for (int i = 0; i < pieceFilesAll.get(0).size(); i++) {
                    ElementPowPreProcessing u = pairing.getG1().newRandomElement().getImmutable().getElementPowPreProcessing();
                    uLists.add(u);
                }

                /***************************************************************************************/
                //签名阶段
                //long sighStartTime=System.currentTimeMillis();
                //System.out.println("开始签名，时间："+sdff.format(new Date(sighStartTime)));
                start = System.nanoTime();
                ArrayList<Element> signLists;
                Sign sign = new Sign();
                signLists = sign.sign(pieceFilesAll, fileUtil, uLists, g, x);
                end = System.nanoTime();
                signTime = (end - start) / 1000_000;
                //long sighEndTime=System.currentTimeMillis();
                //System.out.println("签名完毕，时间："+sdff.format(new Date(sighEndTime)));
                //System.out.println("签名所用时间："+sdff.format(new Date(sighEndTime-sighStartTime)));

                /***************************************************************************************/
                //查询阶段
                //long checkStartTime=System.currentTimeMillis();
                //System.out.println("开始查询，时间："+sdff.format(new Date(checkStartTime)));
                start = System.nanoTime();
                Check check = new Check();
                //求viLists
                ArrayList<Element> viLists;
                viLists = check.getViList(pairing, signLists);
                //求sigma
                Element sigmasValues = check.getSigh(pairing, signLists, viLists);
                //求miu
                ArrayList<Element> miuLists;
                miuLists = check.getMiuList(pieceFilesAll, fileUtil, viLists);
                end = System.nanoTime();
                checkTime = (end - start) / 1000_000;
                //long checkEndTime=System.currentTimeMillis();
                //System.out.println("查询完毕，时间："+sdff.format(new Date(checkEndTime)));
                //System.out.println("查询所用时间："+sdff.format(new Date(checkEndTime-checkStartTime)));

                /***************************************************************************************/
                //开始验证
                //long verifyStartTime=System.currentTimeMillis();
                //System.out.println("开始验证，时间："+sdff.format(new Date(verifyStartTime)));
                start = System.nanoTime();
                Verify verify = new Verify();
                boolean result = verify.verifyResult(pairing, g, uLists, v, sigmasValues, viLists, signLists, miuLists);
                end = System.nanoTime();
                verifyTime = (end - start) / 1000_000;
                //long verifyEndTime=System.currentTimeMillis();
                //System.out.println("验证完毕，时间："+sdff.format(new Date(verifyEndTime)));
                //System.out.println("验证结果："+result+"  验证所用时间："+sdff.format(new Date(verifyEndTime-verifyStartTime)));

                //********yqj*********
                //删除目录文件
                FileUtil.deleteDir(filePath);

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
    }
}
