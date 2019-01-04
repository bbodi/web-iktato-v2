package hu.nevermind.antd

import hu.nevermind.utils.Style
import hu.nevermind.utils.hu.nevermind.antd.StringOrReactElement
import hu.nevermind.utils.hu.nevermind.antd.jsUndefined
import react.RBuilder
import react.RClass
import react.RHandler
import react.RProps

external interface LayoutProps : RProps {
    var className: String
    var style: Style
    var hasSider: Boolean
}

external interface HeaderProps : RProps {
    var className: String
    var style: Style
    var hasSider: Boolean
}

external interface ContentProps : RProps {
    var className: String
    var style: Style
    var hasSider: Boolean
}

external interface FooterProps : RProps {
    var className: String
    var style: Style
    var hasSider: Boolean
}

object Breakpoint {
    val xs: Breakpoint = js("'xs'")
    val sm: Breakpoint = js("'sm'")
    val md: Breakpoint = js("'md'")
    val lg: Breakpoint = js("'lg'")
    val xl: Breakpoint = js("'xl'")
    val xxl: Breakpoint = js("'xxl'")
}


object Theme {
    val light: Theme = js("'light'")
    val dark: Theme = js("'dark'")
}

external interface SiderProps : RProps {
    var style: Style
    var breakpoint: Breakpoint
    var className: String
    var collapsed: Boolean
    var collapsedWidth: Int
    var collapsible: Boolean
    var defaultCollapsed: Boolean
    var reverseArrow: Boolean
    var theme: Theme
    var trigger: StringOrReactElement?
    var width: Int
    var onCollapse: (Boolean, Any) -> Unit
    var onBreakpoint: (Boolean) -> Unit
}


object LayoutComponents {
    val Layout: RClass<LayoutProps> = kotlinext.js.require("antd").Layout
    val Header: RClass<HeaderProps> = kotlinext.js.require("antd").Layout.Header
    val Footer: RClass<FooterProps> = kotlinext.js.require("antd").Layout.Footer
    val Content: RClass<ContentProps> = kotlinext.js.require("antd").Layout.Content
    val Sider: RClass<SiderProps> = kotlinext.js.require("antd").Layout.Sider
}


fun RBuilder.Layout(className: String = "",
                    style: Style = jsUndefined,
                    hasSider: Boolean = jsUndefined,
                    handler: RHandler<LayoutProps>) {
    LayoutComponents.Layout {
        attrs.className = className
        attrs.style = style
        attrs.hasSider = hasSider
        handler()
    }
}


fun RBuilder.Header(className: String = "",
                    style: Style = jsUndefined,
                    hasSider: Boolean = jsUndefined,
                    handler: RHandler<HeaderProps>) {
    LayoutComponents.Header {
        attrs.className = className
        attrs.style = style
        attrs.hasSider = hasSider
        handler()
    }
}


fun RBuilder.Content(handler: RHandler<ContentProps>) {
    LayoutComponents.Content {
        handler()
    }
}


fun RBuilder.Footer(handler: RHandler<FooterProps>) {
    LayoutComponents.Footer {
        handler()
    }
}

fun RBuilder.Sider(handler: RHandler<SiderProps>) {
    LayoutComponents.Sider {
        handler()
    }
}
