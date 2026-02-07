package com.example.smssheettracker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.example.smssheettracker.domain.TransactionRepository
import com.example.smssheettracker.util.SmsParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        val fullBody = messages.joinToString(separator = "") { it.messageBody ?: "" }
        val sender = messages.firstOrNull()?.displayOriginatingAddress

        val parsed = SmsParser.parse(sender, fullBody)
        if (parsed == null) {
            Log.d(TAG, "Ignored SMS sender=$sender")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val repository = TransactionRepository(context)
            repository.insertAndQueueSync(parsed)
            Log.i(TAG, "Accepted SMS trx=${parsed.trxId}, wallet=${parsed.walletSheet}")
        }
    }

    companion object {
        private const val TAG = "SmsReceiver"
    }
}
