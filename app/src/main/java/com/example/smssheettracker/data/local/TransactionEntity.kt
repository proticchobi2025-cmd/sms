package com.example.smssheettracker.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    indices = [Index(value = ["trxId"], unique = true)]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val walletSheet: String,
    val date: String,
    val time: String,
    val trxId: String,
    val recv: String,
    val out: String,
    val amount: String,
    val number: String,
    val rawSms: String,
    val syncState: String = "PENDING",
    val createdAtMillis: Long = System.currentTimeMillis(),
    val lastError: String? = null
)
