package com.jarvanmo.rammus

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.BadgeIconType
import com.alibaba.sdk.android.push.CommonCallback
import com.alibaba.sdk.android.push.huawei.HuaWeiRegister
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory
import com.alibaba.sdk.android.push.register.*
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.NotificationUtils
import com.blankj.utilcode.util.Utils
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar


class RammusPlugin() : MethodCallHandler, FlutterPlugin, ActivityAware {
    lateinit var applicationContext: Context;
    lateinit var activity: Activity

    companion object {
        private const val TAG = "RammusPlugin"
        private val handler = Handler()

        @JvmStatic
        fun registerWith(registrar: Registrar) {
            val channel = MethodChannel(registrar.messenger(), "com.jarvanmo/rammus")
            RammusPushHandler.methodChannel = channel
            channel.setMethodCallHandler(RammusPlugin())
        }

        @JvmStatic
        fun initPushService(applicationContext: Application) {
            RammusPlugin().applicationContext = applicationContext
            PushServiceFactory.init(applicationContext)
            PushServiceFactory.getCloudPushService().register(applicationContext, object : CommonCallback {
                override fun onSuccess(response: String?) {
                    handler.postDelayed({
                        RammusPushHandler.methodChannel?.invokeMethod("initCloudChannelResult", mapOf(
                                "isSuccessful" to true,
                                "response" to response
                        ))
                    }, 2000)

                }

                override fun onFailed(errorCode: String?, errorMessage: String?) {
                    handler.postDelayed({
                        RammusPushHandler.methodChannel?.invokeMethod("initCloudChannelResult", mapOf(
                                "isSuccessful" to false,
                                "errorCode" to errorCode,
                                "errorMessage" to errorMessage
                        ))
                    }, 2000)

                }
            })
            PushServiceFactory.getCloudPushService().setPushIntentService(RammusPushIntentService::class.java)
            val appInfo = applicationContext.packageManager
                    .getApplicationInfo(applicationContext.packageName, PackageManager.GET_META_DATA)
            val xiaomiAppId = appInfo.metaData.getString("com.xiaomi.push.client.app_id")
            val xiaomiAppKey = appInfo.metaData.getString("com.xiaomi.push.client.app_key")
            if ((xiaomiAppId != null && xiaomiAppId.isNotBlank())
                    && (xiaomiAppKey != null && xiaomiAppKey.isNotBlank())) {
                Log.d(TAG, "正在注册小米推送服务...")
                MiPushRegister.register(applicationContext, xiaomiAppId, xiaomiAppKey)
            }
            val huaweiAppId = appInfo.metaData.getString("com.huawei.hms.client.appid")
            if (huaweiAppId != null && huaweiAppId.toString().isNotBlank()) {
                Log.d(TAG, "正在注册华为推送服务...")
                HuaWeiRegister.register(applicationContext as Application?)
            }
            val oppoAppKey = appInfo.metaData.getString("com.oppo.push.client.app_key")
            val oppoAppSecret = appInfo.metaData.getString("com.oppo.push.client.app_secret")
            if ((oppoAppKey != null && oppoAppKey.isNotBlank())
                    && (oppoAppSecret != null && oppoAppSecret.isNotBlank())) {
                Log.d(TAG, "正在注册Oppo推送服务...")
                OppoRegister.register(applicationContext, oppoAppKey, oppoAppSecret)
            }
            val vivoAppId = appInfo.metaData.getString("com.vivo.push.app_id")
            val vivoApiKey = appInfo.metaData.getString("com.vivo.push.api_key")
            if ((vivoAppId != null && vivoAppId.isNotBlank())
                    && (vivoApiKey != null && vivoApiKey.isNotBlank())) {
                Log.d(TAG, "正在注册Vivo推送服务...")
                VivoRegister.register(applicationContext)
            }

        }


    }


    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        val channel = MethodChannel(binding.binaryMessenger, "com.jarvanmo/rammus")
        RammusPushHandler.methodChannel = channel
        channel.setMethodCallHandler(this)
    }


    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    }


    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "deviceId" -> result.success(PushServiceFactory.getCloudPushService().deviceId)
            "turnOnPushChannel" -> turnOnPushChannel(result)
            "turnOffPushChannel" -> turnOffPushChannel(result)
            "checkPushChannelStatus" -> checkPushChannelStatus(result)
            "bindAccount" -> bindAccount(call, result)
            "unbindAccount" -> unbindAccount(result)
            "bindTag" -> bindTag(call, result)
            "unbindTag" -> unbindTag(call, result)
            "listTags" -> listTags(call, result)
            "addAlias" -> addAlias(call, result)
            "removeAlias" -> removeAlias(call, result)
            "listAliases" -> listAliases(result)
            "setupNotificationManager" -> setupNotificationManager(call, result)
            "bindPhoneNumber" -> bindPhoneNumber(call, result)
            "unbindPhoneNumber" -> unbindPhoneNumber(result)
            else -> result.notImplemented()
        }

    }


    private fun turnOnPushChannel(result: Result) {
        val pushService = PushServiceFactory.getCloudPushService()
        pushService.turnOnPushChannel(object : CommonCallback {
            override fun onSuccess(response: String?) {
                result.success(mapOf(
                        "isSuccessful" to true,
                        "response" to response
                ))

            }

            override fun onFailed(errorCode: String?, errorMessage: String?) {
                result.success(mapOf(
                        "isSuccessful" to false,
                        "errorCode" to errorCode,
                        "errorMessage" to errorMessage
                ))
            }
        })
    }

    private fun turnOffPushChannel(result: Result) {
        val pushService = PushServiceFactory.getCloudPushService()
        pushService.turnOffPushChannel(object : CommonCallback {
            override fun onSuccess(response: String?) {
                result.success(mapOf(
                        "isSuccessful" to true,
                        "response" to response
                ))

            }

            override fun onFailed(errorCode: String?, errorMessage: String?) {
                result.success(mapOf(
                        "isSuccessful" to false,
                        "errorCode" to errorCode,
                        "errorMessage" to errorMessage
                ))
            }
        })
    }


    private fun checkPushChannelStatus(result: Result) {
        val pushService = PushServiceFactory.getCloudPushService()
        pushService.checkPushChannelStatus(object : CommonCallback {
            override fun onSuccess(response: String?) {
                result.success(mapOf(
                        "isSuccessful" to true,
                        "response" to response
                ))

            }

            override fun onFailed(errorCode: String?, errorMessage: String?) {
                result.success(mapOf(
                        "isSuccessful" to false,
                        "errorCode" to errorCode,
                        "errorMessage" to errorMessage
                ))
            }
        })
    }


    private fun bindAccount(call: MethodCall, result: Result) {
        val pushService = PushServiceFactory.getCloudPushService()
        pushService.bindAccount(call.arguments as String?, object : CommonCallback {
            override fun onSuccess(response: String?) {
                result.success(mapOf(
                        "isSuccessful" to true,
                        "response" to response
                ))

            }

            override fun onFailed(errorCode: String?, errorMessage: String?) {
                result.success(mapOf(
                        "isSuccessful" to false,
                        "errorCode" to errorCode,
                        "errorMessage" to errorMessage
                ))
            }
        })
    }


    private fun unbindAccount(result: Result) {
        val pushService = PushServiceFactory.getCloudPushService()
        pushService.unbindAccount(object : CommonCallback {
            override fun onSuccess(response: String?) {
                result.success(mapOf(
                        "isSuccessful" to true,
                        "response" to response
                ))

            }

            override fun onFailed(errorCode: String?, errorMessage: String?) {
                result.success(mapOf(
                        "isSuccessful" to false,
                        "errorCode" to errorCode,
                        "errorMessage" to errorMessage
                ))
            }
        })
    }


    private fun bindPhoneNumber(call: MethodCall, result: Result) {
        val pushService = PushServiceFactory.getCloudPushService()
        pushService.bindPhoneNumber(call.arguments as String?, object : CommonCallback {
            override fun onSuccess(response: String?) {
                result.success(mapOf(
                        "isSuccessful" to true,
                        "response" to response
                ))

            }

            override fun onFailed(errorCode: String?, errorMessage: String?) {
                result.success(mapOf(
                        "isSuccessful" to false,
                        "errorCode" to errorCode,
                        "errorMessage" to errorMessage
                ))
            }
        })
    }


    private fun unbindPhoneNumber(result: Result) {
        val pushService = PushServiceFactory.getCloudPushService()
        pushService.unbindPhoneNumber(object : CommonCallback {
            override fun onSuccess(response: String?) {
                result.success(mapOf(
                        "isSuccessful" to true,
                        "response" to response
                ))

            }

            override fun onFailed(errorCode: String?, errorMessage: String?) {
                result.success(mapOf(
                        "isSuccessful" to false,
                        "errorCode" to errorCode,
                        "errorMessage" to errorMessage
                ))
            }
        })
    }


    private fun bindTag(call: MethodCall, result: Result) {
        val target = call.argument("target") ?: 1
        val tagsInArrayList = call.argument("tags") ?: arrayListOf<String>()
        val alias = call.argument<String?>("alias")

        val arr = arrayOfNulls<String>(tagsInArrayList.size)
        val tags: Array<String> = tagsInArrayList.toArray(arr)

        val pushService = PushServiceFactory.getCloudPushService()

        //绑定标签
        pushService.bindTag(target, tags, alias, object : CommonCallback {
            override fun onSuccess(response: String?) {
                result.success(mapOf(
                        "isSuccessful" to true,
                        "response" to response
                ))

            }

            override fun onFailed(errorCode: String?, errorMessage: String?) {
                result.success(mapOf(
                        "isSuccessful" to false,
                        "errorCode" to errorCode,
                        "errorMessage" to errorMessage
                ))
            }
        })


    }


    private fun unbindTag(call: MethodCall, result: Result) {
        val target = call.argument("target") ?: 1
        val tagsInArrayList = call.argument("tags") ?: arrayListOf<String>()
        val alias = call.argument<String?>("alias")

        val arr = arrayOfNulls<String>(tagsInArrayList.size)
        val tags: Array<String> = tagsInArrayList.toArray(arr)

        val pushService = PushServiceFactory.getCloudPushService()

        pushService.unbindTag(target, tags, alias, object : CommonCallback {
            override fun onSuccess(response: String?) {
                result.success(mapOf(
                        "isSuccessful" to true,
                        "response" to response
                ))

            }

            override fun onFailed(errorCode: String?, errorMessage: String?) {
                result.success(mapOf(
                        "isSuccessful" to false,
                        "errorCode" to errorCode,
                        "errorMessage" to errorMessage
                ))
            }
        })
    }

    private fun listTags(call: MethodCall, result: Result) {
        val target = call.arguments as Int? ?: 1
        val pushService = PushServiceFactory.getCloudPushService()
        pushService.listTags(target, object : CommonCallback {
            override fun onSuccess(response: String?) {
                result.success(mapOf(
                        "isSuccessful" to true,
                        "response" to response
                ))

            }

            override fun onFailed(errorCode: String?, errorMessage: String?) {
                result.success(mapOf(
                        "isSuccessful" to false,
                        "errorCode" to errorCode,
                        "errorMessage" to errorMessage
                ))
            }
        })
    }


    private fun addAlias(call: MethodCall, result: Result) {
        val alias = call.arguments as String?
        val pushService = PushServiceFactory.getCloudPushService()
        pushService.addAlias(alias, object : CommonCallback {
            override fun onSuccess(response: String?) {
                result.success(mapOf(
                        "isSuccessful" to true,
                        "response" to response
                ))

            }

            override fun onFailed(errorCode: String?, errorMessage: String?) {
                result.success(mapOf(
                        "isSuccessful" to false,
                        "errorCode" to errorCode,
                        "errorMessage" to errorMessage
                ))
            }
        })
    }

    private fun removeAlias(call: MethodCall, result: Result) {
        val alias = call.arguments as String?
        val pushService = PushServiceFactory.getCloudPushService()
        pushService.removeAlias(alias, object : CommonCallback {
            override fun onSuccess(response: String?) {
                result.success(mapOf(
                        "isSuccessful" to true,
                        "response" to response
                ))

            }

            override fun onFailed(errorCode: String?, errorMessage: String?) {
                result.success(mapOf(
                        "isSuccessful" to true,
                        "errorCode" to errorCode,
                        "errorMessage" to errorMessage
                ))
            }
        })
    }

    private fun listAliases(result: Result) {
        val pushService = PushServiceFactory.getCloudPushService()
        pushService.listAliases(object : CommonCallback {
            override fun onSuccess(response: String?) {
                result.success(mapOf(
                        "isSuccessful" to true,
                        "response" to response
                ))

            }

            override fun onFailed(errorCode: String?, errorMessage: String?) {
                result.success(mapOf(
                        "isSuccessful" to false,
                        "errorCode" to errorCode,
                        "errorMessage" to errorMessage
                ))
            }
        })
    }


    private fun setupNotificationManager(call: MethodCall, result: Result) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notifications = call.arguments as List<Map<String, Any?>>
            val data = notifications[0]
            val channelConfig = NotificationUtils.ChannelConfig(data["id"].toString(),
                    data["name"].toString(), data["importance"] as Int)
            channelConfig.setDescription(data["description"].toString())
            channelConfig.setShowBadge(true)
            NotificationUtils.notify(123, channelConfig) {
                it.setSmallIcon(android.R.drawable.stat_notify_chat)
                        .setContentTitle(data["name"].toString())
                        .setContentText(data["description"].toString())
                        .setContentIntent(PendingIntent.getActivity(Utils.getApp(),
                                1111,
                                Intent(activity, activity.javaClass), 0))
                        .setAutoCancel(true)
            }
        }
        result.success(true)
    }


    override fun onDetachedFromActivity() {
    }


    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    }


    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity

    }


    override fun onDetachedFromActivityForConfigChanges() {
    }


}
