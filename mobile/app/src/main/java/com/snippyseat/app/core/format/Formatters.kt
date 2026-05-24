package com.snippyseat.app.core.format

import java.text.NumberFormat
import java.time.ZoneId
import java.util.Locale

val SnippyZoneId: ZoneId = ZoneId.of("Asia/Kolkata")

private val indianCurrencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
    maximumFractionDigits = 0
}

fun formatPrice(amount: Int): String = indianCurrencyFormat.format(amount)

