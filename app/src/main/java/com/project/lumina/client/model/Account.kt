package com.project.lumina.client.model

import com.google.gson.annotations.SerializedName
import com.project.lumina.client.constructors.AccountManager
import com.project.lumina.relay.util.XboxDeviceInfo

class Account(
    @SerializedName("remark") var remark: String,
    @SerializedName("device") val platform: XboxDeviceInfo,
    @SerializedName("refresh_token") var refreshToken: String
) {

    /**
     * @return accessToken
     */
    fun refresh(): String {
        val isCurrent = AccountManager.currentAccount == this
        val (accessToken, refreshToken) = platform.refreshToken(refreshToken)
        this.refreshToken = refreshToken
        if (isCurrent) {
            
            AccountManager.selectAccount(this)
        }
        AccountManager.save()
        return accessToken
    }
}