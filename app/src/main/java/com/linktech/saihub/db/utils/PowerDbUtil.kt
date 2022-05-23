package com.linktech.saihub.db.utils

import com.linktech.saihub.db.bean.PowerBean
import com.linktech.saihub.greendao.PowerBeanDao
import com.linktech.saihub.greendao.WalletAddressBeanDao
import com.linktech.saihub.manager.DbManager

class PowerDbUtil {

    companion object {

        fun getPollList(): MutableList<PowerBean>? {
            val loadAll = DbManager.getInstance().daoSession.powerBeanDao?.loadAll()
            return loadAll?.toMutableList() ?: mutableListOf()
        }

        fun savePollItem(powerBean: PowerBean): Long? {
            if (powerBean?.mId == null) {
                return DbManager.getInstance().daoSession.powerBeanDao?.insert(powerBean)
            } else {
                DbManager.getInstance().daoSession.powerBeanDao?.update(powerBean)
                return 0
            }
        }

        fun deleteItem(itemData: PowerBean) {
            DbManager.getInstance().daoSession.powerBeanDao?.delete(itemData)
        }

        fun havePowerCount(): Int {
            val list = DbManager.getInstance().daoSession.powerBeanDao?.loadAll()
            return list?.size ?: 0
        }

        fun haveSamePowerNumber(powerNumber: String, powerEditNumber: String, isEdit: Boolean): Boolean {
            val list = DbManager.getInstance().daoSession.powerBeanDao?.queryBuilder()?.where(
                PowerBeanDao.Properties.Number.eq(powerNumber)
            )?.list()
            if (list == null || list.size == 0) {
                return false
            }
            if (isEdit) {
                val iterator = list.iterator()
                while (iterator.hasNext()) {
                    val next = iterator.next()
                    if (next.number == powerEditNumber) {
                        iterator.remove()
                    }
                }
            }

            return list != null && list.size > 0
        }


    }


}