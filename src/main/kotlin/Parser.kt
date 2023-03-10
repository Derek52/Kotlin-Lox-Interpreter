import TokenType.*
import kotlin.math.exp

class Parser(val tokens: List<Token>) {
    class ParseError : RuntimeException()

    var current = 0

    fun parse() : List<Stmt> {
        val statements = ArrayList<Stmt>()
        while (isNotAtEnd()) {
            statements.add(declaration()!!)
        }
        return statements
    }

    fun match(vararg types: TokenType) : Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }
        return false
    }

    fun equality() : Expr {
        var expr = comparison()
        while(match(BANG_EQUAL, EQUAL_EQUAL)) {
            val operator = previous()
            val right = comparison()
            expr = Binary(expr, operator, right)
        }
        return expr
    }

    fun comparison() : Expr {
        var expr = term()
        while(match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            val operator = previous()
            val right = term()
            expr = Binary(expr,operator, right)
        }
        return expr
    }

    fun term() : Expr {
        var expr = factor()
        while(match(MINUS, PLUS)) {
            val operator = previous()
            val right = factor()
            expr = Binary(expr, operator, right)
        }
        return expr
    }

    fun factor() : Expr {
        var expr = unary()
        while(match(SLASH, STAR)) {
            val operator = previous()
            val right = unary()
            expr = Binary(expr, operator, right)
        }
        return expr
    }

    fun unary() : Expr {
        if(match(BANG, MINUS)) {
            val operator = previous()
            val right = unary()
            return Unary(operator, right)
        }
        return primary()
    }

    fun primary() : Expr {
        if(match(TRUE)) return Literal(true)
        if(match(FALSE)) return Literal(false)
        if(match(NIL)) return Literal(null)

        if(match(NUMBER, STRING)) {
            return Literal(previous().literal)
        }

        if (match(IDENTIFIER)) {
            return Variable(previous())
        }

        if (match(LEFT_PAREN)) {
            val expr = expression()
            consume(RIGHT_PAREN, "Expect ')' after expression")
            return Grouping(expr)
        }

        throw error(peek(), "Expect expression.")
    }

    fun expression() : Expr {
        return assignment()
    }

    fun declaration() : Stmt? {
        try {
            if(match(VAR)) return varDeclaration()
            return statement()
        } catch (error : ParseError) {
            synchronize()
            return null
        }
    }

    fun statement() : Stmt {
        if (match(FOR)) return forStatement()
        if (match(IF)) return ifStatement()
        if (match(PRINT)) return printStatement()
        if (match(WHILE)) return whileStatement()
        if (match(LEFT_BRACE)) return BlockStmt(block())

        return expressionStatement()
    }

    fun forStatement() : Stmt {
        consume(LEFT_PAREN, "Expect '(' after 'for'.")

        var initializer: Stmt? = null
        if (match(SEMICOLON)) {
            initializer = null
        } else if (match(VAR)) {
            initializer = varDeclaration()
        } else {
            initializer = expressionStatement()
        }

        var condition: Expr? = null

        if (!check(SEMICOLON)) {
            condition = expression()
        }
        consume(SEMICOLON, "Expect ';' after for-loop condition.")


        var increment: Expr? = null
        if (!check(RIGHT_PAREN)) {
            increment = expression()
        }
        consume(RIGHT_PAREN, "Expect ')' after loop condition.")

        var body = statement()

        increment?.let { inc ->
            body = BlockStmt(listOf(body, ExpressionStmt(inc)))
        }

        if (condition == null) condition = Literal(true)

        body = WhileStmt(condition, body)

        initializer?.let {
            body = BlockStmt(listOf(initializer, body))
        }

        return body
    }

    fun ifStatement() : Stmt {
        consume(LEFT_PAREN, "Expect '(' after 'if'.")
        val condition = expression()
        consume(RIGHT_PAREN, "Expect ')' after if condition.")

        val thenBranch = statement()
        var elseBranch: Stmt? = null
        if (match(ELSE)) {
            elseBranch = statement()
        }

        return IfStmt(condition, thenBranch, elseBranch)
    }

    fun whileStatement() : Stmt {
        consume(LEFT_PAREN, "Expect '(' after 'while'.")
        val condition = expression()
        consume(RIGHT_PAREN, "Expect ')' after condition.")
        val body = statement()
        return WhileStmt(condition, body)
    }

    fun block() : List<Stmt> {
        val statements = ArrayList<Stmt>()

        while(!check(RIGHT_BRACE)  && isNotAtEnd()) {
            statements.add(declaration()!!)
        }

        consume(RIGHT_BRACE, "Expect '}' after block.")
        return statements
    }

    fun expressionStatement() : Stmt {
        val expr = expression()
        consume(SEMICOLON, "Expect ';' after expression.")
        return ExpressionStmt(expr)
    }

    fun assignment(): Expr {
        val expr = or()

        if (match(EQUAL)) {
            val equals = previous()
            val value = assignment()

            if (expr is Variable) {
                val name = expr.name
                return Assign(name, value)
            }

            error(equals, "Invalid assignment target.")
        }

        return expr
    }

    fun or() : Expr {
        var expr = and()

        while (match(OR)) {
            val operator = previous()
            val right = and()
            expr = Logical(expr, operator, right)
        }
        return expr
    }

    fun and() : Expr {
        var expr = equality()

        while (match(AND)) {
            val operator = previous()
            val right = equality()
            expr = Logical(expr, operator, right)
        }
        return expr
    }

    fun printStatement() : Stmt {
        val value = expression()
        consume(SEMICOLON, "Expect ';' after value.")
        return PrintStmt(value)
    }

    fun varDeclaration() : Stmt {
        val name = consume(IDENTIFIER, "Expect variable name.")

        var initializer : Expr? = null
        if (match(EQUAL)) {
            initializer = expression()
        }

        consume(SEMICOLON, "Expect ';' after variable declaration.")
        return VarStmt(name, initializer)
    }

    fun check(type: TokenType) : Boolean {
        if (isAtEnd()) return false
        return peek().type == type
    }

    fun peek() : Token {
        return tokens[current]
    }

    fun consume(type: TokenType, message: String) : Token {
        if (check(type)) return advance()
        throw error(peek(), message)
    }

    fun advance() : Token {
        if (isNotAtEnd()) current++
        return previous()
    }

    fun previous() : Token {
        return tokens[current - 1]
    }



    fun error(token: Token, message: String) : ParseError {
        KLox.error(token, message)
        return ParseError()
    }

    fun synchronize() {
        advance()
        while(isNotAtEnd()) {
            if (previous().type == SEMICOLON) return

            val typesToReturnFrom = listOf(CLASS, FOR, FUN, IF, PRINT, RETURN, VAR, WHILE)
            for (type in typesToReturnFrom) {
                if (peek().type == type) return
            }
            advance()
        }
    }

    fun isAtEnd() : Boolean {
        return peek().type == EOF
    }

    fun isNotAtEnd() : Boolean {
        return !isAtEnd()
    }
}