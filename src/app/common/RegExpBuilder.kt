package app.common

import kotlin.js.RegExp


//var r = kotlinext.js.require("regexpbuilder")

@JsModule("regexpbuilder")
external object RegExpBuilder {
    fun find(str: String):  RegExpBuilder = definedExternally
    fun min(num: Int):  RegExpBuilder = definedExternally
    fun max(num: Int):  RegExpBuilder = definedExternally
    fun exactly(num: Int):  RegExpBuilder = definedExternally
    fun then(str: String):  RegExpBuilder = definedExternally
    fun like(pattern:  RegExpBuilder = definedExternally):  RegExpBuilder = definedExternally
    fun append(pattern:  RegExpBuilder = definedExternally):  RegExpBuilder = definedExternally
    fun optional(pattern:  RegExpBuilder = definedExternally):  RegExpBuilder = definedExternally
    fun either(pattern:  RegExpBuilder = definedExternally):  RegExpBuilder = definedExternally
    fun either(pattern: String):  RegExpBuilder = definedExternally
    fun neither(pattern:  RegExpBuilder = definedExternally):  RegExpBuilder = definedExternally
    fun neither(pattern: String):  RegExpBuilder = definedExternally
    fun or(pattern:  RegExpBuilder = definedExternally):  RegExpBuilder = definedExternally
    fun or(pattern: String):  RegExpBuilder = definedExternally
    fun nor(pattern:  RegExpBuilder = definedExternally):  RegExpBuilder = definedExternally
    fun nor(pattern: String):  RegExpBuilder = definedExternally
    fun anything():  RegExpBuilder = definedExternally
    fun something():  RegExpBuilder = definedExternally
    fun lineBreak():  RegExpBuilder = definedExternally
    fun lineBreaks():  RegExpBuilder = definedExternally
    fun whitespace():  RegExpBuilder = definedExternally
    fun tab():  RegExpBuilder = definedExternally
    fun tabs():  RegExpBuilder = definedExternally
    fun letter():  RegExpBuilder = definedExternally
    fun letters():  RegExpBuilder = definedExternally
    fun startOfInput():  RegExpBuilder = definedExternally
    fun startOfLine():  RegExpBuilder = definedExternally
    fun endOfInput():  RegExpBuilder = definedExternally
    fun endOfLine():  RegExpBuilder = definedExternally
    fun lowerCaseLetter():  RegExpBuilder = definedExternally
    fun lowerCaseLetters():  RegExpBuilder = definedExternally
    fun upperCaseLetter():  RegExpBuilder = definedExternally
    fun upperCaseLetters():  RegExpBuilder = definedExternally
    fun digit():  RegExpBuilder = definedExternally
    fun digits():  RegExpBuilder = definedExternally
    fun asGroup():  RegExpBuilder = definedExternally
    fun ofGroup():  RegExpBuilder = definedExternally
    fun ignoreCase():  RegExpBuilder = definedExternally
    fun anythingBut(str: String):  RegExpBuilder = definedExternally
    fun of(str: String):  RegExpBuilder = definedExternally
    fun maybe(str: String):  RegExpBuilder = definedExternally
    fun from(str: Array<String>):  RegExpBuilder = definedExternally
    fun some(str: Array<String>):  RegExpBuilder = definedExternally
    fun maybeSome(str: Array<String>):  RegExpBuilder = definedExternally
    fun notFrom(str: Array<String>):  RegExpBuilder = definedExternally

    fun getRegExp(): RegExp = definedExternally
}