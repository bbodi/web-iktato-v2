package hu.nevermind.utils.hu.nevermind.antd


import hu.nevermind.antd.Theme
import hu.nevermind.utils.Style
import org.w3c.dom.events.Event
import react.RBuilder
import react.RClass
import react.RHandler
import react.RProps

object Menu {
    val Menu: RClass<MenuProps> = kotlinext.js.require("antd").Menu
    val Item: RClass<ItemProps> = kotlinext.js.require("antd").Menu.Item
    val SubMenu: RClass<SubMenuProps> = kotlinext.js.require("antd").Menu.SubMenu
    val ItemGroup: RClass<ItemGroupProps> = kotlinext.js.require("antd").Menu.ItemGroup
    val Divider: RClass<RProps> = kotlinext.js.require("antd").Menu.Divider
}

object MenuMode {
    val vertical: MenuMode = js("'vertical'")
    val verticalRight: MenuMode = js("'vertical-right'")
    val horizontal: MenuMode = js("'horizontal'")
    val inline: MenuMode = js("'inline'")
}

external interface OnClickEventData {
    val item: Any
    val key: Key
    val keyPath: Array<String>
    val domEvent: Event
}

external interface OnSelectEventData {
    val item: Any
    val key: Key
    val selectedKeys: Array<Key>
}

external interface MenuProps : RProps {
    var defaultOpenKeys: Array<Key>
    var defaultSelectedKeys: Array<Key>
    var forceSubMenuRender: Boolean
    var inlineCollapsed: Boolean
    var inlineIndent: Int
    var mode: MenuMode
    var multiple: Boolean
    var openKeys: Array<Key>
    var selectable: Boolean
    var selectedKeys: Array<Key>
    var style: Style
    @JsName("subMenuCloseDelay")
    var subMenuCloseDelayInSeconds: Float
    @JsName("subMenuOpenDelay")
    var subMenuOpenDelayInSeconds: Float
    var theme: Theme
    var onClick: (OnClickEventData) -> Unit
    var onDeselect: (OnSelectEventData) -> Unit
    var onOpenChange: (Array<Key>) -> Unit
    var onSelect: (OnSelectEventData) -> Unit
}

external interface ItemProps : RProps {
    var disabled: Boolean
    var key: Key
}

external interface SubMenuProps : RProps {
    // 	sub menus or sub menu items	Array<MenuItem|SubMenu>
    var children: Array<Any>
    var disabled: Boolean
    var key: Key
    var title: StringOrReactElement?
    var onTitleClick: (Key, Any) -> Unit
}

external interface ItemGroupProps : RProps {
    var key: Key
    var title: StringOrReactElement?
}


fun RBuilder.Menu(handler: RHandler<MenuProps>) {
    Menu.Menu {
        handler()
    }
}

fun RBuilder.MenuItem(key: Key,
                      disabled: Boolean = false,
                      handler: RHandler<ItemProps> = {}) {
    Menu.Item {
        attrs.disabled = disabled
        attrs.key = key
        handler()
    }
}

fun RBuilder.SubMenu(key: Key,
                     handler: RHandler<SubMenuProps>) {
    Menu.SubMenu {
        attrs.key = key
        handler()
    }
}


fun RBuilder.MenuItemGroup(key: Key,
                           handler: RHandler<ItemGroupProps>) {
    Menu.ItemGroup {
        attrs.key = key
        handler()
    }
}