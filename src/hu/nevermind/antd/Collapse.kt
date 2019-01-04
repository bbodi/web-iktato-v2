package hu.nevermind.antd

import hu.nevermind.utils.hu.nevermind.antd.Key
import hu.nevermind.utils.hu.nevermind.antd.StringOrReactElement
import react.RBuilder
import react.RClass
import react.RHandler
import react.RProps


object Collapse {
    val Collapse: RClass<CollapseProps> = kotlinext.js.require("antd").Collapse
    val Panel: RClass<PanelProps> = kotlinext.js.require("antd").Collapse.Panel
}


external interface CollapseProps : RProps {
    var accordion: Boolean
    var activeKey: Key
    var bordered: Boolean
    var defaultActiveKey: Key
    var onChange: () -> Unit
    var destroyInactivePanel: Boolean
}

external interface PanelProps : RProps {
    var disabled: Boolean
    var forceRender: Boolean
    var header: StringOrReactElement?
    var key: Key
    var showArrow: Boolean
}

fun RBuilder.Collapse(handler: RHandler<CollapseProps>) {
    Collapse.Collapse {
        handler()
    }
}


fun RBuilder.Panel(key: Key,
                   handler: RHandler<PanelProps>
) {
    Collapse.Panel {
        attrs.key = key
        handler()
    }
}