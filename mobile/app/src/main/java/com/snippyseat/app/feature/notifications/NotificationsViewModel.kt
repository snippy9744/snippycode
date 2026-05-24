package com.snippyseat.app.feature.notifications

import androidx.lifecycle.ViewModel
import com.snippyseat.app.data.user.UserNotification
import com.snippyseat.app.data.user.UserNotificationType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class NotificationsUiState(
    val notifications: List<UserNotification> = sampleNotifications,
)

@HiltViewModel
class NotificationsViewModel @Inject constructor() : ViewModel() {
    private val mutableUiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = mutableUiState.asStateFlow()

    fun dismiss(id: String) {
        mutableUiState.update { state ->
            state.copy(notifications = state.notifications.filterNot { it.id == id })
        }
    }
}

private val sampleNotifications = listOf(
    UserNotification(
        id = "noti-1",
        title = "Booking confirmed",
        body = "Red Chair Studio has confirmed your 6:30 PM slot.",
        group = "Today",
        type = UserNotificationType.BOOKING_CONFIRMED,
        timeLabel = "10 min ago",
    ),
    UserNotification(
        id = "noti-2",
        title = "Reminder",
        body = "Your appointment starts in 1 hour. Reach ready, skip the queue.",
        group = "Today",
        type = UserNotificationType.REMINDER,
        timeLabel = "1 hr ago",
    ),
    UserNotification(
        id = "noti-3",
        title = "Review request",
        body = "How was your haircut at Urban Trim Lounge?",
        group = "Earlier",
        type = UserNotificationType.REVIEW_REQUEST,
        timeLabel = "Yesterday",
    ),
    UserNotification(
        id = "noti-4",
        title = "Premium offer",
        body = "Get priority slots and no convenience fee this month.",
        group = "Earlier",
        type = UserNotificationType.PROMO,
        timeLabel = "Mon",
    ),
)
