package hu.nevermind.utils.app.megrendeles

import app.*
import app.common.moment
import app.megrendeles.MegrendelesFormState
import app.megrendeles.downloadFile
import hu.nevermind.antd.*
import hu.nevermind.antd.table.ColumnAlign
import hu.nevermind.antd.table.ColumnProps
import hu.nevermind.antd.table.Table
import hu.nevermind.utils.app.DefinedReactComponent
import hu.nevermind.utils.hu.nevermind.antd.StringOrReactElement
import hu.nevermind.utils.hu.nevermind.antd.message
import hu.nevermind.utils.jsStyle
import hu.nevermind.utils.store.*
import kotlinext.js.jsObject
import kotlinx.html.js.onClickFunction
import org.w3c.files.File
import org.w3c.xhr.BLOB
import org.w3c.xhr.XMLHttpRequest
import org.w3c.xhr.XMLHttpRequestResponseType
import react.RBuilder
import react.RElementBuilder
import react.buildElement
import react.dom.*
import store.Action
import store.UploadData
import store.addListener
import store.removeListener
import kotlin.js.Promise


data class FajlokTabParams(val formState: MegrendelesFormState,
                           val onSaveFunctions: Array<(Megrendeles) -> Megrendeles>,
                           val globalDispatch: (Action) -> Unit,
                           val appState: AppState,
                           val setFormState: Dispatcher<MegrendelesFormState>,
                           val alvallalkozoStore: AlvallalkozoState)

data class FajlokTabComponentState(val uploadModalVisible: Boolean,
                                   val tartalmazEnergetika: Boolean?,
                                   val tartalmazErtekbecsles: Boolean?,
                                   val uploadingFiles: List<File>)

object FajlokTabComponent : DefinedReactComponent<FajlokTabParams>() {
    override fun RBuilder.body(props: FajlokTabParams) {
        val (tabState, setTabState) = useState(FajlokTabComponentState(
                uploadModalVisible = false,
                tartalmazEnergetika = null,
                tartalmazErtekbecsles = null,
                uploadingFiles = emptyList()
        ))
        useEffectWithCleanup(RUN_ONLY_WHEN_MOUNT) {
            addListener("FajlokTab") { megr ->
                if (megr is Megrendeles) {
                    props.setFormState(props.formState.copy(megrendeles = props.formState.megrendeles.copy(
                            ertekbecslesFeltoltve = megr.ertekbecslesFeltoltve,
                            energetikaFeltoltve = megr.energetikaFeltoltve,
                            readByAdmin = megr.readByAdmin,
                            files = megr.files
                    )))
                }
            }
            val cleanup: () -> Unit = { removeListener("FajlokTab") }
            cleanup
        }

        useEffect {
            props.onSaveFunctions[3] = { globalMegrendeles ->
                globalMegrendeles.copy(
                )
            }
        }
        div {
            Collapse {
                attrs.bordered = false
                attrs.defaultActiveKey = arrayOf("Fájl feltöltés", "Feltöltött fájlok")
                Panel("Fájl feltöltés") {
                    attrs.header = StringOrReactElement.fromString("Fájl feltöltés")
                    feltoltesPanel(tabState, setTabState)
                }
                Panel("Feltöltött fájlok") {
                    attrs.header = StringOrReactElement.fromString("Feltöltött fájlok")
                    Row {
                        if (props.formState.megrendelesFieldsFromExcel != null) {
                            Col(span = 14) {
                                fileTable(props)
                            }
                            Col(offset = 1, span = 9) {
                                excelImportResultTable(
                                        props.appState.alvallalkozoState,
                                        props.formState.megrendeles,
                                        props.formState.megrendelesFieldsFromExcel
                                )
                            }
                        } else {
                            Col(offset = 5, span = 14) {
                                fileTable(props)
                            }
                        }
                    }
                }
            }
            uploadModal(tabState, props, setTabState, props.globalDispatch)
        }
    }

    private data class OldNewValues(val name: String, val newValue: String, val oldValue: String) {

    }

    private fun oldNewValues(name: String, newValue: Int?, oldValue: Int?): OldNewValues {
        val formattedNewValue = newValue?.let { parseGroupedStringToNum(it.toString()).second } ?: ""
        val formattedOldValue = oldValue?.let { parseGroupedStringToNum(it.toString()).second } ?: ""
        return OldNewValues(
                name,
                formattedNewValue,
                formattedOldValue
        )
    }

    private fun RBuilder.excelImportResultTable(
            alvallalkozoStore: AlvallalkozoState,
            megr: Megrendeles,
            excel: MegrendelesFieldsFromExternalSource) {
        val columns = arrayOf(
                ColumnProps {
                    title = "Név"
                    dataIndex = "name"
                    width = 100
                    render = { cell: String, row: OldNewValues ->
                        buildElement {
                            div {
                                if (row.oldValue != row.newValue && row.oldValue.isNotEmpty()) {
                                    Tooltip("Jelenlegi érték: ${row.oldValue}") {
                                        Icon("exclamation-circle", style = jsStyle { color = "black"; cursor="pointer" }) {
                                            attrs.asDynamic().theme="twoTone"
                                            attrs.asDynamic().twoToneColor="rgb(235, 148, 6)"
                                        }
                                    }
                                    +" "
                                }
                                span {
                                    attrs.jsStyle = jsStyle { color = "black" }
                                    +cell
                                }
                            }
                        }
                    }
                },
                ColumnProps {
                    title = "Új érték"
                    dataIndex = "newValue"
                    align = ColumnAlign.right
                    width = 50
                    render = { cell: String, _ ->
                        buildElement {
                            span {
                                attrs.jsStyle = jsStyle {
                                    fontFamily = """"Lucida Console", Monaco, monospace"""
                                }
                                +cell
                            }
                        }
                    }
                }
        )
        Table {
            attrs.columns = columns
            attrs.dataSource = arrayOf(
                    OldNewValues("Helyszínelő", excel.helyszinelo ?: "", megr.helyszinelo ?: ""),
                    OldNewValues("Eladási ár", excel.adasvetel ?: "", megr.eladasiAr?.toString() ?: ""),
                    OldNewValues("Adásvétel dátuma", excel.adasvetelDatuma
                            ?: "", megr.adasvetelDatuma?.format(dateFormat) ?: ""),
                    OldNewValues("Értékbecslő", excel.ertekBecslo
                            ?: "", alvallalkozoStore.ertekbecslok[megr.ertekbecsloId]?.name ?: ""),
                    oldNewValues("Fajlagos becsült ár", excel.fajlagosAr?.toInt(), megr.fajlagosEladAr),
                    oldNewValues("Becsült érték", excel.forgalmiErtek?.toInt(), megr.becsultErtek),
                    OldNewValues("Hrsz", excel.hrsz ?: "", megr.hrsz),
                    oldNewValues("Lakás terület", excel.ingatlanTerulet?.toInt(), megr.lakasTerulet),
                    OldNewValues("Lakás típusa", excel.ingatlanTipusa?.toLowerCase()
                            ?: "", megr.ingatlanBovebbTipus.toLowerCase()),
                    oldNewValues("Készültségi fok", excel.keszultsegiFok?.toInt(), megr.keszultsegiFok),
                    OldNewValues("Kapcsolattartó neve", excel.kolcsonIgenylo ?: "", megr.ugyfelNeve),
                    OldNewValues("Szemle időpontja", excel.szemleIdopontja
                            ?: "", megr.szemleIdopontja?.format(dateFormat) ?: ""),
                    oldNewValues("Telek terület", excel.telekMeret?.toInt(), megr.telekTerulet)
            )
            attrs.rowKey = "name"
            attrs.asDynamic().size = "middle"
        }
    }


    private fun RBuilder.fileTable(props: FajlokTabParams) {
        val megr = props.formState.megrendeles
        val columns = arrayOf(
                ColumnProps {
                    title = "Név"
                    dataIndex = "name"
                    width = 100
                },
                ColumnProps {
                    title = "Méret"
                    dataIndex = "humanReadableSize"
                    align = ColumnAlign.right
                    width = 50
                    render = { cell: String, _ ->
                        buildElement {
                            span {
                                attrs.jsStyle = jsStyle {
                                    fontFamily = """"Lucida Console", Monaco, monospace"""
                                }
                                +cell
                            }
                        }
                    }
                },
                ColumnProps {
                    title = ""
                    key = "action"
                    align = ColumnAlign.right
                    width = 150
                    render = { _, fileData: FileData ->
                        buildElement {
                            span {
                                a(href = null) {
                                    attrs.onClickFunction = {
                                        val x = XMLHttpRequest()
                                        val str = fileData.name
                                        val encodedFilename = js("btoa(encodeURIComponent(str))")
                                        val url = "/download/${megr.azonosito}/$encodedFilename/"
                                        x.open("GET", "url", true)
                                        x.responseType = XMLHttpRequestResponseType.BLOB
                                        x.onload = { e ->
                                            downloadFile(e.target.asDynamic().response, fileData.name)
                                        }
                                        x.send()
                                    }
                                    +"Letöltés"
                                }
                                if (fileData.name.endsWith(".xlsx")) {
                                    Divider(type = DividerType.vertical)
                                    a(href = null) {
                                        attrs.onClickFunction = {
                                            communicator.parseExcel(megr.azonosito, fileData.name) { excel ->
                                                props.setFormState(props.formState.copy(
                                                        megrendeles = setMegrendeloFieldsFromExcel(megr, excel, props).copy(
                                                                modified = moment() // so it will trigger a tabState update on the relevant tabs
                                                        ),
                                                        megrendelesFieldsFromExcel = excel
                                                ))
                                            }
                                        }
                                        +"Adatok betöltése"
                                    }
                                }

                            }
                        }
                    }

                }
        )

        Table {
            attrs.columns = columns
            attrs.dataSource = megr.files
            attrs.rowKey = "name"
            attrs.onRow = { account ->
                jsObject {
                    //this.asDynamic().onClick = { globalDispatch(Action.ChangeURL(Path.account.withOpenedEditorModal((account as Account).id))) }
                }
            }
            attrs.asDynamic().size = "middle"
        }
    }

    private fun setMegrendeloFieldsFromExcel(megr: Megrendeles, excel: MegrendelesFieldsFromExternalSource, props: FajlokTabParams): Megrendeles {
        var updatedMegr = megr.copy()
        ifExcelIsFilledButMegrIsNot(excel.helyszinelo ?: "", megr.helyszinelo ?: "") {
            updatedMegr = updatedMegr.copy(helyszinelo = it);
        }
        ifExcelIsFilledButMegrIsNot(excel.adasvetel ?: "", megr.eladasiAr?.toString() ?: "") {
            updatedMegr = updatedMegr.copy(eladasiAr = (it as String?)?.toIntOrNull())
        }
        ifExcelIsFilledButMegrIsNot(excel.adasvetelDatuma ?: "", megr.adasvetelDatuma?.format(dateFormat) ?: "") {
            updatedMegr = updatedMegr.copy(adasvetelDatuma = moment(it, dateFormat))
        }
        ifExcelIsFilledButMegrIsNot(excel.ertekBecslo
                ?: "", props.alvallalkozoStore.ertekbecslok[megr.ertekbecsloId]?.name ?: "") { ebName ->
            updatedMegr = updatedMegr.copy(ertekbecsloId = props.alvallalkozoStore.ertekbecslok.values.first { it.name == ebName }.id)
        }
        //ifExcelIsFilledButMegrIsNot(excel.fajlagosAr, megrendeles.fajlagosEladAr) { megrendeles.fajlagosEladAr = it }
        if (excel.fajlagosAr != null) {
            updatedMegr = updatedMegr.copy(fajlagosBecsultAr = excel.fajlagosAr?.toInt())
        }
        //ifExcelIsFilledButMegrIsNot(excel.szemleIdopontja ?: "", megrendeles.szemleIdopontja?.format(dateFormat) ?: "") { megrendeles.szemleIdopontja = moment(it, dateFormat) }
        if (excel.szemleIdopontja != null) {
            updatedMegr = updatedMegr.copy(szemleIdopontja = moment(excel.szemleIdopontja!!, dateFormat))
        }
        ifExcelIsFilledButMegrIsNot(excel.forgalmiErtek?.toInt(), megr.becsultErtek) {
            updatedMegr = updatedMegr.copy(becsultErtek = it)
        }
        ifExcelIsFilledButMegrIsNot(excel.hrsz ?: "", megr.hrsz) {
            updatedMegr = updatedMegr.copy(hrsz = it)
        }
        ifExcelIsFilledButMegrIsNot(excel.ingatlanTerulet?.toInt(), megr.lakasTerulet?.toInt()) {
            updatedMegr = updatedMegr.copy(lakasTerulet = it)
        }
        ifExcelIsFilledButMegrIsNot(excel.ingatlanTipusa?.toLowerCase()
                ?: "", megr.ingatlanBovebbTipus.toLowerCase()) { tipus ->
            updatedMegr = updatedMegr.copy(ingatlanBovebbTipus = ingatlanBovebbTipusaArray.firstOrNull { it.toLowerCase() == tipus.toLowerCase() }
                    ?: "")
        }
        ifExcelIsFilledButMegrIsNot(excel.keszultsegiFok?.toInt(), megr.keszultsegiFok) { updatedMegr = updatedMegr.copy(keszultsegiFok = it) }
        ifExcelIsFilledButMegrIsNot(excel.kolcsonIgenylo ?: "", megr.ugyfelNeve) {
            updatedMegr = updatedMegr.copy(ugyfelNeve = it)
        }
        ifExcelIsFilledButMegrIsNot(excel.telekMeret?.toInt(), megr.telekTerulet?.toInt()) {
            updatedMegr = updatedMegr.copy(telekTerulet = it)
        }
        return updatedMegr
    }

    private fun Int.toMB() = this / 1048576

    private fun RBuilder.uploadModal(tabState: FajlokTabComponentState,
                                     props: FajlokTabParams,
                                     setTabState: Dispatcher<FajlokTabComponentState>,
                                     globalDispatch: (Action) -> Unit) {
        val megrendeles = props.formState.megrendeles
        Modal {
            attrs.title = StringOrReactElement.fromString("Fájlok feltöltése")
            attrs.visible = tabState.uploadModalVisible
            attrs.okText = "Feltöltés"
            attrs.okButtonProps = jsObject {
                disabled = tabState.uploadingFiles.any { it.size.toMB() > 6 }
            }
            attrs.onOk = {
                communicator.upload(
                        UploadData(
                                megrendeles,
                                tabState.uploadingFiles,
                                tabState.tartalmazErtekbecsles ?: false,
                                tabState.tartalmazEnergetika ?: false
                        )) { response: Megrendeles ->
                    setTabState(tabState.copy(uploadingFiles = emptyList(), uploadModalVisible = false))
                    globalDispatch(Action.MegrendelesekFromServer(arrayOf(response)))
                    message.success("Fájlok sikeresen feltöltve")
                }
            }
            attrs.onCancel = {
                setTabState(tabState.copy(uploadModalVisible = false))
            }
            Form {
                if (megrendeles.munkatipus.isEnergetika()) {
                    FormItem {
                        attrs.label = StringOrReactElement.fromString("A feltölteni kívánt fájlok tartalmazzák az Energetikai tanusítványt")
                        RadioGroup {
                            attrs.value = tabState.tartalmazEnergetika
                            attrs.onChange = { e ->
                                setTabState(tabState.copy(
                                        tartalmazEnergetika = e.target.asDynamic().value
                                ))
                            }
                            Radio(true) { +"Igen" }
                            Radio(false) { +"Nem" }
                        }
                    }
                }
                if (megrendeles.munkatipus.isErtekbecsles()) {
                    FormItem {
                        attrs.label = StringOrReactElement.fromString("A feltölteni kívánt fájlok tartalmazzák az Értékbecslést")
                        RadioGroup {
                            attrs.value = tabState.tartalmazErtekbecsles
                            attrs.onChange = { e ->
                                setTabState(tabState.copy(
                                        tartalmazErtekbecsles = e.target.asDynamic().value
                                ))
                            }
                            Radio(true) { +"Igen" }
                            Radio(false) { +"Nem" }
                        }
                    }
                }
                val energetikaChosen = !megrendeles.munkatipus.isEnergetika() || tabState.tartalmazEnergetika != null
                val ebChosen = !megrendeles.munkatipus.isErtekbecsles() || tabState.tartalmazErtekbecsles != null
                if (energetikaChosen && ebChosen) {
                    uploader(setTabState, tabState)
                }
            }
        }
    }

    private fun RElementBuilder<FormProps>.uploader(setTabState: Dispatcher<FajlokTabComponentState>, tabState: FajlokTabComponentState) {
        Dragger {
            attrs.name = "file"
            attrs.multiple = true
            attrs.onRemove = { removingFile ->
                setTabState(tabState.copy(uploadingFiles = tabState.uploadingFiles.filterIndexed { index, file ->
                    index.toString() != removingFile.uid
                }))
                false
            }
            attrs.fileList = tabState.uploadingFiles.mapIndexed { index, file ->
                val tooBig = file.size.toMB() > 6
                DefaultFileListItem(
                        uid = index.toString(),
                        name = file.name + if (tooBig) "(${file.size.toMB()} MB)" else "",
                        status = if (tooBig) "error" else "done",
                        response = if (tooBig) "A fájlok maximális mérete 6 MB lehet!" else null,
                        url = "#"
                )
            }.toTypedArray()
            attrs.action = { file: File ->
                setTabState(tabState.copy(uploadingFiles = tabState.uploadingFiles + file))
                Promise<Any> { resolve, reject ->
                    resolve(0)
                }
            }
            attrs.onChange = { info ->

            }
            p(classes = "ant-upload-drag-icon") {
                Icon("inbox")
            }
            p(classes = "ant-upload-text") {
                +"Kattints vagy mozgass egy fájlt ide a feltöltéshez"
            }
            p(classes = "ant-upload-hint") {
                +"Support for a single or bulk upload. Strictly prohibit from uploading company data or other band files"
            }
        }
    }
}

private fun ifExcelIsFilledButMegrIsNot(newValue: String, oldValue: String, callIfEquals: (newValue: String) -> Unit) {
    if (oldValue.isEmpty() && newValue.isNotEmpty()) {
        callIfEquals(newValue)
    }
}

private fun ifExcelIsFilledButMegrIsNot(newValue: Int?, oldValue: Int?, callIfEquals: (newValue: Int) -> Unit) {
    if (oldValue == null && newValue != null) {
        callIfEquals(newValue)
    }
}

private fun RBuilder.feltoltesPanel(state: FajlokTabComponentState, setTabState: Dispatcher<FajlokTabComponentState>) {
    Button {
        attrs.type = ButtonType.default
        attrs.onClick = {
            setTabState(state.copy(uploadModalVisible = true))
        }
        Icon("upload")
        +" Fájlok feltöltése"
    }
}