package com.github.danielchemko.winmdj.explore.root

import com.github.danielchemko.winmdj.core.MdObjectMapper
import com.github.danielchemko.winmdj.core.mdspec.*
import com.github.danielchemko.winmdj.explore.model.WinMdTreeItem
import com.github.danielchemko.winmdj.explore.model.WinMdTreeItemAction
import com.github.danielchemko.winmdj.explore.utils.FxmlResource
import com.github.danielchemko.winmdj.explore.utils.WinMdController
import com.github.danielchemko.winmdj.parser.WinMdNavigator
import com.sun.javafx.collections.ObservableListWrapper
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleLongProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableValue
import javafx.collections.transformation.SortedList
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.name
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions
import kotlin.reflect.full.isSuperclassOf


private val EXCLUDED_FUNCTIONS = setOf(
    "equals",
    "clone",
    "toString",
    "hashCode",
    "copy",
    "getByte",
    "getShort",
    "getDouble",
    "getUByte",
    "getULong",
    "getFloat",
    "getLong",
    "getChar",
    "getInt",
    "getUInt",
    "getAsBoolean",
    "getUShort",
    "getString",
    "getValueBlob",
    "getValueClass",
    "getStub",
    "getVersionRaw",
    "getRawType",
)


@FxmlResource("/winmd-explore-root.fxml", newStage = true, sceneX = 1800, sceneY = 750, "WinMD Explorer")
class RootController : WinMdController() {
    @FXML
    private lateinit var assemblyView: TreeView<WinMdTreeItem>

    @FXML
    private lateinit var metadataTableView: TableView<WinMdObject>

    private lateinit var rootItem: TreeItem<WinMdTreeItem>

    @FXML
    fun initialize() {
        metadataTableView.isEditable = false

        rootItem = TreeItem(WinMdTreeItem(text = "/"))

        val winMdFile = Path.of("Windows.Win32.winmd")
        if (winMdFile.toFile().exists()) {
            addWinMd(winMdFile)
        }

//        val allFilesSet = mutableSetOf<Path>()
//        try {
//            visitAllFilesIn(Path.of("C:/"), allFilesSet)
//        } catch (e: Throwable) {
//            e.printStackTrace()
//        }
//        allFilesSet.forEach { addWinMd(it) }

        val ilSpyPath = Path.of("C:/Program Files/ILSpy_selfcontained_8.2.0.7535-x64")
        ilSpyPath.toFile().list()!!.filter { it.contains(".dll") || it.contains(".exe") }
            .map { ilSpyPath.resolve(it) }
            .forEach {
                try {
                    addWinMd(it)
                } catch (e: Exception) {
                    println(e.message)
                }
            }

        val winSystem32Path = Path.of("C:/Windows/System32")
        winSystem32Path.toFile().list()!!.filter { it.contains(".dll") || it.contains(".exe") }
            .map { winSystem32Path.resolve(it) }
            .forEach {
                try {
                    addWinMd(it)
                } catch (e: Exception) {
                    println(e.message)
                }
            }

        assemblyView.root = rootItem
    }

    @Throws(IOException::class)
    private fun visitAllFilesIn(path: Path, allFiles: MutableSet<Path>) {
        try {
            Files.newDirectoryStream(path).use { stream ->
                for (entry in stream) {
                    if (allFiles.contains(entry)) {
                        continue
                    }
                    if (Files.isDirectory(entry)) {
                        visitAllFilesIn(entry, allFiles)
                    } else if (entry.name.contains(".dll") || entry.name.contains(".exe")) {
                        allFiles.add(entry)
                    }
                }
            }
        } catch (e: java.nio.file.AccessDeniedException) {
            /* Do nothing */
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun addWinMd(path: Path) {
        try {
            val fileName = path.toFile().name
            val navigator = WinMdNavigator()
            navigator.parseFile(path)

            val objectMapper = MdObjectMapper(navigator)

            val fileItem = TreeItem(
                WinMdTreeItem(
                    text = "${fileName} (${
                        CLRMetadataType.entries.toTypedArray().sumOf { navigator.getCount(it) }
                    })"
                )
            )
            val tablesItem = TreeItem(WinMdTreeItem(text = "Tables"))

            CLRMetadataType.entries.filter { navigator.getCount(it) > 0 }.map { type ->
                val count = navigator.getCount(type)
                tablesItem.children.add(
                    TreeItem(
                        WinMdTreeItem(
                            "${
                                type.bitSetIndex.toUByte().toHexString(HexFormat.UpperCase)
                            } ${type.name} ($count)", actions = listOf(
                                WinMdTreeItemAction("selected") { selectTable(objectMapper, navigator, type) }
                            )
                        )
                    )
                )
            }

            if (tablesItem.children.isNotEmpty()) {
                fileItem.children.add(tablesItem)
                rootItem.children.add(fileItem)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun selectTable(objectMapper: MdObjectMapper, navigator: WinMdNavigator, type: CLRMetadataType) {

        metadataTableView.columns.clear()

        val allFunctions = getClassInterface(type).functions

        val columnNameAndRankAndFunction = allFunctions.mapNotNull { function ->
            val columnNameAndRank = convertFunctionNameToColumnName(function.name) ?: return@mapNotNull null
            columnNameAndRank to function
        }.sortedBy { it.first.second }

        metadataTableView.columns.addAll(columnNameAndRankAndFunction.map {
            val columnName = it.first.first
            val function = it.second
            val columnInfo: ObjectColumn? = function.findAnnotation<ObjectColumn>()

            val returnType: KClass<*>
            val returnIsPlural: Boolean
            if (List::class.isSuperclassOf(function.returnType.classifier as KClass<*>)) {
                returnType = function.returnType.arguments[0].type!!.classifier as KClass<*>
                returnIsPlural = true
            } else {
                returnType = function.returnType.classifier as KClass<*>
                returnIsPlural = false
            }

            val column: TableColumn<WinMdObject, Any?> = TableColumn<WinMdObject, Any?>(columnName)
            column.isSortable = true

            when {
                returnType == Integer::class -> {
                    column.setCellValueFactory { r ->
                        val retr: Int = try {
                            function.call(r.value) as Int
                        } catch (e: Exception) {
                            -1
                        }
                        SimpleIntegerProperty(retr) as ObservableValue<Any?>
                    }
                }

                returnType == UInt::class -> {
                    column.setCellValueFactory { r ->
                        val retr: Int = try {
                            (function.call(r.value) as UInt).toInt()
                        } catch (e: Exception) {
                            -1
                        }
                        SimpleIntegerProperty(retr) as ObservableValue<Any?>
                    }
                    column.setCellFactory { _ -> HexCell<Number>(8) as TableCell<WinMdObject, Any?> }
                }

                returnType == UShort::class -> {
                    column.setCellValueFactory { r ->
                        val retr: Int = try {
                            (function.call(r.value) as UShort).toInt()
                        } catch (e: Exception) {
                            -1
                        }
                        SimpleIntegerProperty(retr) as ObservableValue<Any?>
                    }
                    column.setCellFactory { _ -> HexCell<Number>(4) as TableCell<WinMdObject, Any?> }
                }

                returnType == String::class -> {
                    column.setCellValueFactory { r ->
                        val retr: String = try {
                            function.call(r.value) as String
                        } catch (e: Exception) {
                            "N/A"
                        }
                        SimpleStringProperty(retr) as ObservableValue<Any?>
                    }
                }

                returnType == ByteArray::class -> {
                    column.setCellValueFactory { r ->
                        val retr: ByteArray? = try {
                            function.call(r.value) as ByteArray?
                        } catch (e: Exception) {
                            null
                        }

                        if (retr == null) {
                            SimpleStringProperty("") as ObservableValue<Any?>
                        } else if (retr.size < 10) {
                            SimpleStringProperty(retr.toHexString(HexFormat.UpperCase)) as ObservableValue<Any?>
                        } else {
                            SimpleStringProperty(
                                "${
                                    retr.toHexString(
                                        0,
                                        10,
                                        HexFormat.UpperCase
                                    )
                                } ..."
                            ) as ObservableValue<Any?>
                        }
                    }
                }

                returnType == Any::class -> {
                    column.setCellValueFactory { r ->
                        val retr: Any? = try {
                            function.call(r.value) as Any?
                        } catch (e: Exception) {
                            null
                        }

                        if (r.value != null && retr == null) {
                            SimpleStringProperty("<???>") as ObservableValue<Any?>
                        } else if (retr == null) {
                            SimpleStringProperty("") as ObservableValue<Any?>
                        } else if (retr is ByteArray && retr.size < 10) {
                            SimpleStringProperty(retr.toHexString(HexFormat.UpperCase)) as ObservableValue<Any?>
                        } else if (retr is ByteArray) {
                            SimpleStringProperty(
                                "${
                                    retr.toHexString(
                                        0,
                                        10,
                                        HexFormat.UpperCase
                                    )
                                } ..."
                            ) as ObservableValue<Any?>
                        } else {
                            SimpleStringProperty(retr.toString()) as ObservableValue<Any?>
                        }
                    }
                }

                Enum::class.isSuperclassOf(returnType) -> {
                    column.setCellValueFactory { r ->
                        val retr: Enum<*>? = try {
                            function.call(r.value) as Enum<*>?
                        } catch (e: Exception) {
                            null
                        }
                        if (retr == null) {
                            SimpleStringProperty("") as ObservableValue<Any?>
                        } else {
                            SimpleStringProperty(retr.name) as ObservableValue<Any?>
                        }
                    }
                }

                WinMdObject::class.isSuperclassOf(returnType) -> {
                    val sourceClazz = getClassInterface(type)
                    val targetType = getObjectType(returnType as KClass<out WinMdObject>).objectType
                    column.setCellValueFactory { r ->
                        val targetValue = r.value

                        val colOrdinal = columnInfo!!.ordinal
                        if (columnInfo!!.table == LookupType.TARGET || columnInfo!!.table == LookupType.TARGET_LIST) {
                            try {
                                val rawPtr = targetValue.getStub().getObjectTableValue(type, colOrdinal)
                                if (rawPtr.toString().toInt() < 1 || rawPtr.toString().toInt() > navigator.getCount(
                                        targetType
                                    )
                                ) {
                                    SimpleStringProperty("") as ObservableValue<Any?>
                                } else {
                                    when (rawPtr) {
                                        is UShort -> SimpleIntegerProperty(rawPtr.toInt()) as ObservableValue<Any?>
                                        is UInt -> SimpleIntegerProperty(rawPtr.toInt()) as ObservableValue<Any?>
                                        is ULong -> SimpleLongProperty(rawPtr.toLong()) as ObservableValue<Any?>
                                        else -> SimpleStringProperty(rawPtr.toString()) as ObservableValue<Any?>
                                    }
                                }
                            } catch (e: Throwable) {
                                e.printStackTrace()
                                SimpleStringProperty("<err>") as ObservableValue<Any?>
                            }
                        } else if (columnInfo.table == LookupType.REVERSE_TARGET) {
                            try {
                                val reverseLookupItem: Any? = targetValue.getStub().computeReverseLookup(
                                    sourceClazz,
                                    columnInfo.ordinal,
                                    returnType,
                                    returnIsPlural
                                )

                                if (reverseLookupItem is List<*>) {
                                    SimpleStringProperty("${reverseLookupItem.size}") as ObservableValue<Any?>
                                } else if (reverseLookupItem == null) {
                                    SimpleStringProperty("") as ObservableValue<Any?>
                                } else if (reverseLookupItem is WinMdObject) {
                                    SimpleIntegerProperty(reverseLookupItem.getRowNumber()) as ObservableValue<Any?>
                                } else {
                                    TODO()
                                }
                            } catch (e: Throwable) {
                                e.printStackTrace()
                                SimpleStringProperty("<err>") as ObservableValue<Any?>
                            }
                        } else {
                            SimpleStringProperty("<todo>") as ObservableValue<Any?>
                        }
                    }

                    column.setCellFactory {
                        JumpCell(
                            navigator,
                            sourceClazz,
                            type,
                            returnType,
                        ) { type, ptr ->
                            println("foose $type->$ptr")
                        } as TableCell<WinMdObject, Any?>
                    }
                }

                WinMdCompositeReference::class.isSuperclassOf(returnType) -> {
                    val sourceClazz = getClassInterface(type)
                    column.setCellValueFactory { r ->
                        val targetValue = r.value

                        val colOrdinal = columnInfo!!.ordinal
                        if (columnInfo.table == LookupType.TARGET || columnInfo.table == LookupType.TARGET_LIST) {
                            try {
                                val rawPtr = targetValue.getStub().getObjectTableValue(type, colOrdinal)
                                val typeAndRow = navigator.calculateInterfacePtr(
                                    returnType as KClass<out WinMdCompositeReference>,
                                    rawPtr
                                )
                                if (typeAndRow == null) {
                                    SimpleStringProperty("") as ObservableValue<Any?>
                                } else if (typeAndRow.second < 1 || typeAndRow.second > navigator.getCount(
                                        getObjectType(
                                            typeAndRow.first
                                        ).objectType
                                    )
                                ) {
                                    SimpleStringProperty("<oob>") as ObservableValue<Any?>
                                } else {
                                    when (rawPtr) {
                                        is UShort -> SimpleIntegerProperty(rawPtr.toInt()) as ObservableValue<Any?>
                                        is UInt -> SimpleIntegerProperty(rawPtr.toInt()) as ObservableValue<Any?>
                                        is ULong -> SimpleLongProperty(rawPtr.toLong()) as ObservableValue<Any?>
                                        else -> SimpleStringProperty(rawPtr.toString()) as ObservableValue<Any?>
                                    }
                                }
                            } catch (e: Throwable) {
                                e.printStackTrace()
                                SimpleStringProperty("<err>") as ObservableValue<Any?>
                            }
                        } else if (columnInfo.table == LookupType.REVERSE_TARGET) {
                            try {
                                val reverseLookupItem = targetValue.getStub().computeReverseLookup(
                                    getClassInterface(type),
                                    columnInfo.ordinal,
                                    returnType,
                                    returnIsPlural
                                )

                                if (reverseLookupItem == null) {
                                    SimpleStringProperty("") as ObservableValue<Any?>
                                } else {
                                    val item = if (reverseLookupItem is List<*>) {
                                        reverseLookupItem[0]
                                    } else {
                                        reverseLookupItem
                                    }

                                    item as WinMdObject
                                    val bitsetIndex =
                                        getObjectType(item::class as KClass<out WinMdObject>).objectType.bitSetIndex
                                    val remotePtr = item.getRowNumber() or (bitsetIndex shl 24)

                                    SimpleIntegerProperty(remotePtr) as ObservableValue<Any?>
                                }
                            } catch (e: Throwable) {
                                e.printStackTrace()
                                SimpleStringProperty("<err>") as ObservableValue<Any?>
                            }
                        } else {
                            SimpleStringProperty("<???>") as ObservableValue<Any?>
                        }
                    }

                    column.setCellFactory {
                        JumpCell(
                            navigator,
                            sourceClazz,
                            null,
                            returnType,
                        ) { type, ptr ->
                            println("foose $type->$ptr")
                        } as TableCell<WinMdObject, Any?>
                    }
                }

                else -> {
                    println("No handler for $returnType")
                }
            }

            column
        })

        metadataTableView.items = SortedList(
            TableListWrapper(TableList(navigator, objectMapper, getClassInterface(type), type))
        )
        (metadataTableView.items as SortedList).comparatorProperty().bind(metadataTableView.comparatorProperty())
    }

    private fun convertFunctionNameToColumnName(name: String): Pair<String, Int>? {
        if (EXCLUDED_FUNCTIONS.contains(name)) {
            return null
        }

        return when (name) {
            "getRowNumber" -> "RID" to 0
            "getToken" -> "Token" to 1
            "getOffset" -> "Offset" to 2

            "getAttributes" -> "Attrib" to 3

            "getParent" -> "Parent" to 4
            "getPropertyMap" -> "Parent" to 4
            "getEventMap" -> "Parent" to 4
            "getEnclosingType" -> "Parent" to 4
            "getTypeDefinition" -> "TypeDef" to 4
            "getExtends" -> "Extends" to 4
            "getEventType" -> "EventType" to 4
            "getField" -> "Field" to 6
            "getResolutionScope" -> "ResScope" to 7
            "getRva" -> "RVA" to 8

            "getImplementationAttributes" -> "Impl. Att." to 21
            "getFlags" -> "Flags" to 22

            "getName" -> "Name" to 28
            "getNamespace" -> "Namespace" to 29

            "getVersion" -> "Version" to 30
            "getBaseGenerationId" -> "BaseGen." to 31
            "getGeneration" -> "Gen." to 31
            "getGenerationId" -> "GenId" to 31
            "getMVid" -> "MVid" to 31

            "getSignature" -> "Signature" to 50
            "getSequence" -> "Sequence" to 51

            "getParameters" -> "Parameters" to 60
            "getGenericParameters" -> "GenericP." to 61
            "getInterface" -> "Iface" to 62
            "getInterfaceDecl" -> "IfaceDec." to 62
            "getInterfaceImpl" -> "IfaceImpl" to 63
            "getFields" -> "Fields" to 64
            "getMethodImplementation" -> "MethodImpl" to 65

            "getChildTypeReferences" -> "ChildTypes" to 70
            "getSubTypes" -> "SubTypes" to 71
            "getMethods" -> "Methods" to 73
            "getConstructor" -> "Ctor" to 74
            "getAttribute" -> "Attribute" to 75
            "getMethod" -> "Method" to 76
            "getAssociation" -> "Assoc." to 77
            "getImplementation" -> "Impl." to 78

            "getNestedClass" -> "Child" to 80
            "getAsToken" -> "Token" to 90
            "getType" -> "Type" to 92
            "getValue" -> "Value" to 93
            "getClassSize" -> "ClzSize" to 94
            "getPackingSize" -> "PkgSize" to 95
            "getImportName" -> "ImportNm." to 96
            "getTargetScope" -> "TrgtScope" to 97
            "getProperties" -> "Properties" to 98

            "getEvents" -> "Events" to 100
            "getPropertyMaps" -> "PropMap" to 101
            "getEventMap" -> "EvMap" to 102
            "getImplementationMap" -> "ImplMap" to 103
            "getSecurityAttribute" -> "Sec." to 104
            "getCustomAttribute" -> "Cust." to 105
            "getFieldMarshal" -> "FieldMarshal" to 106
            "getMemberReference" -> "MemberRef" to 107
            "getMemberForwarded" -> "MemberFwd" to 108
            "getAnonymousBlob" -> "BLOB" to 109
            "getMethodSemantics" -> "Semantics" to 110
            "getConstant" -> "Constant" to 112
            "getMethodBody" -> "MethodBody" to 113
            "getMethodDeclaration" -> "MethodDecl" to 114
            "getMethodImplementations" -> "MethodImpl" to 115

            "getHashAlgorithmId" -> "HashAl." to 120
            "getCulture" -> "Culture" to 121
            "getFieldOffset" -> "FldOffset" to 121
            "getOwner" -> "Owner" to 122
            "getNumber" -> "Number" to 123
            "getAction" -> "Action" to 124
            "getPermissionSet" -> "Permissions" to 125

            "getAsPublicKey" -> "PublicKey" to 130
            "getPublicKey" -> "PublicKey" to 131
            "getNativeType" -> "NativeType" to 132
            "getSignatureRaw" -> "SignatureRaw" to 133

            "isNested" -> null
            "isRtSpecialName" -> null

            else -> name to 99
        }
    }

    @FXML
    fun assemblyViewOnMouseClick(mouseEvent: MouseEvent) {
        val items = assemblyView.selectionModel.selectedItems
        if (items.isNotEmpty()) {
            val selected = items.get(0)
            selected.value.actions.firstOrNull { it.actionText == "selected" }?.action?.invoke()
        }
    }


    @FXML
    fun assemblyViewKeyPressed(keyEvent: KeyEvent) {
        val items = assemblyView.selectionModel.selectedItems
        if (items.isNotEmpty()) {
            val selected = items.get(0)
            selected.value.actions.firstOrNull { it.actionText == "selected" }?.action?.invoke()
        }
    }

    @FXML
    fun assemblyViewKeyReleased(keyEvent: KeyEvent) {
    }

    @FXML
    fun assemblyViewKeyTyped(keyEvent: KeyEvent) {
        val items = assemblyView.selectionModel.selectedItems
        if (items.isNotEmpty()) {
            val selected = items.get(0)
            selected.value.actions.firstOrNull { it.actionText == "selected" }?.action?.invoke()
        }
    }

    @FXML
    fun quitApplicationAction() {
        getWindow().close()
    }
}

class TableListWrapper<T : WinMdObject>(list: TableList<T>) : ObservableListWrapper<T>(list)

class TableList<T : WinMdObject>(
    val navigator: WinMdNavigator,
    val objectMapper: MdObjectMapper,
    val clazz: KClass<T>,
    val type: CLRMetadataType,
) : AbstractList<T>() {
    override val size: Int = navigator.getCount(type)
    private val cursor = objectMapper.getCursor(clazz.java)

    override fun get(index: Int): T {
        // TODO reuse fixed object and float the row number instead?
        return cursor.get(index + 1)
    }
}
