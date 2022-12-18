import TokenType.*

class Parser(val tokens: List<Token>) {
    class ParseError : RuntimeException()

    var current = 0

    fun parse() : List<Stmt> {
        val statements = ArrayList<Stmt>()
        while(isNotAtEnd()) {
            if (declaration() == null) {
                KLox.error(0, "declaration was null")
            } else {
                val statement = declaration()
                statement?.let {
                    statements.add(it)
                }
            }
        }
        return statements
    }

    fun match(vararg types: TokenType) : Boolean {
        for (type in types) {
            println("Attempting to match ${type.name}")
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
        if (match(TRUE)) return Literal(true)
        if (match(FALSE)) return Literal(false)
        if (match(NIL)) return Literal(null)

        if (match(NUMBER, STRING)) {
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

    fun declaration() : Stmt? {
        try {
            if (match(VAR)) {
                return varDeclaration()
            }
            return statement()
        } catch (error : ParseError) {
            synchronize()
            return null
        }
    }

    fun varDeclaration() : Stmt {
        val name = consume(IDENTIFIER, "Expect variable name.")

        var initializer : Expr? = null
        if (match(EQUAL)) {
            initializer = expression() as Literal
        }

        consume(SEMICOLON, "Expect ';' after variable declaration")
        return VarStmt(name, initializer!!)
    }

    fun expression() : Expr {
        return equality()
    }

    fun statement() : Stmt {
        if (match(PRINT)) return printStatement()
        return expressionStatement()
    }

    fun expressionStatement() : Stmt {
        val expr = expression()
        consume(SEMICOLON, "Expect ';' after expression")
        return ExpressionStmt(expr)
    }

    fun printStatement() : Stmt {
        val value = expression()
        consume(SEMICOLON, "Expect ';' after value")
        return PrintStmt(value)
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