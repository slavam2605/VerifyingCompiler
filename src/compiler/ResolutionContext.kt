package compiler

import compiler.resolved.FunctionDescriptor
import compiler.resolved.ResolvedDescriptor

class ResolutionContext {
    private val layers = mutableListOf(
        ContextLayer() // top-level declarations
    )
    private var currentFunction: FunctionDescriptor? = null

    val currentFunctionNotNull
        get() = currentFunction!!

    fun <T> withLayer(block: () -> T): T {
        addLayer()
        return try {
            block()
        } finally {
            dropLayer()
        }
    }

    fun <T> withFunctionLayer(descriptor: FunctionDescriptor, block: () -> T): T {
        currentFunction = descriptor
        return try {
            withLayer {
                descriptor.parameters.forEach { parameter ->
                    addDeclaration(parameter.name, parameter)
                }

                block()
            }
        } finally {
            currentFunction = null
        }
    }

    fun addDeclaration(name: String, descriptor: ResolvedDescriptor) {
        val lastLayer = layers.last()
        if (lastLayer.symbols.containsKey(name)) {
            throw CompilationException("Declaration with name = $name already exists at current layer")
        }
        lastLayer.symbols[name] = descriptor
    }

    fun findDeclaration(name: String): ResolvedDescriptor? {
        layers.asReversed().forEach { layer ->
            val descriptor = layer.symbols[name]
            if (descriptor != null) {
                return descriptor
            }
        }
        return null
    }

    private fun addLayer() {
        layers.add(ContextLayer())
    }

    private fun dropLayer() {
        layers.removeAt(layers.lastIndex)
    }

    private class ContextLayer {
        val symbols = mutableMapOf<String, ResolvedDescriptor>()
    }
}