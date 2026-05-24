package com.snippyseat.app.core.model

enum class UserRole {
    USER,
    SELLER,
    ADMIN,
    ;

    companion object {
        fun from(value: String?): UserRole? = entries.firstOrNull { it.name == value }
    }
}
