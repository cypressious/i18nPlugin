package utils

import com.eny.i18n.plugin.utils.*
import org.junit.Test
import kotlin.test.assertEquals

class ExpressionKeyParserTest : TestBase {

//    fileName:ROOT.Key2.Key3
    @Test
    fun parseSimpleLiteral() {
        val literal = listOf(
            KeyElement.fromLiteral("fileName:ROOT.Key2.Key3")
        )
        val parser = ExpressionKeyParser()
        val expected = FullKey(
            listOf(Literal("fileName")),
            listOf(
                Literal("ROOT"),
                Literal("Key2"),
                Literal("Key3")
            )
        )
        val actual = parser.parse(literal)
        assertEquals(expected, actual)
        assertEquals(23, actual?.length)
        assertEquals(8, actual?.nsLength)
        assertEquals(14, actual?.keyLength)
    }

    //${fileExpr}:ROOT.Key1.Key31       / sample
    @Test
    fun parseExpressionWithFilePartInTemplate() {
        val elements = listOf(
            KeyElement("\${fileExpr}", "sample", KeyElementType.TEMPLATE),
            KeyElement(":ROOT.Key1.Key31", ":ROOT.Key1.Key31", KeyElementType.LITERAL)
        )
        val parser = ExpressionKeyParser()
        val expectedFileName = listOf("sample")
        val expectedKey = listOf("ROOT", "Key1", "Key31")
        val parsed = parser.parse(elements)
        assertEquals(expectedFileName, extractTexts(parsed?.fileName ?: listOf()))
        assertEquals(expectedKey, extractTexts(parsed?.compositeKey ?: listOf()))
        assertEquals(27, parsed?.length)
        assertEquals(11, parsed?.nsLength)
        assertEquals(15, parsed?.keyLength)
    }

    //prefix${fileExpr}:ROOT.Key4.Key5  / sample
    @Test
    fun parsePrefixedExpressionWithFilePartInTemplate() {
        val elements = listOf(
                KeyElement("prefix", "prefix", KeyElementType.LITERAL),
                KeyElement("\${fileExpr}", "sample", KeyElementType.TEMPLATE),
                KeyElement(":ROOT.Key4.Key5", ":ROOT.Key4.Key5", KeyElementType.LITERAL)
        )
        val parser = ExpressionKeyParser()
        val expectedFileName = listOf("prefixsample")
        val expectedKey = listOf("ROOT", "Key4", "Key5")
        val parsed = parser.parse(elements)
        assertEquals(expectedFileName, extractTexts(parsed?.fileName ?: listOf()))
        assertEquals(expectedKey, extractTexts(parsed?.compositeKey ?: listOf()))
//        assertEquals(17, parsed?.nsLength)
//        assertEquals(14, parsed?.keyLength)
//        assertEquals(32, parsed?.length)
    }

    //${fileExpr}postfix:ROOT.Key4.Key5  / sample
    @Test
    fun parsePostfixedExpressionWithFilePartInTemplate() {
        val elements = listOf(
            KeyElement("\${fileExpr}", "sample", KeyElementType.TEMPLATE),
            KeyElement("postfix", "postfix", KeyElementType.LITERAL),
            KeyElement(":ROOT.Key4.Key5", ":ROOT.Key4.Key5", KeyElementType.LITERAL)
        )
        val parser = ExpressionKeyParser()
        val expectedFileName = listOf("sample", "postfix")
        val expectedKey = listOf("ROOT", "Key4", "Key5")
        val parsed = parser.parse(elements)
        assertEquals(expectedFileName, extractTexts(parsed?.fileName ?: listOf()))
        assertEquals(expectedKey, extractTexts(parsed?.compositeKey ?: listOf()))
    }

    //prefix${fileExpr}postfix:ROOT.Key4.Key5  / sample
    @Test
    fun parseMixedExpressionWithFilePartInTemplate() {
        val elements = listOf(
                KeyElement("prefix", "prefix", KeyElementType.LITERAL),
                KeyElement("\${fileExpr}", "sample", KeyElementType.TEMPLATE),
                KeyElement("postfix", "postfix", KeyElementType.LITERAL),
                KeyElement(":ROOT.Key4.Key5", ":ROOT.Key4.Key5", KeyElementType.LITERAL)
        )
        val parser = ExpressionKeyParser()
        val expectedFileName = listOf("prefixsamplepostfix")
        val expectedKey = listOf("ROOT", "Key4", "Key5")
        val parsed = parser.parse(elements)
        assertEquals(expectedFileName, extractTexts(parsed?.fileName ?: listOf()))
        assertEquals(expectedKey, extractTexts(parsed?.compositeKey ?: listOf()))
    }

    //prefix${fileExpr}postfix.ROOT.Key4.Key5   / partFile:partKey
    @Test
    fun parseNsSeparatorInExpression() {
        val elements = listOf(
                KeyElement("prefix", "prefix", KeyElementType.LITERAL),
                KeyElement("\${fileExpr}", "partFile:partKey", KeyElementType.TEMPLATE),
                KeyElement("postfix", "postfix", KeyElementType.LITERAL),
                KeyElement(".ROOT.Key4.Key5", ".ROOT.Key4.Key5", KeyElementType.LITERAL)
        )
        val parser = ExpressionKeyParser()
        val expectedFileName = listOf("prefix", "partFile")
        val expectedKey = listOf("partKeypostfix", "ROOT", "Key4", "Key5")
        val parsed = parser.parse(elements)
        assertEquals(expectedFileName, extractTexts(parsed?.fileName ?: listOf()))
        assertEquals(expectedKey, extractTexts(parsed?.compositeKey ?: listOf()))
    }

    //filename:${key}   / Key0.Key2.Key21
    @Test
    fun parseExpressionWithKeyInTemplate() {
        val elements = listOf(
            KeyElement("filename:", "filename:", KeyElementType.LITERAL),
            KeyElement("\${key}", "Key0.Key2.Key21", KeyElementType.TEMPLATE)
        )
        val parser = ExpressionKeyParser()
        val expectedFileName = listOf("filename")
        val expectedKey = listOf("Key0", "Key2", "Key21")
        val parsed = parser.parse(elements)
        assertEquals(expectedFileName, extractTexts(parsed?.fileName ?: listOf()))
        assertEquals(expectedKey, extractTexts(parsed?.compositeKey ?: listOf()))
    }

//    filename:${key}item   / Key0.Key2.Key21.
    @Test
    fun parseExpressionWithKeyInTemplate2() {
        val elements = listOf(
                KeyElement("filename:", "filename:", KeyElementType.LITERAL),
                KeyElement("\${key}", "Key0.Key2.Key21.", KeyElementType.TEMPLATE),
                KeyElement("item", "item", KeyElementType.LITERAL)
        )
        val parser = ExpressionKeyParser()
        val expectedFileName = listOf("filename")
        val expectedKey = listOf("Key0", "Key2", "Key21", "item")
        val parsed = parser.parse(elements)
        assertEquals(expectedFileName, extractTexts(parsed?.fileName ?: listOf()))
        assertEquals(expectedKey, extractTexts(parsed?.compositeKey ?: listOf()))
    }

    //filename:${key}.item   / Key0.Key2.Key21
    @Test
    fun parseExpressionWithKeyInTemplate3() {
        val elements = listOf(
                KeyElement("filename:", "filename:", KeyElementType.LITERAL),
                KeyElement("\${key}", "Key0.Key2.Key21", KeyElementType.TEMPLATE),
                KeyElement(".item", ".item", KeyElementType.LITERAL)
        )
        val parser = ExpressionKeyParser()
        val expectedFileName = listOf("filename")
        val expectedKey = listOf("Key0", "Key2", "Key21", "item")
        val parsed = parser.parse(elements)
        assertEquals(expectedFileName, extractTexts(parsed?.fileName ?: listOf()))
        assertEquals(expectedKey, extractTexts(parsed?.compositeKey ?: listOf()))
    }

    //filename:${key}item   / Key0.Key2.Key21
    @Test
    fun parseExpressionWithKeyInTemplate4() {
        val elements = listOf(
                KeyElement("filename:", "filename:", KeyElementType.LITERAL),
                KeyElement("\${key}", "Key0.Key2.Key21", KeyElementType.TEMPLATE),
                KeyElement("item", "item", KeyElementType.LITERAL)
        )
        val parser = ExpressionKeyParser()
        val expectedFileName = listOf("filename")
        val expectedKey = listOf("Key0", "Key2", "Key21item")
        val parsed = parser.parse(elements)
        assertEquals(expectedFileName, extractTexts(parsed?.fileName ?: listOf()))
        assertEquals(expectedKey, extractTexts(parsed?.compositeKey ?: listOf()))
    }

    //filename:root.${key}  / Key0.Key2.Key21
    @Test
    fun partOfKeyIsExpression() {
        val elements = listOf(
            KeyElement("filename:root.", "filename:root.", KeyElementType.LITERAL),
            KeyElement("\${key}", "Key0.Key2.Key21", KeyElementType.TEMPLATE)
        )
        val parser = ExpressionKeyParser()
        val expectedFileName = listOf("filename")
        val expectedKey = listOf("root", "Key0", "Key2", "Key21")
        val parsed = parser.parse(elements)
        assertEquals(expectedFileName, extractTexts(parsed?.fileName ?: listOf()))
        assertEquals(expectedKey, extractTexts(parsed?.compositeKey ?: listOf()))
    }

    //filename:root${key}   / .Key0.Key2.Key21
    @Test
    fun partOfKeyIsExpression2() {
        val elements = listOf(
                KeyElement("filename:root", "filename:root", KeyElementType.LITERAL),
                KeyElement("\${key}", ".Key0.Key2.Key21", KeyElementType.TEMPLATE)
        )
        val parser = ExpressionKeyParser()
        val expectedFileName = listOf("filename")
        val expectedKey = listOf("root", "Key0", "Key2", "Key21")
        val parsed = parser.parse(elements)
        assertEquals(expectedFileName, extractTexts(parsed?.fileName ?: listOf()))
        assertEquals(expectedKey, extractTexts(parsed?.compositeKey ?: listOf()))
    }

    //filename:root${key}   / Key0.Key2.Key21
    @Test
    fun partOfKeyIsExpression3() {
        val elements = listOf(
                KeyElement("filename:root", "filename:root", KeyElementType.LITERAL),
                KeyElement("\${key}", "Key0.Key2.Key21", KeyElementType.TEMPLATE)
        )
        val parser = ExpressionKeyParser()
        val expectedFileName = listOf("filename")
        val expectedKey = listOf("rootKey0", "Key2", "Key21")
        val parsed = parser.parse(elements)
        assertEquals(expectedFileName, extractTexts(parsed?.fileName ?: listOf()))
        assertEquals(expectedKey, extractTexts(parsed?.compositeKey ?: listOf()))
    }
}