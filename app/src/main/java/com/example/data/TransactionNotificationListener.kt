package com.example.data

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class TransactionNotificationListener : NotificationListenerService() {

    private lateinit var database: PlannerDatabase
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        database = PlannerDatabase.getDatabase(applicationContext)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val packageName = sbn.packageName
        // Check if the notification comes from DANA (official package name: id.dana)
        // For testing, we can also check for test notification triggers
        if (packageName == "id.dana" || packageName == applicationContext.packageName) {
            val extras = sbn.notification.extras
            val title = (extras.getCharSequence(Notification.EXTRA_TITLE) ?: extras.getString(Notification.EXTRA_TITLE) ?: "").toString()
            val text = (extras.getCharSequence(Notification.EXTRA_TEXT) ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT) ?: "").toString()

            Log.d("NotificationListener", "Received notification from $packageName. Title: $title, Text: $text")

            parseAndInsertTransaction(title, text)
        }
    }

    private fun parseAndInsertTransaction(title: String, text: String) {
        val fullText = "$title $text"
        
        // Match Rp amounts (e.g., Rp 50.000, Rp500.000, Rp 1.250.000)
        val rpPattern = Pattern.compile("Rp\\s*([0-9.,]+)")
        val matcher = rpPattern.matcher(fullText)
        
        if (matcher.find()) {
            val rawAmount = matcher.group(1) ?: return
            val amount = rawAmount.replace(".", "").replace(",", "").toDoubleOrNull() ?: return

            // Determine if income or expense based on keywords popular in Indonesian payment gateways (DANA, GoPay, etc.)
            val isIncome = fullText.contains("menerima", ignoreCase = true) ||
                           fullText.contains("masuk", ignoreCase = true) ||
                           fullText.contains("dikirimkan ke kamu", ignoreCase = true) ||
                           fullText.contains("ditambahkan", ignoreCase = true) ||
                           fullText.contains("top up", ignoreCase = true) ||
                           fullText.contains("refund", ignoreCase = true)

            val type = if (isIncome) "income" else "expense"
            
            // Clean up the description
            val description = when {
                isIncome -> "DANA Masuk (Otomatis)"
                fullText.contains("pembayaran", ignoreCase = true) || fullText.contains("bayar", ignoreCase = true) -> "Pembayaran DANA (Otomatis)"
                fullText.contains("kirim", ignoreCase = true) || fullText.contains("transfer", ignoreCase = true) -> "Transfer DANA (Otomatis)"
                else -> "Transaksi DANA (Otomatis)"
            }

            serviceScope.launch {
                try {
                    val transaction = BudgetTransaction(
                        description = description,
                        amount = amount,
                        type = type,
                        date = System.currentTimeMillis()
                    )
                    database.plannerDao().insertTransaction(transaction)
                    Log.d("NotificationListener", "Successfully auto-logged DANA transaction: $description -> $amount")
                } catch (e: Exception) {
                    Log.e("NotificationListener", "Error auto-logging DANA transaction", e)
                }
            }
        }
    }
}
