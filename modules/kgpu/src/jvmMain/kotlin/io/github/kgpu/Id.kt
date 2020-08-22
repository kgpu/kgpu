package io.github.kgpu

/**
 * Represents a Wgpu ID
 *
 * Bits 0-32 are the index
 * Bits 32-61 are the epoch
 * Bits 61-64 are the backend
 */
class Id(val index: Int, val epoch: Int, val backend: Int) {

    companion object {
        fun fromLong(input: Long) : Id{
            val backend = input shr 61;
            val epoch = input shr 32 and ((1 shl 29) - 1);

            return Id(input.toInt(), epoch.toInt(), backend.toInt());
        }
    }

    override fun toString(): String {
        val backend = when(backend){
            0 -> "Empty"
            1 -> "Vulkan"
            2 -> "Metal"
            3 -> "Dx12"
            4 -> "Dx11"
            5 -> "OpenGl"

            else -> {
                "UnknownBackend($backend)"
            }
        }

        return "($index, $epoch, $backend)"
    }
}