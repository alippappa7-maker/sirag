import re

with open("app/src/main/java/com/siraj/app/core/utils/Resource.kt", "r") as f:
    content = f.read()

new_resource = """
package com.siraj.app.core.utils

import com.siraj.app.core.error.AppError

sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String, val error: AppError? = null) : Resource<Nothing>()
    object Loading : Resource<Nothing>()
}
"""

with open("app/src/main/java/com/siraj/app/core/utils/Resource.kt", "w") as f:
    f.write(new_resource.strip())
