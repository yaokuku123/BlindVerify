package main;

import BlindSignature.Sign;
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
public class Main2 {
    public static void main(String[] args) throws IOException, InterruptedException{
//        System.out.println(System.currentTimeMillis());
//        //调用cutFile()函数 传人参数分别为 （原大文件，切割后存放的小文件的路径，切割规定的内存大小）
//        cutFile("G:\\迅雷下载\\111.doc", "G:\\迅雷下载\\cuts", 1024);
//        System.out.println(System.currentTimeMillis());

        //转成后的时间的格式
        SimpleDateFormat sdff=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        StringBuilder sb = new StringBuilder();
        FileUtil fileUtil = new FileUtil();
        String filePath=FileUtil.currentWorkDir  +"\\cuts\\";


       long originFileSize = 1024 * 1024;// 1mb    初始文件大小
        int blockFileSize = 1024 * 16;// 32K     切割后的文件块大小
        int pieceFileSize = 1024 * 2;// 1K     切割后的文件片大小



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


        //存放所有切割好的文件块
        ArrayList<File> partFiles = FileUtil.getDirFiles(filePath,
                ".part");
        Collections.sort(partFiles, new FileUtil.FileComparator());
//
        //将文件块在切分为文件片
        for (int i=0;i<partFiles.size();i++){
            File partFile=partFiles.get(i);
            partFile.length();
            fileUtil.splitBySize(partFile, pieceFileSize,"."+(i+1)+"_piece");

        }
        Thread.sleep(10000);// 稍等10秒，等前面的小文件全都写完


        ArrayList<ArrayList<File>> pieceFilesAll=new ArrayList<ArrayList<File>>();
        for (int i=0;i<partFiles.size();i++){
            ArrayList<File> pieceFiles = FileUtil.getDirFiles(filePath,
                    "."+(i+1)+"_piece");
            Collections.sort(partFiles, new FileUtil.FileComparator());
            pieceFilesAll.add(pieceFiles);

        }


        //盲签名第二版
        System.out.println("初始化密码协议，时间："+sdff.format(new Date(System.currentTimeMillis())));
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
        //Element u = pairing.getG1().newRandomElement().getImmutable();
        Element v = g.powZn(x);

        //签名阶段
        ArrayList<Element> signLists=new ArrayList<>();
        ArrayList<Element> uLists=new ArrayList<>();

        System.out.println("开始签名，时间："+sdff.format(new Date(System.currentTimeMillis())));

        //生成U
        for(int i=0;i<pieceFilesAll.get(0).size();i++){
            Element u = pairing.getG1().newRandomElement().getImmutable();
            uLists.add(u);
        }




        for (int i=0;i<pieceFilesAll.size();i++){
            int len=pieceFilesAll.get(i).size();
            File partFile=pieceFilesAll.get(i).get(0);
            byte[] bytesByFile=fileUtil.getBytes(partFile);
            BigInteger mb=new BigInteger(bytesByFile);
            Element uContinuedProduct=uLists.get(0).pow(mb);
            for (int j=1;j<len;j++) {

                BigInteger mb1=new BigInteger(fileUtil.getBytes(pieceFilesAll.get(i).get(j)));
                uContinuedProduct=uContinuedProduct.mul(uLists.get(j).pow(mb1));
            }

            Element e=Sign.sign(g, uContinuedProduct, x, i+1);
            signLists.add(e);
            System.out.println(signLists.size());

        }

        System.out.println("签名完毕，时间："+sdff.format(new Date(System.currentTimeMillis())));


        //查询阶段
        //求sigma
        System.out.println("开始查询，时间："+sdff.format(new Date(System.currentTimeMillis())));
        ArrayList<Element> v_iLists=new ArrayList<>();
        Element v_i=pairing.getZr().newRandomElement().getImmutable();
        v_iLists.add(v_i);
        Element sigmasValues=sigma(signLists.get(0),v_i);

        for (int i=1;i<signLists.size();i++){
            Element v_i_1=pairing.getZr().newRandomElement().getImmutable();
            v_iLists.add(v_i_1);
            sigmasValues=sigmasValues.mul(sigma(signLists.get(i),v_i_1));

        }


        //求miu
        ArrayList<Element> miuLists=new ArrayList<>();
        for(int j=0;j<pieceFilesAll.get(0).size();j++){
            ArrayList<File> pieceFile=pieceFilesAll.get(0);
            File file=pieceFile.get(j);
            BigInteger mb = new BigInteger(fileUtil.getBytes(file));
            v_i=v_iLists.get(0);
            Element sum=v_i.mul(mb);
            for (int i=1;i<pieceFilesAll.size();i++){
                File file1=pieceFilesAll.get(i).get(j);
                BigInteger mb1 = new BigInteger(fileUtil.getBytes(file1));
                sum=sum.add(v_iLists.get(i).mul(mb1));

            }
            miuLists.add(sum);

        }


        System.out.println("查询完毕，时间："+sdff.format(new Date(System.currentTimeMillis())));

        //

        //开始验证
        System.out.println("开始验证，时间："+sdff.format(new Date(System.currentTimeMillis())));
        Element pairing1 = pairing.pairing(sigmasValues, g);   //e(u,v)^ab
        Element v_i_1=v_iLists.get(0);
        Element hashValue=g.pow(BigInteger.valueOf(HashUtil.javaDefaultHash(String.valueOf(1)))).powZn(v_i_1);
        for (int i=1;i<signLists.size();i++){
            Element v_i_i=v_iLists.get(i);
            hashValue=hashValue.mul(g.pow(BigInteger.valueOf(HashUtil.javaDefaultHash(String.valueOf(i+1)))).powZn(v_i_i));
        }

        Element miuValue=miuLists.get(0);
        Element uValue=uLists.get(0);
        Element miuValues=uValue.powZn(miuValue);
        for (int i=1;i<miuLists.size();i++){
            Element miu=miuLists.get(i);
            miuValues=miuValues.mul(uLists.get(i).powZn(miu));

        }

        Element pairing2 = pairing.pairing(hashValue.mul(miuValues), v);   //e(u,v)^ab

        boolean result=false;
        if(pairing1.equals(pairing2)){
            result=true;
        }else {
            result = false;
        }
        System.out.println("验证完毕，时间："+sdff.format(new Date(System.currentTimeMillis())));
        System.out.println("验证结果："+result);

    }
}
