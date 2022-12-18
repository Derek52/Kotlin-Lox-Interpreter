abstract class Stmt() {

	interface Visitor<R> {
		fun visitExpressionStmt(stmt: ExpressionStmt) : R
		fun visitVarStmt(stmt: VarStmt) : R
		fun visitPrintStmt(stmt: PrintStmt) : R
	}

	abstract fun <R> accept(visitor: Visitor<R>): R
}

class ExpressionStmt(val expression: Expr) : Stmt() {

	override fun <R> accept(visitor: Visitor<R>) : R {
		return visitor.visitExpressionStmt(this)
	}
}

class VarStmt(val name: Token, val initializer: Expr) : Stmt() {

	override fun <R> accept(visitor: Visitor<R>) : R {
		return visitor.visitVarStmt(this)
	}
}

class PrintStmt(val expression: Expr) : Stmt() {

	override fun <R> accept(visitor: Visitor<R>) : R {
		return visitor.visitPrintStmt(this)
	}
}

