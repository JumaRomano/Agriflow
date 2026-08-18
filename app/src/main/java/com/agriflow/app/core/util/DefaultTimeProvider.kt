package com.agriflow.app.core.util

import javax.inject.Inject

class DefaultTimeProvider @Inject constructor() : TimeProvider {
    override fun currentTimeMillis(): Long {
        return System.currentTimeMillis()
    }
}
