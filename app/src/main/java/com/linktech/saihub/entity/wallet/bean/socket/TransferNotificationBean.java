package com.linktech.saihub.entity.wallet.bean.socket;

/**
 * Created by tromo on 2022/3/14.
 */
public class TransferNotificationBean {
    private String coin;
    private DataBean data;

    public String getCoin() {
        return coin;
    }

    public void setCoin(String coin) {
        this.coin = coin;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        private String amount;
        private int blockNumber;
        private String coin;
        private String content;
        private String contractAddress;
        private long createdDate;
        private long createdTimestamp;
        private String fromAddress;
        private String hash;
        private long lastModifiedDate;
        private long lastModifiedTimestamp;
        private int status;
        private long time;
        private long timestamp;
        private String toAddress;
        private int type;

        public String getAmount() {
            return amount;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }

        public int getBlockNumber() {
            return blockNumber;
        }

        public void setBlockNumber(int blockNumber) {
            this.blockNumber = blockNumber;
        }

        public String getCoin() {
            return coin;
        }

        public void setCoin(String coin) {
            this.coin = coin;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getContractAddress() {
            return contractAddress;
        }

        public void setContractAddress(String contractAddress) {
            this.contractAddress = contractAddress;
        }

        public long getCreatedDate() {
            return createdDate;
        }

        public void setCreatedDate(long createdDate) {
            this.createdDate = createdDate;
        }

        public long getCreatedTimestamp() {
            return createdTimestamp;
        }

        public void setCreatedTimestamp(long createdTimestamp) {
            this.createdTimestamp = createdTimestamp;
        }

        public String getFromAddress() {
            return fromAddress;
        }

        public void setFromAddress(String fromAddress) {
            this.fromAddress = fromAddress;
        }

        public String getHash() {
            return hash;
        }

        public void setHash(String hash) {
            this.hash = hash;
        }

        public long getLastModifiedDate() {
            return lastModifiedDate;
        }

        public void setLastModifiedDate(long lastModifiedDate) {
            this.lastModifiedDate = lastModifiedDate;
        }

        public long getLastModifiedTimestamp() {
            return lastModifiedTimestamp;
        }

        public void setLastModifiedTimestamp(long lastModifiedTimestamp) {
            this.lastModifiedTimestamp = lastModifiedTimestamp;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public String getToAddress() {
            return toAddress;
        }

        public void setToAddress(String toAddress) {
            this.toAddress = toAddress;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }
    }
}
