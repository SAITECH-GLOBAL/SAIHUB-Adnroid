/*
 * *******************************************************************
 *   @项目名称: BHex Android
 *   @文件名称: MediaContentCallback.java
 *   @Date: 19-5-22 下午9:30
 *   @Author: ppzhao
 *   @Description:
 *   @Copyright（C）: 2019 BlueHelix Inc.   All rights reserved.
 *   注意：本内容仅限于内部传阅，禁止外泄以及用于其他的商业目的.
 *  *******************************************************************
 */

package com.linktech.saihub.util.screenshot;

import android.net.Uri;

public interface MediaContentCallback {
    void onChange(Uri contentUri, boolean selfChange);
}
