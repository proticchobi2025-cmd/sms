package com.example.smssheettracker.util

import com.example.smssheettracker.data.local.TransactionEntity
import com.example.smssheettracker.data.model.TransactionType
import com.example.smssheettracker.data.model.WalletType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SmsParser {
    private val amountRegex = Regex("""(?:Tk\.?|BDT\s?)(\d{1,3}(?:,\d{3})*(?:\.\d{1,2})?|\d+(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE)
    private val trxRegex = Regex("""(?:TrxID|TxnID|Transaction\sID)\s*[:#-]?\s*([A-Z0-9]{8,20})""", RegexOption.IGNORE_CASE)
    private val phoneRegex = Regex("""(?:from|to)\s+((?:\+?88)?01[3-9]\d{8})""", RegexOption.IGNORE_CASE)

    private val receiveKeywords = listOf("you have received", "received", "deposit")
    private val outKeywords = listOf("cash out", "sent", "payment", "withdraw")

    fun parse(sender: String?, body: String, now: Long = System.currentTimeMillis()): TransactionEntity? {
        val walletType = WalletType.fromSender(sender) ?: return null

        val trxId = trxRegex.find(body)?.groupValues?.get(1)?.uppercase(Locale.ROOT) ?: return null
        val amount = amountRegex.find(body)?.groupValues?.get(1)?.replace(",", "") ?: "0"
        val number = phoneRegex.find(body)?.groupValues?.get(1) ?: "N/A"

        val txType = classify(body)
        val recv = if (txType == TransactionType.RECEIVE) "YES" else "NO"
        val out = if (txType == TransactionType.OUT) "YES" else "NO"

        val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(now))
        val time = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(now))

        return TransactionEntity(
            walletSheet = walletType.sheetName,
            date = date,
            time = time,
            trxId = trxId,
            recv = recv,
            out = out,
            amount = amount,
            number = number,
            rawSms = body
        )
    }

    fun classify(body: String): TransactionType {
        val normalized = body.lowercase(Locale.ROOT)
        if (receiveKeywords.any { normalized.contains(it) }) return TransactionType.RECEIVE
        if (outKeywords.any { normalized.contains(it) }) return TransactionType.OUT
        return TransactionType.UNKNOWN
    }
}
