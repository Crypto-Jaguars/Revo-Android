package com.example.fideicomisoapproverring.guests.ui.views

class TrieNode {
    val children = mutableMapOf<Char, TrieNode>()
    var isEndOfWord = false
    var suggestions = mutableListOf<String>() // Store suggestions for this node
}

class Trie {
    private val root = TrieNode()

    fun insert(word: String) {
        var currentNode = root
        for (char in word) {
            currentNode = currentNode.children.computeIfAbsent(char) { TrieNode() }
            currentNode.suggestions.add(word) // Add word to suggestions
        }
        currentNode.isEndOfWord = true
    }

    fun search(prefix: String): List<String> {
        var currentNode = root
        for (char in prefix) {
            currentNode = currentNode.children[char] ?: return emptyList()
        }
        return currentNode.suggestions // Return suggestions from the last node
    }
}