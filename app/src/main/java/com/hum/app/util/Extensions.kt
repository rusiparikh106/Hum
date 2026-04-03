package com.hum.app.util

import com.google.firebase.Timestamp
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun Double.toCurrencyString(): String {
    val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    return format.format(this)
}

fun Timestamp.toFormattedDate(): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(this.toDate())
}

fun Timestamp.toShortDate(): String {
    val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
    return sdf.format(this.toDate())
}

fun Date.toTimestamp(): Timestamp = Timestamp(this)

fun generateInviteCode(): String {
    val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    return (1..Constants.INVITE_CODE_LENGTH)
        .map { chars.random() }
        .joinToString("")
}

fun Timestamp.isThisMonth(): Boolean {
    val cal = Calendar.getInstance()
    val now = Calendar.getInstance()
    cal.time = this.toDate()
    return cal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
            cal.get(Calendar.MONTH) == now.get(Calendar.MONTH)
}

fun Timestamp.monthYearKey(): String {
    val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    return sdf.format(this.toDate())
}

fun Timestamp.monthLabel(): String {
    val sdf = SimpleDateFormat("MMM yy", Locale.getDefault())
    return sdf.format(this.toDate())
}
