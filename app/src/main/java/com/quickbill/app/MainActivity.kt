package com.quickbill.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.quickbill.app.ui.QuickBillNavHost
import com.quickbill.app.ui.theme.QuickBillTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuickBillTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    QuickBillNavHost()
                }
            }
        }
    }
}
