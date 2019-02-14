package hu.nevermind.antd.table

import hu.nevermind.utils.hu.nevermind.antd.StringOrReactElement
import kotlinext.js.jsObject
import react.RClass
import react.RProps
import react.ReactElement

val Table: RClass<TableProps> = kotlinext.js.require("antd").Table

external interface TableProps : RProps {
    var columns: Array<ColumnProps>
    var dataSource: Array<out Any>
    var rowKey: String
    var onRow: (dynamic) -> Any
}


fun ColumnProps(body: ColumnProps.() -> Unit): ColumnProps {
    return jsObject<ColumnProps> { body() }
}

object ColumnAlign {
    val right: ColumnAlign = js("'right'")
    val left: ColumnAlign = js("'left'")
    val center: ColumnAlign = js("'center'")
}

external interface FilterDropDownData {
    val setSelectedKeys: (Array<String>) -> Unit
    val selectedKeys: Array<String>
    val confirm: () -> Unit
    val clearFilters: () -> Unit
}

external interface ColumnProps {
    var filterIcon: (Boolean) -> StringOrReactElement
    var onFilterDropdownVisibleChange: (Boolean) -> Unit
    var filterDropdown: (FilterDropDownData) -> ReactElement?
    var title: String
    var dataIndex: String
    var width: Int
    var align: ColumnAlign
    var key: String
    //                  render: (RBuilder.(text: String, record: dynamic, Int) -> ReactElement?)? = null) {
    var render: (cell: dynamic, row: dynamic, rowIndex: Int) -> ReactElement?
    var filters: Any?
    var onFilter: (value: dynamic, record: dynamic) -> Boolean
//    val render: (String, Any, Int) -> ReactElement? = render?.let { render ->
//        { text: String, record: Any, index: Int ->
//            buildElement {
//                render(text, record, index)
//            }
//        }
//    } ?: js("null")
}