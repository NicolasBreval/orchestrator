package org.nitb.orchestrator.config

import java.io.File
import java.io.FileInputStream
import java.lang.reflect.InvocationTargetException
import java.math.BigDecimal
import java.math.BigInteger
import java.util.*

object ConfigManager {

    // region PUBLIC METHODS

    // region STRING PROPERTIES

    fun getProperty(key: String): String? {
        return properties.getProperty(key)
    }

    fun getProperty(key: String, orElse: String): String {
        return getProperty(key) ?: orElse
    }

    @Throws(Throwable::class)
    fun getProperty(key: String, orElse: Throwable): String {
        return getProperty(key) ?: throw orElse
    }

    // endregion

    // region BOOLEAN PROPERTIES

    fun getBoolean(key: String): Boolean {
        return getProperty(key)?.toBooleanStrictOrNull() ?: false
    }

    fun getBoolean(key: String, orElse: Boolean): Boolean {
        return getProperty(key)?.toBooleanStrictOrNull() ?: orElse
    }

    @Throws(Throwable::class)
    fun getBoolean(key: String, orElse: Throwable): Boolean {
        return getProperty(key)?.toBooleanStrictOrNull() ?: throw orElse
    }

    // endregion

    // region NUMERIC PROPERTIES

    // region BYTE PROPERTIES

    fun getByte(key: String): Byte? {
        val value = getProperty(key)?.lowercase()?.trim()
        return toByteOrNull(value)
    }

    fun getByte(key: String, orElse: Byte): Byte {
        return getByte(key) ?: orElse
    }

    @Throws(Throwable::class)
    fun getByte(key: String, orElse: Throwable): Byte {
        return getByte(key) ?: throw orElse
    }

    // endregion

    // region SHORT PROPERTIES

    fun getShort(key: String): Short? {
        val value = getProperty(key)?.lowercase()?.trim()
        return toShortOrNull(value)
    }

    fun getShort(key: String, orElse: Short): Short {
        return getShort(key) ?: orElse
    }

    @Throws(Throwable::class)
    fun getShort(key: String, orElse: Throwable): Short {
        return getShort(key) ?: throw orElse
    }

    // endregion

    // region INT PROPERTIES

    fun getInt(key: String): Int? {
        val value = getProperty(key)?.lowercase()?.trim()
        return toIntOrNull(value)
    }

    fun getInt(key: String, orElse: Int): Int {
        return getInt(key) ?: orElse
    }

    @Throws(Throwable::class)
    fun getInt(key: String, orElse: Throwable): Int {
        return getInt(key) ?: throw orElse
    }

    // endregion

    // region LONG PROPERTIES

    fun getLong(key: String): Long? {
        val value = getProperty(key)?.lowercase()?.trim()
        return toLongOrNull(value)
    }

    fun getLong(key: String, orElse: Long): Long {
        return getLong(key) ?: orElse
    }

    @Throws(Throwable::class)
    fun getLong(key: String, orElse: Throwable): Long {
        return getLong(key)?: throw orElse
    }

    // endregion

    // region FLOAT PROPERTIES

    fun getFloat(key: String): Float? {
        return toFloatOrNull(getProperty(key))
    }

    fun getFloat(key: String, orElse: Float): Float {
        return getProperty(key)?.toFloatOrNull() ?: orElse
    }

    @Throws(Throwable::class)
    fun getFloat(key: String, orElse: Throwable): Float {
        return getProperty(key)?.toFloatOrNull() ?: throw orElse
    }

    // endregion

    // region DOUBLE PROPERTIES

    fun getDouble(key: String): Double? {
        return toDoubleOrNull(getProperty(key))
    }

    fun getDouble(key: String, orElse: Double): Double {
        return getProperty(key)?.toDoubleOrNull() ?: orElse
    }

    @Throws(Throwable::class)
    fun getDouble(key: String, orElse: Throwable): Double {
        return getProperty(key)?.toDoubleOrNull() ?: throw orElse
    }

    // endregion

    // region BIGINTEGER PROPERTIES

    fun getBigInt(key: String): BigInteger? {
        val value = getProperty(key)?.lowercase()?.trim()
        return toBigIntegerOrNull(value)
    }

    fun getBigInt(key: String, orElse: BigInteger): BigInteger {
        return getBigInt(key) ?: orElse
    }

    @Throws(Throwable::class)
    fun getBigInt(key: String, orElse: Throwable): BigInteger {
        return getProperty(key)?.toBigIntegerOrNull() ?: throw orElse
    }

    // endregion

    // region BIGDECIMAL PROPERTIES

    fun getBigDecimal(key: String): BigDecimal? {
        return getProperty(key)?.toBigDecimalOrNull()
    }

    fun getBigDecimal(key: String, orElse: BigDecimal): BigDecimal {
        return getProperty(key)?.toBigDecimalOrNull() ?: orElse
    }

    @Throws(Throwable::class)
    fun getBigDecimal(key: String, orElse: Throwable): BigDecimal {
        return getProperty(key)?.toBigDecimalOrNull() ?: throw orElse
    }

    // endregion

    // endregion

    // region ENUM PROPERTIES

    fun <T: Enum<T>> getEnumProperty(key: String, enumClass: Class<T>): T? {
        return getProperty(key)?.let { toEnumOrNull(it, enumClass) }
    }

    fun <T: Enum<T>> getEnumProperty(key: String, enumClass: Class<T>, orElse: T): T {
        return getEnumProperty(key, enumClass) ?: orElse
    }

    @Throws(Throwable::class)
    fun <T: Enum<T>> getEnumProperty(key: String, enumClass: Class<T>, orElse: Throwable): T {
        return getEnumProperty(key, enumClass) ?: throw orElse
    }

    // endregion

    // region LIST PROPERTIES

    // region STRING PROPERTIES

    fun getProperties(key: String, separator: String = ","): List<String> {
        return getProperty(key)?.split(separator) ?: listOf()
    }

    // endregion

    // region BOOLEAN PROPERTIES

    fun getBooleans(key: String, separator: String = ","): List<Boolean> {
        return getProperty(key)?.split(separator)?.map { it.toBoolean() } ?: listOf()
    }

    // endregion

    // region NUMERIC PROPERTIES

    fun getBytes(key: String, separator: String = ","): List<Byte?> {
        return getProperty(key)?.split(separator)?.map { toByteOrNull(it) } ?: listOf()
    }

    fun getShorts(key: String, separator: String = ","): List<Short?> {
        return getProperty(key)?.split(separator)?.map { toShortOrNull(it) } ?: listOf()
    }

    fun getInts(key: String, separator: String = ","): List<Int?> {
        return getProperty(key)?.split(separator)?.map { toIntOrNull(it) } ?: listOf()
    }

    fun getLongs(key: String, separator: String = ","): List<Long?> {
        return getProperty(key)?.split(separator)?.map { toLongOrNull(it) } ?: listOf()
    }

    fun getFloats(key: String, separator: String = ","): List<Float?> {
        return getProperty(key)?.split(separator)?.map { toFloatOrNull(it) } ?: listOf()
    }

    fun getDoubles(key: String, separator: String = ","): List<Double?> {
        return getProperty(key)?.split(separator)?.map { toDoubleOrNull(it) } ?: listOf()
    }

    fun getBigInts(key: String, separator: String = ","): List<BigInteger?> {
        return getProperty(key)?.split(separator)?.map { toBigIntegerOrNull(it) } ?: listOf()
    }

    fun getBigDecimals(key: String, separator: String = ","): List<BigDecimal?> {
        return getProperty(key)?.split(separator)?.map { it.toBigDecimalOrNull() } ?: listOf()
    }

    // endregion

    // region ENUM PROPERTIES

    fun <T: Enum<T>> getEnums(key: String, enumClass: Class<T>, separator: String = ","): List<T?> {
        return getProperty(key)?.split(separator)?.map { toEnumOrNull(it, enumClass) } ?: listOf()
    }

    // endregion

    // endregion

    // endregion

    // region INTERNAL METHODS

    internal fun clearProperties() {
        properties.clear()
    }

    internal fun setProperties(properties: Map<String, String>) {
        for ((key, value) in properties) {
            this.properties.setProperty(key, value)
        }
    }

    // endregion

    // region PRIVATE PROPERTIES

    private val properties = Properties()

    // endregion

    // region PRIVATE METHODS

    private fun toByteOrNull(value: String?): Byte? {
        val radix = if (value?.startsWith("0x") == true) 16 else 10
        return value?.replace("0x", "")?.toByteOrNull(radix)
    }

    private fun toShortOrNull(value: String?): Short? {
        val radix = if (value?.startsWith("0x") == true) 16 else 10
        return value?.replace("0x", "")?.toShortOrNull(radix)
    }

    private fun toIntOrNull(value: String?): Int? {
        val radix = if (value?.startsWith("0x") == true) 16 else 10
        return value?.replace("0x", "")?.toIntOrNull(radix)
    }

    private fun toLongOrNull(value: String?): Long? {
        val radix = if (value?.startsWith("0x") == true) 16 else 10
        return value?.replace("0x", "")?.toLongOrNull(radix)
    }

    private fun toBigIntegerOrNull(value: String?): BigInteger? {
        val radix = if (value?.startsWith("0x") == true) 16 else 10
        return value?.replace("0x", "")?.toBigIntegerOrNull(radix)
    }

    private fun toFloatOrNull(value: String?): Float? {
        val floatValue = value?.toFloatOrNull()

        return if (floatValue == Float.POSITIVE_INFINITY || floatValue == Float.NEGATIVE_INFINITY) {
            return null
        } else floatValue
    }

    private fun toDoubleOrNull(value: String?): Double? {
        val doubleValue = value?.toDoubleOrNull()

        return if (doubleValue == Double.POSITIVE_INFINITY || doubleValue == Double.NEGATIVE_INFINITY) {
            return null
        } else doubleValue
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T: Enum<T>> toEnumOrNull(value: String, enumClass: Class<T>): T? {
        return try {
            enumClass.getMethod("valueOf", String::class.java).invoke(null, value) as T
        } catch (e: InvocationTargetException) {
            null
        }
    }

    // endregion

    // region INIT

    init {
        val isEnv = System.getProperty("config-env") == "true"
        val configResourcesFile = ConfigManager::class.java.classLoader.getResource("config.properties")?.path
        val configFile = System.getProperty("config-file")?.let { if (isEnv) System.getenv(it) else it } ?: configResourcesFile ?: "config.properties"

        if (File(configFile).exists()) {
            FileInputStream(File(configFile)).use { fileInputStream ->
                properties.load(fileInputStream)
            }
        }
    }

    // endregion
}