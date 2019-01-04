package store

import app.common.Moment
import app.common.moment
import hu.nevermind.iktato.RestUrl
import hu.nevermind.utils.store.Role
import hu.nevermind.utils.store.communicator
import hu.nevermind.utils.store.dateTimeFormat
import hu.nevermind.utils.store.fractionalSecondFormat

data class SajatAr(
        var id: Int = 0,
        var leiras: String = "",
        var nettoAr: Int = 0,
        var megrendelo: String,
        var afa: Int = 0,
        var munkatipus: String,

        var created: Moment = moment(),
        var createdBy: String = "",
        var modified: Moment = moment(),
        var modifiedBy: String = ""
) {

}


data class SajatArState(
        val allMunkatipus: Array<String> = emptyArray(),
        val allLeiras: Array<String> = emptyArray(),
        val allMegrendelo: Array<String> = emptyArray(),
        val sajatArak: Map<Int, SajatAr> = emptyMap()
) {

    fun getSajatArakFor(megrendelo: String, munkatipus: String): Collection<SajatAr> {
        return sajatArak.values
                .filter { it.megrendelo == megrendelo }
                .filter { it.munkatipus == munkatipus }

    }

    fun getMunkatipusokForMegrendelo(megrendelo: String): Collection<String> {
        return sajatArak.values
                .filter { it.megrendelo == megrendelo }
                .map { it.munkatipus }
                .distinct()
    }
}

fun sajatArActionHandler(state: SajatArState, action: Action): SajatArState {
    return when (action) {
        is Action.MegrendelesekFromServer -> state
        is Action.SetLoggedInUser -> {
            val sajatArak = if (action.data != null && action.data.role == Role.ROLE_ADMIN) {
                loadSajatArak()
            } else {
                hashMapOf()
            }
            SajatArState(
                    sajatArak = sajatArak,
                    allLeiras = sajatArak.values.map { it.leiras }.distinct().toTypedArray(),
                    allMunkatipus = sajatArak.values.map { it.munkatipus }.distinct().toTypedArray(),
                    allMegrendelo = sajatArak.values.map { it.megrendelo }.distinct().toTypedArray()
            )
        }
        is Action.ChangeURL -> state
        is Action.changeURLSilently -> state
        is Action.FilterMegrendelesek -> state
        is Action.SajatArFromServer -> {
            val newSajatArak = state.sajatArak + (action.sajatAr.id to jsonToSajatAr(action.sajatAr))
            state.copy(
                    sajatArak = newSajatArak,
                    allLeiras = newSajatArak.values.map { it.leiras }.distinct().toTypedArray(),
                    allMunkatipus = newSajatArak.values.map { it.munkatipus }.distinct().toTypedArray(),
                    allMegrendelo = newSajatArak.values.map { it.megrendelo }.distinct().toTypedArray()
            )
        }
        is Action.AccountFromServer -> state
        is Action.AlvallalkozoFromServer -> state
        is Action.ErtekbecsloFromServer -> state
        is Action.RegioOsszerendelesFromServer -> state
        is Action.DeleteRegioOsszerendeles -> state
    }
}


private fun loadSajatArak(): Map<Int, SajatAr> {
    return communicator.getEntitiesFromServer(RestUrl.getAllSajatAr) { returnedArray: Array<dynamic> ->
        returnedArray.map { json ->
            Pair(json.id, jsonToSajatAr(json))
        }.toMap()
    }
}

private fun jsonToSajatAr(json: dynamic): SajatAr {
    return SajatAr(
            id = json.id,
            leiras = json.leiras,
            afa = json.afa,
            nettoAr = (json.nettoAr as Double).toInt(),
            munkatipus = json.munkatipus,
            megrendelo = json.megrendelo,
            created = moment(json.created, dateTimeFormat),
            modified = moment(json.modified, fractionalSecondFormat),
            createdBy = json.createdBy,
            modifiedBy = json.modifiedBy
    )
}