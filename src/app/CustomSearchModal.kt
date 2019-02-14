package hu.nevermind.utils.app

import app.AlvallalkozoScreenIds
import app.AppState
import app.Dispatcher
import app.common.moment
import app.megrendeles.SzuroMezo
import app.useState
import hu.nevermind.antd.*
import hu.nevermind.utils.hu.nevermind.antd.StringOrReactElement
import hu.nevermind.utils.jsStyle
import hu.nevermind.utils.store.dateFormat
import kotlinext.js.jsObject
import react.*
import react.dom.br
import store.*

private data class CustomSearchModalState(
        val szuroMezok: List<SzuroMezo>
)

fun customSearchModal(
        appState: AppState,
        szuroMezok: List<SzuroMezo>,
        visible: Boolean,
        onClose: (Boolean, List<SzuroMezo>) -> Unit): ReactElement {
    return createElement(type = { props: dynamic ->
        val appState: AppState = props.appState
        val szuroMezok: List<SzuroMezo> = props.szuroMezok
        val visible: Boolean = props.visible
        val (state, setState) = useState(CustomSearchModalState(szuroMezok))

        buildElement {
            Modal {
                attrs.visible = visible
                attrs.width = 800
                attrs.title = StringOrReactElement.fromString("Megrendelés Szűrés")
                attrs.cancelButtonProps = jsObject {
                    this.asDynamic().id = AlvallalkozoScreenIds.modal.buttons.close
                }
                attrs.okButtonProps = jsObject {
                    disabled = with(state) {
                        state.szuroMezok.isEmpty()
                    }
                }
                attrs.onOk = {
                    onClose(true, state.szuroMezok)
                }
                attrs.onCancel = {
                    onClose(false, emptyList())
                }
                Row {
                    Select {
                        attrs.asDynamic().style = jsStyle { minWidth = 300 }
                        attrs.showSearch = true
                        attrs.notFoundContent = "Nincs találat"
                        attrs.filterOption = { inputString, optionElement ->
                            (optionElement.props.children as String).toUpperCase().replace(" ", "").contains(inputString.toUpperCase().replace(" ", ""))
                        }
                        attrs.onSelect = { value: MegrendelesColumnData, option ->
                            onElementSelect(state, setState, value)
                        }
                        columnDefinitions.forEach {
                            Option { attrs.value = it; +it.columnTitle }
                        }
                    }
                }
                br{};br{}
                Form {
                    state.szuroMezok.forEachIndexed { index, szuroMezo ->
                        Row {
                            Col(span = 8) {
                                Input {
                                    attrs.disabled = true
                                    attrs.value = szuroMezo.columnData.columnTitle
                                    attrs.asDynamic().style = jsStyle { fontWeight = "bold" }
                                }
                            }
                            when {
                                szuroMezo.columnData.renderer == ebRenderer ->
                                    ebOperatorAndOperand(appState, state, setState, index, szuroMezo)
                                szuroMezo.columnData.renderer == avRenderer ->
                                    avOperatorAndOperand(appState, state, setState, index, szuroMezo)
                                szuroMezo.columnData.megrendelesFieldType == MegrendelesFieldType.Int ->
                                    intOperatorAndOperand(state, setState, index, szuroMezo)
                                szuroMezo.columnData.megrendelesFieldType == MegrendelesFieldType.String ->
                                    stringOperatorAndOperand(state, setState, index, szuroMezo)
                                szuroMezo.columnData.megrendelesFieldType == MegrendelesFieldType.Date ->
                                    dateOperatorAndOperand(state, setState, index, szuroMezo)
                            }
                            Tooltip("Szűrő törlése") {
                                Button {
                                    attrs.icon = "delete"
                                    attrs.type = ButtonType.danger
                                    attrs.onClick = {
                                        setState(state.copy(
                                                szuroMezok = state.szuroMezok.take(index) + state.szuroMezok.drop(index + 1)
                                        ))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }, props = jsObject<dynamic> {
        this.szuroMezok = szuroMezok
        this.appState = appState
        this.visible = visible
    })
}

private fun onElementSelect(state: CustomSearchModalState,
                            setState: Dispatcher<CustomSearchModalState>,
                            columnData: MegrendelesColumnData) {
    val keyEquality = columnData.renderer == ebRenderer || columnData.renderer == avRenderer
    val defaultOperator = if (columnData.megrendelesFieldType == MegrendelesFieldType.Int || keyEquality) {
        "="
    } else if (columnData.megrendelesFieldType == MegrendelesFieldType.Date) {
        "="
    } else {
        "Tartalmazza"
    }
    val defaultOperand = if (columnData.megrendelesFieldType == MegrendelesFieldType.Int || keyEquality) {
        "0"
    } else if (columnData.megrendelesFieldType == MegrendelesFieldType.Date) {
        moment().format(dateFormat)
    } else {
        ""
    }
    setState(state.copy(
            szuroMezok = state.szuroMezok + SzuroMezo(columnData, defaultOperator, defaultOperand)
    ))
}

private fun RBuilder.ebOperatorAndOperand(appState: AppState,
                                          state: CustomSearchModalState,
                                          setState: Dispatcher<CustomSearchModalState>,
                                          index: Int,
                                          szuroMezo: SzuroMezo) {
    Col(span = 6) {
        operatorSelect(arrayOf("="), szuroMezo) { operator, operand ->
            szuroMezoChanged(state, setState, index, operand, operator)
        }
    }
    Col(span = 6) {
        val data = appState.alvallalkozoState.ertekbecslok.mapValues { it.value.name }.toList()
        selectInput(szuroMezo, data) { operator, operand ->
            szuroMezoChanged(state, setState, index, operand, operator)
        }
    }
}

private fun RBuilder.avOperatorAndOperand(appState: AppState,
                                          state: CustomSearchModalState, setState: Dispatcher<CustomSearchModalState>,
                                          index: Int,
                                          szuroMezo: SzuroMezo) {
    Col(span = 6) {
        operatorSelect(arrayOf("="), szuroMezo) { operator, operand ->
            szuroMezoChanged(state, setState, index, operand, operator)
        }
    }
    Col(span = 6) {
        val data = appState.alvallalkozoState.alvallalkozok.mapValues { it.value.name }.toList()
        selectInput(szuroMezo, data) { operator, operand ->
            szuroMezoChanged(state, setState, index, operand, operator)
        }
    }
}

private fun RBuilder.intOperatorAndOperand(state: CustomSearchModalState, setState: Dispatcher<CustomSearchModalState>,
                                           index: Int, szuroMezo: SzuroMezo) {
    Col(span = 6) {
        operatorSelect(arrayOf("egyenlő", "nagyobb", "kisebb", "nagyobb egyenlő", "kisebb egyenlő"), szuroMezo) { operator, operand ->
            szuroMezoChanged(state, setState, index, operand, operator)
        }
    }
    Col(span = 6) {
        numberInput(szuroMezo) { operator, operand ->
            szuroMezoChanged(state, setState, index, operand, operator)
        }
    }
}

private fun RBuilder.stringOperatorAndOperand(state: CustomSearchModalState, setState: Dispatcher<CustomSearchModalState>,
                                              index: Int, szuroMezo: SzuroMezo) {
    Col(span = 6) {
        operatorSelect(arrayOf("tartalmazza", "nem tartamazza", "kezdődik", "végződik"), szuroMezo) { operator, operand ->
            szuroMezoChanged(state, setState, index, operand, operator)
        }
    }
    Col(span = 6) {
        stringInput(szuroMezo) { operator, operand ->
            szuroMezoChanged(state, setState, index, operand, operator)
        }
    }
}

private fun RBuilder.dateOperatorAndOperand(state: CustomSearchModalState, setState: Dispatcher<CustomSearchModalState>,
                                            index: Int, szuroMezo: SzuroMezo) {
    Col(span = 6) {
        operatorSelect(arrayOf("aznap", "később", "korábban", "aznap vagy később", "aznap vagy korábban", "NULL"), szuroMezo) { operator, operand ->
            szuroMezoChanged(state, setState, index, operand, operator)
        }
    }
    if (szuroMezo.operator != "NULL") {
        Col(span = 6) {
            dateInput(szuroMezo) { operator, operand ->
                szuroMezoChanged(state, setState, index, operand, operator)
            }
        }
    }
}

private fun RBuilder.operatorSelect(values: Array<String>, szuroMezo: SzuroMezo, whenChange: (String, String) -> Unit) {
    Select {
        attrs.value = szuroMezo.operator
        attrs.onSelect = { value: String, option ->
            whenChange(value, szuroMezo.operand)
        }
        values.forEach {
            Option { attrs.value = it; +it }
        }
    }
}

private fun szuroMezoChanged(state: CustomSearchModalState, setState: Dispatcher<CustomSearchModalState>,
                             index: Int,
                             operand: String,
                             operator: String) {
    setState(state.copy(
            szuroMezok = state.szuroMezok.take(index) + state.szuroMezok[index].copy(
                    operator = operator,
                    operand = operand
            ) + state.szuroMezok.drop(index + 1)
    ))
}

private fun RBuilder.stringInput(szuroMezo: SzuroMezo, whenChange: (String, String) -> Unit) {
    Input {
        attrs.value = szuroMezo.operand
        attrs.onChange = { e ->
            val value = e.currentTarget.asDynamic().value
            whenChange(szuroMezo.operator, value)
        }
    }
}

private fun <KEY> RBuilder.selectInput(szuroMezo: SzuroMezo, data: List<Pair<KEY, String>>, whenChange: (String, String) -> Unit) {
    Select {
        attrs.value = szuroMezo.operand
        attrs.asDynamic().style = jsStyle { textAlign = "right" }
        attrs.onSelect = { value: String, option ->
            whenChange(szuroMezo.operator, value)
        }
        (listOf(Pair("0", "")) + data).forEach {
            Option { attrs.value = it.first.toString(); +it.second }
        }
    }
}

private fun RBuilder.numberInput(szuroMezo: SzuroMezo, whenChange: (String, String) -> Unit) {
    MyNumberInput {
        attrs.value = szuroMezo.operand
        attrs.asDynamic().style = jsStyle { textAlign = "right" }
        attrs.onValueChange = { value ->
            whenChange(szuroMezo.operator, value.toString())
        }
    }
}

private fun RBuilder.dateInput(szuroMezo: SzuroMezo, whenChange: (String, String) -> Unit) {
    DatePicker {
        attrs.allowClear = false
        attrs.value = moment(szuroMezo.operand)
        attrs.onChange = { date, str ->
            if (date != null) {
                whenChange(szuroMezo.operator, date.format(dateFormat))
            }
        }
    }
}
