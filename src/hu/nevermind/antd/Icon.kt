package hu.nevermind.antd

import hu.nevermind.utils.Style
import hu.nevermind.utils.hu.nevermind.antd.jsUndefined
import react.RBuilder
import react.RClass
import react.RProps

val Icon: RClass<IconProps> = kotlinext.js.require("antd").Icon


external interface IconProps : RProps {
    var spin: Boolean
    var style: Style
    var type: String
}

fun RBuilder.Icon(
        type: String,
        spin: Boolean = false,
        style: Style = jsUndefined
) {
    Icon {
        attrs.spin = spin
        attrs.type = type
        attrs.style = style
    }
}