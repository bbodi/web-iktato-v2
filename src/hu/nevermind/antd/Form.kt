package hu.nevermind.antd

import hu.nevermind.utils.hu.nevermind.antd.StringOrReactElement
import hu.nevermind.utils.jsStyle
import kotlinx.html.InputType
import org.w3c.dom.events.Event
import org.w3c.dom.events.InputEvent
import react.RBuilder
import react.RClass
import react.RHandler
import react.RProps

val FormComp: RClass<FormProps> = kotlinext.js.require("antd").Form
val InputComp: RClass<InputProps> = kotlinext.js.require("antd").Input
val TextAreaComp: RClass<TextAreaProps> = kotlinext.js.require("antd").Input.TextArea
val FormItemComp: RClass<FormItemProps> = kotlinext.js.require("antd").Form.Item


object FormLayout {
    val vertical: FormLayout = js("'vertical'")
    val horizontal: FormLayout = js("'horizontal'")
    val inline: FormLayout = js("'inline'")
}


external interface FormProps : RProps {
    var layout: FormLayout
    var onSubmit: (Event) -> Unit
}

fun RBuilder.Form(handler: RHandler<FormProps> = {}) {
    FormComp {
        handler()
    }
}


external interface InputProps : RProps {
    var layout: FormLayout
    var prefix: StringOrReactElement?
    var disabled: Boolean
    var addonAfter: StringOrReactElement?
    var placeholder: String
    var value: String
    var defaultValue: String
    var type: InputType
    var onChange: (e: InputEvent) -> Unit
    var onPressEnter: () -> Unit
}

external interface TextAreaProps : RProps {
    var rows: Int
    var disabled: Boolean
    var value: String
    var defaultValue: String
    var onChange: (e: InputEvent) -> Unit
    var onPressEnter: () -> Unit
}

external interface MyNumberInputProps : InputProps {
    var number: Long?
    var onValueChange: (value: Long?) -> Unit
}

fun RBuilder.Input(handler: RHandler<InputProps> = {}) {
    InputComp {
        handler()
    }
}

fun RBuilder.TextArea(handler: RHandler<TextAreaProps> = {}) {
    TextAreaComp {
        handler()
    }
}


fun RBuilder.MyNumberInput(handler: RHandler<MyNumberInputProps> = {}) {
    Input {
        handler.asDynamic()(this)
        if (attrs.asDynamic().style != null) {
//            attrs.asDynamic().style["textAlign"] = "right"
        } else {
//            attrs.asDynamic().style = jsStyle { textAlign = "right" }
        }
        if (attrs.asDynamic().className != null) {
//            attrs.asDynamic().className += "my-number-input"
        } else {
//            attrs.asDynamic().className = "my-number-input"
        }
        if (attrs.asDynamic().number) {
            attrs.value = parseGroupedStringToNum((attrs.unsafeCast<MyNumberInputProps>()).number.toString()).second
        } else {
            attrs.value = ""
        }
        attrs.onChange = { e ->
            val value: String = (e.currentTarget.asDynamic().value as String).let {
                if (it.lastOrNull() == 'm') {
                    it + "000000"
                } else if (it.lastOrNull() == 'k') {
                    it + "000"
                } else it
            }
            attrs.asDynamic().onValueChange(parseGroupedStringToNum(value).first)
        }
    }
}

fun Number?.format(): String? = if (this == null) {
    null
} else {
    parseGroupedStringToNum(this.toString()).second
}

fun Number.format(): String = parseGroupedStringToNum(this.toString()).second

fun parseGroupedStringToNum(value: String): Pair<Long?, String> {
    val onlyDigits = value.filter { it in "0123456789" }
    val sign = if (value.startsWith('-')) -1 else 1
    val str = onlyDigits.ifEmpty { null }
            ?.map { it }
            ?.reversed()
            ?.chunked(3)
            ?.map { it + ' ' }
            ?.flatten()
            ?.reversed()
            ?.drop(1) // first char is an unnecessary space
            ?.joinToString("")?.let { (if (sign == -1) "-" else "") + it }
    val visibleValue = str ?: (if (value == "-") "-" else "")
    val num = onlyDigits.toLongOrNull()?.times(sign)
    return Pair(num, visibleValue)
}

object ValidateStatus {
    val error: ValidateStatus = js("'error'")
    val warning: ValidateStatus = js("'warning'")
    val validating: ValidateStatus = js("'validating'")
    val success: ValidateStatus = js("'success'")
}

external interface FormItemProps : RProps {
    var validateStatus: ValidateStatus?
    var label: StringOrReactElement
    var help: StringOrReactElement?
    var required: Boolean
    var colon: Boolean
    var hasFeedback: Boolean
    var labelCol: ColProperties
    var wrapperCol: ColProperties
}

fun RBuilder.FormItem(handler: RHandler<FormItemProps> = {}) {
    FormItemComp {
        handler()
    }
}

