package com.quickbill.app

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.quickbill.app.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * Application-wide singletons. Kept intentionally tiny: QuickBill has no
 * dependency-injection framework because there is nothing complex enough to
 * justify one - a single Room database and a SharedPreferences wrapper.
 *
 * Also owns QuickBill's data-retention policy: bills older than
 * [RETENTION_DAYS] are deleted automatically, both right away whenever the
 * app is opened and once a day in the background via WorkManager (so bills
 * don't linger just because the app wasn't opened for a while).
 */
class QuickBillApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        const val RETENTION_DAYS = 30L

        fun retentionCutoffMillis(): Long =
            System.currentTimeMillis() - RETENTION_DAYS * 24 * 60 * 60 * 1000
    }

    override fun onCreate() {
        super.onCreate()

        // Run once immediately so old bills disappear the moment the app opens.
        appScope.launch {
            database.billDao().deleteOlderThan(retentionCutoffMillis())
        }

        // ...and keep enforcing it about once a day even across long stretches
        // where the app is installed but not opened.
        val cleanupRequest = PeriodicWorkRequestBuilder<BillCleanupWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "quickbill_bill_retention_cleanup",
            ExistingPeriodicWorkPolicy.KEEP,
            cleanupRequest
        )
    }
}
