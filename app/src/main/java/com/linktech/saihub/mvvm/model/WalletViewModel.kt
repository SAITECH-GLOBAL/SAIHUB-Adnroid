package com.linktech.saihub.mvvm.model

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.linktech.saihub.R
import com.linktech.saihub.app.Constants
import com.linktech.saihub.app.Constants.TYPE_CREATE
import com.linktech.saihub.app.Constants.TYPE_IMPORT
import com.linktech.saihub.app.SaiHubApplication
import com.linktech.saihub.db.bean.ChildAddressBean
import com.linktech.saihub.db.bean.TokenInfoBean
import com.linktech.saihub.db.bean.WalletBean
import com.linktech.saihub.db.utils.ChildAddressDaoUtil
import com.linktech.saihub.db.utils.TokenDaoUtil
import com.linktech.saihub.db.utils.WalletDaoUtils
import com.linktech.saihub.db.utils.WalletDbUtil
import com.linktech.saihub.entity.wallet.ExtPubKeyEntity
import com.linktech.saihub.entity.wallet.js.JsMultiSigAddressEntity
import com.linktech.saihub.manager.CacheListManager
import com.linktech.saihub.manager.wallet.ManagerBTCWallet
import com.linktech.saihub.manager.wallet.MnemonicUtil
import com.linktech.saihub.mvvm.base.BaseViewModel
import com.linktech.saihub.mvvm.base.VmLiveData
import com.linktech.saihub.mvvm.base.VmState
import com.linktech.saihub.mvvm.base.launchVmRequest
import com.linktech.saihub.mvvm.repository.WalletRepository
import com.linktech.saihub.net.ex.ApiException
import com.linktech.saihub.util.LogUtils
import com.linktech.saihub.util.system.checkPublicKey
import com.linktech.saihub.util.walutils.PrivateKeyCheckUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class WalletViewModel() : BaseViewModel() {

    private val walletRepository by lazy { WalletRepository() }

    val mPhraseData by lazy { VmLiveData<List<String>>() }

    val mCreateData by lazy { VmLiveData<Boolean>() }

    val mImportData by lazy { VmLiveData<Boolean>() }

    //导入内容是否合法 合法类型
    val mValidateData by lazy { VmLiveData<Int>() }

    val mWalletListData by lazy { VmLiveData<List<WalletBean>>() }
    val mWalletListObserverData by lazy { VmLiveData<List<WalletBean>>() }

    val mCurrentWalletData by lazy { VmLiveData<WalletBean>() }

    val mSendServerData by lazy { VmLiveData<Boolean>() }

    val mMultiSigData by lazy { VmLiveData<Pair<List<Any>, String>>() }

    val mSingleSigJsData by lazy { VmLiveData<String>() }

    val mPushIntentData by lazy { VmLiveData<Pair<WalletBean, TokenInfoBean>>() }

    val mChangeAddressData by lazy { VmLiveData<Boolean>() }

    //创建助记词
    fun createPhrase(type: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                MnemonicUtil.createMnemonic(type)
            }.onSuccess {
                mPhraseData.postValue(VmState.Success(it))
            }.onFailure {
                mPhraseData.postValue(VmState.Fail(it))
            }
        }
    }

    //创建钱包
    fun createWallet(phrase: List<String?>?, walletBean: WalletBean, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                mCreateData.postValue(VmState.Loading)
                val createWalletBean =
                    ManagerBTCWallet.getInstance().createWalletForMnemonic(
                        walletBean.name, walletBean.password, phrase, walletBean.addressType,
                        walletBean.passphrase
                    )
                val aLong: Long = WalletDaoUtils.insertNewWallet(createWalletBean)
                createWalletBean.id = aLong
                TokenDaoUtil.insertTokenForWalType(aLong, createWalletBean, context)

                //BTC存储默认的子地址
                val childAddressNormalBeans: List<ChildAddressBean> =
                    ManagerBTCWallet.getInstance().getChildAddressNormalBeans(createWalletBean)
                ChildAddressDaoUtil.insertChildAddress(childAddressNormalBeans, createWalletBean)
//                sendAddressToServer(childAddressNormalBeans.map {
//                    it.childAddress
//                }, TYPE_CREATE)
                CacheListManager.instance?.saveWalletFlag(1)
                createWalletBean
            }.onSuccess {
                LogUtils.e("CreateWallet---error-WalletDaoUtils---insertNewWallet$it")
                //创建成功之后更新为当前选中钱包
                updateSelectWallet(it)
                mCreateData.postValue(VmState.Success(true))
            }.onFailure {
                mCreateData.postValue(VmState.Fail(it))
            }
        }
    }

    //导入钱包意图
    fun importIntentWallet(content: String) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                //先判断多签钱包因为多签格式也带空格0.0
                if (ManagerBTCWallet.getInstance().validMultiSig(content)) {
                    val parseMultiSigInput =
                        ManagerBTCWallet.getInstance().parseMultiSigInput(content)
                    if ((parseMultiSigInput[2] as? String)?.let {
                            WalletDbUtil.checkWalletPublicKey(it)
                        } == true) {
                        existTip()
                    } else {
                        mValidateData.postValue(VmState.Success(Constants.VALIDATE_MULTI_SIG))
                    }
                } else if (content.contains(" ")) {
                    //有空格是助记词
                    checkByMnemonic(content)
                } else if (PrivateKeyCheckUtil.checkBTCPrivateKey(content)) {
                    //按照私钥解析
                    //判断重复钱包
                    if (WalletDbUtil.checkWalletPrivateKey(content)) {
                        existTip()
                    } else {
                        mValidateData.postValue(VmState.Success(Constants.VALIDATE_PRIVATE_KEY))
                    }

                } else if (checkPublicKey(content)) {
                    //拓展公钥
                    val pubKey = if (content.contains("ExtPubKey")) {
                        val extPubKeyEntity =
                            Gson().fromJson(content, ExtPubKeyEntity::class.java)
                        extPubKeyEntity.extPubKey
                    } else {
                        content
                    }
                    if (pubKey?.let { WalletDbUtil.checkWalletPublicKey(it) } == true) {
                        existTip()
                    } else {
                        mValidateData.postValue(VmState.Success(Constants.VALIDATE_PUBLIC_KEY))
                    }
                } else if (ManagerBTCWallet.getInstance().validAddress(content)) {
                    if (WalletDbUtil.checkWalletAddress(content)) {
                        existTip()
                    } else {
                        mValidateData.postValue(VmState.Success(Constants.VALIDATE_ADDRESS))
                    }
                } else {
                    mValidateData.postValue(VmState.Error(ApiException(getString(R.string.wallet_import_error))))
                }
            }.onFailure {
                mValidateData.postValue(VmState.Error(ApiException(it.message)))
            }
        }

    }

    private fun checkByMnemonic(content: String) {
        runCatching {
            MnemonicUtil.validateMnemonics(content)
            true
        }.onSuccess {
            //判断重复钱包
            if (WalletDbUtil.checkWalletMnemonic(content)) {
                existTip()
            } else {
                mValidateData.postValue(VmState.Success(Constants.VALIDATE_MNEMONIC))
            }
        }.onFailure {
            mValidateData.postValue(VmState.Error(ApiException(getString(R.string.wallet_import_error))))
        }
    }

    //钱包已存在 提示
    private fun existTip() {
        mValidateData.postValue(
            VmState.Error(
                ApiException(
                    getString(R.string.same_wallet_tip)
                )
            )
        )
    }

    //导入钱包  除多签类型
    fun importWallet(
        importType: Int,
        walletIntentBean: WalletBean,
        content: String,
        context: Context
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                mImportData.postValue(VmState.Loading)
                val walletBean = when (importType) {
                    Constants.VALIDATE_MNEMONIC -> {
                        ManagerBTCWallet.getInstance().importMnemonic(
                            walletIntentBean.name,
                            content.split(" "),
                            walletIntentBean.password,
                            walletIntentBean.addressType,
                            walletIntentBean.passphrase
                        )
                    }
                    Constants.VALIDATE_PRIVATE_KEY -> {
                        ManagerBTCWallet.getInstance().loadWalletByPrivateKey(
                            walletIntentBean,
                            content,
                            walletIntentBean.password
                        )
                    }
                    Constants.VALIDATE_PUBLIC_KEY -> {
                        ManagerBTCWallet.getInstance()
                            .importPublicKey(walletIntentBean, content)
                    }
                    else -> {
                        ManagerBTCWallet.getInstance()
                            .importAddressObserver(walletIntentBean, content)
                    }
                }

                //存储默认代币
                val aLong: Long = WalletDaoUtils.insertNewWallet(walletBean)
                walletBean.id = aLong
                TokenDaoUtil.insertTokenForWalType(aLong, walletBean, context)

                //BTC存储默认的子地址
                val childAddressNormalBeans: List<ChildAddressBean> =
                    ManagerBTCWallet.getInstance().getChildAddressNormalBeans(walletBean)
                ChildAddressDaoUtil.insertChildAddress(childAddressNormalBeans, walletBean)
                //上报地址
//                sendAddressToServer(childAddressNormalBeans.map {
//                    it.childAddress
//                }, TYPE_IMPORT)
                CacheListManager.instance?.saveWalletFlag(1)
                walletBean
            }.onSuccess {
                updateSelectWallet(it)
                mImportData.postValue(VmState.Success(true))
            }.onFailure {
                mImportData.postValue(VmState.Error(ApiException(it.message)))
            }
        }
    }

    //解密 ur account 公钥类型钱包
    fun convertUrAccountToAccount(content: String) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                mSingleSigJsData.postValue(VmState.Loading)
                val jsData =
                    "javascript:convertUrAccountToAccount('${content}')"
                jsData
            }.onSuccess {
                mSingleSigJsData.postValue(VmState.Success(it))
            }.onFailure {
                mSingleSigJsData.postValue(VmState.Error(ApiException(it.message)))
            }
        }
    }

    //导入钱包  多签类型 解析数据获取address
    fun getMultiSigAddress(
        content: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                mMultiSigData.postValue(VmState.Loading)
                val output = ManagerBTCWallet.getInstance()
                    .parseMultiSigInput(content)
                val jsData =
                    "javascript:getMultiSigAddresses(${Gson().toJson(output[1])},${true},${
                        (output[0] as? String)?.split(",")?.get(0)
                    })"
                Pair(output, jsData)
            }.onSuccess {
                mMultiSigData.postValue(VmState.Success(it))
            }
        }
    }

    //导入多签钱包
    fun importMultiSigWallet(
        walletIntentBean: WalletBean,
        content: String,
        list: String,
        context: Context,
        output: List<Any>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val jsMultiSigAddressEntity =
                    Gson().fromJson(list.substring(1, list.length - 1).replace("\\", ""),
                        JsMultiSigAddressEntity::class.java)
                val walletBean = ManagerBTCWallet.getInstance().importMultiSig(
                    walletIntentBean,
                    content,
                    jsMultiSigAddressEntity.externalAddresses,
                    output)
                //存储默认代币
                val aLong: Long = WalletDaoUtils.insertNewWallet(walletBean)
                walletBean.id = aLong
                TokenDaoUtil.insertTokenForWalType(aLong, walletBean, context)

                //BTC存储默认的子地址
                val childAddressNormalBeans: List<ChildAddressBean> =
                    ManagerBTCWallet.getInstance()
                        .getChildAddressNormalBeans(jsMultiSigAddressEntity.externalAddresses,
                            jsMultiSigAddressEntity.internalAddress, walletBean)
                ChildAddressDaoUtil.insertChildAddress(childAddressNormalBeans, walletBean)
                //上报地址
//                sendAddressToServer(childAddressNormalBeans.map {
//                    it.childAddress
//                }, TYPE_IMPORT)
                CacheListManager.instance?.saveWalletFlag(1)
                walletBean
            }.onSuccess {
                updateSelectWallet(it)
                mImportData.postValue(VmState.Success(true))
            }.onFailure {
                mImportData.postValue(VmState.Error(ApiException(it.message)))
            }
        }

    }

    //获取钱包列表
    fun getWalletList() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                WalletDaoUtils.loadAllOperate()
            }.onSuccess {
                mWalletListData.postValue(VmState.Success(it))
            }.onFailure {
                mWalletListData.postValue(VmState.Error(ApiException(it)))
            }
        }
    }

    //获取观察者钱包列表
    fun getObserverWalletList() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                WalletDaoUtils.loadAllObserver()
            }.onSuccess {
                mWalletListObserverData.postValue(VmState.Success(it))
            }.onFailure {
                mWalletListObserverData.postValue(VmState.Error(ApiException(it)))
            }
        }
    }

    //更新选中钱包
    fun updateSelectWallet(walletBean: WalletBean) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                WalletDaoUtils.updateCurrent(walletBean.id)
            }.onSuccess {
                mCurrentWalletData.postValue(VmState.Success(it))
            }.onFailure {
                mCurrentWalletData.postValue(VmState.Error(ApiException(it)))
            }
        }
    }

    //根据地址判断是哪个钱包
    fun getWalletByAddress(address: String, coin: String) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val id = ChildAddressDaoUtil.getWalletByAddress(address)
                if (id == -1L)
                    return@launch
                val walletBean = WalletDaoUtils.getWalletById(id)
                val tokenList = TokenDaoUtil.loadTokenListForWalId(id)

                val filter = tokenList.filter {
                    it.tokenShort == coin
                }
                if (filter.isEmpty())
                    return@launch
                Pair<WalletBean, TokenInfoBean>(walletBean, filter[0])
            }.onSuccess {
                mPushIntentData.postValue(VmState.Success(it))
            }.onFailure {
                mPushIntentData.postValue(VmState.Error(ApiException(it)))
            }
        }
    }

    //更新钱包的主地址
    fun updateMainAddressWallet(address: String, walletBean: WalletBean) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                WalletDaoUtils.updateWalletAddress(walletBean.id, address)
            }.onSuccess {
                mChangeAddressData.postValue(VmState.Success(true))
            }.onFailure {
                mChangeAddressData.postValue(VmState.Error(ApiException(it)))
            }
        }
    }

    //上报地址
    private fun sendAddressToServer(addressList: List<String>, type: Int) {
        launchVmRequest({ walletRepository.sendAddressToServer(addressList, type) },
            mSendServerData)
    }
}