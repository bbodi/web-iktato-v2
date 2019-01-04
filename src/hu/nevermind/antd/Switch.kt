package hu.nevermind.antd

import hu.nevermind.utils.hu.nevermind.antd.StringOrReactElement
import react.RBuilder
import react.RClass
import react.RHandler
import react.RProps

val SwitchComp: RClass<SwitchProps> = kotlinext.js.require("antd").Switch


external interface SwitchProps : RProps {
    var checked: Boolean
    var checkedChildren: StringOrReactElement
    var unCheckedChildren: StringOrReactElement
    var onChange: (Boolean) -> Unit
    var disabled: Boolean
}

fun RBuilder.Switch(handler: RHandler<SwitchProps> = {}) {
    SwitchComp {
        handler()
    }
}