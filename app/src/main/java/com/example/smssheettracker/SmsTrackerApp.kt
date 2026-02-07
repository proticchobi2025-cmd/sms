package com.example.smssheettracker

import android.app.Application
import android.util.Log
import androidx.work.Configuration

class SmsTrackerApp : Application(), Configuration.Provider {

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .build()
}
