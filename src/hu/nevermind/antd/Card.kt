package hu.nevermind.antd

import hu.nevermind.utils.hu.nevermind.antd.StringOrReactElement
import react.RBuilder
import react.RClass
import react.RHandler
import react.RProps

val CardComp: RClass<CardProps> = kotlinext.js.require("antd").Card


external interface CardProps : RProps {
    var title: StringOrReactElement?
}

fun RBuilder.Card(
        handler: RHandler<CardProps> = {}
) {
    CardComp {
        handler()
    }
}
