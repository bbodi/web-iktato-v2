package hu.nevermind.antd

import hu.nevermind.utils.hu.nevermind.antd.StringOrReactElement
import react.RBuilder
import react.RClass
import react.RHandler
import react.RProps

val PopoverComp: RClass<PopoverProps> = kotlinext.js.require("antd").Popover


external interface PopoverProps : RProps {
    var visible: Boolean
    var content: StringOrReactElement
    var title: StringOrReactElement?
}

fun RBuilder.Popover(handler: RHandler<PopoverProps> = {}) {
    PopoverComp {
        handler()
    }
}