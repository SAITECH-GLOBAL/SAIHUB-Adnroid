package com.linktech.saihub.db.bean;

import com.linktech.saihub.entity.wallet.bean.transaction.TransferSendBean;
import com.linktech.saihub.util.system.CommonStringKt;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

@Entity
public class TransferServerBean implements Serializable {
    static final long serialVersionUID = 42L;
    /**
     * createdBy : null
     * lastModifiedBy : null
     * createdDate : 2020-11-16T07:21:07.000+0000
     * lastModifiedDate : 2020-11-16T07:21:07.000+0000
     * createdTimestamp : 1605511267000
     * lastModifiedTimestamp : 1605511267000
     * id : 690473
     * coin : ETH
     * type : 2
     * trxContractType : null
     * amount : 2997
     * fee : 0
     * status : 1
     * fromAddress : 0x0a98fb70939162725ae66e626fe4b52cff62c2e5
     * toAddress : 0x6a9e7d6f26e114da74ec1f5f7b82d181da18c8d6
     * contractAddress : 0xdac17f958d2ee523a2206206994597c13d831ec7
     * hash : 0x693e309c04cc4d169318b98070ffa2f96ea4ba82f890304f8b3de17bcf08e8b3
     * blockNumber : 11267550
     * time : 2020-11-16T07:18:45.000+0000
     * timestamp : 1605511125000
     * <p>
     * content : {"blockHash":"0x3e6b756c6781cf510810789189758e953fcca931899b36a962c6f84e36cc33a3",
     * "blockNumber":11267550,
     * "blockNumberRaw":"0xabedde",
     * "chainId":1,"from":"0x0a98fb70939162725ae66e626fe4b52cff62c2e5",
     * "gas":100000,"gasPrice":31000000000,
     * "gasPriceRaw":"0x737be7600","gasRaw":"0x186a0",
     * "hash":"0x693e309c04cc4d169318b98070ffa2f96ea4ba82f890304f8b3de17bcf08e8b3",
     * "input":"0xa9059cbb0000000000000000000000006a9e7d6f26e114da74ec1f5f7b82d181da18c8d600000000000000000000000000000000000000000000000000000000b2a29740",
     * "nonce":1184378,"nonceRaw":"0x12127a","r":"0x3fab578a297ecf4867db65bd43f77ab3b5e71e395e382490a21d2e88fe343775","s":"0x617eedfb4189bdf91b308987717db42d96fe94c0ed006b42b35a3625eb0363b4",
     * "to":"0xdac17f958d2ee523a2206206994597c13d831ec7","transactionIndex":146,"transactionIndexRaw":"0x92","v":38,"value":0,"valueRaw":"0x0"}
     */

    @Id(autoincrement = true)
    private Long mId;

    private String id;

    private String amount;
    private String blockHash;
    private long blockNumber;
    private String blockTime;
    private long confirmations;
    private String coin; //padding 存储的是链名称  //walletBean.getWalletType()
    private String content;
    private String createdDate;
    private String fromAddress;
    private String hash;
    private int status; //状态 0未知 1成功 2失败 3转账中
    private String time;
    private long timestamp;
    private String toAddress;
    private int type; //   //1转入 2转出
    private long createdTimestamp;
    private String fee;

    private long walletId;
    // 地址类型  btc区分用
    private int childAddressType;

    //padding 数据存储的padding 数据
    private String transferCoin;



    @Generated(hash = 279593804)
    public TransferServerBean(Long mId, String id, String amount, String blockHash, long blockNumber, String blockTime, long confirmations, String coin, String content, String createdDate,
            String fromAddress, String hash, int status, String time, long timestamp, String toAddress, int type, long createdTimestamp, String fee, long walletId, int childAddressType,
            String transferCoin) {
        this.mId = mId;
        this.id = id;
        this.amount = amount;
        this.blockHash = blockHash;
        this.blockNumber = blockNumber;
        this.blockTime = blockTime;
        this.confirmations = confirmations;
        this.coin = coin;
        this.content = content;
        this.createdDate = createdDate;
        this.fromAddress = fromAddress;
        this.hash = hash;
        this.status = status;
        this.time = time;
        this.timestamp = timestamp;
        this.toAddress = toAddress;
        this.type = type;
        this.createdTimestamp = createdTimestamp;
        this.fee = fee;
        this.walletId = walletId;
        this.childAddressType = childAddressType;
        this.transferCoin = transferCoin;
    }

    @Generated(hash = 2119469789)
    public TransferServerBean() {
    }



    public static TransferServerBean newInstance(TransferSendBean sendBean, String hash, WalletBean walletBean) {
        TransferServerBean serverBean = new TransferServerBean();
        serverBean.setAmount(sendBean.getMoneyNumber());
        serverBean.setHash(hash);
        serverBean.setCoin("BTC");
        serverBean.setTransferCoin(sendBean.getTokenName());
        serverBean.setToAddress(sendBean.getToAddress());
        serverBean.setFromAddress(sendBean.getAddress());
        serverBean.setCreatedTimestamp(System.currentTimeMillis());
        serverBean.setTimestamp(System.currentTimeMillis());
        serverBean.setId("");
        serverBean.setStatus(3);
        serverBean.setType(3);
        serverBean.setTime("");
        serverBean.setWalletId(walletBean.getId());
        serverBean.setChildAddressType(CommonStringKt.getTypeForAddress(walletBean.getAddress()));
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("gasPrice", sendBean.getGasLimit());
            jsonObject.put("gas", sendBean.getGasPrice());
            serverBean.setContent(jsonObject.toString());
        } catch (JSONException e) {
            serverBean.setContent("");
            e.printStackTrace();
        }
        return serverBean;
    }

    public static TransferServerBean newInstance(TransferServerBean bean) {
        TransferServerBean serverBean = new TransferServerBean();
        serverBean.setHash(bean.getHash());
        serverBean.setCoin(bean.getCoin());
        serverBean.setStatus(bean.getStatus());
        serverBean.setType(bean.getType());
        serverBean.setTimestamp(bean.getTimestamp());
        serverBean.setAmount(bean.getAmount());
        serverBean.setFromAddress(bean.getFromAddress());
        serverBean.setToAddress(bean.getToAddress());
        serverBean.setBlockNumber(bean.getBlockNumber());
        serverBean.setContent(bean.getContent());
        return serverBean;
    }

    public Long getMId() {
        return this.mId;
    }

    public void setMId(Long mId) {
        this.mId = mId;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAmount() {
        return this.amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getBlockHash() {
        return this.blockHash;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }

    public long getBlockNumber() {
        return this.blockNumber;
    }

    public void setBlockNumber(long blockNumber) {
        this.blockNumber = blockNumber;
    }

    public String getBlockTime() {
        return this.blockTime;
    }

    public void setBlockTime(String blockTime) {
        this.blockTime = blockTime;
    }

    public long getConfirmations() {
        return this.confirmations;
    }

    public void setConfirmations(long confirmations) {
        this.confirmations = confirmations;
    }

    public String getCoin() {
        return this.coin;
    }

    public void setCoin(String coin) {
        this.coin = coin;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreatedDate() {
        return this.createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getFromAddress() {
        return this.fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getHash() {
        return this.hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public int getStatus() {
        return this.status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getTime() {
        return this.time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getToAddress() {
        return this.toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getCreatedTimestamp() {
        return this.createdTimestamp;
    }

    public void setCreatedTimestamp(long createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public String getFee() {
        return this.fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    public long getWalletId() {
        return this.walletId;
    }

    public void setWalletId(long walletId) {
        this.walletId = walletId;
    }

    public int getChildAddressType() {
        return this.childAddressType;
    }

    public void setChildAddressType(int childAddressType) {
        this.childAddressType = childAddressType;
    }

    public String getTransferCoin() {
        return this.transferCoin;
    }

    public void setTransferCoin(String transferCoin) {
        this.transferCoin = transferCoin;
    }



}
