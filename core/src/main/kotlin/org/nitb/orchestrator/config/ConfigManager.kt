package org.nitb.orchestrator.config

import java.io.File
import java.io.FileInputStream
import java.lang.reflect.InvocationTargetException
import java.math.BigDecimal
import java.math.BigInteger
import java.nio.file.Paths
import java.util.*

/**
 * Object used to get properties from a properties file. There are many methods to specify properties file:
 *
 * * **By program arguments**: If program contains a program argument called config-file, file is obtained from path represented as the argument's value.
 * * **By environment variable**: If program contains a program argument called config-env with value *true*, file is obtained from *config-env* environment variable's value.
 * * **By resources**: If program contains a program argument called config-resource-path, file is obtained from path represented as the argument's value.
 * * **By default**: If none of the above options have been indicated, tries to obtain from working directory, with filename as *config.properties*
 */
object ConfigManager {

    // region PUBLIC METHODS

    // region STRING PROPERTIES

    /**
     * Obtains a [String] property from config file, or null if property doesn't exist.
     * @param key Key used to search value in config file.
     * @return Value related to key, or null if property doesn't exist.
     */
    fun getProperty(key: String): String? {
        return properties.getProperty(key)
    }

    /**
     * Obtains a [String] property from config file, or a default value if property doesn't exist.
     * @param key Key used to search value in config file.
     * @param orElse Default value to return if property doesn't exist.
     * @return Value related to key, or default value if property doesn't exist.
     */
    fun getProperty(key: String, orElse: String): String {
        return getProperty(key) ?: orElse
    }

    /**
     * Obtains a [String] property from config file, or throws an exception if property doesn't exist.
     * @param key Key used to search value in config file.
     * @param orElse Throwable object to throw if property doesn't exist.
     * @return Value related to key, or throws an exception if property doesn't exist.
     */
    @Throws(Throwable::class)
    fun getProperty(key: String, orElse: Throwable): String {
        return getProperty(key) ?: throw orElse
    }

    // endregion

    // region BOOLEAN PROPERTIES

    /**
     * Obtains a [Boolean] property from config file, or false if property doesn't exist.
     * @param key Key used to search value in config file.
     * @return Value related to key, or null if property doesn't exist.
     */
    fun getBoolean(key: String): Boolean {
        return getProperty(key)?.toBooleanStrictOrNull() ?: false
    }

    /**
     * Obtains a [Boolean] property from config file, or a default value if property doesn't exist.
     * @param key Key used to search value in config file.
     * @param orElse Default value to return if property doesn't exist.
     * @return Value related to key, or default value if property doesn't exist.
     */
    fun getBoolean(key: String, orElse: Boolean): Boolean {
        return getProperty(key)?.toBooleanStrictOrNull() ?: orElse
    }

    /**
     * Obtains a [Boolean] property from config file, or throws an exception if property doesn't exist.
     * @param key Key used to search value in config file.
     * @param orElse Throwable object to throw if property doesn't exist.
     * @return Value related to key, or throws an exception if property doesn't exist.
     */
    @Throws(Throwable::class)
    fun getBoolean(key: String, orElse: Throwable): Boolean {
        return getProperty(key)?.toBooleanStrictOrNull() ?: throw orElse
    }

    // endregion

    // region NUMERIC PROPERTIES

    // region BYTE PROPERTIES

    /**
     * Obtains a [Byte] property from config file, or null if property doesn't exist.
     * @param key Key used to search value in config file.
     * @return Value related to key, or null if property doesn't exist.
     */
    fun getByte(key: String): Byte? {
        val value = getProperty(key)?.lowercase()?.trim()
        return toByteOrNull(value)
    }

    /**
     * Obtains a [Byte] property from config file, or a default value if property doesn't exist.
     * @param key Key used to search value in config file.
     * @param orElse Default value to return if property doesn't exist.
     * @return Value related to key, or default value if property doesn't exist.
     */
    fun getByte(key: String, orElse: Byte): Byte {
        return getByte(key) ?: orElse
    }

    /**
     * Obtains a [Byte] property from config file, or throws an exception if property doesn't exist.
     * @param key Key used to search value in config file.
     * @param orElse Throwable object to throw if property doesn't exist.
     * @return Value related to key, or throws an exception if property doesn't exist.
     */
    @Throws(Throwable::class)
    fun getByte(key: String, orElse: Throwable): Byte {
        return getByte(key) ?: throw orElse
    }

    // endregion

    // region SHORT PROPERTIES

    /**
     * Obtains a [Short] property from config file, or null if property doesn't exist.
     * @param key Key used to search value in config file.
     * @return Value related to key, or null if property doesn't exist.
     */
    fun getShort(key: String): Short? {
        val value = getProperty(key)?.lowercase()?.trim()
        return toShortOrNull(value)
    }

    /**
     * Obtains a [Short] property from config file, or a default value if property doesn't exist.
     * @param key Key used to search value in config file.
     * @param orElse Default value to return if property doesn't exist.
     * @return Value related to key, or default value if property doesn't exist.
     */
    fun getShort(key: String, orElse: Short): Short {
        return getShort(key) ?: orElse
    }

    /**
     * Obtains a [Short] property from config file, or throws an exception if property doesn't exist.
     * @param key Key used to search value in config file.
     * @param orElse Throwable object to throw if property doesn't exist.
     * @return Value related to key, or throws an exception if property doesn't exist.
     */
    @Throws(Throwable::class)
    fun getShort(key: String, orElse: Throwable): Short {
        return getShort(key) ?: throw orElse
    }

    // endregion

    // region INT PROPERTIES

    /**
     * Obtains a [Int] property from config file, or null if property doesn't exist.
     * @param key Key used to search value in config file.
     * @return Value related to key, or null if property doesn't exist.
     */
    fun getInt(key: String): Int? {
        val value = getProperty(key)?.lowercase()?.trim()
        return toIntOrNull(value)
    }

    /**
     * Obtains a [Int] property from config file, or a default value if property doesn't exist.
     * @param key Key used to search value in config file.
     * @param orElse Default value to return if property doesn't exist.
     * @return Value related to key, or default value if property doesn't exist.
     */
    fun getInt(key: String, orElse: Int): Int {
        return getInt(key) ?: orElse
    }

    /**
     * Obtains a [Int] property from config file, or throws an exception if property doesn't exist.
     * @param key Key used to search value in config file.
     * @param orElse Throwable object to throw if property doesn't exist.
     * @return Value related to key, or throws an exception if property doesn't exist.
     */
    @Throws(Throwable::class)
    fun getInt(key: String, orElse: Throwable): Int {
        return getInt(key) ?: throw orElse
    }

    // endregion

    // region LONG PROPERTIES

    /**
     * Obtains a [Long] property from config file, or null if property doesn't exist.
     * @param key Key used to search value in config file.
     * @return Value related to key, or null if property doesn't exist.
     */
    fun getLong(key: String): Long? {
        val value = getProperty(key)?.lowercase()?.trim()
        return toLongOrNull(value)
    }

    /**
     * Obtains a [Long] property from config file, or a default value if property doesn't exist.
     * @param key Key used to search value in config file.
     * @param orElse Default value to return if property doesn't exist.
     * @return Value related to key, or default value if property doesn't exist.
     */
    fun getLong(key: String, orElse: Long): Long {
        return getLong(key) ?: orElse
    }

    /**
     * Obtains a [Long] property from config file, or throws an exception if property doesn't exist.
     * @param key Key used to search value in config file.
     * @param orElse Throwable object to throw if property doesn't exist.
     * @return Value related to key, or throws an exception if property doesn't exist.
     */
    @Throws(Throwable::class)
    fun getLong(key: String, orElse: Throwable): Long {
        return getLong(key)?: throw orElse
    }

    // endregion

    // region FLOAT PROPERTIES

    /**
     * Obtains a [Float] property from config file, or null if property doesn't exist.
     * @param key Key used to search value in config file.
     * @return Value related to key, or null if property doesn't exist.
     */
    fun getFloat(key: String): Float? {
        return toFloatOrNull(getProperty(key))
    }

    /**
     * Obtains a [Float] property from config file, or a default value if property doesn't exist.
     * @param key Key used to search value in config file.
     * @param orElse Default value to return if property doesn't exist.
     * @return Value related to key, or default value if property doesn't exist.
     */
    fun getFloat(key: String, orElse: Float): Float {
        return getProperty(key)?.toFloatOrNull() ?: orElse
    }

    /**
     * Obtains a [Float] property from config file, or throws an exception if property doesn't exist.
     * @param key Key used to search value in config file.
     * @param orElse Throwable object to throw if property doesn't exist.
     * @return Value related to key, or throws an exception if property doesn't exist.
     */    
    @Throws(Throwable::class)
    fun getFloat(key: String, orElse: Throwable): Float {
        return getProperty(key)?.toFloatOrNull() ?: throw orElse
    }

    // endregion

    // region DOUBLE PROPERTIES

    /**
     * Obtains a [Double] property from config file, or null if property doesn't exist.
     * @param key Key used to search value in config file.
     * @return Value related to key, or null if property doesn't exist.
     */
    fun getDouble(key: String): Double? {
        return toDoubleOrNull(getProperty(key))
    }

    /**
     * Obtains a [Double] property from config file, or a default value if property doesn't exist.
     * @param key Key used to search value in config file.
     * @param orElse Default value to return if property doesn't exist.
     * @return Value related to key, or default value if property doesn't exist.
     */
    fun getDouble(key: String, orElse: Double): Double {
        return getProperty(key)?.toDoubleOrNull() ?: orElse
    }

    /**
     * Obtains a [Double] property from config file, or throws an exception if property doesn't exist.
     * @param key Key used to search value in config file.
     * @param orElse Throwable object to throw if property doesn't exist.
     * @return Value related to key, or throws an exception if property doesn't exist.
     */
    @Throws(Throwable::class)
    fun getDouble(key: String, orElse: Throwable): Double {
        return getProperty(key)?.toDoubleOrNull() ?: throw orElse
    }

    // endregion

    // region BIGINTEGER PROPERTIES

    /**
     * Obtains a [BigInteger] property from config file, or null if property doesn't exist.
     * @param key Key used to search value in config file.
     * @return Value related to key, or null if property doesn't exist.
     */
    fun getBigInt(key: String): BigInteger? {
        val value = getProperty(key)?.lowercase()?.trim()
        return toBigIntegerOrNull(value)
    }

    /**
     * Obtains a [BigInteger] property from config file, or a default value if property doesn't exist.
     * @param key Key used to search value in config file.
     * @param orElse Default value to return if property doesn't exist.
     * @return Value related to key, or default value if property doesn't exist.
     */
    fun getBigInt(key: String, orElse: BigInteger): BigInteger {
        return getBigInt(key) ?: orElse
    }

    /**
     * Obtains a [BigInteger] property from config file, or throws an exception if property doesn't exist.
     * @param key Key used to search value in config file.
     * @param orElse Throwable object to throw if property doesn't exist.
     * @return Value related to key, or throws an exception if property doesn't exist.
     */
    @Throws(Throwable::class)
    fun getBigInt(key: String, orElse: Throwable): BigInteger {
        return getProperty(key)?.toBigIntegerOrNull() ?: throw orElse
    }

    // endregion

    // region BIGDECIMAL PROPERTIES

    /**
     * Obtains a [BigDecimal] property from config file, or null if property doesn't exist.
     * @param key Key used to search value in config file.
     * @return Value related to key, or null if property doesn't exist.
     */
    fun getBigDecimal(key: String): BigDecimal? {
        return getProperty(key)?.toBigDecimalOrNull()
    }

    /**
     * Obtains a [BigDecimal] property from config file, or a default value if property doesn't exist.
     * @param key Key used to search value in config file.
     * @param orElse Default value to return if property doesn't exist.
     * @return Value related to key, or default value if property doesn't exist.
     */
    fun getBigDecimal(key: String, orElse: BigDecimal): BigDecimal {
        return getProperty(key)?.toBigDecimalOrNull() ?: orElse
    }

    /**
     * Obtains a [BigDecimal] property from config file, or throws an exception if property doesn't exist.
     * @param key Key used to search value in config file.
     * @param orElse Throwable object to throw if property doesn't exist.
     * @return Value related to key, or throws an exception if property doesn't exist.
     */
    @Throws(Throwable::class)
    fun getBigDecimal(key: String, orElse: Throwable): BigDecimal {
        return getProperty(key)?.toBigDecimalOrNull() ?: throw orElse
    }

    // endregion

    // endregion

    // region ENUM PROPERTIES

    /**
     * Obtains a [Enum] property from config file, specifying [Enum] type, or null if property doesn't exist.
     * @param key Key used to search value in config file.
     * @param enumClass Class used to convert from string to [Enum] object.
     * @return Value related to key, or null if property doesn't exist.
     */
    fun <T: Enum<T>> getEnumProperty(key: String, enumClass: Class<T>): T? {
        return getProperty(key)?.let { toEnumOrNull(it, enumClass) }
    }

    /**
     * Obtains a [Enum] property from config file, specifying [Enum] type, or a default value if property doesn't exist.
     * @param key Key used to search value in config file.
     * @param enumClass Class used to convert from string to [Enum] object.
     * @param orElse Default value to return if property doesn't exist.
     * @return Value related to key, or default value if property doesn't exist.
     */
    fun <T: Enum<T>> getEnumProperty(key: String, enumClass: Class<T>, orElse: T): T {
        return getEnumProperty(key, enumClass) ?: orElse
    }

    /**
     * Obtains a [Enum] property from config file, specifying [Enum] type, or throws an exception if property doesn't exist.
     * @param key Key used to search value in config file.
     * @param enumClass Class used to convert from string to [Enum] object.
     * @param orElse Throwable object to throw if property doesn't exist.
     * @return Value related to key, or throws an exception if property doesn't exist.
     */
    @Throws(Throwable::class)
    fun <T: Enum<T>> getEnumProperty(key: String, enumClass: Class<T>, orElse: Throwable): T {
        return getEnumProperty(key, enumClass) ?: throw orElse
    }

    // endregion

    // region LIST PROPERTIES

    // region STRING PROPERTIES

    /**
     * Obtains a list of [String] objects, splitting a string by a specified separator.
     * @param key Key used to search value in config file.
     * @param separator Separator string used to divide elements in property value.
     * @return Value related to key, or empty list if property doesn't exist.
     */
    fun getProperties(key: String, separator: String = ","): List<String> {
        return getProperty(key)?.split(separator) ?: listOf()
    }

    // endregion

    // region BOOLEAN PROPERTIES

    /**
     * Obtains a list of [Boolean] objects, splitting a string by a specified separator, and applying same transformation as method to obtain a single value.
     * @param key Key used to search value in config file.
     * @param separator Separator string used to divide elements in property value.
     * @return Value related to key, or empty list if property doesn't exist.
     */
    fun getBooleans(key: String, separator: String = ","): List<Boolean> {
        return getProperty(key)?.split(separator)?.map { it.toBoolean() } ?: listOf()
    }

    // endregion

    // region NUMERIC PROPERTIES

    /**
     * Obtains a list of [Byte] objects, splitting a string by a specified separator, and applying same transformation as method to obtain a single value.
     * @param key Key used to search value in config file.
     * @param separator Separator string used to divide elements in property value.
     * @return Value related to key, or empty list if property doesn't exist.
     */
    fun getBytes(key: String, separator: String = ","): List<Byte?> {
        return getProperty(key)?.split(separator)?.map { toByteOrNull(it) } ?: listOf()
    }

    /**
     * Obtains a list of [Short] objects, splitting a string by a specified separator, and applying same transformation as method to obtain a single value.
     * @param key Key used to search value in config file.
     * @param separator Separator string used to divide elements in property value.
     * @return Value related to key, or empty list if property doesn't exist.
     */
    fun getShorts(key: String, separator: String = ","): List<Short?> {
        return getProperty(key)?.split(separator)?.map { toShortOrNull(it) } ?: listOf()
    }

    /**
     * Obtains a list of [Int] objects, splitting a string by a specified separator, and applying same transformation as method to obtain a single value.
     * @param key Key used to search value in config file.
     * @param separator Separator string used to divide elements in property value.
     * @return Value related to key, or empty list if property doesn't exist.
     */
    fun getInts(key: String, separator: String = ","): List<Int?> {
        return getProperty(key)?.split(separator)?.map { toIntOrNull(it) } ?: listOf()
    }

    /**
     * Obtains a list of [Long] objects, splitting a string by a specified separator, and applying same transformation as method to obtain a single value.
     * @param key Key used to search value in config file.
     * @param separator Separator string used to divide elements in property value.
     * @return Value related to key, or empty list if property doesn't exist.
     */
    fun getLongs(key: String, separator: String = ","): List<Long?> {
        return getProperty(key)?.split(separator)?.map { toLongOrNull(it) } ?: listOf()
    }

    /**
     * Obtains a list of [Float] objects, splitting a string by a specified separator, and applying same transformation as method to obtain a single value.
     * @param key Key used to search value in config file.
     * @param separator Separator string used to divide elements in property value.
     * @return Value related to key, or empty list if property doesn't exist.
     */
    fun getFloats(key: String, separator: String = ","): List<Float?> {
        return getProperty(key)?.split(separator)?.map { toFloatOrNull(it) } ?: listOf()
    }

    /**
     * Obtains a list of [Double] objects, splitting a string by a specified separator, and applying same transformation as method to obtain a single value.
     * @param key Key used to search value in config file.
     * @param separator Separator string used to divide elements in property value.
     * @return Value related to key, or empty list if property doesn't exist.
     */
    fun getDoubles(key: String, separator: String = ","): List<Double?> {
        return getProperty(key)?.split(separator)?.map { toDoubleOrNull(it) } ?: listOf()
    }

    /**
     * Obtains a list of [BigInteger] objects, splitting a string by a specified separator, and applying same transformation as method to obtain a single value.
     * @param key Key used to search value in config file.
     * @param separator Separator string used to divide elements in property value.
     * @return Value related to key, or empty list if property doesn't exist.
     */
    fun getBigInts(key: String, separator: String = ","): List<BigInteger?> {
        return getProperty(key)?.split(separator)?.map { toBigIntegerOrNull(it) } ?: listOf()
    }

    /**
     * Obtains a list of [BigDecimal] objects, splitting a string by a specified separator, and applying same transformation as method to obtain a single value.
     * @param key Key used to search value in config file.
     * @param separator Separator string used to divide elements in property value.
     * @return Value related to key, or empty list if property doesn't exist.
     */
    fun getBigDecimals(key: String, separator: String = ","): List<BigDecimal?> {
        return getProperty(key)?.split(separator)?.map { it.toBigDecimalOrNull() } ?: listOf()
    }

    // endregion

    // region ENUM PROPERTIES

    /**
     * Obtains a list of [Enum] objects, splitting a string by a specified separator, and applying same transformation as method to obtain a single value.
     * @param key Key used to search value in config file.
     * @param enumClass Class used to convert from string to [Enum] object.
     * @param separator Separator string used to divide elements in property value.
     * @return Value related to key, or empty list if property doesn't exist.
     */
    fun <T: Enum<T>> getEnums(key: String, enumClass: Class<T>, separator: String = ","): List<T?> {
        return getProperty(key)?.split(separator)?.map { toEnumOrNull(it, enumClass) } ?: listOf()
    }

    // endregion

    // endregion

    // endregion

    // region INTERNAL METHODS

    /**
     * Removes all properties previously obtained from config file. This method is used only in tests, it's not possible to be called outside module.
     */
    internal fun clearProperties() {
        properties.clear()
    }

    /**
     * Adds new properties to [ConfigManager].
     * @param properties New pairs of key, value to be inserted in [ConfigManager]
     */
    fun setProperties(properties: Map<String, String>) {
        for ((key, value) in properties) {
            this.properties.setProperty(key, value)
        }
    }

    /**
     * Used to get properties file
     */
    fun getPropertiesFileLocation(): String {
        return propertiesFile
    }

    // endregion

    // region PRIVATE PROPERTIES

    /**
     * [Properties] object with all properties obtained from config file.
     */
    private val properties = Properties()

    /**
     * Path of loaded properties file
     */
    private val propertiesFile: String = Paths.get(System.getProperty("config-file")
        ?.let { if (System.getProperty("config-env") == "true") System.getenv("config-file") else it }
        ?: System.getProperty("config-resource-path")?.let { ConfigManager::class.java.classLoader.getResource(it)?.path }
        ?: "config.properties").toAbsolutePath().toString()

    // endregion

    // region PRIVATE METHODS

    /**
     * Transforms a [String] to [Byte]. Checks if value starts with 0x to transform it using 16 radix.
     * @param value Value to be parsed.
     * @return [Byte] parsed from [String], or null if is not possible to parse.
     */
    private fun toByteOrNull(value: String?): Byte? {
        val radix = if (value?.startsWith("0x") == true) 16 else 10
        return value?.replace("0x", "")?.toByteOrNull(radix)
    }

    /**
     * Transforms a [String] to [Short]. Checks if value starts with 0x to transform it using 16 radix.
     * @param value Value to be parsed.
     * @return [Short] parsed from [String], or null if is not possible to parse.
     */
    private fun toShortOrNull(value: String?): Short? {
        val radix = if (value?.startsWith("0x") == true) 16 else 10
        return value?.replace("0x", "")?.toShortOrNull(radix)
    }

    /**
     * Transforms a [String] to [Int]. Checks if value starts with 0x to transform it using 16 radix.
     * @param value Value to be parsed.
     * @return [Int] parsed from [String], or null if is not possible to parse.
     */
    private fun toIntOrNull(value: String?): Int? {
        val radix = if (value?.startsWith("0x") == true) 16 else 10
        return value?.replace("0x", "")?.toIntOrNull(radix)
    }

    /**
     * Transforms a [String] to [Long]. Checks if value starts with 0x to transform it using 16 radix.
     * @param value Value to be parsed.
     * @return [Long] parsed from [String], or null if is not possible to parse.
     */
    private fun toLongOrNull(value: String?): Long? {
        val radix = if (value?.startsWith("0x") == true) 16 else 10
        return value?.replace("0x", "")?.toLongOrNull(radix)
    }

    /**
     * Transforms a [String] to [BigInteger]. Checks if value starts with 0x to transform it using 16 radix.
     * @param value Value to be parsed.
     * @return [BigInteger] parsed from [String], or null if is not possible to parse.
     */
    private fun toBigIntegerOrNull(value: String?): BigInteger? {
        val radix = if (value?.startsWith("0x") == true) 16 else 10
        return value?.replace("0x", "")?.toBigIntegerOrNull(radix)
    }

    /**
     * Transforms a [String] to [Float]. If result is Infinity, positive or negative, returns null.
     * @param value Value to be parsed.
     * @return [Float] parsed from [String], or null if is not possible to parse, or if is Infinite.
     */
    private fun toFloatOrNull(value: String?): Float? {
        val floatValue = value?.toFloatOrNull()

        return if (floatValue == Float.POSITIVE_INFINITY || floatValue == Float.NEGATIVE_INFINITY) {
            return null
        } else floatValue
    }

    /**
     * Transforms a [String] to [Double]. If result is Infinity, positive or negative, returns null.
     * @param value Value to be parsed.
     * @return [Double] parsed from [String], or null if is not possible to parse, or if is Infinite.
     */
    private fun toDoubleOrNull(value: String?): Double? {
        val doubleValue = value?.toDoubleOrNull()

        return if (doubleValue == Double.POSITIVE_INFINITY || doubleValue == Double.NEGATIVE_INFINITY) {
            return null
        } else doubleValue
    }

    /**
     * Transforms a [String] to [Enum]. If is not possible to parse value, returns null.
     * @param value Value to be parsed.
     * @param enumClass [Enum] class used to parse value.
     * @return [Enum] value parsed from [String], or null if is not possible.
     */
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
        if (File(propertiesFile).exists()) {
            FileInputStream(File(propertiesFile)).use { fileInputStream ->
                properties.load(fileInputStream)
            }
        }
        properties.putAll(System.getProperties())
    }

    // endregion
}