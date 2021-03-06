package com.eny.i18n.plugin.localization.json

import com.eny.i18n.plugin.factory.ContentGenerator
import com.eny.i18n.plugin.factory.LocalizationFactory
import com.eny.i18n.plugin.ide.settings.Settings
import com.eny.i18n.plugin.key.FullKey
import com.eny.i18n.plugin.key.lexer.Literal
import com.eny.i18n.plugin.utils.PluginBundle
import com.intellij.json.JsonFileType
import com.intellij.json.JsonLanguage
import com.intellij.json.psi.JsonElementGenerator
import com.intellij.json.psi.JsonObject
import com.intellij.lang.Language
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.PsiElement

private val tabChar = "  "

class JsonLocalizationFactory: LocalizationFactory {
    override fun contentGenerator(): ContentGenerator = JsonContentGenerator()
}

/**
 * Generates JSON translation content
 */
class JsonContentGenerator: ContentGenerator {

    override fun generateContent(compositeKey: List<Literal>, value: String): String =
        compositeKey.foldRightIndexed("\"$value\"", { i, key, acc ->
            val tab = tabChar.repeat(i)
            "{\n$tabChar$tab\"${key.text}\": $acc\n$tab}"
        })

    override fun getFileType(): FileType = JsonFileType.INSTANCE
    override fun getLanguage(): Language = JsonLanguage.INSTANCE
    override fun getDescription(): String = PluginBundle.getMessage("quickfix.create.json.translation.files")
    override fun isSuitable(element: PsiElement): Boolean = element is JsonObject
    override fun generateTranslationEntry(element: PsiElement, key: String, value: String) {
        val obj = element as JsonObject
        val generator = JsonElementGenerator(element.project)
        val keyValue = generator.createProperty(key, value)
        val props = obj.getPropertyList()
        val separator = generator.createComma()
        val pair = if (Settings.getInstance(element.project).extractSorted) {
            val before = props.takeWhile {it.name < key}
            if (before.isEmpty()) {
                Pair(separator, obj.addBefore(keyValue, props.first()))
            } else {
                Pair(keyValue, obj.addAfter(separator, before.last()))
            }
        }
        else {
            Pair(keyValue, obj.addAfter(separator, props.last()))
        }
        obj.addAfter(
            pair.first,
            pair.second
        )
    }
    override fun generate(element: PsiElement, fullKey: FullKey, unresolved: List<Literal>, translationValue: String?) =
        generateTranslationEntry(
            element,
            unresolved.first().text,
            generateContent(unresolved.drop(1), translationValue ?: fullKey.source)
        )
}