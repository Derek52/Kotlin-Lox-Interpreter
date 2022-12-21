import java.util.*

class Resolver(val interpreter: Interpreter) :  Expr.Visitor<Void?>, Stmt.Visitor<Void?> {

    enum class FunctionType {
        NONE, FUNCTION
    }

    val scopes = Stack<HashMap<String, Boolean>>()
    var currentFunction = FunctionType.NONE

    fun resolve(statements: List<Stmt>) {
        for (stmt in statements) {
            resolve(stmt)
        }
    }

    fun resolve(stmt : Stmt) {
        stmt.accept(this)
    }

    fun resolve(expr: Expr) {
        expr.accept(this)
    }

    fun declare(name: Token) {
        if (scopes.isEmpty()) return

        val scope = scopes.peek()
        if(scope.contains(name.lexeme)) {
            KLox.error(name, "Already a variable with this name in this scope.")
        }
        scope[name.lexeme] = false
    }

    fun define(name: Token) {
        if (scopes.isEmpty()) return

        scopes.peek()[name.lexeme] = true
    }

    fun resolveLocal(expr: Expr, name: Token) {
        for ( i in scopes.size - 1 downTo 0 ) {
            if (scopes[i].containsKey(name.lexeme)) {
                interpreter.resolve(expr, scopes.size - 1 - i)
                return
            }
        }
    }

    fun beginScope() {
        scopes.push(HashMap())
    }

    fun endScope() {
        scopes.pop()
    }

    override fun visitAssignExpr(expr: Assign): Void? {
        resolve(expr.value)
        resolveLocal(expr, expr.name)
        return null
    }

    override fun visitBinaryExpr(expr: Binary): Void? {
        resolve(expr.left)
        resolve(expr.right)
        return null
    }

    override fun visitCallExpr(expr: Call): Void? {
        resolve(expr.callee)

        for (arg in expr.arguments) {
            resolve(arg)
        }
        return null
    }

    override fun visitLogicalExpr(expr: Logical): Void? {
        resolve(expr.left)
        resolve(expr.right)
        return null
    }

    override fun visitGroupingExpr(expr: Grouping): Void? {
        resolve(expr.expression)
        return null
    }

    override fun visitLiteralExpr(expr: Literal): Void? {
        return null
    }

    override fun visitUnaryExpr(expr: Unary): Void? {
        resolve(expr.right)
        return null
    }

    override fun visitVariableExpr(expr: Variable): Void? {
        if (scopes.isNotEmpty() &&
                scopes.peek()[expr.name.lexeme] == false) {
            KLox.error(expr.name, "Can't read local variable in its own initializer.")
        }
        resolveLocal(expr, expr.name)
        return null
    }

    override fun visitBlockStmt(stmt: BlockStmt): Void? {
        beginScope()
        resolve(stmt.statements)
        endScope()
        return null
    }

    override fun visitExpressionStmt(stmt: ExpressionStmt): Void? {
        resolve(stmt.expression)
        return null
    }

    override fun visitFunctionStmt(stmt: FunctionStmt): Void? {
        declare(stmt.name)
        define(stmt.name)
        resolveFunction(stmt, FunctionType.FUNCTION)
        //resolveFunction(stmt)
        return null
    }

    fun resolveFunction(stmt: FunctionStmt, funcType: FunctionType) {
        val enclosingFunction = currentFunction
        currentFunction = funcType
        beginScope()
        for ( param in stmt.params ) {
            declare(param)
            define(param)
        }
        resolve(stmt.body)
        endScope()
        currentFunction = enclosingFunction
    }

    override fun visitVarStmt(stmt: VarStmt): Void? {
        declare(stmt.name)
        stmt.initializer?.let { initializer ->
            resolve(initializer)
        }
        define(stmt.name)
        return null
    }

    override fun visitIfStmt(stmt: IfStmt): Void? {
        resolve(stmt.condition)
        resolve(stmt.thenBranch)
        stmt.elseBranch?.let { elseBranch ->
            resolve(elseBranch)
        }
        return null
    }

    override fun visitPrintStmt(stmt: PrintStmt): Void? {
        resolve(stmt.expression)
        return null
    }

    override fun visitReturnStmt(stmt: ReturnStmt): Void? {
        if (currentFunction == FunctionType.NONE) {
            KLox.error(stmt.keyword, "Can't return from top-level code.")
        }
        stmt.value?.let { returnValue ->
            resolve(returnValue)
        }
        return null
    }

    override fun visitWhileStmt(stmt: WhileStmt): Void? {
        resolve(stmt.condition)
        resolve(stmt.body)
        return null
    }

}
