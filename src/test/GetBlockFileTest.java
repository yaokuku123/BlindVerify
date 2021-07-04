package test;

import util.FileUtil;

import java.io.File;
import java.util.Arrays;

/**
 * Copyright(C),2019-2021,XXX公司
 * FileName: GetBlockFileTest
 * Author: yaoqijun
 * Date: 2021/6/23 10:36
 */
public class GetBlockFileTest {
    public static void main(String[] args) {

        //ok

        String filePath = "D:\\study\\code\\test\\zerotrust-demo\\uploadFile\\multichain-2.0-latest.tar.gz";
        //初始化配置
        int originFileSize = (int) new File(filePath).length(); //获取文件大小
        int pieceFileSize = 64; //定义片大小
        int pieceFileCount = 128; //定义片数量
        int blockFileSize = pieceFileCount * pieceFileSize; //计算获取块大小
        int blockFileCount = originFileSize / blockFileSize; //获取块数量

        FileUtil fileUtil = new FileUtil();
        byte[] bytes = fileUtil.getBytes(filePath, 0, blockFileSize);
        System.out.println(Arrays.toString(bytes));
        System.out.println("size: " + bytes.length);
    }
}
