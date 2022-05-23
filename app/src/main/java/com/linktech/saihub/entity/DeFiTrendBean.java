package com.linktech.saihub.entity;

import java.util.List;

/**
 * Created by tromo on 2021/7/7.
 */
public class DeFiTrendBean {

    /**
     * code : 0
     * msg : success
     * data : {"status":0,"message":"success","result":[{"token0PriceUsd":"2113.32111721","token1Price":"118.03023041","createdDate":1625097600000},{"token0PriceUsd":"2276.89526906","token1Price":"118.56242106","createdDate":1625011200000},{"token0PriceUsd":"2165.53131860","token1Price":"116.72830430","createdDate":1624924800000},{"token0PriceUsd":"2079.63967207","token1Price":"117.95777744","createdDate":1624838400000},{"token0PriceUsd":"1976.46965070","token1Price":"115.99539364","createdDate":1624752000000},{"token0PriceUsd":"1827.00504053","token1Price":"113.07396635","createdDate":1624665600000},{"token0PriceUsd":"1817.67514521","token1Price":"113.65203875","createdDate":1624579200000},{"token0PriceUsd":"1987.46179136","token1Price":"110.01950107","createdDate":1624492800000},{"token0PriceUsd":"1966.00798239","token1Price":"110.90071693","createdDate":1624406400000},{"token0PriceUsd":"1871.11628660","token1Price":"114.54214944","createdDate":1624320000000},{"token0PriceUsd":"1886.45352687","token1Price":"119.04098305","createdDate":1624233600000},{"token0PriceUsd":"2241.63537685","token1Price":"108.05773393","createdDate":1624147200000},{"token0PriceUsd":"2172.99985815","token1Price":"109.10112921","createdDate":1624060800000},{"token0PriceUsd":"2234.04279152","token1Price":"109.39037006","createdDate":1623974400000},{"token0PriceUsd":"2370.80084359","token1Price":"107.34228876","createdDate":1623888000000},{"token0PriceUsd":"2360.50154890","token1Price":"108.76764483","createdDate":1623801600000},{"token0PriceUsd":"2552.47416378","token1Price":"108.38748552","createdDate":1623715200000},{"token0PriceUsd":"2573.60722505","token1Price":"107.28536659","createdDate":1623628800000},{"token0PriceUsd":"2505.13548533","token1Price":"108.00685735","createdDate":1623542400000},{"token0PriceUsd":"2375.48988439","token1Price":"111.18108159","createdDate":1623456000000},{"token0PriceUsd":"2350.12213074","token1Price":"107.53839606","createdDate":1623369600000},{"token0PriceUsd":"2473.03278288","token1Price":"105.07531091","createdDate":1623283200000},{"token0PriceUsd":"2602.45838285","token1Price":"103.89319259","createdDate":1623196800000},{"token0PriceUsd":"2513.44100330","token1Price":"107.14715415","createdDate":1623110400000},{"token0PriceUsd":"2584.77549124","token1Price":"106.89789142","createdDate":1623024000000}]}
     */

    private int code;
    private String msg;
    private DataDTO data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public DataDTO getData() {
        return data;
    }

    public void setData(DataDTO data) {
        this.data = data;
    }

    public static class DataDTO {
        /**
         * status : 0
         * message : success
         * result : [{"token0PriceUsd":"2113.32111721","token1Price":"118.03023041","createdDate":1625097600000},{"token0PriceUsd":"2276.89526906","token1Price":"118.56242106","createdDate":1625011200000},{"token0PriceUsd":"2165.53131860","token1Price":"116.72830430","createdDate":1624924800000},{"token0PriceUsd":"2079.63967207","token1Price":"117.95777744","createdDate":1624838400000},{"token0PriceUsd":"1976.46965070","token1Price":"115.99539364","createdDate":1624752000000},{"token0PriceUsd":"1827.00504053","token1Price":"113.07396635","createdDate":1624665600000},{"token0PriceUsd":"1817.67514521","token1Price":"113.65203875","createdDate":1624579200000},{"token0PriceUsd":"1987.46179136","token1Price":"110.01950107","createdDate":1624492800000},{"token0PriceUsd":"1966.00798239","token1Price":"110.90071693","createdDate":1624406400000},{"token0PriceUsd":"1871.11628660","token1Price":"114.54214944","createdDate":1624320000000},{"token0PriceUsd":"1886.45352687","token1Price":"119.04098305","createdDate":1624233600000},{"token0PriceUsd":"2241.63537685","token1Price":"108.05773393","createdDate":1624147200000},{"token0PriceUsd":"2172.99985815","token1Price":"109.10112921","createdDate":1624060800000},{"token0PriceUsd":"2234.04279152","token1Price":"109.39037006","createdDate":1623974400000},{"token0PriceUsd":"2370.80084359","token1Price":"107.34228876","createdDate":1623888000000},{"token0PriceUsd":"2360.50154890","token1Price":"108.76764483","createdDate":1623801600000},{"token0PriceUsd":"2552.47416378","token1Price":"108.38748552","createdDate":1623715200000},{"token0PriceUsd":"2573.60722505","token1Price":"107.28536659","createdDate":1623628800000},{"token0PriceUsd":"2505.13548533","token1Price":"108.00685735","createdDate":1623542400000},{"token0PriceUsd":"2375.48988439","token1Price":"111.18108159","createdDate":1623456000000},{"token0PriceUsd":"2350.12213074","token1Price":"107.53839606","createdDate":1623369600000},{"token0PriceUsd":"2473.03278288","token1Price":"105.07531091","createdDate":1623283200000},{"token0PriceUsd":"2602.45838285","token1Price":"103.89319259","createdDate":1623196800000},{"token0PriceUsd":"2513.44100330","token1Price":"107.14715415","createdDate":1623110400000},{"token0PriceUsd":"2584.77549124","token1Price":"106.89789142","createdDate":1623024000000}]
         */

        private int status;
        private String message;
        private List<ResultDTO> result;

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public List<ResultDTO> getResult() {
            return result;
        }

        public void setResult(List<ResultDTO> result) {
            this.result = result;
        }

        public static class ResultDTO {
            /**
             * token0PriceUsd : 2113.32111721
             * token1Price : 118.03023041
             * createdDate : 1625097600000
             */

            private String token0PriceUsd;
            private String token1Price;
            private long createdDate;

            public String getToken0PriceUsd() {
                return token0PriceUsd;
            }

            public void setToken0PriceUsd(String token0PriceUsd) {
                this.token0PriceUsd = token0PriceUsd;
            }

            public String getToken1Price() {
                return token1Price;
            }

            public void setToken1Price(String token1Price) {
                this.token1Price = token1Price;
            }

            public long getCreatedDate() {
                return createdDate;
            }

            public void setCreatedDate(long createdDate) {
                this.createdDate = createdDate;
            }
        }
    }
}
