package app

import hu.nevermind.antd.*
import hu.nevermind.antd.table.ColumnAlign
import hu.nevermind.antd.table.ColumnProps
import hu.nevermind.antd.table.Table
import hu.nevermind.iktato.Path
import hu.nevermind.iktato.RestUrl
import hu.nevermind.utils.app.AlvallalkozoModalComponent
import hu.nevermind.utils.app.AlvallalkozoModalParams
import hu.nevermind.utils.app.DefinedReactComponent
import hu.nevermind.utils.hu.nevermind.antd.message
import hu.nevermind.utils.jsStyle
import hu.nevermind.utils.store.Alvallalkozo
import hu.nevermind.utils.store.communicator
import kotlinext.js.jsObject
import kotlinx.html.DIV
import kotlinx.html.style
import react.RBuilder
import react.RElementBuilder
import react.buildElement
import react.dom.RDOMBuilder
import react.dom.div
import react.dom.jsStyle
import react.dom.li
import react.dom.ul
import store.Action


data class AlvallalkozoScreenState(
        val nameSarchText: String,
        val tagsagiSzamSearchText: String
)

data class AlvallalkozoScreenParams(val editingAlvallalkozoId: Int?,
                                    val appState: AppState,
                                    val globalDispatch: (Action) -> Unit)

object AlvallalkozoScreenComponent : DefinedReactComponent<AlvallalkozoScreenParams>() {
    override fun RBuilder.body(props: AlvallalkozoScreenParams) {
        val appState: AppState = props.appState
        val globalDispatch: (Action) -> Unit = props.globalDispatch
        val editingAlvallalkozoId: Int? = props.editingAlvallalkozoId
        val (state, setState) = useState(AlvallalkozoScreenState("", ""))
        div {
            Row {
                Col(offset = 20, span = 3) {
                    addNewButton(globalDispatch)
                }
            }
            Row {
                Col(span = 22, offset = 1) {
                    alvallalkozoTable(appState, globalDispatch, state, setState)
                }
            }
            if (editingAlvallalkozoId != null) {
                alvallalkozoEditingModal(editingAlvallalkozoId, appState, globalDispatch)
            }
        }
    }
}


private fun RDOMBuilder<DIV>.alvallalkozoEditingModal(editingAlvallalkozoId: Int, appState: AppState, globalDispatch: (Action) -> Unit) {
    val editingAlvallalkozo = if (editingAlvallalkozoId == 0) {
        Alvallalkozo(
                id = 0
        )
    } else {
        appState.alvallalkozoState.alvallalkozok[editingAlvallalkozoId]!!
    }
    AlvallalkozoModalComponent.insert(this, AlvallalkozoModalParams(editingAlvallalkozo, appState) { okButtonPressed, alvallalkozo ->
        if (okButtonPressed && alvallalkozo != null) {
            communicator.saveEntity<Any, dynamic>(RestUrl.saveAlvallalkozo, alvallalkozo) { response ->
                globalDispatch(Action.AlvallalkozoFromServer(response))
                globalDispatch(Action.ChangeURL(Path.alvallalkozo.root))
                message.success("Alvállalkozó ${if (alvallalkozo.id == 0) "létrehozva" else "módosítva"}")
            }
        } else {
            globalDispatch(Action.ChangeURL(Path.alvallalkozo.root))
        }
    })
}

private fun RElementBuilder<ColProps>.addNewButton(globalDispatch: (Action) -> Unit) {
    Button {
        attrs.asDynamic().id = AlvallalkozoScreenIds.addButton
        attrs.type = ButtonType.primary
        attrs.block = true
        attrs.onClick = {
            globalDispatch(Action.ChangeURL(Path.alvallalkozo.withOpenedAlvallalkozoEditorModal(0)))
        }
        Icon("plus-circle")
        +" Hozzáadás"
    }
}


private fun RBuilder.alvallalkozoTable(appState: AppState,
                                       globalDispatch: (Action) -> Unit,
                                       state: AlvallalkozoScreenState,
                                       setState: Dispatcher<AlvallalkozoScreenState>) {
    val columns = arrayOf(
            StringFilteringColumnProps<Alvallalkozo>(state.nameSarchText,
                    id = AlvallalkozoScreenIds.table.nameSearchButton,
                    setState = { changedSearchText -> setState(state.copy(nameSarchText = changedSearchText)) },
                    fieldGetter = { alvallalkozo -> alvallalkozo.name }
            ) {
                title = "Név"; dataIndex = "name"; width = 150
            },
            ColumnProps {
                title = "Telefon"; dataIndex = "phone"; width = 130
                render = { phones: String, _, _ ->
                    buildElement {
                        val phoneArr = phones.split("[^\\d\\+\\-\\/]+".toRegex())
                        if (phoneArr.size > 1) {
                            ul {
                                attrs.jsStyle { listStyle = "none"; margin = 0; padding = 0 }
                                phoneArr.forEach {
                                    li {
                                        attrs.jsStyle { margin = 0; padding = 0 }
                                        +it
                                    }
                                }
                            }
                        } else {
                            +phones
                        }
                    }
                }
            },
            ColumnProps {
                title = "Kapcsolattartó"; dataIndex = "kapcsolatTarto"; width = 150
            },
            StringFilteringColumnProps<Alvallalkozo>(state.tagsagiSzamSearchText,
                    setState = { changedSearchText -> setState(state.copy(tagsagiSzamSearchText = changedSearchText)) },
                    fieldGetter = { alvallalkozo -> alvallalkozo.tagsagiSzam }
            ) {
                title = "Tagsági szám"; dataIndex = "tagsagiSzam"; width = 100
            },
            ColumnProps {
                title = "Cím"; dataIndex = "cim"; width = 300
            },
            ColumnProps {
                title = "Állapot"; key = "formState"; width = 50; align = ColumnAlign.center
                render = { alvallalkozo: Alvallalkozo, _, _ ->
                    buildElement {
                        Tag {
                            attrs.color = if (alvallalkozo.disabled) "red" else "green"
                            +(if (alvallalkozo.disabled) "Tiltva" else "Engedélyezve")
                        }
                    }
                }
                filters = arrayOf<dynamic>(
                        jsObject { this.asDynamic().text = "Engedélyezve"; this.asDynamic().value = "false" },
                        jsObject { this.asDynamic().text = "Tiltva"; this.asDynamic().value = "true" }
                )
                onFilter = { value: String, record: Alvallalkozo ->
                    record.disabled == value.toBoolean()
                }
            },
            ColumnProps {
                title = "Szerk"; key = "action"; width = 200; align = ColumnAlign.center
                render = { row: Alvallalkozo, _, rowIndex ->
                    buildElement {
                        div {
                            attrs.jsStyle = jsStyle { width = "190px" }
                            Tooltip("Szerkesztés") {
                                Button {
                                    attrs.asDynamic().id = AlvallalkozoScreenIds.table.row.editButton(rowIndex)
                                    attrs.icon = "edit"
                                    attrs.onClick = {
                                        globalDispatch(Action.ChangeURL(Path.alvallalkozo.withOpenedAlvallalkozoEditorModal(row.id)))
                                    }
                                }
                            }
                            Divider(type = DividerType.vertical)
                            Tooltip("Értékbecslők") {
                                val ertekbecslok = appState.alvallalkozoState.getErtekbecslokOf(row).size
                                Badge(ertekbecslok) {
                                    attrs.showZero = true
                                    attrs.asDynamic().style = jsStyle {
                                        backgroundColor = "#1890ff"
                                    }
                                    Button {
                                        attrs.asDynamic().id = AlvallalkozoScreenIds.table.row.ertekbecslok(rowIndex)
                                        attrs.icon = "usergroup-add"
                                        attrs.onClick = {
                                            globalDispatch(Action.ChangeURL(Path.ertekbecslo.root(row.id)))
                                        }
                                    }

                                }
                            }
                            Divider(type = DividerType.vertical)
                            Tooltip("Régió összerendelések") {
                                Button {
                                    attrs.asDynamic().id = AlvallalkozoScreenIds.table.row.regioButton(rowIndex)
                                    attrs.icon = "picture"
                                    attrs.onClick = {
                                        globalDispatch(Action.ChangeURL(Path.alvallalkozo.regio(row.id)))
                                    }
                                }
                            }
                            Divider(type = DividerType.vertical)
                            Tooltip("Törlés") {
                                Button {
                                    attrs.asDynamic().id = AlvallalkozoScreenIds.table.row.deleteButton(rowIndex)
                                    attrs.icon = "delete"
                                    attrs.type = ButtonType.danger
                                    attrs.disabled =
                                            appState.alvallalkozoState.getErtekbecslokOf(row).isNotEmpty() ||
                                            appState.accountStore.accounts.any { it.alvallalkozoId == row.id }
                                    attrs.onClick = {
                                        Modal.confim {
                                            title = "Biztos törli a '${row.name}' Alvállalkozót?"
                                            okText = "Igen"
                                            cancelText = "Mégsem"
                                            okType = ButtonType.danger
                                            onOk = {
                                                globalDispatch(Action.DeleteAlvallalkozo(row))
                                                null
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
    )
    Table {
        attrs.columns = columns
        attrs.dataSource = appState.alvallalkozoState.alvallalkozok.values.sortedBy { it.name }.toTypedArray()
        attrs.rowKey = "id"
        attrs.bordered = true
        attrs.asDynamic().size = "middle"
    }
}

object AlvallalkozoScreenIds {
    val screenId = "alvallalkozoScreen"
    val addButton = "${screenId}_addButton"

    object table {
        val id = "${screenId}_table"
        val nameSearchButton = "${id}_name_search_button"

        object row {
            val editButton: (Int) -> String = { rowIndex -> "${id}_editButton_$rowIndex" }
            val deleteButton: (Int) -> String = { rowIndex -> "${id}_deleteButton_$rowIndex" }
            val ertekbecslok: (Int) -> String = { rowIndex -> "${id}_ebs$rowIndex" }
            val regioButton: (Int) -> String = { rowIndex -> "${id}_regioButton_$rowIndex" }
        }
    }

    object modal {
        val id = "${screenId}_modal"

        object inputs {
            val name = "${id}_name"
            val phone = "${id}_phone"
            val szamlaszam = "${id}_szamlaszam"
            val kapcsolatTarto = "${id}_kapcsolattarto"
            val email = "${id}_email"
            val adoszam = "${id}adoszam"
            val tagsagiSzam = "${id}_tagsagiSzam"
            val cim = "${id}_cim"
            val loginname = "${id}_loginname"
            val keszpenzes = "${id}_keszpenzes"
            val disabled = "${id}_disabled"
        }

        object buttons {
            val save = "${id}_saveButton"
            val close = "${id}_closeButton"
        }
    }
}
