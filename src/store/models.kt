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
        val id: Int,
        val fullName: String = "",
        val username: String = "",
        val disabled: Boolean = false,
        val role: Role,
        val alvallalkozoId: Int,
        val plainPassword: String) {

}

data class Alvallalkozo(
        val id: Int = 0,
        val name: String = "",
        val phone: String = "",
        val szamlaSzam: String = "",
        val kapcsolatTarto: String = "",
        val email: String = "",
        val adoszam: String = "",
        val tagsagiSzam: String = "",
        val cim: String = "",
        val keszpenzes: Boolean = false,
        val disabled: Boolean = false
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
        val id: Int = 0,
        val name: String = "",
        val alvallalkozoId: Int,
        val phone: String = "",
        val email: String = "",
        val comment: String = "",
        val disabled: Boolean = false
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
        val id: Int = 0,
        val alvallalkozoId: Int,
        val megye: String,
        val leiras: String = "",
        val munkatipus: String = "",
        val nettoAr: Int = 0,
        val afa: Int = 0,
        val jutalek: Int = 0
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
    val helyszinelo: String? = null
    val ingatlanTipusa: String? = null
    val ertekBecslo: String? = null
    val telekMeret: Double? = null
    val keszultsegiFok: Double? = null
    val ingatlanTerulet: Double? = null
    val forgalmiErtek: Double? = null

    val adasvetel: String? = null
    val adasvetelDatuma: String? = null

    val szemleIdopontja: String? = null
    val fajlagosAr: Double? = null
    val kolcsonIgenylo: String? = null
    val hrsz: String? = null

    // import text
    val ebAzonosito: String? = null
    val etAzonosito: String? = null
    val hatarido: Moment? = null
    val ugyfelNeve: String? = null
    val ugyfelTelefonszama: String? = null
    val ertesitesiNev: String? = null
    val ertesitesiTel: String? = null
    val lakasCel: String? = null
    val hitelOsszeg: Int? = null
    val ajanlatSzam: String? = null
    val szerzodesSzam: String? = null
    val energetika = false
    val ertekbecsles = false
    val irsz: String? = null
    val telepules: String? = null
    val regio: String? = null
}


data class Megrendeles(
        val id: Int = 0,
        val azonosito: String = "",

        val ugyfelNeve: String = "",
        val ugyfelTel: String = "",
        val ugyfelEmail: String = "",

        val hrsz: String = "",
        val regio: String = "",
        val irsz: String = "",
        val telepules: String = "",
        val kerulet: String = "",
        val utca: String = "",
        val utcaJelleg: String = "",
        val hazszam: String = "",
        val lepcsohaz: String = "",
        val emelet: String = "",
        val ajto: String = "",
        val ingatlanBovebbTipus: String = "",
        val ingatlanTipusMunkadijMeghatarozasahoz: String? = null, // ~ sajat AR
        val lakasTerulet: Int? = null,
        val telekTerulet: Int? = null,
        val keszultsegiFok: Int? = null,


        val eladasiAr: Int? = null,
        val becsultErtek: Int? = null,
        val fajlagosEladAr: Int? = null,
        val fajlagosBecsultAr: Int? = null,

        val statusz: Statusz = Statusz.B1,
        // Excelből jön
        val helyszinelo: String? = null,

        val ellenorizve: Boolean = false,

        val megrendelve: Moment? = moment(), // Megrendelés dátuma
        val hatarido: Moment? = moment(),
        val feltoltveMegrendelonek: Moment? = null, // Feltöltve megrendelőnek
        val energetikaFeltoltve: Moment? = null, // ???
        val rogzitve: Moment? = null,
        val szemleIdopontja: Moment? = null,
        val adasvetelDatuma: Moment? = null,
        val zarolva: Moment? = null,
        val keszpenzesBefizetes: Moment? = null, // Iktató: Számla kifizetése, nálunk: Készpénzes befizetés
        val penzBeerkezettDatum: Moment? = null,
        val megrendelesMegtekint: Moment? = null, // Megrendelés átvétel ideje
        val ertekbecslesFeltoltve: Moment? = null, // Vállalkozó feltöltöte a kész anyago

        val megrendelo: String = "",
        val hitelTipus: String = "",
        val foVallalkozo: String = "",

        val szamlaSorszama: String = "",
        val ertekbecsloDija: Int? = null,

        val megjegyzes: String = "",
        val problema: String = "",

        val alvallalkozoId: Int = 0,
        val ertekbecsloId: Int = 0,

        val ertesitesiNev: String = "",
        val ertesitesiTel: String = "",
        val hitelOsszeg: Int? = null,
        val szamlazhatoDij: Int? = null,
        val ajanlatSzam: String = "",
        val szerzodesSzam: String = "",
        val hetKod: String? = null,
        val munkatipus: String = "",
        val afa: Int = 0,

        val created: Moment = moment(),
        val createdBy: String = "",
        val modified: Moment = moment(),
        val modifiedBy: String = "",

        val files: Array<FileData> = emptyArray(),
        val akadalyok: Array<Akadaly> = emptyArray(),
        val readByAdmin: Boolean = false,
        val readByAlvallalkozo: Boolean = false

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
        val id: Int = 0,
        val leiras: String = "",
        val nettoAr: Int = 0,
        val megrendelo: String,
        val afa: Int = 0,
        val munkatipus: String,

        val created: Moment = moment(),
        val createdBy: String = "",
        val modified: Moment = moment(),
        val modifiedBy: String = ""
) {

}
