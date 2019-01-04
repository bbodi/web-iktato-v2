package hu.nevermind.antd

import react.RBuilder
import react.RClass
import react.RHandler
import react.RProps

val BreadcrumbComp: RClass<BreadcrumbProps> = kotlinext.js.require("antd").Breadcrumb
val BreadcrumbItemComp: RClass<BreadcrumbItemProps> = kotlinext.js.require("antd").Breadcrumb.Item


external interface BreadcrumbProps : RProps {

}

fun RBuilder.Breadcrumb(handler: RHandler<BreadcrumbProps> = {}) {
    BreadcrumbComp {
        handler()
    }
}


external interface BreadcrumbItemProps : RProps {

}

fun RBuilder.BreadcrumbItem(handler: RHandler<BreadcrumbItemProps> = {}) {
    BreadcrumbItemComp {
        handler()
    }
}