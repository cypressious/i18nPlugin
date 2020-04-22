package com.eny.i18n.plugin.ide.folding

import com.intellij.lang.javascript.psi.JSCallExpression

/**
 * Js i18n folding builder
 */
class JsFoldingBuilder: FoldingBuilderBase<JSCallExpression>(
    JSCallExpression::class.java,
    FoldingElementsCollectorJs()
)