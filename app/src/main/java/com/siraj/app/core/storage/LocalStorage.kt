package com.siraj.app.core.storage

interface LocalStorage {
    fun saveString(
        key: String,
        value: String,
    )

    fun getString(
        key: String,
        defaultValue: String? = null,
    ): String?

    fun clear()
}

// Will be implemented later with DataStore or SharedPreferences
class SirajLocalStorage : LocalStorage {
    override fun saveString(
        key: String,
        value: String,
    ) {
        // Implementation
    }

    override fun getString(
        key: String,
        defaultValue: String?,
    ): String? = defaultValue

    override fun clear() {
        // Implementation
    }
}
