abstract class Expr() {

	interface Visitor<R> {
		fun visitUnaryExpr(expr: Unary) : R
		fun visitBinaryExpr(expr: Binary) : R
		fun visitGroupingExpr(expr: Grouping) : R
		fun visitLiteralExpr(expr: Literal) : R
		fun visitVariableExpr(expr: Variable) : R
	}

	abstract fun <R> accept(visitor: Visitor<R>): R
}

class Unary(val operator: Token, val right: Expr) : Expr() {

	override fun <R> accept(visitor: Visitor<R>) : R {
		return visitor.visitUnaryExpr(this)
	}
}

class Binary(val left: Expr, val operator: Token, val right: Expr) : Expr() {

	override fun <R> accept(visitor: Visitor<R>) : R {
		return visitor.visitBinaryExpr(this)
	}
}

class Grouping(val expression: Expr) : Expr() {

	override fun <R> accept(visitor: Visitor<R>) : R {
		return visitor.visitGroupingExpr(this)
	}
}

class Literal(val value: Any?) : Expr() {

	override fun <R> accept(visitor: Visitor<R>) : R {
		return visitor.visitLiteralExpr(this)
	}
}

class Variable(name: Token) : Expr() {

	override fun <R> accept(visitor: Visitor<R>) : R {
		return visitor.visitVariableExpr(this)
	}
}

