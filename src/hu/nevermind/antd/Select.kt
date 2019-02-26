package hu.nevermind.antd

import react.*

val SelectComp: RClass<SelectProps> = kotlinext.js.require("antd").Select
val OptionComp: RClass<OptionProps> = kotlinext.js.require("antd").Select.Option

external interface SelectProps : RProps {
    var value: Any?
    var filterOption: (String, ReactElement) -> Boolean
    var onChange: (Any, Any) -> Unit
    var onSelect: (dynamic, Any) -> Unit
    var disabled: Boolean
    var showSearch: Boolean
    var notFoundContent: String
    var defaultValue: Any
    var defaultValues: Array<Any>
}


fun RBuilder.Select(handler: RHandler<SelectProps> = {}) {
    SelectComp {
        handler()
    }
}

object StringOrNumber {
    inline fun from(str: String): StringOrNumber = str.unsafeCast<StringOrNumber>()
    inline fun from(str: Number): StringOrNumber = str.unsafeCast<StringOrNumber>()
}

external interface OptionProps : RProps {
    var key: String
    var title: String
    var value: StringOrNumber
    var disabled: Boolean
}


fun RBuilder.Option(value: String, text: String) {
    OptionComp {
        attrs.value = StringOrNumber.from(value)
        +text
    }
}

fun RBuilder.Option(value: Number, text: String) {
    OptionComp {
        attrs.value = StringOrNumber.from(value)
        +text
    }
}

fun RBuilder.Option(value: String, handler: RHandler<OptionProps> = {}) {
    OptionComp {
        attrs.value = StringOrNumber.from(value)
        handler()
    }
}

fun RBuilder.Option(value: Number, handler: RHandler<OptionProps> = {}) {
    OptionComp {
        attrs.value = StringOrNumber.from(value)
        handler()
    }
}
