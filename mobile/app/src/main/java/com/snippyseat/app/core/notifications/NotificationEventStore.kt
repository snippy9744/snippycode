package com.snippyseat.app.core.notifications

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class InAppNotification(
    val title: String,
    val body: String,
    val deepLink: String? = null,
)

@Singleton
class NotificationEventStore @Inject constructor() {
    private val mutableEvents = MutableSharedFlow<InAppNotification>(extraBufferCapacity = 8)
    val events = mutableEvents.asSharedFlow()

    fun emit(notification: InAppNotification) {
        mutableEvents.tryEmit(notification)
    }
}

