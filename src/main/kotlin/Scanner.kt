import TokenType.*
class Scanner(val source: String) {

    var start = 0
    var current = 0
    var line = 1

    companion object {
        val keywords = HashMap<String, TokenType>()
    }

    init {
        keywords["and"] = AND
        keywords["class"] = CLASS
        keywords["else"] = ELSE
        keywords["false"] = FALSE
        keywords["for"] = FOR
        keywords["fun"] = FUN
        keywords["if"] = IF
        keywords["nil"] = NIL
        keywords["or"] = OR
        keywords["print"] = PRINT
        keywords["return"] = RETURN
        keywords["super"] = SUPER
        keywords["this"] = THIS
        keywords["true"] = TRUE
        keywords["var"] = VAR
        keywords["while"] = WHILE
    }

    fun scanTokens() : List<Token> {
        val tokens = ArrayList<Token>()
        while(!isAtEnd()) {
            start = current
            scanToken(tokens)
        }
        tokens.add(Token(EOF, "", null, line))
        return tokens
    }

    fun scanToken(tokens: MutableList<Token>) {
        var c : Char = advance()

        when(c) {
            '(' -> addToken(tokens, LEFT_PAREN)
            ')' -> addToken(tokens, RIGHT_PAREN)
            '{' -> addToken(tokens, LEFT_BRACE)
            '}' -> addToken(tokens, RIGHT_BRACE)
            ',' -> addToken(tokens, COMMA)
            '.' -> addToken(tokens, DOT)
            '-' -> addToken(tokens, MINUS)
            '+' -> addToken(tokens, PLUS)
            ';' -> addToken(tokens, SEMICOLON)
            '*' -> addToken(tokens, STAR)

            '!' -> addToken(tokens, if (match('=')) BANG_EQUAL else BANG)
            '=' -> addToken(tokens, if (match('=')) EQUAL_EQUAL else EQUAL)
            '<' -> addToken(tokens, if (match('=')) LESS_EQUAL else LESS)
            '>' -> addToken(tokens, if (match('=')) GREATER_EQUAL else GREATER)
            //'' -> addToken(tokens, )

            ' ' -> {}
            '\r' -> {}
            '\t' -> {}

            '\n' -> line++

            '/' -> {
                if (match('/')) {
                    while(peek() != '\n' && !isAtEnd()) {
                        advance()
                    }
                } else {
                    addToken(tokens, SLASH)
                }
            }

            '"' -> {
                string(tokens)
            }

            else -> {
                if(isDigit(c)) {
                    number(tokens)
                } else if (isAlpha(c)) {
                    identifier(tokens)
                } else {
                    KLox.error(line, "Unexpected Character: $c")
                }
            }

        }
    }

    fun identifier(tokens: MutableList<Token>) {
        while(isAlphaNumeric(peek())) {
            advance()
        }

        val text = source.substring(start, current)
        var type = keywords[text]
        if (type == null) type = IDENTIFIER
        addToken(tokens, type)
    }

    fun number(tokens: MutableList<Token>) {
        while(isDigit(peek())) {
            advance()
        }
        if (peek() == '.'  && isDigit(peekNext())) {
            advance()

            while (isDigit(peek())) {
                advance()
            }
        }

        addToken(tokens, NUMBER, source.substring(start, current).toDouble())
    }
    fun string(tokens: MutableList<Token>) {
        while(peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++
            advance()
        }

        if (isAtEnd()) {
            KLox.error(line, "Unterminated String.")
        }

        advance()

        val value = source.substring(start+1, current-1)
        addToken(tokens, STRING, value)
    }

    fun match(expected: Char) : Boolean {
        if (isAtEnd()) return false
        if (source[current] != expected) return false

        current++
        return true
    }

    fun peek() : Char {
        if (isAtEnd()) return 0.toChar()
        return source[current]
    }

    fun peekNext() : Char {
        if (current + 1 >= source.length) return 0.toChar()
        return source[current + 1]
    }

    fun isAtEnd() : Boolean {
        return current >= source.length
    }

    fun advance() : Char {
        return source[current++]
    }

    fun addToken(tokens: MutableList<Token>, type: TokenType) {
        addToken(tokens, type, null)
    }

    fun addToken(tokens: MutableList<Token>, type: TokenType, literal: Any?) {
        val text = source.substring(start, current)
        tokens.add(Token(type, text, literal, line))
    }

    fun isAlpha(c: Char) : Boolean {
        return (c in 'a'..'z' ||
                c in 'A' .. 'Z' ||
                c == '_')
    }

    fun isDigit(c: Char) : Boolean {
        return c in '0'..'9'
    }

    fun isAlphaNumeric(c: Char) : Boolean {
        return isAlpha(c) || isDigit(c)
    }
}