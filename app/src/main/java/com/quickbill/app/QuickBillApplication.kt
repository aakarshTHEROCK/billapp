package com.quickbill.app

import android.app.Application
import com.quickbill.app.data.AppDatabase

/**
 * Application-wide singletons. Kept intentionally tiny: QuickBill has no
 * dependency-injection framework because there is nothing complex enough to
 * justify one - a single Room database and a SharedPreferences wrapper.
 */
class QuickBillApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
}
