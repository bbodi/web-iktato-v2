package app

import hu.nevermind.antd.*
import hu.nevermind.antd.table.ColumnAlign
import hu.nevermind.antd.table.ColumnProps
import hu.nevermind.antd.table.Table
import hu.nevermind.iktato.Path
import hu.nevermind.iktato.RestUrl
import hu.nevermind.utils.app.regioModal
import hu.nevermind.utils.hu.nevermind.antd.Menu
import hu.nevermind.utils.hu.nevermind.antd.MenuItem
import hu.nevermind.utils.hu.nevermind.antd.MenuMode
import hu.nevermind.utils.hu.nevermind.antd.message
import hu.nevermind.utils.jsStyle
import hu.nevermind.utils.store.LoggedInUser
import hu.nevermind.utils.store.RegioOsszerendeles
import hu.nevermind.utils.store.communicator
import kotlinext.js.jsObject
import kotlinx.html.DIV
import react.*
import react.dom.RDOMBuilder
import react.dom.div
import react.dom.jsStyle
import react.dom.span
import store.Action
import store.megyek

private data class RegioScreenState(val selectedMegye: String)

fun regioScreen(
        alvallalkozoId: Int,
        editingRegioId: Int?,
        user: LoggedInUser,
        appState: AppState,
        globalDispatch: (Action) -> Unit): ReactElement {
    return createElement({ props: dynamic ->
        val user: LoggedInUser = props.user
        val appState: AppState = props.appState
        val globalDispatch: (Action) -> Unit = props.globalDispatch
        val editingRegioId: Int? = props.editingRegioId
        val alvallalkozoId: Int = (props.alvallalkozoId as Int)
        val (state, setState) = useState(RegioScreenState(megyek.first()))

        buildElement {
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
                                globalDispatch(Action.ChangeURL(Path.alvallalkozo.regio(value)))
                            }
                            appState.alvallalkozoState.alvallalkozok.values.sortedBy { it.name }.forEach {
                                Option { attrs.value = it.id; +it.name }
                            }
                        }
                    }
                    BreadcrumbItem { +"Régiók" }
                }
                Row { Col(span = 24) { attrs.asDynamic().style = jsStyle { height = 20 } } }
                Row {
                    Col(offset = 7, span = 2) {
                        addNewButton(alvallalkozoId, globalDispatch)
                    }
                }
                Row {
                    Col(span = 6) {
                        Menu {
                            attrs.mode = MenuMode.inline
                            attrs.selectedKeys = arrayOf(state.selectedMegye)
                            attrs.onSelect = { e ->
                                setState(state.copy(selectedMegye = e.key))
                            }
                            val megyeCounts = appState.alvallalkozoState.getRegioOsszerendelesek(alvallalkozoId).groupBy { it.megye }
                            megyek.forEach { megyeName ->
                                MenuItem(megyeName) {
                                    attrs.asDynamic().style = jsStyle {
                                        height = 30
                                    }
                                    val count = megyeCounts[megyeName]?.size ?: 0
                                    if (count > 0) {
                                        Badge(count) {
                                            attrs.asDynamic().style = jsStyle {
                                                backgroundColor = "#1890ff"
                                            }
                                            span {
                                                attrs.jsStyle {
                                                    marginRight = "15px"
                                                }
                                                +megyeName
                                            }
                                        }
                                    } else {
                                        +megyeName
                                    }
                                }
//                                }
                            }
                        }
                    }
                    Col(span = 18) {
                        Row {
                            Col(span = 22, offset = 1) {
                                regioTable(alvallalkozoId, state.selectedMegye, appState, globalDispatch)
                            }
                        }
                    }
                }
                if (editingRegioId != null) {
                    regioEditingModal(alvallalkozoId, editingRegioId, state.selectedMegye, appState, globalDispatch)
                }
            }
        }
    }, jsObject<dynamic> {
        this.user = user
        this.appState = appState
        this.globalDispatch = globalDispatch
        this.editingRegioId = editingRegioId
        this.alvallalkozoId = alvallalkozoId
    })
}

private fun RDOMBuilder<DIV>.regioEditingModal(alvallalkozoId: Int,
                                               editingRegioId: Int,
                                               megye: String, appState: AppState, globalDispatch: (Action) -> Unit) {
    val editingRegio = if (editingRegioId == 0) {
        RegioOsszerendeles(
                id = 0,
                alvallalkozoId = alvallalkozoId,
                megye = megye
        )
    } else {
        appState.alvallalkozoState.regioOsszerendelesek[editingRegioId]!!
    }
    val alvallalkozo = appState.alvallalkozoState.alvallalkozok[editingRegio.alvallalkozoId]!!
    child(regioModal(editingRegio, alvallalkozo) { okButtonPressed, regioOssz ->
        if (okButtonPressed && regioOssz != null) {
            val sendingEntity = object {
                val id = regioOssz.id
                val afa = regioOssz.afa
                val alvallalkozoId = regioOssz.alvallalkozoId
                val jutalek = regioOssz.jutalek
                val leiras = regioOssz.leiras
                val megye = regioOssz.megye
                val munkatipus = regioOssz.munkatipus
                val nettoAr = regioOssz.nettoAr
            }

            communicator.saveEntity<Any, Any>(RestUrl.saveRegioOsszerendeles, sendingEntity) { response ->
                globalDispatch(Action.RegioOsszerendelesFromServer(response))
                globalDispatch(Action.ChangeURL(Path.alvallalkozo.regio(regioOssz.alvallalkozoId)))
                message.success("Régió összerendelés ${if (regioOssz.id == 0) "létrehozva" else "módosítva"}")
            }
        } else {
            globalDispatch(Action.ChangeURL(Path.alvallalkozo.regio(alvallalkozoId)))
        }
    })
}

private fun RElementBuilder<ColProps>.addNewButton(alvallalkozoId: Int, globalDispatch: (Action) -> Unit) {
    Button {
        attrs.asDynamic().id = AlvallalkozoScreenIds.addButton
        attrs.type = ButtonType.primary
        attrs.onClick = {
            globalDispatch(Action.ChangeURL(Path.alvallalkozo.withOpenedRegioOsszerendelesModal(alvallalkozoId, 0)))
        }
        Icon("plus")
        +" Hozzáadás"
    }
}


private fun RBuilder.regioTable(alvallalkozoId: Int,
                                megye: String,
                                appState: AppState,
                                globalDispatch: (Action) -> Unit) {
    val columns = arrayOf(
            ColumnProps {
                title = "Munkatípus"; dataIndex = "munkatipus"; width = 150
            },
            ColumnProps {
                title = "Leírás"; dataIndex = "leiras"; width = 130
            },
            ColumnProps {
                title = "Nettó ár (Ft)"; dataIndex = "nettoAr"; width = 100; align = ColumnAlign.right
                render = { num: Int ->
                    buildElement {
                        +parseGroupedStringToNum(num.toString()).second
                    }
                }
            },
            ColumnProps {
                title = "Jutalék (Ft)"; dataIndex = "jutalek"; width = 100; align = ColumnAlign.right
                render = { num: Int ->
                    buildElement {
                        +parseGroupedStringToNum(num.toString()).second
                    }
                }
            },
            ColumnProps {
                title = "ÁFA (%)"; dataIndex = "afa"; width = 50; align = ColumnAlign.right
            },
            ColumnProps {
                title = ""; key = "action"; width = 100
                render = { row: RegioOsszerendeles ->
                    buildElement {
                        Row {
                            Col(span = 12) {
                                Tooltip("Szerkesztés") {
                                    Button {
                                        attrs.icon = "edit"
                                        attrs.onClick = {
                                            globalDispatch(Action.ChangeURL(Path.alvallalkozo.withOpenedRegioOsszerendelesModal(alvallalkozoId, row.id)))
                                        }
                                    }
                                }
                            }
                            Col(span = 12) {
                                Tooltip("Törlés") {
                                    Button {
                                        attrs.icon = "delete"
                                        attrs.type = ButtonType.danger
                                        attrs.onClick = {
                                            Modal.confim {
                                                title = "Biztos törli a Régió összerendelést?"
                                                okText = "Igen"
                                                cancelText = "Mégsem"
                                                okType = ButtonType.danger
                                                onOk = {
                                                    globalDispatch(Action.DeleteRegioOsszerendeles(row))
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
            }
    )
    Table {
        attrs.columns = columns
        attrs.dataSource = appState.alvallalkozoState.getRegioOsszerendelesek(alvallalkozoId).filter { it.megye == megye }.toTypedArray()
        attrs.rowKey = "id"
        attrs.asDynamic().size = ButtonSize.small
    }
}