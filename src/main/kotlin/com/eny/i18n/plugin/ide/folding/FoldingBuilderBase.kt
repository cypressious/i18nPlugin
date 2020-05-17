package com.eny.i18n.plugin.ide.folding

import com.eny.i18n.plugin.factory.LanguageFactory
import com.eny.i18n.plugin.ide.settings.Settings
import com.eny.i18n.plugin.key.parser.KeyParser
import com.eny.i18n.plugin.tree.CompositeKeyResolver
import com.eny.i18n.plugin.tree.PropertyReference
import com.eny.i18n.plugin.tree.PsiElementTree
import com.eny.i18n.plugin.key.FullKey
import com.eny.i18n.plugin.utils.LocalizationSourceSearch
import com.eny.i18n.plugin.utils.ellipsis
import com.eny.i18n.plugin.utils.unQuote
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement

internal data class ElementToReferenceBinding(val psiElement: PsiElement, val reference: PropertyReference<PsiElement>)

/**
 * Provides folding mechanism for i18n keys
 */
abstract class FoldingBuilderBase(private val languageFactory: LanguageFactory) : FoldingBuilderEx(), DumbAware, CompositeKeyResolver<PsiElement> {

    private val parser: KeyParser = KeyParser()

    override fun getPlaceholderText(node: ASTNode): String? = ""

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val settings = Settings.getInstance(root.project)
        if (!settings.foldingEnabled) return arrayOf()
        val search = LocalizationSourceSearch(root.project)
        val group = FoldingGroup.newGroup("i18n")
        val foldingProvider = languageFactory.foldingProvider()
        return foldingProvider.collectContainers(root)
            .flatMap { container ->
                val (literals, offset) = foldingProvider.collectLiterals(container)
                literals.mapNotNull { literal ->
                    parser
                        .parse(literal.text.unQuote(), settings.nsSeparator, settings.keySeparator, settings.stopCharacters, settings.vue)
                        ?.let { key -> resolve(literal, search, settings, key) }
                        ?.let { resolved ->
                            val placeholder = resolved.reference.element?.value()?.text?.unQuote()?.ellipsis(settings.foldingMaxLength) ?: ""
                            val textRange = foldingProvider.getFoldingRange(container, offset, resolved.psiElement)
                            FoldingDescriptor(
                                container.node,
                                textRange,
                                group,
                                placeholder
                            )
                        }
                }
            }.toTypedArray()
    }

    private fun resolve(element: PsiElement, search: LocalizationSourceSearch, settings: Settings, fullKey: FullKey): ElementToReferenceBinding? {
        return search
            .findFilesByName(fullKey.ns?.text)
            .filter {
                if (settings.vue) it.name.contains(settings.foldingPreferredLanguage)
                else it.parent == settings.foldingPreferredLanguage
            }
            .map { resolveCompositeKey(fullKey.compositeKey, PsiElementTree.create(it.element)) }
            .firstOrNull { it.unresolved.isEmpty() && it.element?.isLeaf() == true }
            ?.let { ElementToReferenceBinding(element, it) }
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean = true
}
