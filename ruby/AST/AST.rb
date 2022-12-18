def defineType(file, className, basename, fieldList)
  file.write("class #{className}(#{fieldList}) : #{basename}() {\n\n")
  file.write("\toverride fun <R> accept(visitor: Visitor<R>) : R {\n")
  file.write("\t\treturn visitor.visit#{className}#{basename}(this)\n")
  file.write("\t}\n}\n\n")
end

def defineVisitor(file, basename, types)
  file.write("\tinterface Visitor<R> {\n")
  types.each do |type|
    info = type.split(';')
    typename = info[0].strip
    file.write("\t\tfun visit#{typename}#{basename}(#{basename.downcase}: #{typename}) : R\n")
  end
  file.write("\t}\n\n")
end

exprASTDefinitions = [
  "Binary     ; val left: Expr, val operator: Token, val right: Expr",
  "Grouping   ; val expression: Expr",
  "Literal    ; val value: Any?",
  "Unary      ; val operator: Token, val right: Expr"
]

stmtASTDefinitions = [
  "Expression ; val expression: Expr",
  "Print      ; val expression: Expr"
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

exit