package com.github.danielchemko.winmdj.core

import com.github.danielchemko.winmdj.core.mdspec.WinMdObject
import com.github.danielchemko.winmdj.parser.WinMdNavigator
import kotlin.reflect.KClass


private val EXTENSIONS = arrayOf(WinMdObject::class.java)

class MdObjectFacadeManager {
    fun <T : WinMdObject> getFacade(clazz: KClass<T>, navigator: WinMdNavigator): MdObjectFacade<T> {


//        if (!isConcrete(extensionType)) {
//            return Optional.empty()
//        }
//
//        return Optional.of(onDemandTypeCache.computeIfAbsent(extensionType, GeneratorSqlObjectFactory::getOnDemandClass)
//            .invoke { handle -> handle.invokeExact(jdbi) })

//        val proxyClass: Class<*> = Proxy.getProxyClass();

//        Proxy.newProxyInstance(clazz.java.getClassLoader(), clazz.java, buildHandler(clazz))

//        val f: Foo =
//        var c = findConstructor(Class.forName(getGeneratedClassName(clazz::class.java)), *EXTENSIONS)
//        c.invoke()


//        val clazz1 = lookup.accessClass(clazz.java)


        return TODO()
    }

//    private fun <T : WinMdObject> buildHandler(java: KClass<T>): JavaProxyObjectFacade<T> {
//
//
//    }
}

private fun getGeneratedClassName(extensionType: Class<*>): String {
    return extensionType.getPackage().name + "." + extensionType.simpleName + "Impl"
}

interface MdObjectFacade<T : WinMdObject> {
    fun get(assemblyId: Int): T
}

class MethodHandleObjectFacade<T : WinMdObject>(
    clazz: KClass<T>
) : MdObjectFacade<T> {
//    private val handleHolder =
//        findConstructor(Class.forName(getGeneratedClassName(clazz::class.java)), *EXTENSIONS)

    override fun get(rowIndex: Int): T {
//        TODO("Not yet implemented")
//        val holder = handleHolder.invoke(assemblyId)
//        holder.
        return TODO()
    }
}

//class JavaProxyObjectFacade<T : WinMdObject> : MdObjectFacade<T>, InvocationHandler {
//    override fun invoke(proxy: Any?, method: Method?, args: Array<out Any>?): Any {
//
//        TODO("Not yet implemented")
//    }
//
//    override fun get(assemblyId: Long): T {
//        proxyClass.getConstructor(InvocationHandler::class.java).newInstance()
//        return this
//    }
//
//    class WrappedStub
//}
