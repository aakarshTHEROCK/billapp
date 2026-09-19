package com.quickbill.app.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.quickbill.app.ui.screens.BusinessDetailsScreen
import com.quickbill.app.ui.screens.CreateBillScreen
import com.quickbill.app.ui.screens.HistoryScreen
import com.quickbill.app.ui.screens.HomeScreen
import com.quickbill.app.ui.screens.PreviewScreen

object Routes {
    const val HOME = "home"
    const val CREATE = "create"
    const val HISTORY = "history"
    const val BUSINESS_DETAILS = "business_details"
    const val PREVIEW = "preview/{billId}"
    fun preview(billId: Long) = "preview/$billId"
}

@Composable
fun QuickBillNavHost() {
    val navController: NavHostController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onCreateBill = { navController.navigate(Routes.CREATE) },
                onBillHistory = { navController.navigate(Routes.HISTORY) },
                onBusinessDetails = { navController.navigate(Routes.BUSINESS_DETAILS) },
                onOpenBill = { id -> navController.navigate(Routes.preview(id)) }
            )
        }
        composable(Routes.CREATE) {
            CreateBillScreen(
                onBack = { navController.popBackStack() },
                onBillGenerated = { id ->
                    navController.navigate(Routes.preview(id)) {
                        popUpTo(Routes.HOME)
                    }
                }
            )
        }
        composable(Routes.HISTORY) {
            HistoryScreen(
                onBack = { navController.popBackStack() },
                onOpenBill = { id -> navController.navigate(Routes.preview(id)) }
            )
        }
        composable(Routes.BUSINESS_DETAILS) {
            BusinessDetailsScreen(onBack = { navController.popBackStack() })
        }
        composable(
            Routes.PREVIEW,
            arguments = listOf(navArgument("billId") { type = NavType.LongType })
        ) { backStackEntry ->
            val billId = backStackEntry.arguments?.getLong("billId") ?: -1L
            PreviewScreen(
                billId = billId,
                onBack = { navController.popBackStack() },
                onDone = { navController.popBackStack(Routes.HOME, false) }
            )
        }
    }
}
