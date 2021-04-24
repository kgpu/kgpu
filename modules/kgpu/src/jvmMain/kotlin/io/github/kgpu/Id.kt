package io.github.kgpu

import jdk.incubator.foreign.Addressable
import jdk.incubator.foreign.MemoryAddress

/**
 * Represents a Wgpu ID
 *
 * Bits 0-32 are the index Bits 32-61 are the epoch Bits 61-64 are the backend
 */
class Id(val id: Long) : Addressable {

    constructor(addressable: Addressable) : this(addressable.address().toRawLongValue())

    override fun toString(): String {
        val epoch = id shr 32 and ((1 shl 29) - 1)
        val backendNum = id shr 61
        val index = id.toInt()

        val backend =
            when (backendNum) {
                0L -> "Empty"
                1L -> "Vulkan"
                2L -> "Metal"
                3L -> "Dx12"
                4L -> "Dx11"
                5L -> "OpenGl"
                else -> {
                    "UnknownBackend($backendNum)"
                }
            }

        return "($index, $epoch, $backend)"
    }

    override fun address(): MemoryAddress {
        return MemoryAddress.ofLong(id)
    }
}
