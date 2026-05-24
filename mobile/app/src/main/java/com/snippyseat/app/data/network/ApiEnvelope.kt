package com.snippyseat.app.data.network

data class ApiEnvelope<T>(
    val success: Boolean = true,
    val statusCode: Int? = null,
    val message: String? = null,
    val data: T? = null,
)
