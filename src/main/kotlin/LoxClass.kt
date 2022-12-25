class LoxClass(val name: String, val superClass: LoxClass?,
               val methods: HashMap<String, LoxFunction>) : LoxCallable{

    fun findMethod(name: String) : LoxFunction? {
        if (methods.containsKey(name)) {
           return methods[name]
        }
        superClass?.let {
            return superClass.findMethod(name)
        }
        return null
    }

    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val instance = LoxInstance(this)
        val initializer = findMethod("init")
        initializer?.let {
            initializer.bind(instance).call(interpreter, arguments)
        }

        return instance
    }

    override fun arity(): Int {
        val initializer = findMethod("init")
        initializer?.let { init ->
            return init.arity()
        }
        return 0
    }

    override fun toString() : String {
        return name
    }
}