package hu.nevermind.utils.hu.nevermind.antd

import react.RBuilder
import react.RClass
import react.RHandler
import react.RProps


val InputNumberComp: RClass<InputNumberProps> = kotlinext.js.require("antd").InputNumber

external interface InputNumberProps : RProps {
    var defaultValue: Number
    var value: Number
    var disabled: Boolean
    var max: Number
    var min: Number
    var precision: Number
    var decimalSeparator: String
    var size: String
    var step: Number
    var onChange: (Number) -> Unit
    var formatter: (Number) -> String
    var parser: (String) -> Number
}

fun RBuilder.InputNumber(
        handler: RHandler<InputNumberProps> = {}
) {
    InputNumberComp {
        handler()
    }
}