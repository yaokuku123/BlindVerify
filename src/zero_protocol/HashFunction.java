package zero_protocol;

import it.unisa.dia.gas.jpbc.Element;
import vote_init.VoteMethod;

import java.math.BigInteger;

/**
 * Created by ACER on 2020/5/22.
 */
public class HashFunction {
    public static Element h_a;
    public static Element h_b;
    public static BigInteger xor(BigInteger z1,BigInteger z2){
        char[] chars1 = z1.toString().toCharArray();
        char[] chars2 = z2.toString().toCharArray();
        int length = Math.max(chars1.length,chars2.length);
        char[] charsz1 = new char[length];
        char[] charsz2 = new char[length];
        if(chars1.length<length){
            int addzero = length-chars1.length;
            for (int i = 0; i < addzero; i++) {
                charsz1[i]='0';
            }
            for(int j = addzero;j<length;j++){
                charsz1[j]=chars1[j-addzero];
            }
            for (int k = 0; k < length; k++) {
                charsz2[k]=chars2[k];
            }
        }
        else if(chars2.length<length){
            int addzero = length-chars2.length;
            for (int i = 0; i < addzero; i++) {
                charsz2[i]='0';
            }
            for(int j = addzero;j<length;j++){
                charsz2[j]=chars2[j-addzero];
            }
            for (int k = 0; k < length; k++) {
                charsz1[k]=chars1[k];
            }
        }
        else{
            for (int k = 0; k < length; k++) {
                charsz1[k]=chars1[k];
                charsz2[k]=chars2[k];
            }
        }

        char[] temp = new char[length];
        for(int i=0;i<length;i++){
            if(charsz1[i]==charsz2[i])
                temp[i]='0';
            else
                temp[i]='1';
        }
        String s = String.valueOf(temp);
        BigInteger big = new BigInteger(s);
        return big;
    }

    public static Element hash(Element e){
        h_a = VoteMethod.e1;
        h_b = VoteMethod.e2;
        Element sum = h_a.mul(e).add(h_b);
        String str = sum.toString();
        String[] split = str.split(",");
        StringBuilder sb = new StringBuilder();
        for(String s : split){
            sb.append(s);
        }
        BigInteger big = new BigInteger(sb.toString());
        Element z = VoteMethod.z;
//        System.out.println(big);
        Element result = z.pow(big);
        return result;
    }
}
