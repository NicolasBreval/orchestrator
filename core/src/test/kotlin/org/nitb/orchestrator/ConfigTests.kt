package org.nitb.orchestrator

import org.junit.Test
import org.nitb.orchestrator.config.ConfigManager
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.test.assertEquals
import kotlin.test.assertNull

enum class TestEnum {
    ONE,TWO,THREE,FOUR,FIVE
}

class ConfigTests {

    companion object {
        init {
            // First, cleans properties to ensure that no unnecessary properties are listed
            ConfigManager.clearProperties()

            ConfigManager.setProperties(mapOf(
                "string.property" to "stringProperty",
                "boolean.property" to "false",
                "byte.property.hex" to "0xF",
                "byte.property.dec" to "15",
                "byte.property.over" to "200",
                "short.property.hex" to "0x45",
                "short.property.dec" to "200",
                "short.property.over" to "32768",
                "int.property.hex" to "0x4566",
                "int.property.dec" to "2147483647",
                "int.property.over" to "2147483648",
                "long.property.hex" to "0x456686984756",
                "long.property.dec" to "9223372036854775807",
                "long.property.over" to "9223372036854775808",
                "float.property" to "3.4028235E38F",
                "float.property.over" to "4.4028235E38F",
                "double.property" to "1.7976931348623157E308",
                "double.property.over" to "2.7976931348623157E308",
                "bigint.property.hex" to "0x456686984756",
                "bigint.property.dec" to "92233720368547758078476905984983968950594",
                "bigdecimal.property" to "2.7976931348623157E308",
                "enum.property" to "ONE",
                "string.list" to "a,b,hello,7",
                "boolean.list" to "true, false,6,   true",
                "byte.list" to "0xF;85940385904;1;3;4",
                "short.list" to "0x45ABCD4ABCD3958ABCD32768",
                "int.list" to "1@45@0x9485A8D@2147483648@21474836499@9489504",
                "long.list" to "34^9223372036854775808^0xAFD39",
                "float.list" to "0.45*0.00000003*4.4028235E38F*3059.0490395E23",
                "double.list" to "2.7976931348623157E308_6.7976931348623157E308_0.4563E45_230493.40593030594039403E34",
                "bigint.list" to "0x456686984756€9999999999999999999999999999999999999999999€0493024950495",
                "bigdecimal.list" to "4.4028235E38F-2.7976931348623157E308",
                "enum.list" to "ONE,TWO,TREE,FOUR,FIVE,SIX"
            ))
        }
    }

    // region STRING PROPERTIES

    @Test
    fun existingProperty() {
        assertEquals(ConfigManager.getProperty("string.property"), "stringProperty")
    }

    @Test
    fun nullableStringProperty() {
        assertNull(ConfigManager.getProperty("non-exists"))
    }

    @Test
    fun defaultStringProperty() {
        assertEquals(ConfigManager.getProperty("non-exists", "default"), "default")
    }

    @Test(expected = IllegalArgumentException::class)
    fun throwableStringProperty() {
        ConfigManager.getProperty("non-exists", IllegalArgumentException())
    }

    // endregion

    // region BOOLEAN PROPERTIES

    @Test
    fun existingBooleanProperty() {
        assertEquals(ConfigManager.getBoolean("boolean.property"), false)
    }

    @Test
    fun defaultBooleanProperty() {
        assertEquals(ConfigManager.getBoolean("non-exists", true), true)
    }

    @Test(expected = IllegalArgumentException::class)
    fun throwableBooleanProperty() {
        ConfigManager.getBoolean("non-exists", IllegalArgumentException())
    }

    // endregion

    // region NUMERIC PROPERTIES

    // region BYTE PROPERTIES

    @Test
    fun existingBytePropertyHex() {
        assertEquals(ConfigManager.getByte("byte.property.hex"), 0xF.toByte())
    }

    @Test
    fun existingBytePropertyDec() {
        assertEquals(ConfigManager.getByte("byte.property.dec"), 15)
    }

    @Test
    fun existingByteOverLimit() {
        assertNull(ConfigManager.getByte("byte.property.over"))
    }


    @Test
    fun nullableByteProperty() {
        assertNull(ConfigManager.getByte("non-exists"))
    }

    @Test
    fun defaultByteProperty() {
        assertEquals(ConfigManager.getByte("non-exists", 0x34), 0x34)
    }

    @Test(expected = IllegalArgumentException::class)
    fun throwableByteProperty() {
        ConfigManager.getByte("non-exists", IllegalArgumentException())
    }

    // endregion

    // region SHORT PROPERTIES

    @Test
    fun existingShortPropertyHex() {
        assertEquals(ConfigManager.getShort("short.property.hex"), 0x45.toShort())
    }

    @Test
    fun existingShortPropertyDec() {
        assertEquals(ConfigManager.getShort("short.property.dec"), 200.toShort())
    }

    @Test
    fun existingShortPropertyOverLimit() {
        assertNull(ConfigManager.getShort("short.property.over"))
    }

    @Test
    fun nullableShortProperty() {
        assertNull(ConfigManager.getShort("non-exists"))
    }

    @Test
    fun defaultShortProperty() {
        assertEquals(ConfigManager.getShort("non-exists", 0x34), 0x34)
    }

    @Test(expected = IllegalArgumentException::class)
    fun throwableShortProperty() {
        ConfigManager.getShort("non-exists", IllegalArgumentException())
    }

    // endregion

    // region INT PROPERTIES

    @Test
    fun existingIntPropertyHex() {
        assertEquals(ConfigManager.getInt("int.property.hex"), 0x4566)
    }

    @Test
    fun existingIntPropertyDec() {
        assertEquals(ConfigManager.getInt("int.property.dec"), 2147483647)
    }

    @Test
    fun existingIntPropertyOverLimit() {
        assertNull(ConfigManager.getInt("int.property.over"))
    }

    @Test
    fun nullableIntProperty() {
        assertNull(ConfigManager.getInt("non-exists"))
    }

    @Test
    fun defaultIntProperty() {
        assertEquals(ConfigManager.getInt("non-exists", 0x34), 0x34)
    }

    @Test(expected = IllegalArgumentException::class)
    fun throwableIntProperty() {
        ConfigManager.getInt("non-exists", IllegalArgumentException())
    }

    // endregion

    // region LONG PROPERTIES

    @Test
    fun existingLongPropertyHex() {
        assertEquals(ConfigManager.getLong("long.property.hex"), 0x456686984756)
    }

    @Test
    fun existingLongPropertyDec() {
        assertEquals(ConfigManager.getLong("long.property.dec"), 9223372036854775807L)
    }

    @Test
    fun existingLongPropertyOverLimit() {
        assertNull(ConfigManager.getLong("long.property.over"))
    }

    @Test
    fun nullableLongProperty() {
        assertNull(ConfigManager.getLong("non-exists"))
    }

    @Test
    fun defaultLongProperty() {
        assertEquals(ConfigManager.getLong("non-exists", 0x34), 0x34)
    }

    @Test(expected = IllegalArgumentException::class)
    fun throwableLongProperty() {
        ConfigManager.getLong("non-exists", IllegalArgumentException())
    }

    // endregion

    // region FLOAT PROPERTIES

    @Test
    fun existingFloatPropertyDec() {
        assertEquals(ConfigManager.getFloat("float.property"), Float.MAX_VALUE)
    }

    @Test
    fun existingFloatPropertyOverLimit() {
        assertNull(ConfigManager.getFloat("float.property.over"))
    }

    @Test
    fun nullableFloatProperty() {
        assertNull(ConfigManager.getFloat("non-exists"))
    }

    @Test
    fun defaultFloatProperty() {
        assertEquals(ConfigManager.getFloat("non-exists", 23.4f), 23.4f)
    }

    @Test(expected = IllegalArgumentException::class)
    fun throwableFloatProperty() {
        ConfigManager.getFloat("non-exists", IllegalArgumentException())
    }

    // endregion

    // region DOUBLE PROPERTIES

    @Test
    fun existingDoublePropertyDec() {
        assertEquals(ConfigManager.getDouble("double.property"), Double.MAX_VALUE)
    }

    @Test
    fun existingDoublePropertyOverLimit() {
        assertNull(ConfigManager.getDouble("double.property.over"))
    }

    @Test
    fun nullableDoubleProperty() {
        assertNull(ConfigManager.getDouble("non-exists"))
    }

    @Test
    fun defaultDoubleProperty() {
        assertEquals(ConfigManager.getDouble("non-exists", 23.4), 23.4)
    }

    @Test(expected = IllegalArgumentException::class)
    fun throwableDoubleProperty() {
        ConfigManager.getDouble("non-exists", IllegalArgumentException())
    }

    // endregion

    // region BIGINTEGER PROPERTIES

    @Test
    fun existingBigIntPropertyHex() {
        assertEquals(ConfigManager.getBigInt("bigint.property.hex"), BigInteger("456686984756", 16))
    }

    @Test
    fun existingBigIntPropertyDec() {
        assertEquals(ConfigManager.getBigInt("bigint.property.dec"), BigInteger("92233720368547758078476905984983968950594"))
    }

    @Test
    fun nullableBigIntProperty() {
        assertNull(ConfigManager.getBigInt("non-exists"))
    }

    @Test
    fun defaultBigIntProperty() {
        assertEquals(ConfigManager.getBigInt("non-exists", BigInteger("8594865")), BigInteger("8594865"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun throwableBigIntProperty() {
        ConfigManager.getBigInt("non-exists", IllegalArgumentException())
    }

    // endregion

    // region BIGDECIMAL PROPERTIES

    @Test
    fun existingBigDecimalPropertyDec() {
        assertEquals(ConfigManager.getBigDecimal("bigdecimal.property"), BigDecimal("2.7976931348623157E308"))
    }

    @Test
    fun nullableBigDecimalProperty() {
        assertNull(ConfigManager.getBigDecimal("non-exists"))
    }

    @Test
    fun defaultBigDecimalProperty() {
        assertEquals(ConfigManager.getBigDecimal("non-exists", BigDecimal("8594865.3455433")), BigDecimal("8594865.3455433"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun throwableBigDecimalProperty() {
        ConfigManager.getBigDecimal("non-exists", IllegalArgumentException())
    }

    // endregion

    // endregion

    // region ENUM PROPERTIES

    @Test
    fun existingEnumProperty() {
        assertEquals(ConfigManager.getEnumProperty("enum.property", TestEnum::class.java), TestEnum.ONE)
    }

    @Test
    fun nullableEnumProperty() {
        assertNull(ConfigManager.getEnumProperty("non-exists", TestEnum::class.java))
    }

    @Test
    fun defaultEnumProperty() {
        assertEquals(ConfigManager.getEnumProperty("non-exists", TestEnum::class.java, TestEnum.FIVE), TestEnum.FIVE)
    }

    @Test(expected = IllegalArgumentException::class)
    fun throwableEnumProperty() {
        ConfigManager.getEnumProperty("non-exists", TestEnum::class.java, IllegalArgumentException())
    }

    // endregion

    // region LIST PROPERTIES

    @Test
    fun existingProperties() {
        assertEquals(ConfigManager.getProperties("string.list").size, 4)
    }

    @Test
    fun emptyProperties() {
        assertEquals(ConfigManager.getProperties("non-exists").size, 0)
    }

    @Test
    fun existingBooleanProperties() {
        assertEquals(ConfigManager.getBooleans("boolean.list").size, 4)
    }

    @Test
    fun emptyBooleanProperties() {
        assertEquals(ConfigManager.getBooleans("non-exists").size, 0)
    }

    @Test
    fun existingBytesProperties() {
        val bytes = ConfigManager.getBytes("byte.list", ";")
        assertEquals(bytes.size, 5)
        assertEquals(bytes.filter { it == null }.size, 1)
        assertEquals(bytes.filterNotNull().size, 4)
    }

    @Test
    fun emptyByteProperties() {
        assertEquals(ConfigManager.getBytes("non-exists").size, 0)
    }

    @Test
    fun existingShortProperties() {
        val shorts = ConfigManager.getShorts("short.list", "ABCD")
        assertEquals(shorts.size, 4)
        assertEquals(shorts.filter { it == null }.size, 1)
        assertEquals(shorts.filterNotNull().size, 3)
    }

    @Test
    fun emptyShortProperties() {
        assertEquals(ConfigManager.getShorts("non-exists").size, 0)
    }

    @Test
    fun existingIntProperties() {
        val ints = ConfigManager.getInts("int.list", "@")
        assertEquals(ints.size, 6)
        assertEquals(ints.filter { it == null }.size, 2)
        assertEquals(ints.filterNotNull().size, 4)
    }

    @Test
    fun emptyIntProperties() {
        assertEquals(ConfigManager.getInts("non-exists").size, 0)
    }

    @Test
    fun existingLongProperties() {
        val longs = ConfigManager.getLongs("long.list", "^")
        assertEquals(longs.size, 3)
        assertEquals(longs.filter { it == null }.size, 1)
        assertEquals(longs.filterNotNull().size, 2)
    }

    @Test
    fun emptyLongProperties() {
        assertEquals(ConfigManager.getLongs("non-exists").size, 0)
    }

    @Test
    fun existingFloatProperties() {
        val floats = ConfigManager.getFloats("float.list", "*")
        assertEquals(floats.size, 4)
        assertEquals(floats.filter { it == null }.size, 1)
        assertEquals(floats.filterNotNull().size, 3)
    }

    @Test
    fun emptyFloatProperties() {
        assertEquals(ConfigManager.getFloats("non-exists").size, 0)
    }

    @Test
    fun existingDoubleProperties() {
        val doubles = ConfigManager.getDoubles("double.list", "_")
        assertEquals(doubles.size, 4)
        assertEquals(doubles.filter { it == null }.size, 2)
        assertEquals(doubles.filterNotNull().size, 2)
    }

    @Test
    fun emptyDoubleProperties() {
        assertEquals(ConfigManager.getDoubles("non-exists").size, 0)
    }

    @Test
    fun existingBigIntProperties() {
        assertEquals(ConfigManager.getBigInts("bigint.list", "€").size, 3)
    }

    @Test
    fun existingBigDecimalProperties() {
        assertEquals(ConfigManager.getBigInts("bigdecimal.list", "-").size, 2)
    }

    @Test
    fun existingEnumProperties() {
        val enums = ConfigManager.getEnums("enum.list", TestEnum::class.java)
        assertEquals(enums.size, 6)
        assertEquals(enums.filter { it == null }.size, 2)
        assertEquals(enums.filterNotNull().size, 4)
    }

    // endregion
}