package com.example.smssheettracker.domain

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.smssheettracker.data.local.AppDatabase
import com.example.smssheettracker.data.local.TransactionEntity
import com.example.smssheettracker.worker.SheetsSyncWorker
import java.util.concurrent.TimeUnit

class TransactionRepository(context: Context) {

    private val appContext = context.applicationContext
    private val dao = AppDatabase.getInstance(appContext).transactionDao()

    suspend fun insertAndQueueSync(entity: TransactionEntity): Boolean {
        val inserted = dao.insert(entity) > 0
        if (inserted) {
            scheduleSync(appContext)
        } else {
            Log.i(TAG, "Duplicate TrxID ignored: ${entity.trxId}")
        }
        return inserted
    }

    companion object {
        private const val TAG = "TransactionRepository"
        private const val UNIQUE_SYNC_WORK = "sheets_sync_work"

        fun scheduleSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<SheetsSyncWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                UNIQUE_SYNC_WORK,
                ExistingWorkPolicy.KEEP,
                request
            )
        }
    }
}
