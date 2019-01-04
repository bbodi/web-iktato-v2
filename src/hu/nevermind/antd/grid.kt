package hu.nevermind.antd

import hu.nevermind.utils.hu.nevermind.antd.jsUndefined
import react.RBuilder
import react.RClass
import react.RHandler
import react.RProps

val RowComponent: RClass<RowProps> = kotlinext.js.require("antd").Row

fun RBuilder.Row(align: Align = Align.top,
                 gutter: Int = 0,
                 justify: Justify = Justify.start,
                 type: String = "",
                 handler: RHandler<RowProps>) {
    RowComponent {
        attrs.align = align
        attrs.gutter = gutter
        attrs.justify = justify
        attrs.type = type
        handler()
    }
}

object Align {
    val top: Align = js("'top'")
    val middle: Align = js("'top'")
    val bottom: Align = js("'top'")
}

object Justify {
    val start: Justify = js("'start'")
    val end: Justify = js("'end'")
    val center: Justify = js("'center'")
    val spaceAround: Justify = js("'space-around'")
    val spaceBetween: Justify = js("'space-between'")
}

external interface RowProps : RProps {
    var align: Align
    var gutter: Int
    var justify: Justify
    var type: String
}

val ColComponent: RClass<ColProps> = kotlinext.js.require("antd").Col

fun RBuilder.Col(offset: Int = 0,
                 order: Int = 0,
                 pull: Int = 0,
                 push: Int = 0,
                 span: Int,
                 xs: ColProperties? = null,
                 sm: ColProperties? = null,
                 md: ColProperties? = null,
                 lg: ColProperties? = null,
                 xl: ColProperties? = null,
                 xxl: ColProperties? = null,
                 handler: RHandler<ColProps>) {
    ColComponent {
        attrs.offset = offset
        attrs.order = order
        attrs.pull = pull
        attrs.push = push
        attrs.span = span

        attrs.xs = xs?.value
        attrs.sm = sm?.value
        attrs.md = md?.value
        attrs.lg = lg?.value
        attrs.xl = xl?.value
        attrs.xxl = xxl?.value
        handler()
    }
}

class ColProperties {
    val value: Any

    constructor(num: Int) {
        value = num
    }

    constructor(offset: Int = 0,
                order: Int = 0,
                pull: Int = 0,
                push: Int = 0,
                span: Int = 0) {
        value = object {
            val offset: Int = offset
            val order: Int = order
            val pull: Int = pull
            val push: Int = push
            val span: Int = span
        }
    }
}

external interface ColProps : RProps {
    var offset: Int
    var order: Int
    var pull: Int
    var push: Int
    var span: Int

    var xs: Any?
    var sm: Any?
    var md: Any?
    var lg: Any?
    var xl: Any?
    var xxl: Any?
}