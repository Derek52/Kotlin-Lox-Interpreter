class LoxClass(val name: String, val methods: HashMap<String, LoxFunction>) : LoxCallable{

    fun findMethod(name: String) : LoxFunction? {
        if (methods.containsKey(name)) {
           return methods[name]
        }
        return null
    }

    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val instance = LoxInstance(this)
        return instance
    }

    override fun arity(): Int {
        return 0
    }

    override fun toString() : String {
        return name
    }
}