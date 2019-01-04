package hu.nevermind.antd

import hu.nevermind.utils.hu.nevermind.antd.StringOrReactElement
import react.RBuilder
import react.RClass
import react.RHandler
import react.RProps

val TagComp: RClass<TagProps> = kotlinext.js.require("antd").Tag


external interface TagProps : RProps {
    var color: String
}

fun RBuilder.Tag(handler: RHandler<TagProps> = {}) {
    TagComp {
        handler()
    }
}