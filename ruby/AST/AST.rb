$basename = "Expr"

def defineType(file, className, fieldList)
  file.write("class #{className}(#{fieldList}) : #{$basename}() {\n}\n\n")
end

astDefinitions = [
  "Binary    ; left: Expr, operator: Token, right: Expr",
  "Grouping  ; expression: Expr",
  "Literal   ; value: Any?",
  "Unary     ; operator: Token, right: Expr"
]


path = File.expand_path("../../src/main/kotlin/Expr.kt")
File::open(path, mode='w') do |file|
  file.write("abstract class #{$basename}() {\n}\n\n")

  astDefinitions.each do |n|
    definitions = n.split(';')
    className = definitions[0].strip
    fields = definitions[1].strip
    defineType(file, className, fields)
  end

end