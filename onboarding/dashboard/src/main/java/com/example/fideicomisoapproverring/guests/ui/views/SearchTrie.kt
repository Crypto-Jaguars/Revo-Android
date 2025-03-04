package com.example.fideicomisoapproverring.guests.ui.views

class TrieNode {
    val children = mutableMapOf<Char, TrieNode>()
    var isEndOfWord = false
}

class Trie {
    private val root = TrieNode()

    fun insert(word: String) {
        var current = root
        for (char in word.lowercase()) {
            current = current.children.getOrPut(char) { TrieNode() }
        }
        current.isEndOfWord = true
    }

    fun search(prefix: String): List<String> {
        var current = root
        val prefix = prefix.lowercase()

        // Navigate to the last node of the prefix
        for (char in prefix) {
            current = current.children[char] ?: return emptyList()
        }

        // Find all words with this prefix
        val results = mutableListOf<String>()
        findAllWords(current, prefix, results)
        return results
    }

    private fun findAllWords(node: TrieNode, prefix: String, results: MutableList<String>) {
        if (node.isEndOfWord) {
            results.add(prefix)
        }

        for ((char, childNode) in node.children) {
            findAllWords(childNode, prefix + char, results)
        }
    }
}