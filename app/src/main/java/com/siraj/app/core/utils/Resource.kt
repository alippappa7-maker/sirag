package com.siraj.app.core.utils

import com.siraj.app.core.error.AppError

sealed class Resource<out T> {
    data class Success<out T>(
        val data: T,
    ) : Resource<T>()

    data class Error(
        val message: String,
        val error: AppError? = null,
    ) : Resource<Nothing>()

    object Loading : Resource<Nothing>()
}
