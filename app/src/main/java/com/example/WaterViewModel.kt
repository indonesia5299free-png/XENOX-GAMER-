package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.data.repository.UserPreferencesRepository
import com.example.data.repository.WaterRepository
import com.example.notifications.NotificationHelper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WaterViewModel(application: Application) : AndroidViewModel(application) {
    private val database = Room.databaseBuilder(
        application,
        AppDatabase::class.java, "water-db"
    ).build()

    private val waterRepository = WaterRepository(database.waterDao())
    private val preferencesRepository = UserPreferencesRepository(application)
    private val notificationHelper = NotificationHelper(application)

    val uiState: StateFlow<WaterUiState> = combine(
        waterRepository.getTodayIntakes(),
        waterRepository.getTodayTotalIntake(),
        preferencesRepository.dailyGoalMl,
        preferencesRepository.notificationsEnabled
    ) { intakes, total, goal, notificationsReq ->
        WaterUiState(
            intakes = intakes,
            totalIntakeMl = total ?: 0,
            dailyGoalMl = goal,
            notificationsEnabled = notificationsReq
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WaterUiState()
    )

    fun addWater(amountMl: Int) {
        viewModelScope.launch {
            waterRepository.addIntake(amountMl)
        }
    }

    fun removeWater(id: Int) {
        viewModelScope.launch {
            waterRepository.removeIntake(id)
        }
    }

    fun setDailyGoal(goalMl: Int) {
        viewModelScope.launch {
            preferencesRepository.setDailyGoal(goalMl)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setNotificationsEnabled(enabled)
            if (enabled) {
                notificationHelper.scheduleHydrationReminders()
            } else {
                notificationHelper.cancelHydrationReminders()
            }
        }
    }
}

data class WaterUiState(
    val intakes: List<com.example.data.local.WaterIntake> = emptyList(),
    val totalIntakeMl: Int = 0,
    val dailyGoalMl: Int = 2000,
    val notificationsEnabled: Boolean = true
) {
    val progress: Float
        get() = if (dailyGoalMl > 0) totalIntakeMl.toFloat() / dailyGoalMl else 0f
}
