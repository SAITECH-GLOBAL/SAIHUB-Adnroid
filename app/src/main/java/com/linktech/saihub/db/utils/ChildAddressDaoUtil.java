package com.linktech.saihub.db.utils;

import com.linktech.saihub.R;
import com.linktech.saihub.app.Constants;
import com.linktech.saihub.app.SaiHubApplication;
import com.linktech.saihub.db.bean.ChildAddressBean;
import com.linktech.saihub.db.bean.WalletBean;
import com.linktech.saihub.greendao.ChildAddressBeanDao;
import com.linktech.saihub.manager.DbManager;
import com.linktech.saihub.manager.MMKVManager;
import com.linktech.saihub.manager.wallet.exception.TokenException;

import java.util.ArrayList;
import java.util.List;

public class ChildAddressDaoUtil {

    public static ChildAddressBeanDao childAddressBeanDao = DbManager.getInstance().getDaoSession().getChildAddressBeanDao();


    public static long insertChildAddress(ChildAddressBean childAddressBean) {
        List<ChildAddressBean> list = childAddressBeanDao.queryBuilder().where(ChildAddressBeanDao.Properties.WalletId.eq(childAddressBean.getWalletId()),
                ChildAddressBeanDao.Properties.ChildAddress.eq(childAddressBean.getChildAddress())).orderDesc(ChildAddressBeanDao.Properties.ChildNumber).list();
        if (list != null && list.size() > 0) {
            childAddressBeanDao.deleteInTx(list);
        }
        long id = childAddressBeanDao.insert(childAddressBean);
        return id;
    }

    public static void insertChildAddress(List<ChildAddressBean> childAddressBeanList, WalletBean wallet) {
        if (childAddressBeanList.isEmpty()) {
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < childAddressBeanList.size(); i++) {
            ChildAddressBean childAddressBean1 = childAddressBeanList.get(i);
            if (childAddressBean1 == null) {
                continue;
            }
            stringBuilder.append(i == 0 ? "" : ",");
            stringBuilder.append(childAddressBean1.getChildAddress());
            childAddressBean1.setWalletId(wallet.getId());
            insertChildAddress(childAddressBeanList.get(i));
        }
        wallet.setAddressToServer(stringBuilder.toString());
    }


    public static List<ChildAddressBean> getChildAddressListForWalletId(Long walletId) {
        List<ChildAddressBean> list = childAddressBeanDao.queryBuilder().where(ChildAddressBeanDao.Properties.WalletId.eq(walletId))
                .orderAsc(ChildAddressBeanDao.Properties.ChildNumber).list();
        if (list == null || list.size() <= 0) {
            return null;
        }
        return list;
    }

    /**
     * 切换选择的地址
     */
    public static void selectChildAddress(ChildAddressBean data) {
        List<ChildAddressBean> list = childAddressBeanDao.queryBuilder().where(ChildAddressBeanDao.Properties.WalletId.eq(data.getWalletId()),
                ChildAddressBeanDao.Properties.ChildChangeType.eq(Constants.CHILD_ADDRESS_NOT_CHANGE_TYPE),
                ChildAddressBeanDao.Properties.IsSelect.eq(data.getIsSelect())).orderDesc(ChildAddressBeanDao.Properties.ChildNumber).list();
        if (list == null || list.size() <= 0) {
            return;
        }
        for (ChildAddressBean childAddressBean : list) {
            childAddressBean.setIsSelect(false);
        }
        childAddressBeanDao.updateInTx(list);
        data.setIsSelect(true);

        childAddressBeanDao.update(data);
    }


    /**
     * 私钥地址,切换
     *
     * @param addressType
     * @param walletId
     * @return
     */
    public static ChildAddressBean getKeyChildAddree(int addressType, Long walletId) {
        List<ChildAddressBean> childAddressForType = getChildAddressForTypeNotChange(addressType, walletId);
        if (childAddressForType == null || childAddressForType.size() <= 0) {
            return null;
        }
        return childAddressForType.get(0);
    }

    /**
     * 根据子地址不同获得地址  非找零
     *
     * @param addressType
     * @param walletId
     * @return
     */
    public static List<ChildAddressBean> getChildAddressForTypeNotChange(int addressType, Long walletId) {
        List<ChildAddressBean> list = childAddressBeanDao.queryBuilder().where(ChildAddressBeanDao.Properties.WalletId.eq(walletId),
                ChildAddressBeanDao.Properties.ChildChangeType.eq(Constants.CHILD_ADDRESS_NOT_CHANGE_TYPE),
                ChildAddressBeanDao.Properties.ChildType.eq(addressType)).orderAsc(ChildAddressBeanDao.Properties.ChildNumber).list();
        if (list == null || list.size() <= 0) {
            return null;
        }
        return list;
    }

    public static List<ChildAddressBean> getChangeAddressList(Long walletId) {
        List<ChildAddressBean> list = childAddressBeanDao.queryBuilder().where(ChildAddressBeanDao.Properties.WalletId.eq(walletId),
                ChildAddressBeanDao.Properties.ChildChangeType.eq(Constants.CHILD_ADDRESS_CHANGE_TYPE),
                ChildAddressBeanDao.Properties.ChildType.eq(Constants.CHILD_ADDRESS_NATIVE)).orderAsc(ChildAddressBeanDao.Properties.ChildNumber).list();
        if (list == null || list.size() <= 0) {
            return null;
        }
        List<ChildAddressBean> allList = new ArrayList<>(list);
        list = childAddressBeanDao.queryBuilder().where(ChildAddressBeanDao.Properties.WalletId.eq(walletId),
                ChildAddressBeanDao.Properties.ChildChangeType.eq(Constants.CHILD_ADDRESS_CHANGE_TYPE),
                ChildAddressBeanDao.Properties.ChildType.eq(Constants.CHILD_ADDRESS_NESTED)).orderAsc(ChildAddressBeanDao.Properties.ChildNumber).list();
        if (list == null || list.size() <= 0) {
            return null;
        }
        allList.addAll(list);
        return allList;
    }

    /**
     * 获得地址  找零地址和非找零地址  发送服务器
     *
     * @return
     */
    public static String getChildAddressForTypeToServer(WalletBean walletBean) {
        int child_address_type = walletBean.getAddressType();
        List<ChildAddressBean> addressBeanList = childAddressBeanDao.queryBuilder().where(
                ChildAddressBeanDao.Properties.WalletId.eq(walletBean.getId()),
                ChildAddressBeanDao.Properties.ChildType.eq(child_address_type)).orderAsc(ChildAddressBeanDao.Properties.ChildNumber).list();
        if (addressBeanList == null) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int i1 = 0; i1 < addressBeanList.size(); i1++) {
            stringBuilder.append(i1 == 0 ? "" : ",").append(addressBeanList.get(i1).getChildAddress());
        }
        return stringBuilder.toString();
    }

    /**
     * 获得地址  找零地址和非找零地址  发送服务器
     *
     * @return
     */
    public static List<ChildAddressBean> getChildAddressForTypeToServerList(WalletBean walletBean) {
        int child_address_type = walletBean.getChildAddressType();
        List<ChildAddressBean> addressBeanList = childAddressBeanDao.queryBuilder().where(
                ChildAddressBeanDao.Properties.WalletId.eq(walletBean.getId()),
                ChildAddressBeanDao.Properties.ChildType.eq(child_address_type)).orderAsc(ChildAddressBeanDao.Properties.ChildNumber).list();
        if (addressBeanList == null) {
            addressBeanList = new ArrayList<>();
        }
        return addressBeanList;
    }

    /**
     * 获得显示的子地址
     *
     * @param addressType
     * @param walletId
     * @return
     */
    public static List<ChildAddressBean> getChildAddressForTypeShow(int addressType, Long walletId) {
        List<ChildAddressBean> list = childAddressBeanDao.queryBuilder().where(ChildAddressBeanDao.Properties.WalletId.eq(walletId),
                ChildAddressBeanDao.Properties.IsShow.eq(true),
                ChildAddressBeanDao.Properties.ChildChangeType.eq(Constants.CHILD_ADDRESS_NOT_CHANGE_TYPE),
                ChildAddressBeanDao.Properties.ChildType.eq(addressType)).orderDesc(ChildAddressBeanDao.Properties.ChildNumber).list();
        if (list == null || list.size() <= 0) {
            return null;
        }
        return list;
    }


    /**
     * 通过地址获取当前的childNumber
     *
     * @param walletId
     * @return
     */
    public static int getChildNumberForAddress(String address, Long walletId) {
        List<ChildAddressBean> list = childAddressBeanDao.queryBuilder().where(ChildAddressBeanDao.Properties.WalletId.eq(walletId),
                ChildAddressBeanDao.Properties.ChildAddress.eq(address)).list();
        if (list == null || list.isEmpty()) {
            return -1;
        }
        return list.get(0).getChildNumber();
    }

    /**
     * 获得查询地址-通过不同的地址类型  转账获取utxo用
     *
     * @param addressType
     * @param walletBean  不用区分不同币种的USDT
     * @return
     */
    public static String getChildAddressStrTransferForType(int addressType, String tokenName, WalletBean walletBean) {
        if (tokenName.equals("USDT")) {
            return walletBean.getAddress();
        }
        List<ChildAddressBean> list = childAddressBeanDao.queryBuilder().where(ChildAddressBeanDao.Properties.WalletId.eq(walletBean.getId()),
                ChildAddressBeanDao.Properties.ChildType.eq(addressType)).orderDesc(ChildAddressBeanDao.Properties.ChildNumber).list();
        if (list == null || list.isEmpty()) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i == 0) {
                stringBuilder.append(list.get(i).getChildAddress());
            } else {
                stringBuilder.append(",").append(list.get(i).getChildAddress());
            }
        }
        return stringBuilder.toString();
    }


    public static long getChildAddressCountForTypeShow(int addressType, Long id) {
        long count = childAddressBeanDao.queryBuilder().where(ChildAddressBeanDao.Properties.WalletId.eq(id),
                ChildAddressBeanDao.Properties.IsShow.eq(true),
                ChildAddressBeanDao.Properties.ChildChangeType.eq(Constants.CHILD_ADDRESS_NOT_CHANGE_TYPE),
                ChildAddressBeanDao.Properties.ChildType.eq(addressType)).count();
        return count;
    }

    public static long getWalletByAddress(String address) {
        List<ChildAddressBean> list = childAddressBeanDao.queryBuilder().where(ChildAddressBeanDao.Properties.ChildAddress.eq(address)).list();
        if (list.isEmpty()) {
            return -1;
        }
        return list.get(0).walletId;
    }

    public static void deleteChildAddressForWalletId(Long walletId) {
        List<ChildAddressBean> list = childAddressBeanDao.queryBuilder().where(ChildAddressBeanDao.Properties.WalletId.eq(walletId)).list();
        childAddressBeanDao.deleteInTx(list);
    }

    /**
     * 获得找零地址
     *
     * @return
     */
    public static String getChangeAddress(WalletBean walletBean) {
        if (walletBean.getExistType() != Constants.EXIST_PRIVATE_KEY && walletBean.getExistType() != Constants.EXIST_ADDRESS) {
            int child_address_type = walletBean.getChildAddressType();
//            int child_address_type = MMKVManager.getInstance().mmkv().decodeInt(walletBean.getId() + Constants.CHILD_ADDRESS_TYPE, Constants.CHILD_ADDRESS_SWIGET);
            int child_address_number = MMKVManager.getInstance().mmkv().decodeInt(walletBean.getId() + Constants.CHILD_ADDRESS_CHANGE_NUMBER_KEY);
            if (child_address_number >= 19) {
                child_address_number = 0;
            }
            if (child_address_number < 0) {
                child_address_number = 0;
            }
            List<ChildAddressBean> list = childAddressBeanDao.queryBuilder().where(ChildAddressBeanDao.Properties.WalletId.eq(walletBean.getId()),
                    ChildAddressBeanDao.Properties.ChildType.eq(child_address_type),
                    ChildAddressBeanDao.Properties.ChildChangeType.eq(Constants.CHILD_ADDRESS_CHANGE_TYPE),
                    ChildAddressBeanDao.Properties.ChildNumber.eq(child_address_number)).list();
            child_address_number++;
            MMKVManager.getInstance().mmkv().encode(walletBean.getId() + Constants.CHILD_ADDRESS_CHANGE_NUMBER_KEY, child_address_number);
            if (list == null || list.isEmpty()) {
                return null;
            }
            return list.get(0).getChildAddress();
        } else {
            return walletBean.getAddress();
        }
    }

    /**
     * 添加显示地址 创建
     *
     * @param addressType
     * @param walletId
     * @param childNumber
     * @return
     */
    public static ChildAddressBean getChildAddressForChildNumber(int addressType, Long walletId, int childNumber) {
        List<ChildAddressBean> list = childAddressBeanDao.queryBuilder().where(ChildAddressBeanDao.Properties.WalletId.eq(walletId),
                ChildAddressBeanDao.Properties.ChildType.eq(addressType),
                ChildAddressBeanDao.Properties.ChildNumber.eq(childNumber),
                ChildAddressBeanDao.Properties.ChildChangeType.eq(Constants.CHILD_ADDRESS_NOT_CHANGE_TYPE)
        ).orderAsc(ChildAddressBeanDao.Properties.ChildNumber).list();
        if (list == null || list.size() <= 0) {
            return null;
        }
        return list.get(0);
    }

    /**
     * 添加显示的子地址
     *
     * @param currentWallet
     * @param addressType
     */
    public static ChildAddressBean createChildAddress(WalletBean currentWallet, int addressType) {
        long count = ChildAddressDaoUtil.getChildAddressCountForTypeShow(addressType, currentWallet.getId());
        if (count > 50) {
            throw new TokenException(SaiHubApplication.Companion.getInstance().getString(R.string.not_create_address));
        }
        ChildAddressBean childAddressForChildNumber = getChildAddressForChildNumber(addressType, currentWallet.getId(), (int) count);
        childAddressForChildNumber.setIsShow(true);
        childAddressBeanDao.update(childAddressForChildNumber);
        return childAddressForChildNumber;
    }

    public static String getChildAddressPrivateKey(String address, long id) {
        List<ChildAddressBean> list = childAddressBeanDao.queryBuilder().where(ChildAddressBeanDao.Properties.ChildAddress.eq(address),
                ChildAddressBeanDao.Properties.WalletId.eq(id)).list();
        if (list == null || list.size() <= 0) {
            return null;
        }
        return list.get(0).getPrivateKey();
    }
}
