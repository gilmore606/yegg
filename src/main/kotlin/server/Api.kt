package com.dlfsystems.yegg.server

import com.dlfsystems.yegg.compiler.CompileException
import com.dlfsystems.yegg.util.systemEpoch
import com.dlfsystems.yegg.value.VInt
import io.ktor.server.request.authorization
import io.ktor.server.routing.RoutingRequest
import kotlinx.serialization.Serializable
import javax.crypto.Cipher
import javax.crypto.Cipher.DECRYPT_MODE
import javax.crypto.Cipher.ENCRYPT_MODE
import javax.crypto.spec.SecretKeySpec
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.text.Charsets.UTF_8

object Api {

    // To authorize, post to /auth with { username:"xxx", password:"yyy" } to receive a persistent token.
    // The token will be set in a Set-cookie: header, and also returned in the body.  You can use the token
    // as a Bearer authorization header to authorize API calls if cookies aren't convenient.
    //
    // The decrypted token string is two strings separated by a space: the player ID, and the issue time.

    const val COOKIE_NAME = "yegg"

    // Algorithm and key for encrypting the token.
    private const val CIPHER_ALGO = "AES"
    private const val SECRET_KEY = "M0ra53c!7784AuGE"  // 16 bytes for AES

    private val keySpec = SecretKeySpec(SECRET_KEY.toByteArray(), CIPHER_ALGO)
    private val encryptor = Cipher.getInstance(CIPHER_ALGO).apply { init(ENCRYPT_MODE, keySpec) }
    private val decryptor = Cipher.getInstance(CIPHER_ALGO).apply { init(DECRYPT_MODE, keySpec) }

    private const val TAG = "API"

    @OptIn(ExperimentalEncodingApi::class)
    fun getAuthToken(username: String, password: String): String? {
        Yegg.world.getPlayerLogin(username, password)?.also { player ->
            val cookie = "${player.id} ${systemEpoch()}"
            Log.i(TAG, "Authenticated user $username (token '$cookie')")
            return Base64.encode(encryptor.doFinal(cookie.toByteArray(UTF_8)))
        }
        Log.i(TAG, "Failed authentication for $username")
        return null
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun isAuthorized(request: RoutingRequest): Boolean {
        val raw = request.cookies[COOKIE_NAME] ?: request.authorization()?.removePrefix("Bearer ")
        raw?.also {
            val cookie = decryptor.doFinal(Base64.decode(it)).toString(UTF_8)
            if (!cookie.contains(" ")) return false
            val (playerID, authTime) = cookie.split(" ")
            Yegg.world.getObjByID(playerID)?.also { player ->
                if (!player.isPlayer()) return false
                val lastAuthUpdate = player.getProp("lastAuthUpdate")?.let { it as VInt }?.v ?: 0
                return (authTime.toLong() >= lastAuthUpdate)
            }
        }
        return false
    }

    // GET /verb/{traitName}/{verbName}
    fun getVerbCode(traitName: String, verbName: String): String? {
        Yegg.world.getTrait(traitName)?.also { trait ->
            trait.verbs[verbName]?.also { verb ->
                return verb.source
            }
        }
        return null
    }

    // PUT /verb/{traitName}/{verbName}
    fun setVerbCode(traitName: String, verbName: String, verbCode: String): String {
        Yegg.world.getTrait(traitName)?.also { trait ->
            try {
                Log.i(TAG, "Programmed $traitName.$verbName : \n$verbCode")
                trait.programVerb(verbName, verbCode)
                return "OK"
            } catch (e: CompileException) {
                return "ERROR: ${e.message}"
            }
        }
        return "ERROR: no such trait"
    }

    @Serializable
    data class AuthRequest(
        val username: String,
        val password: String,
    )

}
