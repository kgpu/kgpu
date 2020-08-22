package model

import io.github.kgpu.kcgmath.Vec3

/**
 * Represents an obj model
 */
class Model(text: String) {

    val vertices: MutableList<Vec3> = mutableListOf()
    val indices: MutableList<Int> = mutableListOf()

    init {
        for (line in text.lines()) {
            if (line.startsWith("#")) {
                continue
            }

            val command = line.split(" ")

            when (command.firstOrNull()) {
                "v" -> addVertex(command)
                "f" -> addFace(command)
            }
        }
    }

    private fun addFace(command: List<String>) {
        fun parseIndex(text: String) : Int{
            val index = text.split("/").firstOrNull() ?: text

            return index.toIntOrNull() ?: throw ModelLoadException("Failed to parse int: $index")
        }

        if (command.size != 4)
            throw ModelLoadException(
                "face did not have 3 arguments. Line: ${command.joinToString(" ")}"
            )

        indices.add(parseIndex(command[1]) - 1)
        indices.add(parseIndex(command[2]) - 1)
        indices.add(parseIndex(command[3]) - 1)
    }

    private fun addVertex(command: List<String>) {
        if (command.size != 4)
            throw ModelLoadException(
                "Vertex did not have 3 arguments. Line: ${command.joinToString(" ")}"
            )

        val vertex = Vec3()
        vertex.x = parseFloat(command[1])
        vertex.y = parseFloat(command[2])
        vertex.z = -parseFloat(command[3])

        vertices.add(vertex)
    }

    private fun parseFloat(text: String): Float {
        return text.toFloatOrNull() ?: throw ModelLoadException("Failed to parse float: $text")
    }

    override fun toString(): String {
        return """
            Obj Model {
                vertices: [${vertices.joinToString()}],
                indices: [${indices.joinToString()}]
            }
        """.trimIndent()
    }

    fun getVertexArray() : FloatArray{
        val out = FloatArray(vertices.size * 3);

       vertices.forEachIndexed{ index, it ->
           out[index * 3] = it.x
           out[index * 3 + 1] = it.y
           out[index * 3 + 2] = it.z
       }

        return out
    }
}

class ModelLoadException(message: String) : RuntimeException(message)
