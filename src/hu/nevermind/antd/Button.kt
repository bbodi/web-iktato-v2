package hu.nevermind.antd

import hu.nevermind.utils.hu.nevermind.antd.jsUndefined
import react.RBuilder
import react.RClass
import react.RHandler
import react.RProps
import react.dom.DOMProps

val ButtonComp: RClass<ButtonProps> = kotlinext.js.require("antd").Button
val ButtonGroupComp: RClass<ButtonGroupProps> = kotlinext.js.require("antd").Button.Group


object ButtonType {
    val primary: ButtonType = js("'primary'")
    val ghost: ButtonType = js("'ghost'")
    val dashed: ButtonType = js("'dashed'")
    val danger: ButtonType = js("'danger'")
    val default: ButtonType = js("'default'")
}

object ButtonSize {
    val default: ButtonSize = js("'default'")
    val large: ButtonSize = js("'large'")
    val small: ButtonSize = js("'small'")
}

fun ButtonSize.asString(): String = this.asDynamic()

object ButtonShape {
    val circle: ButtonShape = js("'circle'")
}

external interface ButtonGroupProps : RProps {
    var size: ButtonSize
}


external interface ButtonProps : RProps {
    var disabled: Boolean
    var ghost: Boolean
    var href: String
    var htmlType: String
    var icon: String?
    var loading: Any
    var shape: ButtonShape
    var size: ButtonSize
    var target: String
    var type: ButtonType
    var block: Boolean
    var onClick: () -> Unit
}

fun RBuilder.Button(
        disabled: Boolean = false,
        ghost: Boolean = false,
        href: String = jsUndefined,
        htmlType: kotlinx.html.ButtonType = jsUndefined,
        icon: String? = jsUndefined,
        loading: Boolean = false,
        loadingDelay: Int? = null,
        shape: ButtonShape = jsUndefined,
        size: ButtonSize = jsUndefined,
        target: String = jsUndefined,
        type: ButtonType = hu.nevermind.antd.ButtonType.default,
        block: Boolean = false,
        onClick: () -> Unit = jsUndefined,
        handler: RHandler<ButtonProps> = {}
) {
    ButtonComp {
        attrs.disabled = disabled
        attrs.ghost = ghost
        if (href != jsUndefined) {
            attrs.href = href
        }
        if (htmlType != undefined) {
            attrs.htmlType = htmlType.name
        }
        attrs.icon = icon
        attrs.loading = loadingDelay ?: loading
        attrs.shape = shape
        attrs.size = size
        attrs.target = target
        attrs.type = type
        attrs.onClick = onClick
        attrs.block = block
        handler()
    }
}


fun RBuilder.ButtonGroup(
        size: ButtonSize = jsUndefined,
        handler: RHandler<ButtonGroupProps> = {}
) {
    ButtonGroupComp {
        attrs.size = size
        handler()
    }
}