package hu.nevermind.antd

import react.RBuilder
import react.RClass
import react.RHandler
import react.RProps


object Affix {
    val Affix: RClass<AffixProps> = kotlinext.js.require("antd").Affix
}


external interface AffixProps : RProps {
    var offsetTop: Int
}


fun RBuilder.Affix(handler: RHandler<AffixProps>) {
    Affix.Affix {
        handler()
    }
}