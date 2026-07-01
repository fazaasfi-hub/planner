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

            val lowerText = fullText.lowercase()

            val isExpenseExplicit = lowerText.contains("berhasil kirim") ||
                                    lowerText.contains("berhasil transfer ke") ||
                                    lowerText.contains("transfer ke ") ||
                                    lowerText.contains("pembayaran") ||
                                    lowerText.contains("berhasil bayar") ||
                                    lowerText.contains("telah dibayar") ||
                                    lowerText.contains("ditarik") ||
                                    lowerText.contains("kirim uang") ||
                                    lowerText.contains("bayar di")

            val isIncomeExplicit = lowerText.contains("menerima") ||
                                   lowerText.contains("diterima") ||
                                   lowerText.contains("terima saldo") ||
                                   lowerText.contains("terima uang") ||
                                   lowerText.contains("masuk") ||
                                   lowerText.contains("ke kamu") ||
                                   lowerText.contains("ditambahkan") ||
                                   lowerText.contains("top up") ||
                                   lowerText.contains("isi saldo") ||
                                   lowerText.contains("transfer dari") ||
                                   lowerText.contains("dana kaget") ||
                                   lowerText.contains("cashback") ||
                                   lowerText.contains("refund") ||
                                   lowerText.contains("uang masuk") ||
                                   lowerText.contains("berhasil masuk") ||
                                   lowerText.contains("terima transfer")

            // Filter out promotional/ad notifications that often contain "Rp" but aren't transactions
            val isPromo = lowerText.contains("promo") ||
                          lowerText.contains("voucher") ||
                          lowerText.contains("dapatkan") ||
                          lowerText.contains("menangkan") ||
                          lowerText.contains("yuk") ||
                          lowerText.contains("buruan") ||
                          lowerText.contains("diskon") ||
                          lowerText.contains("potongan") ||
                          lowerText.contains("cek sekarang") ||
                          lowerText.contains("special for you") ||
                          lowerText.contains("khusus buat kamu")

            if (isPromo) {
                Log.d("NotificationListener", "Ignored DANA promo notification: $fullText")
                return
            }

            // Stricter type determination - must be explicit
            val type = when {
                isIncomeExplicit && !isExpenseExplicit -> "income"
                isExpenseExplicit && !isIncomeExplicit -> "expense"
                isIncomeExplicit && isExpenseExplicit -> {
                    // If both are present, try to find which one is more specific/dominant
                    if (lowerText.contains("menerima") || lowerText.contains("masuk")) "income"
                    else if (lowerText.contains("kirim") || lowerText.contains("bayar")) "expense"
                    else return // Too ambiguous
                }
                else -> return // Ignore if not explicitly income or expense
            }

            // Ignore very small amounts that might be noise (less than 100 rupiah)
            if (amount < 100) {
                Log.d("NotificationListener", "Ignored tiny DANA amount: $amount")
                return
            }

            // Clean up the description
            val description = when {
                type == "income" && (lowerText.contains("isi saldo") || lowerText.contains("top up")) -> "Top Up Saldo DANA (Otomatis)"
                type == "income" -> "DANA Masuk (Otomatis)"
                type == "expense" && (lowerText.contains("pembayaran") || lowerText.contains("bayar")) -> "Pembayaran DANA (Otomatis)"
                type == "expense" && (lowerText.contains("kirim") || lowerText.contains("transfer")) -> "Transfer DANA (Otomatis)"
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
