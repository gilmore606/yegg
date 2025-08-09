@file:Suppress("NOTHING_TO_INLINE")

package com.dlfsystems.yegg.vm

import com.dlfsystems.yegg.compiler.CodePos
import com.dlfsystems.yegg.compiler.TypeSpec
import com.dlfsystems.yegg.server.mcp.MCP
import com.dlfsystems.yegg.server.Yegg
import com.dlfsystems.yegg.server.mcp.Task
import com.dlfsystems.yegg.util.pop
import com.dlfsystems.yegg.util.push
import com.dlfsystems.yegg.value.Value
import com.dlfsystems.yegg.vm.Opcode.*
import com.dlfsystems.yegg.value.*
import com.dlfsystems.yegg.vm.VMException.Type.*

// A stack machine for one-time execution of an Executable in a Context.

class VM(
    val c: Context,
    val vThis: VObj?,
    val exe: Executable,
    val args: List<Value> = listOf(),
) {
    // Program Counter: index of the opcode we're about to execute (or argument we're about to fetch).
    private var pc: Int = 0

    // The local stack.
    private val stack = ArrayDeque<Value>()
    private val stackLimit = Yegg.world.getSysInt("stackLimit")
    private fun dumpStack() = stack.joinToString(",") { "$it" }

    // Local variables by ID.
    private val variables: MutableMap<Int, Value> = mutableMapOf()

    // Active exception handlers set by O_TRY.
    private data class ErrHandler(
        val errors: Set<VMException.Type>,
        val errVarID: Int,
        val dest: Int,
        val stackDepth: Int
    )
    private val errHandlers = ArrayDeque<ErrHandler>()

    // Preserve error position.
    var pos: CodePos = CodePos(0, 0, 0)
    private fun fail(type: VMException.Type, m: String) { throw VMException(type, m) }

    // Set true on Result.Call return to drop the return value (i.e. don't put it on stack).
    // Used by optimizer opcodes.
    private var dropReturnValue: Boolean = false

    // Value of this execution as an expression. Returned if we have no other return value.
    private var exprValue: Value = VVoid

    private inline fun push(v: Value) = stack.addFirst(v)
    private inline fun peek() = stack.first()
    private inline fun pop() = stack.removeFirst().also {
        if (it.type == Value.Type.VOID) fail(E_VOID, "void in expression")
    }
    private inline fun popTwo() = listOf(pop(), pop())
    private inline fun popThree() = listOf(pop(), pop(), pop())
    private inline fun popFour() = listOf(pop(), pop(), pop(), pop())
    private inline fun next() = exe.code[pc++]

    override fun toString() = "$exe"


    init {
        exe.getInitialVars(args).forEach { (name, v) ->
            setVar(name, v)
        }
        setVar("args", VList.make(args))
        setVar("this", vThis ?: VNull)
        setVar("player", c.vPlayer)
    }

    private fun setVar(name: String, value: Value) {
        exe.symbols[name]?.also { variables[it] = value }
    }


    sealed interface Result {
        @JvmInline value class Return(val v: Value) : Result
        data class Call(val exe: Executable, val args: List<Value>, val vThis: VObj? = null): Result
        @JvmInline value class Suspend(val seconds: Int): Result
    }

    // If passed a vReturn, push it to the stack.
    // Commence execution at current program counter.
    fun execute(vReturn: Value? = null): Result {
        vReturn?.also {
            if (dropReturnValue) dropReturnValue = false
            else push(it)
        }
        while (true) {
            try {
                return executeCode()
            } catch (e: Exception) {
                val err = (e as? VMException ?: VMException(
                        E_SYS, "${e.message} (mem $pc)\n${e.stackTraceToString()}"
                    )).withPos(pos)
                catchError(err) || throw err
            }
        }
    }

    fun catchError(err: VMException): Boolean {
        while (errHandlers.isNotEmpty()) {
            val handler = errHandlers.pop()
            if (handler.errors.isEmpty() || handler.errors.contains(err.type)) {
                val varID = if (handler.errVarID > -1) handler.errVarID else (exe.symbols["it"] ?: -1)
                if (varID > -1) variables[varID] = VErr(err.type, err.m)
                pc = handler.dest
                while (stack.size > handler.stackDepth) pop()
                return true
            }
        }
        return false
    }

    private fun executeCode(): Result {
        while (pc < exe.code.size) {
            if (--c.ticksLeft < 0) fail(E_LIMIT, "tick limit exceeded")
            if (stack.size > stackLimit) fail(E_MAXREC, "stack depth exceeded")

            val word = next()
            pos = word.pos

            when (word.opcode) {

                O_DISCARD -> {
                    if (stack.isNotEmpty()) exprValue = stack.removeFirst()
                }

                // Value ops

                O_VAL -> {
                    push(next().value!!)
                }
                O_LISTVAL -> {
                    val count = next().intFromV
                    val elements = mutableListOf<Value>()
                    repeat(count) { elements.add(0, pop()) }
                    push(VList(elements))
                }
                O_MAPVAL -> {
                    val count = next().intFromV
                    val entries = mutableMapOf<Value, Value>()
                    repeat(count) { entries.put(pop(), pop()) }
                    push(VMap(entries))
                }
                O_FUNVAL -> {
                    val block = next().intFromV
                    // Capture variables from scope
                    val withVars = buildMap {
                        (pop() as VList).v.map { (it as VString).v }.forEach { varName ->
                            exe.symbols[varName]?.also {
                                variables[it]?.also { put(varName, it) }
                            }
                        }
                    }
                    val args = (pop() as VList).v.map { (it as VString).v }
                    push(exe.getLambda(block, c.vThis, args, withVars))
                }
                O_ERRVAL -> {
                    val rawerr = next().value as VErr
                    val m = pop()
                    push(VErr(rawerr.v, m.asString()))
                }

                // Index/range ops

                O_GETI -> {
                    val (index, source) = popTwo()
                    source.getIndex(index)?.also { push(it) }
                        ?: fail(E_TYPE, "cannot index into ${source.type} with ${index.type}")
                }
                O_GETRANGE -> {
                    val (end, start, source) = popThree()
                    source.getRange(start, end)?.also { push(it) }
                        ?: fail(E_TYPE, "cannot range into ${source.type} with ${start.type}..${end.type}")
                }
                O_SETI -> {
                    val (index, target, v) = popThree()
                    if (!target.setIndex(index, v)) fail(E_RANGE, "cannot index into ${target.type} with ${index.type}")
                }
                O_SETRANGE -> {
                    val (end, start, target, v) = popFour()
                    if (!target.setRange(start, end, v)) fail(E_RANGE, "cannot range into ${target.type} with ${target.type}..${start.type}")
                }

                // Control flow ops

                O_IF -> {
                    val elseAddr = next().address!!
                    val condition = pop()
                    if (condition.isFalse()) pc = elseAddr
                }
                O_IFNON -> {
                    val elseAddr = next().address!!
                    if (peek().isNull()) pop() else pc = elseAddr
                }
                O_IFNULL -> {
                    val elseAddr = next().address!!
                    if (peek().isNull()) pc = elseAddr
                }
                O_IFVAREQ -> {
                    val varID = next().intFromV
                    val elseAddr = next().address!!
                    if (variables[varID] != pop()) pc = elseAddr
                }
                O_JUMP -> {
                    val addr = next().address!!
                    // Unresolved jump dest means end-of-code
                    if (addr >= 0) pc = addr else return Result.Return(exprValue)
                }
                O_RETURN -> {
                    if (stack.isEmpty()) return Result.Return(exprValue)
                    if (stack.size > 1) fail(E_SYS, "stack polluted on return!  ${dumpStack()}")
                    return Result.Return(pop())
                }
                O_RETURNNULL -> {
                    if (stack.isNotEmpty()) fail(E_SYS, "stack polluted on returnnull!  ${dumpStack()}")
                    return Result.Return(VVoid)
                }
                O_RETVAL -> {
                    return Result.Return(next().value!!)
                }
                O_RETVAR -> {
                    return Result.Return(variables[next().intFromV]!!)
                }
                O_THROW -> {
                    val err = pop()
                    if (err is VErr) fail(err.v, err.m ?: "")
                    else fail(E_USER, err.asString())
                }
                O_TRY -> {
                    val errCount = next().intFromV
                    val errs = buildSet { repeat(errCount) {
                        (pop() as? VErr)?.also { add(it.v) } ?: fail(E_TYPE, "cannot catch non-ERR")
                    } }
                    val varID = next().intFromV
                    val irq = next().address!!
                    errHandlers.push(ErrHandler(errs, varID, irq, stack.size))
                }
                O_TRYEND -> {
                    errHandlers.pop()
                }

                // Verb ops

                O_CALL, O_VCALL, O_VCALLST -> {
                    val argCount = next().intFromV
                    val name = (if (word.opcode == O_CALL) pop() else next().value!!).asString()
                    val args = buildList { repeat(argCount) { add(0, pop()) } }
                    val subj = pop()
                    // If static built-in verb, call directly without returning
                    subj.callStaticVerb(c, name, args)?.also {
                        if (word.opcode != O_VCALLST) push(it)
                    } ?: subj.getVerb(name)?.also { verb ->
                        if (word.opcode == O_VCALLST) dropReturnValue = true
                        return Result.Call(verb, args, subj as? VObj)
                    } ?: fail(E_VERBNF, "no such verb $name")
                }
                O_FUNCALL, O_FUNCALLST -> {
                    val name = (next().value as VString).v
                    val argCount = next().intFromV
                    val args = buildList { repeat(argCount) { add(0, pop()) } }
                    // If static sys function, call directly without returning
                    Yegg.world.sys.callStaticVerb(c, name, args)?.also {
                        if (word.opcode != O_FUNCALLST) push(it)
                    } ?: exe.symbols[name]?.also { varID ->
                        variables[varID]?.let { value ->
                            if (value is Executable) {
                                return Result.Call(value, args)
                            } else fail(E_TYPE, "cannot invoke ${value.type} as fun")
                        }
                    } ?: fail(E_VARNF, "no such fun $name")
                }
                O_PASS, O_PASSST -> {
                    val argCount = next().intFromV
                    val args = buildList { repeat(argCount) { add(0, pop()) } }
                    if (word.opcode == O_PASSST) dropReturnValue = true
                    exe.getPassExe()?.also { return Result.Call(it, args, vThis) }
                        ?: fail(E_VERBNF, "verb not found")
                }

                // Task ops

                O_SUSPEND -> {
                    val sec = pop()
                    return Result.Suspend((sec as VInt).v)
                }
                O_FORK -> {
                    val (exe, sec) = popTwo()
                    val task = Task.make(
                        connection = c.connection,
                        exe = exe as VFun,
                        vThis = c.vThis,
                        vPlayer = c.vPlayer,
                    )
                    MCP.schedule(task, (sec as VInt).v)
                    push(task.vID)
                }
                O_READLINE, O_READLINES -> {
                    c.taskID?.also { taskID ->
                        c.connection?.also { conn ->
                            conn.requestReadLines(taskID, word.opcode == O_READLINE)
                            return Result.Suspend(Int.MAX_VALUE)
                        }
                    }
                    fail(E_SYS, "cannot readline in headless task")
                }

                // Variable ops

                O_GETVAR -> {
                    val varID = next().intFromV
                    variables[varID]?.also { push(it) }
                        ?: fail(E_VARNF, "variable not found")
                }
                O_SETVAR -> {
                    val varID = next().intFromV
                    val v = pop()
                    variables[varID] = v
                }
                O_SETGETVAR -> {
                    variables[next().intFromV] = peek()
                }
                O_INCVAR, O_DECVAR -> {
                    val varID = next().intFromV
                    variables[varID]?.also {
                        if (it is VInt)
                            variables[varID] = VInt(it.v + if (word.opcode == O_INCVAR) 1 else -1)
                        else fail(E_TYPE, "cannot increment ${it.type}")
                    } ?: fail(E_VARNF, "variable not found")
                }
                O_DESTRUCT -> {
                    val (source, types, vars) = popThree()
                    if (source !is VList) fail(E_TYPE, "cannot destructure from non-list")
                    if ((source as VList).v.size < (vars as VList).v.size) fail(E_RANGE, "missing args")
                    vars.v.forEachIndexed { i, vn ->
                        val s = source.v[i]
                        val type = TypeSpec.fromVInt((types as VList).v[i] as VInt)
                        if (!type.matches(s)) fail(E_TYPE, "${s.type} is not $type")
                        setVar((vn as VString).v, s)
                    }
                }

                // Iterator ops

                O_ITERSIZE -> {
                    val subj = pop()
                    subj.iterableSize()?.also {
                        push(VInt(it))
                    } ?: fail(E_TYPE, "cannot iterate ${subj.type}")
                }
                O_ITERPICK -> {
                    val sourceID = next().intFromV
                    val indexID = next().intFromV
                    variables[sourceID]!!.getIndex(variables[indexID] as VInt)?.also { push(it) }
                        ?: fail(E_TYPE, "cannot iterate")
                }

                // Property ops

                O_GETPROP, O_VGETPROP -> {
                    val name = if (word.opcode == O_VGETPROP) next().value!! else pop()
                    val source = pop()
                    if (name is VString) {
                        source.getProp(name.v)?.also { push(it) } ?: fail(E_PROPNF, "property not found")
                    } else fail(E_PROPNF, "property name must be string")
                }
                O_SETPROP -> {
                    val (name, target, v) = popThree()
                    if (!target.setProp((name as VString).v, v)) fail(E_PROPNF, "property not found")
                }
                O_TRAIT, O_VTRAIT -> {
                    val name = if (word.opcode == O_VTRAIT) next().value!! else pop()
                    if (name is VString) {
                        Yegg.world.getTrait(name.v)?.also { push(it.vTrait) }
                            ?: fail (E_TRAITNF, "no such trait $name")
                    } else fail(E_TRAITNF, "trait name must be string")
                }

                // Boolean ops

                O_NEGATE -> {
                    val v = pop()
                    v.negate()?.also { push(it) } ?: fail(E_TYPE, "cannot negate ${v.type}")
                }
                O_IN -> {
                    val (source, v) = popTwo()
                    v.isIn(source)?.also { push(VBool(it)) }
                        ?: fail(E_TYPE, "cannot check ${v.type} in ${source.type}")
                }
                O_ISTRAIT -> {
                    val (v, target) = popTwo()
                    if (v.type != Value.Type.TRAIT) fail(E_TYPE, "${v.type} is not TRAIT")
                    if (target.type != Value.Type.OBJ) fail(E_TYPE, "${target.type} cannot have traits")
                    (v as VTrait).trait()?.also { trait ->
                        (target as VObj).obj()?.also { obj ->
                            push(VBool(obj.inheritsTrait(trait.id)))
                        } ?: run { push(Yegg.vFalse) }
                    } ?: run { push(Yegg.vFalse) }
                }
                O_ISTYPE -> {
                    val target = pop()
                    val typeID = next().value!!
                    push(VBool(target.type == Value.Type.entries[(typeID as VInt).v]))
                }
                O_CMP_EQ, O_CMP_GT, O_CMP_GE, O_CMP_LT, O_CMP_LE -> {
                    val (a2, a1) = popTwo()
                    when (word.opcode) {
                        O_CMP_EQ -> push(VBool(a1.cmpEq(a2)))
                        O_CMP_GT -> push(VBool(a1.cmpGt(a2)))
                        O_CMP_GE -> push(VBool(a1.cmpGe(a2)))
                        O_CMP_LT -> push(VBool(a1.cmpLt(a2)))
                        O_CMP_LE -> push(VBool(a1.cmpLe(a2)))
                        else -> { }
                    }
                }
                O_CMP_EQZ -> { push(VBool(pop().cmpEq(Yegg.vZero))) }
                O_CMP_NEZ -> { push(VBool(!pop().cmpEq(Yegg.vZero))) }
                O_CMP_GTZ -> { push(VBool(pop().cmpGt(Yegg.vZero))) }
                O_CMP_GEZ -> { push(VBool(pop().cmpGe(Yegg.vZero))) }
                O_CMP_LTZ -> { push(VBool(pop().cmpLt(Yegg.vZero))) }
                O_CMP_LEZ -> { push(VBool(pop().cmpLe(Yegg.vZero))) }

                // Math ops

                O_ADD -> {
                    val (a2, a1) = popTwo()
                    a1.plus(a2)?.also { push(it) }
                        ?: fail(E_TYPE, "cannot add ${a1.type} and ${a2.type}")
                }
                O_ADDVAL -> {
                    val a1 = pop()
                    val a2 = next().value!!
                    a1.plus(a2)?.also { push(it) }
                        ?: fail(E_TYPE, "cannot add ${a1.type} and ${a2.type}")
                }
                O_CONCAT -> {
                    val (a2, a1) = popTwo()
                    val a3 = next().value!!
                    a1.plus(a2)?.also { a12 ->
                        a12.plus(a3)?.also { push(it) }
                            ?: fail(E_TYPE, "cannot add ${a12.type} and ${a3.type}")
                    } ?: fail(E_TYPE, "cannot add ${a1.type} and ${a2.type}")
                }
                O_MULT -> {
                    val (a2, a1) = popTwo()
                    a1.multiply(a2)?.also { push(it) }
                        ?: fail(E_TYPE, "cannot multiply ${a1.type} and ${a2.type}")
                }
                O_DIV -> {
                    val (a2, a1) = popTwo()
                    if (a2.isZero() || a1.isZero()) fail(E_DIV, "divide by zero")
                    a1.divide(a2)?.also { push(it) }
                        ?: fail(E_TYPE, "cannot divide ${a1.type} and ${a2.type}")
                }
                O_POWER -> {
                    val (a2, a1) = popTwo()
                    a1.toPower(a2)?.also { push(it) }
                        ?: fail(E_TYPE, "cannot raise ${a1.type} to power ${a2.type}")
                }
                O_MODULUS -> {
                    val (a2, a1) = popTwo()
                    a1.modulo(a2)?.also { push(it) }
                        ?: fail(E_TYPE, "cannot modulo ${a1.type} by ${a2.type}")
                }

                else -> fail(E_SYS, "unknown opcode $word")
            }
        }
        return Result.Return(if (stack.isEmpty()) exprValue else pop())
    }

}
