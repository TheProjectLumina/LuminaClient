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
     * Refresh the access token using the stored refresh token
     * @return accessToken
     */
    fun refresh(): String {
        val isCurrent = AccountManager.currentAccount == this
        val (accessToken, refreshToken) = platform.refreshToken(refreshToken, isAuthCode = false)
        this.refreshToken = refreshToken
        if (isCurrent) {
            AccountManager.selectAccount(this)
        }
        AccountManager.save()
        return accessToken
    }
}