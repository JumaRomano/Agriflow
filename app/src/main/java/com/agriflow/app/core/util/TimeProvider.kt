/**
 * Represents the interface [TimeProvider] providing core functionality within the application.
 */
package com.agriflow.app.core.util

interface TimeProvider {
    fun currentTimeMillis(): Long
}
