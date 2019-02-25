package hu.nevermind.utils.app

import app.Dispatcher
import app.common.Moment
import app.common.moment
import app.megrendeles.MegrendelesScreenIds
import app.megrendeles.tabTitle
import app.useState
import hu.nevermind.antd.*
import hu.nevermind.iktato.Path
import hu.nevermind.iktato.RestUrl
import hu.nevermind.utils.app.megrendeles.AkadalyokTabComponent
import hu.nevermind.utils.app.megrendeles.AkadalyokTabParams
import hu.nevermind.utils.app.megrendeles.FajlokTabComponent
import hu.nevermind.utils.app.megrendeles.FajlokTabParams
import hu.nevermind.utils.hu.nevermind.antd.StringOrReactElement
import hu.nevermind.utils.hu.nevermind.antd.message
import hu.nevermind.utils.jsStyle
import hu.nevermind.utils.store.Megrendeles
import hu.nevermind.utils.store.communicator
import hu.nevermind.utils.store.dateFormat
import react.RBuilder
import react.RElementBuilder
import store.Action


data class AlvallalkozoMegrendelesFormModalParams(val megrendeles: Megrendeles,
                                                  val visible: Boolean,
                                                  val globalDispatch: Dispatcher<Action>)

data class AlvallalkozoMegrendelesFormModalState(val activeKey: String,
                                                 val szamlaSorszama: String,
                                                 val szamlatKifizetteAzUgyfel: Moment?,
                                                 val szemleIdopontja: Moment?)


object AlvallalkozoMegrendelesFormModalComponent : DefinedReactComponent<AlvallalkozoMegrendelesFormModalParams>() {
    override fun RBuilder.body(props: AlvallalkozoMegrendelesFormModalParams) {
        val (modalState, setModalState) = useState(AlvallalkozoMegrendelesFormModalState(
                activeKey = MegrendelesScreenIds.modal.tab.first,
                szamlaSorszama = props.megrendeles.szamlaSorszama,
                szamlatKifizetteAzUgyfel = props.megrendeles.keszpenzesBefizetes,
                szemleIdopontja = props.megrendeles.szemleIdopontja
        ))

        // Az akadály tab csak igy tudja közölni a Megjegyzését
        val onSaveFunctions = Array(6) { size -> { megr: Megrendeles -> megr } }
        Modal {
            attrs.width = 720
            attrs.visible = props.visible
            attrs.okText = "Mentés"
            attrs.cancelText = "Bezárás"
            attrs.title = StringOrReactElement.fromString(props.megrendeles.azonosito)
            attrs.onOk = {
                val finalMegrendeles = onSaveFunctions.fold(props.megrendeles) { megrendeles, onSaveFunctionOfTab ->
                    onSaveFunctionOfTab(megrendeles)
                }
                val dataToSave = object {
                    val id = props.megrendeles.id
                    val szamlaSorszama = modalState.szamlaSorszama
                    val szemleIdopontja = modalState.szemleIdopontja
                    val szamlatKifizetteAzUgyfel = modalState.szamlatKifizetteAzUgyfel
                    val megjegyzes = finalMegrendeles.megjegyzes
                }
                communicator.saveEntity<Any, dynamic>(RestUrl.saveMegrendeles, dataToSave) { response ->
                    props.globalDispatch(Action.MegrendelesekFromServer(arrayOf(response)))
                    props.globalDispatch(Action.ChangeURL(Path.megrendeles.root))
                    message.success("A Megrendelés sikeresen módosult")
                }
            }
            attrs.onCancel = {
                props.globalDispatch(Action.ChangeURL(Path.megrendeles.root))
            }
            Tabs {
                attrs.activeKey = modalState.activeKey
                attrs.onChange = { key ->
                    setModalState(modalState.copy(
                            activeKey = key
                    ))
                }
                TabPane {
                    attrs.key = MegrendelesScreenIds.modal.tab.first
                    attrs.tab = StringOrReactElement.fromReactElement(tabTitle("Adatok", icon = "list-alt", color = "black", id = MegrendelesScreenIds.modal.tab.first))
                    adatokTab(modalState, setModalState, props)
                }
                TabPane {
                    attrs.key = MegrendelesScreenIds.modal.tab.akadalyok
                    val akadalyok = props.megrendeles.akadalyok
                    val akadalyokSize = maxOf(akadalyok.size, if (props.megrendeles.megjegyzes.isNotEmpty()) 1 else 0)
                    attrs.tab = StringOrReactElement.fromReactElement(tabTitle("Megjegyzés/Akadály", icon = "message", badgeNum = akadalyokSize, color = "black", id = MegrendelesScreenIds.modal.tab.akadalyok))
                    AkadalyokTabComponent.insert(this,
                            AkadalyokTabParams(megrendeles = props.megrendeles,
                                    onSaveFunctions = onSaveFunctions,
                                    globalDispatch = props.globalDispatch,
                                    setFormState = null))
                }
                TabPane {
                    attrs.key = MegrendelesScreenIds.modal.tab.files
                    attrs.tab = StringOrReactElement.fromReactElement(tabTitle("Fájlok", icon = "upload",
                            badgeNum = props.megrendeles.files.size,
                            color = "black",
                            id = MegrendelesScreenIds.modal.tab.files))
                    FajlokTabComponent.insert(this, FajlokTabParams(props.megrendeles,
                            megrendelesFieldsFromExcel = null,
                            setMegrendelesFieldsFromExcel = null,
                            onSaveFunctions = onSaveFunctions,
                            globalDispatch = props.globalDispatch,
                            appState = null,
                            setFormState = null,
                            alvallalkozoStore = null))
                }
            }
        }
    }

    private fun RElementBuilder<TabPaneProps>.adatokTab(modalState: AlvallalkozoMegrendelesFormModalState, setModalState: Dispatcher<AlvallalkozoMegrendelesFormModalState>, props: AlvallalkozoMegrendelesFormModalParams) {
        Collapse {
            attrs.bordered = false
            attrs.defaultActiveKey = arrayOf("Szerkeszthető adatok", "Csak olvasható adatok")
            Panel("Szerkeszthető adatok") {
                attrs.header = StringOrReactElement.fromString("Szerkeszthető adatok")

                Row {
                    Col(span = 11) {
                        FormItem {
                            attrs.label = StringOrReactElement.fromString("Számla sorszáma")
                            Input {
                                attrs.value = modalState.szamlaSorszama
                                attrs.onChange = { e ->
                                    setModalState(
                                            modalState.copy(szamlaSorszama = e.currentTarget.asDynamic().value)
                                    )
                                }
                            }
                        }
                    }
                    Col(offset = 2, span = 11) {
                        FormItem {
                            attrs.label = StringOrReactElement.fromString("Készpénzes kifizetés")
                            Checkbox {
                                attrs.checked = modalState.szamlatKifizetteAzUgyfel != null
                                attrs.onChange = { checked ->
                                    setModalState(modalState.copy(
                                            szamlatKifizetteAzUgyfel = if (checked) moment() else null
                                    ))
                                }
                            }
                            DatePicker {
                                attrs.asDynamic().id = MegrendelesScreenIds.modal.input.keszpenzesKifizetes
                                attrs.allowClear = false
                                attrs.disabled = modalState.szamlatKifizetteAzUgyfel == null
                                attrs.asDynamic().id = MegrendelesScreenIds.modal.input.feltoltveMegrendelonek
                                attrs.value = modalState.szamlatKifizetteAzUgyfel
                                attrs.onChange = { date, str ->
                                    if (date != null) {
                                        setModalState(modalState.copy(
                                                szamlatKifizetteAzUgyfel = date
                                        ))
                                    }
                                }
                            }
                        }
                        FormItem {
                            attrs.label = StringOrReactElement.fromString("Szemle időpontja")
                            Checkbox {
                                attrs.checked = modalState.szemleIdopontja != null
                                attrs.asDynamic().id = MegrendelesScreenIds.modal.input.szemleIdopontjaCheckbox
                                attrs.onChange = { checked ->
                                    setModalState(modalState.copy(
                                            szemleIdopontja = if (checked) moment() else null
                                    ))
                                }
                            }
                            DatePicker {
                                attrs.asDynamic().id = MegrendelesScreenIds.modal.input.szemleIdopontja
                                attrs.allowClear = false
                                attrs.disabled = modalState.szemleIdopontja == null
                                attrs.value = modalState.szemleIdopontja
                                attrs.onChange = { date, str ->
                                    if (date != null) {
                                        setModalState(modalState.copy(
                                                szemleIdopontja = date
                                        ))
                                    }
                                }
                            }
                        }

                    }
                }
            }
            Panel("Csak olvasható adatok") {
                attrs.header = StringOrReactElement.fromString("Csak olvasható adatok")

                readOnlyFields(props)

            }
        }
    }

    private fun RElementBuilder<PanelProps>.readOnlyFields(props: AlvallalkozoMegrendelesFormModalParams) {
        Row {
            Col(span = 11) {
                readOnlyField("Megrendelés rögzítve", props.megrendeles.rogzitve?.format(dateFormat)
                        ?: "")
            }
            Col(offset = 2, span = 11) {
                readOnlyField("Határidő", props.megrendeles.hatarido?.format(dateFormat) ?: "")
            }

            Col(span = 11) {
                readOnlyField("Azonosító", props.megrendeles.azonosito)
            }
            Col(offset = 2, span = 11) {
                readOnlyField("Név", props.megrendeles.ugyfelNeve)
            }

            Col(span = 11) {
                readOnlyField("Telefon", props.megrendeles.ugyfelTel)
            }
            Col(offset = 2, span = 11) {
                readOnlyField("HRSZ", props.megrendeles.hrsz)
            }

            Col(span = 11) {
                readOnlyField("Régió", props.megrendeles.regio)
            }
            Col(offset = 2, span = 11) {
                readOnlyField("Írszám", props.megrendeles.irsz)
            }

            Col(span = 11) {
                readOnlyField("Település", props.megrendeles.telepules)
            }
            Col(offset = 2, span = 11) {
                readOnlyField("Kerület", props.megrendeles.kerulet)
            }

            Col(span = 11) {
                readOnlyField("Közterület neve", props.megrendeles.utca)
            }
            Col(offset = 2, span = 11) {
                readOnlyField("Közterület jellege", props.megrendeles.utcaJelleg)
            }

            Col(span = 11) {
                readOnlyField("Házszám", props.megrendeles.hazszam)
            }
            Col(offset = 2, span = 11) {
                readOnlyField("Lépcsőház", props.megrendeles.lepcsohaz)
            }

            Col(span = 11) {
                readOnlyField("Emelet", props.megrendeles.emelet)
            }
            Col(offset = 2, span = 11) {
                readOnlyField("Ajtó ", props.megrendeles.ajto)
            }

            Col(span = 11) {
                readOnlyField("Ingatlan típus", props.megrendeles.ingatlanTipusMunkadijMeghatarozasahoz
                        ?: "")
            }
            Col(offset = 2, span = 11) {
                readOnlyField("Felvenni kívánt hitel összege", props.megrendeles.hitelOsszeg.format()
                        ?: "")
            }

            Col(span = 11) {
                readOnlyField("Ajánlatszám", props.megrendeles.ajanlatSzam)
            }
            Col(offset = 2, span = 11) {
                readOnlyField("Szerződésszám", props.megrendeles.szerzodesSzam)
            }
        }
    }

    private fun RBuilder.readOnlyField(label: String, value: String) {
        FormItem {
            attrs.label = StringOrReactElement.fromString(label)
            Input {
                attrs.value = value
                attrs.disabled = true
                attrs.asDynamic().style = jsStyle {
                    color = "rgba(0, 0, 0, 0.5)"
                    cursor = "default"
                }
            }
        }
    }
}
