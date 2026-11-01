package com.example.byhm

import android.app.Application
import android.content.Context
import com.example.byhm.data.local.AppDatabase
import com.example.byhm.data.repository.IncidentRepository
import org.osmdroid.config.Configuration

class DisasterResponderApplication : Application() {

    lateinit var database: AppDatabase
    lateinit var incidentRepository: IncidentRepository

    override fun onCreate() {
        super.onCreate()

        // Global osmdroid performance & caching configuration
        val config = Configuration.getInstance()
        config.load(this, getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        config.userAgentValue = "DisasterResponder-Android"

        // Optimize tile download threading and disk/memory cache performance
        config.tileDownloadThreads = 5.toShort()
        config.tileDownloadMaxQueueSize = 30.toShort()
        config.tileFileSystemCacheMaxBytes = 100L * 1024L * 1024L // 100MB disk tile cache
        config.tileFileSystemCacheTrimBytes = 80L * 1024L * 1024L  // 80MB trim threshold
        config.expirationExtendedDuration = 7L * 24L * 60L * 60L * 1000L // 7 days extended cache for fast loading & offline availability
        config.isMapViewHardwareAccelerated = true

        database = AppDatabase.getDatabase(this)
        incidentRepository = IncidentRepository(database.incidentDao(), this)
    }
}
