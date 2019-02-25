package hu.nevermind.antd

import hu.nevermind.utils.hu.nevermind.antd.jsUndefined
import react.RBuilder
import react.RClass
import react.RHandler
import react.RProps

val BadgeComp: RClass<BadgeProps> = kotlinext.js.require("antd").Badge

object BadgeStatus {
    val success: BadgeStatus = js("'success'")
    val error: BadgeStatus = js("'error'")
    val default: BadgeStatus = js("'default'")
    val processing: BadgeStatus = js("'processing'")
    val warning: BadgeStatus = js("'warning'")
}

external interface BadgeProps : RProps {
    var count: Int
    var overflowCount: Int // = 99
    var title: String
    var showZero: Boolean
    var dot: Boolean
    var status: BadgeStatus
}

fun RBuilder.Badge(
        count: Int,
        overflowCount: Int = 99,
        title: String = jsUndefined,
        status: BadgeStatus = jsUndefined,
        handler: RHandler<BadgeProps> = {}
) {
    BadgeComp {
        attrs.count = count
        attrs.overflowCount = overflowCount
        if (title != jsUndefined) {
            attrs.title = title
        }
        if (status != jsUndefined) {
            attrs.status = status
        }
        handler()
    }
}