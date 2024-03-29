package com.github.danielchemko.winmdj.util

import java.io.Closeable
import java.sql.SQLException


class Handle(
//    jdbi: Jdbi,
//    connectionCleaner: Cleanable,
//    transactionHandler: TransactionHandler,
//    statementBuilder: StatementBuilder,
//    connection: Connection
) : Closeable {
//    private val jdbi: Jdbi
//    private val connectionCleaner: Cleanable
//    private val transactionHandler: TransactionHandler
//    private val connection: Connection
//    private val forceEndTransactions: Boolean
//
//    private var statementBuilder: StatementBuilder
//
//    // the fallback context. It is used when resetting the Handle state.
//    private val defaultExtensionContext: ExtensionContext
//
//    private var currentExtensionContext: ExtensionContext
//
//    @GuardedBy("transactionCallbacks")
//    private val transactionCallbacks: MutableList<TransactionCallback> = ArrayList<TransactionCallback>()
//
//    private val cleanables: MutableSet<Cleanable?> = LinkedHashSet()
//
//    private val handleListeners: MutableSet<HandleListener>
//
//    private val closed = AtomicBoolean()
//
//    init {
//        this.jdbi = jdbi
//        this.connectionCleaner = connectionCleaner
//        this.connection = connection
//
//        // create a copy to detach config from the jdbi to allow local changes.
//        this.defaultExtensionContext = ExtensionContext.forConfig(jdbi.getConfig().createCopy())
//        this.currentExtensionContext = defaultExtensionContext
//
//        this.statementBuilder = statementBuilder
//        this.handleListeners = config.get(Handles::class.java).copyListeners()
//
//        addCleanable({ statementBuilder.close(connection) })
//
//        // both of these methods are bad because they leak a reference to this handle before the c'tor finished.
//        this.transactionHandler = transactionHandler.specialize(this)
//        this.forceEndTransactions = !this.transactionHandler.isInTransaction(this)
//    }
//
//    /**
//     * Returns the [Jdbi] object used to create this handle.
//     *
//     * @return The [Jdbi] object used to create this handle.
//     */
//    fun getJdbi(): Jdbi {
//        return jdbi
//    }
//
//    val config: ConfigRegistry
//        /**
//         * The current configuration object associated with this handle.
//         *
//         * @return A [ConfigRegistry] object that is associated with the handle.
//         */
//        get() = currentExtensionContext.getConfig()
//
//    /**
//     * Get the JDBC [Connection] this Handle uses.
//     *
//     * @return the JDBC [Connection] this Handle uses.
//     */
//    fun getConnection(): Connection {
//        return connection
//    }
//
//    /**
//     * Returns the current [StatementBuilder] which is used to create new JDBC [java.sql.Statement] objects.
//     *
//     * @return the current [StatementBuilder].
//     */
//    fun getStatementBuilder(): StatementBuilder {
//        return statementBuilder
//    }
//
//    /**
//     * Set the statement builder for this handle.
//     *
//     * @param builder StatementBuilder to be used. Must not be null.
//     * @return this
//     */
//    fun setStatementBuilder(builder: StatementBuilder): Handle {
//        this.statementBuilder = builder
//        return this
//    }
//
//    /**
//     * Add a specific [HandleListener] which is called for specific events for this Handle. Note that
//     * it is not possible to add a listener that wants to implement [HandleListener.handleCreated] this way
//     * as the handle has already been created. Use [Handles.addListener] in this case.
//     * <br></br>
//     * A listener added through this call is specific to the handle and not shared with other handles.
//     *
//     * @param handleListener A [HandleListener] object.
//     * @return The handle itself.
//     */
//    fun addHandleListener(handleListener: HandleListener): Handle {
//        handleListeners.add(handleListener)
//
//        return this
//    }
//
//    /**
//     * Remove a [HandleListener] from this handle.
//     * <br></br>
//     * Removing the listener only affects the current handle. To remove a listener for all future handles, use [Handles.removeListener].
//     *
//     * @param handleListener A [HandleListener] object.
//     * @return The handle itself.
//     */
//    fun removeHandleListener(handleListener: HandleListener): Handle {
//        handleListeners.remove(handleListener)
//
//        return this
//    }
//
//    /**
//     * Registers a `Cleanable` to be invoked when the handle is closed. Any cleanable registered here will only be cleaned once.
//     *
//     *
//     * Resources cleaned up by Jdbi include [java.sql.ResultSet], [java.sql.Statement], [java.sql.Array], and [StatementBuilder].
//     *
//     * @param cleanable the Cleanable to clean on close
//     */
//    fun addCleanable(cleanable: Cleanable?) {
//        synchronized(cleanables) {
//            cleanables.add(cleanable)
//        }
//    }
//
//    /**
//     * Unregister a `Cleanable` from the Handle.
//     *
//     * @param cleanable the Cleanable to be unregistered.
//     */
//    fun removeCleanable(cleanable: Cleanable?) {
//        synchronized(cleanables) {
//            cleanables.remove(cleanable)
//        }
//    }
//
//    /**
//     * Closes the handle, its connection, and any other database resources it is holding.
//     *
//     * @throws CloseException       if any resources throw exception while closing
//     * @throws TransactionException if called while the handle has a transaction open. The open transaction will be
//     * rolled back.
//     */
//    override fun close() {
//        if (closed.getAndSet(true)) {
//            return
//        }
//
//        // do this at call time, otherwise running the cleanables may affect the state of the other handle objects (e.g. the config)
//        val doForceEndTransactions =
//            this.forceEndTransactions && config.get(Handles::class.java).isForceEndTransactions()
//
//        try {
//            val throwableSuppressor: ThrowableSuppressor = ThrowableSuppressor()
//
//            doClean(throwableSuppressor)
//
//            try {
//                cleanConnection(doForceEndTransactions)
//            } catch (t: Throwable) {
//                throwableSuppressor.attachToThrowable(t)
//                throw t
//            }
//
//            throwableSuppressor.throwIfNecessary { t -> CloseException("While closing handle", t) }
//        } finally {
//            LOG.trace("Handle [{}] released", this)
//
//            notifyHandleClosed()
//        }
//    }
//
//    /**
//     * Release any database resource that may be held by the handle. This affects
//     * any statement that was created from the Handle.
//     */
//    fun clean() {
//        val throwableSuppressor: ThrowableSuppressor = ThrowableSuppressor()
//
//        doClean(throwableSuppressor)
//
//        throwableSuppressor.throwIfNecessary()
//    }
//
//    val isClean: Boolean
//        /**
//         * Returns true if the Handle currently holds no database resources.
//         * <br></br>
//         * Note that this method will return `false` right after statement creation
//         * as every statement registers its statement context with the handle. Once
//         *
//         * @return True if the handle holds no database resources.
//         */
//        get() {
//            synchronized(cleanables) {
//                return cleanables.isEmpty()
//            }
//        }
//
//    private fun doClean(throwableSuppressor: ThrowableSuppressor) {
//        var cleanablesCopy: List<Cleanable?>
//
//        synchronized(cleanables) {
//            cleanablesCopy = ArrayList(cleanables)
//            cleanables.clear()
//        }
//
//        Collections.reverse(cleanablesCopy)
//
//        for (cleanable: Cleanable? in cleanablesCopy) {
//            throwableSuppressor.suppressAppend(cleanable::close)
//        }
//    }
//
//    /**
//     * Returns true if the [Handle] has been closed.
//     *
//     * @return True if the Handle is closed.
//     */
//    fun isClosed(): Boolean {
//        return closed.get()
//    }
//
//    /**
//     * Convenience method which creates a query with the given positional arguments.
//     *
//     * @param sql  SQL or named statement
//     * @param args arguments to bind positionally
//     * @return query object
//     */
//    fun select(sql: CharSequence?, vararg args: Any?): Query {
//        val query = this.createQuery(sql)
//        var position = 0
//        for (arg: Any? in args) {
//            query.bind(position++, arg)
//        }
//        return query
//    }
//
//    /**
//     * Convenience method which creates a query with the given positional arguments. Takes a string argument for backwards compatibility reasons.
//     *
//     * @param sql  SQL or named statement
//     * @param args arguments to bind positionally
//     * @return query object
//     * @see Handle.select
//     */
//    fun select(sql: String?, vararg args: Any?): Query {
//        return select(sql as CharSequence?, *args)
//    }
//
//    /**
//     * Execute a SQL statement, and return the number of rows affected by the statement.
//     *
//     * @param sql  the SQL statement to execute, using positional parameters (if any).
//     * @param args positional arguments.
//     * @return the number of rows affected.
//     */
//    fun execute(sql: CharSequence?, vararg args: Any?): Int {
//        createUpdate(sql).use { stmt ->
//            var position: Int = 0
//            for (arg: Any? in args) {
//                stmt.bind(position++, arg)
//            }
//            return stmt.execute()
//        }
//    }
//
//    /**
//     * Execute a SQL statement, and return the number of rows affected by the statement. Takes a string argument for backwards compatibility reasons.
//     *
//     * @param sql  the SQL statement to execute, using positional parameters (if any).
//     * @param args positional arguments.
//     * @return the number of rows affected.
//     * @see Handle.execute
//     */
//    fun execute(sql: String?, vararg args: Any?): Int {
//        return execute(sql as CharSequence?, *args)
//    }
//
//    /**
//     * Create a non-prepared (no bound parameters, but different SQL) batch statement.
//     *
//     * @return empty batch
//     * @see Handle.prepareBatch
//     */
//    fun createBatch(): Batch {
//        return Batch(this)
//    }
//
//    /**
//     * Prepare a batch to execute. This is for efficiently executing more than one
//     * of the same statements with different parameters bound.
//     *
//     * @param sql the batch SQL.
//     * @return a batch which can have "statements" added.
//     */
//    fun prepareBatch(sql: CharSequence?): PreparedBatch {
//        return PreparedBatch(this, sql)
//    }
//
//    /**
//     * Prepare a batch to execute. This is for efficiently executing more than one
//     * of the same statements with different parameters bound. Takes a string argument for backwards compatibility reasons.
//     *
//     * @param sql the batch SQL.
//     * @return a batch which can have "statements" added.
//     * @see Handle.prepareBatch
//     */
//    fun prepareBatch(sql: String?): PreparedBatch {
//        return prepareBatch(sql as CharSequence?)
//    }
//
//    /**
//     * Create a call to a stored procedure.
//     *
//     * @param sql the stored procedure sql.
//     * @return the Call.
//     */
//    fun createCall(sql: CharSequence?): Call {
//        return Call(this, sql)
//    }
//
//    /**
//     * Create a call to a stored procedure. Takes a string argument for backwards compatibility reasons.
//     *
//     * @param sql the stored procedure sql.
//     * @return the Call.
//     * @see Handle.createCall
//     */
//    fun createCall(sql: String?): Call {
//        return createCall(sql as CharSequence?)
//    }
//
//    /**
//     * Return a Query instance that executes a statement
//     * with bound parameters and maps the result set into Java types.
//     *
//     * @param sql SQL that may return results.
//     * @return a Query builder.
//     */
//    fun createQuery(sql: CharSequence?): Query {
//        return Query(this, sql)
//    }
//
//    /**
//     * Return a Query instance that executes a statement
//     * with bound parameters and maps the result set into Java types. Takes a string argument for backwards compatibility reasons.
//     *
//     * @param sql SQL that may return results.
//     * @return a Query builder.
//     * @see Handle.createQuery
//     */
//    fun createQuery(sql: String?): Query {
//        return createQuery(sql as CharSequence?)
//    }
//
//    /**
//     * Creates a Script from the given SQL script.
//     *
//     * @param sql the SQL script.
//     * @return the created Script.
//     */
//    fun createScript(sql: CharSequence?): Script {
//        return Script(this, sql)
//    }
//
//    /**
//     * Create an Insert or Update statement which returns the number of rows modified. Takes a string argument for backwards compatibility reasons.
//     *
//     * @param sql the statement sql.
//     * @return the Update builder.
//     * @see Handle.createScript
//     */
//    fun createScript(sql: String?): Script {
//        return createScript(sql as CharSequence?)
//    }
//
//    /**
//     * Create an Insert or Update statement which returns the number of rows modified.
//     *
//     * @param sql the statement sql.
//     * @return the Update builder.
//     */
//    fun createUpdate(sql: CharSequence?): Update {
//        return Update(this, sql)
//    }
//
//    /**
//     * Create an Insert or Update statement which returns the number of rows modified. Takes a string argument for backwards compatibility reasons.
//     *
//     * @param sql the statement sql.
//     * @return the Update builder.
//     * @see Handle.createUpdate
//     */
//    fun createUpdate(sql: String?): Update {
//        return createUpdate(sql as CharSequence?)
//    }
//
//    /**
//     * Access database metadata that returns a [java.sql.ResultSet]. All methods of [org.jdbi.v3.core.result.ResultBearing] can be used to format
//     * and map the returned results.
//     *
//     * <pre>
//     * List&lt;String&gt; catalogs = h.queryMetadata(DatabaseMetaData::getCatalogs)
//     * .mapTo(String.class)
//     * .list();
//    </pre> *
//     *
//     *
//     * returns the list of catalogs from the current database.
//     *
//     * @param metadataFunction Maps the provided [java.sql.DatabaseMetaData] object onto a [java.sql.ResultSet] object.
//     * @return The metadata builder.
//     */
//    fun queryMetadata(metadataFunction: MetaData.MetaDataResultSetProvider?): ResultBearing {
//        return MetaData(this, metadataFunction)
//    }
//
//    /**
//     * Access all database metadata that returns simple values.
//     *
//     * <pre>
//     * boolean supportsTransactions = handle.queryMetadata(DatabaseMetaData::supportsTransactions);
//    </pre> *
//     *
//     * @param metadataFunction Maps the provided [java.sql.DatabaseMetaData] object to a response object.
//     * @return The response object.
//     */
//    fun <T> queryMetadata(metadataFunction: MetaData.MetaDataValueProvider<T>?): T {
//        MetaData(this, metadataFunction).use { metadata ->
//            return metadata.execute()
//        }
//    }
//
//    val isInTransaction: Boolean
//        /**
//         * Returns whether the handle is in a transaction. Delegates to the underlying [TransactionHandler].
//         *
//         * @return True if the handle is in a transaction.
//         */
//        get() = transactionHandler.isInTransaction(this)
//
//    /**
//     * Start a transaction.
//     *
//     * @return the same handle.
//     */
//    fun begin(): Handle {
//        transactionHandler.begin(this)
//        LOG.trace("Handle [{}] begin transaction", this)
//        return this
//    }
//
//    /**
//     * Commit a transaction.
//     *
//     * @return the same handle.
//     */
//    fun commit(): Handle {
//        val start = System.nanoTime()
//        transactionHandler.commit(this)
//        LOG.trace("Handle [{}] commit transaction in {}ms", this, msSince(start))
//        drainCallbacks()
//            .forEach(TransactionCallback::afterCommit)
//        return this
//    }
//
//    /**
//     * Rollback a transaction.
//     *
//     * @return the same handle.
//     */
//    fun rollback(): Handle {
//        val start = System.nanoTime()
//        transactionHandler.rollback(this)
//        LOG.trace("Handle [{}] rollback transaction in {}ms", this, msSince(start))
//        drainCallbacks()
//            .forEach(TransactionCallback::afterRollback)
//        return this
//    }
//
//    /**
//     * Execute an action the next time this Handle commits, unless it is rolled back first.
//     *
//     * @param afterCommit the action to execute after commit.
//     * @return this Handle.
//     */
//    @Beta
//    fun afterCommit(afterCommit: Runnable): Handle {
//        return addTransactionCallback(object : TransactionCallback() {
//            fun afterCommit() {
//                afterCommit.run()
//            }
//        })
//    }
//
//    /**
//     * Execute an action the next time this Handle rolls back, unless it is committed first.
//     *
//     * @param afterRollback the action to execute after rollback.
//     * @return this Handle.
//     */
//    @Beta
//    fun afterRollback(afterRollback: Runnable): Handle {
//        return addTransactionCallback(object : TransactionCallback() {
//            fun afterRollback() {
//                afterRollback.run()
//            }
//        })
//    }
//
//    fun drainCallbacks(): List<TransactionCallback> {
//        synchronized(transactionCallbacks) {
//            val result: List<TransactionCallback> =
//                ArrayList<Any?>(transactionCallbacks)
//            transactionCallbacks.clear()
//            return result
//        }
//    }
//
//    fun addTransactionCallback(cb: TransactionCallback): Handle {
//        if (!isInTransaction) {
//            throw IllegalStateException("Handle must be in transaction")
//        }
//        synchronized(transactionCallbacks) {
//            transactionCallbacks.add(cb)
//        }
//        return this
//    }
//
//    /**
//     * Rollback a transaction to a named savepoint.
//     *
//     * @param savepointName the name of the savepoint, previously declared with [Handle.savepoint].
//     * @return the same handle.
//     */
//    fun rollbackToSavepoint(savepointName: String?): Handle {
//        val start = System.nanoTime()
//        transactionHandler.rollbackToSavepoint(this, savepointName)
//        LOG.trace("Handle [{}] rollback to savepoint \"{}\" in {}ms", this, savepointName, msSince(start))
//        return this
//    }
//
//    /**
//     * Create a transaction savepoint with the name provided.
//     *
//     * @param name The name of the savepoint.
//     * @return The same handle.
//     */
//    fun savepoint(name: String?): Handle {
//        transactionHandler.savepoint(this, name)
//        LOG.trace("Handle [{}] savepoint \"{}\"", this, name)
//        return this
//    }
//
//    /**
//     * Release a previously created savepoint.
//     *
//     * @param savepointName the name of the savepoint to release.
//     * @return the same handle.
//     */
//    @Deprecated("Use {@link Handle#releaseSavepoint(String)}")
//    fun release(savepointName: String?): Handle {
//        return releaseSavepoint(savepointName)
//    }
//
//    /**
//     * Release a previously created savepoint.
//     *
//     * @param savepointName the name of the savepoint to release.
//     * @return the same handle.
//     */
//    fun releaseSavepoint(savepointName: String?): Handle {
//        transactionHandler.releaseSavepoint(this, savepointName)
//        LOG.trace("Handle [{}] release savepoint \"{}\"", this, savepointName)
//        return this
//    }
//
//    val isReadOnly: Boolean
//        /**
//         * Whether the connection is in read-only mode.
//         *
//         * @return True if the connection is in read-only mode.
//         * @see Connection.isReadOnly
//         */
//        get() {
//            try {
//                return connection.isReadOnly()
//            } catch (e: SQLException) {
//                throw UnableToManipulateTransactionIsolationLevelException(
//                    "Could not get read-only status for a connection",
//                    e
//                )
//            }
//        }
//
//    /**
//     * Set the Handle read-only. This acts as a hint to the database to improve performance or concurrency.
//     * <br></br>
//     * May not be called in an active transaction!
//     *
//     * @param readOnly whether the Handle is readOnly.
//     * @return this Handle.
//     * @see Connection.setReadOnly
//     */
//    fun setReadOnly(readOnly: Boolean): Handle {
//        try {
//            connection.setReadOnly(readOnly)
//        } catch (e: SQLException) {
//            throw UnableToManipulateTransactionIsolationLevelException("Could not setReadOnly", e)
//        }
//        return this
//    }
//
//    /**
//     * Executes `callback` in a transaction, and returns the result of the callback.
//     *
//     * @param callback a callback which will receive an open handle, in a transaction.
//     * @param <R>      type returned by callback
//     * @param <X>      exception type thrown by the callback, if any
//     * @return value returned from the callback
//     * @throws X any exception thrown by the callback
//    </X></R> */
//    @Throws(X::class)
//    fun <R, X : Exception?> inTransaction(callback: HandleCallback<R, X>): R {
//        return if (isInTransaction
//        ) callback.withHandle(this)
//        else transactionHandler.inTransaction(this, callback)
//    }
//
//    /**
//     * Executes `callback` in a transaction.
//     *
//     * @param consumer a callback which will receive an open handle, in a transaction.
//     * @param <X>      exception type thrown by the callback, if any
//     * @throws X any exception thrown by the callback
//    </X> */
//    @Throws(X::class)
//    fun <X : Exception?> useTransaction(consumer: HandleConsumer<X>) {
//        inTransaction(consumer.asCallback())
//    }
//
//    /**
//     * Executes `callback` in a transaction, and returns the result of the callback.
//     *
//     *
//     * This form accepts a transaction isolation level which will be applied to the connection
//     * for the scope of this transaction, after which the original isolation level will be restored.
//     *
//     *
//     * @param level    the transaction isolation level which will be applied to the connection for the scope of this
//     * transaction, after which the original isolation level will be restored.
//     * @param callback a callback which will receive an open handle, in a transaction.
//     * @param <R>      type returned by callback
//     * @param <X>      exception type thrown by the callback, if any
//     * @return value returned from the callback
//     * @throws X any exception thrown by the callback
//    </X></R> */
//    @Throws(X::class)
//    fun <R, X : Exception?> inTransaction(level: TransactionIsolationLevel, callback: HandleCallback<R, X>): R {
//        if (isInTransaction) {
//            val currentLevel: TransactionIsolationLevel = transactionIsolationLevel
//            if (currentLevel !== level && level !== TransactionIsolationLevel.UNKNOWN) {
//                throw TransactionException(
//                    "Tried to execute nested transaction with isolation level " + level + ", "
//                            + "but already running in a transaction with isolation level " + currentLevel + "."
//                )
//            }
//            return callback.withHandle(this)
//        }
//
//        SetTransactionIsolation(level).use { isolation ->
//            return transactionHandler.inTransaction(this, level, callback)
//        }
//    }
//
//    /**
//     * Executes `callback` in a transaction.
//     *
//     *
//     * This form accepts a transaction isolation level which will be applied to the connection
//     * for the scope of this transaction, after which the original isolation level will be restored.
//     *
//     *
//     * @param level    the transaction isolation level which will be applied to the connection for the scope of this
//     * transaction, after which the original isolation level will be restored.
//     * @param consumer a callback which will receive an open handle, in a transaction.
//     * @param <X>      exception type thrown by the callback, if any
//     * @throws X any exception thrown by the callback
//    </X> */
//    @Throws(X::class)
//    fun <X : Exception?> useTransaction(level: TransactionIsolationLevel, consumer: HandleConsumer<X>) {
//        inTransaction(level, consumer.asCallback())
//    }
//
//    /**
//     * Set the transaction isolation level on the underlying connection if it is different from the current isolation level.
//     *
//     * @param level the [TransactionIsolationLevel] to use.
//     * @throws UnableToManipulateTransactionIsolationLevelException if isolation level is not supported by the underlying connection or JDBC driver.
//     */
//    @Deprecated("Use {@link Handle#setTransactionIsolationLevel(int)}")
//    fun setTransactionIsolation(level: TransactionIsolationLevel?) {
//        transactionIsolationLevel = level
//    }
//
//    /**
//     * Set the transaction isolation level on the underlying connection if it is different from the current isolation level.
//     *
//     * @param level the isolation level to use.
//     * @see Handle.setTransactionIsolationLevel
//     * @see Connection.TRANSACTION_NONE
//     *
//     * @see Connection.TRANSACTION_READ_UNCOMMITTED
//     *
//     * @see Connection.TRANSACTION_READ_COMMITTED
//     *
//     * @see Connection.TRANSACTION_REPEATABLE_READ
//     *
//     * @see Connection.TRANSACTION_SERIALIZABLE
//     *
//     */
//    @Deprecated("Use {@link Handle#setTransactionIsolationLevel(TransactionIsolationLevel)}")
//    fun setTransactionIsolation(level: Int) {
//        transactionIsolationLevel = level
//    }
//
//    /**
//     * Set the transaction isolation level on the underlying connection if it is different from the current isolation level.
//     *
//     * @param level the isolation level to use.
//     * @see Handle.setTransactionIsolationLevel
//     * @see Connection.TRANSACTION_NONE
//     *
//     * @see Connection.TRANSACTION_READ_UNCOMMITTED
//     *
//     * @see Connection.TRANSACTION_READ_COMMITTED
//     *
//     * @see Connection.TRANSACTION_REPEATABLE_READ
//     *
//     * @see Connection.TRANSACTION_SERIALIZABLE
//     */
//    fun setTransactionIsolationLevel(level: Int) {
//        try {
//            if (connection.getTransactionIsolation() !== level) {
//                connection.setTransactionIsolation(level)
//            }
//        } catch (e: SQLException) {
//            throw UnableToManipulateTransactionIsolationLevelException(level, e)
//        }
//    }
//
//    var transactionIsolationLevel: TransactionIsolationLevel
//        /**
//         * Obtain the current transaction isolation level.
//         *
//         * @return the current isolation level on the underlying connection.
//         */
//        get() {
//            try {
//                return TransactionIsolationLevel.valueOf(connection.getTransactionIsolation())
//            } catch (e: SQLException) {
//                throw UnableToManipulateTransactionIsolationLevelException("unable to access current setting", e)
//            }
//        }
//        /**
//         * Set the transaction isolation level on the underlying connection if it is different from the current isolation level.
//         *
//         * @param level the [TransactionIsolationLevel] to use.
//         * @throws UnableToManipulateTransactionIsolationLevelException if isolation level is not supported by the underlying connection or JDBC driver.
//         */
//        set(level) {
//            if (level !== TransactionIsolationLevel.UNKNOWN) {
//                setTransactionIsolationLevel(level.intValue())
//            }
//        }
//
//    /**
//     * Create a Jdbi extension object of the specified type bound to this handle. The returned extension's lifecycle is
//     * coupled to the lifecycle of this handle. Closing the handle will render the extension unusable.
//     *
//     * @param extensionType the extension class
//     * @param <T>           the extension type
//     * @return the new extension object bound to this handle
//    </T> */
//    fun <T> attach(extensionType: Class<T>?): T {
//        return config
//            .findFor(extensionType, ConstantHandleSupplier.of(this))
//            .orElseThrow { NoSuchExtensionException(extensionType) }
//    }
//
//    val extensionMethod: ExtensionMethod
//        /**
//         * Returns the extension method currently bound to the handle's context.
//         *
//         * @return the extension method currently bound to the handle's context
//         */
//        get() = currentExtensionContext.extensionMethod
//
//    fun acceptExtensionContext(extensionContext: ExtensionContext?): Handle {
//        this.currentExtensionContext = extensionContext ?: defaultExtensionContext
//
//        return this
//    }
//
//    private fun notifyHandleCreated() {
//        handleListeners.forEach(Consumer<HandleListener> { listener: HandleListener ->
//            listener.handleCreated(this)
//        })
//    }
//
//    private fun notifyHandleClosed() {
//        handleListeners.forEach(Consumer<HandleListener> { listener: HandleListener ->
//            listener.handleClosed(this)
//        })
//    }
//
//    private fun cleanConnection(doForceEndTransactions: Boolean) {
//        val throwableSuppressor: ThrowableSuppressor = ThrowableSuppressor()
//
//        var wasInTransaction = false
//
//        if (doForceEndTransactions) {
//            wasInTransaction =
//                throwableSuppressor.suppressAppend( // if the connection was not closed, check whether it is in a transaction
//                    // if any of this throws an exception, assume that the connection was closed,
//                    // skip the transaction check and record the exception
//                    { !connection.isClosed() && isInTransaction },
//                    false
//                )
//        }
//
//        if (wasInTransaction) {
//            throwableSuppressor.suppressAppend { this.rollback() }
//        }
//
//        try {
//            connectionCleaner.close()
//        } catch (e: SQLException) {
//            val ce: CloseException = CloseException("Unable to close Connection", e)
//            throwableSuppressor.attachToThrowable(ce)
//            throw ce
//        }
//
//        throwableSuppressor.throwIfNecessary { t -> CloseException("Failed to clear transaction status on close", t) }
//
//        if (wasInTransaction) {
//            throw TransactionException(
//                ("Improper transaction handling detected: A Handle with an open "
//                        + "transaction was closed. Transactions must be explicitly committed or rolled back "
//                        + "before closing the Handle. "
//                        + "Jdbi has rolled back this transaction automatically."
//                        + "This check may be disabled by calling getConfig(Handles.class).setForceEndTransactions(false).")
//            )
//        }
//    }
//
//    override fun equals(o: Any?): Boolean {
//        if (this === o) {
//            return true
//        }
//        if (o == null || javaClass != o.javaClass) {
//            return false
//        }
//        val handle = o as Handle
//        return jdbi.equals(handle.jdbi) && connection.equals(handle.connection)
//    }
//
//    override fun hashCode(): Int {
//        return Objects.hash(jdbi, connection)
//    }
//
//    internal inner class SetTransactionIsolation(setLevel: TransactionIsolationLevel?) : AutoCloseable {
//        private val prevLevel: TransactionIsolationLevel
//
//        init {
//            prevLevel = this.transactionIsolationLevel
//            this.transactionIsolationLevel = setLevel
//        }
//
//        override fun close() {
//            this.transactionIsolationLevel = prevLevel
//        }
//    }

    override fun close() {
        TODO("Not yet implemented")
    }

    companion object {
        @Throws(SQLException::class)
        fun createHandle(
//            jdbi: Jdbi,
//            connectionCleaner: Cleanable,
//            transactionHandler: TransactionHandler,
//            statementBuilder: StatementBuilder,
//            connection: Connection
        ): Handle {
            val handle = Handle()

            return handle
        }
    }
}