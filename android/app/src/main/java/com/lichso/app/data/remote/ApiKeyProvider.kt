package com.lichso.app.data.remote

import com.lichso.app.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides the API key at runtime with basic obfuscation.
 *
 * WARNING / TODO: This is NOT truly secure. Any determined attacker can still
 * extract the key from the APK via runtime debugging or memory inspection.
 *
 * The PROPER solution is to proxy API calls through your own backend server
 * (e.g., Firebase Functions, Cloud Run) so the key NEVER exists on the client.
 *
 * This class exists to:
 * 1. Avoid the key being a simple plaintext string in the dex
 * 2. Make it slightly harder to extract via static analysis
 * 3. Centralize key access for easy migration to a backend proxy later
 */
@Singleton
class ApiKeyProvider @Inject constructor() {

    /**
     * Returns the OpenRouter API key.
     * The key is stored in BuildConfig (loaded from local.properties at build time).
     * R8/ProGuard will obfuscate this class and the field access.
     */
    fun getOpenRouterApiKey(): String {
        // BuildConfig value — R8 will inline & obfuscate since we no longer -keep BuildConfig
        val raw = BuildConfig.OPENROUTER_API_KEY
        if (raw.isBlank()) {
            throw IllegalStateException(
                "OPENROUTER_API_KEY is not set. Add it to local.properties."
            )
        }
        return raw
    }
}
