package com.linktech.saihub.db.utils;

import com.linktech.saihub.db.bean.WalletAddressBean;
import com.linktech.saihub.greendao.WalletAddressBeanDao;
import com.linktech.saihub.manager.DbManager;

import java.util.Iterator;
import java.util.List;

public class AddressDaoUtil {

    public static WalletAddressBeanDao walletAddressBeanDao = DbManager.getInstance().getDaoSession().getWalletAddressBeanDao();


    /**
     * 查询是否有相同地址
     *
     * @param address
     * @param walletType
     * @return
     */
    public static boolean qureyRepeatAddress(String address, int walletType) {
        List<WalletAddressBean> list = walletAddressBeanDao.queryBuilder().where(
                WalletAddressBeanDao.Properties.Address.eq(address)).list();
        return list != null && list.size() > 0;
    }


    /**
     * 地址本查询数量
     */
    public static int haveAddressNumber() {
        List<WalletAddressBean> list = walletAddressBeanDao.loadAll();
        return list == null ? 0 : list.size();
    }


    /**
     * 地址本添加地址 判重   查询是否有相同地址
     *
     * @param address
     * @param isEdit  是否编辑状态
     * @return
     */
    public static boolean haveSameAddressBook(String address, String editAddress, boolean isEdit) {
        List<WalletAddressBean> list = walletAddressBeanDao.queryBuilder().where(
                WalletAddressBeanDao.Properties.Address.eq(address)).list();
        if (list == null || list.size() == 0) {
            return false;
        }
        if (isEdit) {
            Iterator<WalletAddressBean> iterator = list.iterator();
            while (iterator.hasNext()) {
                WalletAddressBean next = iterator.next();
                if (next.getAddress().equals(editAddress)) {
                    iterator.remove();
                }
            }
        }

        return list != null && list.size() > 0;
    }


    public static List<WalletAddressBean> refresh(int page, int pageSize) {
        List<WalletAddressBean> list = walletAddressBeanDao.queryBuilder().offset((page - 1) * pageSize).limit(pageSize).list();
        return list;
    }

    public static List<WalletAddressBean> loadAll() {
        List<WalletAddressBean> walletAddressBeans = walletAddressBeanDao.loadAll();
        return walletAddressBeans;
    }

    public static void delete(WalletAddressBean walletAddressBean) {
        walletAddressBeanDao.delete(walletAddressBean);
    }

    public static void delete(long id) {
        walletAddressBeanDao.deleteByKey(id);
    }


    public static long saveAddressItem(WalletAddressBean itemData) {
        if (itemData.getMId() == null) {
            return DbManager.getInstance().getDaoSession().getWalletAddressBeanDao().insert(itemData);
        } else {
            DbManager.getInstance().getDaoSession().getWalletAddressBeanDao().update(itemData);
            return 0;
        }
    }


    public static long insert(WalletAddressBean walletAddressBean) {
        long insert = walletAddressBeanDao.insert(walletAddressBean);
        return insert;
    }

    public static long update(WalletAddressBean addressBean) {
        walletAddressBeanDao.update(addressBean);
        return addressBean.getMId();
    }
}
