package hu.nevermind.utils.demo

import hu.nevermind.antd.*
import react.*
import react.dom.br
import react.dom.div
import react.dom.jsStyle

interface ButtonDemoPageState : RState {
    var loadingButton: Boolean
    var loadingIcon: Boolean
    var radioGroupSize: ButtonSize
}

class ButtonDemoPage : RComponent<RProps, ButtonDemoPageState>() {
    override fun ButtonDemoPageState.init() {
        loadingButton = false
        loadingIcon = false
        radioGroupSize = ButtonSize.default
    }

    override fun RBuilder.render() {
        Row {
            Col(span = 11) {
                example(primaryDefaultDashed, "", "", buttonDemoStyle, open = true)
                example(sizeDemoBox, "", "", buttonDemoStyle, open = true)
                example(loadingDemoBox, "", "", buttonDemoStyle, open = true)
                example(buttonGroup, "", "", buttonDemoStyle, open = true)
                example(blockButtonDemo, "", "", buttonDemoStyle, open = true)
            }
            Col(span = 11, offset = 1) {
                example(iconDemoBox, "TODO", "", buttonDemoStyle, open = true)
                example(disabledDemoBox, "TODO", "", buttonDemoStyle, open = true)

            }
        }
        Divider()
    }

    val primaryDefaultDashed: RBuilder.() -> Unit = {
        div {
            Button(type = ButtonType.primary) { +"Primary" }
            Button { +"Default" }
            Button(type = ButtonType.dashed) { +"Dashed" }
            Button(type = ButtonType.danger) { +"Danger" }
        }
    }

    val sizeDemoBox: RBuilder.() -> Unit = {
        div {
            RadioGroup(value = state.radioGroupSize.asString(), onChange = { setState { radioGroupSize = it.target.asDynamic().value } }) {
                RadioButton(value = ButtonSize.large.asString()) { +"Large" }
                RadioButton(value = ButtonSize.default.asString()) { +"Default" }
                RadioButton(value = ButtonSize.small.asString()) { +"Small" }
            }
            br {};br {}
            Button(type = ButtonType.primary, size = state.radioGroupSize) { +"Primary" }
            Button(size = state.radioGroupSize) { +"Normal" }
            Button(type = ButtonType.dashed, size = state.radioGroupSize) { +"Dashed" }
            Button(type = ButtonType.danger, size = state.radioGroupSize) { +"Danger" }
            br {}
            Button(type = ButtonType.primary, shape = ButtonShape.circle, icon = "download", size = state.radioGroupSize)
            Button(type = ButtonType.primary, icon = "download", size = state.radioGroupSize) { +"Download" }
            br {}
            ButtonGroup(size = state.radioGroupSize) {
                Button(type = ButtonType.primary) { Icon("left"); +"Backward" }
                Button(type = ButtonType.primary) { +"Forward"; Icon("right") }
            }
        }
    }

    val buttonGroup: RBuilder.() -> Unit = {
        div {
            +"TODO: ButtonGroup"
        }
    }

    val iconDemoBox: RBuilder.() -> Unit = {
        div {
            Button(type = ButtonType.primary, shape = ButtonShape.circle, icon = "search")
            Button(type = ButtonType.primary, icon = "search") { +"Search" }
            Button(shape = ButtonShape.circle, icon = "search")
            Button(icon = "search") { +"Search" }
            br { }
            Button(shape = ButtonShape.circle, icon = "search")
            Button(icon = "search") { +"Search" }
            Button(type = ButtonType.dashed, shape = ButtonShape.circle, icon = "search")
            Button(type = ButtonType.dashed, icon = "search") { +"Search" }
        }
    }

    val blockButtonDemo: RBuilder.() -> Unit = {
        div {
            Button(type = ButtonType.primary, block = true) { +"Primary" }
            Button(block = true) { +"Default" }
            Button(type = ButtonType.dashed, block = true) { +"Dashed" }
            Button(type = ButtonType.danger, block = true) { +"Danger" }
        }
    }

    val disabledDemoBox: RBuilder.() -> Unit = {
        div {
            Button(type = ButtonType.primary) { +"Primary" }
            Button(type = ButtonType.primary, disabled = true) { +"Primary(disabled)" }
            br {}
            Button { +"Default" }
            Button(disabled = true) { +"Default(disabled)" }
            br {}
            Button(type = ButtonType.dashed) { +"Dashed" }
            Button(type = ButtonType.dashed, disabled = true) { +"Dashed(disabled)" }
            div {
                attrs.jsStyle { padding = "8px 8px 0 8px"; background = "rgb(190, 200, 200)" }
                Button(ghost = true) { +"Ghost" }
                Button(ghost = true, disabled = true) { +"Ghost(disabled)" }
            }
        }
    }

    val loadingDemoBox: RBuilder.() -> Unit = {
        div {
            Button(type = ButtonType.primary, loading = true) { +"Loading" }
            Button(type = ButtonType.primary, size = ButtonSize.small, loading = true) { +"Loading" }
            br {}
            Button(type = ButtonType.primary,
                    loading = state.loadingButton,
                    onClick = { setState { loadingButton = true } }
            ) { +"Click me!" }
            Button(type = ButtonType.primary,
                    icon = "poweroff",
                    loading = state.loadingIcon,
                    onClick = { setState { loadingIcon = true } }
            ) { +"Click me!" }
            br {}
            Button(shape = ButtonShape.circle, loading = true)
            Button(type = ButtonType.primary, shape = ButtonShape.circle, loading = true)
        }
    }

    private val buttonDemoStyle = """
[id^=component-demo-box] .ant-btn {
  margin-right: 8px;
  margin-bottom: 12px;
}
[id^=component-demo-box] .ant-btn-group > .ant-btn,
[id^=component-demo-box] .ant-btn-group > span > .ant-btn {
  margin-right: 0;
}

    """.trimIndent()

}