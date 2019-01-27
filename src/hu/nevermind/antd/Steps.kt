package hu.nevermind.antd

import hu.nevermind.utils.hu.nevermind.antd.StringOrReactElement
import hu.nevermind.utils.hu.nevermind.antd.jsUndefined
import react.RBuilder
import react.RClass
import react.RHandler
import react.RProps


val StepsComp: RClass<StepsProps> = kotlinext.js.require("antd").Steps
val StepComp: RClass<StepProps> = kotlinext.js.require("antd").Steps.Step


object StepsStatus {
    val wait: StepsStatus = js("'wait'")
    val process: StepsStatus = js("'process'")
    val finish: StepsStatus = js("'finish'")
    val error: StepsStatus = js("'error'")
}

object StepsDirection {
    val vertical: StepsDirection = js("'vertical'")
    val horizontal: StepsDirection = js("'horizontal'")
}

external interface StepsProps : RProps {
    var current: Int
    var direction: StepsDirection
    var labelPlacement: StepsDirection
    var size: String?
    var status: StepsStatus
    var initial: Int
}

fun RBuilder.Steps(handler: RHandler<StepsProps> = {}) {
    StepsComp {
        handler()
    }
}

external interface StepProps : RProps {
    var title: StringOrReactElement
    var description: StringOrReactElement?
    var icon: StringOrReactElement
    var status: StepsStatus
}

fun RBuilder.Step(handler: RHandler<StepProps> = {}) {
    StepComp {
        handler()
    }
}