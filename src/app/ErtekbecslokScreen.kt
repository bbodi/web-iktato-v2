package app

import hu.nevermind.antd.*
import hu.nevermind.antd.table.ColumnProps
import hu.nevermind.antd.table.Table
import hu.nevermind.iktato.Path
import hu.nevermind.iktato.RestUrl
import hu.nevermind.utils.app.DefinedReactComponent
import hu.nevermind.utils.app.ErtekbecsloModalComponent
import hu.nevermind.utils.app.ErtekbecsloModalParams
import hu.nevermind.utils.hu.nevermind.antd.message
import hu.nevermind.utils.jsStyle
import hu.nevermind.utils.store.Ertekbecslo
import hu.nevermind.utils.store.communicator
import kotlinext.js.jsObject
import react.RBuilder
import react.RElementBuilder
import react.buildElement
import react.children
import react.dom.div
import store.Action

data class ErtekbecsloScreenParams(val alvallalkozoId: Int,
                                   val editingErtekbecsloId: Int?,
                                   val appState: AppState,
                                   val globalDispatch: (Action) -> Unit)

object ErtekbecsloScreenComponent : DefinedReactComponent<ErtekbecsloScreenParams>() {
    override fun RBuilder.body(props: ErtekbecsloScreenParams) {
        val appState: AppState = props.appState
        val globalDispatch: (Action) -> Unit = props.globalDispatch
        val editingErtekbecsloId: Int? = props.editingErtekbecsloId
        val alvallalkozoId: Int = props.alvallalkozoId
        div {
            Breadcrumb {
                BreadcrumbItem {
                    Select {
                        attrs.asDynamic().style = jsStyle { minWidth = 300 }
                        attrs.showSearch = true
                        attrs.filterOption = { inputString, optionElement ->
                            (optionElement.props.children as String).toUpperCase().replace(" ", "").contains(inputString.toUpperCase().replace(" ", ""))
                        }
                        attrs.value = alvallalkozoId
                        attrs.onSelect = { value: Int, option ->
                            globalDispatch(Action.ChangeURL(Path.ertekbecslo.root(value)))
                        }
                        appState.alvallalkozoState.alvallalkozok.values.sortedBy { it.name }.forEach {
                            Option { attrs.value = it.id; +it.name }
                        }
                    }
                }
                BreadcrumbItem { +"Értékbecslők" }
            }
            Row { Col(span = 24) { attrs.asDynamic().style = jsStyle { height = 20 } } }
            Row {
                Col(offset = 3, span = 2) {
                    addNewButton(alvallalkozoId, globalDispatch)
                }
            }
            Row {
                Col(span = 18, offset = 3) {
                    ertekbecsloTable(alvallalkozoId, appState, globalDispatch)
                }
            }
            if (editingErtekbecsloId != null) {
                ertekbecsloEditingModal(alvallalkozoId, editingErtekbecsloId, appState, globalDispatch)
            }
        }
    }
}


private fun RBuilder.ertekbecsloEditingModal(alvallalkozoId: Int, editingErtekbecsloId: Int, appState: AppState, globalDispatch: (Action) -> Unit) {
    val editingErtekbecslo = if (editingErtekbecsloId == 0) {
        Ertekbecslo(
                id = 0,
                alvallalkozoId = alvallalkozoId
        )
    } else {
        appState.alvallalkozoState.ertekbecslok[editingErtekbecsloId]!!
    }

    ErtekbecsloModalComponent.insert(this, ErtekbecsloModalParams(editingErtekbecslo, appState) { okButtonPressed, eb ->
        if (okButtonPressed && eb != null) {
            val sendingEntity = object {
                val id = eb.id
                val name = eb.name
                val email = eb.email
                val telefonszam = eb.phone
                val megjegyzes = eb.comment
                val alvallalkozoId = eb.alvallalkozoId
                val megjelenhet = if (eb.disabled) "nem" else "igen"
            }
            communicator.saveEntity<Any, Any>(RestUrl.saveErtekBecslo, sendingEntity) { response ->
                globalDispatch(Action.ErtekbecsloFromServer(response))
                globalDispatch(Action.ChangeURL(Path.ertekbecslo.root(eb.alvallalkozoId)))
                message.success("Értékbecslő ${if (eb.id == 0) "létrehozva" else "módosítva"}")
            }
        } else {
            globalDispatch(Action.ChangeURL(Path.ertekbecslo.root(editingErtekbecslo.alvallalkozoId)))
        }
    })
}

private fun RElementBuilder<ColProps>.addNewButton(alvallalkozoId: Int, globalDispatch: (Action) -> Unit) {
    Button {
        attrs.asDynamic().id = AlvallalkozoScreenIds.addButton
        attrs.type = ButtonType.primary
        attrs.onClick = {
            globalDispatch(Action.ChangeURL(Path.ertekbecslo.withOpenedErtekbecsloEditorModal(alvallalkozoId, 0)))
        }
        Icon("plus")
        +" Hozzáadás"
    }
}


private fun RBuilder.ertekbecsloTable(alvallalkozoId: Int,
                                      appState: AppState,
                                      globalDispatch: (Action) -> Unit) {
    val columns = arrayOf(
            ColumnProps {
                title = "Név"; dataIndex = "name"; width = 150
            },
            ColumnProps {
                title = "Telefon"; dataIndex = "phone"; width = 130
            },
            ColumnProps {
                title = "Email"; dataIndex = "email"; width = 150
            },
            ColumnProps {
                title = "Állapot"; key = "formState"; width = 50
                render = { ertekbecslo: Ertekbecslo, _, _ ->
                    buildElement {
                        Tag {
                            attrs.color = if (ertekbecslo.disabled) "red" else "green"
                            +(if (ertekbecslo.disabled) "Tiltva" else "Engedélyezve")
                        }
                    }
                }
                filters = arrayOf<dynamic>(
                        jsObject { this.asDynamic().text = "Engedélyezve"; this.asDynamic().value = "false" },
                        jsObject { this.asDynamic().text = "Tiltva"; this.asDynamic().value = "true" }
                )
                onFilter = { value: String, record: Ertekbecslo ->
                    record.disabled == value.toBoolean()
                }
            },
            ColumnProps {
                title = ""; key = "action"; width = 100
                render = { row: Ertekbecslo, _, _ ->
                    buildElement {
                        div {
                            Tooltip("Szerkesztés") {
                                Button {
                                    attrs.icon = "edit"
                                    attrs.onClick = {
                                        globalDispatch(Action.ChangeURL(Path.ertekbecslo.withOpenedErtekbecsloEditorModal(alvallalkozoId, row.id)))
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
        attrs.dataSource = appState.alvallalkozoState.getErtekbecslokOf(alvallalkozoId).sortedBy { it.name }.toTypedArray()
        attrs.rowKey = "id"
        attrs.asDynamic().size = "middle"
    }
}