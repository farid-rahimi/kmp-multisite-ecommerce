package com.solutionium.shared.data.database

import androidx.room.Room
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

actual val databaseModule: Module = module {
    includes(databaseDaoModule)

    single<WooDatabase> {
        Room.databaseBuilder<WooDatabase>(
            name = databaseFilePath(),
            factory = { WooDatabaseConstructor.initialize() }
        )
            .setQueryCoroutineContext(Dispatchers.Default)
            .setDriver(androidx.sqlite.driver.bundled.BundledSQLiteDriver())
            .build()
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun databaseFilePath(): String {
    val applicationSupportPath = requireNotNull(
        NSSearchPathForDirectoriesInDomains(
            directory = NSApplicationSupportDirectory,
            domainMask = NSUserDomainMask,
            expandTilde = true
        ).firstOrNull() as? String
    ) { "Unable to resolve iOS Application Support directory for Room database" }

    val fileManager = NSFileManager.defaultManager
    if (!fileManager.fileExistsAtPath(applicationSupportPath)) {
        check(
            fileManager.createDirectoryAtPath(
                path = applicationSupportPath,
                withIntermediateDirectories = true,
                attributes = null,
                error = null
            )
        ) {
            "Unable to create iOS Application Support directory for Room database"
        }
    }

    return "$applicationSupportPath/woo_database.db"
}
