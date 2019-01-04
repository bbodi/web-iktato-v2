package hu.nevermind.antd

import hu.nevermind.utils.Style
import hu.nevermind.utils.hu.nevermind.antd.StringOrReactElement
import hu.nevermind.utils.hu.nevermind.antd.jsUndefined
import react.RBuilder
import react.RClass
import react.RProps

val SliderComp: RClass<SliderProps> = kotlinext.js.require("antd").Slider

external interface SliderProps : RProps {
    var min: Int
    var max: Int
    var value: Any
    var onChange: (Int) -> Unit
    var onAfterChange: (Int) -> Unit
    var marks: Any
    var step: Int?
    var disabled: Boolean
    var dots: Boolean
    var autoFocus: Boolean
    var included: Boolean
    var range: Boolean
    var vertical: Boolean
    var defaultValue: Any
    var tipFormatter: (String) -> String
}

class SliderValue {
    val value: Any

    constructor(value: Number) {
        this.value = value
    }

    constructor(rangeStart: Number, rangeEnd: Number) {
        value = arrayOf(rangeStart, rangeEnd)
    }
}

class MarkEntry(label: StringOrReactElement, val style: Style = jsUndefined) {
    val label: Any? = label
}

fun RBuilder.Slider(value: SliderValue,
                    defaultValue: SliderValue = SliderValue(0),
                    min: Int = 0,
                    max: Int = 100,
                    marks: Map<Number, MarkEntry> = jsUndefined,
                    step: Int? = 1,
                    disabled: Boolean = false,
                    autoFocus: Boolean = false,
                    dots: Boolean = false,
                    range: Boolean = false,
                    vertical: Boolean = false,
                    included: Boolean = true,
                    tipFormatter: (String) -> String = { it },
                    onChange: (Int) -> Unit = jsUndefined,
                    onAfterChange: (Int) -> Unit = jsUndefined) {
    SliderComp {
        attrs.min = min
        attrs.max = max
        attrs.value = value.value
        attrs.onChange = onChange
        attrs.onAfterChange = onAfterChange
        attrs.marks = marks.let { map -> val jsobj = js("{}"); map.forEach { jsobj[it.key] = it.value }; jsobj }
        attrs.step = step
        attrs.disabled = disabled
        attrs.dots = dots
        attrs.defaultValue = defaultValue.value
        attrs.autoFocus = autoFocus
        attrs.included = included
        attrs.range = range
        attrs.vertical = vertical
        attrs.tipFormatter = tipFormatter
    }
}