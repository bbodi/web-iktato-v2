package hu.nevermind.utils.app

import app.*
import app.common.Granularity
import app.common.TimeUnit
import app.common.moment
import app.megrendeles.MegrendelesScreenIds
import app.megrendeles.getNextWeekDay
import hu.nevermind.iktato.Path
import hu.nevermind.utils.hu.nevermind.antd.message
import hu.nevermind.utils.store.*
import jquery.jq
import org.w3c.dom.*
import store.Action
import kotlin.browser.document
import kotlin.browser.window
import kotlin.dom.hasClass
import kotlin.js.*

private val ReactTestUtils = kotlinext.js.require("react-dom/test-utils")

open class TestBuilder(val description: TestDescription, val afterItFinished: () -> Unit) {

    var testIsHalted = false
    var callbackAfterHaltedTestCompletion: () -> Unit = { error("'testDone()' were called before 'haltTestingUntilExternalEvent()'") }

    fun haltTestingUntilExternalEvent() {
        console.info("[TEST] ---> HALTED")
        callbackAfterHaltedTestCompletion = {
            afterItFinished()
        }
        testIsHalted = true
    }

    fun testDone() {
        later(100) {
            // a testDone általában a Store emitChange-ét lekezelő metódusban fut, ahol viszont nem történhet újabb emittálás
            callbackAfterHaltedTestCompletion()
        }
    }

    fun assertTrue(b: Boolean) {
        if (!b) {
            js("debugger")
            error("${description.toString()}\n expected: true, but got false")
        }
    }

    fun assertFalse(b: Boolean) {
        if (b) {
            js("debugger")
            error("${description.toString()}\n expected: false, but got true")
        }
    }

    fun <T> assertEquals(expected: T, actual: T) {
        if (expected != actual) {
            js("debugger")
            error("${description.toString()}\n expected: $expected, but got $actual")
        }
    }
}

data class OnDef(val descr: String, val delayAfterOn: Int, val body: OnBuilder.() -> Unit)
class GivenBuilder(val body: GivenBuilder.() -> Unit, val delayAfterGivenBody: Int, val description: TestDescription) {

    private var ons = arrayListOf<OnDef>()

    fun on(onDescr: String, delayAfterOn: Int = 100, body: OnBuilder.() -> Unit) {
        ons.add(OnDef(onDescr, delayAfterOn, body))
    }

    fun runFirstOn(afterNoMoreOnToRun: () -> Unit, onFinally: () -> Unit) {
        val onDef = ons.firstOrNull()
        if (onDef == null) {
            afterNoMoreOnToRun()
            return
        }
        val afterNoMoreItToRun = {
            runFirstOn(afterNoMoreOnToRun, onFinally)
        }
        var afterItFinished: (OnBuilder) -> Unit = {}
        afterItFinished = { on: OnBuilder ->
            console.info("[TEST] ---> SUCCEED")
            on.removeFirstIt()
            try {
                on.runFirstIt(afterItFinished, afterNoMoreItToRun)
            } catch (e: Throwable) {
                onFinally()
            }
        }
        ons.removeAt(0)
        val on = OnBuilder(description.copy(on = onDef.descr))
        val runBodyAndCollectIts = onDef.body
        runGivenBodyToInitializeEnvironmentForOns()
        later(delayAfterGivenBody) {
            on.runBodyAndCollectIts()
            later(onDef.delayAfterOn) {
                on.runFirstIt(afterItFinished, afterNoMoreItToRun)
            }
        }
    }

    fun runGivenBodyToInitializeEnvironmentForOns() {
        val tmpGiven = GivenBuilder(body, 0, description)
        tmpGiven.body()
    }
}

class OnBuilder(val description: TestDescription) {

    private val its = arrayListOf<Pair<String, TestBuilder.() -> Unit>>()

    fun removeFirstIt() {
        its.removeAt(0)
    }

    fun it(itDescr: String, body: TestBuilder.() -> Unit) {
        its.add(itDescr to body)
    }

    fun runFirstIt(afterItFinished: (OnBuilder) -> Unit, afterNoMoreItToRun: () -> Unit) {
        val itDef = its.firstOrNull()
        if (itDef == null) {
            afterNoMoreItToRun()
            return
        }
        val it = TestBuilder(description.copy(it = itDef.first), { afterItFinished(this) })
        val body = itDef.second
        console.info("[TEST] ${it.description}")
        try {
            it.body()
            if (!it.testIsHalted) {
                afterItFinished(this)
            }
        } catch (e: dynamic) {
            console.error("[TEST] Error during test: ${it.description}", e)
            throw e;
        }
    }
}

data class TestDescription(val given: String, val on: String?, val it: String?)

fun runFirstGiven(onFinally: () -> Unit) {
    val givenDef = givens.firstOrNull()
    if (givenDef == null) {
        console.log("[TEST] ALL TESTS SUCCEED")
        message.success("[TEST] ALL TESTS SUCCEED")
        onFinally()
        return
    }
    givens.removeAt(0)
    val body = givenDef.body
    val given = GivenBuilder(body, givenDef.delayAfterGivenBody, TestDescription(givenDef.descr, null, null))
    try {
        given.body()
        given.runFirstOn(afterNoMoreOnToRun = { runFirstGiven(onFinally) },
                onFinally = onFinally)
    } catch (e: Throwable) {
        onFinally()
    }
}

data class GivenDef(val descr: String, val delayAfterGivenBody: Int, val body: GivenBuilder.() -> Unit)

private val givens = arrayListOf<GivenDef>()

fun given(description: String, delay: Int = 100, body: GivenBuilder.() -> Unit) {
    givens.add(GivenDef(description, delay, body))
}

fun later(delay: Int = 100, body: () -> Unit) {
    window.setTimeout({
        body()
    }, delay)
}

fun simulateChangeAndSetValue(id: String, body: () -> String = { "" }) {
    simulateChangeInput(id) { input ->
        val str = body()
        if (input != null) {
            input.asDynamic().value = str
        }
        str
    }
}

fun htmlEncode(html: String): String {
    return document.createElement("a").appendChild(
            document.createTextNode(html)).parentNode.asDynamic().innerHTML;
}

fun simulateChangeInput(id: String, body: (Element?) -> String = { "" }) {
    try {
        val input: Element = document.getElementById(id)!!.let { element ->
            if (element !is HTMLInputElement && element !is HTMLTextAreaElement) {
                val autoCompletionInput = element.getElementsByTagName("input")[0]
                if (autoCompletionInput != null && autoCompletionInput != undefined) {
                    autoCompletionInput as HTMLInputElement
                } else {
                    // Select
                    ReactTestUtils.Simulate.click(element);
                    val options = document.getElementsByTagName("li").asList()
                            .filter { it is Element }
                            .map { it as Element }
                            .filter { it.hasClass("ant-select-dropdown-menu-item") }
                    val dropDownItem = options
                            .filter { it.innerHTML == htmlEncode(body(null)) }
                            .firstOrNull()
                    if (dropDownItem == null) {
                        val liElements = options.map { it.innerHTML }
                        val expectedId = id
                        val expectedElement = element
                        val expectedItemNotFound = htmlEncode(body(null))
                        js("debugger")
                    } else {
                        ReactTestUtils.Simulate.click(dropDownItem);
                    }
                    return
                }
            } else element
        }
        require(input.getAttribute("disabled") != "disabled") { "$id is disabled, cannot change its value!" }
        require(input.getAttribute("readOnly") != "readOnly") { "$id is readOnly, cannot change its value!" }
        body(input)
        ReactTestUtils.Simulate.change(input);
    } catch (e: Throwable) {
        js("debugger")
        throw e;
    }
}

fun String.appearOnScreen(): Boolean = document.getElementById(this) != null

fun String.simulateClick() {
    try {
        ReactTestUtils.Simulate.click(document.getElementById(this))
    } catch (e: Throwable) {
        js("debugger")
        throw e;
    }
}

fun String.checkboxClick() {
    try {
        document.getElementById(this).asDynamic().click()
    } catch (e: Throwable) {
        js("debugger")
        throw e;
    }
}

fun String.inputElementValue(): String? {
    try {
        val element = document.getElementById(this)!!
        if (element.hasClass("ant-select")) {
            return element.children.get(0)!!.children.get(0)!!.children.get(0)!!.innerHTML
        } else {
            error(element)
        }
    } catch (e: Throwable) {
        js("debugger")
        throw e;
    }
}

fun String.simulateEnter() {
    ReactTestUtils.Simulate.keyDown(document.getElementById(this), object {
        val key = "Enter"
        val keyCode = 13
        val which = 13
    })
}

fun tableCellValue(row: Int, column: Int): String {
    return tableCell(row, column)
            .textContent!!
}

private fun tableCell(row: Int, column: Int): Element {
    return tableRows().get(row)!!
            .getElementsByTagName("td").get(column)!!
}

private fun tableRows() = document.getElementsByTagName("tbody").get(0)!!
        .getElementsByTagName("tr")

fun Element?.simulateClick() {
    ReactTestUtils.Simulate.click(this)
}

fun runIntegrationTests(globalDispatcher: Dispatcher<Action>, appState: AppState) {
    val testMegrendelo = "TestMegrendelő"
    val testMunkatipus = "TestMunkatipus"
    val testAlvallalkozName = "TestAlvállalkozó"
    var testLeiras = "Test leírás"
    var testNettoAr = 10000
    var testAfa = 20
    var sajatArId = 0
    var alvallalkozId = 0
    var regioId = 0
    var regioId2 = 0
    var newMegrendelesId = 0
    val azonosito = Date().getTime().toString()
    given("SajatÁr is open") {
        globalDispatcher(Action.ChangeURL(Path.sajatAr.root))

        on("Clicking on 'Add' button") {
            SajatArScreenIds.addButton.simulateClick()

            it("should open the Modal") {
                assertTrue(SajatArScreenIds.modal.id.appearOnScreen())
            }
        }

        on("Create new Megrendelő, Munkatipus, SajatAr") {
            SajatArScreenIds.addButton.simulateClick()

            simulateChangeAndSetValue(SajatArScreenIds.modal.input.megrendelo) { testMegrendelo }
            simulateChangeAndSetValue(SajatArScreenIds.modal.input.munkatipus) { testMunkatipus }
            simulateChangeAndSetValue(SajatArScreenIds.modal.input.leiras) { testLeiras }
            simulateChangeAndSetValue(SajatArScreenIds.modal.input.nettoAr) { testNettoAr.toString() }
            simulateChangeAndSetValue(SajatArScreenIds.modal.input.afa) { testAfa.toString() }

            it("The server should send back the modified entity") {
                globalEventListeners.add { appState ->
                    sajatArId = appState.sajatArState.sajatArak.keys.sortedDescending().first()
                    val sajatAr = appState.sajatArState.sajatArak[sajatArId]!!
                    assertEquals(testMegrendelo, sajatAr.megrendelo)
                    assertEquals(testMunkatipus, sajatAr.munkatipus)
                    assertEquals(testLeiras, sajatAr.leiras)
                    assertEquals(testNettoAr, sajatAr.nettoAr)
                    assertEquals(testAfa, sajatAr.afa)

                    globalEventListeners.removeAt(globalEventListeners.lastIndex)
                    testDone()
                }
                haltTestingUntilExternalEvent()
                SajatArScreenIds.modal.button.save.simulateClick()
            }
            it("The modal should be closed") {
                assertFalse(SajatArScreenIds.modal.id.appearOnScreen())
            }
            it("The created megrendelő and munkatipus is selected") {
                assertEquals(testMegrendelo, SajatArScreenIds.megrendeloSelect.inputElementValue())
                assertEquals(testMunkatipus, SajatArScreenIds.munkatipusSelect.inputElementValue())
            }
            it("The created SajatAr appears in the table") {
                assertEquals(testLeiras, tableCellValue(0, 0))
                assertEquals("10 000", tableCellValue(0, 1))
                assertEquals("20", tableCellValue(0, 2))
            }
        }
        on("Modifying Megrendelő") {
            if (tableRows().length != 1) {
                error("Ezen teszt működéséhez csak 1 sor lehet a Test táblázatban, töröld a többit manuálisan DB-ből. Ezzel: " +
                        "delete from sajat_arak where leiras like 'Test le%'!")

            }
            tableRows().get(0).simulateClick()

            testLeiras += "_modified"
            testNettoAr += 1000
            testAfa += 10

            simulateChangeAndSetValue(SajatArScreenIds.modal.input.leiras) { testLeiras }
            simulateChangeAndSetValue(SajatArScreenIds.modal.input.nettoAr) { testNettoAr.toString() }
            simulateChangeAndSetValue(SajatArScreenIds.modal.input.afa) { testAfa.toString() }

            it("The server should send back the modified entity") {
                globalEventListeners.add { appState ->
                    val sajatAr = appState.sajatArState.sajatArak[sajatArId]!!
                    assertEquals(testMegrendelo, sajatAr.megrendelo)
                    assertEquals(testMunkatipus, sajatAr.munkatipus)
                    assertEquals(testLeiras, sajatAr.leiras)
                    assertEquals(testNettoAr, sajatAr.nettoAr)
                    assertEquals(testAfa, sajatAr.afa)

                    globalEventListeners.removeAt(globalEventListeners.lastIndex)
                    testDone()
                }
                haltTestingUntilExternalEvent()
                SajatArScreenIds.modal.button.save.simulateClick()
            }
        }
    }
    given("Alvállalkozó screen is open") {
        globalDispatcher(Action.ChangeURL(Path.alvallalkozo.root))

        on("Clicking on Add button") {
            AlvallalkozoScreenIds.addButton.simulateClick()
            it("should show the modal window") {
                assertTrue(AlvallalkozoScreenIds.modal.id.appearOnScreen())
            }
            it("The Save button is disabled by default") {
                assertHasAttribute(AlvallalkozoScreenIds.modal.buttons.save, "disabled")
            }
        }

        on("Creating a new alvállalkozó") {
            AlvallalkozoScreenIds.addButton.simulateClick()
            simulateChangeAndSetValue(AlvallalkozoScreenIds.modal.inputs.name) { testAlvallalkozName }
            simulateChangeAndSetValue(AlvallalkozoScreenIds.modal.inputs.phone) { "443650" }
            simulateChangeAndSetValue(AlvallalkozoScreenIds.modal.inputs.szamlaszam) { "1234" }
            simulateChangeAndSetValue(AlvallalkozoScreenIds.modal.inputs.adoszam) { "4321" }
            simulateChangeAndSetValue(AlvallalkozoScreenIds.modal.inputs.kapcsolatTarto) { "Kapcsolattartó" }
            simulateChangeAndSetValue(AlvallalkozoScreenIds.modal.inputs.email) { "email@asd.com" }
            simulateChangeAndSetValue(AlvallalkozoScreenIds.modal.inputs.cim) { "Cím" }
            simulateChangeAndSetValue(AlvallalkozoScreenIds.modal.inputs.tagsagiSzam) { "6789" }

            it("The server should send back the modified entity") {
                globalEventListeners.add { appState ->
                    alvallalkozId = appState.alvallalkozoState.alvallalkozok.keys.sortedDescending().first()
                    val alv = appState.alvallalkozoState.alvallalkozok[alvallalkozId]!!
                    assertEquals(testAlvallalkozName, alv.name)
                    assertEquals("443650", alv.phone)
                    assertEquals("4321", alv.adoszam)
                    assertEquals("1234", alv.szamlaSzam)
                    assertEquals("Kapcsolattartó", alv.kapcsolatTarto)
                    assertEquals("email@asd.com", alv.email)
                    assertEquals("Cím", alv.cim)
                    assertEquals("6789", alv.tagsagiSzam)
                    assertEquals(false, alv.keszpenzes)

                    globalEventListeners.removeAt(globalEventListeners.lastIndex)
                    testDone()
                }
                haltTestingUntilExternalEvent()
                AlvallalkozoScreenIds.modal.buttons.save.simulateClick()
            }
            it("The created entity should appear in the table") {
                val id = AlvallalkozoScreenIds.table.nameSearchButton
                id.simulateClick()
                simulateChangeAndSetValue("${id}_input") { testAlvallalkozName }
                "${id}_search_button".simulateClick()
                assertEquals(testAlvallalkozName, tableCellValue(0, 0))
                assertEquals("443650", tableCellValue(0, 1))
                assertEquals("Kapcsolattartó", tableCellValue(0, 2))
                assertEquals("6789", tableCellValue(0, 3))
                assertEquals("Cím", tableCellValue(0, 4))
            }
        }
        on("Modifying existing alvállalkozó") {
            if (tableRows().length != 1) {
                error("Ezen teszt működéséhez csak 1 sor lehet a Test táblázatban, töröld a többit manuálisan DB-ből. Ezzel: " +
                        "delete from alvallalkozo where cegnev like '%TestAlvállalkozó%' !")
            }
            tableRows().get(0).simulateClick()
            AlvallalkozoScreenIds.table.row.editButton(0).simulateClick()
            simulateChangeAndSetValue(AlvallalkozoScreenIds.modal.inputs.phone) { "11443650" }
            simulateChangeAndSetValue(AlvallalkozoScreenIds.modal.inputs.szamlaszam) { "111234" }
            simulateChangeAndSetValue(AlvallalkozoScreenIds.modal.inputs.adoszam) { "114321" }
            simulateChangeAndSetValue(AlvallalkozoScreenIds.modal.inputs.kapcsolatTarto) { "11Kapcsolattartó" }
            simulateChangeAndSetValue(AlvallalkozoScreenIds.modal.inputs.email) { "11email@asd.com" }
            simulateChangeAndSetValue(AlvallalkozoScreenIds.modal.inputs.cim) { "11Cím" }
            simulateChangeAndSetValue(AlvallalkozoScreenIds.modal.inputs.tagsagiSzam) { "116789" }
            AlvallalkozoScreenIds.modal.inputs.keszpenzes.checkboxClick()

            it("The server should send back the modified entity") {
                globalEventListeners.add { appState ->
                    val alv = appState.alvallalkozoState.alvallalkozok[alvallalkozId]!!
                    assertEquals(testAlvallalkozName, alv.name)
                    assertEquals("11443650", alv.phone)
                    assertEquals("114321", alv.adoszam)
                    assertEquals("111234", alv.szamlaSzam)
                    assertEquals("11Kapcsolattartó", alv.kapcsolatTarto)
                    assertEquals("11email@asd.com", alv.email)
                    assertEquals("11Cím", alv.cim)
                    assertEquals("116789", alv.tagsagiSzam)
                    assertEquals(true, alv.keszpenzes)

                    globalEventListeners.removeAt(globalEventListeners.lastIndex)
                    testDone()
                }
                haltTestingUntilExternalEvent()
                AlvallalkozoScreenIds.modal.buttons.save.simulateClick()
            }
        }
    }

    given("Régió screen is open for the created Alvállalkozó") {
        globalDispatcher(Action.ChangeURL(Path.alvallalkozo.regio(alvallalkozId)))

        on("Clicking on Add button") {
            RegioScreenIds.addButton.simulateClick()
            it("should show the modal window") {
                assertTrue(RegioScreenIds.modal.id.appearOnScreen())
            }
            it("The Save button is disabled by default") {
                assertHasAttribute(RegioScreenIds.modal.buttons.save, "disabled")
            }
        }

        on("Creating a new Régió") {
            RegioScreenIds.addButton.simulateClick()
            simulateChangeAndSetValue(RegioScreenIds.modal.inputs.munkatipus) { Munkatipusok.Ertekbecsles.str }
            simulateChangeAndSetValue(RegioScreenIds.modal.inputs.leiras) { "test leírás" }
            simulateChangeAndSetValue(RegioScreenIds.modal.inputs.nettoAr) { "10000" }
            simulateChangeAndSetValue(RegioScreenIds.modal.inputs.jutalek) { "20000" }
            simulateChangeAndSetValue(RegioScreenIds.modal.inputs.afa) { "27" }

            it("The server should send back the created entity") {
                globalEventListeners.add { appState ->
                    assertEquals(1, appState.alvallalkozoState.getRegioOsszerendelesek(alvallalkozId).size)

                    val regio = appState.alvallalkozoState.getRegioOsszerendelesek(alvallalkozId).first()
                    regioId = regio.id
                    assertEquals("Baranya", regio.megye)
                    assertEquals("Értékbecslés", regio.munkatipus)
                    assertEquals("test leírás", regio.leiras)
                    assertEquals(10000, regio.nettoAr)
                    assertEquals(20000, regio.jutalek)
                    assertEquals(27, regio.afa)

                    globalEventListeners.removeAt(globalEventListeners.lastIndex)
                    testDone()
                }
                haltTestingUntilExternalEvent()
                RegioScreenIds.modal.buttons.save.simulateClick()
            }
            it("The created entity should appear in the table") {
                assertEquals(1, tableRows().length)
                assertEquals("Értékbecslés", tableCellValue(0, 0))
                assertEquals("test leírás", tableCellValue(0, 1))
                assertEquals("10 000", tableCellValue(0, 2))
                assertEquals("20 000", tableCellValue(0, 3))
                assertEquals("27", tableCellValue(0, 4))
            }
        }
        on("Creating a second new Régió") {
            RegioScreenIds.addButton.simulateClick()
            simulateChangeAndSetValue(RegioScreenIds.modal.inputs.munkatipus) { Munkatipusok.Energetika.str }
            simulateChangeAndSetValue(RegioScreenIds.modal.inputs.leiras) { "test másik leírás" }
            simulateChangeAndSetValue(RegioScreenIds.modal.inputs.nettoAr) { "90000" }
            simulateChangeAndSetValue(RegioScreenIds.modal.inputs.jutalek) { "80000" }
            simulateChangeAndSetValue(RegioScreenIds.modal.inputs.afa) { "72" }

            it("The server should send back the created entity") {
                globalEventListeners.add { appState ->
                    assertEquals(2, appState.alvallalkozoState.getRegioOsszerendelesek(alvallalkozId).size)

                    val regio = appState.alvallalkozoState.getRegioOsszerendelesek(alvallalkozId)[1]
                    regioId2 = regio.id
                    assertEquals("Baranya", regio.megye)
                    assertEquals("Energetika", regio.munkatipus)
                    assertEquals("test másik leírás", regio.leiras)
                    assertEquals(90000, regio.nettoAr)
                    assertEquals(80000, regio.jutalek)
                    assertEquals(72, regio.afa)

                    globalEventListeners.removeAt(globalEventListeners.lastIndex)
                    testDone()
                }
                haltTestingUntilExternalEvent()
                RegioScreenIds.modal.buttons.save.simulateClick()
            }
            it("The table has to contain two rows") {
                assertEquals(2, tableRows().length)
            }
            it("The already existing regio should not change") {
                assertEquals("Értékbecslés", tableCellValue(0, 0))
                assertEquals("test leírás", tableCellValue(0, 1))
                assertEquals("10 000", tableCellValue(0, 2))
                assertEquals("20 000", tableCellValue(0, 3))
                assertEquals("27", tableCellValue(0, 4))
            }
            it("The created entity should appear in the table") {
                assertEquals("Energetika", tableCellValue(1, 0))
                assertEquals("test másik leírás", tableCellValue(1, 1))
                assertEquals("90 000", tableCellValue(1, 2))
                assertEquals("80 000", tableCellValue(1, 3))
                assertEquals("72", tableCellValue(1, 4))
            }
        }
        on("Modifying existing Régió") {
            RegioScreenIds.table.row.editButton(0).simulateClick()
            simulateChangeAndSetValue(RegioScreenIds.modal.inputs.munkatipus) { Munkatipusok.EnergetikaAndErtekBecsles.str }
            simulateChangeAndSetValue(RegioScreenIds.modal.inputs.leiras) { "test leírás - mod" }
            simulateChangeAndSetValue(RegioScreenIds.modal.inputs.nettoAr) { "120000" }
            simulateChangeAndSetValue(RegioScreenIds.modal.inputs.jutalek) { "230000" }
            simulateChangeAndSetValue(RegioScreenIds.modal.inputs.afa) { "27" }

            it("The server should send back the modified entity") {
                globalEventListeners.add { appState ->
                    val regio = appState.alvallalkozoState.getRegioOsszerendelesek(alvallalkozId).first()
                    assertEquals("Baranya", regio.megye)
                    assertEquals("Energetika&Értékbecslés", regio.munkatipus)
                    assertEquals("test leírás - mod", regio.leiras)
                    assertEquals(120000, regio.nettoAr)
                    assertEquals(230000, regio.jutalek)
                    assertEquals(27, regio.afa)

                    globalEventListeners.removeAt(globalEventListeners.lastIndex)
                    testDone()
                }
                haltTestingUntilExternalEvent()
                RegioScreenIds.modal.buttons.save.simulateClick()
            }
            it("The modified entity should appear in the table") {
                assertEquals("Energetika&Értékbecslés", tableCellValue(0, 0))
                assertEquals("test leírás - mod", tableCellValue(0, 1))
                assertEquals("120 000", tableCellValue(0, 2))
                assertEquals("230 000", tableCellValue(0, 3))
                assertEquals("27", tableCellValue(0, 4))
            }
            it("The unmodified entity (second row) should not change") {
                assertEquals("Energetika", tableCellValue(1, 0))
                assertEquals("test másik leírás", tableCellValue(1, 1))
                assertEquals("90 000", tableCellValue(1, 2))
                assertEquals("80 000", tableCellValue(1, 3))
                assertEquals("72", tableCellValue(1, 4))
            }
        }
        on("Modifying existing Régió, afa cannot be greater than 100") {
            RegioScreenIds.table.row.editButton(0).simulateClick()
            simulateChangeAndSetValue(RegioScreenIds.modal.inputs.afa) { "340000" }

            it("The server should send back the modified entity") {
                globalEventListeners.add { appState ->
                    val regio = appState.alvallalkozoState.getRegioOsszerendelesek(alvallalkozId).first()
                    assertEquals(100, regio.afa)

                    globalEventListeners.removeAt(globalEventListeners.lastIndex)
                    testDone()
                }
                haltTestingUntilExternalEvent()
                RegioScreenIds.modal.buttons.save.simulateClick()
            }
            it("The modified entity should appear in the table") {
                assertEquals("100", tableCellValue(0, 4))
            }
        }
        on("Deleting a Regio") {
            RegioScreenIds.table.row.deleteButton(1).simulateClick()
            it("The delete modal should pop up") {
                val confirmYesButton = document.getElementsByClassName("ant-modal-confirm-btns").get(0)?.children?.get(1)
                assertTrue(confirmYesButton != null)
            }
        }
        on("Deleting clicking the Yes button e.g. deleting a regio") {
            val confirmYesButton = document.getElementsByClassName("ant-modal-confirm-btns").get(0)?.children?.get(1)

            it("The server should send back the modified entity") {
                globalEventListeners.add { appState ->
                    assertEquals(1, appState.alvallalkozoState.getRegioOsszerendelesek(alvallalkozId).size)

                    globalEventListeners.removeAt(globalEventListeners.lastIndex)
                    testDone()
                }
                haltTestingUntilExternalEvent()
                confirmYesButton.simulateClick()
            }
            it("The deleted entity must be removed from the table") {
                assertEquals(1, tableRows().length)
            }
            it("The other regio should stay in the table") {
                assertEquals("Energetika&Értékbecslés", tableCellValue(0, 0))
                assertEquals("test leírás - mod", tableCellValue(0, 1))
                assertEquals("120 000", tableCellValue(0, 2))
                assertEquals("230 000", tableCellValue(0, 3))
                assertEquals("100", tableCellValue(0, 4))
            }
        }
    }

    runFirstGiven(onFinally = {
//        if (sajatArId != 0) {
//            communicator.deleteEntity("SajatAr", sajatArId)
//        }
//        if (newMegrendelesId != 0) {
//            communicator.deleteEntity("Megrendeles", newMegrendelesId)
//        }
//        if (regioId != 0) {
//            communicator.deleteEntity("RegioOsszerendeles", regioId)
//        }
//        if (regioId2 != 0) {
//            communicator.deleteEntity("RegioOsszerendeles", regioId2)
//        }
//        if (alvallalkozId != 0) {
//            communicator.deleteEntity("Alvallalkozo", alvallalkozId)
//        }
    })
    if (true) {
        return
    }
    given("test creating NEW Megrendeles with only the mandatory fields") {
        globalDispatcher(Action.ChangeURL(Path.megrendeles.edit(0)))

        on("Click on Save button after fields were modified") {
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.megrendelo) { "Fundamenta Zrt." }
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.regio) { "Baranya" }
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.munkatipus) { Munkatipusok.Ertekbecsles.str }
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.alvallalkozo) { "24" } // Test Kft.
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.ingatlanTipusMunkadijMeghatarozasahoz) { "1" } // Panel lakás
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.fovallalkozo) { "Presting Zrt." }
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.hitelTipus) { "vásárlás" }
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.helyrajziSzam) { "1234" }
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.iranyitoszam) { "7636" }
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.telepules) { "Pécs" }
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.kozteruletNeve) { "Illyés Gy." }
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.hitelOsszege) { "12000000" }

            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.ugyfelNev) { "Ügyfél neve" }
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.ugyfelTel) { "Ügyfél száma" }
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.ebAzonosito) { azonosito }

            it("The server should send back the modified entity") {
                val prevMinute = moment().subtract(1, TimeUnit.Minutes)
                val nextMinute = moment().add(1, TimeUnit.Minutes)
                globalEventListeners.add { appState ->
                    newMegrendelesId = appState.megrendelesState.megrendelesek.keys.sortedDescending().first()
                    val megr: Megrendeles = appState.megrendelesState.megrendelesek[newMegrendelesId]!!
                    assertTrue(megr.modified.isBetween(prevMinute, nextMinute))
                    assertTrue(megr.created.isBetween(prevMinute, nextMinute) || megr.created.isSame(prevMinute, Granularity.Minute))
                    assertTrue(megr.rogzitve!!.isBetween(prevMinute, nextMinute) || megr.rogzitve!!.isSame(prevMinute, Granularity.Minute))
                    assertEquals("Fundamenta Zrt.", megr.megrendelo)
                    assertEquals("Baranya", megr.regio)
                    assertEquals(Munkatipusok.Ertekbecsles.str, megr.munkatipus)
                    assertEquals("Presting Zrt.", megr.foVallalkozo)
                    assertEquals(24, megr.alvallalkozoId)
                    assertEquals(67, megr.ertekbecsloId)
                    assertEquals("vásárlás", megr.hitelTipus)

                    assertEquals("1234", megr.hrsz)
                    assertEquals("7636", megr.irsz)
                    assertEquals("Pécs", megr.telepules)

                    assertEquals(12000000, megr.hitelOsszeg)

                    assertEquals("Ügyfél neve", megr.ugyfelNeve)
                    assertEquals("Ügyfél száma", megr.ugyfelTel)
                    assertEquals(azonosito, megr.azonosito)

                    assertEquals("Ügyfél neve", megr.ertesitesiNev)
                    assertEquals("Ügyfél száma", megr.ertesitesiTel)
                    assertEquals("", megr.ugyfelEmail)
                    assertEquals("Panel lakás", megr.ingatlanTipusMunkadijMeghatarozasahoz)
                    assertEquals(10, megr.ertekbecsloDija)
                    assertEquals(18000, megr.szamlazhatoDij)
                    assertEquals(null, megr.helyszinelo)
                    assertEquals("lakás", megr.ingatlanBovebbTipus)
                    assertEquals(null, megr.keszultsegiFok)
                    assertEquals(null, megr.lakasTerulet)
                    assertEquals(null, megr.telekTerulet)
                    assertEquals(null, megr.becsultErtek)
                    assertEquals(null, megr.eladasiAr)
                    assertEquals(null, megr.fajlagosBecsultAr)
                    assertEquals(null, megr.fajlagosEladAr)
                    assertEquals(false, megr.ellenorizve)
                    assertEquals(Statusz.B1, megr.statusz)

                    assertEquals("", megr.kerulet)
                    assertEquals("Illyés Gy.", megr.utca)
                    assertEquals("", megr.utcaJelleg)
                    assertEquals("", megr.hazszam)
                    assertEquals("", megr.lepcsohaz)
                    assertEquals("", megr.emelet)
                    assertEquals("", megr.ajto)
                    assertEquals("", megr.ajanlatSzam)
                    assertEquals("", megr.szerzodesSzam)
                    assertEquals(null, megr.hetKod)
                    assertEquals("", megr.szamlaSorszama)

                    assertTrue(moment().isSame(megr.megrendelve!!, Granularity.Day))
                    assertTrue(getNextWeekDay(5).isSame(megr.hatarido!!, Granularity.Day))
                    assertEquals(null, megr.megrendelesMegtekint)
                    assertEquals(null, megr.energetikaFeltoltve)
                    assertEquals(null, megr.ertekbecslesFeltoltve)
                    assertEquals(null, megr.szemleIdopontja)
                    assertEquals(null, megr.feltoltveMegrendelonek)
                    assertEquals(null, megr.zarolva)
                    assertEquals(null, megr.keszpenzesBefizetes)
                    assertEquals(null, megr.penzBeerkezettDatum)
                    assertEquals(null, megr.adasvetelDatuma)

                    globalEventListeners.removeAt(globalEventListeners.lastIndex)
                    testDone()
                }
                haltTestingUntilExternalEvent()
                MegrendelesScreenIds.modal.button.save.simulateClick()
            }
        }
    }
    given("test creating NEW Megrendeles by importing email texts") {
        globalDispatcher(Action.ChangeURL(Path.megrendeles.edit(0)))

        on("Importing EB text") {
            MegrendelesScreenIds.modal.button.import.simulateClick()
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.importTextArea) {
                """Tisztelt Partnerünk!

Ezúton küldjük Önnek megrendelésünket, 950/2 HRSZ számú, természetben 8181 Berhida, Kálvin tér 7. sz. alatt található ingatlannal kapcsolatban az alábbi feladat(ok) elvégzésére. A teljesítéshez szükséges dokumentumokat kérje az ügyféltől.
Elvégzendő feladatok:
- Értékbecslés készítése:
• Azonosítója: 143821
• Határideje: 2016.05.23.
Megrendelés adatai:
Hiteligénylő neve: Major Tamásné
Hiteligénylő telefonszáma: +36305017894
Kapcsolattartó neve: Major Tamásné
Kapcsolattartó telefonszáma: +36305017894
Lakáscél: felújítás
Felvenni kívánt hitel összege: 6250000
Ajánlatszám: 323878919
Szerződésszám: 4294997
Az elkészült feladato(ka)t - a szükséges mellékletekkel és/vagy kiegészítő adatokkal együtt -, kérjük legkésőbb a fent megjelölt határidőig portálunkra feltölteni, illetve az ügyfélnek átadni.
Üdvözlettel,
Fundamenta-Lakáskassza Zrt."""
            }

            it("Az alapadatok tabon a megfelelő értékek kellenek beírva legyenek") {
                (MegrendelesScreenIds.modal.tab.first + "___tab").simulateClick()

                assertEquals("2016-05-23", MegrendelesScreenIds.modal.input.hatarido.inputElementValue())
                assertEquals(Munkatipusok.Ertekbecsles.str, MegrendelesScreenIds.modal.input.munkatipus.inputElementValue())
                assertEquals("Major Tamásné", MegrendelesScreenIds.modal.input.ugyfelNev.inputElementValue())
                assertEquals("+36305017894", MegrendelesScreenIds.modal.input.ugyfelTel.inputElementValue())
                assertEquals("Major Tamásné", MegrendelesScreenIds.modal.input.ertesitendoNev.inputElementValue())
                assertEquals("+36305017894", MegrendelesScreenIds.modal.input.ertesitendoTel.inputElementValue())
                assertEquals("felújítás", MegrendelesScreenIds.modal.input.hitelTipus.inputElementValue())
                assertEquals("6 250 000", MegrendelesScreenIds.modal.input.hitelOsszege.inputElementValue())
                assertEquals("323878919", MegrendelesScreenIds.modal.input.ajanlatSzam.inputElementValue())
                assertEquals("4294997", MegrendelesScreenIds.modal.input.szerzodesSzam.inputElementValue())
                assertEquals("8181", MegrendelesScreenIds.modal.input.iranyitoszam.inputElementValue())
                assertEquals("950/2", MegrendelesScreenIds.modal.input.helyrajziSzam.inputElementValue())
                assertEquals("Berhida", MegrendelesScreenIds.modal.input.telepules.inputElementValue())
            }
        }
        on("Importing ET, több ajánlat- és szerződésszám text") {
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.importTextArea) {
                """

Ezúton küldjük Önnek megrendelésünket, 5486 HRSZ számú, természetben 6000 Kecskemét, Márvány utca 48. sz. alatt található ingatlannal kapcsolatban az alábbi feladat(ok) elvégzésére. A teljesítéshez szükséges dokumentumokat kérje az ügyféltől.
Elvégzendő feladatok:
- Energetikai tanúsítvány elkészítése:
• Azonosítója: 143523
• Határideje: 2016.05.18.
Megrendelés adatai:
Hiteligénylő neve: Sallai Tünde
Hiteligénylő telefonszáma: +36706387977
Kapcsolattartó neve: Karai János
Kapcsolattartó telefonszáma: +36306427712
Ajánlatszám: 328357491
Szerződésszám: 4586936

Ajánlatszám: 328357518
Szerződésszám: 4586938
Az elkészült feladato(ka)t - a szükséges mellékletekkel és/vagy kiegészítő adatokkal együtt -, kérjük legkésőbb a fent megjelölt határidőig portálunkra feltölteni, illetve az ügyfélnek átadni.
Üdvözlettel,
Fundamenta-Lakáskassza Zrt."""
            }

            it("Az alapadatok tabon a megfelelő értékek kellenek beírva legyenek") {
                (MegrendelesScreenIds.modal.tab.first + "___tab").simulateClick()

                assertEquals("2016-05-18", MegrendelesScreenIds.modal.input.hatarido.inputElementValue())
                assertEquals(Munkatipusok.Energetika.str, MegrendelesScreenIds.modal.input.munkatipus.inputElementValue())
                assertEquals("Sallai Tünde", MegrendelesScreenIds.modal.input.ugyfelNev.inputElementValue())
                assertEquals("+36706387977", MegrendelesScreenIds.modal.input.ugyfelTel.inputElementValue())
                assertEquals("Karai János", MegrendelesScreenIds.modal.input.ertesitendoNev.inputElementValue())
                assertEquals("+36306427712", MegrendelesScreenIds.modal.input.ertesitendoTel.inputElementValue())
                assertEquals("328357491;328357518", MegrendelesScreenIds.modal.input.ajanlatSzam.inputElementValue())
                assertEquals("4586936;4586938", MegrendelesScreenIds.modal.input.szerzodesSzam.inputElementValue())
                assertEquals("6000", MegrendelesScreenIds.modal.input.iranyitoszam.inputElementValue())
                assertEquals("5486", MegrendelesScreenIds.modal.input.helyrajziSzam.inputElementValue())
                assertEquals("Kecskemét", MegrendelesScreenIds.modal.input.telepules.inputElementValue())
            }
        }
        on("Importing EB, sok ajánlat- és szerződésszám text") {
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.importTextArea) {
                """Tisztelt Partnerünk!

Ezúton küldjük Önnek megrendelésünket, 0100/2 HRSZ számú, természetben 6042 Fülöpháza, II.körzet út 405. sz. alatt található ingatlannal kapcsolatban az alábbi feladat(ok) elvégzésére. A teljesítéshez szükséges dokumentumokat kérje az ügyféltől.
Elvégzendő feladatok:
- Értékbecslés készítése:
• Azonosítója: 143400
• Határideje: 2016.05.17.
Megrendelés adatai:
Hiteligénylő neve: Schmidtné Szantner Barabara
Hiteligénylő telefonszáma: +36704403189
Kapcsolattartó neve: Grill József
Kapcsolattartó telefonszáma: +36309987347
Lakáscél: vásárlás
Felvenni kívánt hitel összege: 14000000
Ajánlatszám: 325610706
Szerződésszám: 4420498

Ajánlatszám: 327849317
Szerződésszám: 4523750

Ajánlatszám: 327373753
Szerződésszám: 4482982

Ajánlatszám: 327373746
Szerződésszám: 4483021
Az elkészült feladato(ka)t - a szükséges mellékletekkel és/vagy kiegészítő adatokkal együtt -, kérjük legkésőbb a fent megjelölt határidőig portálunkra feltölteni, illetve az ügyfélnek átadni.
Üdvözlettel,
Fundamenta-Lakáskassza Zrt."""
            }

            it("Az alapadatok tabon a megfelelő értékek kellenek beírva legyenek") {
                (MegrendelesScreenIds.modal.tab.first + "___tab").simulateClick()

                assertEquals("2016-05-17", MegrendelesScreenIds.modal.input.hatarido.inputElementValue())
                assertEquals(Munkatipusok.Ertekbecsles.str, MegrendelesScreenIds.modal.input.munkatipus.inputElementValue())
                assertEquals("Schmidtné Szantner Barabara", MegrendelesScreenIds.modal.input.ugyfelNev.inputElementValue())
                assertEquals("+36704403189", MegrendelesScreenIds.modal.input.ugyfelTel.inputElementValue())
                assertEquals("Grill József", MegrendelesScreenIds.modal.input.ertesitendoNev.inputElementValue())
                assertEquals("+36309987347", MegrendelesScreenIds.modal.input.ertesitendoTel.inputElementValue())
                assertEquals("vásárlás", MegrendelesScreenIds.modal.input.hitelTipus.inputElementValue())
                assertEquals("14 000 000", MegrendelesScreenIds.modal.input.hitelOsszege.inputElementValue())
                assertEquals("325610706;327849317;327373753;327373746", MegrendelesScreenIds.modal.input.ajanlatSzam.inputElementValue())
                assertEquals("4420498;4523750;4482982;4483021", MegrendelesScreenIds.modal.input.szerzodesSzam.inputElementValue())
                assertEquals("6042", MegrendelesScreenIds.modal.input.iranyitoszam.inputElementValue())
                assertEquals("0100/2", MegrendelesScreenIds.modal.input.helyrajziSzam.inputElementValue())
                assertEquals("Fülöpháza", MegrendelesScreenIds.modal.input.telepules.inputElementValue())
            }
        }
        on("Importing EB, ET, sok ajánlat- és szerződésszám text") {
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.importTextArea) {
                """Tisztelt Partnerünk!

Ezúton küldjük Önnek megrendelésünket, 884 HRSZ számú, természetben 7827 Beremend, Sport u. utca 5/d. sz. alatt található ingatlannal kapcsolatban az alábbi feladat(ok) elvégzésére. A teljesítéshez szükséges dokumentumokat kérje az ügyféltől.
Elvégzendő feladatok:
- Értékbecslés készítése:
• Azonosítója: 142867
• Határideje: 2016.05.09.
- Energetikai tanúsítvány elkészítése:
• Azonosítója: 142866
• Határideje: 2016.05.10.
Megrendelés adatai:
Hiteligénylő neve: bodnár istván
Hiteligénylő telefonszáma: +36308657604
Kapcsolattartó neve: bodrogi istván
Kapcsolattartó telefonszáma: +36308657604
Lakáscél: vásárlás, felújítás
Felvenni kívánt hitel összege: 8000000
Ajánlatszám: 324263930
Szerződésszám: 4439733
Az elkészült feladato(ka)t - a szükséges mellékletekkel és/vagy kiegészítő adatokkal együtt -, kérjük legkésőbb a fent megjelölt határidőig portálunkra feltölteni, illetve az ügyfélnek átadni.
Üdvözlettel,
Fundamenta-Lakáskassza Zrt."""
            }

            it("Az alapadatok tabon a megfelelő értékek kellenek beírva legyenek") {
                (MegrendelesScreenIds.modal.tab.first + "___tab").simulateClick()

                assertEquals(Munkatipusok.EnergetikaAndErtekBecsles.str, MegrendelesScreenIds.modal.input.munkatipus.inputElementValue())
                assertEquals("2016-05-09", MegrendelesScreenIds.modal.input.hatarido.inputElementValue())
                assertEquals("bodnár istván", MegrendelesScreenIds.modal.input.ugyfelNev.inputElementValue())
                assertEquals("+36308657604", MegrendelesScreenIds.modal.input.ugyfelTel.inputElementValue())
                assertEquals("bodrogi istván", MegrendelesScreenIds.modal.input.ertesitendoNev.inputElementValue())
                assertEquals("+36308657604", MegrendelesScreenIds.modal.input.ertesitendoTel.inputElementValue())
                assertEquals("vásárlás", MegrendelesScreenIds.modal.input.hitelTipus.inputElementValue())
                assertEquals("8 000 000", MegrendelesScreenIds.modal.input.hitelOsszege.inputElementValue())
                assertEquals("324263930", MegrendelesScreenIds.modal.input.ajanlatSzam.inputElementValue())
                assertEquals("4439733", MegrendelesScreenIds.modal.input.szerzodesSzam.inputElementValue())
                assertEquals("7827", MegrendelesScreenIds.modal.input.iranyitoszam.inputElementValue())
                assertEquals("884", MegrendelesScreenIds.modal.input.helyrajziSzam.inputElementValue())
                assertEquals("Beremend", MegrendelesScreenIds.modal.input.telepules.inputElementValue())
            }
        }
    }
    given("test editing Existing Megrendeles") {
        globalDispatcher(Action.ChangeURL(Path.megrendeles.edit(newMegrendelesId)))

        on("Click on Save button after fields were modified") {
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.megrendelo) { "Állami" }
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.regio) { "Bács-Kiskun" }
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.munkatipus) { Munkatipusok.EnergetikaAndErtekBecsles.str }
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.fovallalkozo) { "Viridis Kft." }
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.alvallalkozo) { "24" } // Test Kft.
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.hitelTipus) { "építés" }
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.helyrajziSzam) { "4321" }
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.iranyitoszam) { "7841" }
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.telepules) { "Adorjás" }
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.kozteruletNeve) { "q" }
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.hitelOsszege) { "12000001" }

            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.ugyfelNev) { "Ügyfél neve2" }
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.ugyfelTel) { "Ügyfél száma2" }
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.ebAzonosito) { azonosito + "ertb" }
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.etAzonosito) { azonosito + "ener" }

            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.kerulet) { "kerulet" }
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.kozteruletJellege) { "kozteruletJellege" }
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.hazszam) { "hazszam" }
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.lepcsohaz) { "lepcsohaz" }
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.emelet) { "emelet" }
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.ajto) { "ajto" }

            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.ajanlatSzam) { "ajanlatSzam" }
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.szerzodesSzam) { "szerzodesSzam" }

            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.megrendelesDatuma) { moment().add(1, TimeUnit.Days).format(dateFormat) }
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.hatarido) { moment().add(2, TimeUnit.Days).format(dateFormat) }

            it("The server should send back the modified entity") {
                val prevMinute = moment().subtract(1, TimeUnit.Minutes)
                val nextMinute = moment().add(1, TimeUnit.Minutes)

                globalEventListeners.add { appState ->
                    val megr: Megrendeles = appState.megrendelesState.megrendelesek[newMegrendelesId]!!

                    assertEquals("Állami", megr.megrendelo)
                    assertEquals("Bács-Kiskun", megr.regio)
                    assertEquals(Munkatipusok.EnergetikaAndErtekBecsles.str, megr.munkatipus)
                    assertEquals("Viridis Kft.", megr.foVallalkozo)
                    assertEquals(24, megr.alvallalkozoId)
                    assertEquals(67, megr.ertekbecsloId)
                    // Állami-n belül csak ez az egy darab ingatlanTipus van, ezért automatikusan kiválasztódik
                    assertEquals("Panel lakás", megr.ingatlanTipusMunkadijMeghatarozasahoz)
                    assertEquals("építés", megr.hitelTipus)

                    assertEquals("4321", megr.hrsz)
                    assertEquals("7841", megr.irsz)
                    assertEquals("Adorjás", megr.telepules)

                    assertEquals(12000001, megr.hitelOsszeg)

                    assertEquals("Ügyfél neve2", megr.ugyfelNeve)
                    assertEquals("Ügyfél száma2", megr.ugyfelTel)
                    assertEquals("EB${azonosito}ertb_ET${azonosito}ener", megr.azonosito)

                    assertEquals("Ügyfél neve2", megr.ertesitesiNev)
                    assertEquals("Ügyfél száma2", megr.ertesitesiTel)
                    assertEquals("", megr.ugyfelEmail)
                    assertEquals("Panel lakás", megr.ingatlanTipusMunkadijMeghatarozasahoz)
                    assertEquals(10000, megr.ertekbecsloDija)
                    assertEquals(20000, megr.szamlazhatoDij)
                    assertEquals(null, megr.helyszinelo)
                    assertEquals("lakás", megr.ingatlanBovebbTipus)
                    assertEquals(null, megr.keszultsegiFok)
                    assertEquals(null, megr.lakasTerulet)
                    assertEquals(null, megr.telekTerulet)
                    assertEquals(null, megr.becsultErtek)
                    assertEquals(null, megr.eladasiAr)
                    assertEquals(null, megr.fajlagosBecsultAr)
                    assertEquals(null, megr.fajlagosEladAr)
                    assertEquals(false, megr.ellenorizve)
                    assertEquals(Statusz.B1, megr.statusz)

                    assertEquals("", megr.ugyfelEmail)
                    assertEquals("kerulet", megr.kerulet)
                    assertEquals("q", megr.utca)
                    assertEquals("kozteruletJellege", megr.utcaJelleg)
                    assertEquals("hazszam", megr.hazszam)
                    assertEquals("lepcsohaz", megr.lepcsohaz)
                    assertEquals("emelet", megr.emelet)
                    assertEquals("ajto", megr.ajto)
                    assertEquals("ajanlatSzam", megr.ajanlatSzam)
                    assertEquals("szerzodesSzam", megr.szerzodesSzam)
                    assertEquals(null, megr.hetKod)
                    assertEquals("", megr.szamlaSorszama)

                    assertTrue(moment().add(1, TimeUnit.Days).isSame(megr.megrendelve!!, Granularity.Day))
                    assertTrue(moment().add(2, TimeUnit.Days).isSame(megr.hatarido!!, Granularity.Day))

                    assertEquals(null, megr.megrendelesMegtekint)
                    assertEquals(null, megr.energetikaFeltoltve)
                    assertEquals(null, megr.ertekbecslesFeltoltve)
                    assertEquals(null, megr.szemleIdopontja)
                    assertEquals(null, megr.feltoltveMegrendelonek)
                    assertEquals(null, megr.zarolva)
                    assertEquals(null, megr.keszpenzesBefizetes)
                    assertEquals(null, megr.penzBeerkezettDatum)
                    assertEquals(null, megr.adasvetelDatuma)

                    globalEventListeners.removeAt(globalEventListeners.lastIndex)
                    testDone()
                }
                haltTestingUntilExternalEvent()
                MegrendelesScreenIds.modal.button.save.simulateClick()
            }
        }
        on("Értékbecslés munkatipus") {
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.munkatipus) { Munkatipusok.Ertekbecsles.str }
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.ebAzonosito) { azonosito }

            it("The server should send back the modified entity") {
                globalEventListeners.add { appState ->
                    val megr = appState.megrendelesState.megrendelesek[newMegrendelesId]!!

                    assertEquals(Munkatipusok.Ertekbecsles.str, megr.munkatipus)
                    assertEquals(azonosito, megr.azonosito)

                    globalEventListeners.removeAt(globalEventListeners.lastIndex)
                    testDone()
                }
                haltTestingUntilExternalEvent()
                MegrendelesScreenIds.modal.button.save.simulateClick()
            }
        }
        on("Energetika munkatipus") {
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.munkatipus) { Munkatipusok.Energetika.str }
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.etAzonosito) { azonosito }

            it("The server should send back the modified entity") {
                globalEventListeners.add { appState ->
                    val megr = appState.megrendelesState.megrendelesek[newMegrendelesId]!!

                    assertEquals(Munkatipusok.Energetika.str, megr.munkatipus)
                    assertEquals(azonosito, megr.azonosito)

                    globalEventListeners.removeAt(globalEventListeners.lastIndex)
                    testDone()
                }

                haltTestingUntilExternalEvent()
                MegrendelesScreenIds.modal.button.save.simulateClick()
            }
        }

        /* TODO: Ez azt teszteli, hogy ha a munkatipus nem ET, BT vagy ET&BT, akkor a rendszernek a sima
        azonositoja töltődik-e
        Ehhez viszont kell egy Régióösszerendelés egy Somogy-gyal egy 4. munkatipushoz.
        on("Egyéb munkatipus") {
            val randomStringGenerator = RandomStringGenerator()
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.munkatipus) { "Valami munkatípus" }
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.azonosito) {
                randomStringGenerator.next()
            }


            it("The server should send back the modified entity") {
                MegrendelesStore.addChangeListener(this) {
                    val megr = MegrendelesStore.megrendelesek[newMegrendelesId]!!

                    assertEquals("Valami munkatípus", megr.munkatipus)
                    randomStringGenerator.reset()
                    assertEquals(randomStringGenerator.next(), megr.azonosito)
                    MegrendelesStore.removeListener(this)
                    testDone()
                }
                haltTestingUntilExternalEvent()
                MegrendelesScreenIds.modal.button.save.simulateClick()
            }
        }*/
        on("Az irányítószám kiválasztása ki kell válassza a települést, de csak azt, a Régiót nem!") {
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.iranyitoszam) { "7636" }
            it("regio marad 'Bács-Kiskun', település: Pécs") {
                assertEquals("Bács-Kiskun", jq("#${MegrendelesScreenIds.modal.input.regio}").`val`())
                assertEquals("Pécs", jq("#${MegrendelesScreenIds.modal.input.telepules}").`val`())
            }
        }
        on("A régió kiválasztása NEM állítja be az alvállalkozó és értékbecslő selectek választható értékeit, ha azok listájában több mint egy elem szerepel") {
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.alvallalkozo) { "0" }
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.ertekbecslo) { "0" }

            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.regio) { "Baranya" }

            it("save") {
                assertEquals("Baranya", jq("#${MegrendelesScreenIds.modal.input.regio}").`val`())
                assertEquals("", jq("#${MegrendelesScreenIds.modal.input.alvallalkozo}").`val`())
                assertEquals("", jq("#${MegrendelesScreenIds.modal.input.ertekbecslo}").`val`())
            }
        }
        on("Alvállalkozó és értékbecslő mentése") {
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.regio) { "Baranya" }
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.munkatipus) { Munkatipusok.Ertekbecsles.str }
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.alvallalkozo) { "24" } // Test Kft.
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.ertekbecslo) { "67" } // BB
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.ebAzonosito) { azonosito }

            it("save") {
                globalEventListeners.add { appState ->
                    val megr = appState.megrendelesState.megrendelesek[newMegrendelesId]!!

                    assertEquals("Baranya", megr.regio)
                    assertEquals(24, megr.alvallalkozoId)
                    assertEquals(67, megr.ertekbecsloId)

                    globalEventListeners.removeAt(globalEventListeners.lastIndex)
                    testDone()
                }
                haltTestingUntilExternalEvent()
                MegrendelesScreenIds.modal.button.save.simulateClick()
            }
        }
        on("ingatlanTipusMunkadijMeghatarozasahoz mentés") {
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.megrendelo) { "Fundamenta Zrt." }
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.munkatipus) { Munkatipusok.Ertekbecsles.str }
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.ingatlanTipusMunkadijMeghatarozasahoz) { "5" } // vegyes funkciójú ingatlan

            it("elmenti") {
                globalEventListeners.add { appState ->
                    val megr = appState.megrendelesState.megrendelesek[newMegrendelesId]!!

                    assertEquals("Vegyes funkciójú ingatlan", megr.ingatlanTipusMunkadijMeghatarozasahoz)
                    assertEquals(null, megr.ertekbecsloDija) // nem tartozik a fentihez dij
                    assertEquals(30000, megr.szamlazhatoDij)

                    globalEventListeners.removeAt(globalEventListeners.lastIndex)
                    testDone()
                }
                haltTestingUntilExternalEvent()
                MegrendelesScreenIds.modal.button.save.simulateClick()
            }
        }
        on("Click on Save button after Comment were added") {
            (MegrendelesScreenIds.modal.tab.akadalyok + "___tab").simulateClick()
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.megjegyzes) { "Ez egy comment" }
            it("The server should send back the modified entity") {
                val prevMinute = moment().subtract(1, TimeUnit.Minutes)
                val nextMinute = moment().add(1, TimeUnit.Minutes)
                globalEventListeners.add { appState ->
                    val megr = appState.megrendelesState.megrendelesek[newMegrendelesId]!!

                    assertTrue(megr.modified.isBetween(prevMinute, nextMinute))
                    assertEquals("Ez egy comment", megr.megjegyzes)

                    globalEventListeners.removeAt(globalEventListeners.lastIndex)
                    testDone()
                }
                haltTestingUntilExternalEvent()
                MegrendelesScreenIds.modal.button.save.simulateClick()
            }
        }
        on("Adding new Akadaly") {
            (MegrendelesScreenIds.modal.tab.akadalyok + "___tab").simulateClick()
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.akadaly.keslekedesOka) { Statusz.G3.name }
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.akadaly.szovegesMagyarazat) { "Szöveges magyarázat Akadályhoz" }
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.akadaly.ujHatarido) { "2017-07-07" }
            it("The server should send back the modified entity") {
                val tabId = MegrendelesScreenIds.modal.tab.akadalyok
                val prevMinute = moment().subtract(1, TimeUnit.Minutes)
                val nextMinute = moment().add(1, TimeUnit.Minutes)

                globalEventListeners.add { appState ->
                    val megr = appState.megrendelesState.megrendelesek[newMegrendelesId]!!

                    assertTrue(megr.modified.isBetween(prevMinute, nextMinute))
                    assertEquals("Ez egy comment", megr.megjegyzes)

                    assertTrue(megr.modified.isBetween(prevMinute, nextMinute) || megr.modified.isSame(prevMinute, Granularity.Minute))
                    assertEquals(1, megr.akadalyok.size)
                    assertEquals("2017-07-07", megr.akadalyok[0].ujHatarido!!.format(dateFormat))
                    assertEquals("Szöveges magyarázat Akadályhoz", megr.akadalyok[0].leiras)
                    assertTrue(megr.akadalyok[0].rogzitve.isBetween(prevMinute, nextMinute) || megr.akadalyok[0].rogzitve.isSame(prevMinute, Granularity.Minute))
                    assertEquals(Statusz.G3, megr.akadalyok[0].statusz)

                    later(1000) {
                        assertEquals(megr.akadalyok[0].rogzitve.format(dateTimeFormat), tableCellValue(0, 0))
                        assertEquals("2017-07-07", tableCellValue(0, 1))
                        assertEquals("(G3) Dokumentumhiány", tableCellValue(0, 2))
                        assertEquals("Szöveges magyarázat Akadályhoz", tableCellValue(0, 3))

                        globalEventListeners.removeAt(globalEventListeners.lastIndex)
                        testDone()
                    }
                }
                haltTestingUntilExternalEvent()
                MegrendelesScreenIds.modal.button.akadalyFeltoltes.simulateClick()
            }
        }
        on("Click on Save button after fields from the 2nd tab were modified") {
            (MegrendelesScreenIds.modal.tab.ingatlanAdatai + "___tab").simulateClick()
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.helyszinelo) { "helyszinelo" }
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.ingatlanBovebbTipus) { "lakóház, udvar, gazdasági épület" }
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.ingatlanKeszultsegiFoka) { "69" }
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.lakasTerulet) { "100" }
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.telekTerulet) { "200" }
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.becsultErtek) { "10000000" }
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.eladasiAr) { "20000000" }
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.hetKod) { "het" }
            simulateChangeAndSetValue(MegrendelesScreenIds.modal.input.adasvetelDatuma) { "2016-04-19" }

            it("The server should send back the modified entity") {
                val prevMinute = moment().subtract(1, TimeUnit.Minutes)
                val nextMinute = moment().add(1, TimeUnit.Minutes)
                globalEventListeners.add { appState ->
                    val megr = appState.megrendelesState.megrendelesek[newMegrendelesId]!!
                    assertTrue(megr.modified.isBetween(prevMinute, nextMinute))
                    assertEquals("Fundamenta Zrt.", megr.megrendelo)
                    assertEquals("Baranya", megr.regio)
                    assertEquals(Munkatipusok.Ertekbecsles.str, megr.munkatipus)
                    assertEquals(azonosito, megr.azonosito)
                    assertEquals("Viridis Kft.", megr.foVallalkozo)
                    assertEquals(24, megr.alvallalkozoId)
                    assertEquals(67, megr.ertekbecsloId)
                    assertEquals("építés", megr.hitelTipus)

                    assertEquals("4321", megr.hrsz)
                    assertEquals("7636", megr.irsz)
                    assertEquals("Pécs", megr.telepules)

                    assertEquals(12000001, megr.hitelOsszeg)

                    assertEquals("Ügyfél neve2", megr.ugyfelNeve)
                    assertEquals("Ügyfél száma2", megr.ugyfelTel)

                    assertEquals("Ügyfél neve2", megr.ertesitesiNev)
                    assertEquals("Ügyfél száma2", megr.ertesitesiTel)
                    assertEquals("", megr.ugyfelEmail)
                    assertEquals("Vegyes funkciójú ingatlan", megr.ingatlanTipusMunkadijMeghatarozasahoz)
                    assertEquals(null, megr.ertekbecsloDija)
                    assertEquals(30000, megr.szamlazhatoDij)
                    assertEquals("helyszinelo", megr.helyszinelo)
                    assertEquals("lakóház, udvar, gazdasági épület", megr.ingatlanBovebbTipus)
                    assertEquals(69, megr.keszultsegiFok)
                    assertEquals(100, megr.lakasTerulet)
                    assertEquals(200, megr.telekTerulet)
                    assertEquals(10000000, megr.becsultErtek)
                    assertEquals(20000000, megr.eladasiAr)
                    assertEquals(100000, megr.fajlagosBecsultAr)
                    assertEquals(200000, megr.fajlagosEladAr)
                    assertEquals(false, megr.ellenorizve)
                    assertEquals(Statusz.B1, megr.statusz)

                    assertEquals("", megr.ugyfelEmail)
                    assertEquals("kerulet", megr.kerulet)
                    assertEquals("q", megr.utca)
                    assertEquals("kozteruletJellege", megr.utcaJelleg)
                    assertEquals("hazszam", megr.hazszam)
                    assertEquals("lepcsohaz", megr.lepcsohaz)
                    assertEquals("emelet", megr.emelet)
                    assertEquals("ajto", megr.ajto)
                    assertEquals("ajanlatSzam", megr.ajanlatSzam)
                    assertEquals("szerzodesSzam", megr.szerzodesSzam)
                    assertEquals("het", megr.hetKod)
                    assertEquals("", megr.szamlaSorszama)

                    assertTrue(moment().add(1, TimeUnit.Days).isSame(megr.megrendelve!!, Granularity.Day))
                    assertTrue(moment().add(2, TimeUnit.Days).isSame(megr.hatarido!!, Granularity.Day))

                    assertEquals(null, megr.megrendelesMegtekint)
                    assertEquals(null, megr.energetikaFeltoltve)
                    assertEquals(null, megr.ertekbecslesFeltoltve)
                    assertEquals(null, megr.szemleIdopontja)
                    assertEquals(null, megr.feltoltveMegrendelonek)
                    assertEquals(null, megr.zarolva)
                    assertEquals(null, megr.keszpenzesBefizetes)
                    assertEquals(null, megr.penzBeerkezettDatum)
                    assertEquals("2016-04-19", megr.adasvetelDatuma!!.format(dateFormat))
                    globalEventListeners.removeAt(globalEventListeners.lastIndex)
                    testDone()
                }
                haltTestingUntilExternalEvent()
                MegrendelesScreenIds.modal.button.save.simulateClick()
            }
        }
    }
}

private fun TestBuilder.assertHasAttribute(id: String, attrName: String) {
    assertTrue(document.getElementById(id)!!.hasAttribute(attrName))
}