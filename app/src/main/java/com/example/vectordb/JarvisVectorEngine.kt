package com.example.vectordb

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.math.sqrt

/**
 * 64-Dimensional Semantic Dense Vector Embedding & Vector Database Engine.
 * Provides on-device vector embedding computation and K-Nearest Neighbor (KNN)
 * cosine similarity search for Memory, Clipboard, Battery telemetry, and Theft logs.
 */
data class VectorRecord(
    val id: String = UUID.randomUUID().toString(),
    val tag: String,
    val text: String,
    val embedding: FloatArray,
    val category: String = "GENERAL",
    val timestamp: Long = System.currentTimeMillis(),
    val metadata: Map<String, String> = emptyMap()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as VectorRecord
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}

class JarvisVectorEngine(private val context: Context) {

    private val vectorStore = mutableListOf<VectorRecord>()
    private val dimension = 64

    /**
     * Compute a 64-dimensional semantic embedding vector for any arbitrary text.
     * Combines character n-gram hashes, word semantic weights, and positional phase factors
     * to ensure cosine similarity accurately groups related texts (passwords, urls, code, battery).
     */
    fun computeEmbedding(text: String): FloatArray {
        val vector = FloatArray(dimension)
        if (text.isBlank()) return vector

        val clean = text.lowercase().trim()
        val tokens = clean.split(Regex("[^a-zA-Z0-9_:/.-]+")).filter { it.isNotEmpty() }

        // Token frequency & feature hashing
        tokens.forEachIndexed { index, token ->
            val hash1 = token.hashCode()
            val hash2 = token.reversed().hashCode()
            val dim1 = (Math.abs(hash1) % dimension)
            val dim2 = (Math.abs(hash2) % dimension)

            val tokenWeight = when {
                token.startsWith("http") || token.contains(".com") || token.contains("/") -> 3.5f
                token.contains("pass") || token.contains("key") || token.contains("secret") || token.contains("pin") -> 4.0f
                token.contains("fun ") || token.contains("val ") || token.contains("class ") || token.contains("{") -> 3.0f
                token.contains("battery") || token.contains("power") || token.contains("drain") || token.contains("%") -> 3.0f
                token.contains("theft") || token.contains("alarm") || token.contains("breach") -> 3.5f
                else -> 1.0f
            }

            vector[dim1] += tokenWeight * (1f + (index.toFloat() / (tokens.size + 1)))
            vector[dim2] += (tokenWeight * 0.5f)
        }

        // Substring 3-gram character projection for structural affinity
        for (i in 0 until clean.length - 2) {
            val tri = clean.substring(i, i + 3)
            val dim = Math.abs(tri.hashCode()) % dimension
            vector[dim] += 0.35f
        }

        // L2 Unit Normalization
        var normSq = 0f
        for (v in vector) normSq += v * v
        val norm = sqrt(normSq)
        if (norm > 0f) {
            for (i in vector.indices) {
                vector[i] /= norm
            }
        }
        return vector
    }

    /**
     * Computes Cosine Similarity between two L2-normalized embedding vectors (-1.0 to 1.0)
     */
    fun cosineSimilarity(v1: FloatArray, v2: FloatArray): Float {
        if (v1.size != v2.size) return 0f
        var dot = 0f
        for (i in v1.indices) {
            dot += v1[i] * v2[i]
        }
        return dot
    }

    /**
     * Store a record into the Vector Database
     */
    fun insert(tag: String, text: String, category: String = "GENERAL", metadata: Map<String, String> = emptyMap()): VectorRecord {
        val embedding = computeEmbedding(text)
        val record = VectorRecord(
            tag = tag,
            text = text,
            embedding = embedding,
            category = category,
            metadata = metadata
        )
        synchronized(vectorStore) {
            if (vectorStore.size >= 250) {
                val toDrop = vectorStore.size - 200
                repeat(toDrop) {
                    if (vectorStore.isNotEmpty()) vectorStore.removeAt(0)
                }
            }
            vectorStore.add(record)
        }
        return record
    }

    /**
     * Search nearest neighbors in Vector DB by text query
     */
    fun search(query: String, topK: Int = 3, minSimilarity: Float = 0.25f): List<Pair<VectorRecord, Float>> {
        val queryEmbedding = computeEmbedding(query)
        val candidates: List<VectorRecord>
        synchronized(vectorStore) {
            candidates = ArrayList(vectorStore)
        }
        return candidates
            .map { record -> Pair(record, cosineSimilarity(queryEmbedding, record.embedding)) }
            .filter { it.second >= minSimilarity }
            .sortedByDescending { it.second }
            .take(topK)
    }

    /**
     * Categorize clipboard text using semantic vector analysis
     */
    fun categorizeClip(text: String): String {
        val t = text.trim()
        val embedding = computeEmbedding(t)

        val linkArchetype = computeEmbedding("https://example.com/login?token=xyz website url link")
        val passArchetype = computeEmbedding("mySecretPassword#2026 PIN pass credentials")
        val codeArchetype = computeEmbedding("fun main() { val x = 42; return x; } public class int import")
        val contactArchetype = computeEmbedding("+1234567890 john.doe@email.com phone call contact")

        val linkSim = cosineSimilarity(embedding, linkArchetype)
        val passSim = cosineSimilarity(embedding, passArchetype)
        val codeSim = cosineSimilarity(embedding, codeArchetype)
        val contactSim = cosineSimilarity(embedding, contactArchetype)

        return when {
            t.startsWith("http://") || t.startsWith("https://") || t.contains("www.") || linkSim > 0.45f -> "LINK"
            t.length in 6..40 && (t.any { it.isDigit() } && t.any { !it.isLetterOrDigit() }) && passSim > 0.35f -> "PASSWORD"
            t.contains("{") || t.contains("class ") || t.contains("fun ") || t.contains("import ") || codeSim > 0.40f -> "CODE"
            t.contains("@") || t.filter { it.isDigit() }.length >= 10 || contactSim > 0.40f -> "CONTACT"
            else -> "NOTE"
        }
    }

    fun getAllRecords(): List<VectorRecord> {
        synchronized(vectorStore) {
            return ArrayList(vectorStore)
        }
    }

    fun getRecordsCount(): Int {
        synchronized(vectorStore) {
            return vectorStore.size
        }
    }
}
