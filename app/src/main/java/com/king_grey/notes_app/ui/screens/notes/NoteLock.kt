package com.king_grey.notes_app.ui.screens.notes

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Asks for fingerprint, face or the screen lock, then calls [onSuccess].
 *
 * Use it from the host when the editor reports [NoteEditorScreen]'s onToggleLock, and again before
 * opening a locked note. Needs `androidx.biometric:biometric` and a [FragmentActivity]
 * (or AppCompatActivity) as the host activity.
 *
 * Returns false without showing anything when the phone has no screen lock at all, so the host can
 * tell the person to set one up.
 */
fun authenticate(
    activity: FragmentActivity,
    title: String,
    subtitle: String? = null,
    onFailure: () -> Unit = {},
    onSuccess: () -> Unit,
): Boolean {
    val allowed = BIOMETRIC_STRONG or DEVICE_CREDENTIAL
    if (BiometricManager.from(activity)
            .canAuthenticate(allowed) != BiometricManager.BIOMETRIC_SUCCESS
    ) {
        return false
    }
    val prompt = BiometricPrompt(
        activity,
        ContextCompat.getMainExecutor(activity),
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onFailure()
            }
        },
    )
    val info = BiometricPrompt.PromptInfo.Builder()
        .setTitle(title)
        .apply { if (subtitle != null) setSubtitle(subtitle) }
        // With DEVICE_CREDENTIAL allowed the system shows "Use screen lock" itself, and a negative
        // button must not be set.
        .setAllowedAuthenticators(allowed)
        .build()
    prompt.authenticate(info)
    return true
}