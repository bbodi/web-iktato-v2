package store

import hu.nevermind.iktato.RestUrl
import hu.nevermind.utils.store.Account
import hu.nevermind.utils.store.Role
import hu.nevermind.utils.store.communicator


data class AccountStore(
        val accounts: Array<Account>
) {

    fun findByUsername(username: String): Account? {
        return accounts.firstOrNull { it.username == username.orEmpty() }
    }

    fun findById(id: Int): Account? {
        return accounts.firstOrNull { it.id == id }
    }
}

fun accountActionHandler(state: AccountStore, action: Action): AccountStore {
    return when (action) {
        is Action.DeleteAlvallalkozo -> state
        is Action.DeleteMegrendeles -> state
        is Action.MegrendelesekFromServer -> state
        is Action.SetLoggedInUser -> {
            AccountStore(accounts = if (action.data?.role == Role.ROLE_ADMIN) loadAccounts() else emptyArray())
        }
        is Action.ChangeURL -> state
        is Action.changeURLSilently -> state
        is Action.SetActiveFilter -> state
        is Action.SajatArFromServer -> state
        is Action.AccountFromServer -> {
            val accountFromServer = jsonToAccount(action.response)
            val index = state.accounts.indexOfFirst { it.id == accountFromServer.id }
            if (index == -1) {
                state.copy(accounts = state.accounts + accountFromServer)
            } else {
                state.accounts[index] = accountFromServer
                state.copy(accounts = state.accounts)
            }
        }
        is Action.AlvallalkozoFromServer -> state
        is Action.ErtekbecsloFromServer -> state
        is Action.RegioOsszerendelesFromServer -> state
        is Action.DeleteRegioOsszerendeles -> state
    }
}

private fun loadAccounts(): Array<Account> {
    return communicator.getEntitiesFromServer(RestUrl.getAccountsFromServer) { returnedArray: Array<dynamic> ->
        val newEntities = returnedArray.map { json ->
            jsonToAccount(json)
        }.toTypedArray()
        newEntities
    }
}

private fun jsonToAccount(json: dynamic): Account {
    return Account(json.id,
            json.fullName,
            json.username,
            json.disabled,
            toRole(json.rawRole),
            json.alvallalkozoId,
            "")
}

private fun toRole(roleName: String): Role {
    return if (roleName.contains("admin") || roleName.contains("ADMIN")) {
        Role.ROLE_ADMIN
    } else {
        Role.ROLE_USER
    }
}