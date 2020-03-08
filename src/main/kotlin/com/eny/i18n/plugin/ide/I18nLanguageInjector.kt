package com.eny.i18n.plugin.ide

import com.intellij.lang.Language
import com.intellij.lang.javascript.patterns.JSPatterns
import com.intellij.openapi.util.TextRange
import com.intellij.psi.InjectedLanguagePlaces
import com.intellij.psi.LanguageInjector
import com.intellij.psi.PsiLanguageInjectionHost

class I18nLanguageInjector : LanguageInjector {
    val pattern = JSPatterns.jsArgument("t", 0)

    override fun getLanguagesToInject(host: PsiLanguageInjectionHost, injectionPlacesRegistrar: InjectedLanguagePlaces) {
        if (host.isValidHost && host.textRange.length > 1 && pattern.accepts(host)) {
            val range = TextRange(1, host.textRange.length - 1)
            injectionPlacesRegistrar.addPlace(Language.findLanguageByID("I18n")!!, range, null, null);
        }
    }
}