package com.linktech.saihub.manager.wallet.transfer;


import com.linktech.saihub.R;
import com.linktech.saihub.app.Constants;
import com.linktech.saihub.app.SaiHubApplication;
import com.linktech.saihub.db.bean.WalletBean;
import com.linktech.saihub.db.utils.ChildAddressDaoUtil;
import com.linktech.saihub.entity.wallet.bean.UnspentOutput;
import com.linktech.saihub.entity.wallet.bean.transaction.TransferSendBean;
import com.linktech.saihub.manager.AES;
import com.linktech.saihub.manager.wallet.btc.ParamsManagerBtc;
import com.linktech.saihub.manager.wallet.btc.btctransfer.ByteUtil;
import com.linktech.saihub.manager.wallet.exception.Messages;
import com.linktech.saihub.manager.wallet.exception.TokenException;
import com.linktech.saihub.util.LogUtils;
import com.linktech.saihub.util.NumberCountUtils;
import com.linktech.saihub.util.walutils.NumericUtil;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.TransactionOutPoint;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.core.UnsafeByteArrayOutputStream;
import org.bitcoinj.core.Utils;
import org.bitcoinj.core.VarInt;
import org.bitcoinj.script.ScriptBuilder;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * BTC 交易
 */
public class BitcoinTransaction {

    // 2730 sat
//    private final static long DUST_THRESHOLD = 2730;
    private final static long DUST_THRESHOLD = 0;
    private long locktime = 0;


    /**
     * 通过地址获得私钥
     *
     * @param params
     * @param address
     * @return
     * @throws Exception
     */
    private ECKey getECKey(NetworkParameters params, String address, WalletBean walletBean) throws Exception {
        ECKey ecKey = null;
        String childAddressPrivateKey = ChildAddressDaoUtil.getChildAddressPrivateKey(address, walletBean.getId());
        if (childAddressPrivateKey == null) {
            throw new Exception(SaiHubApplication.Companion.getInstance().getString(R.string.sign_error));
        }
        String privateKey = AES.decrypt(childAddressPrivateKey);
        if (privateKey.length() == 51 || privateKey.length() == 52) {
            DumpedPrivateKey dumpedPrivateKey = DumpedPrivateKey.fromBase58(params, privateKey);
            ecKey = dumpedPrivateKey.getKey();
        } else {
            BigInteger privKey = Base58.decodeToBigInteger(privateKey);
            ecKey = ECKey.fromPrivate(privKey);
        }
        return ecKey;
    }


    /**
     * 隔离见证签名交易
     *
     * @param transferBean
     * @param currentWal
     * @return
     * @throws Exception
     */
    public String signSegWitTransaction(TransferSendBean transferBean, WalletBean currentWal) throws Exception {

        String gasLimit = transferBean.getGasLimit();
        String gasPrice = transferBean.getGasPrice();
        BigDecimal bigDecimalGad = NumberCountUtils.mulD(gasLimit, gasPrice);

        long fee = bigDecimalGad.longValue();//矿工费
        long changeAmount = 0L; //找零
        Long utxoAmount = 0L;   //utxo数量
        BigDecimal bigDecimal = new BigDecimal(transferBean.getMoneyNumber());
        BigDecimal amountBigD = bigDecimal.multiply(new BigDecimal(10).pow(8));
        long amount = amountBigD.longValue();

        List<UnspentOutput> utxos = transferBean.getUtxoList();
        NetworkParameters params = ParamsManagerBtc.getParams();

        List<UnspentOutput> needUtxos = new ArrayList<>();
        //获取未消费列表
        if (utxos == null || utxos.size() == 0) {
            throw new Exception(Objects.requireNonNull(SaiHubApplication.Companion.getInstance()).getString(R.string.error_tip_net));
        }
        //遍历未花费列表，组装合适的item
        for (UnspentOutput utxo : utxos) {
            if (utxoAmount >= (amount + fee)) {
                break;
            } else {
                needUtxos.add(utxo);
                utxoAmount += utxo.getValue();
            }
        }
        //余额判断
        if (changeAmount < 0) {
            throw new Exception(SaiHubApplication.Companion.getInstance().getString(R.string.balance_not_sufficient));
        }
        String changeAddressStr = ChildAddressDaoUtil.getChangeAddress(currentWal);
        if (changeAddressStr == null) {
            throw new Exception(SaiHubApplication.Companion.getInstance().getString(R.string.change_address_get_error));
        }
        //消费列表总金额 - 已经转账的金额 - 手续费 就等于需要返回给自己的金额了
        changeAmount = utxoAmount - (amount + fee);

        ///////////////////////////////////////////
        boolean hasChange = false;
        NetworkParameters network = ParamsManagerBtc.getParams();

        Address changeAddress = Address.fromString(network, changeAddressStr);

        Address toAddress = Address.fromString(network, transferBean.getToAddress());

        byte[] targetScriptPubKey = ScriptBuilder.createOutputScript(toAddress).getProgram();

        byte[] changeScriptPubKey = ScriptBuilder.createOutputScript(changeAddress).getProgram();

        byte[] hashPrevouts;
        byte[] hashOutputs;
        byte[] hashSequence;

        try {
            // calc hash prevouts
            UnsafeByteArrayOutputStream stream = new UnsafeByteArrayOutputStream();
            for (UnspentOutput outputsBean : needUtxos) {
                TransactionOutPoint outPoint = new TransactionOutPoint(params, outputsBean.getTxOutputN(), Sha256Hash.wrap(outputsBean.getTxHashBigEndian()));
                outPoint.bitcoinSerialize(stream);
            }

            hashPrevouts = Sha256Hash.hashTwice(stream.toByteArray());

            // calc hash outputs
            stream = new UnsafeByteArrayOutputStream();

            TransactionOutput targetOutput = new TransactionOutput(params, null, Coin.valueOf(amount), toAddress);
            targetOutput.bitcoinSerialize(stream);

            if (changeAmount >= DUST_THRESHOLD) {
                hasChange = true;
                TransactionOutput changeOutput = new TransactionOutput(params, null, Coin.valueOf(changeAmount), changeAddress);
                changeOutput.bitcoinSerialize(stream);
            }

//
//      Utils.uint64ToByteStreamLE(BigInteger.valueOf(amount), stream);
//      stream.write(new VarInt(targetScriptPubKey.length).encode());
//      stream.write(targetScriptPubKey);
//      Utils.uint64ToByteStreamLE(BigInteger.valueOf(changeAmount), stream);
//      stream.write(new VarInt(changeScriptPubKey.length).encode());
//      stream.write(changeScriptPubKey);

            hashOutputs = Sha256Hash.hashTwice(stream.toByteArray());

            // calc hash sequence
            stream = new UnsafeByteArrayOutputStream();

            for (UnspentOutput outputsBean : needUtxos) {
                Utils.uint32ToByteStreamLE(outputsBean.getSequence(), stream);
            }
            hashSequence = Sha256Hash.hashTwice(stream.toByteArray());

            // calc witnesses and redemScripts
            List<byte[]> witnesses = new ArrayList<>();
            List<String> redeemScripts = new ArrayList<>();
            int address_type = currentWal.getChildAddressType();
//            int address_type = MMKVManager.getInstance().mmkv().decodeInt(currentWal.getId() + Constants.CHILD_ADDRESS_TYPE, Constants.CHILD_ADDRESS_SWIGET);
            for (int i = 0; i < needUtxos.size(); i++) {
                UnspentOutput outputsBean = needUtxos.get(i);

                ECKey ecKey = getECKey(params, outputsBean.getAddress(address_type), currentWal);
                String redeemScript;
                if (address_type == Constants.CHILD_ADDRESS_NATIVE) {
                    //001469925fbf6a684f72ed2057b5bb765d95af473799
//                    redeemScript = ScriptBuilder.createP2WPKHOutputScript(ecKey.getPubKeyHash()).toString();
                    redeemScript = String.format("0014%s", NumericUtil.bytesToHex(ecKey.getPubKeyHash()));
                } else {
                    redeemScript = String.format("0014%s", NumericUtil.bytesToHex(ecKey.getPubKeyHash()));
                }
                redeemScripts.add(redeemScript);

                // calc outpoint
                stream = new UnsafeByteArrayOutputStream();
                TransactionOutPoint txOutPoint = new TransactionOutPoint(params, outputsBean.getTxOutputN(), Sha256Hash.wrap(outputsBean.getTxHashBigEndian()));
                txOutPoint.bitcoinSerialize(stream);
                byte[] outpoint = stream.toByteArray();

                // calc scriptCode
                byte[] scriptCode = NumericUtil.hexToBytes(String.format("0x1976a914%s88ac", NumericUtil.bytesToHex(ecKey.getPubKeyHash())));

                // before sign
                stream = new UnsafeByteArrayOutputStream();
                Utils.uint32ToByteStreamLE(2L, stream);
                stream.write(hashPrevouts);
                stream.write(hashSequence);
                stream.write(outpoint);
                stream.write(scriptCode);
                Utils.uint64ToByteStreamLE(BigInteger.valueOf(outputsBean.getValue()), stream);
                Utils.uint32ToByteStreamLE(outputsBean.getSequence(), stream);
                stream.write(hashOutputs);
                Utils.uint32ToByteStreamLE(locktime, stream);
                // hashType 1 = all
                Utils.uint32ToByteStreamLE(1L, stream);

                byte[] hashPreimage = stream.toByteArray();
                byte[] sigHash = Sha256Hash.hashTwice(hashPreimage);
                ECKey.ECDSASignature signature = ecKey.sign(Sha256Hash.wrap(sigHash));
                byte hashType = 0x01;
                // witnesses
                byte[] sig = ByteUtil.concat(signature.encodeToDER(), new byte[]{hashType});
                witnesses.add(sig);
            }

            // the second stream is used to calc the traditional txhash
            UnsafeByteArrayOutputStream[] serialStreams = new UnsafeByteArrayOutputStream[]{
                    new UnsafeByteArrayOutputStream(), new UnsafeByteArrayOutputStream()
            };
            for (int idx = 0; idx < 2; idx++) {
                stream = serialStreams[idx];
                Utils.uint32ToByteStreamLE(2L, stream); // version
                if (idx == 0) {
                    stream.write(0x00); // maker
                    stream.write(0x01); // flag
                }
                // inputs
                stream.write(new VarInt(needUtxos.size()).encode());
                for (int i = 0; i < needUtxos.size(); i++) {
                    UnspentOutput outputsBean = needUtxos.get(i);
                    stream.write(NumericUtil.reverseBytes(NumericUtil.hexToBytes(outputsBean.getTxHashBigEndian())));
                    Utils.uint32ToByteStreamLE(outputsBean.getTxOutputN(), stream);

                    // the length of byte array that follows, and this length is used by OP_PUSHDATA1
                    stream.write(0x17);
                    // the length of byte array that follows, and this length is used by cutting array
                    stream.write(0x16);
                    stream.write(NumericUtil.hexToBytes(redeemScripts.get(i)));
                    Utils.uint32ToByteStreamLE(outputsBean.getSequence(), stream);
                }
                // outputs
                // outputs size
                int outputSize = hasChange ? 2 : 1;
                stream.write(new VarInt(outputSize).encode());
                Utils.uint64ToByteStreamLE(BigInteger.valueOf(amount), stream);
                stream.write(new VarInt(targetScriptPubKey.length).encode());
                stream.write(targetScriptPubKey);
                if (hasChange) {
                    Utils.uint64ToByteStreamLE(BigInteger.valueOf(changeAmount), stream);
                    stream.write(new VarInt(changeScriptPubKey.length).encode());
                    stream.write(changeScriptPubKey);
                }

                // the first stream is used to calc the segwit hash
                if (idx == 0) {
                    for (int i = 0; i < witnesses.size(); i++) {
                        ECKey ecKey = getECKey(params, needUtxos.get(i).getAddress(address_type), currentWal);
                        byte[] wit = witnesses.get(i);
                        stream.write(new VarInt(2).encode());
                        stream.write(new VarInt(wit.length).encode());
                        stream.write(wit);
                        stream.write(new VarInt(ecKey.getPubKey().length).encode());
                        stream.write(ecKey.getPubKey());
                    }
                }

                Utils.uint32ToByteStreamLE(locktime, stream);
            }
            byte[] signed = serialStreams[0].toByteArray();
            String signedHex = NumericUtil.bytesToHex(signed);
            String wtxID = NumericUtil.bytesToHex(Sha256Hash.hashTwice(signed));
            wtxID = NumericUtil.beBigEndianHex(wtxID);
            String txHash = NumericUtil.bytesToHex(Sha256Hash.hashTwice(serialStreams[1].toByteArray()));
//            txHash = NumericUtil.beBigEndianHex(txHash);
            return signedHex;
        } catch (IOException ex) {
            throw new TokenException("OutputStream error");
        }
    }


    //隔离见证U交易签名
    public String signUsdtSegWitTransaction(TransferSendBean transferBean, WalletBean currentWal) throws Exception {

        NetworkParameters network = ParamsManagerBtc.getParams();

        String gasLimit = transferBean.getGasLimit();
        String gasPrice = transferBean.getGasPrice();
        BigDecimal bigDecimalGad = NumberCountUtils.mulD(gasLimit, gasPrice);
        long fee = bigDecimalGad.longValue();//矿工费

        //转账数量
        BigDecimal bigDecimal = new BigDecimal(transferBean.getMoneyNumber());
        BigDecimal amountBigD = bigDecimal.multiply(new BigDecimal(10).pow(8));
        Long amount = amountBigD.longValue();

        long totalAmount = 0L;

        long needAmount = 546L;

        List<UnspentOutput> needUtxos = new ArrayList<>();

        //获取未消费列表
        if (transferBean.getUtxoList() == null || transferBean.getUtxoList().size() == 0) {
            throw new Exception(SaiHubApplication.Companion.getInstance().getString(R.string.error_tip_net));
        }

        //遍历未花费列表，组装合适的item
        for (UnspentOutput utxo : transferBean.getUtxoList()) {
            if (totalAmount >= (needAmount + fee)) {
                break;
            } else {
                needUtxos.add(utxo);
                totalAmount += utxo.getValue();
            }
        }

        if (totalAmount < needAmount) {
            throw new TokenException(Messages.INSUFFICIENT_FUNDS);
        }

        String changeAddressStr = ChildAddressDaoUtil.getChangeAddress(currentWal);
        if (changeAddressStr == null) {
            throw new Exception(SaiHubApplication.Companion.getInstance().getString(R.string.change_address_get_error));
        }
        long changeAmount = totalAmount - (needAmount + fee);

        ///////////////////////////////////////////
        boolean hasChange = false;
        Address changeAddress = Address.fromString(network, changeAddressStr);

        Address toAddress = Address.fromString(network, transferBean.getToAddress());

        byte[] targetScriptPubKey = ScriptBuilder.createOutputScript(toAddress).getProgram();

        byte[] changeScriptPubKey = ScriptBuilder.createOutputScript(changeAddress).getProgram();

        byte[] hashPrevouts;
        byte[] hashOutputs;
        byte[] hashSequence;

        try {
            // calc hash prevouts
            UnsafeByteArrayOutputStream stream = new UnsafeByteArrayOutputStream();
            for (UnspentOutput outputsBean : needUtxos) {
                TransactionOutPoint outPoint = new TransactionOutPoint(network, outputsBean.getTxOutputN(), Sha256Hash.wrap(outputsBean.getTxHashBigEndian()));
                outPoint.bitcoinSerialize(stream);
            }

            hashPrevouts = Sha256Hash.hashTwice(stream.toByteArray());

            // calc hash outputs
            stream = new UnsafeByteArrayOutputStream();


            String usdtHex = "6a146f6d6e69" + String.format("%016x", 31) + String.format("%016x", amount);
            byte[] usdtScript = Utils.HEX.decode(usdtHex);
            TransactionOutput targetOutputUsdt = new TransactionOutput(network, null, Coin.valueOf(0L), usdtScript);
            targetOutputUsdt.bitcoinSerialize(stream);

            TransactionOutput targetOutput = new TransactionOutput(network, null, Coin.valueOf(needAmount), toAddress);
            targetOutput.bitcoinSerialize(stream);

            if (changeAmount >= DUST_THRESHOLD) {
                hasChange = true;
                TransactionOutput changeOutput = new TransactionOutput(network, null, Coin.valueOf(changeAmount), changeAddress);
                changeOutput.bitcoinSerialize(stream);
            }

            hashOutputs = Sha256Hash.hashTwice(stream.toByteArray());

            // calc hash sequence
            stream = new UnsafeByteArrayOutputStream();

            for (UnspentOutput outputsBean : needUtxos) {
                Utils.uint32ToByteStreamLE(outputsBean.getSequence(), stream);
            }
            hashSequence = Sha256Hash.hashTwice(stream.toByteArray());

            // calc witnesses and redemScripts
            List<byte[]> witnesses = new ArrayList<>();
            List<String> redeemScripts = new ArrayList<>();
            int address_type = currentWal.getChildAddressType();
            for (int i = 0; i < needUtxos.size(); i++) {
                UnspentOutput outputsBean = needUtxos.get(i);

                ECKey ecKey = getECKey(network, outputsBean.getAddress(address_type), currentWal);
                String redeemScript = String.format("0014%s", NumericUtil.bytesToHex(ecKey.getPubKeyHash()));
                redeemScripts.add(redeemScript);

                // calc outpoint
                stream = new UnsafeByteArrayOutputStream();
                TransactionOutPoint txOutPoint = new TransactionOutPoint(network, outputsBean.getTxOutputN(), Sha256Hash.wrap(outputsBean.getTxHashBigEndian()));
                txOutPoint.bitcoinSerialize(stream);
                byte[] outpoint = stream.toByteArray();

                // calc scriptCode
                byte[] scriptCode = NumericUtil.hexToBytes(String.format("0x1976a914%s88ac", NumericUtil.bytesToHex(ecKey.getPubKeyHash())));

                // before sign
                stream = new UnsafeByteArrayOutputStream();
                Utils.uint32ToByteStreamLE(2L, stream);
                stream.write(hashPrevouts);
                stream.write(hashSequence);
                stream.write(outpoint);
                stream.write(scriptCode);
                Utils.uint64ToByteStreamLE(BigInteger.valueOf(outputsBean.getValue()), stream);
                Utils.uint32ToByteStreamLE(outputsBean.getSequence(), stream);
                stream.write(hashOutputs);
                Utils.uint32ToByteStreamLE(locktime, stream);
                // hashType 1 = all
                Utils.uint32ToByteStreamLE(1L, stream);

                byte[] hashPreimage = stream.toByteArray();
                byte[] sigHash = Sha256Hash.hashTwice(hashPreimage);
                ECKey.ECDSASignature signature = ecKey.sign(Sha256Hash.wrap(sigHash));
                byte hashType = 0x01;
                // witnesses
                byte[] sig = ByteUtil.concat(signature.encodeToDER(), new byte[]{hashType});
                witnesses.add(sig);
            }

            // the second stream is used to calc the traditional txhash
            UnsafeByteArrayOutputStream[] serialStreams = new UnsafeByteArrayOutputStream[]{
                    new UnsafeByteArrayOutputStream(), new UnsafeByteArrayOutputStream()
            };
            for (int idx = 0; idx < 2; idx++) {
                stream = serialStreams[idx];
                Utils.uint32ToByteStreamLE(2L, stream); // version
                if (idx == 0) {
                    stream.write(0x00); // maker
                    stream.write(0x01); // flag
                }
                // inputs
                stream.write(new VarInt(needUtxos.size()).encode());
                for (int i = 0; i < needUtxos.size(); i++) {
                    UnspentOutput outputsBean = needUtxos.get(i);
                    stream.write(NumericUtil.reverseBytes(NumericUtil.hexToBytes(outputsBean.getTxHashBigEndian())));
                    Utils.uint32ToByteStreamLE(outputsBean.getTxOutputN(), stream);

                    // the length of byte array that follows, and this length is used by OP_PUSHDATA1
                    stream.write(0x17);
                    // the length of byte array that follows, and this length is used by cutting array
                    stream.write(0x16);
                    stream.write(NumericUtil.hexToBytes(redeemScripts.get(i)));
                    Utils.uint32ToByteStreamLE(outputsBean.getSequence(), stream);
                }

                // outputs
                // outputs size
                int outputSize = hasChange ? 3 : 2;
                stream.write(new VarInt(outputSize).encode());

                Utils.uint64ToByteStreamLE(BigInteger.valueOf(0L), stream);
                stream.write(new VarInt(usdtScript.length).encode());
                stream.write(usdtScript);

                Utils.uint64ToByteStreamLE(BigInteger.valueOf(needAmount), stream);
                stream.write(new VarInt(targetScriptPubKey.length).encode());
                stream.write(targetScriptPubKey);

                if (hasChange) {
                    Utils.uint64ToByteStreamLE(BigInteger.valueOf(changeAmount), stream);
                    stream.write(new VarInt(changeScriptPubKey.length).encode());
                    stream.write(changeScriptPubKey);
                }

                // the first stream is used to calc the segwit hash
                if (idx == 0) {
                    for (int i = 0; i < witnesses.size(); i++) {
                        ECKey ecKey = getECKey(network, needUtxos.get(i).getAddress(address_type), currentWal);
                        byte[] wit = witnesses.get(i);
                        stream.write(new VarInt(2).encode());
                        stream.write(new VarInt(wit.length).encode());
                        stream.write(wit);
                        stream.write(new VarInt(ecKey.getPubKey().length).encode());
                        stream.write(ecKey.getPubKey());
                    }
                }

                Utils.uint32ToByteStreamLE(locktime, stream);
            }
            byte[] signed = serialStreams[0].toByteArray();
            String signedHex = NumericUtil.bytesToHex(signed);
            LogUtils.e("http==  " + signedHex);
            return signedHex;
        } catch (IOException ex) {
            throw new TokenException("OutputStream error");
        }
    }


}
