package app.common

import kotlin.js.Date

val moment: MomentExt = kotlinext.js.require("moment")

external interface MomentExt {
    operator fun invoke(): Moment
    operator fun invoke(vararg date: Any): Moment
    operator fun invoke(input: String, format: String): Moment
}

fun latest(vararg args: Moment?): Moment? {
    return args.filterNotNull().maxBy { it.toDate().getTime() }
}

external interface Moment {
    fun format(formatString: String? = definedExternally): String
    fun valueOf(): Int
    fun clone(): Moment
    fun millisecond(value: Int? = definedExternally): Int
    fun second(value: Int? = definedExternally): Int
    fun minute(value: Int? = definedExternally): Int
    fun hour(value: Int? = definedExternally): Int
    fun date(value: Int? = definedExternally): Int
    fun day(value: Int? = definedExternally): Int
    fun weekday(value: Int? = definedExternally): Int
    fun isoWeekday(value: Int? = definedExternally): Int
    fun dayOfYear(value: Int? = definedExternally): Int
    fun week(value: Int? = definedExternally): Int
    fun isoWeek(value: Int? = definedExternally): Int
    fun month(value: Int? = definedExternally): Int
    fun quarter(value: Int? = definedExternally): Int
    fun year(value: Int? = definedExternally): Int
    fun weekYear(value: Int? = definedExternally): Int
    fun isoWeekYear(value: Int? = definedExternally): Int
    fun weeksInYear(): Int
    fun locale(localeName: String): Unit
    fun toDate(): Date
    fun isValid(): Boolean

    fun utcOffset(num: Int): Moment

    fun startOf(unit: String): Unit

    fun add(value: Int, unit: TimeUnit): Moment

    fun subtract(value: Int, unit: TimeUnit): Moment

    fun isSame(other: Any, granularity : Granularity? = definedExternally): Boolean
    fun isAfter(other: Any, granularity : Granularity? = definedExternally): Boolean
    fun isBefore(other: Any, granularity : Granularity? = definedExternally): Boolean
    fun isBetween(a: Any, b : Any): Boolean

    fun diff(date: Moment, timeUNit: TimeUnit): Int 
}

object Granularity {
    val Day: Granularity = js("'day'")
    val Month: Granularity = js("'month'")
    val Minute: Granularity = js("'minute'")
}

object TimeUnit {
    val Years: TimeUnit = js("'years'")
    val Month: TimeUnit = js("'months'")
    val Weeks: TimeUnit = js("'weeks'")
    val Days: TimeUnit = js("'days'")
    val Hours: TimeUnit = js("'hours'")
    val Minutes: TimeUnit = js("'minutes'")
    val Seconds: TimeUnit = js("'seconds'")
    val Milliseconds: TimeUnit = js("'milliseconds'")
}