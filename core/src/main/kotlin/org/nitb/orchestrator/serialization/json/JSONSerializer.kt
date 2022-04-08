package org.nitb.orchestrator.serialization.json

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.lang.NullPointerException

/**
 * Serializer object used to convert an object to a JSON and vice-versa.
 */
object JSONSerializer {

    // region PUBLIC METHOD

    /**
     * Serializes object to a JSON object adding a property named *className* with name of object class. This property
     * is used for Subscriptions serialization to make easy future deserialization.
     * @param obj Object to serialize.
     */
    fun serializeWithClassName(obj: Any): String {
        val node = jacksonMapper.get().convertValue(obj, JsonNode::class.java)
        (node as ObjectNode).put("className", obj::class.java.name)
        return node.toString()
    }

    /**
     * Deserializes a JSON string to an object, using a property inside JSON string called *className*.
     * @param content JSON string to deserialize.
     */
    fun deserializeWithClassName(content: String): Any {
        try {
            val mapper = jacksonMapper.get()
            val node = mapper.readValue(content, JsonNode::class.java)
            val clazz = Class.forName((node as ObjectNode).get("className").asText())
            node.remove("className")
            return mapper.readValue(node.toString(), clazz)
        } catch (e: NullPointerException) {
            System.currentTimeMillis()
            throw e
        }
    }

    /**
     * Serializes an object to a JSON string.
     * @param obj Object to serialize.
     */
    fun serialize(obj: Any): String {
        return jacksonMapper.get().writeValueAsString(obj)
    }

    /**
     * Deserializes JSON string to an object, using class to specify target type.
     * @param content JSON string to deserialize.
     * @param clazz Class used to deserialize JSON string.
     */
    fun <T> deserialize(content: String, clazz: Class<T>): T {
        return jacksonMapper.get().readValue(content, clazz)
    }

    /**
     * Obtains JSON schema of a class.
     * @param clazz Class to obtain their schema.
     * @param pretty If is true, serializes object with indenting and new lines.
     */
    fun getSchema(clazz: Class<*>, pretty: Boolean = false): String? {
        val mapper = jacksonMapper.get()
        val schemaGenerator = JsonSchemaGenerator(mapper)

        return if (pretty)
            mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schemaGenerator.generateSchema(clazz))
        else
            mapper.writeValueAsString(schemaGenerator.generateSchema(clazz))
    }

    // endregion

    // region PRIVATE PROPERTIES

    /**
     * Mapper object used to serialize and deserialize objects.
     */
    private val jacksonMapper = ThreadLocal.withInitial {
        val jacksonMapper = jacksonObjectMapper()
        jacksonMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        jacksonMapper
    }

    // endregion
}