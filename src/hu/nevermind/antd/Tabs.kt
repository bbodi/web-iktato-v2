package hu.nevermind.antd

import hu.nevermind.utils.hu.nevermind.antd.StringOrReactElement
import react.*


val TabsComp: RClass<TabsProps> = kotlinext.js.require("antd").Tabs
val TabPaneComp: RClass<TabPaneProps> = kotlinext.js.require("antd").Tabs.TabPane


object TabsSize {
    val large: TabsSize get() = "large".asDynamic().unsafeCast<TabsSize>()
    val default: TabsSize get() = "default".asDynamic().unsafeCast<TabsSize>()
    val small: TabsSize get() = "small".asDynamic().unsafeCast<TabsSize>()
}

object TabsPosition {
    val top: TabsPosition get() = "top".asDynamic().unsafeCast<TabsPosition>()
    val right: TabsPosition get() = "right".asDynamic().unsafeCast<TabsPosition>()
    val left: TabsPosition get() = "left".asDynamic().unsafeCast<TabsPosition>()
    val bottom: TabsPosition get() = "bottom".asDynamic().unsafeCast<TabsPosition>()
}

object TabsType {
    val line: TabsType get() = "line".asDynamic().unsafeCast<TabsType>()
    val card: TabsType get() = "card".asDynamic().unsafeCast<TabsType>()
    val editableCard: TabsType get() = "editable-card".asDynamic().unsafeCast<TabsType>()
}

external interface TabsProps : RProps {
    var onChange: (activeKey: String) -> Unit
    var onEdit: (targetKey: String, action: String) -> Unit
    var onNextClick: () -> Unit
    var onPrevClick: () -> Unit
    var onTabClick: () -> Unit
    var defaultActiveKey: String
    var activeKey: String
    var animated: Boolean
    var hideAdd: Boolean
    var size: TabsSize
    var tabBarExtraContent: ReactElement
    var tabBarGutter: Number
    var tabBarStyle: Any
    var tabPosition: TabsPosition
    var type: TabsType
}

fun RBuilder.Tabs(handler: RHandler<TabsProps> = {}) {
    TabsComp {
        handler()
    }
}

external interface TabPaneProps : RProps {
    var forceRender: Boolean
    var key: String
    var tab: StringOrReactElement
}

fun RBuilder.TabPane(text: String? = null, handler: RHandler<TabPaneProps> = {}) {
    TabPaneComp {
        if (text != null) {
            attrs.tab = StringOrReactElement.fromString(text)
        }
        handler()
    }
}
