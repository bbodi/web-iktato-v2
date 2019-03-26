package app.megrendeles

import app.AppState
import app.Dispatcher
import app.common.Granularity
import app.common.Moment
import app.common.latest
import app.common.moment
import app.megrendeles.MegrendelesScreen.haviTeljesitesFilter
import app.megrendeles.MegrendelesScreen.haviTeljesitesFilterButton
import app.megrendeles.MegrendelesScreen.mindFilter
import app.megrendeles.MegrendelesScreen.mindFilterTabPane
import app.useState
import hu.nevermind.antd.*
import hu.nevermind.antd.autocomplete.AutoComplete
import hu.nevermind.antd.table.ColumnAlign
import hu.nevermind.antd.table.ColumnProps
import hu.nevermind.antd.table.SortedInfo
import hu.nevermind.antd.table.Table
import hu.nevermind.iktato.Path
import hu.nevermind.iktato.RestUrl
import hu.nevermind.utils.app.*
import hu.nevermind.utils.hu.nevermind.antd.StringOrReactElement
import hu.nevermind.utils.hu.nevermind.antd.message
import hu.nevermind.utils.jsStyle
import hu.nevermind.utils.store.*
import kotlinext.js.jsObject
import kotlinx.html.DIV
import kotlinx.html.js.onClickFunction
import org.w3c.xhr.BLOB
import org.w3c.xhr.XMLHttpRequest
import org.w3c.xhr.XMLHttpRequestResponseType
import react.*
import react.dom.*
import store.*
import kotlin.browser.document
import kotlin.browser.window

val downloadFile: (url: String, name: String) -> Unit = kotlinext.js.require("downloadjs")

object MegrendelesScreenIds {
    val screenId = "megrendelesScreen"

    val rowEdit = { index: Int -> "${screenId}edit_$index" }
    val rowDelete = { index: Int -> "${screenId}delete_$index" }

    val addButton = "${screenId}_addButton"

    object menu {
        private val prefix = "${screenId}_menu_"

        val megrendelesek = "${prefix}megr"
        val sajatar = "${prefix}sajatAr"
        val alvallalkozok = "${prefix}alv"
        val ertekbecslok = "${prefix}eb"
        val felhasznalok = "${prefix}users"
        val regiok = "${prefix}regiok"
    }

    object modal {
        private val prefix = "${screenId}_modal_"
        val id = "${prefix}root"

        object tab {
            private val prefix = "${modal.prefix}tab_"
            val first = "${prefix}first"
            val feltoltesZarolas = "${prefix}second"
            val ingatlanAdatai = "${prefix}ingatlanAdatai"
            val files = "${prefix}files"
            val akadalyok = "${prefix}akadalyok"
            val penzBeerkezett = "${prefix}penzBeerkezett"
        }

        object akadaly {
            private val prefix = "${modal.prefix}akadaly_input_"
            val keslekedesOka = "${prefix}keslekedesOka"
            val ujHatarido = "${prefix}ujHatarido"
            val szovegesMagyarazat = "${prefix}szovegesMagyarazat"
        }

        object input {
            private val prefix = "${modal.prefix}input_"
            val megrendelo = "${prefix}megrendelo"
            val fovallalkozo = "${prefix}fovallalkozo"

            val megjegyzes = "${prefix}megjegyzes"

            val megrendelesDatuma = "${prefix}megrendelesDatuma"
            val munkatipus = "${prefix}munkatipus"
            val azonosito = "${prefix}azonosito"
            val ebAzonosito = "${prefix}ebAzonosito"
            val etAzonosito = "${prefix}etAzonosito"
            val ugyfelNev = "${prefix}ugyfelNev"
            val ugyfelTel = "${prefix}ugyfelTel"
            val ugyfelEmail = "${prefix}ugyfelEmail"

            val helyrajziSzam = "${prefix}helyrajziSzam"
            val regio = "${prefix}regio"
            val iranyitoszam = "${prefix}iranyitoszam"
            val telepules = "${prefix}telepules"
            val kerulet = "${prefix}kerulet"
            val kozteruletNeve = "${prefix}kozteruletNeve"
            val kozteruletJellege = "${prefix}kozteruletJellege"
            val hazszam = "${prefix}hazszam"
            val lepcsohaz = "${prefix}lepcsohaz"
            val emelet = "${prefix}emelet"
            val ajto = "${prefix}ajto"

            val ertesitesiSzemelyAzonos = "${prefix}ertesitesiSzemelyAzonos"
            val ertesitendoNev = "${prefix}ertesitendoNev"
            val ertesitendoTel = "${prefix}ertesitendoTel"

            val hitelTipus = "${prefix}hitelTipus"
            val hitelOsszege = "${prefix}hitelOsszege"
            val ajanlatSzam = "${prefix}ajanlatSzam"
            val szerzodesSzam = "${prefix}szerzodesSzam"
            val hetKod = "${prefix}hetKod"

            val statusz = "${prefix}statusz"
            val hatarido = "${prefix}hatarido"
            val megrendelesAtvetelIdeje = "${prefix}megrendelesAtvetelIdeje"
            val vallalkozoFeltoltotteAKeszAnyagot = "${prefix}vallalkozoFeltoltotteAKeszAnyagot"
            val energetikaFeltoltese = "${prefix}energetikaFeltoltese"
            val szemleIdopontja = "${prefix}szemleIdopontja"
            val szemleIdopontjaCheckbox = "${prefix}szemleIdopontjaCheckbox"
            val keszpenzesKifizetes = "${prefix}keszpenzesKifizetes"
            val feltoltveMegrendelonek = "${prefix}feltoltveMegrendelonek"
            val zarolva = "${prefix}zarolva"
            val szamlaKifizetese = "${prefix}szamlaKifizetese"
            val penzBeerkezettSzamlara = "${prefix}penzBeerkezettSzamlara"
            val adasvetelDatumaCheckbox = "${prefix}adasvetelDatumaCheckbox"
            val adasvetelDatuma = "${prefix}adasvetelDatuma"

            val alvallalkozo = "${prefix}alvallalkozo"
            val ertekbecslo = "${prefix}ertekbecslo"
            val ertekbecsloDija = "${prefix}ertekbecsloDija"
            val helyszinelo = "${prefix}helyszinelo"
            val ingatlanTipusMunkadijMeghatarozasahoz = "${prefix}ingatlanTipusMunkadijMeghatarozasahoz"
            val ingatlanBovebbTipus = "${prefix}ingatlanBovebbTipus"
            val lakasTerulet = "${prefix}lakasTerulet"
            val telekTerulet = "${prefix}telekTerulet"
            val ingatlanKeszultsegiFoka = "${prefix}ingatlanKeszultsegiFoka"
            val eladasiAr = "${prefix}eladasiAr"
            val becsultErtek = "${prefix}becsultErtek"
            val fajlagosEladasiAr = "${prefix}fajlagosEladasiAr"
            val fajlagosBecsultAr = "${prefix}fajlagosBecsultAr"

            val szamlaSorszama = "${prefix}szamlaSorszama"
            val szamlazhatoDij = "${prefix}szamlazhatoDij"
            val importTextArea = "${prefix}importTextArea"
        }

        object button {
            private val prefix = "${modal.prefix}button_"
            val save = "${prefix}save"
            val akadalyFeltoltes = "${prefix}akadalyFeltoltes"
            val close = "${prefix}close"
            val import = "${prefix}import"
        }
    }
}


interface MegrendelesFilter {
    val id: String
    fun label(state: MegrendelesTableFilterState, appState: AppState): String
    fun predicate(state: MegrendelesTableFilterState, megr: Megrendeles): Boolean
}

open class SimpleMegrendelesFilter(
        override val id: String,
        val label: String,
        val icon: String? = null,
        private val predicate: Megrendeles.() -> Boolean) : MegrendelesFilter {

    override fun label(state: MegrendelesTableFilterState, appState: AppState): String {
        return label
    }

    override fun predicate(state: MegrendelesTableFilterState, megr: Megrendeles): Boolean {
        return megr.predicate()
    }
}

val atNemVettFilter = SimpleMegrendelesFilter("filter_atnemvett", "Át nem vett") {
    atNemVettPredicate()
}

private fun Megrendeles.atNemVettPredicate(): Boolean {
    return (megrendelesMegtekint == null)
            .and(!alvallalkozoFeltoltotteFajlokat)
            .and(penzBeerkezettDatum == null && keszpenzesBefizetes == null)
            .and(feltoltveMegrendelonek == null)
            .and(zarolva == null)
}

private val atvettFilterForAlv = SimpleMegrendelesFilter("filter_atvett_alv", "Átvett") {
    (megrendelesMegtekint != null)
            .and(!alvallalkozoFeltoltotteFajlokat)
            .and(penzBeerkezettDatum == null && keszpenzesBefizetes == null)
            .and(feltoltveMegrendelonek == null)
            .and(zarolva == null)
            .and(!this.isAkadalyos())
            .or( // vagy egyik filter sem teljesül
                    !this.atNemVettPredicate() &&
                            !this.isAkadalyos() &&
                            !this.utalasHianyzikPredicate() &&
                            !this.alvallalkozoVegzettVele()
            )
}

private val atvettFilter = SimpleMegrendelesFilter("filter_atvett", "Átvett") {
    (megrendelesMegtekint != null)
            .and(!alvallalkozoFeltoltotteFajlokat)
            .and(penzBeerkezettDatum == null && keszpenzesBefizetes == null)
            .and(feltoltveMegrendelonek == null)
            .and(zarolva == null)
            .and(!this.isAkadalyos())
            .or( // vagy egyik filter sem teljesül
                    !this.atNemVettPredicate() &&
                            !this.isAkadalyos() &&
                            !this.utalasHianyzikPredicate() &&
                            !this.ellenorzesreVarPredicate() &&
                            !this.archivlasraVarPredicate()
            )
}

fun feladatElvegezveEbbenAHonapban(ertekbecslesFeltoltve: Moment?, energetikaFeltoltve: Moment?): Boolean {
    val latestUplaodDate = arrayOf(ertekbecslesFeltoltve, energetikaFeltoltve)
            .filterNotNull()
            .maxBy { it.toDate().getTime() }
    return latestUplaodDate != null && moment().isSame(latestUplaodDate, Granularity.Month)
}

val alvallalkozoVegzettVeleAdottHonapban = SimpleMegrendelesFilter("filter_alvallalkozo_vegzett", "Elvégezve", "check") {
    alvallalkozoVegzettVele()
}

private fun Megrendeles.alvallalkozoVegzettVele(): Boolean {
    return (megrendelesMegtekint != null)
            .and(alvallalkozoFeltoltotteFajlokat)
            .and(penzBeerkezettDatum != null || keszpenzesBefizetes != null)
            .and(feladatElvegezveEbbenAHonapban(ertekbecslesFeltoltve, energetikaFeltoltve))
}

private val hataridosFilterForAdmin = SimpleMegrendelesFilter("filter_hataridos", "Határidős", "hourglass", {
    isHataridos()
})

private fun Megrendeles.isHataridos(): Boolean {
    return (hatarido?.isBefore(moment()) ?: true)
            .and(feltoltveMegrendelonek == null)
            .and(zarolva == null)
}

private val hataridosFilterForAlv = SimpleMegrendelesFilter("filter_hataridos_alv", "Határidős", "hourglass") {
    this.alvallalkozoFeltoltotteFajlokat.not() && this.isHataridos()
}

fun Megrendeles.isAkadalyos(): Boolean {
    val statusIsAkadalyos = statusz in akadalyosStatuszok
    val latestStatusChangerDate = latest(ertekbecslesFeltoltve, energetikaFeltoltve, penzBeerkezettDatum, keszpenzesBefizetes, feltoltveMegrendelonek)
    return if (statusIsAkadalyos && latestStatusChangerDate != null) {
        val lastAkadalyDate = akadalyok.maxBy { it.rogzitve.toDate().getTime() }
        lastAkadalyDate != null && lastAkadalyDate.rogzitve.isAfter(latestStatusChangerDate)
    } else {
        statusIsAkadalyos
    }
}

private val akadalyosFilterForAdmin = SimpleMegrendelesFilter("filter_akadalyos", "Akadályos", "stop") {
    this.isAkadalyos()
}
private val akadalyosFilterForAlvallalkozo = SimpleMegrendelesFilter("filter_akadalyos_alv", "Akadályos", "stop") {
    this.alvallalkozoFeltoltotteFajlokat.not() && this.isAkadalyos()
}
private val utalasHianyzikFilter = SimpleMegrendelesFilter("filter_utalas_hianyzik", "Utalás hiányzik", "dollar") {
    utalasHianyzikPredicate()
}

private fun Megrendeles.utalasHianyzikPredicate(): Boolean {
    return (megrendelesMegtekint != null)
            .and(alvallalkozoFeltoltotteFajlokat)
            .and(penzBeerkezettDatum == null && keszpenzesBefizetes == null)
            .and(feltoltveMegrendelonek == null)
            .and(zarolva == null)
            .and(!this.isAkadalyos())
}

private val ellenorzesreVarFilter = SimpleMegrendelesFilter("ellenorzesre_var", "Ellenőrzésre vár", "check-circle") {
    ellenorzesreVarPredicate()
}

private fun Megrendeles.ellenorzesreVarPredicate(): Boolean {
    return (megrendelesMegtekint != null)
            .and(alvallalkozoFeltoltotteFajlokat)
            .and(penzBeerkezettDatum != null || keszpenzesBefizetes != null)
            .and(feltoltveMegrendelonek == null)
            .and(!this.isAkadalyos())
            .and(zarolva == null)
}

private fun Megrendeles.archivlasraVarPredicate(): Boolean {
    return (megrendelesMegtekint != null)
            .and(alvallalkozoFeltoltotteFajlokat)
            .and(szamlaSorszama.isNotEmpty())
            .and(penzBeerkezettDatum != null || keszpenzesBefizetes != null)
            .and(feltoltveMegrendelonek != null)
            .and(!this.isAkadalyos())
            .and(zarolva == null)
}

private val archivalasraVarFilter = SimpleMegrendelesFilter("filter_archivalasra_var", "Archiválásra vár",
        "file-done",
        { archivlasraVarPredicate() }
)


data class HaviTeljesites(val alvallalkozoId: Int, val date: Moment)

data class SzuroMezo(val columnData: MegrendelesColumnData, val operator: String, val operand: String) {
    fun toSqlWhereClause(): String {
        val keyEquality = columnData.renderer == ebRenderer || columnData.renderer == avRenderer
        return if (columnData.megrendelesFieldType == MegrendelesFieldType.Int || keyEquality) {
            if (operator == "kisebb") {
                "${columnData.dbName} < $operand"
            } else if (operator == "nagyobb") {
                "${columnData.dbName} > $operand"
            } else if (operator == "kisebb egyenlő") {
                "${columnData.dbName} <= $operand"
            } else if (operator == "nagyobb egyenlő") {
                "${columnData.dbName} >= $operand"
            } else if (operator == "egyenlő") {
                "${columnData.dbName} = $operand"
            } else {
                "${columnData.dbName} $operator $operand"
            }
        } else if (columnData.megrendelesFieldType == MegrendelesFieldType.Date) {
            if (operator == "NULL") {
                "(${columnData.dbName} IS NULL OR ${columnData.dbName} = '0000-00-00 00:00')"
            } else {
                val notNull = "(${columnData.dbName} IS NOT NULL AND ${columnData.dbName} <> '0000-00-00 00:00')"
                if (operator == "aznap") {
                    "$notNull AND ${columnData.dbName} BETWEEN '$operand 00:00:00' AND '$operand 23:59:59'"
                } else if (operator == "később") {
                    "$notNull AND ${columnData.dbName} > '$operand'"
                } else if (operator == "korábban") {
                    "$notNull AND ${columnData.dbName} < '$operand'"
                } else if (operator == "aznap vagy korábban") {
                    "$notNull AND (${columnData.dbName} BETWEEN '$operand 00:00:00' AND '$operand 23:59:59') OR (${columnData.dbName} <= '$operand')"
                } else if (operator == "aznap vagy később") {
                    "$notNull AND (${columnData.dbName} BETWEEN '$operand 00:00:00' AND '$operand 23:59:59') OR (${columnData.dbName} >= '$operand')"
                } else {
                    "$notNull AND ${columnData.dbName} $operator '$operand'"
                }
            }
        } else {
            val columnName = columnData.dbName
            if (operand.isEmpty()) {
                "$columnName = ''"
            } else when (operator) {
                "Nem tartamazza" -> "$columnName not like '%$operand%'"
                "Kezdődik" -> "$columnName like '$operand%'"
                "Végződik" -> "$columnName like '%$operand'"
                else -> "$columnName like '%$operand%'" // "Tartalmazza"
            }
        }
    }
}

data class MegrendelesTableFilterState(val activeFilter: MegrendelesFilter,
                                       val mindFilteredMegrendelesIds: List<Int>,
                                       val szuroMezok: List<SzuroMezo>,
                                       val haviTeljesites: HaviTeljesites?)

data class MegrendelesScreenState(
        val showMindFilterModal: Boolean,
        val haviTeljesitesModalOpen: Boolean,
        val sort: SortedInfo
)

data class MegrendelesScreenParams(val appState: AppState,
                                   val showAlvallalkozoSajatAdataiModal: Boolean,
                                   val megrendelesModalId: Int?,
                                   val globalDispatch: (Action) -> Unit)

object MegrendelesScreenComponent : DefinedReactComponent<MegrendelesScreenParams>() {
    override fun RBuilder.body(props: MegrendelesScreenParams) {
        val appState: AppState = props.appState
        val globalDispatch: (Action) -> Unit = props.globalDispatch
        val user = props.appState.maybeLoggedInUser!!
        val (state, setState) = useState(MegrendelesScreenState(
                haviTeljesitesModalOpen = false,
                showMindFilterModal = false,
                sort = SortedInfo("descend", "megrendelve"))
        )
        div {
            val filterState = appState.megrendelesTableFilterState
            Row {
                Tabs {
                    attrs.activeKey = filterState.activeFilter.id
                    attrs.animated = true
                    if (user.isAdmin) {
                        attrs.tabBarExtraContent = StringOrReactElement.from {
                            addNewButton(globalDispatch)
                        }
                    }
                    attrs.size = TabsSize.small
                    attrs.onChange = onFilterChange(globalDispatch, filterState)
                    val table = buildElement {
                        megrendelesekTable(user, appState, globalDispatch, state, setState)
                    }!!
                    simpleFilterTabPanes(
                            filterState,
                            appState,
                            getSimpleFilters(user),
                            table
                    )
                    if (user.isAdmin) {
                        mindFilterTabPane(filterState, setState, globalDispatch, state, table)
                    }
                    haviTeljesitesTabPane(filterState, state, appState, setState, table)
                }
            }
            haviTeljesitesDownloadButton(filterState, user, appState)
            HaviTeljesitesModalComponent.insert(this, HaviTeljesitesModalParams(user,
                    state,
                    filterState,
                    appState.alvallalkozoState.alvallalkozok,
                    setState,
                    globalDispatch
            ))
            if (user.isAdmin && state.showMindFilterModal) {
                customSearchModalComponent(appState, globalDispatch, state, setState)
            } else if (user.isAlvallalkozo){
                AlvallalkozoSajatAdataiModalComponent.insert(this, AlvallalkozoSajatAdataiModalParams(
                        alvallalkozo = appState.alvallalkozoState.alvallalkozok[user.alvallalkozoId]!!,
                        visible = props.showAlvallalkozoSajatAdataiModal,
                        onClose = {
                            globalDispatch(Action.ChangeURL(Path.root))
                        }
                ))
                val openMegr = appState.megrendelesState.megrendelesek[props.megrendelesModalId]
                if (openMegr != null) {
                    AlvallalkozoMegrendelesFormModalComponent.insert(this, AlvallalkozoMegrendelesFormModalParams(
                            megrendeles = openMegr,
                            globalDispatch = globalDispatch,
                            visible = true
                    ))
                }
            }
        }
    }

    private fun onFilterChange(globalDispatch: (Action) -> Unit,
                               filterState: MegrendelesTableFilterState): (String) -> Unit {
        return { key ->
            when (key) {
                atNemVettFilter.id,
                atvettFilter.id,
                atvettFilterForAlv.id,
                hataridosFilterForAdmin.id,
                akadalyosFilterForAdmin.id,
                utalasHianyzikFilter.id,
                ellenorzesreVarFilter.id,
                archivalasraVarFilter.id,
                hataridosFilterForAlv.id,
                akadalyosFilterForAlvallalkozo.id,
                alvallalkozoVegzettVeleAdottHonapban.id -> {
                    globalDispatch(Action.SetActiveFilter(SetActiveFilterPayload.SimpleFilter(filterKeyMap[key]!!)))
                }
                haviTeljesitesFilter.id -> {
                    if (filterState.haviTeljesites != null) {
                        globalDispatch(Action.SetActiveFilter(
                                SetActiveFilterPayload.HaviTeljesites(
                                        filterState.haviTeljesites.alvallalkozoId,
                                        filterState.haviTeljesites.date
                                )
                        ))
                    }
                }
                mindFilter.id -> {
                    globalDispatch(Action.SetActiveFilter(
                            SetActiveFilterPayload.MindFilter(filterState.mindFilteredMegrendelesIds, filterState.szuroMezok)
                    ))
                }
            }
        }
    }

    private fun RBuilder.addNewButton(globalDispatch: (Action) -> Unit) {
        Button {
            attrs.asDynamic().id = MegrendelesScreenIds.addButton
            attrs.type = ButtonType.primary
            attrs.onClick = {
                globalDispatch(Action.ChangeURL(Path.megrendeles.edit(0)))
            }
            Icon("plus-circle")
            +" Hozzáadás"
        }
    }

    private fun RDOMBuilder<DIV>.haviTeljesitesDownloadButton(filterState: MegrendelesTableFilterState, user: LoggedInUser, appState: AppState) {
        if (haviTeljesitesFilter == filterState.activeFilter &&
                filterState.haviTeljesites != null &&
                user.isAdmin) {
            Row {
                val avId = filterState.haviTeljesites.alvallalkozoId
                val dateStr = filterState.haviTeljesites.date.format(monthFormat)
                Button {
                    attrs.onClick = {
                        val x = XMLHttpRequest()
                        x.open("GET", "/haviTeljesites/$avId/$dateStr", true)
                        x.responseType = XMLHttpRequestResponseType.BLOB
                        x.onload = { e ->
                            val avName = appState.alvallalkozoState.alvallalkozok[avId]?.name ?: "ismeretlen"
                            downloadFile(e.target.asDynamic().response,
                                    "$avName ${dateStr}.xlsx")
                        }
                        x.send()

                    }
                    Icon("download")
                    +" Havi teljesítés letöltése"
                }

            }
        }
    }

    private fun RElementBuilder<TabsProps>.haviTeljesitesTabPane(filterState: MegrendelesTableFilterState,
                                                                 state: MegrendelesScreenState,
                                                                 appState: AppState,
                                                                 setState: Dispatcher<MegrendelesScreenState>,
                                                                 table: ReactElement) {
        TabPane {
            attrs.key = haviTeljesitesFilter.id
            attrs.disabled = filterState.haviTeljesites == null
            attrs.tab = StringOrReactElement.from {
                haviTeljesitesFilterButton(
                        appState.maybeLoggedInUser!!,
                        filterState,
                        state,
                        appState.megrendelesState.megrendelesek,
                        appState.alvallalkozoState.alvallalkozok,
                        setState
                )
            }
            if (filterState.activeFilter == haviTeljesitesFilter) {
                child(table)
            }
        }
    }
}

private fun RBuilder.megrendelesekTable(user: LoggedInUser,
                                        appState: AppState,
                                        globalDispatch: (Action) -> Unit,
                                        state: MegrendelesScreenState,
                                        setState: Dispatcher<MegrendelesScreenState>) {

    val filteredMegrendelesek = appState.megrendelesState.megrendelesek.values.filter {
        appState.megrendelesTableFilterState.activeFilter.predicate(appState.megrendelesTableFilterState, it)
    }.toTypedArray()
    val onClick = { megr: Megrendeles ->
        if (user.isAlvallalkozo && megr.atNemVett) {
            communicator.getEntityFromServer<dynamic, Any>(RestUrl.megrendelesAtvetele, object {
                val megrendelesId = megr.id
            }) { response ->
                globalDispatch(Action.MegrendelesekFromServer(arrayOf(response)))
                globalDispatch(Action.ChangeURL(Path.megrendeles.edit(megr.id)))
                message.success("A Megrendelés átvétele sikeres")
            }
        } else {
            communicator.asyncPost<MegrendelesFromServer>(RestUrl.megrendelesMegnyitasa, object {
                val megrendelesId = megr.id
            }) { result ->
                result.ifOk { response ->
                    globalDispatch(Action.MegrendelesekFromServer(arrayOf(response)))
                    globalDispatch(Action.ChangeURL(Path.megrendeles.edit(megr.id)))
                }
            }
        }
    }
    val columns = user.megrendelesTableColumns.map { columnDef ->
        ColumnProps {
            title = columnDef.columnTitle
            dataIndex = columnDef.fieldName
            filters = columnDef.filter?.filterComboValues?.invoke(appState, filteredMegrendelesek)?.map {
                object {
                    val text = it.first;
                    val value = it.second
                }
            }?.toTypedArray()
            if (columnDef.renderer == dateRenderer) {
                sorter = { a, b ->
                    val f1: Moment? = a.asDynamic()[columnDef.fieldName]
                    val f2: Moment? = b.asDynamic()[columnDef.fieldName]
                    if (f1 == null) {
                        1
                    } else if (f2 == null) {
                        -1
                    } else {
                        if (f1.isBefore(f2)) -1 else 1
                    }
                }
            }
            sortOrder = if (state.sort.columnKey == columnDef.fieldName) state.sort.order
                    ?: "descend" else false.asDynamic()
            onFilter = columnDef.filter?.filter.asDynamic()
            this.asDynamic().render = { cell: Any?, row: Any, index: Int -> columnDef.renderer.asDynamic()(cell, row, index, appState) }
        }
    }.let { columns ->
        columns + ColumnProps {
            title = "Szerk"; key = "action"; width = 100; align = ColumnAlign.center
            render = { megr: Megrendeles, _, rowIndex ->
                buildElement {
                    div {
                        Tooltip("Szerkesztés") {
                            Button {
                                attrs.asDynamic().id = MegrendelesScreenIds.rowEdit(rowIndex)
                                attrs.icon = "edit"
                                attrs.onClick = {
                                    onClick(megr)
                                }
                            }
                        }
                        Divider(type = DividerType.vertical)
                        Tooltip("Törlés") {
                            Button {
                                attrs.asDynamic().id = MegrendelesScreenIds.rowDelete(rowIndex)
                                attrs.icon = "delete"
                                attrs.type = ButtonType.danger
                                attrs.onClick = {
                                    Modal.confim {
                                        title = "Biztos törli a '${megr.azonosito}' Megrendelést?"
                                        okText = "Igen"
                                        cancelText = "Mégsem"
                                        okType = ButtonType.danger
                                        onOk = {
                                            globalDispatch(Action.DeleteMegrendeles(megr))
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
    }.toTypedArray()
    Table {
        attrs.columns = columns
        attrs.dataSource = filteredMegrendelesek
        attrs.rowKey = "id"
        attrs.bordered = true
        attrs.asDynamic().pagination = jsObject<dynamic> {
            defaultPageSize = 1000
        }
        attrs.asDynamic().style = jsStyle {
            minHeight = "400px"
        }
        attrs.asDynamic().size = "middle"
        attrs.onChange = { pagination, filters, sorter ->
            setState(state.copy(
                    sort = sorter
            ))
        }
    }
}

private fun RBuilder.customSearchModalComponent(
        appState: AppState,
        globalDispatch: (Action) -> Unit,
        state: MegrendelesScreenState,
        setState: Dispatcher<MegrendelesScreenState>) {
    CustomSearchModalComponent.insert(this, CustomSearchModalParams(
            appState = appState,
            szuroMezok = appState.megrendelesTableFilterState.szuroMezok,
            onClose = { ok, szuroMezok ->
                setState(state.copy(
                        showMindFilterModal = false
                ))
                if (ok) {
                    applyMindFilter(szuroMezok, globalDispatch)
                    globalDispatch(Action.ChangeURL(Path.megrendeles.root))
                }
            }
    ))
}

private fun applyMindFilter(szuroMezok: List<SzuroMezo>, globalDispatch: (Action) -> Unit) {
    val sqlWhereClause = szuroMezok.map { szuroMezo ->
        szuroMezo.toSqlWhereClause()
    }.joinToString(") AND (", "(", ")")
    communicator.getEntitiesFromServer(RestUrl.megrendelesFilter, object {
        val text = sqlWhereClause
    }) { response: Array<MegrendelesFromServer> ->
        val ids = response.map { it.id }
        globalDispatch(Action.SetActiveFilter(
                SetActiveFilterPayload.MindFilter(ids, szuroMezok)
        ))
        globalDispatch(Action.MegrendelesekFromServer(response))
    }
}

val filterKeyMap = mapOf(
        atNemVettFilter.id to atNemVettFilter,
        atvettFilter.id to atvettFilter,
        atvettFilterForAlv.id to atvettFilterForAlv,
        hataridosFilterForAdmin.id to hataridosFilterForAdmin,
        hataridosFilterForAlv.id to hataridosFilterForAlv,
        akadalyosFilterForAdmin.id to akadalyosFilterForAdmin,
        akadalyosFilterForAlvallalkozo.id to akadalyosFilterForAlvallalkozo,
        ellenorzesreVarFilter.id to ellenorzesreVarFilter,
        utalasHianyzikFilter.id to utalasHianyzikFilter,
        archivalasraVarFilter.id to archivalasraVarFilter,
        haviTeljesitesFilter.id to haviTeljesitesFilter,
        alvallalkozoVegzettVeleAdottHonapban.id to alvallalkozoVegzettVeleAdottHonapban,
        mindFilter.id to mindFilter
)

object MegrendelesScreen {

    val haviTeljesitesFilter = object : MegrendelesFilter {
        override val id = "filter_havi_teljesites"
        override fun label(state: MegrendelesTableFilterState, appState: AppState): String {
            return ""
        }

        override fun predicate(state: MegrendelesTableFilterState,
                               megr: Megrendeles): Boolean {
            val props = state.haviTeljesites
            return if (props == null) false else
                megr.alvallalkozoId == props.alvallalkozoId && megr.feltoltveMegrendelonek?.isSame(props.date, Granularity.Month) ?: false
        }
    }

    val mindFilter = object : MegrendelesFilter {
        override val id = "filter_custom"
        override fun label(state: MegrendelesTableFilterState, appState: AppState): String = "Szűrés"

        override fun predicate(state: MegrendelesTableFilterState, megr: Megrendeles): Boolean = megr.id in state.mindFilteredMegrendelesIds
    }

    fun RBuilder.haviTeljesitesFilterButton(loggedInUser: LoggedInUser,
                                            filterState: MegrendelesTableFilterState,
                                            state: MegrendelesScreenState,
                                            megrendelesek: Map<Int, Megrendeles>,
                                            alvallalkozok: Map<Int, Alvallalkozo>,
                                            screenDispatch: Dispatcher<MegrendelesScreenState>) {
        val filteredMegrendelesek = megrendelesek.values.filter { haviTeljesitesFilter.predicate(filterState, it) }
        Popover {
            attrs.title = StringOrReactElement.fromString("Havi teljesítés")
            attrs.content = StringOrReactElement.from {
                a(href = null) {
                    attrs.onClickFunction = {
                        screenDispatch(state.copy(haviTeljesitesModalOpen = true))
                    }
                    +(if (loggedInUser.isAdmin) {
                        "Alvállalkozó és hónap kiválasztása"
                    } else {
                        "Hónap kiválasztása"
                    })
                }
            }
            Badge(filteredMegrendelesek.count()) {
                attrs.showZero = filterState.activeFilter == haviTeljesitesFilter
                attrs.asDynamic().style = jsStyle {
                    backgroundColor = "#1890ff"
                }
                span {
                    attrs.jsStyle {
                        marginRight = "15px"
                    }
                    val props = filterState.haviTeljesites
                    val label = if (props == null) "Havi teljesítés" else {
                        val alvName = alvallalkozok[props.alvallalkozoId]?.name ?: "ismeretlen"
                        "${alvName}: ${props.date.format(monthFormat)}"
                    }
                    +" $label "
                }
            }
        }
    }

    fun RBuilder.mindFilterTabPane(filterState: MegrendelesTableFilterState,
                                   setState: Dispatcher<MegrendelesScreenState>,
                                   globalDispatch: (Action) -> Unit,
                                   state: MegrendelesScreenState,
                                   tabPaneContent: ReactElement) {
        TabPane {
            attrs.key = mindFilter.id
            attrs.disabled = filterState.szuroMezok.isEmpty()
            attrs.tab = StringOrReactElement.from {
                Popover {
                    attrs.title = StringOrReactElement.fromString("Szűrés")
                    attrs.mouseEnterDelay = 0
                    attrs.onVisibleChange = { visible ->
                        window.setTimeout({
                            document.getElementById("quickAzonositoFilterField")?.asDynamic().focus()
                        }, 300)
                    }
                    attrs.content = StringOrReactElement.from {
                        div {
                            a(href = null) {
                                attrs.onClickFunction = {
                                    setState(state.copy(
                                            showMindFilterModal = true
                                    ))
                                }
                                +"Szűrőmezők beállítása"
                            }
                            FormItem {
                                attrs.required
                                attrs.label = StringOrReactElement.fromString("Azonosító")
                                Input {
                                    attrs.asDynamic().id = "quickAzonositoFilterField"
                                    val maybeAzonositoFilter = filterState.szuroMezok.firstOrNull()
                                    attrs.value = if (maybeAzonositoFilter?.columnData?.fieldName == "azonosito") {
                                        maybeAzonositoFilter.operand
                                    } else ""
                                    attrs.onChange = { e ->
                                        val value: String = e.currentTarget.asDynamic().value
                                        if (value.length > 3) {
                                            val szuroMezok = if (maybeAzonositoFilter?.columnData?.fieldName == "azonosito") {
                                                listOf(maybeAzonositoFilter.copy(
                                                        operand = value
                                                )) + filterState.szuroMezok.drop(1)
                                            } else {
                                                listOf(SzuroMezo(columnDefinitions["azonosito"]!!, "Tartalmazza", value)) +
                                                        filterState.szuroMezok
                                            }
                                            applyMindFilter(szuroMezok, globalDispatch)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Icon("search")
                    Badge(filterState.mindFilteredMegrendelesIds.size) {
                        attrs.asDynamic().style = jsStyle {
                            backgroundColor = "#1890ff"
                        }
                        span {
                            attrs.jsStyle {
                                marginRight = "15px"
                            }
                            +" Szűrés"
                        }
                    }
                }
            }
            if (filterState.activeFilter == mindFilter) {
                child(tabPaneContent)
            }
        }
    }
}

private fun getSimpleFilters(loggedInUser: LoggedInUser): Array<SimpleMegrendelesFilter> {
    return when (loggedInUser.role) {
        Role.ROLE_ADMIN -> arrayOf(
                atNemVettFilter,
                atvettFilter,
                hataridosFilterForAdmin,
                akadalyosFilterForAdmin,
                utalasHianyzikFilter,
                ellenorzesreVarFilter,
                archivalasraVarFilter
        )
        Role.ROLE_USER -> arrayOf(
                atNemVettFilter,
                atvettFilterForAlv,
                hataridosFilterForAlv,
                akadalyosFilterForAlvallalkozo,
                utalasHianyzikFilter,
                alvallalkozoVegzettVeleAdottHonapban
        )
        Role.ROLE_ELLENORZO -> emptyArray()
    }
}

private fun RBuilder.simpleFilterTabPanes(
        filterState: MegrendelesTableFilterState,
        appState: AppState,
        simpleFilters: Array<SimpleMegrendelesFilter>,
        tabPaneContent: ReactElement) {
    simpleFilters.forEachIndexed { index, filter ->
        val megrendelesek = appState.megrendelesState.megrendelesek.values.filter { filter.predicate(filterState, it) }
        val count = megrendelesek.count()
        TabPane {
            attrs.key = filter.id
            attrs.tab = StringOrReactElement.from {
                span {
                    if (filter.icon != null) {
                        Icon(filter.icon)
                    }
                    Badge(count) {
                        attrs.showZero = true
                        attrs.asDynamic().style = jsStyle {
                            backgroundColor = when (filter.label) {
                                "Határidős" -> "#f5222d"
                                "Akadályos" -> "#faad14"
                                else -> "#1890ff"
                            }
                        }
                        span {
                            attrs.jsStyle {
                                marginRight = "15px"
                            }
                            +((if (filter.icon == null) "" else " ") + filter.label)
                        }
                    }

                }
            }
            if (filterState.activeFilter == filter) {
                child(tabPaneContent)
            }
        }
    }
}

data class HaviTeljesitesModalParams(val loggedInUser: LoggedInUser,
                                     val state: MegrendelesScreenState,
                                     val filterState: MegrendelesTableFilterState,
                                     val osszesAlvallalkozo: Map<Int, Alvallalkozo>,
                                     val screenDispatch: Dispatcher<MegrendelesScreenState>,
                                     val globalDispatch: (Action) -> Unit)


object HaviTeljesitesModalComponent : DefinedReactComponent<HaviTeljesitesModalParams>() {
    override fun RBuilder.body(props: HaviTeljesitesModalParams) {
        val haviTeljesites = props.filterState.haviTeljesites ?: HaviTeljesites(0, moment())
        val (modalState, modalDispatch) = useState(haviTeljesites)
        Modal {
            attrs.title = StringOrReactElement.fromString("Havi teljesítés")
            attrs.visible = props.state.haviTeljesitesModalOpen
            attrs.okButtonProps = jsObject {
                disabled = modalState.alvallalkozoId == 0
            }
            attrs.onOk = {
                props.screenDispatch(props.state.copy(
                        haviTeljesitesModalOpen = false
                ))
                props.globalDispatch(Action.SetActiveFilter(SetActiveFilterPayload.HaviTeljesites(
                        alvallalkozoId = if (props.loggedInUser.isAdmin) modalState.alvallalkozoId else props.loggedInUser.alvallalkozoId,
                        date = modalState.date
                )))
            }
            attrs.onCancel = {
                props.screenDispatch(props.state.copy(haviTeljesitesModalOpen = false))
            }
            Form {
                FormItem {
                    attrs.labelCol = ColProperties { span = 5 }
                    attrs.wrapperCol = ColProperties { span = 14 }
                    attrs.label = StringOrReactElement.fromString("Alvállalkozó")

                    if (props.loggedInUser.isAdmin) {
                        val alvallalkozok = props.osszesAlvallalkozo.getEnabledAlvallalkozok()
                        AutoComplete(alvallalkozok.map { it.name }.sortedBy { it }.toTypedArray()) {
                            attrs.placeholder = "Alvállalkozó"
                            attrs.filterOption = { inputString, optionElement ->
                                (optionElement.props.children as String).toUpperCase().replace(" ", "").contains(inputString.toUpperCase().replace(" ", ""))
                            }
                            attrs.onSelect = { selectedName ->
                                modalDispatch(modalState.copy(alvallalkozoId = alvallalkozok.first { it.name == selectedName }.id))
                            }
                        }
                    } else {
                        Input {
                            attrs.value = props.osszesAlvallalkozo[props.loggedInUser.alvallalkozoId]?.name ?: ""
                            attrs.disabled = true
                        }
                    }
                }
                FormItem {
                    attrs.labelCol = ColProperties { span = 5 }
                    attrs.wrapperCol = ColProperties { span = 14 }
                    attrs.label = StringOrReactElement.fromString("Hónap")
                    MonthPicker {
                        attrs.allowClear = false
                        attrs.value = modalState.date
                        attrs.disabledDate = { date -> date.isAfter(moment(), Granularity.Month) }
                        attrs.placeholder = "Válassz dátumot"
                        attrs.onChange = { date, dateStr ->
                            if (date != null) {
                                modalDispatch(modalState.copy(date = date))
                            }
                        }
                    }
                }
            }
        }
    }
}