package top.niunaijun.blackboxa.app

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import top.niunaijun.blackbox.BlackBoxCore
import top.niunaijun.blackbox.utils.FileLogger

class App : Application() {

    companion object {

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private lateinit var mContext: Context

        @JvmStatic
        fun getContext(): Context {
            return mContext
        }
    }

    override fun attachBaseContext(base: Context?) {
        try {
            super.attachBaseContext(base)

            if (base != null) {
                mContext = base
                try {
                    FileLogger.init(base)
                } catch (e: Throwable) {
                    Log.e("App", "Error in FileLogger.init: ${e.message}")
                }
            }

            try {
                BlackBoxCore.get().closeCodeInit()
            } catch (e: Exception) {
                Log.e("App", "Error in closeCodeInit: ${e.message}")
            }

            try {
                BlackBoxCore.get().onBeforeMainApplicationAttach(this, base)
            } catch (e: Exception) {
                Log.e("App", "Error in onBeforeMainApplicationAttach: ${e.message}")
            }

            try {
                AppManager.doAttachBaseContext(base!!)
            } catch (e: Exception) {
                Log.e("App", "Error in doAttachBaseContext: ${e.message}")
            }

            try {
                BlackBoxCore.get().onAfterMainApplicationAttach(this, base)
            } catch (e: Exception) {
                Log.e("App", "Error in onAfterMainApplicationAttach: ${e.message}")
            }
        } catch (e: Exception) {
            Log.e("App", "Critical error in attachBaseContext: ${e.message}", e)
            if (base != null) {
                mContext = base
            }
        }
    }

    override fun onCreate() {
        try {
            super.onCreate()
            AppManager.doOnCreate(mContext)
        } catch (e: Exception) {
            Log.e("App", "Error in onCreate: ${e.message}", e)
        }
    }
}
