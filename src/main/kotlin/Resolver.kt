import java.util.*

class Resolver(val interpreter: Interpreter) :  Expr.Visitor<Void?>, Stmt.Visitor<Void?> {

    enum class ClassType {
        NONE, CLASS, SUBCLASS
    }
    private var currentClass = ClassType.NONE

    enum class FunctionType {
        NONE, FUNCTION, METHOD, INITIALIZER
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

    override fun visitGetExpr(expr: GetExpr): Void? {
        resolve(expr.loxObject)
        return null
    }

    override fun visitLogicalExpr(expr: Logical): Void? {
        resolve(expr.left)
        resolve(expr.right)
        return null
    }

    override fun visitSetExpr(expr: SetExpr): Void? {
        resolve(expr.value)
        resolve(expr.loxObject)
        return null
    }

    override fun visitSuperExpr(expr: Super): Void? {
        if (currentClass == ClassType.NONE) {
            KLox.error(expr.keyword, "Can't use 'super' outside of a class.")
        } else if (currentClass != ClassType.SUBCLASS) {
            KLox.error(expr.keyword, "Can't use 'super' in a class with no superclass.")
        }

        resolveLocal(expr, expr.keyword)
        return null
    }

    override fun visitThisExpr(expr: This): Void? {
        if (currentClass == ClassType.NONE) {
            KLox.error(expr.keyword, "Can't use 'this' outside of a class.")
            return null
        }
        resolveLocal(expr, expr.keyword)
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

    override fun visitClassStmt(stmt: ClassStmt): Void? {
        var enclosingClass = currentClass
        currentClass = ClassType.CLASS

        declare(stmt.name)
        define(stmt.name)
        stmt.superClass?.let {
            if (stmt.superClass.name.lexeme == stmt.name.lexeme) {
                KLox.error(stmt.superClass.name, "A class can not inherit from itself.")
            }
            currentClass = ClassType.SUBCLASS
            resolve(stmt.superClass)

            beginScope()
            scopes.peek()["super"] = true
        }

        beginScope()
        scopes.peek()["this"] = true

        for (method in stmt.methods) {
            val declaration = if (method.name.lexeme == "init") {
                FunctionType.INITIALIZER
            } else {
                FunctionType.METHOD
            }
            resolveFunction(method, declaration)
        }

        endScope()
        stmt.superClass?.let { endScope() }
        currentClass = enclosingClass
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
            if (currentFunction == FunctionType.INITIALIZER) {
                KLox.error(stmt.keyword, "Can't return a value from an initializer")
            }
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
