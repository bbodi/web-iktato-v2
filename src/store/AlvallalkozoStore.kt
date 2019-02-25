package hu.nevermind.utils.store

import hu.nevermind.iktato.RestUrl
import hu.nevermind.utils.hu.nevermind.antd.message
import store.Action


data class AlvallalkozoState(
        val allMunkatipus: Array<String> = emptyArray(),
        val allLeiras: Array<String> = emptyArray(),
        var alvallalkozok: Map<Int, Alvallalkozo> = hashMapOf(),
        var ertekbecslok: Map<Int, Ertekbecslo> = hashMapOf(),
        var regioOsszerendelesek: Map<Int, RegioOsszerendeles> = hashMapOf()
) {

    fun getAlvallalkozoOf(eb: Ertekbecslo): Alvallalkozo {
        return alvallalkozok[eb.alvallalkozoId]!!
    }

    fun getAlvallalkozoOf(regio: RegioOsszerendeles): Alvallalkozo {
        return alvallalkozok[regio.alvallalkozoId]!!
    }

    fun getErtekbecslokOf(av: Alvallalkozo): Collection<Ertekbecslo> {
        return getErtekbecslokOf(av.id)
    }

    fun getErtekbecslokOf(avId: Int): Collection<Ertekbecslo> {
        return ertekbecslok.values.filter { it.alvallalkozoId == avId }
    }

    fun getErtekbecslokWithoutAlvallalkozo(): Collection<Ertekbecslo> {
        return ertekbecslok.values.filter { it.alvallalkozoId == 0 }
    }

    fun getRegioOsszerendelesek(av: Alvallalkozo): Collection<RegioOsszerendeles> {
        return getRegioOsszerendelesek(av.id)
    }

    fun getRegioOsszerendelesek(avId: Int): List<RegioOsszerendeles> {
        return regioOsszerendelesek.values.filter { it.alvallalkozoId == avId }
    }

    fun getEnabledAlvallalkozok() = alvallalkozok.values.filter { !it.disabled }

    fun getSelectableAlvallalkozok(regio: String): Collection<Alvallalkozo> {
        return regioOsszerendelesek.values
                .filter { it.megye == regio }
                .map { regioOsszerendeles -> getAlvallalkozoOf(regioOsszerendeles) }
                .filter { alv -> alv.disabled == false }
                .distinctBy { alv -> alv.id }
    }
}

fun Map<Int, Alvallalkozo>.getEnabledAlvallalkozok() = this.values.filter { !it.disabled }

fun alvallalkozoActionHandler(state: AlvallalkozoState, action: Action): AlvallalkozoState {
    return when (action) {
        is Action.MegrendelesekFromServer -> state
        is Action.SetLoggedInUser -> {
            val loggedInUser = action.data
            if (loggedInUser != null) {
                val newState = when (loggedInUser.role) {
                    Role.ROLE_ADMIN -> {
                        state.copy(
                                alvallalkozok = loadAlvallalkozok().map { it.id to it }.toMap(),
                                ertekbecslok = loadErtekbecslok().map { it.id to it }.toMap(),
                                regioOsszerendelesek = loadRegioOsszerendelesek().map { it.id to it }.toMap()
                        )
                    }
                    Role.ROLE_USER -> state.copy(
                            alvallalkozok = loadSingleAlvallalkozoFromServer().let { hashMapOf(it.id to it) }
                    )
                    Role.ROLE_ELLENORZO -> state
                }
                newState.copy(
                        allLeiras = newState.regioOsszerendelesek.values.map { it.leiras }.distinct().toTypedArray(),
                        allMunkatipus = newState.regioOsszerendelesek.values.map { it.munkatipus }.distinct().toTypedArray()
                )
            } else {
                state.copy(
                        alvallalkozok = hashMapOf(),
                        ertekbecslok = hashMapOf(),
                        regioOsszerendelesek = hashMapOf(),
                        allLeiras = emptyArray(),
                        allMunkatipus = emptyArray()
                )
            }
        }
        is Action.ChangeURL -> state
        is Action.changeURLSilently -> state
        is Action.SetActiveFilter -> state
        is Action.SajatArFromServer -> state
        is Action.AccountFromServer -> state
        is Action.DeleteRegioOsszerendeles -> {
            communicator.deleteEntity("RegioOsszerendeles", action.regioOsszerendeles.id)
            message.success("Régió összerendelés törölve")
            state.copy(regioOsszerendelesek = state.regioOsszerendelesek - action.regioOsszerendeles.id)
        }
        is Action.RegioOsszerendelesFromServer -> {
            val regioOsszerendeles = createRegioOsszerendelesFromJson(action.response)
            state.copy(regioOsszerendelesek = state.regioOsszerendelesek + (regioOsszerendeles.id to regioOsszerendeles))
        }
        is Action.ErtekbecsloFromServer -> {
            val eb = createErtekbecsloFromJson(action.response)
            state.copy(ertekbecslok = state.ertekbecslok + (eb.id to eb))
        }
        is Action.AlvallalkozoFromServer -> {
            val av = createAlvallalkozoFromJson(action.response)
            state.copy(alvallalkozok = state.alvallalkozok + (av.id to av))
        }
    }
}

private fun loadSingleAlvallalkozoFromServer(): Alvallalkozo {
    return communicator.getEntityFromServer(RestUrl.getAlvallalkozoForAccount) { json: dynamic ->
        createAlvallalkozoFromJson(json)
    }
}

private fun loadRegioOsszerendelesek(): List<RegioOsszerendeles> {
    return communicator.getEntitiesFromServer(RestUrl.getAllRegioOsszerendeles) { returnedArray: Array<dynamic> ->
        returnedArray.map { json ->
            createRegioOsszerendelesFromJson(json)
        }
    }
}

private fun loadErtekbecslok(): List<Ertekbecslo> {
    return communicator.getEntitiesFromServer(RestUrl.getAllErtekbecslo) { returnedArray: Array<dynamic> ->
        returnedArray.map { json ->
            createErtekbecsloFromJson(json)
        }
    }
}

private fun loadAlvallalkozok(): List<Alvallalkozo> {
    return communicator.getEntitiesFromServer(RestUrl.getAlvallalkozokFromServer) { returnedArray: Array<dynamic> ->
        returnedArray.map { json ->
            createAlvallalkozoFromJson(json)
        }
    }
}

private fun createErtekbecsloFromJson(json: dynamic): Ertekbecslo {
    return Ertekbecslo(
            json.id,
            json.name,
            json.alvallalkozoId,
            json.phone,
            json.email,
            json.comment,
            json.megjelenhet == "nem"
    )
}

private fun createAlvallalkozoFromJson(json: dynamic): Alvallalkozo {
    return Alvallalkozo(
            json.id,
            json.name,
            json.phone,
            json.szamlaSzam,
            json.kapcsolatTarto,
            json.email,
            json.adoszam,
            json.tagsagiSzam,
            json.cim,
            json.keszpenzes == "igen",
            json.disabled == "igen"
    )
}

private fun createRegioOsszerendelesFromJson(json: dynamic): RegioOsszerendeles {
    return RegioOsszerendeles(
            json.id,
            alvallalkozoId = json.alvallalkozoId,
            megye = json.megye,
            leiras = json.leiras,
            munkatipus = json.munkatipus,
            nettoAr = json.nettoAr,
            afa = json.afa,
            jutalek = json.jutalek
    )
}

//fun registerEditingHandlers() {
//    register(globalDispatch, Actions.alvallalkozoFromServer) { response ->
//        _alvallalkozok.put(response.id, createAlvallalkozoFromJson(response))
//        emitChange()
//    }
//    register(globalDispatch, Actions.ertekbecsloFromServer) { response ->
//        _ertekbecslok.put(response.id, createErtekbecsloFromJson(response))
//        emitChange()
//    }
//    register(globalDispatch, Actions.regioOsszerendelesFromServer) { response ->
//        _regioOsszerendelesek.put(response.id, createRegioOsszerendelesFromJson(response))
//        emitChange()
//
//    }
//    register(globalDispatch, Actions.deleteRegioOsszerendeles) { modReg ->
//        communicator.deleteEntity("RegioOsszerendeles", modReg.id) {
//            _regioOsszerendelesek.remove(modReg.id)
//            emitChange()
//        }
//    }
//}