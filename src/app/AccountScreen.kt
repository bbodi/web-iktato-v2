package app

import hu.nevermind.antd.*
import hu.nevermind.antd.table.ColumnAlign
import hu.nevermind.antd.table.ColumnProps
import hu.nevermind.antd.table.Table
import hu.nevermind.iktato.Path
import hu.nevermind.iktato.RestUrl
import hu.nevermind.utils.app.AccountModalComponent
import hu.nevermind.utils.app.AccountModalParams
import hu.nevermind.utils.app.DefinedReactComponent
import hu.nevermind.utils.hu.nevermind.antd.StringOrReactElement
import hu.nevermind.utils.hu.nevermind.antd.message
import hu.nevermind.utils.jsStyle
import hu.nevermind.utils.store.Account
import hu.nevermind.utils.store.Role
import hu.nevermind.utils.store.communicator
import kotlinext.js.jsObject
import kotlinx.html.DIV
import react.RBuilder
import react.RClass
import react.buildElement
import react.dom.RDOMBuilder
import react.dom.div
import store.Action
import kotlin.browser.window


val Highlighter: RClass<dynamic> = kotlinext.js.require("react-highlight-words")

data class AccountScreenState(
        val usernameSearchText: String,
        val fullnameSearchText: String
)

data class AccountScreenParams(val editingAccountId: Int?,
                               val appState: AppState,
                               val globalDispatch: (Action) -> Unit)

object AccountScreenComponent : DefinedReactComponent<AccountScreenParams>() {
    override fun RBuilder.body(props: AccountScreenParams) {
        val appState: AppState = props.appState
        val globalDispatch: (Action) -> Unit = props.globalDispatch
        val editingAccountId: Int? = props.editingAccountId
        val (state, setState) = useState(AccountScreenState("", ""))
        div {
            Row {
                Col(offset = 18, span = 3) {
                    addNewButton(globalDispatch)
                }
            }
            Row {
                Col(offset = 3, span = 18) {
                    accountTable(appState, globalDispatch, state, setState)
                }
            }
            if (editingAccountId != null) {
                accountEditingModal(editingAccountId, appState, globalDispatch)
            }
        }
    }
}

private fun RDOMBuilder<DIV>.accountEditingModal(editingAccountId: Int, appState: AppState, globalDispatch: (Action) -> Unit) {
    val editingAccount = if (editingAccountId == 0) {
        Account(
                id = 0,
                role = Role.ROLE_USER,
                alvallalkozoId = 0,
                plainPassword = ""
        )
    } else {
        appState.accountStore.accounts.first { it.id == editingAccountId }
    }
    AccountModalComponent.insert(this, AccountModalParams(editingAccount, appState) { okButtonPressed, account ->
        if (okButtonPressed && account != null) {
            val sendingEntity = object {
                val id = account.id
                val username = account.username
                val role = account.role.name
                val plainPassword = account.plainPassword
                val disabled = account.disabled
                val alvallalkozoId = account.alvallalkozoId
                val fullName = account.fullName
            }
            communicator.saveEntity<Any, dynamic>(RestUrl.saveAccount, sendingEntity) { response ->
                globalDispatch(Action.AccountFromServer(response))
                globalDispatch(Action.ChangeURL(Path.account.root))
                message.success("Felhasználó ${if (account.id == 0) "létrehozva" else "módosítva"}")
            }
        } else {
            globalDispatch(Action.ChangeURL(Path.account.root))
        }
    })
}

private fun RBuilder.addNewButton(globalDispatch: (Action) -> Unit) {
    Button {
        attrs.asDynamic().id = AccountScreenIds.addButton
        attrs.type = ButtonType.primary
        attrs.block = true
        attrs.onClick = {
            globalDispatch(Action.ChangeURL(Path.account.withOpenedEditorModal(0)))
        }
        Icon("plus-circle")
        +" Hozzáadás"
    }
}

fun <T> StringFilteringColumnProps(searchText: String,
                                   setState: Dispatcher<String>,
                                   fieldGetter: (T) -> String,
                                   id: String? = null,
                                   body: ColumnProps.() -> Unit): ColumnProps {
    return ColumnProps {
        filterIcon = { filtered ->
            StringOrReactElement.from {
                Icon(type = "search", style = jsStyle { color = if (filtered) "#1890ff" else "" }) {
                    attrs.asDynamic().id = id
                }
            }
        }
        var searchInput: Any? = null

        onFilterDropdownVisibleChange = { visible ->
            if (visible) {
                window.setTimeout({ searchInput.asDynamic()?.select() }, 0)
            }
        }
        filterDropdown = { filterDropdownData ->
            buildElement {
                val handleReset = {
                    filterDropdownData.clearFilters()
                    setState("")
                }
                val handleSearch = {
                    filterDropdownData.confirm()
                    if (filterDropdownData.selectedKeys[0].isEmpty()) {
                        handleReset()
                    } else {
                        setState(filterDropdownData.selectedKeys[0])
                    }
                }

                div("custom-filter-dropdown") {
                    Input {
                        attrs.asDynamic().id = if (id != null) "${id}_input" else null
                        attrs.asDynamic().ref = { node: Any -> searchInput = node }
                        attrs.placeholder = "Gépeljen be egy kifejezést majd nyomjon entert"
                        attrs.value = filterDropdownData.selectedKeys[0]
                        attrs.onChange = { e ->
                            val input = e.target.asDynamic().value as String?
                            if (input != null) {

                            }
                            filterDropdownData.setSelectedKeys(if (input != null) arrayOf(input) else emptyArray())
                        }
                        attrs.onPressEnter = { handleSearch() }
                        jsStyle { width = 188; marginBottom = 8; display = "block" }
                    }
                    Button {
                        attrs.asDynamic().id = if (id != null) "${id}_search_button" else null
                        attrs.type = ButtonType.primary
                        attrs.onClick = { handleSearch() }
                        attrs.icon = "search"
                        attrs.size = ButtonSize.small
                        jsStyle { width = 90; marginRight = 8 }
                        +"Keresés"
                    }
                    Button {
                        attrs.onClick = { handleReset() }
                        attrs.size = ButtonSize.small
                        jsStyle { width = 90 }
                        +"Törlés"
                    }
                }
            }
        }
        onFilter = { value, record ->
            fieldGetter(record as T).toUpperCase().replace(" ", "").contains((value as String).toUpperCase().replace(" ", ""))
        }
        render = { text: String, _, _ ->
            buildElement {
                Highlighter {
                    attrs.asDynamic().highlightStyle = jsStyle { backgroundColor = "#ffc069"; padding = 0 }
                    attrs.asDynamic().searchWords = arrayOf(searchText)
                    attrs.asDynamic().autoEscape = true
                    attrs.asDynamic().textToHighlight = text
                }
            }
        }
        body()
    }
}


private fun RBuilder.accountTable(appState: AppState,
                                  globalDispatch: (Action) -> Unit,
                                  state: AccountScreenState,
                                  setState: Dispatcher<AccountScreenState>) {
    val columns = arrayOf(
            StringFilteringColumnProps<Account>(state.usernameSearchText,
                    setState = { changedSearchText -> setState(state.copy(usernameSearchText = changedSearchText)) },
                    fieldGetter = { account -> account.username }
            ) {
                title = "Felhasználónév"; dataIndex = "username"; width = 100
            },
            StringFilteringColumnProps<Account>(state.fullnameSearchText,
                    setState = { changedSearchText -> setState(state.copy(fullnameSearchText = changedSearchText)) },
                    fieldGetter = { account -> account.fullName }
            ) {
                title = "Név"; dataIndex = "fullName"; width = 125
            },
            ColumnProps {
                title = "Állapot"; key = "formState"; width = 50; align = ColumnAlign.center
                render = { account: Account, _, _ ->
                    buildElement {
                        Tag {
                            attrs.color = if (account.disabled) "red" else "green"
                            +(if (account.disabled) "Tiltva" else "Engedélyezve")
                        }
                    }
                }
                filters = arrayOf<dynamic>(
                        jsObject { this.asDynamic().text = "Engedélyezve"; this.asDynamic().value = "false" },
                        jsObject { this.asDynamic().text = "Tiltva"; this.asDynamic().value = "true" }
                )
                onFilter = { value: String, record: Account ->
                    record.disabled == value.toBoolean()
                }
            },
            ColumnProps {
                title = "Szerk"; key = "action"; width = 50; align = ColumnAlign.center
                render = { row: Account, _, rowIndex ->
                    buildElement {
                        Tooltip("Szerkesztés") {
                            Button {
                                attrs.asDynamic().id = AccountScreenIds.table.row.editButton(rowIndex)
                                attrs.icon = "edit"
                                attrs.onClick = {
                                    globalDispatch(Action.ChangeURL(Path.account.withOpenedEditorModal(row.id)))
                                }
                            }
                        }
                    }
                }
            }
    )
    Table {
        attrs.columns = columns
        attrs.dataSource = appState.accountStore.accounts.sortedBy { it.fullName }.toTypedArray()
        attrs.rowKey = "id"
        attrs.bordered = true
        attrs.asDynamic().size = "middle"
    }
}

object AccountScreenIds {
    val screenId = "accountScreen"
    val addButton = "${screenId}_addButton"

    object table {
        val id = "${screenId}_table"

        object row {
            val editButton: (Int) -> String = { rowIndex -> "${id}_editButton_$rowIndex" }
        }
    }

    object modal {
        val id = "${screenId}_modal"

        object inputs {
            val username = "${id}_username"
            val fullName = "${id}_fullName"
            val alvallalkozo = "${id}_alvallalkozo"
            val password = "${id}_password"
            val disabled = "${id}_disabled"
        }

        object buttons {
            val save = "${id}_saveButton"
            val close = "${id}_closeButton"
        }
    }
}