import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

fun main() {
    val klox = KLox()
    println("Hola")
    klox.main(arrayOf())
}

class KLox {

    @Throws(IOException::class)
    fun main(args: Array<String>) {
        if (args.size > 1) {
            println("usage: Klox [script]")
            exitProcess(64)
        } else if (args.size == 1) {
            runFile(args[0])
        } else {
            runPrompt()
        }
    }

    @Throws(IOException::class)
    fun runFile(path: String) {
        val bytes = Files.readAllBytes(Paths.get(path))
        run(String(bytes, Charset.defaultCharset()))

        if(hadError) {
            exitProcess(65)
        }
    }

    fun runPrompt() {
        val input = InputStreamReader(System.`in`)
        val reader = BufferedReader(input)

        while(true) {
            print("> ")
            reader.readLine()?.let {
                run(it)
                hadError = false
            } ?: break
        }
    }

    fun run(source: String) {
        val scanner = Scanner(source)
        val tokens = scanner.scanTokens()

        for (token in tokens) {
            println(token)
        }
    }

    companion object {
        var hadError = false
        fun error(line: Int, message: String) {
            report(line, "", message)
        }

        fun report(line: Int, where: String, message: String) {
            println("[line $line] Error $where: $message")
            hadError = true
        }
    }
}