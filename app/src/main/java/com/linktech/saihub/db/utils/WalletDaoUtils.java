package com.linktech.saihub.db.utils;

import android.text.TextUtils;

import com.linktech.saihub.app.Constants;
import com.linktech.saihub.db.bean.ChildAddressBean;
import com.linktech.saihub.db.bean.WalletBean;
import com.linktech.saihub.entity.wallet.bean.AddressBean;
import com.linktech.saihub.greendao.WalletBeanDao;
import com.linktech.saihub.manager.DbManager;
import com.linktech.saihub.util.LogUtils;

import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class WalletDaoUtils {
    public static WalletBeanDao walletDao = DbManager.getInstance().getDaoSession().getWalletBeanDao();

    /**
     * 插入新创建钱包
     *
     * @param wallet 新创建钱包
     */
    public static Long insertNewWallet(WalletBean wallet) {
        updateCurrent(-1);
        wallet.setIsCurrent(true);
        long insert = walletDao.insert(wallet);
        return insert;
    }


    /**
     * 更新选中钱包
     *
     * @param id 钱包ID
     */
    public static WalletBean updateCurrent(long id) {
        List<WalletBean> ethWallets = walletDao.loadAll();
        WalletBean currentWallet = null;
        for (WalletBean ethwallet : ethWallets) {
            if (id != -1 && ethwallet.getId() == id) {
                ethwallet.setIsCurrent(true);
                currentWallet = ethwallet;
            } else {
                ethwallet.setIsCurrent(false);
            }
            walletDao.update(ethwallet);
        }
        return currentWallet;
    }

    public static int queryWalletCount() {
        List<WalletBean> infoBeanList = walletDao.loadAll();
        if (infoBeanList != null) {
            return infoBeanList.size();
        }
        return 0;
    }

    public static int queryWalletName(String name) {
        List<WalletBean> infoBeanList = walletDao.queryBuilder().where(WalletBeanDao.Properties.Name.eq(name)).list();
        if (infoBeanList != null) {
            return infoBeanList.size();
        }
        return 0;
    }

    public static boolean queryWalletMnemonic(WalletBean walletBean) {
        List<WalletBean> infoBeanList = walletDao.queryBuilder().where(
                WalletBeanDao.Properties.Mnemonic.eq(walletBean.getMnemonic())
        ).list();
        if (infoBeanList != null && infoBeanList.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    // Android:观察者模式，导入钱包与已有钱包不能相同，没有判断；第二个和第三个钱包相同；
    public static boolean queryWalletAddressForObServer(String address, int walletType) {
        List<WalletBean> infoBeanList = walletDao.queryBuilder().where(
                WalletBeanDao.Properties.Address.eq(address)
        ).list();
        if (infoBeanList != null && infoBeanList.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 查询是否有重复的钱包
     *
     * @param address
     * @param walletType
     * @return
     */
    public static boolean queryWalletAddress(String address, int walletType) {
        List<WalletBean> infoBeanList = walletDao.queryBuilder().where(WalletBeanDao.Properties.Address.eq(address)
        ).list();
        if (infoBeanList != null && infoBeanList.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean queryWalletMnemonic(String mnemonic, int walletType) {
        List<WalletBean> infoBeanList = walletDao.queryBuilder().where(
                WalletBeanDao.Properties.Mnemonic.eq(mnemonic)
        ).list();
        if (infoBeanList != null && infoBeanList.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean queryWalletPrivate(String privatekey, int walletType) {
        List<WalletBean> infoBeanList = walletDao.queryBuilder().where(
                WalletBeanDao.Properties.PrivateKey.eq(privatekey)
        ).list();
        if (infoBeanList != null && infoBeanList.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取当前钱包
     *
     * @return 钱包对象
     */
    public static WalletBean getCurrent() {
        List<WalletBean> ethWallets = walletDao.loadAll();
        for (WalletBean ethwallet : ethWallets) {
            if (ethwallet.getIsCurrent()) {
                ethwallet.setIsCurrent(true);
                return ethwallet;
            }
        }
        return null;
    }

    /**
     * 获得钱包的地址信息
     */
    public static String getAllWalletAddress() {
        String address = "";
        List<WalletBean> ethWallets = walletDao.loadAll();
        if (ethWallets == null) {
            return address;
        }
        for (WalletBean walletItem : ethWallets) {
            if (walletItem.getIsObserver()) {
                address += walletItem.getAddress() + ",";
            } else {
                String childAddressForTypeToServer = ChildAddressDaoUtil.getChildAddressForTypeToServer(walletItem);
                address += childAddressForTypeToServer + ",";
            }
        }
        return address;
    }

    /**
     * 获得钱包的地址信息- /rest/asset/notification/list/v2  列表v2
     * {
     * "addressList": [
     * {
     * "address": "string",
     * "coin": "string"
     * }
     * ],
     * "importTime": 0,
     * "pageNo": 0,
     * "pageSize": 0
     * }
     */
    public static List<CoinAddressBean> getAllWalletAddress2() {
        List<WalletBean> walletBeanList = walletDao.loadAll();
        List<CoinAddressBean> addressList = new ArrayList<>();
        if (walletBeanList == null) {
            return addressList;
        }
        Map<String, List<WalletBean>> walletMap = new HashMap<>();
        for (WalletBean ethWallet : walletBeanList) {
            List<WalletBean> walletBeans = walletMap.get("BTC");
            if (walletBeans == null) {
                walletBeans = new ArrayList<>();
                walletMap.put("BTC", walletBeans);
            }
            walletBeans.add(ethWallet);
        }
        for (Map.Entry<String, List<WalletBean>> stringListEntry : walletMap.entrySet()) {
            if (stringListEntry.getValue() == null || stringListEntry.getValue().size() <= 0) {
                continue;
            }
            StringBuilder address = new StringBuilder();
            for (WalletBean bean : stringListEntry.getValue()) {
                if (bean.getIsObserver()) {
                    address.append(bean.getAddress() + ",");
                } else {
                    if (!TextUtils.isEmpty(bean.getPrivateKey())) {
                        address.append(bean.getAddressToServer() + ",");
                    } else if (!TextUtils.isEmpty(bean.getMnemonic())) {
                        String childAddressForTypeToServer = ChildAddressDaoUtil.getChildAddressForTypeToServer(bean);
                        address.append(childAddressForTypeToServer + ",");
                    }
                }
            }
            //BTC/ETH/TRX/HT/BNB,OMNI_USDT
            if (stringListEntry.getKey().equals("BTC")) {
                CoinAddressBean coinAddressBeanUsdt = new CoinAddressBean();
                coinAddressBeanUsdt.coin = "OMNI_USDT";
                coinAddressBeanUsdt.address = address.toString();
                addressList.add(coinAddressBeanUsdt);
            }
            CoinAddressBean coinAddressBean = new CoinAddressBean();
            coinAddressBean.coin = stringListEntry.getKey();
            coinAddressBean.address = address.toString();
            addressList.add(coinAddressBean);
        }
        return addressList;
    }


    public static class CoinAddressBean {

        public CoinAddressBean() {
        }

        public CoinAddressBean(String address, String coin) {
            this.address = address;
            this.coin = coin;
        }

        public String address;
        public String coin;
    }

    /**
     * 获得所有钱包的订阅信息
     *
     * @return 钱包对象
     */
    public static List<String> getAllWalletSubAddress() {
        List<WalletBean> walletBeanList = walletDao.loadAll();
        List<String> addressList = new ArrayList<>();
        if (walletBeanList == null) {
            return addressList;
        }
        Map<String, List<WalletBean>> walletMap = new HashMap<>();
        for (WalletBean ethWallet : walletBeanList) {
            List<WalletBean> walletBeans = walletMap.get("BTC");
            if (walletBeans == null) {
                walletBeans = new ArrayList<>();
                walletMap.put("BTC", walletBeans);
            }
            walletBeans.add(ethWallet);
        }
        for (Map.Entry<String, List<WalletBean>> stringListEntry : walletMap.entrySet()) {
            if (stringListEntry.getValue() == null || stringListEntry.getValue().size() <= 0) {
                continue;
            }
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("ws_type", "transferList");
                jsonObject.put("sub", "1");
                jsonObject.put("coin", "BTC");
                StringBuilder addressBuilder = new StringBuilder();
                for (int i = 0; i < stringListEntry.getValue().size(); i++) {
                    WalletBean walletBean = stringListEntry.getValue().get(i);
                    if (walletBean.getIsObserver()) {
                        addressBuilder.append((i == 0 ? "" : ",") + walletBean.getAddress());
                    } else {
                        if (!TextUtils.isEmpty(walletBean.getPrivateKey())) {
                            addressBuilder.append((i == 0 ? "" : ",") + walletBean.getAddressToServer());
                        } else if (!TextUtils.isEmpty(walletBean.getMnemonic())) {
                            String childAddressForTypeToServer = ChildAddressDaoUtil.getChildAddressForTypeToServer(walletBean);
                            addressBuilder.append((i == 0 ? "" : ",") + childAddressForTypeToServer);
                        }
                    }
                }
                jsonObject.put("address", addressBuilder.toString());
                addressList.add(jsonObject.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return addressList;
    }

    /**
     * 查询所有钱包
     */
    public static List<WalletBean> loadAll() {
        return walletDao.loadAll();
    }

    /**
     * 查询所有钱包(除闪电网络钱包以外的非观察者) 按照时间倒序排序
     */
    public static List<WalletBean> loadAllOperate() {
        return walletDao.queryBuilder().where(WalletBeanDao.Properties.ExistType.notEq(Constants.EXIST_LIGHTNING), WalletBeanDao.Properties.IsObserver.eq(false))
                .orderDesc(WalletBeanDao.Properties.CreateTime).list();
    }

    /**
     * 查询所有钱包(闪电网络钱包) 按照时间倒序排序
     */
    public static List<WalletBean> loadAllLightning() {
        return walletDao.queryBuilder().where(WalletBeanDao.Properties.ExistType.eq(Constants.EXIST_LIGHTNING))
                .orderDesc(WalletBeanDao.Properties.CreateTime).list();
    }

    /**
     * 查询所有钱包(观察者) 按照时间倒序排序
     */
    public static List<WalletBean> loadAllObserver() {
        return walletDao.queryBuilder().where(WalletBeanDao.Properties.IsObserver.eq(true))
                .orderDesc(WalletBeanDao.Properties.CreateTime).list();
    }

    /**
     * 检查钱包名称是否存在
     *
     * @param name
     * @return
     */
    public static boolean walletNameChecking(String name) {
        List<WalletBean> ethWallets = loadAll();
        for (WalletBean ethWallet : ethWallets
        ) {
            if (TextUtils.equals(ethWallet.getName(), name)) {
                return true;
            }
        }
        return false;
    }

    public static WalletBean getWalletById(long walletId) {
        return walletDao.load(walletId);

    }

    /**
     * 设置isBackup为已备份
     */
    public static void updateWalletIsBack(WalletBean walletBean) {
        List<WalletBean> list = walletDao.queryBuilder().where(WalletBeanDao.Properties.Mnemonic.eq(walletBean.getMnemonic())).list();
        if (list == null) {
            return;
        }
        for (WalletBean bean : list) {
            bean.setIsBackup(true);
            walletDao.update(bean);
        }
    }

    /**
     * 以助记词检查钱包是否存在
     *
     * @param mnemonic
     * @return true if repeat
     */
    public static boolean checkRepeatByMenmonic(String mnemonic) {
        List<WalletBean> ethWallets = loadAll();
        for (WalletBean ethWallet : ethWallets
        ) {
            if (TextUtils.isEmpty(ethWallet.getMnemonic())) {
                LogUtils.d("wallet mnemonic empty");
                continue;
            }
            if (TextUtils.equals(ethWallet.getMnemonic().trim(), mnemonic.trim())) {
                LogUtils.d("aleady");
                return true;
            }
        }
        return false;
    }


    public static boolean isValid(String mnemonic) {
        return mnemonic.split(" ").length >= 12;
    }


    /**
     * 修改钱包名称
     *
     * @param walletId
     * @param name
     */
    public static WalletBean updateWalletName(long walletId, String name) {
        WalletBean wallet = walletDao.load(walletId);
        wallet.setName(name);
        walletDao.update(wallet);
        return wallet;
    }

    /**
     * 修改钱包主地址
     *
     * @param walletId
     * @param address
     */
    public static void updateWalletAddress(long walletId, String address) {
        WalletBean wallet = walletDao.load(walletId);
        wallet.setAddress(address);
        walletDao.update(wallet);
    }


    /**
     * 查看钱包中是否有密码钱包
     */
    public static boolean havaWalletPwd() {
        List<WalletBean> walletBeans = walletDao.loadAll();
        for (WalletBean walletBean : walletBeans) {
            if (!walletBean.getIsObserver()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否发送地址到服务器 更改本地
     *
     * @param walletBean
     */
    public static void updateWalletAddAddress(WalletBean walletBean) {
        WalletBean wallet = walletDao.load(walletBean.getId());
        wallet.setIsReportAddressToServer(true);
        walletDao.update(wallet);
    }


    /**
     * 是否发送地址到服务器 list
     *
     * @param walletBeans
     */
    public static void updateWalletAddAddress(List<WalletBean> walletBeans) {
        for (WalletBean walletBean : walletBeans) {
            updateWalletAddAddress(walletBean);
        }
    }

    public static List<WalletBean> loadNotSendServerAddressList() {
        List<WalletBean> walletBeans = walletDao.loadAll();
        List<WalletBean> walletList = new ArrayList<>();
        for (WalletBean walletBean : walletBeans) {
            if (!walletBean.getIsReportAddressToServer()) {
                walletList.add(walletBean);
            }
//            walletList.add(walletBean);
        }
        return walletList;
    }

    public static void addAddressBeanToList(List<AddressBean> notSendServer, WalletBean walletBean) {
        //上报服务器地址
        if (walletBean.getAddressToServer() == null) {
            return;
        }
        String[] split = walletBean.getAddressToServer().split(",");
        for (String address : split) {
            AddressBean addressBean = new AddressBean();
            addressBean.setType(AddressBean.TYPE_IMPORT);
            addressBean.setCoin("BTC");
            addressBean.setAddress(address);
            notSendServer.add(addressBean);
        }
    }

    /**
     * 更新BTC钱包地址
     *
     * @param currentWallet
     * @param childAddressBean
     */
    public static void updateWalletBTCAddress(WalletBean currentWallet, ChildAddressBean childAddressBean) {
        WalletBean wallet = walletDao.load(currentWallet.getId());
        wallet.setAddress(childAddressBean.getChildAddress());
        wallet.setChildAddressType(childAddressBean.getChildType());
        walletDao.update(wallet);
    }

    /**
     * 获得第一个钱包
     *
     * @return
     */
    public static WalletBean getFirstWalletBean() {
        List<WalletBean> walletBeans = walletDao.queryBuilder().where(WalletBeanDao.Properties.IsObserver.eq(false)).list();
        if (walletBeans == null || walletBeans.isEmpty()) {
            return null;
        }
        Collections.sort(walletBeans, (o1, o2) -> {
            if (o1.getCreateTime() - o2.getCreateTime() > 0) {
                return 1;
            } else {
                return -1;
            }
        });
        return walletBeans.get(0);
    }


    /**
     * 更改钱包的数据
     */
    public static void updateWallet(WalletBean walletBean) {
        walletDao.update(walletBean);
    }

    public static void deleteWallet(@Nullable WalletBean walletBean) {
        walletDao.delete(walletBean);
    }

    //删除钱包 根据时间 重新设置一个当前的的钱包 并返回
    public static WalletBean deleteBackWallet(@Nullable WalletBean walletBean) {
        walletDao.delete(walletBean);
        return setCurrentAfterDelete();
    }

    public static WalletBean setCurrentAfterDelete() {
        List<WalletBean> walletList = walletDao.loadAll();
        if (walletList != null && walletList.size() > 0) {
            Collections.sort(walletList, (o1, o2) -> {
                if (o1.getCreateTime() < o2.getCreateTime()) {
                    return -1;
                } else {
                    return 1;
                }
            });
            WalletBean ethWallet = walletList.get(0);
            ethWallet.setIsCurrent(true);
            walletDao.update(ethWallet);
            return ethWallet;
        } else {
            return null;
        }
    }

}
