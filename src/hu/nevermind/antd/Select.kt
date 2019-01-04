package hu.nevermind.antd

import react.*

val SelectComp: RClass<SelectProps> = kotlinext.js.require("antd").Select
val OptionComp: RClass<OptionProps> = kotlinext.js.require("antd").Select.Option

external interface SelectProps : RProps {
    var value: Any
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


external interface OptionProps : RProps {
    var key: String
    var title: String
    var value: Any
    var disabled: Boolean
}


fun RBuilder.Option(handler: RHandler<OptionProps> = {}) {
    OptionComp {
        handler()
    }
}


