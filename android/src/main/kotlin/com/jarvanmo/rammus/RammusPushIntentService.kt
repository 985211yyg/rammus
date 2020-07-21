package com.jarvanmo.rammus

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.util.Log
import com.alibaba.sdk.android.push.AliyunMessageIntentService
import com.alibaba.sdk.android.push.notification.CPushMessage
import com.blankj.utilcode.util.NotificationUtils
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import java.lang.Exception


class RammusPushIntentService : AliyunMessageIntentService() {
    private val handler = Handler()

    override fun onNotificationRemoved(context: Context, messageId: String?) {
        Log.e("RammusPushIntentService", "onNotificationRemoved messageId is $messageId")
        handler.postDelayed({
            RammusPushHandler.methodChannel?.invokeMethod("onNotificationRemoved", messageId)
        }, 100)

    }

    override fun onNotification(context: Context, title: String?, summary: String?, extras: MutableMap<String, String>?) {
        Log.e("RammusPushIntentService", "onNotification title is $title, summary is $summary, extras: $extras")
        handler.postDelayed({
            RammusPushHandler.methodChannel?.invokeMethod("onNotification", mapOf(
                    "title" to title,
                    "summary" to summary,
                    "extras" to extras
            ))
        }, 100)
      
    }

    override fun onMessage(context: Context, message: CPushMessage) {
        Log.e("RammusPushIntentService", "onMessage title is ${message.title}, messageId is ${message.messageId}, content is ${message.content}")
        handler.postDelayed({
            RammusPushHandler.methodChannel?.invokeMethod("onMessageArrived", mapOf(
                    "appId" to message.appId,
                    "content" to message.content,
                    "messageId" to message.messageId,
                    "title" to message.title,
                    "traceInfo" to message.traceInfo
            ))
        }, 100)
    }

    override fun onNotificationOpened(p0: Context?, title: String?, summary: String?, extras: String?) {

        Log.e("RammusPushIntentService", "onNotificationOpened title is $title, summary is $summary, extras: $extras")
        handler.postDelayed({
            RammusPushHandler.methodChannel?.invokeMethod("onNotificationOpened", mapOf(
                    "title" to title,
                    "summary" to summary,
                    "extras" to extras
            ))
        }, 100)
    }

    override fun onNotificationReceivedInApp(p0: Context?, title: String?, summary: String?, extras: MutableMap<String, String>?, openType: Int, openActivity: String?, openUrl: String?) {
        Log.e("RammusPushIntentService", "onNotificationReceivedInApp title is $title, summary is $summary, extras: $extras")
        handler.postDelayed({
            RammusPushHandler.methodChannel?.invokeMethod("onNotificationReceivedInApp", mapOf(
                    "title" to title,
                    "summary" to summary,
                    "extras" to extras,
                    "openType" to openType,
                    "openActivity" to openActivity,
                    "openUrl" to openUrl
            ))
        }, 100)
    }

    override fun onNotificationClickedWithNoAction(context: Context, title: String?, summary: String?, extras: String?) {
        Log.e("RammusPushIntentService", "onNotificationClickedWithNoAction title is $title, summary is $summary, extras: $extras")
        handler.postDelayed({
            RammusPushHandler.methodChannel?.invokeMethod("onNotificationClickedWithNoAction", mapOf(
                    "title" to title,
                    "summary" to summary,
                    "extras" to extras
            ))
        }, 100)
    }


}