package hu.nevermind.antd

import hu.nevermind.utils.hu.nevermind.antd.StringOrReactElement
import react.RBuilder
import react.RClass
import react.RHandler
import react.RProps

val TooltipComp: RClass<TooltipProps> = kotlinext.js.require("antd").Tooltip


external interface TooltipProps : RProps {
    var title: String
}

fun RBuilder.Tooltip(title: String,
                     handler: RHandler<TooltipProps> = {}) {
    TooltipComp {
        attrs.title = title
        handler()
    }
}