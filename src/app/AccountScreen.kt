package app

import hu.nevermind.antd.*
import hu.nevermind.antd.table.ColumnProps
import hu.nevermind.antd.table.Table
import hu.nevermind.iktato.Path
import hu.nevermind.iktato.RestUrl
import hu.nevermind.utils.app.accountModal
import hu.nevermind.utils.hu.nevermind.antd.StringOrReactElement
import hu.nevermind.utils.hu.nevermind.antd.message
import hu.nevermind.utils.jsStyle
import hu.nevermind.utils.store.Account
import hu.nevermind.utils.store.LoggedInUser
import hu.nevermind.utils.store.Role
import hu.nevermind.utils.store.communicator
import kotlinext.js.jsObject
import kotlinx.html.DIV
import react.*
import react.dom.RDOMBuilder
import react.dom.div
import store.Action
import kotlin.browser.window


val Highlighter: RClass<dynamic> = kotlinext.js.require("react-highlight-words")

data class AccountScreenState(
        val usernameSearchText: String,
        val fullnameSearchText: String
)

fun accountScreen(
        editingAccountId: Int?,
        user: LoggedInUser,
        appState: AppState,
        globalDispatch: (Action) -> Unit): ReactElement {
    return createElement({ props: dynamic ->
        val user: LoggedInUser = props.user
        val appState: AppState = props.appState
        val globalDispatch: (Action) -> Unit = props.globalDispatch
        val editingAccountId: Int? = props.editingAccountId
        val (state, setState) = useState(AccountScreenState("", ""))


        buildElement {
            div {
                Row {
                    Col(offset = 5, span = 2) {
                        addNewButton(globalDispatch)
                    }
                }
                Row {
                    Col(span = 14, offset = 5) {
                        accountTable(appState, globalDispatch, state, setState)
                    }
                }
                if (editingAccountId != null) {
                    accountEditingModal(editingAccountId, appState, globalDispatch)
                }
            }
        }
    }, jsObject<dynamic> {
        this.user = user
        this.appState = appState
        this.globalDispatch = globalDispatch
        this.editingAccountId = editingAccountId
    })
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
    child(accountModal(editingAccount, appState) { okButtonPressed, account ->
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

private fun RElementBuilder<ColProps>.addNewButton(globalDispatch: (Action) -> Unit) {
    Button {
        attrs.asDynamic().id = AccountScreenIds.addButton
        attrs.type = ButtonType.primary
        attrs.onClick = {
            globalDispatch(Action.ChangeURL(Path.account.withOpenedEditorModal(0)))
        }
        Icon("plus")
        +" Hozzáadás"
    }
}

fun <T> StringFilteringColumnProps(searchText: String,
                               setState: Dispatcher<String>,
                               fieldGetter: (T) -> String,
                               body: ColumnProps.() -> Unit): ColumnProps {
    return ColumnProps {
        filterIcon = { filtered ->
            StringOrReactElement.from {
                Icon(type = "search", style = jsStyle { color = if (filtered) "#1890ff" else "" })
            }
        }
        var searchInput: Any? = null

        onFilterDropdownVisibleChange = { visible ->
            if (visible) {
                window.setTimeout({ searchInput.asDynamic().select() }, 0)
            }
        }
        filterDropdown = { filterDropdownData ->
            buildElement {
                val handleSearch = {
                    filterDropdownData.confirm()
                    setState(filterDropdownData.selectedKeys[0])
                }

                val handleReset = {
                    filterDropdownData.clearFilters()
                    setState("")
                }
                div("custom-filter-dropdown") {
                    Input {
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
        render = { text: String ->
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
                title = "Név"; dataIndex = "fullName"; width = 100
            },
            ColumnProps {
                title = "Állapot"; key = "state"; width = 100
                render = { account: Account ->
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
            }
    )
    Table {
        attrs.columns = columns
        attrs.dataSource = appState.accountStore.accounts.sortedBy { it.fullName }.toTypedArray()
        attrs.rowKey = "id"
        attrs.onRow = { account ->
            jsObject {
                this.asDynamic().onClick = { globalDispatch(Action.ChangeURL(Path.account.withOpenedEditorModal((account as Account).id))) }
            }
        }
        attrs.asDynamic().size = ButtonSize.small
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