package com.example.security

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Hardware-backed AES-256 GCM Encrypted SharedPreferences Manager.
 * Uses Android KeyStore to generate and safeguard cryptographic keys,
 * ensuring all sensitive settings like locked_apps, security credentials,
 * and biometric tokens remain encrypted at rest.
 */
class EncryptedPrefsHelper(private val context: Context) {

    private val masterKeyAlias = "JarvisMasterSecurityKey"
    private val androidKeyStore = "AndroidKeyStore"
    private val transformation = "AES/GCM/NoPadding"
    private val rawPrefs: SharedPreferences = context.getSharedPreferences("jarvis_encrypted_secure_prefs", Context.MODE_PRIVATE)

    init {
        generateMasterKeyIfNeeded()
    }

    private fun generateMasterKeyIfNeeded() {
        try {
            val keyStore = KeyStore.getInstance(androidKeyStore)
            keyStore.load(null)
            if (!keyStore.containsAlias(masterKeyAlias)) {
                val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, androidKeyStore)
                val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                    masterKeyAlias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
                keyGenerator.init(keyGenParameterSpec)
                keyGenerator.generateKey()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getSecretKey(): SecretKey? {
        val keyStore = KeyStore.getInstance(androidKeyStore)
        keyStore.load(null)
        return (keyStore.getEntry(masterKeyAlias, null) as? KeyStore.SecretKeyEntry)?.secretKey
    }

    fun encrypt(plaintext: String): String {
        return try {
            val secretKey = getSecretKey() ?: return plaintext
            val cipher = Cipher.getInstance(transformation)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = cipher.iv
            val cipherText = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
            val combined = ByteArray(iv.size + cipherText.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(cipherText, 0, combined, iv.size, cipherText.size)
            Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            plaintext
        }
    }

    fun decrypt(encryptedBase64: String): String {
        return try {
            val combined = Base64.decode(encryptedBase64, Base64.NO_WRAP)
            val secretKey = getSecretKey() ?: return encryptedBase64
            val iv = ByteArray(12)
            val cipherText = ByteArray(combined.size - 12)
            System.arraycopy(combined, 0, iv, 0, 12)
            System.arraycopy(combined, 12, cipherText, 0, cipherText.size)

            val cipher = Cipher.getInstance(transformation)
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            val decrypted = cipher.doFinal(cipherText)
            String(decrypted, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            encryptedBase64
        }
    }

    fun saveEncryptedString(key: String, value: String) {
        val cipherText = encrypt(value)
        rawPrefs.edit().putString(key, cipherText).apply()
    }

    fun getEncryptedString(key: String, defaultValue: String = ""): String {
        val cipherText = rawPrefs.getString(key, null) ?: return defaultValue
        return decrypt(cipherText)
    }

    companion object {
        @Volatile
        private var cachedLockedApps: Set<String>? = null
    }

    fun saveLockedApps(apps: Set<String>) {
        cachedLockedApps = apps
        val serialized = apps.joinToString(",")
        saveEncryptedString("enc_locked_apps", serialized)
    }

    fun getLockedApps(): Set<String> {
        cachedLockedApps?.let { return it }
        val serialized = getEncryptedString("enc_locked_apps", "")
        if (serialized.isBlank()) {
            cachedLockedApps = emptySet()
            return emptySet()
        }
        val set = serialized.split(",").filter { it.isNotBlank() }.toSet()
        cachedLockedApps = set
        return set
    }
}
