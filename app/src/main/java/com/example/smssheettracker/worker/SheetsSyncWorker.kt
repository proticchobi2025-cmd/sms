package com.example.smssheettracker.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.smssheettracker.data.local.AppDatabase
import com.example.smssheettracker.data.remote.GoogleSheetsApiHelper

class SheetsSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val dao = AppDatabase.getInstance(appContext).transactionDao()
    private val helper = GoogleSheetsApiHelper(appContext)

    override suspend fun doWork(): Result {
        val spreadsheetId = inputData.getString(KEY_SPREADSHEET_ID)
            ?: BuildConfigHolder.spreadsheetId

        if (spreadsheetId.isBlank()) {
            Log.e(TAG, "Spreadsheet ID missing")
            return Result.failure()
        }

        return try {
            val pending = dao.getPending()
            pending.forEach { row ->
                try {
                    helper.appendTransaction(spreadsheetId, row)
                    dao.updateSyncState(row.id, "SYNCED", null)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed row trx=${row.trxId}", e)
                    dao.updateSyncState(row.id, "PENDING", e.message)
                    throw e
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "SheetsSyncWorker"
        const val KEY_SPREADSHEET_ID = "spreadsheet_id"
    }
}

object BuildConfigHolder {
    const val spreadsheetId = "PUT_SPREADSHEET_ID_HERE"
}
