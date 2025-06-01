package ru.radiationx.shared.ktx.android

import android.content.Context
import android.text.format.DateUtils
import java.util.Date


fun Date.relativeDate(context: Context) = DateUtils.getRelativeDateTimeString(
    context,
    time,
    DateUtils.MINUTE_IN_MILLIS,
    DateUtils.DAY_IN_MILLIS * 2,
    DateUtils.FORMAT_SHOW_TIME
).toString()