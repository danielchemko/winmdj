//package com.github.danielchemko.winmdj.util
//
//import com.sun.javafx.binding.BidirectionalBinding
//import com.sun.javafx.binding.BidirectionalBinding.*
//import com.sun.javafx.binding.BindingHelperObserver
//import com.sun.javafx.binding.ExpressionHelper
//import javafx.beans.InvalidationListener
//import javafx.beans.Observable
//import javafx.beans.WeakInvalidationListener
//import javafx.beans.binding.*
//import javafx.beans.property.*
//import javafx.beans.value.ChangeListener
//import javafx.beans.value.ObservableNumberValue
//import javafx.beans.value.ObservableValue
//import javafx.beans.value.WritableNumberValue
//import javafx.collections.FXCollections
//import javafx.collections.ObservableList
//import java.lang.ref.WeakReference
//import java.util.*
//
//open class SimpleShortProperty : ShortPropertyBase {
//    val bean: Any
//    val name: String
//
//    constructor(var1: Short) : this(DEFAULT_BEAN!!, "", var1)
//
//    @JvmOverloads
//    constructor(var1: Any? = DEFAULT_BEAN, var2: String? = "") {
//        this.bean = var1!!
//        this.name = var2 ?: ""
//    }
//
//    constructor(var1: Any, var2: String?, var3: Short) : super(var3) {
//        this.bean = var1
//        this.name = var2 ?: ""
//    }
//
//    companion object {
//        private val DEFAULT_BEAN: Any? = null
//        private const val DEFAULT_NAME = ""
//    }
//}
//
//abstract class ShortPropertyBase : ShortProperty {
//    private var value: Short = 0
//    private var observable: ObservableShortValue? = null
//    private var listener: InvalidationListener? = null
//    private var valid = true
//    private var helper: ExpressionHelper<Number>? = null
//
//    constructor()
//
//    constructor(var1: Short) {
//        this.value = var1
//    }
//
//    override fun addListener(var1: InvalidationListener) {
//        this.helper = ExpressionHelper.addListener<Number>(this.helper, this, var1)
//    }
//
//    override fun removeListener(var1: InvalidationListener) {
//        this.helper = ExpressionHelper.removeListener<Number>(this.helper, var1)
//    }
//
//    override fun addListener(var1: javafx.beans.value.ChangeListener<in Number>) {
//        this.helper = ExpressionHelper.addListener<Number>(this.helper, this, var1)
//    }
//
//    override fun removeListener(var1: javafx.beans.value.ChangeListener<in Number>) {
//        this.helper = ExpressionHelper.removeListener<Number>(this.helper, var1)
//    }
//
//    protected open fun fireValueChangedEvent() {
//        ExpressionHelper.fireValueChangedEvent<Number>(this.helper)
//    }
//
//    private fun markInvalid() {
//        if (this.valid) {
//            this.valid = false
//            this.invalidated()
//            this.fireValueChangedEvent()
//        }
//    }
//
//    protected open fun invalidated() {
//    }
//
//    override fun get(): Short {
//        this.valid = true
//        return if (this.observable == null) this.value else observable.get()
//    }
//
//    override fun set(var1: Short) {
//        if (!this.isBound) {
//            if (this.value != var1) {
//                this.value = var1
//                this.markInvalid()
//            }
//        } else {
//            val var10002 =
//                if (this.getBean() != null && this.getName() != null) this.getBean().javaClass.getSimpleName() + "." + this.getName() + " : " else ""
//            throw RuntimeException(var10002 + "A bound value cannot be set.")
//        }
//    }
//
//    val isBound: Boolean
//        get() = this.observable != null
//
//    override fun bind(var1: ObservableValue<out Number>) {
//        if (var1 == null) {
//            throw NullPointerException("Cannot bind to null")
//        } else {
//            val var2: Any
//            if (var1 is ObservableShortValue) {
//                var2 = var1 as ObservableShortValue
//            } else if (var1 is ObservableNumberValue) {
//                val var3: ObservableNumberValue = var1 as ObservableNumberValue
//                var2 = object : ValueWrapper(var1) {
//                    protected override fun computeValue(): Short {
//                        return var3.value.toShort()
//                    }
//
//                    override fun getValue(): Number {
//                        return computeValue()
//                    }
//                }
//            } else {
//                var2 = object : ValueWrapper(var1) {
//                    protected override fun computeValue(): Short {
//                        val var1x = var1.getValue() as Number?
//                        return var1x?.toShort() ?: 0
//                    }
//
//                    override fun getValue(): Number {
//                        return computeValue()
//                    }
//                }
//            }
//
//            if (var2 as ObservableShortValue != observable) {
//                this.unbind()
//                this.observable = var2 as ObservableShortValue
//                if (this.listener == null) {
//                    this.listener = Listener(this)
//                }
//
//                observable.addListener(this.listener)
//                this.markInvalid()
//            }
//        }
//    }
//
//    override fun unbind() {
//        if (this.observable != null) {
//            this.value = observable.get()
//            observable.removeListener(this.listener)
//            if (observable is ValueWrapper) {
//                (observable as ValueWrapper?)!!.dispose()
//            }
//
//            this.observable = null
//        }
//    }
//
//    override fun toString(): String {
//        val var1: Any = this.getBean()
//        val var2: String = this.getName()
//        val var3 = StringBuilder("IntegerProperty [")
//        if (var1 != null) {
//            var3.append("bean: ").append(var1).append(", ")
//        }
//
//        if (var2 != null && var2 != "") {
//            var3.append("name: ").append(var2).append(", ")
//        }
//
//        if (this.isBound) {
//            var3.append("bound, ")
//            if (this.valid) {
//                var3.append("value: ").append(this.get())
//            } else {
//                var3.append("invalid")
//            }
//        } else {
//            var3.append("value: ").append(this.get())
//        }
//
//        var3.append("]")
//        return var3.toString()
//    }
//
//    private class Listener(var1: IntegerPropertyBase?) : InvalidationListener, WeakListener {
//        private val wref: WeakReference<IntegerPropertyBase?>
//
//        init {
//            this.wref = WeakReference<Any?>(var1)
//        }
//
//        override fun invalidated(var1: javafx.beans.Observable) {
//            val var2 = wref.get()
//            if (var2 == null) {
//                var1.removeListener(this)
//            } else {
//                var2.markInvalid()
//            }
//        }
//
//        override fun wasGarbageCollected(): Boolean {
//            return wref.get() == null
//        }
//    }
//
//    private abstract inner class ValueWrapper(var2: ObservableValue<out Number>) : ShortBinding() {
//        private val observable: ObservableValue<out Number> = var2
//
//        init {
//            this.bind(*arrayOf<javafx.beans.Observable>(var2))
//        }
//
//        override fun dispose() {
//            this.unbind(*arrayOf<javafx.beans.Observable>(this.observable))
//        }
//    }
//}
//
//abstract class ShortProperty : ReadOnlyShortProperty(), javafx.beans.property.Property<Number?>,
//    WritableShortValue {
//    override fun setValue(var1: Number) {
//        if (var1 == null) {
//            com.sun.javafx.binding.Logging.getLogger().fine(
//                "Attempt to set integer property to null, using default value instead.",
//                java.lang.NullPointerException()
//            )
//            this.set(0)
//        } else {
//            this.set(var1.toInt())
//        }
//    }
//
//    fun bindBidirectional(var1: javafx.beans.property.Property<Number>) {
//        javafx.beans.binding.Bindings.bindBidirectional<Number>(this, var1)
//    }
//
//    fun unbindBidirectional(var1: javafx.beans.property.Property<Number>) {
//        javafx.beans.binding.Bindings.unbindBidirectional<Number>(this, var1)
//    }
//
//    override fun toString(): String {
//        val var1: Any = this.getBean()
//        val var2: String = this.getName()
//        val var3 = java.lang.StringBuilder("IntegerProperty [")
//        if (var1 != null) {
//            var3.append("bean: ").append(var1).append(", ")
//        }
//
//        if (var2 != null && var2 != "") {
//            var3.append("name: ").append(var2).append(", ")
//        }
//
//        var3.append("value: ").append(this.get()).append("]")
//        return var3.toString()
//    }
//
//    override fun asObject(): ObjectProperty<Short> {
//        return object : ObjectPropertyBase<Short?>() {
//            init {
//                BidirectionalBinding.bindNumber(this, this@ShortProperty)
//            }
//
//            val bean: Any
//                get() = null
//
//            val name: String
//                get() = this@ShortProperty.getName()
//        }
//    }
//
//    companion object {
//        fun shortProperty(var0: javafx.beans.property.Property<Short?>): ShortProperty {
//            Objects.requireNonNull(var0, "Property cannot be null")
//            return object : ShortPropertyBase() {
//                init {
//                    BidirectionalBinding.bindNumber(this, var0)
//                }
//            }
//        }
//
//        fun bindNumber(var0: Property<Short?>?, var1: ShortProperty?): BidirectionalBinding {
//            return bindNumber<Number>(var0 as Property<*>?, var1 as Property<*>?)
//        }
//
//        private fun <T : Number?> bindNumber(var0: Property<T?>, var1: Property<Number>): BidirectionalBinding {
//            checkParameters(var0, var1)
//            val var2: TypedNumberBidirectionalBinding<*> = TypedNumberBidirectionalBinding<Any?>(var0, var1)
//            var0.setValue(var1.value)
//            var0.value
//            var0.addListener(var2)
//            var1.addListener(var2)
//            return var2
//        }
//
//        private class TypedNumberBidirectionalBinding<T : Number?> private constructor(
//            var1: Property<T>,
//            var2: Property<Number>
//        ) : BidirectionalBinding(var1, var2) {
//            private val propertyRef1: WeakReference<Property<T>?>
//            private val propertyRef2: WeakReference<Property<Number>?>
//            private var oldValue: T
//            private var updating = false
//
//            init {
//                this.oldValue = var1.value as Number
//                this.propertyRef1 = WeakReference<Any?>(var1)
//                this.propertyRef2 = WeakReference<Any?>(var2)
//            }
//
//            override fun getProperty1(): Property<T>? {
//                return propertyRef1.get()
//            }
//
//            override fun getProperty2(): Property<Number>? {
//                return propertyRef2.get()
//            }
//
//            override fun invalidated(var1: Observable) {
//                if (!this.updating) {
//                    val var2 = propertyRef1.get() as Property<Number?>?
//                    val var3 = propertyRef2.get() as Property<T?>?
//                    if (var2 != null && var3 != null) {
//                        try {
//                            this.updating = true
//                            val var4: Number
//                            if (var2 === var1) {
//                                var4 = var2.value as Number
//                                var3.setValue(var4)
//                                var3.value
//                                this.oldValue = var4
//                            } else {
//                                var4 = var3.value as Number
//                                var2.setValue(var4)
//                                var2.value
//                                this.oldValue = var4
//                            }
//                        } catch (var11: java.lang.RuntimeException) {
//                            try {
//                                if (var2 === var1) {
//                                    var2.setValue(this.oldValue)
//                                    var2.value
//                                } else {
//                                    var3.setValue(this.oldValue)
//                                    var3.value
//                                }
//                            } catch (var10: Exception) {
//                                var10.addSuppressed(var11)
//                                unbind<Any>(var2, var3)
//                                throw java.lang.RuntimeException(
//                                    "Bidirectional binding failed together with an attempt to restore the source property to the previous value. Removing the bidirectional binding from properties $var2 and $var3",
//                                    var10
//                                )
//                            }
//
//                            throw java.lang.RuntimeException(
//                                "Bidirectional binding failed, setting to the previous value",
//                                var11
//                            )
//                        } finally {
//                            this.updating = false
//                        }
//                    } else {
//                        var2?.removeListener(this)
//
//                        var3?.removeListener(this)
//                    }
//                }
//            }
//        }
//    }
//}
//
//abstract class ReadOnlyShortPropertyBase : ReadOnlyShortProperty() {
//    var helper: ExpressionHelper<Number?>? = null
//
//    override fun addListener(var1: InvalidationListener) {
//        this.helper = ExpressionHelper.addListener(this.helper, this, var1)
//    }
//
//    override fun removeListener(var1: InvalidationListener) {
//        this.helper = ExpressionHelper.removeListener(this.helper, var1)
//    }
//
//    override fun addListener(var1: ChangeListener<in Number?>) {
//        this.helper = ExpressionHelper.addListener(this.helper, this, var1)
//    }
//
//    override fun removeListener(var1: ChangeListener<in Number?>) {
//        this.helper = ExpressionHelper.removeListener(this.helper, var1)
//    }
//
//    protected open fun fireValueChangedEvent() {
//        ExpressionHelper.fireValueChangedEvent(this.helper)
//    }
//}
//
//abstract class ReadOnlyShortProperty : ShortExpression(), javafx.beans.property.ReadOnlyProperty<Number?> {
//    override fun toString(): String {
//        val var1: Any = this.getBean()
//        val var2: String = this.getName()
//        val var3 = java.lang.StringBuilder("ReadOnlyIntegerProperty [")
//        if (var1 != null) {
//            var3.append("bean: ").append(var1).append(", ")
//        }
//
//        if (var2 != null && var2 != "") {
//            var3.append("name: ").append(var2).append(", ")
//        }
//
//        var3.append("value: ").append(this.get()).append("]")
//        return var3.toString()
//    }
//
//    override fun asObject(): ReadOnlyObjectProperty<Short> {
//        return object : ReadOnlyObjectPropertyBase<Short>() {
//            private var valid = true
//            private val listener: InvalidationListener = InvalidationListener { var1x: javafx.beans.Observable? ->
//                if (this.valid) {
//                    this.valid = false
//                    this.fireValueChangedEvent()
//                }
//            }
//
//            init {
//                this@ReadOnlyShortProperty.addListener(WeakInvalidationListener(this.listener))
//            }
//
//            override fun get(): Short {
//                this.valid = true
//                return this@ReadOnlyShortProperty.getValue() as Short
//            }
//
//            override fun getBean(): Any {
//                return this@ReadOnlyShortProperty.bean
//            }
//
//            override fun getName(): String {
//                return this@ReadOnlyShortProperty.name
//            }
//        }
//    }
//
//    companion object {
//        fun <T : Short?> readOnlyShortProperty(var0: javafx.beans.property.ReadOnlyProperty<T>?): ReadOnlyShortProperty {
//            if (var0 == null) {
//                throw java.lang.NullPointerException("Property cannot be null")
//            } else {
//                return (if (var0 is ReadOnlyShortProperty) var0 else object : ReadOnlyShortPropertyBase() {
//                    private var valid = true
//                    private val listener: InvalidationListener =
//                        InvalidationListener { var1x: javafx.beans.Observable? ->
//                            if (this.valid) {
//                                this.valid = false
//                                this.fireValueChangedEvent()
//                            }
//                        }
//
//                    init {
//                        var0.addListener(WeakInvalidationListener(this.listener))
//                    }
//
//                    override fun getValue(): Number {
//                        return get()
//                    }
//
//                    override fun get(): Short {
//                        this.valid = true
//                        val var1 = var0.getValue() as Number?
//                        return var1?.toShort() ?: 0
//                    }
//
//                    override fun getBean(): Any {
//                        return bean!!
//                    }
//
//                    override fun getName(): String {
//                        return var0.getName()
//                    }
//
//                    var bean: Any? = null
//                })
//            }
//        }
//    }
//}
//
//abstract class ShortExpression : NumberExpressionBase(), ObservableShortValue {
//    fun shortValue(): Short {
//        return this.get()
//    }
//
//    override fun intValue(): Int {
//        return this.get().toInt()
//    }
//
//    override fun longValue(): Long {
//        return this.get().toLong()
//    }
//
//    override fun floatValue(): Float {
//        return this.get().toFloat()
//    }
//
//    override fun doubleValue(): Double {
//        return this.get().toDouble()
//    }
//
//    var value: Short? = null
//
//    override fun negate(): ShortBinding {
//        return javafx.beans.binding.Bindings.negate(this) as ShortBinding
//    }
//
//    override fun add(var1: Double): DoubleBinding {
//        return javafx.beans.binding.Bindings.add(this, var1)
//    }
//
//    override fun add(var1: Float): FloatBinding {
//        return javafx.beans.binding.Bindings.add(this, var1) as FloatBinding
//    }
//
//    override fun add(var1: Long): LongBinding {
//        return javafx.beans.binding.Bindings.add(this, var1) as LongBinding
//    }
//
//    override fun add(var1: Int): ShortBinding {
//        return javafx.beans.binding.Bindings.add(this, var1) as ShortBinding
//    }
//
//    override fun subtract(var1: Double): DoubleBinding {
//        return javafx.beans.binding.Bindings.subtract(this, var1)
//    }
//
//    override fun subtract(var1: Float): FloatBinding {
//        return javafx.beans.binding.Bindings.subtract(this, var1) as FloatBinding
//    }
//
//    override fun subtract(var1: Long): LongBinding {
//        return javafx.beans.binding.Bindings.subtract(this, var1) as LongBinding
//    }
//
//    override fun subtract(var1: Int): ShortBinding {
//        return javafx.beans.binding.Bindings.subtract(this, var1) as ShortBinding
//    }
//
//    override fun multiply(var1: Double): DoubleBinding {
//        return javafx.beans.binding.Bindings.multiply(this, var1)
//    }
//
//    override fun multiply(var1: Float): FloatBinding {
//        return javafx.beans.binding.Bindings.multiply(this, var1) as FloatBinding
//    }
//
//    override fun multiply(var1: Long): LongBinding {
//        return javafx.beans.binding.Bindings.multiply(this, var1) as LongBinding
//    }
//
//    override fun multiply(var1: Int): ShortBinding {
//        return javafx.beans.binding.Bindings.multiply(this, var1) as ShortBinding
//    }
//
//    override fun divide(var1: Double): DoubleBinding {
//        return javafx.beans.binding.Bindings.divide(this, var1)
//    }
//
//    override fun divide(var1: Float): FloatBinding {
//        return javafx.beans.binding.Bindings.divide(this, var1) as FloatBinding
//    }
//
//    override fun divide(var1: Long): LongBinding {
//        return javafx.beans.binding.Bindings.divide(this, var1) as LongBinding
//    }
//
//    override fun divide(var1: Int): ShortBinding {
//        return javafx.beans.binding.Bindings.divide(this, var1) as ShortBinding
//    }
//
//    open fun asObject(): ObjectExpression<Short?>? {
//        return object : ObjectBinding<Short?>() {
//            init {
//                this.bind(*arrayOf<javafx.beans.Observable>(this@ShortExpression))
//            }
//
//            override fun dispose() {
//                this.unbind(*arrayOf<javafx.beans.Observable>(this@ShortExpression))
//            }
//
//            protected override fun computeValue(): Short {
//                return this@ShortExpression.value ?: 0
//            }
//        }
//    }
//
//    companion object {
//        fun shortExpression(var0: ObservableShortValue?): ShortExpression? {
//            if (var0 == null) {
//                throw java.lang.NullPointerException("Value must be specified.")
//            } else {
//                return (if (var0 is ShortExpression) var0 else object : ShortBinding() {
//                    init {
//                        super.bind(*arrayOf<javafx.beans.Observable>(var0))
//                    }
//
//                    override fun dispose() {
//                        super.unbind(*arrayOf<javafx.beans.Observable>(var0))
//                    }
//
//                    protected override fun computeValue(): Short {
//                        return var0.get()
//                    }
//
//                    override fun getValue(): Number {
//                        TODO("Not yet implemented")
//                    }
//
//                    val dependencies: ObservableList<ObservableShortValue>
//                        get() = FXCollections.singletonObservableList<ObservableShortValue>(var0)
//                }) as ShortExpression?
//            }
//        }
//
//        fun <T : Number?> integerExpression(var0: ObservableValue<T>?): ShortExpression? {
//            if (var0 == null) {
//                throw java.lang.NullPointerException("Value must be specified.")
//            } else {
//                return (if (var0 is ShortExpression) var0 else object : ShortBinding() {
//                    init {
//                        super.bind(*arrayOf<javafx.beans.Observable>(var0))
//                    }
//
//                    override fun dispose() {
//                        super.unbind(*arrayOf<javafx.beans.Observable>(var0))
//                    }
//
//                    protected override fun computeValue(): Short {
//                        val var1 = var0.getValue() as Number?
//                        return var1?.toShort() ?: 0
//                    }
//
//                    override fun getValue(): Number {
//                        TODO("Not yet implemented")
//                    }
//
//                    val dependencies: ObservableList<ObservableValue<T>>
//                        get() = FXCollections.singletonObservableList<ObservableValue<T>>(var0)
//                }) as ShortExpression?
//            }
//        }
//    }
//}
//
//abstract class ShortBinding : ShortExpression(), NumberBinding {
//    private var valid = false
//    private var observer: BindingHelperObserver? = null
//    private var helper: ExpressionHelper<Number>? = null
//
//    override fun addListener(var1: InvalidationListener) {
//        this.helper = ExpressionHelper.addListener(this.helper, this, var1)
//    }
//
//    override fun removeListener(var1: InvalidationListener) {
//        this.helper = ExpressionHelper.removeListener(this.helper, var1)
//    }
//
//    override fun addListener(var1: ChangeListener<in Number>) {
//        this.helper = ExpressionHelper.addListener(this.helper, this, var1)
//    }
//
//    override fun removeListener(var1: ChangeListener<in Number>) {
//        this.helper = ExpressionHelper.removeListener(this.helper, var1)
//    }
//
//    protected fun bind(vararg var1: Observable) {
//        if (var1 != null && var1.size > 0) {
//            if (this.observer == null) {
//                this.observer = BindingHelperObserver(this)
//            }
//
//            val var2: Array<out Observable> = var1
//            val var3 = var1.size
//
//            for (var4 in 0 until var3) {
//                val var5 = var2[var4]
//                var5.addListener(this.observer)
//            }
//        }
//    }
//
//    protected fun unbind(vararg var1: Observable) {
//        if (this.observer != null) {
//            val var2: Array<out Observable> = var1
//            val var3 = var1.size
//
//            for (var4 in 0 until var3) {
//                val var5 = var2[var4]
//                var5.removeListener(this.observer)
//            }
//        }
//    }
//
//    override fun dispose() {
//    }
//
//    override fun getDependencies(): ObservableList<*> {
//        return FXCollections.emptyObservableList<Any>()
//    }
//
//    override fun get(): Short {
//        if (!this.valid) {
//            this.value = this.computeValue()
//            this.valid = true
//        }
//
//        return this.value ?: 0
//    }
//
//    protected open fun onInvalidating() {
//    }
//
//    override fun invalidate() {
//        if (this.valid) {
//            this.valid = false
//            this.onInvalidating()
//            ExpressionHelper.fireValueChangedEvent(this.helper)
//        }
//    }
//
//    override fun isValid(): Boolean {
//        return this.valid
//    }
//
//    protected abstract fun computeValue(): Short
//
//    override fun toString(): String {
//        return if (this.valid) "ShortBinding [value: " + this.get() + "]" else "ShortBinding [invalid]"
//    }
//}
//
//interface ObservableShortValue : ObservableNumberValue {
//    fun get(): Short
//}
//
//interface WritableShortValue : WritableNumberValue {
//    fun get(): Int
//
//    fun set(var1: Int)
//
//    override fun setValue(var1: Number)
//}
