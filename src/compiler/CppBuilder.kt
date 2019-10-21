package compiler

class CppBuilder {
    private val builder = StringBuilder()
    private var indentLevel = 0

    override fun toString() = builder.toString()

    private fun makeIndent() {
        for (i in 0 until indentLevel) {
            builder.append("    ")
        }
    }

    fun appendFunctionHeader(name: String, returnType: String, parameters: List<Pair<String, String>>) {
        makeIndent()
        builder
            .append(returnType)
            .append(" ")
            .append(name)
            .append("(")
        parameters.forEachIndexed { index, (name, type) ->
            if (index > 0) {
                builder.append(", ")
            }
            builder
                .append(type)
                .append(" ")
                .append(name)
        }
        builder.append(") ")
    }

    fun appendVarDeclaration(formattedDecl: String, initializer: String? = null) {
        makeIndent()
        builder.append(formattedDecl)
        if (initializer != null) {
            builder
                .append(" = ")
                .append(initializer)
        }
        builder.appendln(";")
    }

    fun appendHeader(name: String) {
        builder
            .append("#include <")
            .append(name)
            .appendln(">")
    }

    fun appendNewLine() {
        builder.appendln()
    }

    fun withBlock(block: CppBuilder.() -> Unit) {
        builder.appendln("{")
        indentLevel++
        block()
        indentLevel--
        makeIndent()
        builder.appendln("}")
    }
}