def defineType(file, className, basename, fieldList)
  if basename == "Stmt"
    file.write("class #{className+basename}(#{fieldList}) : #{basename}() {\n\n")
  else
    file.write("class #{className}(#{fieldList}) : #{basename}() {\n\n")
  end
  file.write("\toverride fun <R> accept(visitor: Visitor<R>) : R {\n")
  file.write("\t\treturn visitor.visit#{className}#{basename}(this)\n")
  file.write("\t}\n}\n\n")
end

def defineVisitor(file, basename, types)
  file.write("\tinterface Visitor<R> {\n")
  types.each do |type|
    info = type.split(';')
    typename = info[0].strip
    if basename == "Stmt"
      file.write("\t\tfun visit#{typename}#{basename}(#{basename.downcase}: #{typename+basename}) : R\n")
    else
      file.write("\t\tfun visit#{typename}#{basename}(#{basename.downcase}: #{typename}) : R\n")
    end
  end
  file.write("\t}\n\n")
end


exprASTDefinitions = [
  "Assign     ; val name: Token, val value: Expr",
  "Binary     ; val left: Expr, val operator: Token, val right: Expr",
  "Call       ; val callee: Expr, val paren: Token, val arguments: List<Expr>",
  "Logical    ; val left: Expr, val operator: Token, val right: Expr",
  "Grouping   ; val expression: Expr",
  "Literal    ; val value: Any?",
  "Unary      ; val operator: Token, val right: Expr",
  "Variable   ; val name: Token"
]

stmtASTDefinitions = [
  "Block      ; val statements: List<Stmt>",
  "Expression ; val expression: Expr",
  "Function   ; val name: Token, val params: List<Token>, val body: List<Stmt>",
  "Var        ; val name: Token, val initializer: Expr?",
  "If         ; val condition: Expr, val thenBranch: Stmt, val elseBranch: Stmt?",
  "Print      ; val expression: Expr",
  "While      ; val condition: Expr, val body: Stmt"
]


def writeASTFile(basename, astDefinitions)
  path = File.expand_path("../../src/main/kotlin/#{basename}.kt")
  File::open(path, mode='w') do |file|
    file.write("abstract class #{basename}() {\n\n")
    defineVisitor(file, basename, astDefinitions)
    file.write("\tabstract fun <R> accept(visitor: Visitor<R>): R\n")
    file.write("}\n\n")

    astDefinitions.each do |n|
      definitions = n.split(';')
      className = definitions[0].strip
      fields = definitions[1].strip
      defineType(file, className, basename, fields)
    end

  end
end

writeASTFile("Expr", exprASTDefinitions)
writeASTFile("Stmt", stmtASTDefinitions)