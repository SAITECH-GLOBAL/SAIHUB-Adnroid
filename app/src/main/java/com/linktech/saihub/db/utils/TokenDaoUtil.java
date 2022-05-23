package com.linktech.saihub.db.utils;

import android.content.Context;
import android.text.TextUtils;

import com.linktech.saihub.R;
import com.linktech.saihub.app.Constants;
import com.linktech.saihub.app.StringConstants;
import com.linktech.saihub.db.bean.TokenInfoBean;
import com.linktech.saihub.db.bean.WalletBean;
import com.linktech.saihub.entity.wallet.bean.RateEntity;
import com.linktech.saihub.greendao.TokenInfoBeanDao;
import com.linktech.saihub.greendao.WalletBeanDao;
import com.linktech.saihub.manager.DbManager;
import com.linktech.saihub.util.NumberCountUtils;
import com.linktech.saihub.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TokenDaoUtil {

    public static TokenInfoBeanDao tokenBeanDao = DbManager.getInstance().getDaoSession().getTokenInfoBeanDao();
    public static WalletBeanDao walletBeanDao = DbManager.getInstance().getDaoSession().getWalletBeanDao();

    //初始化存入 btc及u
    public static void insertTokenForWalType(Long id, WalletBean walletBean, Context context) {

        TokenInfoBean tokenBeanBTC = new TokenInfoBean();
        tokenBeanBTC.setWalId(id);
        tokenBeanBTC.setTokenShort("BTC");
        tokenBeanBTC.setAddress(walletBean.getAddress());
        tokenBeanBTC.setTokenFull("BTC");
        tokenBeanBTC.setPlaces(8);
        tokenBeanBTC.setLogo(StringUtils.imageTranslateUri(R.mipmap.icon_btc, context));
        tokenBeanBTC.setSelect(true);
        tokenBeanBTC.setTokenType(Constants.WAL_TOKEN_TYPE_NOT);

        TokenInfoBean tokenBean = new TokenInfoBean();
        tokenBean.setWalId(id);
        tokenBean.setTokenShort("USDT");
        tokenBean.setTokenFull("USDT-OMNI");
        tokenBean.setLogo(StringUtils.imageTranslateUri(R.mipmap.icon_usdt, context));
        tokenBean.setPlaces(6);
        tokenBean.setSelect(true);
        tokenBean.setTokenType(Constants.WAL_TOKEN_TYPE_BTC_OMNI);

        List<TokenInfoBean> tokenList = new ArrayList<>();
        tokenList.add(tokenBeanBTC);
        tokenList.add(tokenBean);
        tokenBeanDao.insertInTx(tokenList);
    }


    public static List<TokenInfoBean> loadAll() {
        List<TokenInfoBean> infoBeanList = tokenBeanDao.loadAll();
        return infoBeanList;
    }

    public static List<TokenInfoBean> loadTokenListForWalId(long walletId) {
        List<TokenInfoBean> mtaskPath = tokenBeanDao.queryBuilder().where(TokenInfoBeanDao.Properties.WalId.eq(walletId)).list();
        if (mtaskPath == null) {
            return new ArrayList<>();
        }
        return mtaskPath;
    }

    //根据指定钱包id和币种获取代币信息
    public static List<TokenInfoBean> loadTokenDataForWalId(long walletId, boolean isBTC) {
        List<TokenInfoBean> mtaskPath;
        if (isBTC) {
            mtaskPath = tokenBeanDao.queryBuilder().where(TokenInfoBeanDao.Properties.WalId.eq(walletId),
                    TokenInfoBeanDao.Properties.TokenShort.eq(StringConstants.BTC)).list();
        } else {
            mtaskPath = tokenBeanDao.queryBuilder().where(TokenInfoBeanDao.Properties.WalId.eq(walletId),
                    TokenInfoBeanDao.Properties.TokenShort.eq(StringConstants.USDT)).list();
        }

        if (mtaskPath == null) {
            return new ArrayList<>();
        }
        return mtaskPath;
    }

    //更新指定钱包的BTC余额
    public static void updateBTCBalanceForWallet(long walletId, BigDecimal balance, RateEntity rateEntity) {
        List<TokenInfoBean> list = tokenBeanDao.queryBuilder().where(TokenInfoBeanDao.Properties.WalId.eq(walletId),
                TokenInfoBeanDao.Properties.TokenShort.eq(StringConstants.BTC)).list();
        if (!list.isEmpty()) {
            TokenInfoBean tokenInfoBean = list.get(0);
            String tokenBalance = NumberCountUtils.getNumberScaleByPow(balance.toPlainString(), tokenInfoBean.getPlaces(), 8);
            tokenInfoBean.setTokenBalance(tokenBalance);
            tokenInfoBean.setCurrency(NumberCountUtils.getConvert(tokenBalance, rateEntity.getBtcCny()));
            tokenInfoBean.setCurrencyUsd(NumberCountUtils.getConvert(tokenBalance, rateEntity.getBtcUsd()));
            tokenInfoBean.setCurrencyRub(NumberCountUtils.getConvert(tokenBalance, rateEntity.getBtcRub()));
            tokenBeanDao.update(tokenInfoBean);
            updateBalanceForWallet(walletId);
        }

    }

    //更新指定钱包的USDT余额
    public static void updateUSDTBalanceForWallet(long walletId, String balance, RateEntity rateEntity) {
        List<TokenInfoBean> list = tokenBeanDao.queryBuilder().where(TokenInfoBeanDao.Properties.WalId.eq(walletId),
                TokenInfoBeanDao.Properties.TokenShort.eq(StringConstants.USDT)).list();
        if (!list.isEmpty()) {
            TokenInfoBean tokenInfoBean = list.get(0);
            tokenInfoBean.setTokenBalance(NumberCountUtils.getNumberScale(balance, 2));
            tokenInfoBean.setCurrency(NumberCountUtils.getConvert(balance, rateEntity.getUsdtCny()));
            tokenInfoBean.setCurrencyUsd(NumberCountUtils.getConvert(balance, rateEntity.getUsdtUsd()));
            tokenInfoBean.setCurrencyRub(NumberCountUtils.getConvert(balance, rateEntity.getUsdtRub()));
            tokenBeanDao.update(tokenInfoBean);
            updateBalanceForWallet(walletId);
        }

    }

    //更新钱包余额
    public static void updateBalanceForWallet(long walletId) {
        List<TokenInfoBean> btcList = tokenBeanDao.queryBuilder().where(TokenInfoBeanDao.Properties.WalId.eq(walletId),
                TokenInfoBeanDao.Properties.TokenShort.eq(StringConstants.BTC)).list();
        List<TokenInfoBean> uList = tokenBeanDao.queryBuilder().where(TokenInfoBeanDao.Properties.WalId.eq(walletId),
                TokenInfoBeanDao.Properties.TokenShort.eq(StringConstants.USDT)).list();
        String currency = NumberCountUtils.addNum(btcList.get(0).getCurrency(), uList.get(0).getCurrency());
        String currencyUsd = NumberCountUtils.addNum(btcList.get(0).getCurrencyUsd(), uList.get(0).getCurrencyUsd());
        String currencyRub = NumberCountUtils.addNum(btcList.get(0).getCurrencyRub(), uList.get(0).getCurrencyRub());
        List<WalletBean> list = walletBeanDao.queryBuilder().where(WalletBeanDao.Properties.Id.eq(walletId)).list();
        WalletBean walletBean = list.get(0);
        walletBean.setAsset(currency);
        walletBean.setAssetRub(currencyRub);
        walletBean.setAssetUSD(currencyUsd);
        walletBeanDao.update(walletBean);
    }

    /**
     * 获得token test环境的tokenlist
     *
     * @param walletBean
     * @return
     */
    public static List<TokenInfoBean> loadTokenListForTestCache(WalletBean walletBean) {
//        if (walletBean.getWalletType() == Constants.WAL_TYPE_BTC) {
        List<TokenInfoBean> mtaskPath = tokenBeanDao.queryBuilder().where(TokenInfoBeanDao.Properties.WalId.eq(walletBean.getId()),
                TokenInfoBeanDao.Properties.ChildAddressType.eq(walletBean.getChildAddressType())).list();
        if (mtaskPath == null || mtaskPath.size() != 2) {
            List<TokenInfoBean> infoBeanList = loadTokenListForWalId(walletBean.getId());
            if (infoBeanList == null) {
                return new ArrayList<>();
            }
            return infoBeanList;
        }
        if (mtaskPath == null) {
            return new ArrayList<>();
        }
        return mtaskPath;
//        }
//        else {
//            List<TokenInfoBean> tokenInfoBeanList = tokenBeanDao.queryBuilder().where(TokenInfoBeanDao.Properties.WalId.eq(walletBean.getId())).list();
//
//            if (tokenInfoBeanList == null) {
//                return new ArrayList<>();
//            }
//            List<TokenInfoBean> mtaskPath = new ArrayList<>();
//            for (TokenInfoBean tokenInfoBean : tokenInfoBeanList) {
//                if (TextUtils.isEmpty(tokenInfoBean.getContractAddr())) {
//                    mtaskPath.add(tokenInfoBean);
//                    break;
//                }
//            }
//            return mtaskPath;
//        }
    }

    public static List<TokenInfoBean> loadTokenListForWalCache(WalletBean walletBean) {
        List<TokenInfoBean> mtaskPath = tokenBeanDao.queryBuilder().where(TokenInfoBeanDao.Properties.WalId.eq(walletBean.getId()),
                TokenInfoBeanDao.Properties.ChildAddressType.eq(walletBean.getChildAddressType())).list();
        if (mtaskPath == null || mtaskPath.size() != 2) {
            List<TokenInfoBean> infoBeanList = loadTokenListForWalId(walletBean.getId());
            if (infoBeanList == null) {
                return new ArrayList<>();
            }
            return infoBeanList;
        }
        if (mtaskPath == null) {
            return new ArrayList<>();
        }
        return mtaskPath;
    }

    public static void deleteTokenList(TokenInfoBean tokenBean) {
        List<TokenInfoBean> mtaskPath = getListTokenForShortAndContract(tokenBean);

        if (mtaskPath != null && mtaskPath.size() > 0) {
            for (TokenInfoBean tokenInfoBean : mtaskPath) {
                tokenBeanDao.delete(tokenInfoBean);
            }
        }
    }

    /**
     * 删除tokenBbean数据的时候,需要判断钱包类型是eth或heco的时候,转换合约地址为小写判断,本地存储eht和heco合约地址为小写
     *
     * @param tokenBean
     */
    public static void deleteToken(TokenInfoBean tokenBean) {
        if (!TextUtils.isEmpty(tokenBean.getContractAddr())) {
            tokenBean.setContractAddr(tokenBean.getContractAddr());
            tokenBeanDao.queryBuilder().where(
                    TokenInfoBeanDao.Properties.TokenShort.eq(tokenBean.getTokenShort()),
                    TokenInfoBeanDao.Properties.ContractAddr.eq(tokenBean.getContractAddr()),
                    TokenInfoBeanDao.Properties.WalId.eq(tokenBean.getWalId()))
                    .buildDelete().executeDeleteWithoutDetachingEntities();
        } else {
            List<TokenInfoBean> list = tokenBeanDao.queryBuilder().where(
                    TokenInfoBeanDao.Properties.TokenShort.eq(tokenBean.getTokenShort()),
                    TokenInfoBeanDao.Properties.WalId.eq(tokenBean.getWalId()))
                    .list();

            Iterator<TokenInfoBean> iterator = list.iterator();
            while (iterator.hasNext()) {
                TokenInfoBean next = iterator.next();
                if (!TextUtils.isEmpty(next.getContractAddr())) {
                    iterator.remove();
                }
            }
            tokenBeanDao.deleteInTx(list);
        }
    }

    public static void deleteWallet(long walletId) {
        tokenBeanDao.queryBuilder().where(TokenInfoBeanDao.Properties.WalId.eq(walletId)).buildDelete().executeDeleteWithoutDetachingEntities();
    }

    public static BigDecimal loadTokenBalanceForWallet(int childAddressType, TokenInfoBean tokenInfoBean) {
        List<TokenInfoBean> list;
        if (TextUtils.isEmpty(tokenInfoBean.getContractAddr())) {
            list = tokenBeanDao.queryBuilder().where(
                    TokenInfoBeanDao.Properties.TokenShort.eq(tokenInfoBean.getTokenShort()),
                    TokenInfoBeanDao.Properties.ChildAddressType.eq(childAddressType),
                    TokenInfoBeanDao.Properties.WalId.eq(tokenInfoBean.getWalId())).list();
            Iterator<TokenInfoBean> iterator = list.iterator();
            while (iterator.hasNext()) {
                TokenInfoBean next = iterator.next();
                if (!TextUtils.isEmpty(next.getContractAddr())) {
                    iterator.remove();
                }
            }
        } else {
            list = tokenBeanDao.queryBuilder().where(
                    TokenInfoBeanDao.Properties.TokenShort.eq(tokenInfoBean.getTokenShort()),
                    TokenInfoBeanDao.Properties.ChildAddressType.eq(childAddressType),
                    TokenInfoBeanDao.Properties.ContractAddr.eq(tokenInfoBean.getContractAddr()),
                    TokenInfoBeanDao.Properties.WalId.eq(tokenInfoBean.getWalId())).list();
        }
        if (list == null || list.isEmpty()) {
            return new BigDecimal(0);
        }
        if (new BigDecimal(list.get(0).getTokenBalance()).compareTo(BigDecimal.ZERO) < 0) {
            return new BigDecimal(0);
        }
        return new BigDecimal(list.get(0).getTokenBalance());
    }

    /**
     * 根据合约地址查询 token是否存在
     *
     * @param contractAddress
     * @param walletBean
     * @return
     */
    public static boolean queryTokenContractAddr(String contractAddress, WalletBean walletBean) {
        if (TextUtils.isEmpty(contractAddress)) {
            return false;
        }
        List<TokenInfoBean> list = tokenBeanDao.queryBuilder().where(
                TokenInfoBeanDao.Properties.ContractAddr.eq(contractAddress),
                TokenInfoBeanDao.Properties.WalId.eq(walletBean.getId())).list();
        if (list == null || list.size() <= 0) {
            return false;
        }
        return true;
    }

    private static List<TokenInfoBean> getListTokenForShortAndContract(TokenInfoBean tokenInfoBean) {
        List<TokenInfoBean> list;
        if (TextUtils.isEmpty(tokenInfoBean.getContractAddr())) {
            list = tokenBeanDao.queryBuilder().where(TokenInfoBeanDao.Properties.TokenShort.eq(tokenInfoBean.getTokenShort()),
                    TokenInfoBeanDao.Properties.WalId.eq(tokenInfoBean.getWalId())).list();
            Iterator<TokenInfoBean> iterator = list.iterator();
            while (iterator.hasNext()) {
                TokenInfoBean next = iterator.next();
                if (!TextUtils.isEmpty(next.getContractAddr())) {
                    iterator.remove();
                }
            }
        } else {
            list = tokenBeanDao.queryBuilder().where(TokenInfoBeanDao.Properties.TokenShort.eq(tokenInfoBean.getTokenShort()),
                    TokenInfoBeanDao.Properties.ContractAddr.eq(tokenInfoBean.getContractAddr()),
                    TokenInfoBeanDao.Properties.WalId.eq(tokenInfoBean.getWalId())).list();
        }
        return list;
    }

    public static BigDecimal getCurrentMainBalance(WalletBean currentWal) {
        List<TokenInfoBean> list = tokenBeanDao.queryBuilder().where(TokenInfoBeanDao.Properties.TokenShort.eq("BTC"),
                TokenInfoBeanDao.Properties.WalId.eq(currentWal.getId())).list();
        if (list != null && !list.isEmpty()) {
            return new BigDecimal(list.get(0).getTokenBalance());
        } else {
            return BigDecimal.ZERO;
        }
    }


    /**
     * 根据合约地址和钱包地址 查询 token
     * btc 切换地址  token表中地址没有更改  所以btc不能用哪个地址查询
     * token 本地数据库中  主币 存储 address有数据  合约地址无数据  相反  代币  address存储无数据  合约地址有数据
     *
     * @param tokenShort
     * @param contractAddress
     * @param walletBean
     * @return
     */
    public static TokenInfoBean getTokenForContractAddr(String tokenShort, String contractAddress, WalletBean walletBean) {
        //查询btc
        List<TokenInfoBean> list = tokenBeanDao.queryBuilder().where(TokenInfoBeanDao.Properties.WalId.eq(walletBean.getId())).list();
        if (list == null || list.isEmpty()) {
            return null;
        }
        for (TokenInfoBean tokenInfoBean : list) {
            if (tokenShort.equals(tokenInfoBean.getTokenShort())) {
                return tokenInfoBean;
            }
        }
        return null;

//        //通过地址查询  主币
//        if (TextUtils.isEmpty(contractAddress)) {
//            List<TokenInfoBean> list = tokenBeanDao.queryBuilder().where(TokenInfoBeanDao.Properties.Address.eq(walletBean.getAddress()),
//                    TokenInfoBeanDao.Properties.WalId.eq(walletBean.getId())).list();
//            Iterator<TokenInfoBean> iterator = list.iterator();
//            while (iterator.hasNext()) {
//                TokenInfoBean next = iterator.next();
//                if (!TextUtils.isEmpty(next.getContractAddr())) {
//                    iterator.remove();
//                }
//            }
//            if (list == null || list.isEmpty()) {
//                return null;
//            }
//            return list.get(0);
//        }
//        //通过合约地址查询代币
//        List<TokenInfoBean> list = tokenBeanDao.queryBuilder().where(TokenInfoBeanDao.Properties.ContractAddr.eq(contractAddress),
//                TokenInfoBeanDao.Properties.WalId.eq(walletBean.getId())).list();
//        if (list == null || list.isEmpty()) {
//            return null;
//        }
//        return list.get(0);
    }

    public static void saveTokenLogo(long walletId, String contractAddr, String logo) {
        List<TokenInfoBean> list = tokenBeanDao.queryBuilder().where(TokenInfoBeanDao.Properties.WalId.eq(walletId), TokenInfoBeanDao.Properties.ContractAddr.eq(contractAddr)).list();
        if (list == null || list.isEmpty()) {
            return;
        }
        TokenInfoBean tokenInfoBean = list.get(0);
        tokenInfoBean.setLogo(logo);
        tokenBeanDao.update(tokenInfoBean);
    }
}
