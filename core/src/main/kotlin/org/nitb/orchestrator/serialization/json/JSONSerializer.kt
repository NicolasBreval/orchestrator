package org.nitb.orchestrator.serialization.json

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

object JSONSerializer {
    private val jacksonMapper: ObjectMapper = jacksonObjectMapper()

    init {
        jacksonMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
    }

    @Synchronized
    fun serializeWithClassName(obj: Any): String {
        val node = jacksonMapper.convertValue(obj, JsonNode::class.java)
        (node as ObjectNode).put("className", obj::class.java.name)
        return node.toString()
    }

    @Synchronized
    fun deserializeWithClassName(content: String): Any {
        val node = jacksonMapper.readValue(content, JsonNode::class.java)
        val clazz = Class.forName((node as ObjectNode).get("className").asText())
        node.remove("className")
        return jacksonMapper.readValue(node.toString(), clazz)
    }

    @Synchronized
    fun serialize(obj: Any): String {
        return jacksonMapper.writeValueAsString(obj)
    }

    @Synchronized
    fun <T> deserialize(content: String, clazz: Class<T>): T {
        return jacksonMapper.readValue(content, clazz)
    }

    @Synchronized
    fun getSchema(clazz: Class<*>, pretty: Boolean = false): String? {
        val schemaGenerator = JsonSchemaGenerator(jacksonMapper)

        return if (pretty)
            jacksonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(schemaGenerator.generateSchema(clazz))
        else
            jacksonMapper.writeValueAsString(schemaGenerator.generateSchema(clazz))
    }
}