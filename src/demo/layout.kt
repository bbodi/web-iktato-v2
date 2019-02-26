package hu.nevermind.utils.demo

import hu.nevermind.antd.*
import hu.nevermind.utils.hu.nevermind.antd.*
import hu.nevermind.utils.jsStyle
import kotlinx.html.id
import react.*
import react.dom.*
import kotlin.js.Math

val Highlight: RClass<RProps> = kotlinext.js.require("react-highlight").default


class LayoutDemoPage : RComponent<RProps, RState>() {
    override fun RBuilder.render() {
        div {
            Row {
                example(classicLayouts, classicLayoutSource.trimMargin(), "Classic layouts", style)
            }
            Divider()
            Row {
                example(headerContentFooter, "TODO".trimMargin(), "Header-Content-Footer", "")
            }
            Divider()
            Row {
                example(headerSider, "TODO".trimMargin(), "Header Sider 2", "")
            }
        }
    }

    val headerSider: RBuilder.() -> Unit = {
        Layout {
            Header {
                attrs.className = "header"
                div(classes = "logo") {
                    attrs.jsStyle = jsStyle {
                        width = "120px"
                        height = "31px"
                        background = "rgba(255,255,255,.2)"
                        margin = "16px 24px 16px 0"
                        float = "left"
                    }
                }
                Menu {
                    attrs.theme = Theme.dark
                    attrs.mode = MenuMode.horizontal
                    attrs.defaultSelectedKeys = arrayOf("2")
                    attrs.style = jsStyle {
                        lineHeight = "64px"
                    }
                    MenuItem("1") { +"nav 1" }
                    MenuItem("2") { +"nav 2" }
                    MenuItem("3") { +"nav 3" }
                }
            }
            Layout {
                Sider {
                    attrs.width = 200
                    attrs.style = jsStyle { background = "#fff" }
                    Menu {
                        attrs.mode = MenuMode.inline
                        attrs.defaultSelectedKeys = arrayOf("1")
                        attrs.defaultOpenKeys = arrayOf("sub1")
                        attrs.style = jsStyle {
                            height = "100%"
                            borderRight = "0"
                        }

                        SubMenu("sub1") {
                            attrs.title = StringOrReactElement.from { span { Icon("user"); +"subnav1" } }
                            MenuItem("1") { +"option1" }
                            MenuItem("2") { +"option2" }
                            MenuItem("3") { +"option3" }
                            MenuItem("4") { +"option4" }
                        }
                        SubMenu("sub2") {
                            attrs.title = StringOrReactElement.from { span { Icon("laptop"); +"subnav2" } }
                            MenuItem("5") { +"option5" }
                            MenuItem("6") { +"option6" }
                            MenuItem("7") { +"option7" }
                            MenuItem("8") { +"option8" }
                        }
                        SubMenu("sub3") {
                            attrs.title = StringOrReactElement.from { span { Icon("notification"); +"subnav3" } }
                            MenuItem("9") { +"option9" }
                            MenuItem("10") { +"option10" }
                            MenuItem("11") { +"option11" }
                            MenuItem("12") { +"option12" }
                        }
                    }
                }
                Layout {
                    attrs.style = jsStyle { padding = "0 24px 24px" }
                    Content {
                        attrs.style = jsStyle { padding = "24"; background = "#fff";margin = 0;minHeight = 200 }
                        +"Content"
                    }
                }
            }
        }
    }


    val headerContentFooter: RBuilder.() -> Unit = {
        Layout {
            attrs.className = "layout"
            Header {
                div(classes = "logo") {
                    attrs.jsStyle = jsStyle {
                        width = "120px"
                        height = "31px"
                        background = "rgba(255,255,255,.2)"
                        margin = "16px 24px 16px 0"
                        float = "left"
                    }
                }
                Menu {
                    attrs.theme = Theme.dark
                    attrs.mode = MenuMode.horizontal
                    attrs.defaultSelectedKeys = arrayOf("2")
                    attrs.style = jsStyle {
                        lineHeight = "64px"
                    }
                    MenuItem("1") { +"nav 1" }
                    MenuItem("2") { +"nav 2" }
                    MenuItem("3") { +"nav 3" }
                }
            }
            Content {
                attrs.style = jsStyle { padding = "0 50px" }
                // TODO
//                <Breadcrumb style ={ { margin: '16px 0' } } >
//                    <Breadcrumb.Item > Home < / Breadcrumb . Item >
//                    <Breadcrumb.Item > List < / Breadcrumb . Item >
//                    <Breadcrumb.Item > App < / Breadcrumb . Item >
//                </Breadcrumb>
                div {
                    attrs.jsStyle = jsStyle { background = "#fff"; padding = 24; minHeight = 280 }
                    +"Content"
                }
            }
            Footer {
                attrs.style = jsStyle { textAlign = "center" }
                +"KAnt Design ©2018 Created by Balázs Bódi"
            }
        }
    }

    val classicLayouts: RBuilder.() -> Unit = {
        Layout {
            Header { +"Header" }
            Content { +"Content" }
            Footer { +"Footer" }
        }
        Layout {
            Header { +"Header" }
            Layout {
                Sider { +"Header" }
                Content { +"Content" }
            }
            Footer { +"Footer" }
        }
        Layout {
            Header { +"Header" }
            Layout {
                Content { +"Content" }
                Sider { +"Header" }
            }
            Footer { +"Footer" }
        }
        Layout {
            Sider { +"Header" }
            Layout {
                Header { +"Header" }
                Content { +"Content" }
                Footer { +"Footer" }
            }
        }
    }
}

fun RBuilder.layoutDemoPage() = child(LayoutDemoPage::class) {}

fun RBuilder.example(element: RBuilder.() -> Unit, source: String, title: String, styleString: String, open: Boolean = false) {
    val codeBlockId = (Math.random() * 10000).toInt().toString()
    h3 { +title }
    style {
        setProp("dangerouslySetInnerHTML", InnerHTML(
                styleString.replace("component-demo-box", "component-demo-box-$codeBlockId")
        ))
    }
    Collapse {
        attrs.defaultActiveKey = arrayOf("showcase")
        Panel("showcase") {
            attrs.header = StringOrReactElement.fromString("Showcase")
            section("code-box") {
                attrs.id = "component-demo-box-$codeBlockId"
                section("code-box-demo") {
                    div {
                        element()
                    }
                }
            }
        }
        Panel("source") {
            attrs.header = StringOrReactElement.fromString("Source code")
            div {
                attrs.jsStyle {
                    textAlign = "left"
                }
                Highlight {
                    attrs.asDynamic().className = "kotlin"
                    +source
                }

            }
        }
    }
    br {  }
}


private const val style = """.code-box-demo {
    text-align: center;
    padding-top: 42px;
    padding-right: 24px;
    padding-bottom: 50px;
    padding-left: 24px;
}
    #component-demo-box {
        border: 1px solid #d3d5d8;
        border-radius: 2px;
            margin: 0 0 16px;
    }

    #component-demo-box .ant-layout-header,
#component-demo-box .ant-layout-footer {
    background: #7dbcea;
    color: #fff;
}

#component-demo-box .ant-layout-footer {
    line-height: 1.5;
}

#component-demo-box .ant-layout-sider {
    background: #3ba0e9;
    color: #fff;
    line-height: 120px;
}

#component-demo-box .ant-layout-content {
    background: rgba(16, 142, 233, 1);
    color: #fff;
    min-height: 120px;
    line-height: 120px;
}

#component-demo-box > .code-box-demo > div > .ant-layout {
    margin-bottom: 48px;
}

#component-demo-box > .code-box-demo > div > .ant-layout:last-child {
    margin: 0;
}
"""

const val classicLayoutSource: String = """
                            |Layout {
                            |    Header { +"Header" }
                            |    Content { +"Content" }
                            |    Footer { +"Footer" }
                            |}
                            |Layout {
                            |    Header { +"Header" }
                            |    Layout {
                            |        Sider { +"Header" }
                            |        Content { +"Content" }
                            |    }
                            |    Footer { +"Footer" }
                            |}
                            |Layout {
                            |    Header { +"Header" }
                            |    Layout {
                            |        Content { +"Content" }
                            |        Sider { +"Header" }
                            |    }
                            |    Footer { +"Footer" }
                            |}
                            |Layout {
                            |    Sider { +"Header" }
                            |    Layout {
                            |        Header { +"Header" }
                            |        Content { +"Content" }
                            |        Footer { +"Footer" }
                            |    }
                            |}
                            """