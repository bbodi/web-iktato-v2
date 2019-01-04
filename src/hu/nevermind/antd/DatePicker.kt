package hu.nevermind.antd

import app.common.Moment
import react.RBuilder
import react.RClass
import react.RHandler
import react.RProps

val MonthPickerComp: RClass<MonthPickerProps> = kotlinext.js.require("antd").DatePicker.MonthPicker
val DatePickerComp: RClass<DatePickerProps> = kotlinext.js.require("antd").DatePicker


external interface MonthPickerProps : RProps {
    var onChange: (Moment?, String) -> Unit
    var value: Moment
    var allowClear: Boolean
    var placeholder: String
    var disabledDate: (Moment) -> Boolean
}

fun RBuilder.MonthPicker(
        handler: RHandler<MonthPickerProps> = {}
) {
    MonthPickerComp {
        handler()
    }
}

external interface DatePickerProps : RProps {
    var onChange: (Moment?, String) -> Unit
    var value: Moment
    var allowClear: Boolean
    var placeholder: String
    var disabledDate: (Moment) -> Boolean
}

fun RBuilder.DatePicker(
        handler: RHandler<DatePickerProps> = {}
) {
    DatePickerComp {
        handler()
    }
}

