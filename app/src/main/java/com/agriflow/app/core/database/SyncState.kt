package com.agriflow.app.core.database

/**
 * State representing synchronization status for offline drafts.
 */
enum class SyncState {
    PENDING,
    SYNCED,
    FAILED
}
