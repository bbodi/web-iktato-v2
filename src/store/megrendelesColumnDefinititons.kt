package store

import app.AppState
import app.common.Moment
import app.megrendeles.alvallalkozoVegzettVeleAdottHonapban
import app.megrendeles.atNemVettFilter
import hu.nevermind.antd.Badge
import hu.nevermind.utils.store.*
import kotlinx.html.InputType
import kotlinx.html.js.onClickFunction
import react.buildElement
import react.dom.a
import react.dom.input


data class MegrendelesColumnData(val dbName: String,
                                 val fieldName: String,
                                 val columnTitle: String,
                                 val renderer: Any,
                                 val megrendelesFieldType: MegrendelesFieldType,
                                 val filter: FilterDef<out Any>? = null)

val stringRenderer = { cell: String?, row: Any, index: Int ->
    cell ?: ""
}

val clickableRenderer = { cell: String?, row: Any, index: Int, appState: AppState, onClick: dynamic ->
    buildElement {
        a {
            attrs.onClickFunction = {
                onClick(row.unsafeCast<Megrendeles>())
            }
            +(cell ?: "")
        }
    }
}


val idRenderer = { cell: String?, row: Any, index: Int, appState: AppState ->
    val megr = row.unsafeCast<Megrendeles>()
    val (alvallalkozoAzAtNemVettTabon, alvallalkozoAzElvegezveTabon) = if (appState.maybeLoggedInUser.isAlvallalkozo) {
        Pair(appState.megrendelesTableFilterState.activeFilter == atNemVettFilter,
                appState.megrendelesTableFilterState.activeFilter == alvallalkozoVegzettVeleAdottHonapban)
    } else Pair(false, false)
    val goodTab = !alvallalkozoAzAtNemVettTabon && !alvallalkozoAzElvegezveTabon
    buildElement {
        if (goodTab && megr.isUnread(appState.maybeLoggedInUser!!.role)) {
            Badge(1) {
                attrs.dot = true
                +(cell ?: "")
            }
        } else {
            +(cell ?: "")
        }
    }
}

val statuszRenderer = { cell: Statusz?, row: Any, index: Int ->
    cell?.text ?: ""
}

val dateRenderer = { cell: Any, entity: Any, index: Int ->
    (cell as Moment?)?.format(dateFormat) ?: ""
}

val numRenderer = { cell: Any, entity: Any, index: Int ->
    //formatNullableNumber(cell as Number?, 0, " ")
    "TODO: formatNullableNumber"
}

val ebRenderer = { ebId: Int, entity: Any, index: Int ->
    //    AlvallalkozoStore.ertekbecslok[ebId]?.name ?: ""
    ""
}

val avRenderer = { avId: Int, entity: Any, index: Int, appState: AppState ->
    appState.alvallalkozoState.alvallalkozok[avId]?.name ?: ""
}

val booleanRenderer = { cell: Any, entity: Any ->
    buildElement {
        input(type = InputType.checkBox) {
            attrs.checked = cell as Boolean
        }
    }
}

enum class MegrendelesFieldType {
    String, Int, Boolean, Date, Select
}

private val exactMatchFilter = { fieldGetter: (Megrendeles) -> String ->
    { value: String, row: Megrendeles ->
        value == fieldGetter(row)
    }
}

data class FilterDef<T>(val filterComboValues: (AppState, Array<Megrendeles>) -> Array<Pair<String, T>>,
                        val filter: ((value: T, row: Megrendeles) -> Boolean))


val megyek = arrayOf(
        "Bács-Kiskun",
        "Baranya",
        "Békés",
        "Borsod-Abaúj-Zemplén",
        "Budapest",
        "Csongrád",
        "Fejér",
        "Győr-Moson-Sopron",
        "Hajdú-Bihar",
        "Heves",
        "Jász-Nagykun-Szolnok",
        "Komárom-Esztergom",
        "Nógrád",
        "Pest",
        "Somogy",
        "Szabolcs-Szatmár-Bereg",
        "Tolna",
        "Vas",
        "Veszprém",
        "Zala"
).sortedArray()

val kozteruletJellegek = arrayOf(
        "dűlő", "sor", "utca", "út", "átjáró", "szőlőhegy"
)

val columnDefinitions = arrayOf<MegrendelesColumnData>(
        MegrendelesColumnData("id", "id", "Id", numRenderer, MegrendelesFieldType.Int),
        MegrendelesColumnData("kulso_id", "azonosito", "Azonosító", idRenderer, MegrendelesFieldType.String),
        MegrendelesColumnData("hrsz", "hrsz", "HRSZ", stringRenderer, MegrendelesFieldType.String),
        MegrendelesColumnData("regio", "regio", "Régió", stringRenderer, MegrendelesFieldType.String,
                FilterDef(
                        filterComboValues = { appState, megrendelesek ->
                            megrendelesek
                                    .map { it.regio }
                                    .distinct()
                                    .sorted()
                                    .map { it to it }.toTypedArray()
                        },
                        filter = exactMatchFilter { it.regio }
                )
        ),
        MegrendelesColumnData("irsz", "irsz", "IRSZ", stringRenderer, MegrendelesFieldType.String),
        MegrendelesColumnData("telepules", "telepules", "Település", stringRenderer, MegrendelesFieldType.String,
                FilterDef(
                        filterComboValues = { appState, megrendelesek ->
                            megrendelesek
                                    .map { it.telepules }
                                    .distinct()
                                    .sorted()
                                    .map { it to it }.toTypedArray()
                        },
                        filter = exactMatchFilter { it.telepules }
                )
        ),
        MegrendelesColumnData("kerulet", "kerulet", "Kerület", stringRenderer, MegrendelesFieldType.String),
        /*utca, "Utca", stringRenderer, MegrendelesFieldType.String),
        MegrendelesColumnData("utca_jelleg", "utcaJelleg", "Utca jelleg", stringRenderer, MegrendelesFieldType.String),
        MegrendelesColumnData("hazszam", "hazszam", "Házszám", stringRenderer, MegrendelesFieldType.String),
        MegrendelesColumnData("lepcsohaz", "lepcsohaz", "Lépcsőház", stringRenderer, MegrendelesFieldType.String),
        MegrendelesColumnData("emelet", "emelet", "Emelet", stringRenderer, MegrendelesFieldType.String),
        MegrendelesColumnData("ajto", "ajto", "Ajtó", stringRenderer, MegrendelesFieldType.String,*/
        MegrendelesColumnData("ingatlan_tipus", "ingatlanBovebbTipus", "Ingatlan bővebb típus", stringRenderer, MegrendelesFieldType.String),
        MegrendelesColumnData("ar_leiras", "ingatlanTipusMunkadijMeghatarozasahoz", "Ingatlan típus (munkadíj meghatározásához)", stringRenderer, MegrendelesFieldType.String), // ~ sajat AR
        MegrendelesColumnData("lakas_terulet", "lakasTerulet", "Lakás terület (m2)", numRenderer, MegrendelesFieldType.Int),
        MegrendelesColumnData("telek_terulet", "telekTerulet", "Telek terület (m2)", numRenderer, MegrendelesFieldType.Int),
        MegrendelesColumnData("keszultsegi_fok", "keszultsegiFok", "Készültségi fok (%)", numRenderer, MegrendelesFieldType.Int),
        MegrendelesColumnData("eladasi_ar", "eladasiAr", "Eladási ár", numRenderer, MegrendelesFieldType.Int),
        MegrendelesColumnData("becsult_ertek", "becsultErtek", "Becsült érték", numRenderer, MegrendelesFieldType.Int),
        MegrendelesColumnData("fajlagos_elad_ar", "fajlagosEladAr", "Fajlagos eladási ár", numRenderer, MegrendelesFieldType.Int),
        MegrendelesColumnData("fajlagos_becs_ar", "fajlagosBecsultAr", "Fajlagos becsült érték", numRenderer, MegrendelesFieldType.Int),
        MegrendelesColumnData("statusz", "statusz", "Státusz", statuszRenderer, MegrendelesFieldType.String,
                FilterDef(
                        filterComboValues = { appState, megrendelesek ->
                            megrendelesek
                                    .map { it.statusz.text }
                                    .distinct()
                                    .sorted()
                                    .map { it to it }.toTypedArray()
                        },
                        filter = exactMatchFilter { it.statusz.text }
                )
        ),
        // Excelből jön
        MegrendelesColumnData("helyszinelo", "helyszinelo", "Helyszínelő", stringRenderer, MegrendelesFieldType.String),
        MegrendelesColumnData("ellenorizve", "ellenorizve", "Ellenőrizve", booleanRenderer, MegrendelesFieldType.Boolean),
        MegrendelesColumnData("megrendelve", "megrendelve", "Megrendelés dátuma", dateRenderer, MegrendelesFieldType.Date), // Megrendelés dátuma
        MegrendelesColumnData("hatarido", "hatarido", "Határidő", dateRenderer, MegrendelesFieldType.Date),
        MegrendelesColumnData("feltoltve", "feltoltveMegrendelonek", "Feltöltve megrendelőnek", dateRenderer, MegrendelesFieldType.Date), // Feltöltve megrendelőnek
        MegrendelesColumnData("energetika_feltoltve", "energetikaFeltoltve", "Energetika feltöltve", dateRenderer, MegrendelesFieldType.Date), // ???
        MegrendelesColumnData("rogzitve", "rogzitve", "Rögzítve", dateRenderer, MegrendelesFieldType.Date),
        MegrendelesColumnData("helyszini_szemle", "szemleIdopontja", "Szemle", dateRenderer, MegrendelesFieldType.Date),
        MegrendelesColumnData("adasvetel_datuma", "adasvetelDatuma", "Adásvétel", dateRenderer, MegrendelesFieldType.Date),
        MegrendelesColumnData("zarolva", "zarolva", "Zárolva", dateRenderer, MegrendelesFieldType.Date),
        MegrendelesColumnData("fizetve_datum", "keszpenzesBefizetes", "Készpénzes befizetés", dateRenderer, MegrendelesFieldType.Date), // Iktató: Számla kifizetése, nálunk: Készpénzes befizetés
        MegrendelesColumnData("penz_beerkezett", "penzBeerkezettDatum", "Pénz beérkezett", dateRenderer, MegrendelesFieldType.Date),
        MegrendelesColumnData("megrendeles_megtekint", "megrendelesMegtekint", "Megrendelés átvétel", dateRenderer, MegrendelesFieldType.Date), // Megrendelés átvétel ideje
        MegrendelesColumnData("alvallalkozo_kesz", "ertekbecslesFeltoltve", "Értékbecslés feltöltve", dateRenderer, MegrendelesFieldType.Date), // Vállalkozó feltöltöte a kész anyago
        MegrendelesColumnData("megrendelo", "megrendelo", "Megrendelő", stringRenderer, MegrendelesFieldType.String),
        MegrendelesColumnData("hiteltipus", "hitelTipus", "Hiteltípus", stringRenderer, MegrendelesFieldType.String),
        MegrendelesColumnData("fovallalkozo", "foVallalkozo", "Fővállalkozó", stringRenderer, MegrendelesFieldType.String),
        MegrendelesColumnData("szamla_sorszama", "szamlaSorszama", "Számla sorszáma", stringRenderer, MegrendelesFieldType.String),
        MegrendelesColumnData("ertekbecslo_dija", "ertekbecsloDija", "Értékbecslőd díja", numRenderer, MegrendelesFieldType.Int),

        MegrendelesColumnData("megjegyzes", "megjegyzes", "Megjegyzés", stringRenderer, MegrendelesFieldType.String),
        MegrendelesColumnData("problema", "problema", "Probléma", stringRenderer, MegrendelesFieldType.String),
        MegrendelesColumnData("alvallalkozo_id", "alvallalkozoId", "Alvállalkozó", avRenderer, MegrendelesFieldType.Select,
                FilterDef(
                        filterComboValues = { appState, megrendelesek ->
                            megrendelesek
                                    .map { appState.alvallalkozoState.alvallalkozok[it.alvallalkozoId] }
                                    .filterNotNull()
                                    .distinctBy { it.id }
                                    .sortedBy { it.name }
                                    .map { it.name to it.id.toString() }.toTypedArray()
                        },
                        filter = exactMatchFilter {
                            it.alvallalkozoId.toString()
                        }
                )
        ),
        MegrendelesColumnData("ertekbecslo_id", "ertekbecsloId", "Értékbecslő", ebRenderer, MegrendelesFieldType.Select),
        MegrendelesColumnData("ert_nev", "ertesitesiNev", "Név", stringRenderer, MegrendelesFieldType.String),
        MegrendelesColumnData("ert_tel", "ertesitesiTel", "Telefonszám", stringRenderer, MegrendelesFieldType.String),
        //MegrendelesColumnData("kapcs_nev", "kapcsolattartoNeve", "Név", stringRenderer, MegrendelesFieldType.String),
        //MegrendelesColumnData("kapcs_tel", "kapcsolattartoTel", "Telefonszám", stringRenderer, MegrendelesFieldType.String),
        MegrendelesColumnData("kapcs_email", "kapcsolattartoEmail", "E-mail", stringRenderer, MegrendelesFieldType.String),

        MegrendelesColumnData("hitelosszeg", "hitelOsszeg", "Hitelösszeg", numRenderer, MegrendelesFieldType.Int),
        MegrendelesColumnData("dij", "szamlazhatoDij", "Számlázható díj", numRenderer, MegrendelesFieldType.Int),
        MegrendelesColumnData("ajanlatszam", "ajanlatSzam", "Ajánlatszám", stringRenderer, MegrendelesFieldType.String),
        MegrendelesColumnData("szerzodesszam", "szerzodesSzam", "Szerződésszám", stringRenderer, MegrendelesFieldType.String),
        MegrendelesColumnData("het_kod", "hetKod", "HET", stringRenderer, MegrendelesFieldType.String),
        MegrendelesColumnData("munkatipus", "munkatipus", "Munkatípus", stringRenderer, MegrendelesFieldType.String),

        MegrendelesColumnData("???_cim", "cim", "Cím", { cell: Any, megr: Megrendeles ->
            "${megr.utca} ${megr.utcaJelleg} ${megr.hazszam} ${megr.lepcsohaz} ${megr.ajto}"
        }, MegrendelesFieldType.Select),
        MegrendelesColumnData("???_alvallalkozo_kesz", "alvallalkozo_kesz", "Alvállalkozó kész", { cell: Any, megr: Megrendeles ->
            var kesz = true
            kesz = kesz && (megr.munkatipus.isEnergetika() && megr.energetikaFeltoltve != null)
            kesz = kesz && (megr.munkatipus.isErtekbecsles() && megr.ertekbecslesFeltoltve != null)
            booleanRenderer(kesz, megr)
        }, MegrendelesFieldType.Boolean),

        MegrendelesColumnData("afa", "afa", "ÁFA", numRenderer, MegrendelesFieldType.Int)
)