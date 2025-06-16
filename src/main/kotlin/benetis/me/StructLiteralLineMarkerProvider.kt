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

        val structIdentifier = typeRef.identifier
        val structName = structIdentifier.text ?: return null

        val qualifier = typeRef.qualifier?.reference?.resolve() as? GoImportSpec
        val packageName = qualifier?.alias ?: qualifier?.path?.removeSurrounding("\"")
        val expectedConstructorName = "New$structName"

        if (typeRef.isInsideConstructor(expectedConstructorName)) return null

        val functions = GoFunctionIndex
            .find(expectedConstructorName, element.project, GlobalSearchScope.projectScope(element.project), null)
            .filterIsInstance<GoFunctionDeclaration>()

        val structPackage = (typeRef.reference.resolve() as? GoTypeSpec)?.containingFile?.packageName

        val target = functions.firstOrNull { function ->
            val functionPackage = function.containingFile.packageName
            functionPackage == packageName || (packageName == null && functionPackage == structPackage)
        } ?: return null

        val navHandler = GutterIconNavigationHandler<PsiElement> { _, _ -> target.navigate(true) }

        val tooltip = if (packageName != null)
            "Use $packageName.$expectedConstructorName instead"
        else
            "Use $expectedConstructorName instead"

        return LineMarkerInfo(
            structIdentifier,
            typeRef.textRange,
            Icons.CONSTRUCTOR_GUTTER,
            { tooltip },
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