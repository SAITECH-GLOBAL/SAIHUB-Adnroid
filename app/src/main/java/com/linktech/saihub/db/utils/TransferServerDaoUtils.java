package com.linktech.saihub.db.utils;

import com.linktech.saihub.db.bean.TokenInfoBean;
import com.linktech.saihub.db.bean.TransferServerBean;
import com.linktech.saihub.db.bean.WalletBean;
import com.linktech.saihub.greendao.TransferServerBeanDao;
import com.linktech.saihub.manager.DbManager;

import java.util.ArrayList;
import java.util.List;


public class TransferServerDaoUtils {

    public static final TransferServerBeanDao transferServerBeanDao = DbManager.getInstance().getDaoSession().getTransferServerBeanDao();


    /**
     * 插入交易记录
     *
     * @param serverBean
     */
    public static long insert(TransferServerBean serverBean) {
        List<TransferServerBean> list = transferServerBeanDao.queryBuilder().where(TransferServerBeanDao.Properties.Hash.eq(serverBean.getHash())).list();
        if (list != null) {
            transferServerBeanDao.deleteInTx(list);
        }
        return transferServerBeanDao.insert(serverBean);
    }

    public static void delete(String hash) {
        List<TransferServerBean> list = transferServerBeanDao.queryBuilder().where(TransferServerBeanDao.Properties.Hash.eq(hash)).list();
        if (list != null && !list.isEmpty()) {
            transferServerBeanDao.deleteInTx(list);
        }
    }

    public static void delete(List<String> hashList) {
        for (String hash : hashList) {
            List<TransferServerBean> list = transferServerBeanDao.queryBuilder().where(TransferServerBeanDao.Properties.Hash.eq(hash)).list();
            if (list != null && !list.isEmpty()) {
                transferServerBeanDao.deleteInTx(list);
            }
        }
    }

    /**
     * 查询本地数据(转账中本地)
     * BTC omni 没有合约地址
     *
     * @param walletBean
     * @param tokenInfoBean
     * @return
     */
    public static List<TransferServerBean> loadTransferPadding(WalletBean walletBean, TokenInfoBean tokenInfoBean) {
        return queryBtcTransfer(walletBean.getId(), tokenInfoBean.getTokenShort());
    }

    private static List<TransferServerBean> queryBtcTransfer(Long walletId, String coin) {
        List<TransferServerBean> list = transferServerBeanDao.queryBuilder().where(
                TransferServerBeanDao.Properties.WalletId.eq(walletId),
                TransferServerBeanDao.Properties.Coin.eq(coin)).list();
        if (list == null) {
            return new ArrayList<>();
        }
        return list;
    }

    /**
     * 根据地址查询记录(转账记录,本地)
     *
     * @param walletId
     * @param address
     * @return
     */
    private static List<TransferServerBean> queryTransferForAddress(long walletId, String address) {
        List<TransferServerBean> list = transferServerBeanDao.queryBuilder().where(
                TransferServerBeanDao.Properties.FromAddress.eq(address),
                TransferServerBeanDao.Properties.WalletId.eq(walletId)).list();
        if (list == null) {
            return new ArrayList<>();
        }
        return list;
    }
}
