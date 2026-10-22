package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.PreferencesManager
import com.example.data.model.CountdownEvent
import com.example.data.model.GridDensity
import com.example.data.model.UserSettings
import com.example.data.model.YearInfo
import com.example.data.repository.EventRepository
import com.example.widget.WidgetRefreshResult
import com.example.widget.WidgetUpdateHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

sealed class WidgetRefreshStatus {
    object Idle : WidgetRefreshStatus()
    object Refreshing : WidgetRefreshStatus()
    data class Success(val message: String, val count: Int) : WidgetRefreshStatus()
    data class NoWidgets(val message: String) : WidgetRefreshStatus()
    data class Failure(val message: String) : WidgetRefreshStatus()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesManager = PreferencesManager(application)
    private val database = AppDatabase.getDatabase(application)
    private val eventRepository = EventRepository(database.eventDao())

    val settings: StateFlow<UserSettings> = preferencesManager.settingsFlow

    private val _currentDate = MutableStateFlow(LocalDate.now())
    val currentDate: StateFlow<LocalDate> = _currentDate.asStateFlow()

    val yearInfo: StateFlow<YearInfo> = combine(settings, _currentDate) { s, date ->
        YearInfo.current(countTodayAsRemaining = s.countTodayAsRemaining, now = date)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = YearInfo.current(now = LocalDate.now())
    )

    val events: StateFlow<List<CountdownEvent>> = eventRepository.eventsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Dot inspection bottom sheet state
    private val _selectedDayOfYear = MutableStateFlow<Int?>(null)
    val selectedDayOfYear: StateFlow<Int?> = _selectedDayOfYear.asStateFlow()

    // Add / Edit event dialog state
    private val _editingEvent = MutableStateFlow<CountdownEvent?>(null)
    val editingEvent: StateFlow<CountdownEvent?> = _editingEvent.asStateFlow()

    private val _isAddEventDialogOpen = MutableStateFlow(false)
    val isAddEventDialogOpen: StateFlow<Boolean> = _isAddEventDialogOpen.asStateFlow()

    private val _prefilledDateForNewEvent = MutableStateFlow<LocalDate?>(null)
    val prefilledDateForNewEvent: StateFlow<LocalDate?> = _prefilledDateForNewEvent.asStateFlow()

    private val _widgetRefreshStatus = MutableStateFlow<WidgetRefreshStatus>(WidgetRefreshStatus.Idle)
    val widgetRefreshStatus: StateFlow<WidgetRefreshStatus> = _widgetRefreshStatus.asStateFlow()

    init {
        viewModelScope.launch {
            eventRepository.seedSampleEventsIfEmpty()
            updateWidget()
        }
    }

    fun selectDay(dayOfYear: Int?) {
        _selectedDayOfYear.value = dayOfYear
    }

    fun openAddEventDialog(prefilledDate: LocalDate? = null) {
        _editingEvent.value = null
        _prefilledDateForNewEvent.value = prefilledDate
        _isAddEventDialogOpen.value = true
    }

    fun openEditEventDialog(event: CountdownEvent) {
        _editingEvent.value = event
        _prefilledDateForNewEvent.value = null
        _isAddEventDialogOpen.value = true
    }

    fun closeEventDialog() {
        _editingEvent.value = null
        _prefilledDateForNewEvent.value = null
        _isAddEventDialogOpen.value = false
    }

    fun saveEvent(
        id: Long = 0,
        title: String,
        targetDate: LocalDate,
        startDate: LocalDate = LocalDate.now(),
        emoji: String = "✨",
        accentHex: String = settings.value.accentHex,
        isStarred: Boolean = false
    ) {
        viewModelScope.launch {
            val event = CountdownEvent(
                id = id,
                title = title,
                targetDate = targetDate,
                startDate = startDate,
                emoji = emoji,
                accentHex = accentHex,
                isStarred = isStarred
            )
            if (id == 0L) {
                eventRepository.insertEvent(event)
            } else {
                eventRepository.updateEvent(event)
            }
            closeEventDialog()
            updateWidget()
        }
    }

    fun deleteEvent(event: CountdownEvent) {
        viewModelScope.launch {
            eventRepository.deleteEvent(event)
            updateWidget()
        }
    }

    fun toggleStarred(event: CountdownEvent) {
        viewModelScope.launch {
            eventRepository.updateEvent(event.copy(isStarred = !event.isStarred))
        }
    }

    fun setAccentColor(hex: String) {
        preferencesManager.updateAccent(hex)
        updateWidget()
    }

    fun setCountTodayAsRemaining(count: Boolean) {
        preferencesManager.updateCountToday(count)
        updateWidget()
    }

    fun setGridDensity(density: GridDensity) {
        preferencesManager.updateGridDensity(density)
    }

    fun setShowPercent(show: Boolean) {
        preferencesManager.updateShowPercent(show)
    }

    fun setWidgetBackgroundMode(mode: com.example.data.model.WidgetBackgroundMode) {
        preferencesManager.updateWidgetBackgroundMode(mode)
        updateWidget()
    }

    fun setWidgetBackgroundOpacity(opacity: Int) {
        preferencesManager.updateWidgetBackgroundOpacity(opacity)
        updateWidget()
    }

    fun seedSampleEvents() {
        viewModelScope.launch {
            eventRepository.seedSampleEvents()
            updateWidget()
        }
    }

    fun updateWidget() {
        WidgetUpdateHelper.updateAllWidgets(getApplication())
    }

    fun refreshWidgetManually() {
        viewModelScope.launch {
            _widgetRefreshStatus.value = WidgetRefreshStatus.Refreshing
            val result = WidgetUpdateHelper.refreshWidgets(getApplication())
            _widgetRefreshStatus.value = when (result) {
                is WidgetRefreshResult.Success -> WidgetRefreshStatus.Success(result.message, result.widgetCount)
                is WidgetRefreshResult.NoWidgets -> WidgetRefreshStatus.NoWidgets(result.message)
                is WidgetRefreshResult.Failure -> WidgetRefreshStatus.Failure(result.error)
            }
            delay(3500)
            _widgetRefreshStatus.value = WidgetRefreshStatus.Idle
        }
    }
}
