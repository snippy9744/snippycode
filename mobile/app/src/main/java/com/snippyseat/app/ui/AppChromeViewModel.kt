package com.snippyseat.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snippyseat.app.core.connectivity.ConnectivityObserver
import com.snippyseat.app.core.notifications.InAppNotification
import com.snippyseat.app.core.notifications.NotificationEventStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class AppChromeViewModel @Inject constructor(
    connectivityObserver: ConnectivityObserver,
    notificationEventStore: NotificationEventStore,
) : ViewModel() {
    val isOnline: StateFlow<Boolean> = connectivityObserver.isOnline
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    private val mutableNotification = MutableStateFlow<InAppNotification?>(null)
    val notification: StateFlow<InAppNotification?> = mutableNotification.asStateFlow()

    init {
        viewModelScope.launch {
            notificationEventStore.events.collect { event ->
                mutableNotification.value = event
                delay(4_000)
                if (mutableNotification.value == event) {
                    mutableNotification.value = null
                }
            }
        }
    }

    fun dismissNotification() {
        mutableNotification.value = null
    }
}

