package store

import app.common.Moment
import app.common.moment
import hu.nevermind.iktato.RestUrl
import hu.nevermind.utils.store.*
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
    val megr = Megrendeles(
            id = json.id,
            azonosito = json.azonosito,

            ugyfelNeve = json.ugyfelNeve,
            ugyfelTel = json.ugyfelTel,
            ugyfelEmail = json.ugyfelEmail,

            hrsz = json.hrsz,
            regio = json.regio,
            irsz = json.irsz,
            telepules = json.telepules,
            kerulet = json.kerulet,
            utca = json.utca,
            utcaJelleg = json.utcaJelleg,
            hazszam = json.hazszam,
            lepcsohaz = json.lepcsohaz,
            emelet = json.emelet,
            ajto = json.ajto,
            ingatlanBovebbTipus = json.ingatlanBovebbTipus,
            ingatlanTipusMunkadijMeghatarozasahoz = json.ingatlanTipusMunkadijMeghatarozasahoz,
            lakasTerulet = strToInt(json.lakasTerulet),
            telekTerulet = strToInt(json.telekTerulet),

            keszultsegiFok = json.keszultsegiFok,
            eladasiAr = strToInt(json.eladasiAr),
            becsultErtek = strToInt(json.becsultErtek),
            fajlagosEladAr = strToInt(json.fajlagosEladAr),
            fajlagosBecsultAr = strToInt(json.fajlagosBecsultAr),

            hitelOsszeg = strToInt(json.hitelOsszeg),
            afa = strToInt(json.afa) ?: 0,
            szamlazhatoDij = strToInt(json.szamlazhatoDij),

            ertesitesiNev = json.ertesitesiNev,
            ertesitesiTel = json.ertesitesiTel,
            ajanlatSzam = json.ajanlatSzam,
            szerzodesSzam = json.szerzodesSzam,
            hetKod = json.hetKod,
            munkatipus = if (json.energetika == "igen" && json.ertekbecsles == "igen") {
                Munkatipusok.EnergetikaAndErtekBecsles.str
            } else if (json.ertekbecsles == "igen") {
                Munkatipusok.Ertekbecsles.str
            } else if (json.energetika == "igen") {
                Munkatipusok.Energetika.str
            } else {
                json.munkatipus
            },

            statusz = Statusz.fromString(json.statusz),

            megrendelo = json.megrendelo,
            hitelTipus = json.hitelTipus,
            foVallalkozo = json.foVallalkozo,

            szamlaSorszama = json.szamlaSorszama,
            ertekbecsloDija = strToInt(json.ertekbecsloDija),

            megjegyzes = json.megjegyzes,
            problema = json.problema,


            alvallalkozoId = json.alvallalkozoId,
            ertekbecsloId = json.ertekbecsloId,
            hatarido = if (json.hatarido != null) {
                readDateTime(json.hatarido, dateFormat)
            } else null,
            megrendelesMegtekint = if (json.megrendelesMegtekint != null) {
                readDateTime(json.megrendelesMegtekint)
            } else null,
            megrendelve = if (json.megrendelve != null) {
                readDateTime(json.megrendelve, dateFormat)
            } else null,
            rogzitve = if (json.rogzitve != null) {
                readDateTime(json.rogzitve)
            } else null,
            zarolva = if (json.zarolva != null) {
                readDateTime(json.zarolva, dateFormat)
            } else null,
            feltoltveMegrendelonek = if (json.feltoltve != null) {
                readDateTime(json.feltoltve, dateFormat)
            } else null,
            keszpenzesBefizetes = if (json.keszpenzesBefizetes != null) {
                readDateTime(json.keszpenzesBefizetes, dateFormat)
            } else null,

            szemleIdopontja = if (json.helysziniSzemle != null) {
                readDateTime(json.helysziniSzemle, dateFormat)
            } else null,
            ertekbecslesFeltoltve = if (json.ertekbecslesFeltoltve != null) {
                readDateTime(json.ertekbecslesFeltoltve)
            } else null,

            energetikaFeltoltve = if (json.energetikaFeltoltve != null) {
                readDateTime(json.energetikaFeltoltve)
            } else null,

            penzBeerkezettDatum = if (!(json.penzBeerkezettDatum as String?).isNullOrEmpty()) {
                readDateTime(json.penzBeerkezettDatum)
            } else null,
            adasvetelDatuma = if (json.adasvetelDatuma != null) {
                readDateTime(json.adasvetelDatuma)
            } else null,

            helyszinelo = json.helyszinelo,
            ellenorizve = json.ellenorizve == "igen",

            created = readDateTime(json.created, dateTimeFormat),
            modified = readDateTime(json.modified, fractionalSecondFormat),
            createdBy = json.createdBy,
            modifiedBy = json.modifiedBy,
            readByAdmin = json.readByAdmin,
            readByAlvallalkozo = json.readByAlvallalkozo,

            files = (json.files as Array<dynamic>).map {
                FileData(it.name, it.humanReadableSize)
            }.toTypedArray(),
            akadalyok = (json.akadalyok as Array<dynamic>).map {
                jsonToAkadaly(it)
            }.toTypedArray()
    )
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