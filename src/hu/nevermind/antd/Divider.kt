package hu.nevermind.antd

import hu.nevermind.utils.Style
import hu.nevermind.utils.hu.nevermind.antd.jsUndefined
import react.RBuilder
import react.RClass
import react.RProps

val Divider: RClass<DividerProps> = kotlinext.js.require("antd").Divider

object Orientation {
    val left: Orientation = js("'left'")
    val right: Orientation = js("'right'")
    val center: Orientation = js("'center'")
}

object DividerType {
    val horizontal: DividerType = js("'horizontal'")
    val vertical: DividerType = js("'vertical'")
}

external interface DividerProps : RProps {
    var className: String
    var dashed: Boolean
    var orientation: Orientation
    var style: Style
    var type: DividerType
}

fun RBuilder.Divider(className: String = "",
                     dashed: Boolean = false,
                     orientation: Orientation = Orientation.center,
                     style: Style = jsUndefined,
                     type: DividerType = DividerType.horizontal) {
    Divider {
        attrs.className = className
        attrs.dashed = dashed
        attrs.orientation = orientation
        attrs.style = style
        attrs.type = type
    }
}
