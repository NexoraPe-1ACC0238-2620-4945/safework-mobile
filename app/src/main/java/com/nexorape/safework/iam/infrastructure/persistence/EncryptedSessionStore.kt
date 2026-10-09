package com.nexorape.safework.iam.infrastructure.persistence

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.nexorape.safework.core.network.SessionCredential
import com.nexorape.safework.core.network.SessionStore
import com.nexorape.safework.core.network.SessionStorageException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Only AES-GCM ciphertext is saved; password/profile/invitation never enter preferences. */
class EncryptedSessionStore(context: Context) : SessionStore {
    private val preferences = context.getSharedPreferences("iam_session", Context.MODE_PRIVATE)
    private val changes = MutableStateFlow(0)
    override val invalidations = changes.asStateFlow()
    private val alias = "safework.iam.session.v1"

    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(alias, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").run {
            init(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256).build())
            generateKey()
        }
    }

    @Synchronized override fun read(): SessionCredential? {
        return try {
            val encrypted = preferences.getString("ciphertext", null) ?: return null
            val bytes = Base64.decode(encrypted, Base64.NO_WRAP)
            require(bytes.size > 28)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, bytes.copyOfRange(0, 12)))
            cipher.updateAAD(alias.toByteArray())
            val text = String(cipher.doFinal(bytes.copyOfRange(12, bytes.size)), Charsets.UTF_8)
            val separator = text.indexOf('\n')
            require(separator > 0 && separator < text.lastIndex)
            SessionCredential(text.substring(separator + 1), text.substring(0, separator))
        } catch (_: Exception) {
            clear()
            null
        }
    }

    @Synchronized override fun write(credential: SessionCredential) {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        cipher.updateAAD(alias.toByteArray())
        val bytes = cipher.iv + cipher.doFinal((credential.apiUrl + "\n" + credential.token).toByteArray())
        if (!preferences.edit().putString("ciphertext", Base64.encodeToString(bytes, Base64.NO_WRAP)).commit()) throw SessionStorageException()
    }

    @Synchronized override fun clear(expectedToken: String?) {
        if (expectedToken != null && read()?.token != expectedToken) return
        if (!preferences.edit().clear().commit()) throw SessionStorageException()
        changes.value += 1
    }
}
