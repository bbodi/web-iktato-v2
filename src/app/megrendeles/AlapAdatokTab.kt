package hu.nevermind.utils.app.megrendeles

import app.AppState
import app.Dispatcher
import app.common.Granularity
import app.common.Moment
import app.common.moment
import app.megrendeles.MegrendelesFormState
import app.megrendeles.MegrendelesScreenIds
import app.useEffect
import app.useState
import hu.nevermind.antd.*
import hu.nevermind.antd.autocomplete.AutoComplete
import hu.nevermind.utils.app.DefinedReactComponent
import hu.nevermind.utils.hu.nevermind.antd.StringOrReactElement
import hu.nevermind.utils.jsStyle
import hu.nevermind.utils.store.*
import react.RBuilder
import react.RElementBuilder
import react.children
import react.dom.div
import react.dom.jsStyle
import react.dom.span
import store.kozteruletJellegek
import store.megyek
import kotlin.math.roundToLong

data class AlapAdatokTabParams(val megrendeles: Megrendeles,
                               val importedTextChanged: Moment?,
                               val importedText: String,
                               val megrendelesFieldsFromExcel: MegrendelesFieldsFromExternalSource?,
                               val appState: AppState,
                               val onSaveFunctions: Array<(Megrendeles) -> Megrendeles>,
                               val setState: Dispatcher<MegrendelesFormState>)

data class AlapAdatokTabComponentState(val megrendeles: Megrendeles,
                                       val importedTextChanged: Moment,
                                       val szamlazhatoDijAfa: Int?,
                                       val azonosito1: String,
                                       val azonosito2: String,
                                       val selectableMunkatipusok: Collection<String>,
                                       val selectableAlvallalkozok: Collection<Alvallalkozo>,
                                       val megrendelesFieldsFromImportText: MegrendelesFieldsFromExternalSource? = null,
                                       val ertesitendoSzemelyAzonos: Boolean)

object AlapAdatokTabComponent : DefinedReactComponent<AlapAdatokTabParams>() {
    override fun RBuilder.body(props: AlapAdatokTabParams) {
        val (tabState, setTabState) = useState {
            val megrendeles = props.megrendeles
            val sameName = !megrendeles.ertesitesiNev.isNullOrEmpty() && megrendeles.ertesitesiNev == megrendeles.ugyfelNeve
            val sameTel = !megrendeles.ertesitesiTel.isNullOrEmpty() && megrendeles.ertesitesiTel == megrendeles.ugyfelTel
            val ertesitendoSzemelyAzonos = megrendeles.id == 0 || (sameName && sameTel)
            val (azonosito1: String, azonosito2: String) = determineAzonositok(megrendeles)
            val sajatArak = props.appState.sajatArState.getSajatArakFor(megrendeles.megrendelo, megrendeles.munkatipus)
            val sajatAr = sajatArak.firstOrNull { it.leiras == megrendeles.ingatlanTipusMunkadijMeghatarozasahoz }
            AlapAdatokTabComponentState(megrendeles.copy(),
                    szamlazhatoDijAfa = sajatAr?.afa,
                    azonosito1 = azonosito1,
                    azonosito2 = azonosito2,
                    selectableAlvallalkozok = props.appState.alvallalkozoState.getSelectableAlvallalkozok(megrendeles.regio),
                    selectableMunkatipusok = munkatipusokForRegio(props.appState.alvallalkozoState, megrendeles.regio),
                    ertesitendoSzemelyAzonos = ertesitendoSzemelyAzonos,
                    importedTextChanged = moment()
            )
        }
        val formWasUpdatedByOtherTab = tabState.megrendeles.modified.isBefore(props.megrendeles.modified)
        if (formWasUpdatedByOtherTab) {
            val (azonosito1: String, azonosito2: String) = determineAzonositok(props.megrendeles)
            setTabState(tabState.copy(
                    megrendeles = overwriteTabState(tabState.megrendeles, props.megrendeles).copy(modified = moment()),
                    azonosito1 = azonosito1.ifEmpty { tabState.azonosito1 },
                    azonosito2 = azonosito2.ifEmpty { tabState.azonosito2 }
            ))
        }
        if (props.importedTextChanged != null) {
            val importEmailTextChanged = tabState.importedTextChanged.isBefore(props.importedTextChanged)
            if (importEmailTextChanged) {
                val importedData = parseImportedText(props.importedText, props.appState.geoData.irszamok)

                val newState = setImportedMegrendelesFields(
                        tabState,
                        props.appState,
                        importedData
                )
                val ertesitendoSzemelyAzonos = with(newState.megrendeles) {
                    val sameName = !ertesitesiNev.isNullOrEmpty() && ertesitesiNev == ugyfelNeve
                    val sameTel = !ertesitesiTel.isNullOrEmpty() && ertesitesiTel == ugyfelTel
                    sameName && sameTel
                }

                val newAzon1 = if (importedData.ebAzonosito != null) importedData.ebAzonosito!! else newState.azonosito1
                val newAzon2 = if (importedData.etAzonosito != null) importedData.etAzonosito!! else newState.azonosito2
                setTabState(newState.copy(
                        ertesitendoSzemelyAzonos = ertesitendoSzemelyAzonos,
                        importedTextChanged = moment(),
                        azonosito1 = newAzon1,
                        azonosito2 = newAzon2,
                        megrendelesFieldsFromImportText = importedData
                ))
            }
        }
        useEffect {
            props.onSaveFunctions[0] = { globalMegrendeles ->
                overwriteTabState(globalMegrendeles, tabState.megrendeles).copy(
                        azonosito = createAzonosito(tabState)
                )
            }
        }
        Collapse {
            attrs.bordered = false
            attrs.defaultActiveKey = arrayOf("Megrendelés", "Ügyfél", "Értesítendő személy", "Cím", "Hitel")
            Panel("Megrendelés") {
                attrs.header = StringOrReactElement.fromString("Megrendelés")
                megrendelesPanel(props.appState, tabState, setTabState, props.megrendelesFieldsFromExcel)
            }
            Panel("Ügyfél") {
                attrs.header = StringOrReactElement.fromString("Ügyfél")
                ugyfelPanel(tabState, setTabState, props.megrendelesFieldsFromExcel)
            }
            Panel("Értesítendő személy") {
                attrs.header = StringOrReactElement.fromString("Értesítendő személy")
                ertesitendoSzemelyPanel(tabState, setTabState)
            }
            Panel("Cím") {
                attrs.header = StringOrReactElement.fromString("Cím")
                cimPanel(props.appState, tabState, setTabState, props.megrendelesFieldsFromExcel)
            }
            Panel("Hitel") {
                attrs.header = StringOrReactElement.fromString("Hitel")
                hitelPanel(tabState, setTabState)
            }
        }
    }

    private fun setImportedMegrendelesFields(state: AlapAdatokTabComponentState,
                                             appState: AppState,
                                             importedData: MegrendelesFieldsFromExternalSource): AlapAdatokTabComponentState {
        var modifiedState = state
        if (importedData.regio != null) {
            setNewRegio(appState, modifiedState, modifiedState.megrendeles, importedData.regio!!) { newState ->
                modifiedState = newState
            }
        }
        if (importedData.hrsz != null) {
            modifiedState = modifiedState.copy(megrendeles = modifiedState.megrendeles.copy(
                    hrsz = importedData.hrsz!!
            ))
        }
        if (importedData.irsz != null) {
            modifiedState = modifiedState.copy(megrendeles = modifiedState.megrendeles.copy(
                    irsz = importedData.irsz!!
            ))
        }
        if (importedData.telepules != null) {
            modifiedState = modifiedState.copy(megrendeles = modifiedState.megrendeles.copy(
                    telepules = importedData.telepules!!
            ))
        }
        if (importedData.ertekbecsles && importedData.energetika) {
            setMunkatipus(appState, modifiedState, modifiedState.megrendeles, Munkatipusok.EnergetikaAndErtekBecsles.str) { newState ->
                modifiedState = newState
            }
        } else if (importedData.ertekbecsles) {
            setMunkatipus(appState, modifiedState, modifiedState.megrendeles, Munkatipusok.Ertekbecsles.str) { newState ->
                modifiedState = newState
            }
        } else if (importedData.energetika) {
            setMunkatipus(appState, modifiedState, modifiedState.megrendeles, Munkatipusok.Energetika.str) { newState ->
                modifiedState = newState
            }
        }
        if (importedData.hatarido != null) {
            modifiedState = modifiedState.copy(megrendeles = modifiedState.megrendeles.copy(
                    hatarido = importedData.hatarido!!
            ))
        }
        if (importedData.ugyfelNeve != null) {
            modifiedState = modifiedState.copy(megrendeles = modifiedState.megrendeles.copy(
                    ugyfelNeve = importedData.ugyfelNeve!!
            ))
        }
        if (importedData.ugyfelTelefonszama != null) {
            modifiedState = modifiedState.copy(megrendeles = modifiedState.megrendeles.copy(
                    ugyfelTel = importedData.ugyfelTelefonszama!!
            ))
        }
        if (importedData.ertesitesiNev != null) {
            modifiedState = modifiedState.copy(megrendeles = modifiedState.megrendeles.copy(
                    ertesitesiNev = importedData.ertesitesiNev!!
            ))
        }
        if (importedData.ertesitesiTel != null) {
            modifiedState = modifiedState.copy(megrendeles = modifiedState.megrendeles.copy(
                    ertesitesiTel = importedData.ertesitesiTel!!
            ))
        }
        if (importedData.lakasCel != null) {
            modifiedState = modifiedState.copy(megrendeles = modifiedState.megrendeles.copy(
                    hitelTipus = importedData.lakasCel!!
            ))
        }
        if (importedData.hitelOsszeg != null) {
            modifiedState = modifiedState.copy(megrendeles = modifiedState.megrendeles.copy(
                    hitelOsszeg = importedData.hitelOsszeg
            ))
        }
        if (importedData.ajanlatSzam != null) {
            modifiedState = modifiedState.copy(megrendeles = modifiedState.megrendeles.copy(
                    ajanlatSzam = importedData.ajanlatSzam!!
            ))
        }
        if (importedData.szerzodesSzam != null) {
            modifiedState = modifiedState.copy(megrendeles = modifiedState.megrendeles.copy(
                    szerzodesSzam = importedData.szerzodesSzam!!
            ))
        }
        modifiedState = modifiedState.copy(megrendeles = modifiedState.megrendeles.copy(
                azonosito = importedData.ebAzonosito ?: importedData.etAzonosito ?: ""
        ))
        return modifiedState
    }

    private fun parseImportedText(text: String, irszamok: List<Irszam>): MegrendelesFieldsFromExternalSource {
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
                importedData.hitelOsszeg = line.substring("Felvenni kívánt hitel összege: ".length).toIntOrNull()
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
                importedData.regio = irszamok.firstOrNull { it.irszam == importedData.irsz }?.megye
            }
        }
        return importedData
    }

    private fun determineAzonositok(megrendeles: Megrendeles): Pair<String, String> {
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
        return Pair(azonosito1, azonosito2)
    }
}

private fun overwriteTabState(base: Megrendeles, overwriteWith: Megrendeles): Megrendeles {
    return base.copy(
            megrendelo = overwriteWith.megrendelo,
            regio = overwriteWith.regio,
            munkatipus = overwriteWith.munkatipus,
            ingatlanTipusMunkadijMeghatarozasahoz = overwriteWith.ingatlanTipusMunkadijMeghatarozasahoz,
            alvallalkozoId = overwriteWith.alvallalkozoId,
            ertekbecsloId = overwriteWith.ertekbecsloId,
            ertekbecsloDija = overwriteWith.ertekbecsloDija,
            szamlazhatoDij = overwriteWith.szamlazhatoDij,
            foVallalkozo = overwriteWith.foVallalkozo,
            megrendelve = overwriteWith.megrendelve,
            hatarido = overwriteWith.hatarido,
            ugyfelNeve = overwriteWith.ugyfelNeve,
            ugyfelTel = overwriteWith.ugyfelTel,
            ugyfelEmail = overwriteWith.ugyfelEmail,
            ertesitesiNev = overwriteWith.ertesitesiNev,
            ertesitesiTel = overwriteWith.ertesitesiTel,
            hrsz = overwriteWith.hrsz,
            irsz = overwriteWith.irsz,
            telepules = overwriteWith.telepules,
            kerulet = overwriteWith.kerulet,
            utcaJelleg = overwriteWith.utcaJelleg,
            utca = overwriteWith.utca,
            hazszam = overwriteWith.hazszam,
            lepcsohaz = overwriteWith.lepcsohaz,
            emelet = overwriteWith.emelet,
            ajto = overwriteWith.ajto,
            hitelTipus = overwriteWith.hitelTipus,
            hitelOsszeg = overwriteWith.hitelOsszeg,
            ajanlatSzam = overwriteWith.ajanlatSzam,
            szerzodesSzam = overwriteWith.szerzodesSzam
    )
}


private fun RElementBuilder<PanelProps>.cimPanel(
        appState: AppState,
        tabState: AlapAdatokTabComponentState,
        setTabState: Dispatcher<AlapAdatokTabComponentState>,
        excel: MegrendelesFieldsFromExternalSource?) {
    Form {
        Row {
            Col(span = 7) {
                FormItem {
                    attrs.label = StringOrReactElement.fromString("Helyrajzi szám")
                    val beillesztett = beilesztettSzovegbolImportalva(
                            tabState.megrendelesFieldsFromImportText?.hrsz?.let {
                                it == tabState.megrendeles.hrsz
                            } ?: false
                    )
                    if (!beillesztett) {
                        addExcelbolBetoltveMessages(tabState.megrendeles.ugyfelNeve, excel?.ugyfelNeve)
                    }
                    Input {
                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.helyrajziSzam
                        attrs.value = tabState.megrendeles.hrsz
                        attrs.onChange = { e ->
                            setTabState(tabState.copy(megrendeles = tabState.megrendeles.copy(hrsz = e.target.asDynamic().value as String?
                                    ?: "")))
                        }
                    }
                }
            }
            Col(offset = 1, span = 7) {
                FormItem {
                    attrs.label = StringOrReactElement.fromString("Irányítószám")
                    val helpMsg = tabState.megrendeles.irsz.let { inputIrsz ->
                        if (inputIrsz.isNullOrEmpty()) {
                            null
                        } else {
                            val irsz = appState.geoData.irszamok.firstOrNull { it.irszam == inputIrsz }
                            if (irsz == null) {
                                "Nem létezik ilyen irányítószám az adatbázisban!"
                            } else if (irsz.megye != tabState.megrendeles.regio) {
                                "A megadott irányítószám nem létezik a kiválasztott régióban!"
                            } else if (irsz.telepules != tabState.megrendeles.telepules) {
                                "A megadott irányítószám nem létezik a kiválasztott településen!"
                            } else {
                                null
                            }
                        }
                    }
                    attrs.validateStatus = if (helpMsg != null) ValidateStatus.warning else null
                    attrs.hasFeedback = helpMsg != null
                    attrs.help = if (helpMsg != null) StringOrReactElement.fromString(helpMsg) else null
                    if (helpMsg == null) {
                        beilesztettSzovegbolImportalva(
                                tabState.megrendelesFieldsFromImportText?.irsz?.let {
                                    it == tabState.megrendeles.irsz
                                } ?: false
                        )
                    }
                    val source: Array<Any> = appState.geoData.irszamok.let { irszamok ->
                        val unknownRegio = tabState.megrendeles.regio !in megyek
                        if (unknownRegio) {
                            irszamok
                        } else {
                            irszamok.filter { it.megye == tabState.megrendeles.regio }
                        }.map { it.irszam }
                    }.filter { it.isNotEmpty() }.distinct().toTypedArray()
                    AutoComplete(source) {
                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.iranyitoszam
                        attrs.value = tabState.megrendeles.irsz
                        attrs.placeholder = "Irányítószám"
                        attrs.filterOption = { inputString, optionElement ->
                            if (inputString.length < 2) false else
                                (optionElement.props.children as String).toUpperCase().replace(" ", "").contains(inputString.toUpperCase().replace(" ", ""))
                        }
                        attrs.onChange = { value ->
                            val irsz = appState.geoData.irszamok.firstOrNull { it.irszam == value }
                            if (irsz != null) {
                                setTabState(tabState.copy(megrendeles = tabState.megrendeles.copy(
                                        irsz = value,
                                        telepules = irsz.telepules
                                )))
                            } else {
                                setTabState(tabState.copy(megrendeles = tabState.megrendeles.copy(irsz = value)))
                            }
                        }
                    }
                }
            }
            Col(offset = 1, span = 7) {
                FormItem {
                    attrs.label = StringOrReactElement.fromString("Település")
                    val helpMsg = tabState.megrendeles.irsz.let { inputIrsz ->
                        val inputTelepules = tabState.megrendeles.telepules
                        if (inputTelepules.isNullOrEmpty() || inputIrsz.isNullOrEmpty()) {
                            null
                        } else {
                            val telepules = appState.geoData.irszamok.firstOrNull { it.telepules == tabState.megrendeles.telepules }
                            val telepulesWithIrszam = appState.geoData.irszamok.firstOrNull { it.telepules == tabState.megrendeles.telepules && it.irszam == tabState.megrendeles.irsz }
                            if (telepules == null) {
                                "Nem létezik ilyen település az adatbázisban!"
                            } else if (telepulesWithIrszam == null) {
                                "A megadott irányítószám nem található a településen!"
                            } else {
                                null
                            }
                        }
                    }
                    attrs.validateStatus = if (helpMsg != null) ValidateStatus.warning else null
                    attrs.hasFeedback = helpMsg != null
                    attrs.help = if (helpMsg != null) StringOrReactElement.fromString(helpMsg) else null
                    if (helpMsg == null) {
                        beilesztettSzovegbolImportalva(
                                tabState.megrendelesFieldsFromImportText?.telepules?.let {
                                    it == tabState.megrendeles.telepules
                                } ?: false
                        )
                    }
                    val source: Array<Any> = appState.geoData.irszamok.let { irszamok ->
                        if (tabState.megrendeles.irsz.isNullOrEmpty()) {
                            irszamok
                        } else {
                            irszamok.filter { it.irszam == tabState.megrendeles.irsz }
                        }.map { it.telepules }.distinct()
                    }.filter { it.isNotEmpty() }.distinct().toTypedArray()
                    AutoComplete(source) {
                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.telepules
                        attrs.value = tabState.megrendeles.telepules
                        attrs.placeholder = "Település"
                        attrs.filterOption = { inputString, optionElement ->
                            if (inputString.length < 3) false else
                                (optionElement.props.children as String).toUpperCase().replace(" ", "").contains(inputString.toUpperCase().replace(" ", ""))
                        }
                        attrs.onChange = { value ->
                            setTabState(tabState.copy(megrendeles = tabState.megrendeles.copy(telepules = value)))
                        }
                    }
                }
            }
        }
        Row {
            Col(span = 7) {
                FormItem {
                    attrs.label = StringOrReactElement.fromString("Kerület")
                    val source: Array<Any> = appState.geoData.varosok.let { varosok ->
                        if (tabState.megrendeles.telepules.isNullOrEmpty()) {
                            varosok
                        } else {
                            varosok.filter {
                                it.telepules == tabState.megrendeles.telepules &&
                                        it.irszam == tabState.megrendeles.irsz
                            }
                        }.map { it.kerulet }
                    }
                            .filter { it.isNotEmpty() }
                            .distinct()
                            .toTypedArray()
                    AutoComplete(source) {
                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.kerulet
                        attrs.value = tabState.megrendeles.kerulet
                        attrs.placeholder = "Kerület"
                        attrs.filterOption = { inputString, optionElement ->
                            if (inputString.length < 2) false else
                                (optionElement.props.children as String).toUpperCase().replace(" ", "").contains(inputString.toUpperCase().replace(" ", ""))
                        }
                        attrs.onChange = { value ->
                            setTabState(tabState.copy(megrendeles = tabState.megrendeles.copy(kerulet = value)))
                        }
                    }
                }
            }
            Col(offset = 1, span = 7) {
                FormItem {
                    attrs.label = StringOrReactElement.fromString("Közterület neve")
                    val source: Array<Any> = appState.geoData.varosok.let { varosok ->
                        if (tabState.megrendeles.irsz.isNullOrEmpty()) {
                            emptyList<String>()
                        } else {
                            varosok.filter {
                                it.kerulet == tabState.megrendeles.kerulet &&
                                        it.irszam == tabState.megrendeles.irsz
                            }.map { it.utcanev }
                        }
                    }.filter { it.isNotEmpty() }.distinct().toTypedArray()
                    AutoComplete(source) {
                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.kozteruletNeve
                        attrs.value = tabState.megrendeles.utca
                        attrs.placeholder = "Közterület neve"
                        attrs.filterOption = { inputString, optionElement ->
                            if (inputString.length < 2) false else
                                (optionElement.props.children as String).toUpperCase().replace(" ", "").contains(inputString.toUpperCase().replace(" ", ""))
                        }
                        attrs.onChange = { value ->
                            val utcaJellegek = appState.geoData.varosok.filter {
                                it.utcanev == value &&
                                        it.irszam == tabState.megrendeles.irsz
                            }.map { it.utotag }
                            val utcaJelleg = if (utcaJellegek.size == 1) utcaJellegek.first() else ""

                            setTabState(tabState.copy(megrendeles = tabState.megrendeles.copy(
                                    utca = value,
                                    utcaJelleg = utcaJelleg
                            )))
                        }
                    }
                }
            }
            Col(offset = 1, span = 7) {
                FormItem {
                    attrs.label = StringOrReactElement.fromString("Közterület jellege")
                    val source: Array<Any> = appState.geoData.varosok.let { varosok ->
                        if (tabState.megrendeles.utca.isNullOrEmpty()) {
                            kozteruletJellegek.asList()
                        } else {
                            varosok.filter {
                                it.utcanev == tabState.megrendeles.utca &&
                                        it.irszam == tabState.megrendeles.irsz
                            }.map { it.utotag }
                        }
                    }.filter { it.isNotEmpty() }.distinct().toTypedArray()
                    AutoComplete(source) {
                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.kozteruletJellege
                        attrs.value = tabState.megrendeles.utcaJelleg
                        attrs.placeholder = "Közterület jellege"
                        attrs.filterOption = { inputString, optionElement ->
                            (optionElement.props.children as String).toUpperCase().replace(" ", "").contains(inputString.toUpperCase().replace(" ", ""))
                        }
                        attrs.onChange = { value ->
                            setTabState(tabState.copy(megrendeles = tabState.megrendeles.copy(utcaJelleg = value)))
                        }
                    }
                }
            }
        }
        Row {
            Col(span = 6) {
                FormItem {
                    attrs.label = StringOrReactElement.fromString("Házszám")
                    Input {
                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.hazszam
                        attrs.value = tabState.megrendeles.hazszam
                        attrs.onChange = { e ->
                            setTabState(tabState.copy(megrendeles = tabState.megrendeles.copy(hazszam = e.target.asDynamic().value as String?
                                    ?: "")))
                        }
                    }
                }
            }
            Col(offset = 1, span = 5) {
                FormItem {
                    attrs.label = StringOrReactElement.fromString("Lépcsőház")
                    Input {
                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.lepcsohaz
                        attrs.value = tabState.megrendeles.lepcsohaz
                        attrs.onChange = { e ->
                            setTabState(tabState.copy(megrendeles = tabState.megrendeles.copy(lepcsohaz = e.target.asDynamic().value as String?
                                    ?: "")))
                        }
                    }
                }
            }
            Col(offset = 1, span = 5) {
                FormItem {
                    attrs.label = StringOrReactElement.fromString("Emelet")
                    Input {
                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.emelet
                        attrs.value = tabState.megrendeles.emelet
                        attrs.onChange = { e ->
                            setTabState(tabState.copy(megrendeles = tabState.megrendeles.copy(emelet = e.target.asDynamic().value as String?
                                    ?: "")))
                        }
                    }
                }
            }
            Col(offset = 1, span = 5) {
                FormItem {
                    attrs.label = StringOrReactElement.fromString("Ajtó")
                    Input {
                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.ajto
                        attrs.value = tabState.megrendeles.ajto
                        attrs.onChange = { e ->
                            setTabState(tabState.copy(megrendeles = tabState.megrendeles.copy(ajto = e.target.asDynamic().value as String?
                                    ?: "")))
                        }
                    }
                }
            }
        }
    }
}

private fun RElementBuilder<PanelProps>.hitelPanel(tabState: AlapAdatokTabComponentState, setTabState: Dispatcher<AlapAdatokTabComponentState>) {
    Form {
        if (tabState.megrendeles.munkatipus.isErtekbecsles()) {
            Row {
                Col(span = 10) {
                    FormItem {
                        attrs.label = StringOrReactElement.fromString("Hitel típus")
                        beilesztettSzovegbolImportalva(tabState.megrendelesFieldsFromImportText?.lakasCel?.let { it == tabState.megrendeles.hitelTipus }
                                ?: false)
                        Select {
                            attrs.asDynamic().id = MegrendelesScreenIds.modal.input.hitelTipus
                            attrs.value = tabState.megrendeles.hitelTipus
                            attrs.onSelect = { newValue: String, option ->
                                setTabState(tabState.copy(megrendeles = tabState.megrendeles.copy(
                                        hitelTipus = newValue
                                )))
                            }
                            arrayOf("vásárlás", "építés", "hitelkiváltás", "felújítás", "bővítés", "közművesítés").forEach {
                                Option { attrs.value = it; +it }
                            }
                        }
                    }
                }
                Col(offset = 2, span = 10) {
                    FormItem {
                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.hitelOsszege
                        beilesztettSzovegbolImportalva(tabState.megrendelesFieldsFromImportText?.hitelOsszeg?.let { it == tabState.megrendeles.hitelOsszeg }
                                ?: false)
                        attrs.label = StringOrReactElement.fromString("Hitel összege")
                        MyNumberInput {
                            attrs.number = tabState.megrendeles.hitelOsszeg?.toLong()
                            attrs.onValueChange = { value ->
                                setTabState(tabState.copy(megrendeles = tabState.megrendeles.copy(
                                        hitelOsszeg = value?.toInt()
                                )))
                            }
                        }

                    }
                }
            }
        }
        Row {
            Col(span = 10) {
                FormItem {
                    attrs.label = StringOrReactElement.fromString("Ajánlatszám")
                    beilesztettSzovegbolImportalva(tabState.megrendelesFieldsFromImportText?.ajanlatSzam?.let { it == tabState.megrendeles.ajanlatSzam }
                            ?: false)
                    Input {
                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.ajanlatSzam
                        attrs.value = tabState.megrendeles.ajanlatSzam
                        attrs.onChange = { e ->
                            setTabState(tabState.copy(megrendeles = tabState.megrendeles.copy(ajanlatSzam = e.target.asDynamic().value as String?
                                    ?: "")))
                        }
                    }
                }
            }
            Col(offset = 2, span = 10) {
                FormItem {
                    attrs.label = StringOrReactElement.fromString("Szerződésszám")
                    beilesztettSzovegbolImportalva(tabState.megrendelesFieldsFromImportText?.szerzodesSzam?.let { it == tabState.megrendeles.szerzodesSzam }
                            ?: false)
                    Input {
                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.szerzodesSzam
                        attrs.value = tabState.megrendeles.szerzodesSzam
                        attrs.onChange = { e ->
                            setTabState(tabState.copy(megrendeles = tabState.megrendeles.copy(szerzodesSzam = e.target.asDynamic().value as String?
                                    ?: "")))
                        }
                    }
                }
            }

        }
    }
}

private fun RElementBuilder<FormItemProps>.beilesztettSzovegbolImportalva(beillesztett: Boolean): Boolean {
    attrs.hasFeedback = beillesztett
    attrs.validateStatus = if (beillesztett) ValidateStatus.success else null
    attrs.help = if (beillesztett) StringOrReactElement.from {
        div {
            attrs.jsStyle = jsStyle { color = "green" }
            +"Beillesztett szövegből importálva"
        }
    } else null
    return beillesztett
}

private fun RElementBuilder<PanelProps>.ertesitendoSzemelyPanel(tabState: AlapAdatokTabComponentState, setTabState: Dispatcher<AlapAdatokTabComponentState>) {
    Form {
        Row {
            Col(span = 24) {
                FormItem {
                    attrs.labelCol = ColProperties { span = 8 }
                    attrs.wrapperCol = ColProperties { span = 16 }
                    attrs.label = StringOrReactElement.fromString("Értesítendő személy azonos az ügyféllel")
                    Checkbox {
                        attrs.checked = tabState.ertesitendoSzemelyAzonos
                        attrs.onChange = { checked ->
                            setTabState(tabState.copy(ertesitendoSzemelyAzonos = checked))
                        }
                    }
                }
            }
        }
        Row {
            Col(span = 7) {
                FormItem {
                    attrs.label = StringOrReactElement.fromString("Név")
                    beilesztettSzovegbolImportalva(tabState.megrendelesFieldsFromImportText?.ertesitesiNev?.let { it == tabState.megrendeles.ertesitesiNev }
                            ?: false)
                    Input {
                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.ertesitendoNev
                        attrs.disabled = tabState.ertesitendoSzemelyAzonos
                        attrs.value = if (tabState.ertesitendoSzemelyAzonos) tabState.megrendeles.ugyfelNeve else tabState.megrendeles.ertesitesiNev
                        attrs.onChange = { e ->
                            setTabState(tabState.copy(megrendeles = tabState.megrendeles.copy(ertesitesiNev = e.target.asDynamic().value as String?
                                    ?: "")))
                        }
                    }
                }
            }
            Col(offset = 1, span = 7) {
                FormItem {
                    attrs.label = StringOrReactElement.fromString("Telefonszám")
                    beilesztettSzovegbolImportalva(tabState.megrendelesFieldsFromImportText?.ertesitesiTel?.let { it == tabState.megrendeles.ertesitesiTel }
                            ?: false)
                    Input {
                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.ertesitendoTel
                        attrs.disabled = tabState.ertesitendoSzemelyAzonos
                        attrs.value = if (tabState.ertesitendoSzemelyAzonos) tabState.megrendeles.ugyfelTel else tabState.megrendeles.ertesitesiTel
                        attrs.onChange = { e ->
                            setTabState(tabState.copy(megrendeles = tabState.megrendeles.copy(ertesitesiTel = e.target.asDynamic().value as String?
                                    ?: "")))
                        }
                    }
                }
            }
        }
    }
}


fun RElementBuilder<FormItemProps>.addExcelbolBetoltveMessages(oldValue: String?, newValue: String?) {
    val excelValueIsNotNull = !newValue.isNullOrEmpty()
    if (excelValueIsNotNull && (oldValue == newValue)) {
        attrs.hasFeedback = true
        attrs.validateStatus = ValidateStatus.success
        attrs.help = StringOrReactElement.from {
            div {
                attrs.jsStyle = jsStyle { color = "green" }
                +"Excelből betőltve"
            }
        }
    } else if (excelValueIsNotNull && oldValue != newValue) {
        attrs.hasFeedback = true
        attrs.validateStatus = ValidateStatus.warning
        attrs.help = StringOrReactElement.from {
            div {
                attrs.jsStyle = jsStyle { color = "rgb(235, 148, 6)" }
                +"Excelből: ${newValue ?: ""}"
            }
        }
    } else {
        attrs.hasFeedback = false
        attrs.validateStatus = null
        attrs.help = null
    }
}

fun RElementBuilder<FormItemProps>.addExcelbolBetoltveMessages(oldValue: Int?, newValue: Int?) {
    addExcelbolBetoltveMessages(
            oldValue.format(),
            newValue.format()
    )
}

private fun RElementBuilder<PanelProps>.ugyfelPanel(tabState: AlapAdatokTabComponentState,
                                                    setTabState: Dispatcher<AlapAdatokTabComponentState>,
                                                    excel: MegrendelesFieldsFromExternalSource?) {
    Form {
        Row {
            Col(span = 7) {
                FormItem {
                    attrs.required = true
                    attrs.label = StringOrReactElement.fromString("Név")
                    val beillesztett = beilesztettSzovegbolImportalva(tabState.megrendelesFieldsFromImportText?.ugyfelNeve?.let { it == tabState.megrendeles.ugyfelNeve }
                            ?: false)
                    if (!beillesztett) {
                        addExcelbolBetoltveMessages(tabState.megrendeles.ugyfelNeve, excel?.ugyfelNeve)
                    }
                    if (attrs.hasFeedback == false) {
                        attrs.validateStatus = if (tabState.megrendeles.ugyfelNeve.isEmpty()) {
                            ValidateStatus.error
                        } else null
                    }
                    Input {
                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.ugyfelNev
                        attrs.value = tabState.megrendeles.ugyfelNeve
                        attrs.onChange = { e ->
                            setTabState(tabState.copy(megrendeles = tabState.megrendeles.copy(ugyfelNeve = e.target.asDynamic().value as String?
                                    ?: "")))
                        }
                    }
                }
            }
            Col(offset = 1, span = 7) {
                FormItem {
                    attrs.required = true
                    beilesztettSzovegbolImportalva(tabState.megrendelesFieldsFromImportText?.ugyfelTelefonszama?.let { it == tabState.megrendeles.ugyfelTel }
                            ?: false)
                    attrs.label = StringOrReactElement.fromString("Telefonszám")
                    Input {
                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.ugyfelTel
                        attrs.value = tabState.megrendeles.ugyfelTel
                        attrs.onChange = { e ->
                            setTabState(tabState.copy(megrendeles = tabState.megrendeles.copy(ugyfelTel = e.target.asDynamic().value as String?
                                    ?: "")))
                        }
                    }
                }
            }
            Col(offset = 1, span = 7) {
                FormItem {
                    attrs.label = StringOrReactElement.fromString("Email cím")
                    Input {
                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.ugyfelEmail
                        attrs.value = tabState.megrendeles.ugyfelEmail
                        attrs.onChange = { e ->
                            setTabState(tabState.copy(megrendeles = tabState.megrendeles.copy(ugyfelEmail = e.target.asDynamic().value as String?
                                    ?: "")))
                        }
                    }
                }
            }
        }
    }
}

private fun RElementBuilder<PanelProps>.megrendelesPanel(
        appState: AppState,
        tabState: AlapAdatokTabComponentState,
        setTabState: Dispatcher<AlapAdatokTabComponentState>,
        excel: MegrendelesFieldsFromExternalSource?) {
    Form {
        Row {
            Col(span = 7) {
                FormItem {
                    attrs.label = StringOrReactElement.fromString("Megrendelő")
                    Select {
                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.megrendelo
                        attrs.value = tabState.megrendeles.megrendelo
                        attrs.onSelect = { newMegrendelo: String, option ->
                            setMegrendelo(appState, tabState.megrendeles, tabState, newMegrendelo, setTabState)
                        }
                        appState.sajatArState.allMegrendelo.forEach { megrendeloName ->
                            Option { attrs.value = megrendeloName; +megrendeloName }
                        }
                    }
                }
            }
            Col(offset = 1, span = 7) {
                FormItem {
                    attrs.label = StringOrReactElement.fromString("Régió")
                    Select {
                        attrs.value = tabState.megrendeles.regio
                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.regio
                        attrs.onSelect = { regio: String, option ->
                            setNewRegio(appState, tabState, tabState.megrendeles, regio, setTabState)
                        }
                        megyek.forEach {
                            Option { attrs.value = it; +it }
                        }
                    }
                }
            }
            Col(offset = 1, span = 7) {
                FormItem {
                    attrs.label = StringOrReactElement.fromString("Munkatípus")
                    Select {
                        attrs.value = tabState.megrendeles.munkatipus
                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.munkatipus
                        attrs.onSelect = { munkatipus, option ->
                            setMunkatipus(appState, tabState, tabState.megrendeles, munkatipus, setTabState)
                        }
                        tabState.selectableMunkatipusok.forEach {
                            Option { attrs.value = it; +it }
                        }
                    }
                }
            }
        }
        Row {
            Col(span = 7) {
                FormItem {
                    attrs.label = StringOrReactElement.fromString("Ingatlan típus (munkadíj meghatározásához)")
                    val sajatArak = appState.sajatArState.getSajatArakFor(tabState.megrendeles.megrendelo, tabState.megrendeles.munkatipus)
                    Select {
                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.ingatlanTipusMunkadijMeghatarozasahoz
                        attrs.value = sajatArak.firstOrNull { it.leiras == tabState.megrendeles.ingatlanTipusMunkadijMeghatarozasahoz }?.id
                                ?: ""
                        attrs.disabled = sajatArak.isEmpty()
                        attrs.onSelect = { sajatArId: Int, option ->
                            setLeiras(appState, tabState, tabState.megrendeles, sajatArId, setTabState)
                        }
                        sajatArak.forEach { sajatAr ->
                            Option { attrs.value = sajatAr.id; +sajatAr.leiras }
                        }
                    }
                }
            }
            Col(offset = 1, span = 7) {
                FormItem {
                    attrs.label = StringOrReactElement.fromString("Alvállalkozó")
                    val selectableAlvallalkozok = tabState.selectableAlvallalkozok
                    Select {
                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.alvallalkozo
                        attrs.value = if (tabState.megrendeles.alvallalkozoId == 0) "" else tabState.megrendeles.alvallalkozoId
                        attrs.disabled = selectableAlvallalkozok.isEmpty()
                        attrs.onSelect = { avId: Int, option ->
                            setTabState(tabState.copy(megrendeles = setAlvallalkozoId(appState, tabState.megrendeles, avId)))
                        }
                        selectableAlvallalkozok.forEach { alv ->
                            Option { attrs.value = alv.id; +alv.name }
                        }
                    }
                }
            }
        }
        Row {
            Col(span = 7) {
                FormItem {
                    attrs.label = StringOrReactElement.fromString("Értékbecslő")
                    val ebName = appState.alvallalkozoState.ertekbecslok[tabState.megrendeles.ertekbecsloId]?.name
                    addExcelbolBetoltveMessages(ebName, excel?.ertekBecslo)
                    val selectableAlvallalkozok = tabState.selectableAlvallalkozok
                    Select {
                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.ertekbecslo
                        attrs.value = if (tabState.megrendeles.ertekbecsloId == 0) "" else tabState.megrendeles.ertekbecsloId
                        attrs.disabled = selectableAlvallalkozok.isEmpty()
                        attrs.onSelect = { ebId: Int, option ->
                            setTabState(tabState.copy(megrendeles = tabState.megrendeles.copy(ertekbecsloId = ebId)))
                        }
                        appState.alvallalkozoState.alvallalkozok[tabState.megrendeles.alvallalkozoId]?.let { alvallalkozo ->
                            appState.alvallalkozoState.getErtekbecslokOf(alvallalkozo).filter { !it.disabled }.forEach { eb ->
                                Option { attrs.value = eb.id; +eb.name }
                            }
                        }
                    }
                }
            }
            Col(offset = 1, span = 7) {
                FormItem {
                    attrs.asDynamic().id = MegrendelesScreenIds.modal.input.ertekbecsloDija
                    attrs.label = StringOrReactElement.fromString("Értékbecslő díja(Ft)")
                    MyNumberInput {
                        attrs.number = tabState.megrendeles.ertekbecsloDija?.toLong()
                        attrs.onValueChange = { value -> setTabState(tabState.copy(megrendeles = tabState.megrendeles.copy(ertekbecsloDija = value?.toInt()))) }
                    }

                }
            }
            Col(offset = 1, span = 7) {
                FormItem {
                    attrs.asDynamic().id = MegrendelesScreenIds.modal.input.szamlazhatoDij
                    attrs.label = StringOrReactElement.fromString("Számlázható díj (Ft)")
                    MyNumberInput {
                        attrs.number = tabState.megrendeles.szamlazhatoDij?.toLong()
                        attrs.addonAfter = StringOrReactElement.from {
                            val afa = if (tabState.megrendeles.szamlazhatoDij != null) {
                                (tabState.megrendeles.szamlazhatoDij * 1.27).roundToLong()
                            } else 0L
                            div {
                                span {
                                    attrs.jsStyle = jsStyle {
                                        fontSize = "10px"
                                    }
                                    +"+ ÁFA(27%) = "
                                }
                                span {
                                    attrs.jsStyle = jsStyle {
                                        fontSize = "14px"
                                    }
                                    +parseGroupedStringToNum(afa.toString()).second
                                }
                            }
                        }
                        attrs.onValueChange = { value -> setTabState(tabState.copy(megrendeles = tabState.megrendeles.copy(szamlazhatoDij = value?.toInt()))) }
                    }

                }
            }
        }
        Row {
            val eb = tabState.megrendeles.munkatipus.isErtekbecsles()
            val et = tabState.megrendeles.munkatipus.isEnergetika()
            if (!eb && !et) {
                Col(span = 7) {
                    ebAzon(tabState, setTabState, "Azonosító")
                }
            } else {
                if (eb && et) {
                    Col(span = 3) {
                        ebAzon(tabState, setTabState, "Értékbecslés Azonosító")
                    }
                    Col(offset = 1, span = 3) {
                        etAzon(tabState, setTabState)
                    }
                } else if (eb) {
                    Col(span = 7) {
                        ebAzon(tabState, setTabState, "Értékbecslés Azonosító")
                    }
                } else {
                    Col(span = 7) {
                        etAzon(tabState, setTabState)
                    }
                }
            }
            Col(offset = 1, span = 7) {
                FormItem {
                    attrs.label = StringOrReactElement.fromString("Fővállalkozó")
                    Select {
                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.fovallalkozo
                        attrs.value = tabState.megrendeles.foVallalkozo
                        attrs.onSelect = { value: String, option ->
                            setTabState(tabState.copy(megrendeles = tabState.megrendeles.copy(foVallalkozo = value)))
                        }
                        arrayOf("", "Presting Zrt.", "Viridis Kft.", "Estating Kft.").forEach {
                            Option { attrs.value = it; +it }
                        }
                    }
                }
            }
        }
        Row {
            Col(span = 8) {
                FormItem {
                    attrs.label = StringOrReactElement.fromString("Megrendelés dátuma")
                    DatePicker {
                        attrs.allowClear = false
                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.megrendelesDatuma
                        attrs.value = tabState.megrendeles.megrendelve?.let { moment(it) } ?: moment()
                        attrs.onChange = { date, str ->
                            if (date != null) {
                                setTabState(tabState.copy(megrendeles = tabState.megrendeles.copy(megrendelve = date)))
                            }
                        }
                    }
                }
            }
            Col(span = 8) {
                FormItem {
                    attrs.label = StringOrReactElement.fromString("Határidő")
                    beilesztettSzovegbolImportalva((tabState.megrendelesFieldsFromImportText?.hatarido to tabState.megrendeles.hatarido).let { (a, b) ->
                        a != null && b != null && a.isSame(b, Granularity.Day)
                    })
                    DatePicker {
                        attrs.allowClear = false
                        attrs.asDynamic().id = MegrendelesScreenIds.modal.input.hatarido
                        attrs.value = tabState.megrendeles.hatarido ?: moment()
                        attrs.onChange = { date, str ->
                            if (date != null) {
                                setTabState(tabState.copy(megrendeles = tabState.megrendeles.copy(hatarido = date)))
                            }
                        }
                    }
                }
            }

        }
    }
}

private fun RElementBuilder<ColProps>.etAzon(tabState: AlapAdatokTabComponentState, setTabState: Dispatcher<AlapAdatokTabComponentState>) {
    FormItem {
        attrs.required = tabState.megrendeles.munkatipus.isEnergetika()
        beilesztettSzovegbolImportalva(tabState.megrendelesFieldsFromImportText?.etAzonosito?.let { it == tabState.azonosito2 }
                ?: false)
        attrs.label = StringOrReactElement.fromString("Energetika Azonosító")
        Input {
            attrs.asDynamic().id = MegrendelesScreenIds.modal.input.etAzonosito
            attrs.value = tabState.azonosito2
            attrs.onChange = { e ->
                setTabState(tabState.copy(azonosito2 = e.target.asDynamic().value as String? ?: ""))
            }
        }
    }
}

private fun RElementBuilder<ColProps>.ebAzon(tabState: AlapAdatokTabComponentState,
                                             setTabState: Dispatcher<AlapAdatokTabComponentState>,
                                             label: String) {
    val eb = tabState.megrendeles.munkatipus.isErtekbecsles()
    val et = tabState.megrendeles.munkatipus.isEnergetika()
    FormItem {
        attrs.required = tabState.megrendeles.munkatipus.isErtekbecsles()
        attrs.label = StringOrReactElement.fromString(label)
        val beillesztett = beilesztettSzovegbolImportalva(tabState.megrendelesFieldsFromImportText?.ebAzonosito?.let { it == tabState.azonosito1 }
                ?: false)
        attrs.validateStatus = if (beillesztett) {
            ValidateStatus.success
        } else if ((eb || (!eb && !et)) && tabState.azonosito1.isEmpty()) {
            ValidateStatus.error
        } else {
            null
        }
        Input {
            attrs.asDynamic().id = MegrendelesScreenIds.modal.input.ebAzonosito
            attrs.value = tabState.azonosito1
            attrs.onChange = { e ->
                setTabState(tabState.copy(azonosito1 = e.target.asDynamic().value as String? ?: ""))
            }
        }
    }
}

fun setNewRegio(appState: AppState,
                oldState: AlapAdatokTabComponentState,
                megr: Megrendeles,
                newRegio: String,
                setTabState: Dispatcher<AlapAdatokTabComponentState>) {
    val newSelectableAlvallalkozok = appState.alvallalkozoState.getSelectableAlvallalkozok(newRegio)
    val defaultSelectedAlvallalkozoId = newSelectableAlvallalkozok.firstOrNull()?.id
    val newMegr = setAlvallalkozoId(appState, megr.copy(regio = newRegio), defaultSelectedAlvallalkozoId)

    val munkatipusok = munkatipusokForRegio(appState.alvallalkozoState, newRegio)
    val currentMunkatipus = munkatipusok.firstOrNull { it == newMegr.munkatipus }
    val defaultMunkatipus = munkatipusok.let { munkatipusok ->
        if (munkatipusok.size == 1) {
            munkatipusok.first()
        } else if (currentMunkatipus != null) {
            currentMunkatipus
        } else {
            null
        }
    } ?: ""

    setMunkatipus(appState, oldState, newMegr, defaultMunkatipus) { newState ->
        setTabState(newState.copy(
                selectableAlvallalkozok = newSelectableAlvallalkozok,
                selectableMunkatipusok = munkatipusok
        ))
    }
}

fun munkatipusokForRegio(alvallalkozoState: AlvallalkozoState, regio: String): List<String> {
    return alvallalkozoState.regioOsszerendelesek.values
            .filter { regioOssz -> regioOssz.megye == regio }
            .map { it.munkatipus }
            .distinct()
}

private fun setMunkatipus(appState: AppState,
                          oldState: AlapAdatokTabComponentState,
                          megr: Megrendeles,
                          newMunkatipus: String,
                          setTabState: Dispatcher<AlapAdatokTabComponentState>) {

    val sajatArId = getOnlySajatArOrNull(appState, megr.megrendelo, newMunkatipus)?.id
    setLeiras(appState, oldState, megr.copy(
            munkatipus = newMunkatipus
    ), sajatArId, setTabState)
}

private fun setAlvallalkozoId(appState: AppState, megr: Megrendeles, alvId: Int?): Megrendeles {
    val ebId = appState.alvallalkozoState.getErtekbecslokOf(alvId ?: 0).firstOrNull()?.id

    return recalcRegioOsszerendeles(megr.copy(
            alvallalkozoId = alvId ?: 0,
            ertekbecsloId = ebId ?: 0
    ), appState.alvallalkozoState)
}

private fun setMegrendelo(appState: AppState, megrendeles: Megrendeles,
                          oldState: AlapAdatokTabComponentState,
                          newMegrendelo: String, setTabState: Dispatcher<AlapAdatokTabComponentState>) {
    val sajatArId = getOnlySajatArOrNull(appState, newMegrendelo, megrendeles.munkatipus)?.id
    setLeiras(appState, oldState, megrendeles.copy(
            megrendelo = newMegrendelo
    ), sajatArId, setTabState)
}

private fun setLeiras(appState: AppState, oldState: AlapAdatokTabComponentState,
                      megr: Megrendeles,
                      sajatArId: Int?,
                      setTabState: Dispatcher<AlapAdatokTabComponentState>) {
    val sajatAr = appState.sajatArState.sajatArak[sajatArId]
    val modifiedMegr = megr.copy(
            ingatlanTipusMunkadijMeghatarozasahoz = sajatAr?.leiras ?: "",
            szamlazhatoDij = sajatAr?.nettoAr
    )

    setTabState(oldState.copy(
            megrendeles = recalcRegioOsszerendeles(modifiedMegr, appState.alvallalkozoState),
            szamlazhatoDijAfa = sajatAr?.afa
    ))
}

private fun recalcRegioOsszerendeles(megr: Megrendeles, alvallalkozoState: AlvallalkozoState): Megrendeles {
    val newRegioOsszerendeles = alvallalkozoState.getRegioOsszerendelesek(megr.alvallalkozoId).firstOrNull { it.megye == megr.regio && it.munkatipus == megr.munkatipus && it.leiras == megr.ingatlanTipusMunkadijMeghatarozasahoz }
    val ebDija = newRegioOsszerendeles?.nettoAr
    return megr.copy(
            ertekbecsloDija = ebDija
    )
}

private fun getOnlySajatArOrNull(appState: AppState, megrendelo: String, munkatipus: String): SajatAr? {
    return appState.sajatArState.getSajatArakFor(megrendelo, munkatipus).firstOrNull()
}

private fun createAzonosito(tabState: AlapAdatokTabComponentState): String {
    return if (tabState.megrendeles.munkatipus == Munkatipusok.EnergetikaAndErtekBecsles.str) {
        "EB${tabState.azonosito1}_ET${tabState.azonosito2}"
    } else if (tabState.megrendeles.munkatipus == Munkatipusok.Energetika.str) {
        tabState.azonosito2
    } else {
        tabState.azonosito1
    }
}