package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.ReporterUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class UserSession(
    val isLoggedIn: Boolean = false,
    val phone: String = "",
    val name: String = "",
    val role: String = "reporter", // "admin" or "reporter"
    val assignedDistrict: String = "నారాయణపేట",
    val assignedState: String = "తెలంగాణ",
    val status: String = "active"
) {
    val isAdmin: Boolean get() = role.equals("admin", ignoreCase = true)
    val isReporter: Boolean get() = role.equals("reporter", ignoreCase = true)
}

class UserSessionManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("aadhya_user_session", Context.MODE_PRIVATE)

    private val _session = MutableStateFlow(loadSession())
    val session: StateFlow<UserSession> = _session.asStateFlow()

    private fun loadSession(): UserSession {
        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        val phone = prefs.getString(KEY_PHONE, "") ?: ""
        val name = prefs.getString(KEY_NAME, "") ?: ""
        val role = prefs.getString(KEY_ROLE, "reporter") ?: "reporter"
        val district = prefs.getString(KEY_DISTRICT, "నారాయణపేట") ?: "నారాయణపేట"
        val state = prefs.getString(KEY_STATE, "తెలంగాణ") ?: "తెలంగాణ"
        val status = prefs.getString(KEY_STATUS, "active") ?: "active"

        return UserSession(
            isLoggedIn = isLoggedIn,
            phone = phone,
            name = name,
            role = role,
            assignedDistrict = district,
            assignedState = state,
            status = status
        )
    }

    fun saveSession(
        phone: String,
        name: String,
        role: String,
        district: String = "నారాయణపేట",
        state: String = "తెలంగాణ",
        status: String = "active"
    ) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_PHONE, phone)
            .putString(KEY_NAME, name)
            .putString(KEY_ROLE, role)
            .putString(KEY_DISTRICT, district)
            .putString(KEY_STATE, state)
            .putString(KEY_STATUS, status)
            .apply()

        _session.value = UserSession(
            isLoggedIn = true,
            phone = phone,
            name = name,
            role = role,
            assignedDistrict = district,
            assignedState = state,
            status = status
        )
    }

    fun logout() {
        prefs.edit().clear().apply()
        _session.value = UserSession()
    }

    fun toReporterUser(): ReporterUser {
        val s = _session.value
        val today = SimpleDateFormat("dd MMM yyyy", Locale("te")).format(Date())
        return ReporterUser(
            id = s.phone.replace("+", "").ifBlank { "rep_${System.currentTimeMillis()}" },
            name = s.name.ifBlank { "ఆధ్య ప్రతినిధి" },
            phone = s.phone,
            role = s.role,
            status = s.status,
            assignedDistrict = s.assignedDistrict,
            assignedState = s.assignedState,
            registeredDate = today
        )
    }

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_PHONE = "phone"
        private const val KEY_NAME = "name"
        private const val KEY_ROLE = "role"
        private const val KEY_DISTRICT = "district"
        private const val KEY_STATE = "state"
        private const val KEY_STATUS = "status"
    }
}
