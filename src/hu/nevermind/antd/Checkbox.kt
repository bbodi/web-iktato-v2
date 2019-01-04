package hu.nevermind.antd

import hu.nevermind.utils.hu.nevermind.antd.StringOrReactElement
import react.RBuilder
import react.RClass
import react.RHandler
import react.RProps

val CheckboxComp: RClass<CheckboxProps> = kotlinext.js.require("antd").Checkbox


external interface CheckboxProps : RProps {
    var checked: Boolean
    var checkedChildren: StringOrReactElement
    var unCheckedChildren: StringOrReactElement
    var onChange: (Boolean) -> Unit
    var disabled: Boolean
    var indeterminate: Boolean
}

fun RBuilder.Checkbox(handler: RHandler<CheckboxProps> = {}) {
    CheckboxComp {
        handler()
        if (attrs.onChange.asDynamic() != null) {
            val userDefinedOnChange = attrs.onChange
            attrs.onChange = { e: dynamic /*e: Event*/ ->
                userDefinedOnChange(e.target.checked)
            }
        }
    }
}