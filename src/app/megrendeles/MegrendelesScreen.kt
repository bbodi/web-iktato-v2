package app.megrendeles

import app.AppState
import app.Dispatcher
import app.common.Granularity
import app.common.Moment
import app.common.latest
import app.common.moment
import app.megrendeles.MegrendelesScreen.haviTeljesitesFilter
import app.megrendeles.MegrendelesScreen.haviTeljesitesFilterButton
import app.megrendeles.MegrendelesScreen.simpleFilterButtons
import app.useState
import hu.nevermind.antd.*
import hu.nevermind.antd.autocomplete.AutoComplete
import hu.nevermind.antd.table.ColumnProps
import hu.nevermind.antd.table.Table
import hu.nevermind.iktato.Path
import hu.nevermind.iktato.RestUrl
import hu.nevermind.utils.app.customSearchModal
import hu.nevermind.utils.hu.nevermind.antd.StringOrReactElement
import hu.nevermind.utils.hu.nevermind.antd.message
import hu.nevermind.utils.jsStyle
import hu.nevermind.utils.store.*
import kotlinext.js.jsObject
import org.w3c.xhr.BLOB
import org.w3c.xhr.XMLHttpRequest
import org.w3c.xhr.XMLHttpRequestResponseType
import react.*
import react.dom.div
import react.dom.jsStyle
import react.dom.span
import store.*

private val downloadFile: (url: String, name: String) -> Unit = kotlinext.js.require("downloadjs")

object MegrendelesScreenIds {
    val screenId = "megrendelesScreen"

    val addButton = "${screenId}_addButton"

    object modal {
        private val prefix = "${screenId}_modal_"
        val id = "${prefix}root"

        object tab {
            private val prefix = "${modal.prefix}tab_"
            val first = "${prefix}first"
            val feltoltesZarolas = "${prefix}second"
            val ingatlanAdatai = "${prefix}ingatlanAdatai"
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
            val szemleIdopontja = "${prefix}helysziniSzemle"
            val keszpenzesKifizetes = "${prefix}keszpenzesKifizetes"
            val feltoltveMegrendelonek = "${prefix}feltoltveMegrendelonek"
            val zarolva = "${prefix}zarolva"
            val szamlaKifizetese = "${prefix}szamlaKifizetese"
            val penzBeerkezettSzamlara = "${prefix}penzBeerkezettSzamlara"
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


private interface MegrendelesFilter {
    fun label(state: MegrendelesScreenState, appState: AppState): String
    fun predicate(state: MegrendelesScreenState, megr: Megrendeles): Boolean
}

private data class SimpleMegrendelesFilter(val label: String, val icon: String? = null, private val predicate: Megrendeles.() -> Boolean) : MegrendelesFilter {

    override fun label(state: MegrendelesScreenState, appState: AppState): String {
        return label
    }

    override fun predicate(state: MegrendelesScreenState, megr: Megrendeles): Boolean {
        return megr.predicate()
    }
}

private val atNemVettFilter = SimpleMegrendelesFilter("Át nem vett") {
    (megrendelesMegtekint == null)
            .and(!alvallalkozoElkeszult)
            //.and(ellenorizve == false) // ???
            .and(penzBeerkezettDatum == null && keszpenzesBefizetes == null)
            .and(feltoltveMegrendelonek == null)
            .and(zarolva == null)
}
private val atvettFilter = SimpleMegrendelesFilter("Átvett") {
    (megrendelesMegtekint != null)
            .and(!alvallalkozoElkeszult)
            .and(penzBeerkezettDatum == null && keszpenzesBefizetes == null)
            .and(feltoltveMegrendelonek == null)
            .and(zarolva == null)
            .and(!this.isAkadalyos())
}

fun feladatElvegezveEbbenAHonapban(ertekbecslesFeltoltve: Moment?, energetikaFeltoltve: Moment?): Boolean {
    val latestUplaodDate = arrayOf(ertekbecslesFeltoltve, energetikaFeltoltve).filterNotNull().maxBy { it.toDate().getTime() }!!
    return moment().isSame(latestUplaodDate, Granularity.Month)
}

private val alvallalkozoVegzettVeleAdottHonapban = SimpleMegrendelesFilter("Elvégezve", "check") {
    alvallalkozoVegzettVele()
}

private fun Megrendeles.alvallalkozoVegzettVele(): Boolean {
    return (megrendelesMegtekint != null)
            .and(alvallalkozoElkeszult)
            .and(penzBeerkezettDatum != null || keszpenzesBefizetes != null)
            .and(feladatElvegezveEbbenAHonapban(ertekbecslesFeltoltve, energetikaFeltoltve))
}

private val hataridosFilterForAdmin = SimpleMegrendelesFilter("Határidős", "time", {
    isHataridos()
})

private fun Megrendeles.isHataridos(): Boolean {
    return (hatarido.isBefore(moment()))
            .and(feltoltveMegrendelonek == null)
            .and(zarolva == null)
}

private val hataridosFilterForAlv = SimpleMegrendelesFilter("Határidős", "time") {
    this.alvallalkozoElkeszult.not() && this.isHataridos()
}

private fun Megrendeles.isAkadalyos(): Boolean {
    val statusIsAkadalyos = statusz in akadalyosStatuszok
    val latestStatusChangerDate = latest(ertekbecslesFeltoltve, energetikaFeltoltve, penzBeerkezettDatum, keszpenzesBefizetes, feltoltveMegrendelonek)
    return if (statusIsAkadalyos && latestStatusChangerDate != null) {
        val lastAkadalyDate = akadalyok.maxBy { it.rogzitve.toDate().getTime() }
        lastAkadalyDate != null && lastAkadalyDate.rogzitve.isAfter(latestStatusChangerDate)
    } else {
        statusIsAkadalyos
    }
}

private val akadalyosFilterForAdmin = SimpleMegrendelesFilter("Akadályos", "ban-circle") {
    this.isAkadalyos()
}
private val akadalyosFilterForAlvallalkozo = SimpleMegrendelesFilter("Akadályos", "ban-circle") {
    this.alvallalkozoElkeszult.not() && this.isAkadalyos()
}
private val utalasHianyzikFilter = SimpleMegrendelesFilter("Utalás hiányzik", "alert") {
    (megrendelesMegtekint != null)
            .and(alvallalkozoElkeszult)
            //.and(ellenorizve == false)
            .and(penzBeerkezettDatum == null && keszpenzesBefizetes == null)
            .and(feltoltveMegrendelonek == null)
            .and(!this.isAkadalyos())
            .and(zarolva == null)
}
private val ellenorzesreVarFilter = SimpleMegrendelesFilter("Ellenőrzésre vár", "check") {
    (megrendelesMegtekint != null)
            .and(alvallalkozoElkeszult)
            //.and(ellenorizve == false) // ???
            .and(penzBeerkezettDatum != null || keszpenzesBefizetes != null)
            .and(feltoltveMegrendelonek == null)
            .and(!this.isAkadalyos())
            .and(zarolva == null)
}
private val archivalasraVarFilter = SimpleMegrendelesFilter("Archiválásra vár", "folder-close") {
    (megrendelesMegtekint != null)
            .and(alvallalkozoElkeszult)
            //.and(ellenorizve == false) // ???
            .and(szamlaSorszama.isNotEmpty())
            .and(penzBeerkezettDatum != null || keszpenzesBefizetes != null)
            .and(feltoltveMegrendelonek != null)
            .and(!this.isAkadalyos())
            .and(zarolva == null)
}


private data class HaviTeljesites(val alvallalkozoId: Int, val date: Moment)

data class SzuroMezo(val columnData: MegrendelesColumnData, val operator: String, val operand: String) {
    fun toSqlWhereClause(): String {
        val keyEquality = columnData.renderer == ebRenderer || columnData.renderer == avRenderer
        return if (operand.isEmpty() || operator.isEmpty()) {
            return "1=1"
        } else if (columnData.megrendelesFieldType == MegrendelesFieldType.Int || keyEquality) {
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
            val dbName = columnData.dbName
            when (operator) {
                "Nem tartamazza" -> "$dbName not like '%$operand%'"
                "Kezdődik" -> "$dbName like '$operand%'"
                "Végződik" -> "$dbName like '%$operand'"
                else -> "$dbName like '%$operand%'" // "Tartalmazza"
            }
        }
    }
}

private data class MegrendelesScreenState(
        val showMindFilterModal: Boolean,
        val activeFilter: MegrendelesFilter,
        val haviTeljesites: HaviTeljesites?,
        val haviTeljesitesModalOpen: Boolean,
        val mindFilteredMegrendelesIds: List<Int>,
        val szuroMezok: List<SzuroMezo>
)


// . = "MyComponent";
fun megrendelesScreen(user: LoggedInUser,
                      appState: AppState,
                      globalDispatch: (Action) -> Unit): ReactElement {
    val megrendelesScreenElement = { props: dynamic ->
        val user: LoggedInUser = props.user
        val appState: AppState = props.appState
        val globalDispatch: (Action) -> Unit = props.globalDispatch
        // props
//    var megrendelesId: Int? by Property()
//    var showAlvallalkozoSajatAdataiModal: Boolean by Property()
//    var showTableConfigurationModal: Boolean by Property()
        val (state, setState) = useState(MegrendelesScreenState(
                activeFilter = atNemVettFilter,
                haviTeljesites = if (user.isAlvallalkozo) {
                    HaviTeljesites(user.alvallalkozoId, moment())
                } else null,
                haviTeljesitesModalOpen = false,
                mindFilteredMegrendelesIds = emptyList(),
                szuroMezok = emptyList(),
                showMindFilterModal = false)
        )
        //    bsGrid({ id = MegrendelesScreenIds.screenId; fluid = true }) {
//        if (LoggedInUserStore.loggedInUser.username == "bb") {
//            Row {
//                bsCol {
//                    bsButton({
//                        onClick = { runIntegrationTests() }
//                    }) {
//                        text("Test")
//                    }
//                }
//            }
//        }
//        if (LoggedInUserStore.isAdmin) {
//            bsRow {
//                bsCol({ md = 12; mdOffset = 0 }) { addNewButton() }
//            }
//        }

        buildElement {
            div {
                Row {
                    simpleFilterButtons(user, state, appState.megrendelesState.megrendelesek, globalDispatch, setState)
                    +"  "
                    haviTeljesitesFilterButton(state, appState.megrendelesState.megrendelesek, appState.alvallalkozoState.alvallalkozok, setState, globalDispatch)
                }
                Row {
                    Col(span = 24) {
                        megrendelesekTable(user, state, appState, globalDispatch)
                    }
                }
                if (haviTeljesitesFilter == state.activeFilter && user.isAdmin) {
                    Row {
                        val avId = state.haviTeljesites!!.alvallalkozoId
                        val dateStr = state.haviTeljesites.date.format(monthFormat)
                        Button {
                            attrs.onClick = {
                                val x = XMLHttpRequest()
                                x.open("GET", "/haviTeljesites/$avId/$dateStr", true)
                                x.responseType = XMLHttpRequestResponseType.BLOB
                                x.onload = { e ->
                                    downloadFile(e.target.asDynamic().response,
                                            "${appState.alvallalkozoState.alvallalkozok[avId]!!.name} ${dateStr}.xlsx")
                                }
                                x.send()

                            }
                            Icon("download")
                            +" Havi teljesítés letöltése"
                        }

                    }
                }
                child(haviTeljesitesModal(state,
                        appState.alvallalkozoState.alvallalkozok,
                        setState,
                        globalDispatch
                ))
                mindFilterModal(appState, globalDispatch, state, setState)
            }
        }


//                megrendelesModal(self)

//                tableConfigurationModal(self)
//    }
    }
    return createElement(megrendelesScreenElement, jsObject<dynamic> {
        this.user = user
        this.appState = appState
        this.globalDispatch = globalDispatch
    })
}

private fun RBuilder.megrendelesekTable(user: LoggedInUser,
                                        screenState: MegrendelesScreenState,
                                        appState: AppState,
                                        globalDispatch: (Action) -> Unit) {
    val filteredMegrendelesek = appState.megrendelesState.megrendelesek.values.filter {
        screenState.activeFilter.predicate(screenState, it)
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
            communicator.asyncPost<dynamic>(RestUrl.megrendelesMegnyitasa, object {
                val megrendelesId = megr.id
            }) { response ->
                globalDispatch(Action.MegrendelesekFromServer(arrayOf(response)))
            }
            globalDispatch(Action.ChangeURL(Path.megrendeles.edit(megr.id)))
        }
    }
    val columns = user.megrendelesTableColumns.map { columnDef ->
        ColumnProps {
            title = columnDef.columnTitle
            dataIndex = columnDef.fieldName
            filters = columnDef.filter?.filterComboValues?.invoke(filteredMegrendelesek)?.map {
                object {
                    val text = it.first;
                    val value = it.second
                }
            }?.toTypedArray()
            onFilter = columnDef.filter?.filter.asDynamic()
            this.asDynamic().render = { cell: Statusz?, row: Any, index: Int -> columnDef.renderer.asDynamic()(cell, row, index, appState, onClick) }
        }
    }.toTypedArray()
    Table {
        attrs.columns = columns
        attrs.dataSource = filteredMegrendelesek
        attrs.rowKey = "id"
        attrs.asDynamic().size = ButtonSize.small
    }
    /*data class MegrendelesColumnData(val dbName: String,
                                 val fieldName: String,
                                 val columnTitle: String,
                                 val renderer: Any,
                                 val megrendelesFieldType: MegrendelesFieldType)*/
//    if (LoggedInUserStore.loggedInUser.megrendelesTableColumns.isNotEmpty()) {
//        bootstrapTable<Megrendeles>({
//            ref = "table"
//            data = filteredMegrendelesek
//            hover = true
//            clearSearch = true
//            pagination = false
//            searchPlaceholder = "Szűrés"
//            selectRow = SelectRowProp(
//                    SelectionMode.radio,
//                    clickToSelect = true,
//                    hideSelectColumn = true
//            )
//            options = object {
//
//            }
//            search = true
//            trClassName = { megr: Megrendeles, rowIndex: Int ->
//                val (alvallalkozoAzAtNemVettTabon, alvallalkozoAzElvegezveTabon) = if (LoggedInUserStore.loggedInUser.role == Role.ROLE_USER) {
//                    Pair(self.state.filter == atNemVettFilter, self.state.filter == alvallalkozoVegzettVeleAdottHonapban)
//                } else Pair(false, false)
//                val goodTab = !alvallalkozoAzAtNemVettTabon && !alvallalkozoAzElvegezveTabon
//                if (goodTab && megr.isUnread(LoggedInUserStore.loggedInUser.role)) {
//                    "unread_megr"
//                } else ""
//            }
//            condensed = true
//        }) {
//            keyColumn("ID", "id", 0)
//            LoggedInUserStore.loggedInUser.megrendelesTableColumns.forEach { confugiredColumnData ->
//                column(confugiredColumnData.columnTitle,
//                        confugiredColumnData.fieldName,
//                        10,
//                        renderer = confugiredColumnData.renderer.asDynamic())
//            }
//        }
//    } else {
////        text("Önnek nincsenek megjeleníthető oszlopok beállítva. Ezt megteheti a 'Beállítások > Táblázat beállítások' menüpontban, vagy kérje az adminisztrátor segítségét.")
//    }
}

private fun RBuilder.mindFilterModal(
        appState: AppState,
        globalDispatch: (Action) -> Unit,
        state: MegrendelesScreenState,
        setState: Dispatcher<MegrendelesScreenState>) {
    child(customSearchModal(appState, state.szuroMezok, visible = state.showMindFilterModal, onClose = { ok, szuroMezok ->
        if (!ok) {
            setState(state.copy(
                    showMindFilterModal = false
            ))
        } else {
            val sqlWhereClause = szuroMezok.map { szuroMezo ->
                szuroMezo.toSqlWhereClause()
            }.joinToString(") AND (", "(", ")")
            communicator.getEntitiesFromServer(RestUrl.megrendelesFilter, object {
                val text = sqlWhereClause
            }) { response: Array<dynamic> ->
                val ids = response.map { it.id as Int }
                setState(state.copy(
                        activeFilter = MegrendelesScreen.mindFilter,
                        mindFilteredMegrendelesIds = ids,
                        showMindFilterModal = false,
                        szuroMezok = szuroMezok
                ))
                globalDispatch(Action.MegrendelesekFromServer(response))
                globalDispatch(Action.ChangeURL(Path.megrendeles.root))
            }
        }
    }))
}


private object MegrendelesScreen {
//    val component: ReactStatefulComponent<MegrendelesScreenProps, MegrendelesScreenState> = StatefulReactBuilder<MegrendelesScreenProps, MegrendelesScreenState>().apply {
//
//
//        componentDidMount = { self ->
//            AlvallalkozoStore.addChangeListener(this) {
//                self.forceUpdate()
//            }
//            GeoStore.addChangeListener(this) {
//                self.forceUpdate()
//            }
//            SajatArStore.addChangeListener(this) {
//                self.forceUpdate()
//            }
//            LoggedInUserStore.addChangeListener(this) {
//                self.forceUpdate()
//            }
//            MegrendelesStore.addChangeListener(this) {
//                self.setState(self.state.copy(megrendelesek = MegrendelesStore.megrendelesek))
//            }
//        }
//
//        componentWillUnmount = { self ->
//            AlvallalkozoStore.removeListener(this)
//            GeoStore.removeListener(this)
//            SajatArStore.removeListener(this)
//            LoggedInUserStore.removeListener(this)
//            MegrendelesStore.removeListener(this)
//        }


    val haviTeljesitesFilter = object : MegrendelesFilter {
        override fun label(state: MegrendelesScreenState, appState: AppState): String {
            return ""
        }

        override fun predicate(state: MegrendelesScreenState,
                               megr: Megrendeles): Boolean {
            val props = state.haviTeljesites
            return if (props == null) false else
                megr.alvallalkozoId == props.alvallalkozoId && megr.feltoltveMegrendelonek?.isSame(props.date, Granularity.Month) ?: false
        }
    }

    val mindFilter = object : MegrendelesFilter {
        override fun label(state: MegrendelesScreenState, appState: AppState): String = "Szűrés"

        override fun predicate(state: MegrendelesScreenState, megr: Megrendeles): Boolean = megr.id in state.mindFilteredMegrendelesIds
    }

    //
//    private fun Component.tableConfigurationModal(self: ReactStatefulComponent<MegrendelesScreenProps, MegrendelesScreenState>) {
//        if (self.props.showTableConfigurationModal) {
//            MegrendelesColumnConfigModal({
//                close = { result, columns ->
//                    if (result == ModalResult.Close) {
//                        Actions.changeURL(Path.megrendeles.root)
//                    } else {
//                        val sendMsg = columns!!.map { it.fieldName }.joinToString(",")
//                        communicator.getEntityFromServer<dynamic>(RestUrl.megrendelesTablazatOszlop, object {
//                            val text = sendMsg
//                        }) { response ->
//                            Actions.megrendelesTableOszlopFromServer(response.text)
//                            Actions.notification(Notification(BsStyle.Success, "Beálíltások elmentve"))
//                            Actions.changeURL(Path.megrendeles.root)
//                        }
//                    }
//                }
//            })
//        }
//    }
//
//
//

    fun RBuilder.haviTeljesitesFilterButton(state: MegrendelesScreenState,
                                            megrendelesek: Map<Int, Megrendeles>,
                                            alvallalkozok: Map<Int, Alvallalkozo>,
                                            screenDispatch: Dispatcher<MegrendelesScreenState>,
                                            globalDispatch: (Action) -> Unit) {
        ButtonGroup {
            Button(disabled = state.haviTeljesites == null,
                    type = if (state.activeFilter == haviTeljesitesFilter) ButtonType.primary else ButtonType.default,
                    onClick = {
                        screenDispatch(state.copy(activeFilter = haviTeljesitesFilter))
//                        globalDispatch(Action.FilterMegrendelesek(MegrendelesFilter(
//                                state.haviTeljesites!!.alvallalkozoId,
//                                state.haviTeljesites.date
//                        )))
                    }) {
                val filteredMegrendelesek = megrendelesek.values.filter { haviTeljesitesFilter.predicate(state, it) }
                Badge(filteredMegrendelesek.count()) {
                    attrs.asDynamic().style = jsStyle {
                        backgroundColor = "#1890ff"
                    }
                    span {
                        attrs.jsStyle {
                            marginRight = "15px"
                        }
                        val props = state.haviTeljesites
                        val label = if (props == null) "Havi teljesítés" else {
                            val alv = alvallalkozok[props.alvallalkozoId]!!
                            "${alv.name}: ${props.date.format(monthFormat)}"
                        }
                        +" $label "
                    }
                }
            }
            Button(
                    icon = "edit",
                    onClick = { screenDispatch(state.copy(haviTeljesitesModalOpen = true)) }
            )
        }
//        div({ className = "dropdown btn-group" }) {
//            bsButton({
//                disabled = self.state.haviTeljesites == null
//                bsStyle = if (self.state.filter == haviTeljesitesFilter) BsStyle.Info else BsStyle.Default
//                onClick = {

//                }
//            }) {
//                text("${haviTeljesitesFilter.label(self)} ")
//                val filteredMegrendelesek = self.state.megrendelesek.values.filter { haviTeljesitesFilter.predicate(self, it) }
//                val count = filteredMegrendelesek.count()
//                if (count > 0) {
//                    span({ className = "badge" }) {
//                        text(count.toString())
//                    }
//                }
//            }
//            bsButton({
//                onClick = { self.setState(self.state.copy(haviTeljesitesModalOpen = true)) }
//            }) {
//                bsIcon("cog")
//            }
//        }
    }


    //    private fun Component.megrendelesModal(self: ReactStatefulComponent<MegrendelesScreenProps, MegrendelesScreenState>) {
//        if (self.props.megrendelesId != null) {
//            val id = self.props.megrendelesId!!
//            if (LoggedInUserStore.isAdmin) {
//                MegrendelesModal({
//                    primaryKey = id
//                    close = { result, megr ->
//                        if (result == ModalResult.Save) {
//                            communicator.saveEntity<Megrendeles, dynamic>(RestUrl.saveMegrendeles, megr!!) { response ->
//                                Actions.megrendelesekFromServer(arrayOf(response))
//                                Actions.changeURL(Path.megrendeles.root)
//                                val str = if (id == 0) "létrejött" else "módosult"
//                                Actions.notification(Notification(BsStyle.Success, "A Megrendelés sikeresen $str"))
//                            }
//                        } else {
//                            Actions.changeURL(Path.megrendeles.root)
//                        }
//                    }
//                })
//            } else if (LoggedInUserStore.isAlvallalkozo) {
//                AlvallalkozoMegrendelesModal({
//                    megrendeles = self.state.megrendelesek[id]!!
//                    close = { dataToSave ->
//                        if (dataToSave != null) {
//                            communicator.saveEntity<Any, dynamic>(RestUrl.saveMegrendeles, dataToSave) { response ->
//                                Actions.megrendelesekFromServer(arrayOf(response))
//                                Actions.changeURL(Path.megrendeles.root)
//                                Actions.notification(Notification(BsStyle.Success, "A Megrendelés sikeresen elmentve"))
//                            }
//                        } else {
//                            Actions.changeURL(Path.megrendeles.root)
//                        }
//                    }
//                })
//            }
//        } else if (self.props.showAlvallalkozoSajatAdataiModal) {
//            AlvallalkozoSajatAdataiModal({
//                alvallalkozo = LoggedInUserStore.loggedInAlvallalkozo
//                close = {
//                    Actions.changeURL(Path.root)
//                }
//            })
//        }
//    }
//
//    private fun cellColorer(): (dynamic) -> String {
//        return { params: dynamic ->
//            val clazz = with(params.data as Megrendeles) {
//                if (!alvallalkozoElkeszult && szamlaSorszama.isNotEmpty() && penzBeerkezettDatum != null) {
//                    // az ügyfél fizetett, alvállalkozó még nem készítette el az értékbecslést
//                    "bg-primary"
//                } else if (alvallalkozoElkeszult && penzBeerkezettDatum == null) {
//                    // alvállalkozó feltöltötte, az ügyfél még nem fizetett
//                    "bg-warning"
//                } else if (isAkadalyos()) {
//                    // akadályos ( nem ért rá, dok.hiány, stb)
//                    "bg-danger"
//                } else if (statusz == Statusz.G4) {
//                    // lemondva
//                    "bg-info"
//                } else if (alvallalkozoElkeszult && penzBeerkezettDatum != null && szamlaSorszama.isNotEmpty()) {
//                    // ellenőrizhető - értékbecslés díja átutalva, az alvállalkozó feltöltötte az értékbecslést
//                    "bg-success"
//                } else {
//                    ""
//                }
//            }
//            """<p class='$clazz'><strong>${params.value}</strong></p>"""
//        }
//    }
//
//    private fun Component.addNewButton() {
//        bsButton({
//            id = MegrendelesScreenIds.addButton
//            bsStyle = BsStyle.Primary
//            onClick = {
//                Actions.changeURL(Path.megrendeles.withOpenedEditorModal(0))
//            }
//        }) {
//            bsIcon("plus")
//            text(" Új Értékbecslés")
//        }
//    }
//
    fun RBuilder.simpleFilterButtons(loggedInUser: LoggedInUser,
                                     state: MegrendelesScreenState,
                                     megrendelesek: Map<Int, Megrendeles>,
                                     globalDispatch: (Action) -> Unit,
                                     setState: Dispatcher<MegrendelesScreenState>) {
        createSimpleFilterButtons(state, megrendelesek, getSimpleFilters(loggedInUser), setState)

        if (loggedInUser.isAdmin) {
            Button(icon = "search",
                    type = if (state.activeFilter == mindFilter) ButtonType.primary else ButtonType.default) {
                attrs.onClick = {
                    setState(state.copy(
                            showMindFilterModal = true
                    ))
                }
                +"Szűrés"
            }
        }
    }

    //
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
                    atvettFilter,
                    hataridosFilterForAlv,
                    akadalyosFilterForAlvallalkozo,
                    utalasHianyzikFilter,
                    alvallalkozoVegzettVeleAdottHonapban
            )
            Role.ROLE_ELLENORZO -> emptyArray()
        }
    }

    private fun RBuilder.createSimpleFilterButtons(state: MegrendelesScreenState,
                                                   megrendelesek: Map<Int, Megrendeles>,
                                                   simpleFilters: Array<SimpleMegrendelesFilter>,
                                                   screenDispatch: Dispatcher<MegrendelesScreenState>) {
        simpleFilters.forEach { filter ->
            val megrendelesek = megrendelesek.values.filter { filter.predicate(state, it) }
            val count = megrendelesek.count()
            Button(type = if (filter === state.activeFilter) ButtonType.primary else ButtonType.default,
                    icon = filter.icon,
                    onClick = {
                        screenDispatch(state.copy(
                                activeFilter = filter
                        ))
                    }) {
                if (count > 0) {
                    filterButtonCountBadge(count, filter)
                } else {
                    +((if (filter.icon == null) "" else " ") + filter.label)
                }
            }
            span {
                attrs.jsStyle {
                    marginRight = "5px"
                }
            }
        }
    }

    private fun RElementBuilder<ButtonProps>.filterButtonCountBadge(count: Int, filter: SimpleMegrendelesFilter) {
        Badge(count) {
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

private fun haviTeljesitesModal(state: MegrendelesScreenState,
                                osszesAlvallalkozo: Map<Int, Alvallalkozo>,
                                screenDispatch: Dispatcher<MegrendelesScreenState>,
                                globalDispatch: (Action) -> Unit): ReactElement {
    return createElement(type = { props: dynamic ->
        val state: MegrendelesScreenState = props.state
        val osszesAlvallalkozo: Map<Int, Alvallalkozo> = props.osszesAlvallalkozo
        val screenDispatch: Dispatcher<MegrendelesScreenState> = props.screenDispatch
        val globalDispatch: (Action) -> Unit = props.globalDispatch

        val alvallalkozok = osszesAlvallalkozo.getEnabledAlvallalkozok()
        val haviTeljesites = state.haviTeljesites ?: HaviTeljesites(0, moment())
        val (modalState, modalDispatch) = useState(haviTeljesites)
        buildElement {
            Modal {
                attrs.title = StringOrReactElement.fromString("Havi teljesítés")
                attrs.visible = state.haviTeljesitesModalOpen
                attrs.okButtonProps = jsObject {
                    disabled = modalState.alvallalkozoId == 0
                }
                attrs.onOk = {
                    screenDispatch(state.copy(
                            haviTeljesitesModalOpen = false,
                            haviTeljesites = modalState,
                            activeFilter = haviTeljesitesFilter
                    ))
                    globalDispatch(Action.FilterMegrendelesek(MegrendelesFilter(modalState.alvallalkozoId, modalState.date)))
                }
                attrs.onCancel = {
                    screenDispatch(state.copy(haviTeljesitesModalOpen = false))
                }
                Form {
                    FormItem {
                        attrs.labelCol = ColProperties(span = 4)
                        attrs.wrapperCol = ColProperties(span = 14)
                        attrs.label = StringOrReactElement.fromString("Alvállalkozó")
                        AutoComplete(alvallalkozok.map { it.name }.sortedBy { it }.toTypedArray()) {
                            attrs.placeholder = "Alvállalkozó"
                            attrs.filterOption = { inputString, optionElement ->
                                (optionElement.props.children as String).toUpperCase().replace(" ", "").contains(inputString.toUpperCase().replace(" ", ""))
                            }
                            attrs.onSelect = { selectedName ->
                                modalDispatch(modalState.copy(alvallalkozoId = alvallalkozok.first { it.name == selectedName }.id))
                            }
                        }
                    }
                    FormItem {
                        attrs.labelCol = ColProperties(span = 4)
                        attrs.wrapperCol = ColProperties(span = 14)
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
        }!!

    }, props = jsObject<dynamic> {
        this.state = state
        this.osszesAlvallalkozo = osszesAlvallalkozo
        this.screenDispatch = screenDispatch
        this.globalDispatch = globalDispatch
    })
}


//fun Component.MegrendelesScreen(
//        properties: MegrendelesScreenProps.() -> Unit = {},
//        init: Component.() -> Unit = {}) {
//    externalReactClass(
//            MegrendelesScreen.component,
//            Ref(initProps(MegrendelesScreenProps(), properties)).asDynamic(),
//            init)
//}