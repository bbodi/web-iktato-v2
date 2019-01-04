package hu.nevermind.demo.screen.megrendeles

import com.github.andrewoma.react.*
import app.common.*
import hu.nevermind.demo.*
import hu.nevermind.demo.screen.*
import hu.nevermind.demo.store.*
import hu.nevermind.reakt.bootstrap.*
import hu.nevermind.reakt.bootstrap.table.*
import kotlin.browser.window

enum class MegrendelesModalTab {
    AlapAdatok,
    FeltoltesZarolas,
    ingatlanAdatai,
    Akadalyok,
    Fajlok,
    PenzBeerkezett,
    Import
}

fun getNextWeekDay(dayCount: Int): Moment {
    var day = moment()
    var step = 0
    while (step < dayCount) {
        day = day.add(1, TimeUnit.Days)
        if (day.isoWeekday() != 6 && day.isoWeekday() != 7) {
            ++step
        }
    }
    return day
}

class MegrendelesFieldsFromExternalSource {
    var helyszinelo: String? = null
    var ingatlanTipusa: String? = null
    var ertekBecslo: String? = null
    var telekMeret: Double? = null
    var keszultsegiFok: Double? = null
    var ingatlanTerulet: Double? = null
    var forgalmiErtek: Double? = null

    var adasvetel: String? = null
    var adasvetelDatuma: String? = null

    var szemleIdopontja: String? = null
    var fajlagosAr: Double? = null
    var kolcsonIgenylo: String? = null
    var hrsz: String? = null

    // import text
    var ebAzonosito: String? = null
    var etAzonosito: String? = null
    var hatarido: Moment? = null
    var ugyfelNeve: String? = null
    var ugyfelTelefonszama: String? = null
    var ertesitesiNev: String? = null
    var ertesitesiTel: String? = null
    var lakasCel: String? = null
    var hitelOsszeg: Int? = null
    var ajanlatSzam: String? = null
    var szerzodesSzam: String? = null
    var energetika = false
    var ertekbecsles = false
    var irsz: String? = null
    var telepules: String? = null
    var regio: String? = null
}

data class MegrendelesModalState(val megrendeles: Megrendeles,
                                 val originalMegrendeles: Megrendeles,
                                 val ertesitendoSzemelyAzonos: Boolean,
                                 val azonosito1: String,
                                 val azonosito2: String,
                                 val selectableAlvallalkozok: Collection<Alvallalkozo>,
                                 val szamlazhatoDijAfa: Int?,
                                 val currentTab: MegrendelesModalTab,
                                 val megrendelesFieldsFromExcel: MegrendelesFieldsFromExternalSource? = null,
                                 val megrendelesFieldsFromImportText: MegrendelesFieldsFromExternalSource? = null,
                                 val importedText: String = "",
                                 val selectableMunkatipusok: List<String>,
                                 val importTextAreaVisible: Boolean = false)

private data class OldNewValues(val name: String, val newValue: String, val oldValue: String) {

}

private fun oldNewValues(name: String, newValue: Int?, oldValue: Int?): OldNewValues {
    val formattedNewValue = formatNullableNumber(newValue, 0, " ") ?: ""
    val formattedOldValue = formatNullableNumber(oldValue, 0, " ") ?: ""
    return OldNewValues(
            name,
            formattedNewValue,
            formattedOldValue
    )
}

fun excelValidation(comments: MutableMap<FieldDef<EditorDialogProps<Int, Megrendeles>, MegrendelesModalState>, FieldComments>, field: FieldDef<EditorDialogProps<Int, Megrendeles>, MegrendelesModalState>, oldValue: String?, newValue: String?) {
    if (((oldValue == newValue || oldValue.isNullOrEmpty()) && !newValue.isNullOrEmpty())) {
        val comment = comments.getOrPut(field) { FieldComments(null, null, null) }.copy(success = "Excelből betőltve")
        comments[field] = comment
    } else if (oldValue != newValue && !oldValue.isNullOrEmpty()) {
        val comment = comments.getOrPut(field) { FieldComments(null, null, null) }.copy(warning = "Excelből: ${newValue ?: ""}")
        comments[field] = comment
    }
}

fun excelValidation(comments: MutableMap<FieldDef<EditorDialogProps<Int, Megrendeles>, MegrendelesModalState>, FieldComments>, field: FieldDef<EditorDialogProps<Int, Megrendeles>, MegrendelesModalState>, oldValue: Int?, newValue: Int?) {
    excelValidation(comments, field, formatNullableNumber(oldValue, 0, " "), formatNullableNumber(newValue, 0, " "))
}

private object MegrendelesModal {
    val component: ReactStatefulComponent<EditorDialogProps<Int, Megrendeles>, MegrendelesModalState> = StatefulReactBuilder<EditorDialogProps<Int, Megrendeles>, MegrendelesModalState>().apply {


        getInitialState = { self ->
            createStateFromProp(self.props.primaryKey)
        }

        componentWillReceiveProps = { self, nextProps ->
            if (nextProps.primaryKey != self.props.primaryKey) {
                self.setState(createStateFromProp(nextProps.primaryKey))
            }
        }

        componentDidMount = { self ->
            MegrendelesStore.addChangeListener(this) {
                val megr = MegrendelesStore.megrendelesek[self.props.primaryKey]
                if (megr != null) {
                    self.setState(self.state.copy(
                            megrendeles = self.state.megrendeles.copy(
                                    akadalyok = megr.akadalyok,
                                    files = megr.files
                            )
                    ))
                }
            }
            jq(".modal-lg.modal-dialog").css("width", "1500px")
        }

        componentWillUnmount = {
            MegrendelesStore.removeListener(this)
            jq(window).asDynamic().unbind("scroll")
        }

        render = { self ->
            bsModal ({
                id = MegrendelesScreenIds.modal.id
                ref = "modal"
                onScroll = { e ->
                    if (self.state.importTextAreaVisible) {
                        val tmp0 = null
                        val y: Float = e.target.asDynamic().scrollTop
                        if (y < 1000) {
                            js("""tmp0 = {marginTop: (y) + "px"}""")
                            jq(self.refs["importTextArea"].refs.input).asDynamic().stop().animate(
                                    tmp0,
                                    "slow")
                        }
                    }
                }
                show = true
                key = self.props.primaryKey.toString()
                onHide = {
                    self.props.close(ModalResult.Close, null)
                }
                backdrop = "static"
                bsSize = BsSize.Large
                animation = false
            }) {
                bsModalHeader ({ closeButton = true }) {
                    bsModalTitle { text("Megrendelés - ${nvl(self.state.azonosito1, self.state.azonosito2)}") }
                }
                val comments: MutableMap<FieldDef<EditorDialogProps<Int, Megrendeles>, MegrendelesModalState>, FieldComments> = hashMapOf()
                collectComments(self, comments,
                        szamlaSorszamaField,
                        keszpenzesBefizetesField,
                        penzBeerkezettField,
                        megjegyzesField)
                body(self, comments)
                footer(self, comments)
            }
        }
    }.build("MegrendelesModal")

    private fun createStateFromProp(id: Int): MegrendelesModalState {
        val megrendeles = if (id == 0)
            Megrendeles(
                    megrendelo = SajatArStore.allMegrendelo.first(),
                    foVallalkozo = "Presting Zrt.",
                    munkatipus = Munkatipusok.Ertekbecsles.str,
                    ingatlanTipusMunkadijMeghatarozasahoz = SajatArStore.getSajatArakFor("Presting Zrt.", Munkatipusok.Ertekbecsles.str).firstOrNull()?.leiras ?: "",
                    hitelTipus = "Vásárlás",
                    ingatlanBovebbTipus = "lakás",
                    hatarido = getNextWeekDay(5),
                    statusz = Statusz.B1
            ) else MegrendelesStore.megrendelesek[id]!!.copy()
        val sameName = !megrendeles.ertesitesiNev.isNullOrEmpty() && megrendeles.ertesitesiNev == megrendeles.ugyfelNeve
        val sameTel = !megrendeles.ertesitesiTel.isNullOrEmpty() && megrendeles.ertesitesiTel == megrendeles.ugyfelTel

        val azonosito1: String
        val azonosito2: String
        if (megrendeles.munkatipus == Munkatipusok.EnergetikaAndErtekBecsles.str) {
            azonosito1 = megrendeles.getErtekbecslesId()
            azonosito2 = megrendeles.getEnergetikaId()
        } else if (megrendeles.munkatipus == Munkatipusok.Energetika.str) {
            azonosito1 = ""
            azonosito2 = megrendeles.azonosito
        } else {
            azonosito1 = megrendeles.azonosito
            azonosito2 = ""
        }
        val sajatArak = SajatArStore.getSajatArakFor(megrendeles.megrendelo, megrendeles.munkatipus)
        val sajatAr = sajatArak.firstOrNull { it.leiras == megrendeles.ingatlanTipusMunkadijMeghatarozasahoz }
        return MegrendelesModalState(
                megrendeles,
                originalMegrendeles = megrendeles.copy(),
                ertesitendoSzemelyAzonos = id == 0 || (sameName && sameTel),
                azonosito1 = azonosito1,
                azonosito2 = azonosito2,
                selectableAlvallalkozok = MegrendelesStore.getSelectableAlvallalkozok(megrendeles.regio),
                szamlazhatoDijAfa = sajatAr?.afa ?: null,
                currentTab = MegrendelesModalTab.AlapAdatok,
                selectableMunkatipusok = munkatipusokForRegio(megrendeles.regio)
        )
    }

    private fun <T> fillComments(comments: Map<FieldDef<EditorDialogProps<Int, Megrendeles>, MegrendelesModalState>, FieldComments>, block: () -> T): Pair<T, String> {
        val errorCount = comments.values.count { it.error != null }
        val warnCount = comments.values.count { it.warning != null }
        val successCount = comments.values.count { it.success != null }

        val ret = block()

        val errorCount2 = comments.values.count { it.error != null }
        val warnCount2 = comments.values.count { it.warning != null }
        val successCount2 = comments.values.count { it.success != null }

        val severeness = if (errorCount != errorCount2) {
            "red"
        } else if (warnCount != warnCount2) {
            "rgb(235, 148, 6)"
        } else if (successCount != successCount2) {
            "rgb(96, 196, 98)"
        } else {
            "white"
        }

        return Pair(ret, severeness)
    }

    private fun Component.body(self: ReactStatefulComponent<EditorDialogProps<Int, Megrendeles>, MegrendelesModalState>,
                               comments: MutableMap<FieldDef<EditorDialogProps<Int, Megrendeles>, MegrendelesModalState>, FieldComments>) {
        bsModalBody ({ closeButton = true }) {
            bsRow {
                bsCol({
                    md = if (self.props.primaryKey == 0 && self.state.currentTab == MegrendelesModalTab.AlapAdatok) {
                        if (self.state.importTextAreaVisible) 6 else 11
                    } else 12
                }) {
                    bsTabs({
                        animation = false
                        activeKey = self.state.currentTab.name
                        onSelect = { eventKey: Any ->
                            self.setState(self.state.copy(currentTab = MegrendelesModalTab.valueOf(eventKey as String)))
                        }
                    }) {
                        alapAdatokTabTitle(comments, self)
                        ingatlanAdataiTabTitle(comments, self)
                        akadalyokTabTitle(comments, self)
                        filesTabTitle(self, comments)
                        penzBeerkezettTabTitle(self)
                        feltoltesZarolasTabTitle(comments, self)
                    }
                }
                if (self.props.primaryKey == 0 && self.state.currentTab == MegrendelesModalTab.AlapAdatok) {
                    if (!self.state.importTextAreaVisible) {
                        bsCol({ md = if (self.state.importTextAreaVisible) 6 else 1 }) {
                            bsButton ({
                                id = MegrendelesScreenIds.modal.button.import
                                onClick = {
                                    self.setState(self.state.copy(
                                            importTextAreaVisible = true
                                    ))
                                }
                            }) { bsIcon("import"); text(" Importálás") }
                        }
                    } else {
                        bsCol({ md = 6 }) {
                            text("Importálni kívánt e-mail szövege:");br()
                            bsInput({
                                id = MegrendelesScreenIds.modal.input.importTextArea
                                type = InputType.Textarea
                                style = styleBuilder { height = "700px" }
                                value = self.state.importedText
                                ref = "importTextArea"
                                onChange = { e ->
                                    val newValue = e.currentTarget.value
                                    val importedData = parseImportedText(newValue)

                                    val newState = setImportedMegrendelesFields(self.state, importedData)
                                    val ertesitendoSzemelyAzonos = with(newState.megrendeles) {
                                        val sameName = !ertesitesiNev.isNullOrEmpty() && ertesitesiNev == ugyfelNeve
                                        val sameTel = !ertesitesiTel.isNullOrEmpty() && ertesitesiTel == ugyfelTel
                                        sameName && sameTel
                                    }

                                    val newAzon1 = if (importedData.ebAzonosito != null) importedData.ebAzonosito!! else self.state.azonosito1
                                    val newAzon2 = if (importedData.etAzonosito != null) importedData.etAzonosito!! else self.state.azonosito2
                                    self.setState(newState.copy(
                                            ertesitendoSzemelyAzonos = ertesitendoSzemelyAzonos,
                                            azonosito1 = newAzon1,
                                            azonosito2 = newAzon2,
                                            importedText = newValue,
                                            megrendelesFieldsFromImportText = importedData
                                    ))
                                }
                            })
                        }
                    }
                }
            }
        }
    }

    private fun setImportedMegrendelesFields(state: MegrendelesModalState,
                                             importedData: MegrendelesFieldsFromExternalSource): MegrendelesModalState {
        var modifiedState = state
        if (importedData.regio != null) {
            setNewRegio(modifiedState, modifiedState.megrendeles, importedData.regio!!) { newState ->
                modifiedState = newState
            }
        }
        if (importedData.hrsz != null) {
            modifiedState.megrendeles.hrsz = importedData.hrsz!!
        }
        if (importedData.irsz != null) {
            modifiedState.megrendeles.irsz = importedData.irsz!!
        }
        if (importedData.telepules != null) {
            modifiedState.megrendeles.telepules = importedData.telepules!!
        }
        if (importedData.ertekbecsles && importedData.energetika) {
            setMunkatipus(modifiedState, modifiedState.megrendeles, Munkatipusok.EnergetikaAndErtekBecsles.str) { newState ->
                modifiedState = newState
            }
        } else if (importedData.ertekbecsles) {
            setMunkatipus(modifiedState, modifiedState.megrendeles, Munkatipusok.Ertekbecsles.str) { newState ->
                modifiedState = newState
            }
        } else if (importedData.energetika) {
            setMunkatipus(modifiedState, modifiedState.megrendeles, Munkatipusok.Energetika.str) { newState ->
                modifiedState = newState
            }
        }
        if (importedData.hatarido != null) {
            modifiedState.megrendeles.hatarido = importedData.hatarido!!
        }
        if (importedData.ugyfelNeve != null) {
            modifiedState.megrendeles.ugyfelNeve = importedData.ugyfelNeve!!
        }
        if (importedData.ugyfelTelefonszama != null) {
            modifiedState.megrendeles.ugyfelTel = importedData.ugyfelTelefonszama!!
        }
        if (importedData.ertesitesiNev != null) {
            modifiedState.megrendeles.ertesitesiNev = importedData.ertesitesiNev!!
        }
        if (importedData.ertesitesiTel != null) {
            modifiedState.megrendeles.ertesitesiTel = importedData.ertesitesiTel!!
        }
        if (importedData.lakasCel != null) {
            modifiedState.megrendeles.hitelTipus = importedData.lakasCel!!
        }
        if (importedData.hitelOsszeg != null) {
            modifiedState.megrendeles.hitelOsszeg = importedData.hitelOsszeg!!
        }
        if (importedData.ajanlatSzam != null) {
            modifiedState.megrendeles.ajanlatSzam = importedData.ajanlatSzam!!
        }
        if (importedData.szerzodesSzam != null) {
            modifiedState.megrendeles.szerzodesSzam = importedData.szerzodesSzam!!
        }
        return modifiedState
    }

    private fun parseImportedText(text: String): MegrendelesFieldsFromExternalSource {
        val importedData = MegrendelesFieldsFromExternalSource()
        var lastAzon = ""
        text.lines().forEach { line ->
            if (line.contains("Energetikai tanúsítvány elkészítése")) {
                importedData.energetika = true
                lastAzon = "et"
            } else if (line.contains("Értékbecslés készítése")) {
                importedData.ertekbecsles = true
                lastAzon = "eb"
            } else if (line.contains("Azonosítója: ")) {
                val startIndex = line.indexOf("Azonosítója") + "Azonosítója: ".length
                if (lastAzon == "eb") {
                    importedData.ebAzonosito = line.substring(startIndex)
                } else {
                    importedData.etAzonosito = line.substring(startIndex)
                }
            } else if (line.contains("Határideje")) {
                val startIndex = line.indexOf("Határideje") + "Határideje: ".length
                val newHatarido = moment(line.substring(startIndex), "YYYY.MM.DD.")
                if (importedData.hatarido == null || newHatarido.isBefore(importedData.hatarido!!)) {
                    importedData.hatarido = newHatarido
                }
            } else if (line.startsWith("Hiteligénylő neve: ")) {
                importedData.ugyfelNeve = line.substring("Hiteligénylő neve: ".length)
            } else if (line.startsWith("Hiteligénylő telefonszáma: ")) {
                importedData.ugyfelTelefonszama = line.substring("Hiteligénylő telefonszáma: ".length)
            } else if (line.startsWith("Kapcsolattartó neve: ")) {
                importedData.ertesitesiNev = line.substring("Kapcsolattartó neve: ".length)
            } else if (line.startsWith("Kapcsolattartó telefonszáma: ")) {
                importedData.ertesitesiTel = line.substring("Kapcsolattartó telefonszáma: ".length)
            } else if (line.startsWith("Lakáscél: ")) {
                importedData.lakasCel = line.substring("Lakáscél: ".length).split(", ").getOrNull(0)
            } else if (line.startsWith("Felvenni kívánt hitel összege: ")) {
                importedData.hitelOsszeg = safeParseInt(line.substring("Felvenni kívánt hitel összege: ".length))
            } else if (line.startsWith("Ajánlatszám: ")) {
                val ajanlatSzamFromText = line.substring("Ajánlatszám: ".length)
                importedData.ajanlatSzam = if (importedData.ajanlatSzam == null) {
                    ajanlatSzamFromText
                } else {
                    "${importedData.ajanlatSzam};$ajanlatSzamFromText"
                }
            } else if (line.startsWith("Szerződésszám: ")) {
                val szerzodesSzamFromText = line.substring("Szerződésszám: ".length)
                importedData.szerzodesSzam = if (importedData.szerzodesSzam == null) {
                    szerzodesSzamFromText
                } else {
                    "${importedData.szerzodesSzam};$szerzodesSzamFromText"
                }
            } else if (line.startsWith("Ezúton küldjük Önnek megrendelésünket")) {
                // Ezúton küldjük Önnek megrendelésünket, 950/2 HRSZ számú, természetben 8181 Berhida, Kálvin tér 7. sz. alatt található ingatlannal kapcsolatban az alábbi feladat(ok) elvégzésére. A teljesítéshez szükséges dokumentumokat kérje az ügyféltől.
                val splittedLine = line.split(",")
                val splittedHrsz = splittedLine.getOrNull(1)?.split(" ")
                importedData.hrsz = splittedHrsz?.getOrNull(1)
                val splittedIrszTelepules = splittedLine.getOrNull(2)?.split(" ")
                importedData.irsz = splittedIrszTelepules?.getOrNull(2)
                importedData.telepules = splittedIrszTelepules?.getOrNull(3)
                importedData.regio = GeoStore.irszamok.firstOrNull { it.irszam == importedData.irsz }?.megye
            }
        }
        return importedData
    }

    private fun Component.penzBeerkezettTabTitle(self: ReactStatefulComponent<EditorDialogProps<Int, Megrendeles>, MegrendelesModalState>) {
        bsTab({
            id = MegrendelesScreenIds.modal.tab.penzBeerkezett
            title = tabTitle("Pénz beérkezett", icon = "usd")
            eventKey = MegrendelesModalTab.PenzBeerkezett.name
        }) {
            penzBeerkezettTabContent(self)
        }
    }

    private fun Component.filesTabTitle(self: ReactStatefulComponent<EditorDialogProps<Int, Megrendeles>, MegrendelesModalState>,
                                        comments: Map<FieldDef<EditorDialogProps<Int, Megrendeles>, MegrendelesModalState>, FieldComments>) {
        bsTab({ title = tabTitle("Fájlok", icon = "open", badgeNum = self.state.megrendeles.files.size); eventKey = MegrendelesModalTab.Fajlok.name }) {
            bsRow {
                bsCol({ xs = 3 }) {
                    bsPanel({
                        collapsible = false
                        bsStyle = BsStyle.Default
                        header = "Fájl feltöltés"
                    }) {
                        FileUploadTab({
                            megrendeles = self.state.megrendeles
                            width = 250
                        })
                    }
                }
                bsCol({ xs = 9 }) {
                    feltoltottFajlokTabContent(self, comments)
                }
            }
        }
    }

    private fun Component.alapAdatokTabTitle(comments: MutableMap<FieldDef<EditorDialogProps<Int, Megrendeles>, MegrendelesModalState>, FieldComments>, self: ReactStatefulComponent<EditorDialogProps<Int, Megrendeles>, MegrendelesModalState>) {
        val (alapAdatok, alapadatokTabColor) = fillComments(comments) {
            AlapAdatok(self, this, { self.refs }, { changeMegrendelesState(self, it) }, comments)
        }
        bsTab({
            id = MegrendelesScreenIds.modal.tab.first
            title = tabTitle("Alap adatok", color = alapadatokTabColor, icon = "list-alt")
            eventKey = MegrendelesModalTab.AlapAdatok.name
        }) {
            if (self.state.currentTab == MegrendelesModalTab.AlapAdatok) {
                val render = alapAdatok.render
                render()
            }
        }
    }

    private fun Component.ingatlanAdataiTabTitle(comments: MutableMap<FieldDef<EditorDialogProps<Int, Megrendeles>, MegrendelesModalState>, FieldComments>, self: ReactStatefulComponent<EditorDialogProps<Int, Megrendeles>, MegrendelesModalState>) {
        val (ingatlanAdatai, ingatlanAdataiColor) = fillComments(comments) {
            IngatlanAdataiTab(self, this, { changeMegrendelesState(self, it) }, comments)
        }
        bsTab({
            id = MegrendelesScreenIds.modal.tab.ingatlanAdatai
            title = tabTitle("Ingatlan adatai", color = ingatlanAdataiColor, icon = "home")
            eventKey = MegrendelesModalTab.ingatlanAdatai.name
        }) {
            if (self.state.currentTab == MegrendelesModalTab.ingatlanAdatai) {
                val render = ingatlanAdatai.render
                render()
            }
        }
    }

    private fun Component.akadalyokTabTitle(comments: MutableMap<FieldDef<EditorDialogProps<Int, Megrendeles>, MegrendelesModalState>, FieldComments>, self: ReactStatefulComponent<EditorDialogProps<Int, Megrendeles>, MegrendelesModalState>) {
        val akadalyok = self.state.megrendeles.akadalyok
        val akadalyokSize = Math.max(akadalyok.size, if (self.state.megrendeles.megjegyzes.isNotEmpty()) 1 else 0)
        bsTab({
            id = MegrendelesScreenIds.modal.tab.akadalyok
            title = tabTitle("Megjegyzés/Akadály", badgeNum = akadalyokSize, icon = "ban-circle")
            eventKey = MegrendelesModalTab.Akadalyok.name
        }) {
            akadalyokTabContent(self, akadalyok.sortedByDescending { it.rogzitve.format(dateTimeFormat) }.toTypedArray(), comments)
        }
    }

    private fun Component.feltoltesZarolasTabTitle(comments: MutableMap<FieldDef<EditorDialogProps<Int, Megrendeles>, MegrendelesModalState>, FieldComments>, self: ReactStatefulComponent<EditorDialogProps<Int, Megrendeles>, MegrendelesModalState>) {
        val (feltoltesZarolas, feltoltesZarolasTabColor) = fillComments(comments) {
            FeltoltesZarolasTab(self, this, { changeMegrendelesState(self, it) }, comments)
        }
        bsTab({
            id = MegrendelesScreenIds.modal.tab.feltoltesZarolas
            title = tabTitle("Feltöltés/Zárolás", color = feltoltesZarolasTabColor, icon = "check")
            eventKey = MegrendelesModalTab.FeltoltesZarolas.name
        }) {
            if (self.state.currentTab == MegrendelesModalTab.FeltoltesZarolas) {
                val render = feltoltesZarolas.render
                render()
            }
        }
    }

    private fun Component.akadalyokTabContent(self: ReactStatefulComponent<EditorDialogProps<Int, Megrendeles>, MegrendelesModalState>,
                                              akadalyok: Array<Akadaly>, comments: MutableMap<FieldDef<EditorDialogProps<Int, Megrendeles>, MegrendelesModalState>, FieldComments>) {
        bsRow {
            bsCol({ md = 12 }) {
                akadalyokTable(akadalyok)
            }
        }
        br()
        bsRow {
            bsCol({ md = 6 }) {
                bsPanel({
                    collapsible = true
                    bsStyle = BsStyle.Default
                    header = "Akadály közlése"
                    defaultExpanded = false
                }) {
                    AkadalyKozles({
                        this.jelenlegiHatarido = self.state.megrendeles.hatarido
                        this.comments = comments.asDynamic()
                        this.onAkadalyKozles = { akadalyReason, ujHatarido, akadalySzoveg ->
                            communicator.getEntityFromServer<dynamic>(RestUrl.akadalyKozles,
                                    object {
                                        val megrendelesId = self.state.megrendeles.id
                                        val ujHatarido = ujHatarido
                                        val akadalyOka = akadalyReason.text
                                        val szoveg = akadalySzoveg
                                    }) { response ->
                                Actions.megrendelesekFromServer(response)
                                Actions.notification(Notification(BsStyle.Success, "Akadály rögztíve"))
                            }
                        }
                    })
                }
            }
            bsCol({ md = 6 }) {
                val headerText = "Megjegyzés" + if (self.state.megrendeles.megjegyzes.isNotEmpty()) " (*)" else ""
                bsPanel({
                    collapsible = true
                    bsStyle = BsStyle.Default
                    header = headerText
                    defaultExpanded = false
                }) {
                    field(self, megjegyzesField, comments)
                }
            }
        }
    }

    private val megjegyzesField = FieldDef<EditorDialogProps<Int, Megrendeles>, MegrendelesModalState>("Megjegyzés",
            input = { self, labelText, comments ->
                bsInputWithComments(comments, {
                    id = MegrendelesScreenIds.modal.input.megjegyzes
                    type = InputType.Textarea
                    label = labelText
                    style = styleBuilder { height = "200px" }
                    value = self.state.megrendeles.megjegyzes
                    onChange = { event ->
                        val value = event.currentTarget.value
                        changeMegrendelesState(self) { copy(megjegyzes = value) }
                    }
                })
            },
            error = { self ->
                lineSeparatedErrorMessagesOrNull(self.state.megrendeles.megjegyzes, Max(255))
            }
    )

    private fun Component.penzBeerkezettTabContent(self: ReactStatefulComponent<EditorDialogProps<Int, Megrendeles>, MegrendelesModalState>) {
        if (self.state.currentTab == MegrendelesModalTab.PenzBeerkezett) {
            bsPanel({
                collapsible = true
                bsStyle = BsStyle.Default
                header = "Számla"
                defaultExpanded = true
            }) {
                val comments = hashMapOf<FieldDef<EditorDialogProps<Int, Megrendeles>, MegrendelesModalState>, FieldComments>()
                bsRow {
                    bsCol({ md = 4 }) { field(self, keszpenzesBefizetesField, comments) }
                    bsCol({ md = 4 }) { field(self, penzBeerkezettField, comments) }
                }
                bsRow {
                    bsCol({ md = 4 }) { field(self, szamlaSorszamaField, comments) }
                }
            }
        }
    }

    private fun Component.feltoltottFajlokTabContent(self: ReactStatefulComponent<EditorDialogProps<Int, Megrendeles>, MegrendelesModalState>,
                                                     comments: Map<FieldDef<EditorDialogProps<Int, Megrendeles>, MegrendelesModalState>, FieldComments>) {
        bsRow {
            bsPanel({
                collapsible = false
                bsStyle = BsStyle.Default
                header = "Feltöltött fájlok"
            }) {
                bsRow {
                    bsCol({ md = 6 }) {
                        bsRow {
                            bsCol({ md = 6 }) { field(self, vallalkozoFeltoltotteField, hashMapOf()) }
                            bsCol({ md = 6 }) { field(self, energetikaFeltoltveField, hashMapOf()) }
                        }
                        FeltoltottFajlokTable({
                            megrendeles = self.state.megrendeles
                            onParseExcel = { excel, megr ->
                                self.setState(self.state.copy(
                                        megrendelesFieldsFromExcel = excel,
                                        megrendeles = megr
                                ))
                            }
                        })

                    }
                    if (self.state.megrendelesFieldsFromExcel != null) {
                        bsCol({ md = 6 }) {
                            bsPanel({
                                collapsible = false
                                bsStyle = BsStyle.Default
                                header = "Excel adatok betöltésének eredménye"
                            }) {
                                dataFromExcelTable(self.state.megrendeles, self.state.megrendelesFieldsFromExcel!!)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun Component.dataFromExcelTable(megr: Megrendeles, excel: MegrendelesFieldsFromExternalSource) {
        bsCol({ md = 12 }) {
            bootstrapTable<OldNewValues>({
                data = arrayOf(
                        OldNewValues("Helyszínelő", excel.helyszinelo ?: "", megr.helyszinelo ?: ""),
                        OldNewValues("Eladási ár", excel.adasvetel ?: "", megr.eladasiAr?.toString() ?: ""),
                        OldNewValues("adasvetelDatuma", excel.adasvetelDatuma ?: "", megr.adasvetelDatuma?.format(dateFormat) ?: ""),
                        OldNewValues("Értékbecslő", excel.ertekBecslo ?: "", AlvallalkozoStore.ertekbecslok[megr.ertekbecsloId]?.name ?: ""),
                        oldNewValues("Fajlagos becsült ár", excel.fajlagosAr?.toInt(), megr.fajlagosBecsultAr),
                        oldNewValues("Becsült érték", excel.forgalmiErtek?.toInt(), megr.becsultErtek),
                        OldNewValues("Hrsz", excel.hrsz ?: "", megr.hrsz),
                        oldNewValues("Lakás terület", excel.ingatlanTerulet?.toInt(), megr.lakasTerulet),
                        OldNewValues("Lakás típusa", excel.ingatlanTipusa?.toLowerCase() ?: "", megr.ingatlanBovebbTipus.toLowerCase()),
                        oldNewValues("Készültségi fok", excel.keszultsegiFok?.toInt(), megr.keszultsegiFok),
                        OldNewValues("Kapcsolattartó neve", excel.kolcsonIgenylo ?: "", megr.ugyfelNeve),
                        OldNewValues("Szemle időpontja", excel.szemleIdopontja ?: "", megr.szemleIdopontja?.format(dateFormat) ?: ""),
                        oldNewValues("Telek terület", excel.telekMeret?.toInt(), megr.telekTerulet)
                )
                hover = true
                selectRow = SelectRowProp(
                        SelectionMode.radio,
                        clickToSelect = true,
                        hideSelectColumn = true
                )
                search = false
                pagination = false
                condensed = true
            }) {
                keyColumn("Név", "name", 0)
                column("Név", "name", 10, renderer = { cell, row ->
                    val row = row as OldNewValues
                    val c = if (row.oldValue != row.newValue && row.oldValue.isNotEmpty()) {
                        "rgb(235, 148, 6)"
                    } else {
                        "white"
                    }
                    createReactElement {
                        span({ style = styleBuilder { color = c } }) {
                            text(cell as String)
                        }
                    }
                })
                column("Új érték", "newValue", 20, dataAlign = DataAlign.Right, renderer = { cell, row -> monospaceFontRenderer(cell, row) })
            }
        }
    }

    private val vallalkozoFeltoltotteField = FieldDef<EditorDialogProps<Int, Megrendeles>, MegrendelesModalState>("Vállalkozó feltöltötte a kész anyagot",
            input = { self, labelText, comments ->
                bsInputWithComments(comments, {
                    type = InputType.Text
                    id = MegrendelesScreenIds.modal.input.vallalkozoFeltoltotteAKeszAnyagot
                    label = labelText
                    disabled = true
                    value = self.state.megrendeles.ertekbecslesFeltoltve?.format(dateTimeFormat).orEmpty()
                })
            }
    )

    private val energetikaFeltoltveField = FieldDef<EditorDialogProps<Int, Megrendeles>, MegrendelesModalState>("Energetika feltöltése",
            input = { self, labelText, comments ->
                bsInputWithComments(comments, {
                    type = InputType.Text
                    id = MegrendelesScreenIds.modal.input.energetikaFeltoltese
                    label = labelText
                    disabled = true
                    value = self.state.megrendeles.energetikaFeltoltve?.format(dateTimeFormat).orEmpty()
                })
            }
    )


    private fun changeMegrendelesState(self: ReactStatefulComponent<EditorDialogProps<Int, Megrendeles>, MegrendelesModalState>,
                                       copyBody: Megrendeles.() -> Megrendeles) {
        self.setState(self.state.copy(megrendeles = self.state.megrendeles.copyBody()))
    }

    private fun Component.footer(self: ReactStatefulComponent<EditorDialogProps<Int, Megrendeles>, MegrendelesModalState>,
                                 comments: Map<FieldDef<EditorDialogProps<Int, Megrendeles>, MegrendelesModalState>, FieldComments>) {
        bsModalFooter {
            bsRow {
                if (self.props.primaryKey != 0) {
                    bsCol({ md = 3 }) {
                        bsButtonGroup({ vertical = true }) {
                            bsButton ({
                                onClick = {
                                    val msg = object {
                                        val id = self.state.megrendeles.id
                                    }
                                    communicator.getEntityFromServer<Any>(RestUrl.emailKuldeseUjra, msg) {
                                        Actions.notification(Notification(BsStyle.Success, "E-mail elküldve"))
                                    }
                                }
                            }) { bsIcon("envelope"); text(" Email küldése újra") }
                        }
                    }
                }
                bsCol({ mdOffset = 5; md = 4 }) {
                    bsRow {
                        bsCol {
                            if (self.props.primaryKey == 0) {
                                field(self, ellenorizveField, comments)
                            }
                            bsButtonGroup {
                                saveButton(self, comments)
                                cancelButton(self.props)
                            }
                        }
                    }
                }
            }
        }
    }

    private val ellenorizveField = FieldDef<EditorDialogProps<Int, Megrendeles>, MegrendelesModalState>("E-mail küldése az alvállalkozónak",
            input = { self, labelText, comments ->
                bsInputWithComments(comments, {
                    type = InputType.Checkbox
                    checked = !self.state.megrendeles.ellenorizve
                    label = labelText
                    onChange = { event ->
                        val checked: Boolean = event.currentTarget.asDynamic().checked
                        changeMegrendelesState(self) { copy(ellenorizve = !checked) }
                    }
                })
            }
    )

    private val szamlaSorszamaField = FieldDef<EditorDialogProps<Int, Megrendeles>, MegrendelesModalState>("Számla sorszáma",
            input = { self, labelText, comments ->
                bsInputWithComments(comments, {
                    id = MegrendelesScreenIds.modal.input.szamlaSorszama
                    type = InputType.Text
                    label = labelText
                    value = self.state.megrendeles.szamlaSorszama
                    onChange = { event ->
                        val value = event.currentTarget.value
                        changeMegrendelesState(self) { copy(szamlaSorszama = value) }
                    }
                })
            },
            error = { self ->
                lineSeparatedErrorMessagesOrNull(self.state.megrendeles.szamlaSorszama, Max(50))
            }
    )

    private val keszpenzesBefizetesField = FieldDef<EditorDialogProps<Int, Megrendeles>, MegrendelesModalState>("Készpénzes befizetés",
            input = { self, labelText, comments ->
                ToggleDate({
                    id = MegrendelesScreenIds.modal.input.szamlaKifizetese
                    label = labelText
                    date = self.state.megrendeles.keszpenzesBefizetes
                    onChangeCallback = { date ->
                        changeMegrendelesState(self) { copy(keszpenzesBefizetes = date) }
                    }
                })
            }
    )

    private val penzBeerkezettField = FieldDef<EditorDialogProps<Int, Megrendeles>, MegrendelesModalState>("Pénz beérkezett számlára",
            input = { self, labelText, comments ->
                ToggleDate({
                    id = MegrendelesScreenIds.modal.input.penzBeerkezettSzamlara
                    label = labelText
                    date = self.state.megrendeles.penzBeerkezettDatum
                    onChangeCallback = { date ->
                        changeMegrendelesState(self) { copy(penzBeerkezettDatum = date) }
                    }
                })
            }
    )

    private fun Component.cancelButton(props: EditorDialogProps<Int, Megrendeles>) {
        bsButton ({
            id = MegrendelesScreenIds.modal.button.close
            bsStyle = BsStyle.Danger
            onClick = { props.close(ModalResult.Close, null) }
        }) { text("Mégsem") }
    }

    private fun Component.saveButton(self: ReactStatefulComponent<EditorDialogProps<Int, Megrendeles>, MegrendelesModalState>,
                                     comments: Map<FieldDef<EditorDialogProps<Int, Megrendeles>, MegrendelesModalState>, FieldComments>) {
        val d = comments.hasError()
        bsButton ({
            id = MegrendelesScreenIds.modal.button.save
            bsStyle = BsStyle.Success
            disabled = d
            onClick = {
                if (!d) {
                    val savedMegr = self.state.megrendeles
                    savedMegr.azonosito = createAzonosito(self.state)
                    if (self.state.ertesitendoSzemelyAzonos) {
                        savedMegr.ertesitesiNev = savedMegr.ugyfelNeve
                        savedMegr.ertesitesiTel = savedMegr.ugyfelTel
                    }
                    val sajatAr = SajatArStore.getSajatArakFor(savedMegr.megrendelo, savedMegr.munkatipus).first { it.leiras == savedMegr.ingatlanTipusMunkadijMeghatarozasahoz }
                    savedMegr.afa = sajatAr.afa
                    self.props.close(ModalResult.Save, savedMegr)
                }
            }
        }) { text("Mentés") }
    }

    private fun createAzonosito(state: MegrendelesModalState): String {
        return if (state.megrendeles.munkatipus == Munkatipusok.EnergetikaAndErtekBecsles.str) {
            "EB${state.azonosito1}_ET${state.azonosito2}"
        } else if (state.megrendeles.munkatipus == Munkatipusok.Energetika.str) {
            state.azonosito2
        } else {
            state.azonosito1
        }
    }
}

fun Component.akadalyokTable(akadalyok: Array<Akadaly>) {
    bootstrapTable<Akadaly>({
        data = akadalyok
        hover = true
        selectRow = SelectRowProp(
                SelectionMode.radio,
                clickToSelect = true,
                hideSelectColumn = true
        )
        search = false
        pagination = false
        condensed = true
    }) {
        keyColumn("ID", "id", 0)
        dateTimeColumn("Rögzítve", "rogzitve", 10)
        dateColumn("Új határidő", "ujHatarido", 7)
        column("Státusz", "statusz", 15, renderer = { cell, row ->
            (cell as Statusz?)?.text ?: ""
        })
        column("Leírás", "leiras", 30, renderer = { cell, row ->
            createReactElement {
                bsOverlayTrigger({
                    rootClose = true
                    trigger = BsTrigger.Click
                    placement = BsPlacement.Top
                    overlay = createReactElement {
                        bsPopover({
                            this.title = "Akadály"
                        }) {
                            span { this.text(cell as String) }
                        }
                    }
                })
                {
                    span { text(cell as String) }
                }
            }
        })
    }
}

fun tabTitle(text: String, icon: String? = null, badgeNum: Int? = null, color: String? = null) = createReactElement {
    div {
        if (icon != null) {
            bsIcon(icon)
        }
        colorText(color ?: "white", " $text")
        if (badgeNum != null) {
            bsBadge { text(badgeNum.toString()) }
        }
    }
}

fun Component.MegrendelesModal(
        properties: EditorDialogProps<Int, Megrendeles>.() -> Unit = {},
        init: Component.() -> Unit = {}) {
    externalReactClass<EditorDialogProps<Int, Megrendeles>>(
            MegrendelesModal.component,
            Ref(initProps(EditorDialogProps<Int, Megrendeles>(), properties)).asDynamic(),
            init)
}