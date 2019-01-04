package hu.nevermind.utils.store

import app.common.Moment
import app.common.RegExpBuilder
import app.common.moment
import store.MegrendelesColumnData
import hu.nevermind.iktato.Communicator
import hu.nevermind.iktato.JqueryAjaxPoster
import store.columnDefinitions

const val dateFormat = "YYYY-MM-DD"
const val monthFormat = "YYYY-MM"
const val dateTimeFormat = "YYYY-MM-DD HH:mm"
const val dateTimeSecondFormat = "YYYY-MM-DD HH:mm:ss"
const val fractionalSecondFormat = "YYYY-MM-DD HH:mm:ss.SSS"

val communicator: Communicator = Communicator(JqueryAjaxPoster())

enum class Role {
    ROLE_ADMIN, ROLE_USER, ROLE_ELLENORZO
}

data class Account(
        var id: Int,
        var fullName: String = "",
        var username: String = "",
        var disabled: Boolean = false,
        var role: Role,
        var alvallalkozoId: Int,
        var plainPassword: String) {

}

data class Alvallalkozo(
        var id: Int = 0,
        var name: String = "",
        var phone: String = "",
        var szamlaSzam: String = "",
        var kapcsolatTarto: String = "",
        var email: String = "",
        var adoszam: String = "",
        var tagsagiSzam: String = "",
        var cim: String = "",
        var keszpenzes: Boolean = false,
        var disabled: Boolean = false
) {
    override fun hashCode(): Int {
        return id
    }

    override fun equals(other: Any?): Boolean {
        if (other is Alvallalkozo) {
            return other.id == this.id
        }
        return false
    }

    override fun toString(): String {
        return "$id: $name"
    }
}

data class Ertekbecslo(
        var id: Int = 0,
        var name: String = "",
        var alvallalkozoId: Int,
        var phone: String = "",
        var email: String = "",
        var comment: String = "",
        var disabled: Boolean = false
) {
    override fun hashCode(): Int {
        return id
    }

    override fun equals(other: Any?): Boolean {
        if (other is Ertekbecslo) {
            return other.id == this.id
        }
        return false
    }

    override fun toString(): String {
        return "$id: $name"
    }
}

data class RegioOsszerendeles(
        var id: Int = 0,
        var alvallalkozoId: Int,
        var megye: String,
        var leiras: String = "",
        var munkatipus: String = "",
        var nettoAr: Int = 0,
        var afa: Int = 0,
        var jutalek: Int = 0
) {
    override fun hashCode(): Int {
        return id
    }

    override fun equals(other: Any?): Boolean {
        if (other is RegioOsszerendeles) {
            return other.id == this.id
        }
        return false
    }

    override fun toString(): String {
        return "$id: ($alvallalkozoId) -> ($megye, $munkatipus)"
    }
}

data class Irszam(val id: Int,
                  val irszam: String,
                  val telepules: String,
                  val megye: String)

data class Varos(val id: Int,
                 val telepules: String,
                 val irszam: String,
                 val utcanev: String,
                 val kerulet: String,
                 val utotag: String)



data class LoggedInUser(val username: String,
                        val fullName: String,
                        val alvallalkozoId: Int,
                        val role: Role,
                        val megrendelesTableColumns: Array<MegrendelesColumnData>) {
    val isAdmin: Boolean
        get() = role == Role.ROLE_ADMIN
    val isAlvallalkozo: Boolean
        get() = role == Role.ROLE_USER
}

fun stringToColumnDefArray(megrendelesTableColumns: String?): Array<MegrendelesColumnData> {
    return megrendelesTableColumns?.split(",")?.map { colName ->
        columnDefinitions.firstOrNull { it.fieldName == colName }.apply {
            if (this == null) {
                console.error("UNKNOWN COLUMN NAME: $colName")
            }
        }
    }?.filterNotNull()?.toTypedArray() ?: emptyArray()
}

val LoggedInUser?.isLoggedIn: Boolean
    get() = this != null



enum class Statusz(val text: String) {
    B1("(B1) (Új megrendelés)"),
    C1("(C1) Feltöltve"),
    D1("(D1) Zárolva"),
    G1("(G1) A kapcsolattartó nem érhető el"),
    G2("(G2) (Anyagi probléma)"),
    G3("(G3) Dokumentumhiány"),
    G4("(G4) Az ügyfél nem kéri az értékbecslést"),
    G5("(G5) Ügyfél nem ért rá"),
    G6("(G6) Az érétkbecslés nem készíthető el");

    companion object {
        fun fromString(str: String): Statusz {
            return if (str.isEmpty()) {
                B1
            } else {
                val status = values().firstOrNull { it.name == str.substring(1, 3) }
                require(status != null) { "There is no Statusz for '$str'" }
                status
            }
        }
    }
}

val akadalyosStatuszok = arrayOf(Statusz.G1, Statusz.G2, Statusz.G3, Statusz.G4, Statusz.G5, Statusz.G6)

enum class Munkatipusok(val str: String) {
    Ertekbecsles("Értékbecslés"),
    Energetika("Energetika"),
    EnergetikaAndErtekBecsles("Energetika&Értékbecslés")
}

// Excelben: Ingatlan megnevezése
val ingatlanBovebbTipusaArray = arrayOf(
        "lakás",
        "lakóház, udvar",
        "lakóház, gazdasági épület",
        "lakóház, udvar, gazdasági épület",
        "lakóház",
        "tanya",
        "beépítetlen terület",
        "üdülő",
        "garázs",
        "egyéb ingatlan"
)

fun String.isErtekbecsles(): Boolean {
    return contains(Munkatipusok.Ertekbecsles.str)
}

fun String.isEnergetika(): Boolean {
    return contains(Munkatipusok.Energetika.str)
}

data class FileData(val name: String, val humanReadableSize: String)

data class Akadaly(val id: Int,
                   val statusz: Statusz,
                   val leiras: String,
                   val ujHatarido: Moment?,
                   val rogzitve: Moment,
                   val megrendelesId: Int)

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


data class Megrendeles(
        var id: Int = 0,
        var azonosito: String = "",

        var ugyfelNeve: String = "",
        var ugyfelTel: String = "",
        var ugyfelEmail: String = "",

        var hrsz: String = "",
        var regio: String = "",
        var irsz: String = "",
        var telepules: String = "",
        var kerulet: String = "",
        var utca: String = "",
        var utcaJelleg: String = "",
        var hazszam: String = "",
        var lepcsohaz: String = "",
        var emelet: String = "",
        var ajto: String = "",
        var ingatlanBovebbTipus: String = "",
        var ingatlanTipusMunkadijMeghatarozasahoz: String? = null, // ~ sajat AR
        var lakasTerulet: Int? = null,
        var telekTerulet: Int? = null,
        var keszultsegiFok: Int? = null,


        var eladasiAr: Int? = null,
        var becsultErtek: Int? = null,
        var fajlagosEladAr: Int? = null,
        var fajlagosBecsultAr: Int? = null,

        var statusz: Statusz = Statusz.B1,
        // Excelből jön
        var helyszinelo: String? = null,

        var ellenorizve: Boolean = false,

        var megrendelve: Moment = moment(), // Megrendelés dátuma
        var hatarido: Moment = moment(),
        var feltoltveMegrendelonek: Moment? = null, // Feltöltve megrendelőnek
        var energetikaFeltoltve: Moment? = null, // ???
        var rogzitve: Moment? = null,
        var szemleIdopontja: Moment? = null,
        var adasvetelDatuma: Moment? = null,
        var zarolva: Moment? = null,
        var keszpenzesBefizetes: Moment? = null, // Iktató: Számla kifizetése, nálunk: Készpénzes befizetés
        var penzBeerkezettDatum: Moment? = null,
        var megrendelesMegtekint: Moment? = null, // Megrendelés átvétel ideje
        var ertekbecslesFeltoltve: Moment? = null, // Vállalkozó feltöltöte a kész anyago

        var megrendelo: String = "",
        var hitelTipus: String = "",
        var foVallalkozo: String = "",

        var szamlaSorszama: String = "",
        var ertekbecsloDija: Int? = null,

        var megjegyzes: String = "",
        var problema: String = "",

        var alvallalkozoId: Int = 0,
        var ertekbecsloId: Int = 0,

        var ertesitesiNev: String = "",
        var ertesitesiTel: String = "",
        var hitelOsszeg: Int? = null,
        var szamlazhatoDij: Int? = null,
        var ajanlatSzam: String = "",
        var szerzodesSzam: String = "",
        var hetKod: String? = null,
        var munkatipus: String = "",
        var afa: Int = 0,

        var created: Moment = moment(),
        var createdBy: String = "",
        var modified: Moment = moment(),
        var modifiedBy: String = "",

        var files: Array<FileData> = emptyArray(),
        var akadalyok: Array<Akadaly> = emptyArray(),
        var readByAdmin: Boolean = false,
        var readByAlvallalkozo: Boolean = false

) {

    fun isUnread(role: Role): Boolean {
        return if (role == Role.ROLE_ADMIN) {
            !this.readByAdmin
        } else {
            !this.readByAlvallalkozo
        }
    }

    fun getErtekbecslesId(): String {
        val match = RegExpBuilder.find("EB").min(2).digits().asGroup().getRegExp().exec(azonosito)
        if (match != null) {
            return match[1]!!
        }
        return azonosito
    }

    fun getEnergetikaId(): String {
        val match = RegExpBuilder.find("ET").min(2).digits().asGroup().getRegExp().exec(azonosito)
        if (match != null) {
            return match[1]!!
        }
        return azonosito
    }

    val atNemVett: Boolean
        get() = megrendelesMegtekint == null
    val atVett: Boolean
        get() = !atNemVett

    val alvallalkozoElkeszult: Boolean
        get() {
            if (munkatipus.isErtekbecsles() && ertekbecslesFeltoltve == null) {
                return false
            }
            if (munkatipus.isEnergetika() && energetikaFeltoltve == null) {
                return false
            }
            return true
        }

    override fun hashCode(): Int {
        return id
    }

    override fun equals(other: Any?): Boolean {
        if (other is Megrendeles) {
            return other.id == this.id
        }
        return false
    }
}

/**
 * object BsStyle {
val Primary: BsStyle = js("'primary'")
val Default: BsStyle = js("'default'")
val Success: BsStyle = js("'success'")
val Info: BsStyle = js("'info'")
val Warning: BsStyle = js("'warning'")
val Danger: BsStyle = js("'danger'")
val Link: BsStyle = js("'link'")
}

 */
data class Notification(val style: String, val text: String)

data class SajatAr(
        var id: Int = 0,
        var leiras: String = "",
        var nettoAr: Int = 0,
        var megrendelo: String,
        var afa: Int = 0,
        var munkatipus: String,

        var created: Moment = moment(),
        var createdBy: String = "",
        var modified: Moment = moment(),
        var modifiedBy: String = ""
) {

}
