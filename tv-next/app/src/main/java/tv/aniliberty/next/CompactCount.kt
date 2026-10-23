package tv.aniliberty.next

/** One decimal at most, stable across device locales; never rounds a count upwards. */
fun compactCount(value: Long): String {
    val count = value.coerceAtLeast(0)
    if (count < 1000) return count.toString()
    return java.math.BigDecimal.valueOf(count)
        .movePointLeft(3)
        .setScale(1, java.math.RoundingMode.DOWN)
        .stripTrailingZeros()
        .toPlainString() + "к"
}
