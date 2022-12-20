abstract class Stmt() {

	interface Visitor<R> {
		fun visitBlockStmt(stmt: BlockStmt) : R
		fun visitExpressionStmt(stmt: ExpressionStmt) : R
		fun visitFunctionStmt(stmt: FunctionStmt) : R
		fun visitVarStmt(stmt: VarStmt) : R
		fun visitIfStmt(stmt: IfStmt) : R
		fun visitPrintStmt(stmt: PrintStmt) : R
		fun visitWhileStmt(stmt: WhileStmt) : R
	}

	abstract fun <R> accept(visitor: Visitor<R>): R
}

class BlockStmt(val statements: List<Stmt>) : Stmt() {

	override fun <R> accept(visitor: Visitor<R>) : R {
		return visitor.visitBlockStmt(this)
	}
}

class ExpressionStmt(val expression: Expr) : Stmt() {

	override fun <R> accept(visitor: Visitor<R>) : R {
		return visitor.visitExpressionStmt(this)
	}
}

class FunctionStmt(val name: Token, params: List<Token>, val body: List<Stmt>) : Stmt() {

	override fun <R> accept(visitor: Visitor<R>) : R {
		return visitor.visitFunctionStmt(this)
	}
}

class VarStmt(val name: Token, val initializer: Expr?) : Stmt() {

	override fun <R> accept(visitor: Visitor<R>) : R {
		return visitor.visitVarStmt(this)
	}
}

class IfStmt(val condition: Expr, val thenBranch: Stmt, val elseBranch: Stmt?) : Stmt() {

	override fun <R> accept(visitor: Visitor<R>) : R {
		return visitor.visitIfStmt(this)
	}
}

class PrintStmt(val expression: Expr) : Stmt() {

	override fun <R> accept(visitor: Visitor<R>) : R {
		return visitor.visitPrintStmt(this)
	}
}

class WhileStmt(val condition: Expr, val body: Stmt) : Stmt() {

	override fun <R> accept(visitor: Visitor<R>) : R {
		return visitor.visitWhileStmt(this)
	}
}

