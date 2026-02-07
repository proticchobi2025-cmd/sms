package com.example.smssheettracker.data.model

enum class WalletType(val senderId: String, val sheetName: String) {
    BKASH("25274", "bKash"),
    ROCKET("16216", "Rocket"),
    NAGAD("62423", "Nagad");

    companion object {
        fun fromSender(sender: String?): WalletType? {
            val normalized = sender?.trim()?.takeLast(5)
            return entries.firstOrNull { it.senderId == normalized }
        }
    }
}
