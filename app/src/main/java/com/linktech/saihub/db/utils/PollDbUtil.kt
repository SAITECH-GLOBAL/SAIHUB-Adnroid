package com.linktech.saihub.db.utils

import com.linktech.saihub.db.bean.PollBean
import com.linktech.saihub.manager.DbManager

class PollDbUtil {

    companion object {

        fun getPollList(): MutableList<PollBean>? {
            val loadAll = DbManager.getInstance().daoSession.pollBeanDao?.loadAll()
            return loadAll?.toMutableList() ?: mutableListOf()
        }

        fun savePollItem(pollBean: PollBean): Long? {
            if (pollBean?.mId == null) {
                return DbManager.getInstance().daoSession.pollBeanDao?.insert(pollBean)
            } else {
                DbManager.getInstance().daoSession.pollBeanDao?.update(pollBean)
                return 0
            }
        }

        fun deleteItem(itemData: PollBean) {
            DbManager.getInstance().daoSession.pollBeanDao?.delete(itemData)
        }

        fun havePowerCount(): Int {
            val list = DbManager.getInstance().daoSession.pollBeanDao?.loadAll()
            return list?.size ?: 0
        }


    }


}