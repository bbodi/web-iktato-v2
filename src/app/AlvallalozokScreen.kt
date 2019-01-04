package app

import hu.nevermind.antd.Button
import hu.nevermind.antd.ButtonSize
import hu.nevermind.antd.ButtonType
import hu.nevermind.antd.Col
import hu.nevermind.antd.ColProps
import hu.nevermind.antd.Icon
import hu.nevermind.antd.Row
import hu.nevermind.antd.Tag
import hu.nevermind.antd.Tooltip
import hu.nevermind.antd.table.ColumnProps
import hu.nevermind.antd.table.Table
import hu.nevermind.iktato.Path
import hu.nevermind.iktato.RestUrl
import hu.nevermind.utils.app.alvallalkozoModal
import hu.nevermind.utils.hu.nevermind.antd.message
import hu.nevermind.utils.store.Alvallalkozo
import hu.nevermind.utils.store.LoggedInUser
import hu.nevermind.utils.store.communicator
import kotlinext.js.jsObject
import kotlinx.html.DIV
import react.RBuilder
import react.RElementBuilder
import react.ReactElement
import react.buildElement
import react.createElement
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

fun alvallalkozoScreen(
        editingAlvallalkozoId: Int?,
        user: LoggedInUser,
        appState: AppState,
        globalDispatch: (Action) -> Unit): ReactElement {
    return createElement({ props: dynamic ->
        val user: LoggedInUser = props.user
        val appState: AppState = props.appState
        val globalDispatch: (Action) -> Unit = props.globalDispatch
        val editingAlvallalkozoId: Int? = props.editingAlvallalkozoId
        val (state, setState) = useState(AlvallalkozoScreenState("", ""))


        buildElement {
            div {
                Row {
                    Col(offset = 1, span = 2) {
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
    }, jsObject<dynamic> {
        this.user = user
        this.appState = appState
        this.globalDispatch = globalDispatch
        this.editingAlvallalkozoId = editingAlvallalkozoId
    })
}

private fun RDOMBuilder<DIV>.alvallalkozoEditingModal(editingAlvallalkozoId: Int, appState: AppState, globalDispatch: (Action) -> Unit) {
    val editingAlvallalkozo = if (editingAlvallalkozoId == 0) {
        Alvallalkozo(
                id = 0
        )
    } else {
        appState.alvallalkozoState.alvallalkozok[editingAlvallalkozoId]!!
    }
    child(alvallalkozoModal(editingAlvallalkozo) { okButtonPressed, alvallalkozo ->
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
        attrs.onClick = {
            globalDispatch(Action.ChangeURL(Path.alvallalkozo.withOpenedAlvallalkozoEditorModal(0)))
        }
        Icon("plus")
        +" Hozzáadás"
    }
}


private fun RBuilder.alvallalkozoTable(appState: AppState,
                                       globalDispatch: (Action) -> Unit,
                                       state: AlvallalkozoScreenState,
                                       setState: Dispatcher<AlvallalkozoScreenState>) {
    val columns = arrayOf(
            StringFilteringColumnProps<Alvallalkozo>(state.nameSarchText,
                    setState = { changedSearchText -> setState(state.copy(nameSarchText = changedSearchText)) },
                    fieldGetter = { alvallalkozo -> alvallalkozo.name }
            ) {
                title = "Név"; dataIndex = "name"; width = 150
            },
            ColumnProps {
                title = "Telefon"; dataIndex = "phone"; width = 130
                render = { phones: String ->
                    buildElement {
                        val phoneArr = phones.split("[^\\d\\+\\-\\/]+".toRegex())
                        if (phoneArr.size > 1) {
                            ul {
                                attrs.jsStyle { listStyle = "none"; margin = 0; padding = 0 }
                                phoneArr.forEach { li {
                                    attrs.jsStyle { margin = 0; padding = 0 }
                                    +it
                                } }
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
                title = "Állapot"; key = "state"; width = 50
                render = { alvallalkozo: Alvallalkozo ->
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
                title = ""; key = "action"; width = 120
                render = { row: Alvallalkozo ->
                    buildElement {
                        div {
                            Tooltip("Szerkesztés") {
                                Button {
                                    attrs.icon = "edit"
                                    attrs.onClick = {
                                        globalDispatch(Action.ChangeURL(Path.alvallalkozo.withOpenedAlvallalkozoEditorModal(row.id)))
                                    }
                                }
                            }
                            +" "
                            Tooltip("Értékbecslők") {
                                Button {
                                    attrs.icon = "usergroup-add"
                                    attrs.onClick = {
                                        globalDispatch(Action.ChangeURL(Path.ertekbecslo.root(row.id)))
                                    }
                                }
                            }
                            Tooltip("Régió összerendelések") {
                                Button {
                                    attrs.icon = "picture"
                                    attrs.onClick = {
                                        globalDispatch(Action.ChangeURL(Path.alvallalkozo.regio(row.id)))
                                    }
                                }
                            } }
                    }
                }
            }
    )
    Table {
        attrs.columns = columns
        attrs.dataSource = appState.alvallalkozoState.alvallalkozok.values.sortedBy { it.name }.toTypedArray()
        attrs.rowKey = "id"
        attrs.asDynamic().size = ButtonSize.small
    }
}

object AlvallalkozoScreenIds {
    val screenId = "alvallalkozoScreen"
    val addButton = "${screenId}_addButton"

    object table {
        val id = "${screenId}_table"

        object row {
            val editButton: (Int) -> String = { rowIndex -> "${id}_editButton_$rowIndex" }
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