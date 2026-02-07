package com.example.smssheettracker.data.remote

import android.content.Context
import com.example.smssheettracker.data.local.TransactionEntity
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.ValueRange
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import java.io.ByteArrayInputStream

class GoogleSheetsApiHelper(private val context: Context) {

    private val credentialStore = SecureCredentialStore(context)

    fun appendTransaction(spreadsheetId: String, item: TransactionEntity) {
        val credentialsJson = credentialStore.getServiceAccountJson()
            ?: throw IllegalStateException("Service account JSON not configured")

        val credentials = GoogleCredentials
            .fromStream(ByteArrayInputStream(credentialsJson.toByteArray()))
            .createScoped(listOf(SheetsScopes.SPREADSHEETS))

        val transport = GoogleNetHttpTransport.newTrustedTransport()
        val service = Sheets.Builder(
            transport,
            GsonFactory.getDefaultInstance(),
            HttpCredentialsAdapter(credentials)
        )
            .setApplicationName("SMS Sheet Tracker")
            .build()

        val row = listOf(
            "",
            item.date,
            item.time,
            item.trxId,
            item.recv,
            item.out,
            item.amount,
            item.number
        )

        val body = ValueRange().setValues(listOf(row))
        service.spreadsheets().values()
            .append(spreadsheetId, "${item.walletSheet}!A:H", body)
            .setValueInputOption("USER_ENTERED")
            .setInsertDataOption("INSERT_ROWS")
            .execute()
    }
}
