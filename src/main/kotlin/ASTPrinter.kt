import java.lang.StringBuilder
import kotlin.math.exp

fun main() {
    val astPrinter = ASTPrinter()

    val expression = Binary(Unary(Token(TokenType.MINUS, "-", null, 1),
        Literal(123)),
        Token(TokenType.STAR, "*", null, 1),
        Grouping(Literal(45.67)))

    println(astPrinter.print(expression))
}

class ASTPrinter : Expr.Visitor<String> {
    fun print(expr: Expr) : String {
        return expr.accept(this)
    }

    private fun parenthesize(name: String, vararg exprs: Expr): String {
        val builder = StringBuilder()

        builder.append("(").append(name)
        for (expr in exprs) {
            builder.append(" ")
            builder.append(expr.accept(this))
        }
        builder.append(")")
        return builder.toString()
    }


    override fun visitBinaryExpr(expr: Binary): String {
        return parenthesize(expr.operator.lexeme, expr.left,  expr.right)
    }

    override fun visitCallExpr(expr: Call): String {
        return parenthesize("Call ${expr.paren.lexeme}")
    }

    override fun visitLogicalExpr(expr: Logical): String {
        return parenthesize(expr.operator.lexeme)
    }

    override fun visitGroupingExpr(expr: Grouping): String {
        return parenthesize("group", expr.expression)
    }

    override fun visitLiteralExpr(expr: Literal): String {
        expr.value?.let {
            return it.toString()
        }
        return "nil"
    }

    override fun visitVariableExpr(expr: Variable): String {
        return parenthesize(expr.name.toString())
    }

    override fun visitAssignExpr(expr: Assign): String {
        return expr.name.toString()
    }

    override fun visitUnaryExpr(expr: Unary): String {
        return parenthesize(expr.operator.lexeme, expr.right)
    }


}