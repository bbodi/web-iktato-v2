package app

import hu.nevermind.antd.*
import hu.nevermind.antd.table.ColumnAlign
import hu.nevermind.antd.table.ColumnProps
import hu.nevermind.antd.table.Table
import hu.nevermind.iktato.Path
import hu.nevermind.iktato.RestUrl
import hu.nevermind.utils.app.DefinedReactComponent
import hu.nevermind.utils.app.sajatArModal
import hu.nevermind.utils.hu.nevermind.antd.message
import hu.nevermind.utils.store.SajatAr
import hu.nevermind.utils.store.communicator
import kotlinext.js.jsObject
import kotlinx.html.DIV
import react.RBuilder
import react.RElementBuilder
import react.buildElement
import react.dom.RDOMBuilder
import react.dom.div
import store.Action

private data class ComponentState(
        val selectedMegrendelo: String,
        val selectedMunkatipus: String
)

data class SajatArScreenParams(val editingSajatArId: Int?,
                               val appState: AppState,
                               val globalDispatch: (Action) -> Unit)

object SajatArScreenComponent : DefinedReactComponent<SajatArScreenParams>() {
    override fun RBuilder.body(props: SajatArScreenParams) {
        val appState: AppState = props.appState
        val globalDispatch: (Action) -> Unit = props.globalDispatch
        val editingSajatArId: Int? = props.editingSajatArId

        val defaultMegrendelo = appState.sajatArState.allMegrendelo.first()
        val (state, setState) = useState(ComponentState(
                selectedMegrendelo = defaultMegrendelo,
                selectedMunkatipus = appState.sajatArState.getMunkatipusokForMegrendelo(defaultMegrendelo).first())
        )
        div {
            Row {
                Col(span = 13, offset = 4) {
                    megrendeloSelect(appState, state, setState)
                    munkatipusSelect(appState, state, setState)
                }
                Col(span = 2) {
                    addNewButton(globalDispatch)
                }
            }
            Row {
                Col(span = 16, offset = 4) {
                    table(appState, state, globalDispatch)
                }
            }
            if (editingSajatArId != null) {
                editingModal(editingSajatArId, appState, globalDispatch, setState, state)
            }
        }
    }
}

private fun RDOMBuilder<DIV>.editingModal(editingSajatArId: Int, appState: AppState, globalDispatch: (Action) -> Unit, componentDispatch: Dispatcher<ComponentState>, state: ComponentState) {
    val editingSajatAr = if (editingSajatArId == 0) {
        SajatAr(
                id = editingSajatArId,
                megrendelo = appState.sajatArState.allMegrendelo.first(),
                munkatipus = appState.sajatArState.allMunkatipus.first(),
                leiras = ""
        )
    } else {
        appState.sajatArState.sajatArak[editingSajatArId]!!
    }
    child(sajatArModal(editingSajatAr, appState) { okButtonPressed, sajatAr ->
        if (okButtonPressed && sajatAr != null) {
            val sendingEntity = object {
                val id = sajatAr.id
                val leiras = sajatAr.leiras
                val afa = sajatAr.afa
                val nettoAr = sajatAr.nettoAr
                val munkatipus = sajatAr.munkatipus
                val megrendelo = sajatAr.megrendelo
            }
            communicator.saveEntity<Any, SajatAr>(RestUrl.saveSajatAr, sendingEntity) { response ->
                globalDispatch(Action.SajatArFromServer(response))
                globalDispatch(Action.ChangeURL(Path.sajatAr.root))
                componentDispatch(state.copy(
                        selectedMegrendelo = sajatAr.megrendelo,
                        selectedMunkatipus = sajatAr.munkatipus
                ))
                message.success("Saját ár ${if (sajatAr.id == 0) "létrehozva" else "módosítva"}")
            }
        } else {
            globalDispatch(Action.ChangeURL(Path.sajatAr.root))
        }
    })
}

private fun RElementBuilder<ColProps>.addNewButton(globalDispatch: (Action) -> Unit) {
    Button {
        attrs.asDynamic().id = SajatArScreenIds.addButton
        attrs.type = ButtonType.primary
        attrs.onClick = {
            globalDispatch(Action.ChangeURL(Path.sajatAr.withOpenedEditorModal(0)))
        }
        Icon("plus")
        +" Hozzáadás"
    }
}

private fun RBuilder.megrendeloSelect(appState: AppState,
                                      state: ComponentState,
                                      componentDispatch: Dispatcher<ComponentState>) {
    Select {
        attrs.asDynamic().id = SajatArScreenIds.megrendeloSelect
        attrs.value = state.selectedMegrendelo
        attrs.onSelect = { value, option ->
            componentDispatch(
                    state.copy(
                            selectedMegrendelo = value,
                            selectedMunkatipus = appState.sajatArState.getMunkatipusokForMegrendelo(value).first()
                    )
            )
        }
        appState.sajatArState.allMegrendelo.forEach { megrendeloName ->
            Option { attrs.value = megrendeloName; +megrendeloName }
        }
    }
}

private fun RBuilder.munkatipusSelect(appState: AppState,
                                      state: ComponentState,
                                      componentDispatch: Dispatcher<ComponentState>) {
    Select {
        attrs.value = state.selectedMunkatipus
        attrs.asDynamic().id = SajatArScreenIds.munkatipusSelect
        attrs.onSelect = { value, option ->
            componentDispatch(
                    state.copy(selectedMunkatipus = value)
            )
        }
        appState.sajatArState.getMunkatipusokForMegrendelo(state.selectedMegrendelo).forEach { munkaTipus ->
            Option { attrs.value = munkaTipus; +munkaTipus }
        }
    }
}

private fun RBuilder.table(appState: AppState,
                           state: ComponentState,
                           globalDispatch: (Action) -> Unit) {
    val rows = appState.sajatArState.sajatArak.values
            .filter { it.megrendelo == state.selectedMegrendelo }
            .filter { it.munkatipus == state.selectedMunkatipus }
            .toTypedArray()


    val columns = arrayOf(
            ColumnProps { title = "Leírás"; dataIndex = "leiras"; width = 300 },
            ColumnProps {
                title = "Nettó ár (Ft)"; dataIndex = "nettoAr"; align = ColumnAlign.right; width = 100
                render = { nettoAr: Int, _, _ ->
                    buildElement {
                        +parseGroupedStringToNum(nettoAr.toString()).second
                    }
                }
            },
            ColumnProps { title = "ÁFA (%)"; dataIndex = "afa"; align = ColumnAlign.right; width = 100 }
    )
    Table {
        attrs.columns = columns
        attrs.dataSource = rows
        attrs.rowKey = "id"
        attrs.onRow = { sajatAr ->
            jsObject {
                this.asDynamic().onClick = { globalDispatch(Action.ChangeURL(Path.sajatAr.withOpenedEditorModal((sajatAr as SajatAr).id))) }
            }
        }
        attrs.asDynamic().size = "middle"
    }
}

object SajatArScreenIds {
    val screenId = "sajatArScreen"

    val addButton = "${screenId}add"
    val megrendeloSelect = "${screenId}megrendelo"
    val munkatipusSelect = "${screenId}munkatipus"

    object modal {
        private val prefix = "${screenId}_modal_"
        val id = "${prefix}root"

        object input {
            private val prefix = "${modal.prefix}input_"
            val megrendelo = "${prefix}megrendelo"
            val munkatipus = "${prefix}munkatipus"
            val leiras = "${prefix}leiras"
            val nettoAr = "${prefix}nettoAr"
            val afa = "${prefix}afa"
        }

        object button {
            private val prefix = "${modal.prefix}button_"
            val save = "${prefix}save"
            val close = "${prefix}close"
        }
    }

    val overlayInput = "overlayInput"

}
