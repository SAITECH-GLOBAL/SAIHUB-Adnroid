package com.linktech.saihub

import com.linktech.saihub.manager.AES
import com.linktech.saihub.manager.wallet.btc.ParamsManagerBtc
import com.linktech.saihub.util.RegexUtils
import com.linktech.saihub.util.walutils.AddressCheckUtils
import org.bitcoinj.core.SegwitAddress
import org.bouncycastle.util.encoders.Hex
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    @Test
    fun testAddressCheck() {
        val isBtc1 = AddressCheckUtils.isBTCValidAddress("1PGY7TePi5yT6eNtjLAmECV4jQWNjVpxm4")
        System.out.println("address check =" + isBtc1)
        val isBtc2 = AddressCheckUtils.isBTCValidAddress("3AYQZzu8RQvxHVAqnNMwuYkKPCMfYjPua7")
        System.out.println("address check =" + isBtc2)
        val isBtc3 = AddressCheckUtils.isBTCValidAddress("bc1qq4xl2c5p4807z49mgcuxyjrqekc84emjpcgmzu")
        System.out.println("address check =" + isBtc3)
    }


    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testUrl() {
        val script = "0020720447b536b12b41026ffbdc7561c9c3daf73246f761f18295e250c3c9d3c897"
        val toBech32 =
            SegwitAddress.fromHash(ParamsManagerBtc.getParams(), Hex.decode(script.length.let {
                script.substring(4, it)
            })).toBech32()
        println("address-------$toBech32")
    }

    @Test
    fun testSlice() {
        var str = "1234567890"
        val slice = str.slice(IntRange(str.length - 4, str.length - 1))
        var str1 = "1234567890"
        System.out.println(slice)
    }

    @Test
    fun test1() {
        var s = "hello"
        s = "world" + "java"
        System.out.println(s)
    }

    @Test
    fun testPassWord() {
        val decrypt = AES.decrypt("APQYdhgBsOAEfxMf5ljfG2YkqBx4qFn1nDl9aL4y7H4=")
        System.out.println(decrypt)
    }
}