package hu.nevermind.antd

import hu.nevermind.utils.hu.nevermind.antd.jsUndefined
import org.w3c.dom.events.Event
import react.RBuilder
import react.RClass
import react.RHandler
import react.RProps

val RadioComp: RClass<RadioProps> = kotlinext.js.require("antd").Radio
val RadioGroupComp: RClass<RadioGroupProps> = kotlinext.js.require("antd").Radio.Group
val RadioButtonComp: RClass<RadioButtonProps> = kotlinext.js.require("antd").Radio.Button

external interface RadioGroupProps : RProps {
    var value: String
    var defaultValue: String
    var name: String
    var options: RadioGroupOptions
    var autoFocus: Boolean
    var size: ButtonSize
    var buttonStyle: ButtonStyle
    var onChange: (e: Event) -> Unit
}

external interface RadioButtonProps : RProps {
    var disabled: Boolean
    var value: String
    var ghost: Boolean
    var href: String
    var htmlType: String
    var icon: String
    var loading: Any
    var shape: ButtonShape
    var size: ButtonSize
    var target: String
    var type: ButtonType
    var block: String
    var onClick: () -> Unit
}

external interface RadioProps : RProps {
    var disabled: Boolean
    var checked: Boolean
    var defaultChecked: Boolean
    var autoFocus: Boolean
    var value: String

}

fun RBuilder.Radio(
        value: String,
        autoFocus: Boolean = false,
        checked: Boolean = jsUndefined,
        defaultChecked: Boolean = false,
        disabled: Boolean = false,
        handler: RHandler<RadioProps> = {}
) {
    RadioComp {
        attrs.disabled = disabled
        attrs.value = value
        if (checked != jsUndefined) {
            attrs.checked = checked
        }
        attrs.autoFocus = autoFocus
        attrs.defaultChecked = defaultChecked
        handler()
    }
}

data class RadioGroupOption(val label: String, val disabled: Boolean)
class RadioGroupOptions {
    var value: Any

    constructor(options: Array<String>) {
        value = options
    }

    constructor(options: Array<RadioGroupOption>) {
        value = options
    }
}

object ButtonStyle {
    val outline: ButtonStyle = js("'outline'")
    val solid: ButtonStyle = js("'solid'")
}

fun RBuilder.RadioGroup(
        value: String = jsUndefined,
        defaultValue: String = jsUndefined,
        name: String = jsUndefined,
        options: RadioGroupOptions = jsUndefined,
        autoFocus: Boolean = false,
        size: ButtonSize = ButtonSize.default,
        buttonStyle: ButtonStyle = ButtonStyle.outline,
        onChange: (e: Event) -> Unit = jsUndefined,
        handler: RHandler<RadioGroupProps> = {}
) {
    RadioGroupComp {
        if (value != undefined) {
            attrs.value = value
        }
        if (defaultValue != undefined) {
            attrs.defaultValue = defaultValue
        }
        if (name != undefined) {
            attrs.name = name
        }
        if (options != undefined) {
            attrs.options = options
        }
        if (onChange != undefined) {
            attrs.onChange = onChange
        }
        attrs.autoFocus = autoFocus
        attrs.size = size
        attrs.buttonStyle = buttonStyle
        handler()
    }
}

fun RBuilder.RadioButton(
        value: String,
        disabled: Boolean = false,
        ghost: Boolean = false,
        href: String = jsUndefined,
        htmlType: kotlinx.html.ButtonType = jsUndefined,
        icon: String = jsUndefined,
        loading: Boolean = false,
        loadingDelay: Int? = null,
        shape: ButtonShape = jsUndefined,
        size: ButtonSize = jsUndefined,
        target: String = jsUndefined,
        block: Boolean = false,
        onClick: () -> Unit = jsUndefined,
        handler: RHandler<RadioButtonProps> = {}
) {
    RadioButtonComp {
        attrs.value = value
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
        if (block) {
            attrs.block = "block"
        }
        handler()
    }
}