package com.example.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Manages Google User Messaging Platform (UMP) consent gathering and Mobile Ads SDK initialization.
 * Never crashes offline or in restricted environments; strictly respects user consent status.
 */
object AdConsentManager {
    private val isMobileAdsInitialized = AtomicBoolean(false)

    private val _canRequestAds = MutableStateFlow(false)
    val canRequestAds: StateFlow<Boolean> = _canRequestAds.asStateFlow()

    private val _isPrivacyOptionsRequired = MutableStateFlow(false)
    val isPrivacyOptionsRequired: StateFlow<Boolean> = _isPrivacyOptionsRequired.asStateFlow()

    fun gatherConsentAndInitialize(activity: Activity) {
        try {
            val consentInfo = UserMessagingPlatform.getConsentInformation(activity)
            val params = ConsentRequestParameters.Builder()
                .setTagForUnderAgeOfConsent(false)
                .build()

            syncConsentState(consentInfo, activity.applicationContext)

            consentInfo.requestConsentInfoUpdate(
                activity,
                params,
                {
                    UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { _ ->
                        syncConsentState(consentInfo, activity.applicationContext)
                    }
                },
                { _ ->
                    // Offline or UMP error: only initialize if prior valid consent allows it
                    syncConsentState(consentInfo, activity.applicationContext)
                }
            )
        } catch (_: Throwable) {
            // Safe fallback in unit tests or unsupported GMS devices
        }
    }

    fun showPrivacyOptionsForm(activity: Activity, onComplete: ((String?) -> Unit)? = null) {
        try {
            UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
                try {
                    val consentInfo = UserMessagingPlatform.getConsentInformation(activity)
                    syncConsentState(consentInfo, activity.applicationContext)
                } catch (_: Throwable) {
                }
                onComplete?.invoke(formError?.message)
            }
        } catch (t: Throwable) {
            onComplete?.invoke("Consent form unavailable offline.")
        }
    }

    private fun syncConsentState(consentInfo: ConsentInformation, appContext: Context) {
        val allowed = consentInfo.canRequestAds()
        _canRequestAds.value = allowed
        _isPrivacyOptionsRequired.value =
            consentInfo.privacyOptionsRequirementStatus == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

        if (allowed && isMobileAdsInitialized.compareAndSet(false, true)) {
            try {
                MobileAds.initialize(appContext) {}
            } catch (_: Throwable) {
                isMobileAdsInitialized.set(false)
            }
        }
    }
}
