package benetis.me

import com.goide.psi.*
import com.goide.stubs.index.GoFunctionIndex
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil

class StructLiteralLineMarkerProvider : LineMarkerProvider {

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        val typeRef = element as? GoTypeReferenceExpression ?: return null
        val composite = typeRef.parent as? GoCompositeLit ?: return null

        if (!composite.isStructLiteral || composite.literalValue?.elementList.isNullOrEmpty()) return null

        val structName = typeRef.identifier.text ?: return null
        val constructorName = "New$structName"

        if (typeRef.isInsideConstructor(constructorName)) return null

        val target = GoFunctionIndex
            .find(constructorName, element.project, GlobalSearchScope.projectScope(element.project), null)
            .filterIsInstance<GoFunctionDeclaration>() // Top level only
            .firstOrNull() ?: return null

        val navHandler = GutterIconNavigationHandler<PsiElement> { _, _ -> target.navigate(true) }

        return LineMarkerInfo(
            typeRef.identifier,
            typeRef.textRange,
            Icons.CONSTRUCTOR_GUTTER,
            { "Use $constructorName instead" },
            navHandler,
            GutterIconRenderer.Alignment.LEFT
        )
    }

    /* Helpers */
    private val GoCompositeLit.isStructLiteral: Boolean
        get() = literalValue != null

    private fun GoTypeReferenceExpression.isInsideConstructor(constrName: String): Boolean =
        PsiTreeUtil.getParentOfType(this, GoFunctionDeclaration::class.java)?.name == constrName
}