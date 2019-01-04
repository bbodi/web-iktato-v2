package store

import app.common.Moment
import app.common.moment
import hu.nevermind.iktato.RestUrl
import hu.nevermind.utils.store.Akadaly
import hu.nevermind.utils.store.FileData
import hu.nevermind.utils.store.Megrendeles
import hu.nevermind.utils.store.Munkatipusok
import hu.nevermind.utils.store.Statusz
import hu.nevermind.utils.store.communicator
import hu.nevermind.utils.store.dateFormat
import hu.nevermind.utils.store.dateTimeFormat
import hu.nevermind.utils.store.dateTimeSecondFormat
import hu.nevermind.utils.store.fractionalSecondFormat
import hu.nevermind.utils.store.monthFormat
import kotlin.browser.window

data class MegrendelesState(
        val megrendelesek: Map<Int, Megrendeles> = hashMapOf()
)

//val megrendelesReducer: Reducer<MegrendelesState, RAction> = { state, action ->
//    if (state == undefined) {
//        MegrendelesState(emptyMap())
//    } else when (action) {
//        is Actions.MegrendelesekFromServer -> {
//            megrendelesekFromServer(state, action)
//        }
//        else -> state
//    }
//}

fun megrendelesekFromServer(state: MegrendelesState, action: Action): MegrendelesState {
    return when (action) {
        is Action.MegrendelesekFromServer -> {
            if (state.megrendelesek.isEmpty()) {
                state.copy(megrendelesek = action.data.map {
                    (it.id as Int) to toMegrendeles(it)
                }.toMap())
            } else {
                val updatedMegrendelesek = action.data.map {
                    val updatedMegr = toMegrendeles(it)
                    val oldMegr = state.megrendelesek[updatedMegr.id]
                    if (oldMegr == null) {
                        console.info("Új Megrendelés: ${updatedMegr.id}, ${updatedMegr.azonosito}")
                    } else {
                        console.info("Megrendelés módosult: ${updatedMegr.id}, ${updatedMegr.azonosito}")
                        val diffs = diff(oldMegr, updatedMegr)
                        diffs?.forEach { diffNode ->
                            if (diffNode.kind == 'E') {
                                if (diffNode.path[0] != "modified")
                                    console.info(" ${diffNode.path[0]}: ${diffNode.lhs} -> ${diffNode.rhs}")
                            }
                        }
                    }
                    updatedMegr.id to updatedMegr
                    // TODO: notification az uj megrendelésről?!
                }
                state.copy(megrendelesek = state.megrendelesek + updatedMegrendelesek)
            }
        }
        is Action.SetLoggedInUser -> state
        is Action.ChangeURL -> state
        is Action.changeURLSilently -> state
        is Action.FilterMegrendelesek -> state.copy(megrendelesek = state.megrendelesek + filterMegrendelesek(
                action.megrendelesFilter.alvallalkozoId,
                action.megrendelesFilter.date
        ))
        is Action.SajatArFromServer -> state
        is Action.AccountFromServer -> state
        is Action.AlvallalkozoFromServer -> state
        is Action.ErtekbecsloFromServer -> state
        is Action.RegioOsszerendelesFromServer -> state
        is Action.DeleteRegioOsszerendeles -> state
    }
}


//
object MegrendelesStore {


    fun init() {
//        register(globalDispatcher, Actions.setLoggedInUser) { loggedInUser ->
//            if (loggedInUser != null) {
//                if (loggedInUser.role == Role.ROLE_ADMIN) {
//        loadMegrendelesek()
//                } else if (loggedInUser.role == Role.ROLE_USER) {
//                    loadMegrendelesekOfAlvallalkozo()
//                }
        window.setTimeout({
            pollServerForChanges(moment())
        }, 30_000)
    }
//            emitChange()
//        }
//        register(globalDispatcher, Actions.megrendelesekFromServer) { responses ->
//            responses.forEach { response ->
//                _megrendelesek.put(response.id, toMegrendeles(response))
//            }
//            emitChange()
//        }
//        register(globalDispatcher, Actions.filterMegrendelesek) { filter ->
//            filterMegrendelesek(filter.alvallalkozoId, filter.date)
//            emitChange()
//        }
}

//
private fun pollServerForChanges(lastUpdateTime: Moment) {
    val message: Any = object {
        val lastUpdate = lastUpdateTime.format(dateTimeSecondFormat)
    }
    val now = moment()
    communicator.getEntityFromServer(RestUrl.getMegrendelesUpdates, message) { response: dynamic ->
        //        store.dispatch(Actions.MegrendelesekFromServer(response))
        window.setTimeout({
            pollServerForChanges(now)
        }, 30_000)
//            if ((response as Array<Any>).isNotEmpty()) {
//                emitChange()
//            }
    }
}

//
//    private fun loadMegrendelesekOfAlvallalkozo() {
//        communicator.getEntitiesFromServer(RestUrl.getMegrendelesekOfAlvallalkozo) { returnedArray: Array<dynamic> ->
//            returnedArray.forEach {
//                val megr = toMegrendeles(it)
//                _megrendelesek.put(megr.id, megr)
//            }
//        }
//    }
//

//
//
//

//
//    fun getSelectableAlvallalkozok(regio: String): Collection<Alvallalkozo> {
//        return AlvallalkozoStore.regioOsszerendelesek.values
//                .filter { it.megye == regio }
//                .map { regioOsszerendeles -> AlvallalkozoStore.getAlvallalkozoOf(regioOsszerendeles) }
//                .filter { alv -> alv.disabled == false }
//                .distinctBy { alv -> alv.id }
//    }
//}

private fun filterMegrendelesek(avId: Int, d: Moment): List<Pair<Int, Megrendeles>> {
    return communicator.getEntitiesFromServer(
            RestUrl.getFilteredMegrendelesFromServer,
            object {
                val alvallalkozoId = avId
                val date = d.format(monthFormat)
            }
    ) { returnedArray: Array<dynamic> ->
        returnedArray.map {
            val megr = toMegrendeles(it)
            megr.id to megr
        }
    }
}

private fun toMegrendeles(json: dynamic): Megrendeles {
    val megr = Megrendeles()
    megr.id = json.id
    megr.azonosito = json.azonosito

    megr.ugyfelNeve = json.ugyfelNeve
    megr.ugyfelTel = json.ugyfelTel
    megr.ugyfelEmail = json.ugyfelEmail

    megr.hrsz = json.hrsz
    megr.regio = json.regio
    megr.irsz = json.irsz
    megr.telepules = json.telepules
    megr.kerulet = json.kerulet
    megr.utca = json.utca
    megr.utcaJelleg = json.utcaJelleg
    megr.hazszam = json.hazszam
    megr.lepcsohaz = json.lepcsohaz
    megr.emelet = json.emelet
    megr.ajto = json.ajto
    megr.ingatlanBovebbTipus = json.ingatlanBovebbTipus
    megr.ingatlanTipusMunkadijMeghatarozasahoz = json.ingatlanTipusMunkadijMeghatarozasahoz
    megr.lakasTerulet = strToInt(json.lakasTerulet)
    megr.telekTerulet = strToInt(json.telekTerulet)

    megr.keszultsegiFok = json.keszultsegiFok
    megr.eladasiAr = strToInt(json.eladasiAr)
    megr.becsultErtek = strToInt(json.becsultErtek)
    megr.fajlagosEladAr = strToInt(json.fajlagosEladAr)
    megr.fajlagosBecsultAr = strToInt(json.fajlagosBecsultAr)

    megr.hitelOsszeg = strToInt(json.hitelOsszeg)
    megr.afa = strToInt(json.afa) ?: 0
    megr.szamlazhatoDij = strToInt(json.szamlazhatoDij)

    megr.ertesitesiNev = json.ertesitesiNev
    megr.ertesitesiTel = json.ertesitesiTel
    megr.ajanlatSzam = json.ajanlatSzam
    megr.szerzodesSzam = json.szerzodesSzam
    megr.hetKod = json.hetKod
    if (json.energetika == "igen" && json.ertekbecsles == "igen") {
        megr.munkatipus = Munkatipusok.EnergetikaAndErtekBecsles.str
    } else if (json.ertekbecsles == "igen") {
        megr.munkatipus = Munkatipusok.Ertekbecsles.str
    } else if (json.energetika == "igen") {
        megr.munkatipus = Munkatipusok.Energetika.str
    } else {
        megr.munkatipus = json.munkatipus
    }

    megr.statusz = Statusz.fromString(json.statusz)

    megr.megrendelo = json.megrendelo
    megr.hitelTipus = json.hitelTipus
    megr.foVallalkozo = json.foVallalkozo

    megr.szamlaSorszama = json.szamlaSorszama
    megr.ertekbecsloDija = strToInt(json.ertekbecsloDija)

    megr.megjegyzes = json.megjegyzes
    megr.problema = json.problema


    megr.alvallalkozoId = json.alvallalkozoId
    megr.ertekbecsloId = json.ertekbecsloId
    if (json.hatarido != null) {
        megr.hatarido = readDateTime(json.hatarido, dateFormat)
    }
    if (json.megrendelesMegtekint != null) {
        megr.megrendelesMegtekint = readDateTime(json.megrendelesMegtekint)
    }
    if (json.megrendelve != null) {
        megr.megrendelve = readDateTime(json.megrendelve, dateFormat)
    }
    if (json.rogzitve != null) {
        megr.rogzitve = readDateTime(json.rogzitve)
    }
    if (json.zarolva != null) {
        megr.zarolva = readDateTime(json.zarolva, dateFormat)
    }
    if (json.feltoltve != null) {
        megr.feltoltveMegrendelonek = readDateTime(json.feltoltve, dateFormat)
    }
    if (json.keszpenzesBefizetes != null) {
        megr.keszpenzesBefizetes = readDateTime(json.keszpenzesBefizetes, dateFormat)
    }

    if (json.helysziniSzemle != null) {
        megr.szemleIdopontja = readDateTime(json.helysziniSzemle, dateFormat)
    }
    if (json.ertekbecslesFeltoltve != null) {
        megr.ertekbecslesFeltoltve = readDateTime(json.ertekbecslesFeltoltve)
    }

    if (json.energetikaFeltoltve != null) {
        megr.energetikaFeltoltve = readDateTime(json.energetikaFeltoltve)
    }

    if (!(json.penzBeerkezettDatum as String?).isNullOrEmpty()) {
        megr.penzBeerkezettDatum = readDateTime(json.penzBeerkezettDatum)
    }
    if (json.adasvetelDatuma != null) {
        megr.adasvetelDatuma = readDateTime(json.adasvetelDatuma)
    }

    megr.helyszinelo = json.helyszinelo
    megr.ellenorizve = json.ellenorizve == "igen"

    megr.created = readDateTime(json.created, dateTimeFormat)
    megr.modified = readDateTime(json.modified, fractionalSecondFormat)
    megr.createdBy = json.createdBy
    megr.modifiedBy = json.modifiedBy
    megr.readByAdmin = json.readByAdmin
    megr.readByAlvallalkozo = json.readByAlvallalkozo

    megr.files = (json.files as Array<dynamic>).map {
        FileData(it.name, it.humanReadableSize)
    }.toTypedArray()
    megr.akadalyok = (json.akadalyok as Array<dynamic>).map {
        jsonToAkadaly(it)
    }.toTypedArray()

    return megr
}

private fun jsonToAkadaly(it: dynamic): Akadaly {
    val akadaly = Akadaly(
            it.id,
            Statusz.fromString(it.statusz),
            it.leiras,
            if (it.ujHatarido != null) readDateTime(it.ujHatarido, dateFormat) else null,
            readDateTime(it.rogzitve, dateTimeFormat),
            it.megrendelesId)
    return akadaly
}

private fun strToInt(str: String): Int? {
    return str.toIntOrNull()
}

// A szerver Europe/Budapest timezone-ban fut, ami GMT+1. (UPDATE: melóheléyen GMT+1, itthon GMT+2...)
// Ezért amikor kiolvassa a DB-ből a dátumokat, úgy kezeli, mintha ebben az időzónában lennének,
// ezért amikor leküldi a szerverre, kivon belőle 1 órát.
// A DB-ben jól tárolódnak, csak a küldött érték lesz 1 órával kevesebb.
// UPDATE 04.01: Szerveroldalon mostantól Europe/Budapest timezone-nal küldjük le az időt, így az elméletileg a jó dátumot tartalmazza.
private fun readDateTime(str: String, format: String = dateTimeFormat): Moment {
    return moment(str, format)// .add(1, TimeUnit.Hours)
}

val diff: (a: Any, b: Any) -> Array<DiffResult>? = kotlinext.js.require("deep-diff").diff

external interface DiffResult {
    val kind: Char
    val path: Array<String>
    val lhs: String
    val rhs: String
}