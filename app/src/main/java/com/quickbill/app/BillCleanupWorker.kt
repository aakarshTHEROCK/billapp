package com.quickbill.app

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/** Daily background job that enforces QuickBill's bill-retention policy. */
class BillCleanupWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return try {
            val app = applicationContext as QuickBillApplication
            app.database.billDao().deleteOlderThan(QuickBillApplication.retentionCutoffMillis())
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
