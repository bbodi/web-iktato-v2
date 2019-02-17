package hu.nevermind.antd

import hu.nevermind.utils.hu.nevermind.antd.StringOrReactElement
import react.RBuilder
import react.RClass
import react.RHandler
import react.RProps

val SpinComp: RClass<SpinProps> = kotlinext.js.require("antd").Spin


external interface SpinProps : RProps {
    var tip: StringOrReactElement
}

fun RBuilder.Spin(handler: RHandler<SpinProps> = {}) {
    SpinComp {
        handler()
    }
}