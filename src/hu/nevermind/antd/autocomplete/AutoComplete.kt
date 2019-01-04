package hu.nevermind.antd.autocomplete

import hu.nevermind.utils.hu.nevermind.antd.Key
import react.*

val AutoComplete: RClass<AutoCompleteProps> = kotlinext.js.require("antd").AutoComplete

/**
 * string|string[]|{ key: string, label: string|ReactNode }|Array<{ key: string, label: string|ReactNode }>
 */
object AutoCompleteDefaultValue {

    fun fromString(defaultValue: String): AutoCompleteDefaultValue {
        return defaultValue.asDynamic()
    }

    fun fromReactElement(defaultValue: ReactElement): AutoCompleteDefaultValue {
        return defaultValue.asDynamic()
    }

    fun fromArray(defaultValue: Array<Pair<Key, Any>>): AutoCompleteDefaultValue {
        return defaultValue.asDynamic()
    }
}

external interface AutoCompleteProps : RProps {
    var style: Any
    var dataSource: Array<Any>
    var onSelect: (String) -> Unit
    var onChange: (String) -> Unit
    var onSearch: (String) -> Unit
    var placeholder: String?
    var value: String

    /**
     * string|string[]|{ key: string, label: string|ReactNode }|Array<{ key: string, label: string|ReactNode }>
     */
    var defaultValue: Any?

    var filterOption: (String, ReactElement) -> Boolean
}


fun RBuilder.AutoComplete(
        dataSource: Array<Any>,
        handler: RHandler<AutoCompleteProps> = {}
) {
    AutoComplete {
        attrs.dataSource = dataSource
        handler()
    }
}