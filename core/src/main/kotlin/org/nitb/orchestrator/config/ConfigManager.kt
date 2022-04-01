package org.nitb.orchestrator.config

import java.io.File
import java.io.FileInputStream
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
        return properties.getProperty(key) ?: orElse
    }

    @Throws(Throwable::class)
    fun getProperty(key: String, orElse: Throwable): String {
        return properties.getProperty(key) ?: throw orElse
    }

    // endregion

    // region BOOLEAN PROPERTIES

    fun getBoolean(key: String): Boolean {
        return properties.getProperty(key)?.toBooleanStrictOrNull() ?: false
    }

    fun getBoolean(key: String, orElse: Boolean): Boolean {
        return properties.getProperty(key)?.toBooleanStrictOrNull() ?: orElse
    }

    @Throws(Throwable::class)
    fun getBoolean(key: String, orElse: Throwable): Boolean {
        return properties.getProperty(key)?.toBooleanStrictOrNull() ?: throw orElse
    }

    // endregion

    // region NUMERIC PROPERTIES

    fun getByte(key: String): Byte? {
        return properties.getProperty(key)?.toByteOrNull()
    }

    fun getByte(key: String, orElse: Byte): Byte {
        return properties.getProperty(key)?.toByteOrNull() ?: orElse
    }

    @Throws(Throwable::class)
    fun getByte(key: String, orElse: Throwable): Byte {
        return properties.getProperty(key)?.toByteOrNull() ?: throw orElse
    }

    fun getShort(key: String): Short? {
        return properties.getProperty(key)?.toShortOrNull()
    }

    fun getShort(key: String, orElse: Short): Short {
        return properties.getProperty(key)?.toShortOrNull() ?: orElse
    }

    @Throws(Throwable::class)
    fun getShort(key: String, orElse: Throwable): Short {
        return properties.getProperty(key)?.toShortOrNull() ?: throw orElse
    }

    fun getInt(key: String): Int? {
        return properties.getProperty(key)?.toIntOrNull()
    }

    fun getInt(key: String, orElse: Int): Int {
        return properties.getProperty(key)?.toIntOrNull() ?: orElse
    }

    @Throws(Throwable::class)
    fun getInt(key: String, orElse: Throwable): Int {
        return properties.getProperty(key)?.toIntOrNull() ?: throw orElse
    }

    fun getLong(key: String): Long? {
        return properties.getProperty(key)?.toLongOrNull()
    }

    fun getLong(key: String, orElse: Long): Long {
        return properties.getProperty(key)?.toLongOrNull() ?: orElse
    }

    @Throws(Throwable::class)
    fun getLong(key: String, orElse: Throwable): Long {
        return properties.getProperty(key)?.toLongOrNull() ?: throw orElse
    }

    fun getFloat(key: String): Float? {
        return properties.getProperty(key)?.toFloatOrNull()
    }

    fun getFloat(key: String, orElse: Float): Float {
        return properties.getProperty(key)?.toFloatOrNull() ?: orElse
    }

    @Throws(Throwable::class)
    fun getFloat(key: String, orElse: Throwable): Float {
        return properties.getProperty(key)?.toFloatOrNull() ?: throw orElse
    }

    fun getDouble(key: String): Double? {
        return properties.getProperty(key)?.toDoubleOrNull()
    }

    fun getDouble(key: String, orElse: Double): Double {
        return properties.getProperty(key)?.toDoubleOrNull() ?: orElse
    }

    @Throws(Throwable::class)
    fun getDouble(key: String, orElse: Throwable): Double {
        return properties.getProperty(key)?.toDoubleOrNull() ?: throw orElse
    }

    fun getBigInt(key: String): BigInteger? {
        return properties.getProperty(key)?.toBigIntegerOrNull()
    }

    fun getBigInt(key: String, orElse: BigInteger): BigInteger {
        return properties.getProperty(key)?.toBigIntegerOrNull() ?: orElse
    }

    @Throws(Throwable::class)
    fun getBigInt(key: String, orElse: Throwable): BigInteger {
        return properties.getProperty(key)?.toBigIntegerOrNull() ?: throw orElse
    }

    fun getBigDecimal(key: String): BigDecimal? {
        return properties.getProperty(key)?.toBigDecimalOrNull()
    }

    fun getBigDecimal(key: String, orElse: BigDecimal): BigDecimal {
        return properties.getProperty(key)?.toBigDecimalOrNull() ?: orElse
    }

    @Throws(Throwable::class)
    fun getBigDecimal(key: String, orElse: Throwable): BigDecimal {
        return properties.getProperty(key)?.toBigDecimalOrNull() ?: throw orElse
    }

    // endregion

    // region LIST PROPERTIES

    fun getProperties(key: String, separator: String = ","): List<String> {
        return properties.getProperty(key)?.split(separator) ?: listOf()
    }

    fun getBooleans(key: String, separator: String = ","): List<Boolean?> {
        return properties.getProperty(key)?.split(separator)?.map { it.toBooleanStrictOrNull() } ?: listOf()
    }

    fun getBytes(key: String, separator: String = ","): List<Byte?> {
        return properties.getProperty(key)?.split(separator)?.map { it.toByteOrNull() } ?: listOf()
    }

    fun getShorts(key: String, separator: String = ","): List<Short?> {
        return properties.getProperty(key)?.split(separator)?.map { it.toShortOrNull() } ?: listOf()
    }

    fun getInts(key: String, separator: String = ","): List<Int?> {
        return properties.getProperty(key)?.split(separator)?.map { it.toIntOrNull() } ?: listOf()
    }

    fun getLongs(key: String, separator: String = ","): List<Long?> {
        return properties.getProperty(key)?.split(separator)?.map { it.toLongOrNull() } ?: listOf()
    }

    fun getFloats(key: String, separator: String = ","): List<Float?> {
        return properties.getProperty(key)?.split(separator)?.map { it.toFloatOrNull() } ?: listOf()
    }

    fun getDoubles(key: String, separator: String = ","): List<Double?> {
        return properties.getProperty(key)?.split(separator)?.map { it.toDoubleOrNull() } ?: listOf()
    }

    fun getBigInts(key: String, separator: String = ","): List<BigInteger?> {
        return properties.getProperty(key)?.split(separator)?.map { it.toBigIntegerOrNull() } ?: listOf()
    }

    fun getBigDecimals(key: String, separator: String = ","): List<BigDecimal?> {
        return properties.getProperty(key)?.split(separator)?.map { it.toBigDecimalOrNull() } ?: listOf()
    }

    // endregion

    // region ENUM PROPERTIES

    @Suppress("UNCHECKED_CAST")
    fun <T: Enum<T>> getEnumProperty(key: String, enumClass: Class<T>): T? {
        return properties.getProperty(key)?.let { enumClass.getMethod("valueOf", String::class.java).invoke(null, it) as T }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T: Enum<T>> getEnumProperty(key: String, enumClass: Class<T>, orElse: T): T {
        return properties.getProperty(key)?.let { enumClass.getMethod("valueOf", String::class.java).invoke(null, it) as T } ?: orElse
    }

    @Throws(Throwable::class)
    @Suppress("UNCHECKED_CAST")
    fun <T: Enum<T>> getEnumProperty(key: String, enumClass: Class<T>, orElse: Throwable): T {
        return properties.getProperty(key)?.let { enumClass.getMethod("valueOf", String::class.java).invoke(null, it) as T } ?: throw orElse
    }
    
    // endregion
    
    // endregion

    // region PRIVATE PROPERTIES

    private val properties = Properties()

    // endregion

    // region INIT

    init {
        val isEnv = System.getProperty("config-env") == "true"
        val configFile = System.getProperty("config-file")?.let { if (isEnv) System.getenv(it) else it } ?: "config.properties"

        if (File(configFile).exists()) {
            FileInputStream(File(configFile)).use { fileInputStream ->
                properties.load(fileInputStream)
            }
        }
    }

    // endregion
}