package com.linktech.saihub.manager.wallet.btc;


import com.linktech.saihub.entity.wallet.bean.UnspentOutput;

import java.math.BigDecimal;
import java.util.List;

/**
 * 旷工费计算
 */
public class FeeUtil {

    //    private final static long DUST_THRESHOLD = 2730;
    private final static long DUST_THRESHOLD = 0;

    //普通 bytes = input*148+34*out+10
    public static long getFeeNormal(String amountStr, List<UnspentOutput> utxoList, String tokenName) {
        long bytes = 0;
        BigDecimal bigDecimal = new BigDecimal(amountStr);
        BigDecimal amountBigD = bigDecimal.multiply(new BigDecimal(10).pow(8));
        long amount = amountBigD.longValue();
        Long utxoAmount = 0L;   //utxo数量
        long changeAmount = 0L; //找零
        int inputSize = 0;  //input数量
        for (UnspentOutput utxo : utxoList) {
            if (utxoAmount >= amount) {
                break;
            } else {
                inputSize++;
                utxoAmount += utxo.getValue();
            }
        }
        changeAmount = utxoAmount - (amount + (inputSize * 148 + 34 * (tokenName.equals("BTC") ? 1 : 2)) + 10);
        bytes = inputSize * 148 + 34 * ((changeAmount > DUST_THRESHOLD ? (tokenName.equals("BTC") ? 2 : 3) : 1) + 10);
        return bytes;
    }

    //    隔离见证  size_原始= input*210+34*out+12
//    size_剥离 = size_原始 ­ input * 134-2 (原始数据减去见证部分，不打折的交易字节)
//    size_实际 = size_剥离 + (input * 134)/4 (向上取整)
    public static long getFeeSegWit(String amountStr, List<UnspentOutput> utxoList, String tokenName) {
        long bytesOriginal = 0;//原始
        long bytesPeel = 0;//剥离
        long bytesReality = 0;//实际
        BigDecimal bigDecimal = new BigDecimal(amountStr);
        BigDecimal amountBigD = bigDecimal.multiply(new BigDecimal(10).pow(8));
        long amount = amountBigD.longValue();
        Long utxoAmount = 0L;   //utxo数量
        long changeAmount; //找零
        int inputSize = 0;  //input数量
        for (UnspentOutput utxo : utxoList) {
            if (utxoAmount >= amount) {
                break;
            } else {
                inputSize++;
                utxoAmount += utxo.getValue();
            }
        }
        changeAmount = utxoAmount - (amount + (inputSize * 210L + 34 * (tokenName.equals("BTC") ? 1 : 2) + 12));
        bytesOriginal = (inputSize * 210L) + 34 * ((changeAmount > DUST_THRESHOLD ? (tokenName.equals("BTC") ? 2 : 3) : 1)) + 10;
        bytesPeel = bytesOriginal - inputSize * 134L - 2;
        long inputSizeBytes = new BigDecimal((inputSize * 134)).divide(new BigDecimal(4), BigDecimal.ROUND_UP).longValue();
        bytesReality = bytesPeel + inputSizeBytes;
        return bytesReality;
    }


}
