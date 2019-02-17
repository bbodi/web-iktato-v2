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
import hu.nevermind.utils.app.DefinedReactComponent
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

val downloadFile: (url: String, name: String) -> Unit = kotlinext.js.require("downloadjs")

object MegrendelesScreenIds {
    val screenId = "megrendelesScreen"

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
    fun label(state: MegrendelesTableFilterState, appState: AppState): String
    fun predicate(state: MegrendelesTableFilterState, megr: Megrendeles): Boolean
}

data class SimpleMegrendelesFilter(val label: String, val icon: String? = null, private val predicate: Megrendeles.() -> Boolean) : MegrendelesFilter {

    override fun label(state: MegrendelesTableFilterState, appState: AppState): String {
        return label
    }

    override fun predicate(state: MegrendelesTableFilterState, megr: Megrendeles): Boolean {
        return megr.predicate()
    }
}

// TODO: a filter gombokon az ikonok tünjenek el kis felbontásban
val atNemVettFilter = SimpleMegrendelesFilter("Át nem vett") {
    (megrendelesMegtekint == null)
            .and(!alvallalkozoFeltoltotteFajlokat)
            //.and(ellenorizve == false) // ???
            .and(penzBeerkezettDatum == null && keszpenzesBefizetes == null)
            .and(feltoltveMegrendelonek == null)
            .and(zarolva == null)
}
private val atvettFilter = SimpleMegrendelesFilter("Átvett") {
    (megrendelesMegtekint != null)
            .and(!alvallalkozoFeltoltotteFajlokat)
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
            .and(alvallalkozoFeltoltotteFajlokat)
            .and(penzBeerkezettDatum != null || keszpenzesBefizetes != null)
            .and(feladatElvegezveEbbenAHonapban(ertekbecslesFeltoltve, energetikaFeltoltve))
}

private val hataridosFilterForAdmin = SimpleMegrendelesFilter("Határidős", "time", {
    isHataridos()
})

private fun Megrendeles.isHataridos(): Boolean {
    return (hatarido?.isBefore(moment()) ?: true)
            .and(feltoltveMegrendelonek == null)
            .and(zarolva == null)
}

private val hataridosFilterForAlv = SimpleMegrendelesFilter("Határidős", "time") {
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

private val akadalyosFilterForAdmin = SimpleMegrendelesFilter("Akadályos", "ban-circle") {
    this.isAkadalyos()
}
private val akadalyosFilterForAlvallalkozo = SimpleMegrendelesFilter("Akadályos", "ban-circle") {
    this.alvallalkozoFeltoltotteFajlokat.not() && this.isAkadalyos()
}
private val utalasHianyzikFilter = SimpleMegrendelesFilter("Utalás hiányzik", "alert") {
    (megrendelesMegtekint != null)
            .and(alvallalkozoFeltoltotteFajlokat)
            //.and(ellenorizve == false)
            .and(penzBeerkezettDatum == null && keszpenzesBefizetes == null)
            .and(feltoltveMegrendelonek == null)
            .and(!this.isAkadalyos())
            .and(zarolva == null)
}
private val ellenorzesreVarFilter = SimpleMegrendelesFilter("Ellenőrzésre vár", "check") {
    (megrendelesMegtekint != null)
            .and(alvallalkozoFeltoltotteFajlokat)
            //.and(ellenorizve == false) // ???
            .and(penzBeerkezettDatum != null || keszpenzesBefizetes != null)
            .and(feltoltveMegrendelonek == null)
            .and(!this.isAkadalyos())
            .and(zarolva == null)
}
private val archivalasraVarFilter = SimpleMegrendelesFilter("Archiválásra vár", "folder-close") {
    (megrendelesMegtekint != null)
            .and(alvallalkozoFeltoltotteFajlokat)
            //.and(ellenorizve == false) // ???
            .and(szamlaSorszama.isNotEmpty())
            .and(penzBeerkezettDatum != null || keszpenzesBefizetes != null)
            .and(feltoltveMegrendelonek != null)
            .and(!this.isAkadalyos())
            .and(zarolva == null)
}


data class HaviTeljesites(val alvallalkozoId: Int, val date: Moment)

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

data class MegrendelesTableFilterState(val activeFilter: MegrendelesFilter,
                                       val mindFilteredMegrendelesIds: List<Int>,
                                       val szuroMezok: List<SzuroMezo>,
                                       val haviTeljesites: HaviTeljesites?)

data class MegrendelesScreenState(
        val showMindFilterModal: Boolean,
        val haviTeljesitesModalOpen: Boolean
)

data class MegrendelesScreenParams(val appState: AppState,
                                   val globalDispatch: (Action) -> Unit)

object MegrendelesScreenComponent : DefinedReactComponent<MegrendelesScreenParams>() {
    override fun RBuilder.body(props: MegrendelesScreenParams) {
        val appState: AppState = props.appState
        val globalDispatch: (Action) -> Unit = props.globalDispatch
        val user = props.appState.maybeLoggedInUser!!
        val (state, setState) = useState(MegrendelesScreenState(
                haviTeljesitesModalOpen = false,
                showMindFilterModal = false)
        )
        div {
            Row {
                Button {
                    attrs.asDynamic().id = MegrendelesScreenIds.addButton
                    attrs.type = ButtonType.primary
                    attrs.onClick = {
                        globalDispatch(Action.ChangeURL(Path.megrendeles.edit(0)))
                    }
                    Icon("plus")
                    +" Hozzáadás"
                }
                Divider(type = DividerType.vertical, orientation = Orientation.left)
                Divider(type = DividerType.vertical, orientation = Orientation.right)
                simpleFilterButtons(
                        user,
                        state,
                        appState.megrendelesTableFilterState,
                        appState.megrendelesState.megrendelesek,
                        globalDispatch,
                        setState
                )
                Divider(type = DividerType.vertical, orientation = Orientation.left)
                Divider(type = DividerType.vertical, orientation = Orientation.right)
                haviTeljesitesFilterButton(
                        appState.megrendelesTableFilterState,
                        state,
                        appState.megrendelesState.megrendelesek,
                        appState.alvallalkozoState.alvallalkozok,
                        setState,
                        globalDispatch
                )
            }
            Row {
                Col(span = 24) {
                    megrendelesekTable(user, appState, globalDispatch)
                }
            }
            if (haviTeljesitesFilter == appState.megrendelesTableFilterState.activeFilter && user.isAdmin) {
                Row {
                    val avId = appState.megrendelesTableFilterState.haviTeljesites!!.alvallalkozoId
                    val dateStr = appState.megrendelesTableFilterState.haviTeljesites.date.format(monthFormat)
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
                    appState.megrendelesTableFilterState,
                    appState.alvallalkozoState.alvallalkozok,
                    setState,
                    globalDispatch
            ))
            mindFilterModal(appState, globalDispatch, state, setState)
        }
    }
}

private fun RBuilder.megrendelesekTable(user: LoggedInUser,
                                        appState: AppState,
                                        globalDispatch: (Action) -> Unit) {
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
        attrs.bordered = true
        attrs.onRow = { megrendeles: Megrendeles ->
            jsObject {
                this.asDynamic().onClick = { onClick(megrendeles) }
            }
        }
        attrs.asDynamic().size = "middle"
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
//            trClassName = { megrendeles: Megrendeles, rowIndex: Int ->
//                val (alvallalkozoAzAtNemVettTabon, alvallalkozoAzElvegezveTabon) = if (LoggedInUserStore.loggedInUser.role == Role.ROLE_USER) {
//                    Pair(self.formState.filter == atNemVettFilter, self.formState.filter == alvallalkozoVegzettVeleAdottHonapban)
//                } else Pair(false, false)
//                val goodTab = !alvallalkozoAzAtNemVettTabon && !alvallalkozoAzElvegezveTabon
//                if (goodTab && megrendeles.isUnread(LoggedInUserStore.loggedInUser.role)) {
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
    child(customSearchModal(appState, appState.megrendelesTableFilterState.szuroMezok, visible = state.showMindFilterModal, onClose = { ok, szuroMezok ->
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
                        showMindFilterModal = false
                ))
                globalDispatch(Action.SetActiveFilter(
                        SetActiveFilterPayload.MindFilter(ids, szuroMezok)
                ))
                globalDispatch(Action.MegrendelesekFromServer(response))
                globalDispatch(Action.ChangeURL(Path.megrendeles.root))
            }
        }
    }))
}


object MegrendelesScreen {
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
//                self.setState(self.formState.copy(megrendelesek = MegrendelesStore.megrendelesek))
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
        override fun label(state: MegrendelesTableFilterState, appState: AppState): String = "Szűrés"

        override fun predicate(state: MegrendelesTableFilterState, megr: Megrendeles): Boolean = megr.id in state.mindFilteredMegrendelesIds
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

    fun RBuilder.haviTeljesitesFilterButton(filterState: MegrendelesTableFilterState,
                                            state: MegrendelesScreenState,
                                            megrendelesek: Map<Int, Megrendeles>,
                                            alvallalkozok: Map<Int, Alvallalkozo>,
                                            screenDispatch: Dispatcher<MegrendelesScreenState>,
                                            globalDispatch: (Action) -> Unit) {
        ButtonGroup {
            Button(disabled = filterState.haviTeljesites == null,
                    type = if (filterState.activeFilter == haviTeljesitesFilter) ButtonType.primary else ButtonType.default,
                    onClick = {
                        globalDispatch(Action.SetActiveFilter(
                                SetActiveFilterPayload.HaviTeljesites(filterState.haviTeljesites!!.alvallalkozoId, filterState.haviTeljesites.date)
                        ))
                    }) {
                val filteredMegrendelesek = megrendelesek.values.filter { haviTeljesitesFilter.predicate(filterState, it) }
                Badge(filteredMegrendelesek.count()) {
                    attrs.asDynamic().style = jsStyle {
                        backgroundColor = "#1890ff"
                    }
                    span {
                        attrs.jsStyle {
                            marginRight = "15px"
                        }
                        val props = filterState.haviTeljesites
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
//                disabled = self.formState.haviTeljesites == null
//                bsStyle = if (self.formState.filter == haviTeljesitesFilter) BsStyle.Info else BsStyle.Default
//                onClick = {

//                }
//            }) {
//                text("${haviTeljesitesFilter.label(self)} ")
//                val filteredMegrendelesek = self.formState.megrendelesek.values.filter { haviTeljesitesFilter.predicate(self, it) }
//                val count = filteredMegrendelesek.count()
//                if (count > 0) {
//                    span({ className = "badge" }) {
//                        text(count.toString())
//                    }
//                }
//            }
//            bsButton({
//                onClick = { self.setState(self.formState.copy(haviTeljesitesModalOpen = true)) }
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
//                    close = { result, megrendeles ->
//                        if (result == ModalResult.Save) {
//                            communicator.saveEntity<Megrendeles, dynamic>(RestUrl.saveMegrendeles, megrendeles!!) { response ->
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
//                    megrendeles = self.formState.megrendelesek[id]!!
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
//                if (!alvallalkozoFeltoltotteFajlokat && szamlaSorszama.isNotEmpty() && penzBeerkezettDatum != null) {
//                    // az ügyfél fizetett, alvállalkozó még nem készítette el az értékbecslést
//                    "bg-primary"
//                } else if (alvallalkozoFeltoltotteFajlokat && penzBeerkezettDatum == null) {
//                    // alvállalkozó feltöltötte, az ügyfél még nem fizetett
//                    "bg-warning"
//                } else if (isAkadalyos()) {
//                    // akadályos ( nem ért rá, dok.hiány, stb)
//                    "bg-danger"
//                } else if (statusz == Statusz.G4) {
//                    // lemondva
//                    "bg-info"
//                } else if (alvallalkozoFeltoltotteFajlokat && penzBeerkezettDatum != null && szamlaSorszama.isNotEmpty()) {
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
                                     filterState: MegrendelesTableFilterState,
                                     megrendelesek: Map<Int, Megrendeles>,
                                     globalDispatch: (Action) -> Unit,
                                     setState: Dispatcher<MegrendelesScreenState>) {
        createSimpleFilterButtons(
                filterState,
                megrendelesek,
                getSimpleFilters(loggedInUser),
                globalDispatch
        )

        if (loggedInUser.isAdmin) {
            Button(icon = "search",
                    type = if (filterState.activeFilter == mindFilter) ButtonType.primary else ButtonType.default) {
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

    private fun RBuilder.createSimpleFilterButtons(filterState: MegrendelesTableFilterState,
                                                   megrendelesek: Map<Int, Megrendeles>,
                                                   simpleFilters: Array<SimpleMegrendelesFilter>,
                                                   globalDispatch: (Action) -> Unit) {
        simpleFilters.forEach { filter ->
            val megrendelesek = megrendelesek.values.filter { filter.predicate(filterState, it) }
            val count = megrendelesek.count()
            Button(type = if (filter === filterState.activeFilter) ButtonType.primary else ButtonType.default,
                    icon = filter.icon,
                    onClick = {
                        globalDispatch(Action.SetActiveFilter(SetActiveFilterPayload.SimpleFilter(filter)))
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
                                filterState: MegrendelesTableFilterState,
                                osszesAlvallalkozo: Map<Int, Alvallalkozo>,
                                screenDispatch: Dispatcher<MegrendelesScreenState>,
                                globalDispatch: (Action) -> Unit): ReactElement {
    return createElement(type = { props: dynamic ->
        val state: MegrendelesScreenState = props.state
        val osszesAlvallalkozo: Map<Int, Alvallalkozo> = props.osszesAlvallalkozo
        val screenDispatch: Dispatcher<MegrendelesScreenState> = props.screenDispatch
        val globalDispatch: (Action) -> Unit = props.globalDispatch

        val alvallalkozok = osszesAlvallalkozo.getEnabledAlvallalkozok()
        val haviTeljesites = filterState.haviTeljesites ?: HaviTeljesites(0, moment())
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
                            haviTeljesitesModalOpen = false
                    ))
                    globalDispatch(Action.SetActiveFilter(SetActiveFilterPayload.HaviTeljesites(
                            modalState.alvallalkozoId, modalState.date
                    )))
                }
                attrs.onCancel = {
                    screenDispatch(state.copy(haviTeljesitesModalOpen = false))
                }
                Form {
                    FormItem {
                        attrs.labelCol = ColProperties { span = 4 }
                        attrs.wrapperCol = ColProperties { span = 14 }
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
                        attrs.labelCol = ColProperties { span = 4 }
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