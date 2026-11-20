package com.example.ads

import android.app.Activity
import android.content.Context
import com.example.BuildConfig
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Manages Google User Messaging Platform (UMP) consent gathering, Mobile Ads SDK initialization,
 * and strictly frequency-capped Interstitial Ads (never inside Safe Vault or file operations).
 */
object AdConsentManager {
    private val isMobileAdsInitialized = AtomicBoolean(false)
    private var preloadedInterstitial: InterstitialAd? = null
    private var isLoadingInterstitial = false
    private var lastInterstitialShownMs = 0L

    // Enforce minimum 3-minute cooldown to prevent intrusive ads or accidental clicks
    private const val MIN_INTERSTITIAL_COOLDOWN_MS = 180_000L

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

    fun preloadInterstitial(context: Context) {
        if (!_canRequestAds.value || isLoadingInterstitial || preloadedInterstitial != null) return
        val unitId = BuildConfig.ADMOB_INTERSTITIAL_UNIT_ID
        if (unitId.isBlank()) return

        try {
            isLoadingInterstitial = true
            InterstitialAd.load(
                context.applicationContext,
                unitId,
                AdRequest.Builder().build(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        isLoadingInterstitial = false
                        preloadedInterstitial = ad
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        isLoadingInterstitial = false
                        preloadedInterstitial = null
                    }
                }
            )
        } catch (_: Throwable) {
            isLoadingInterstitial = false
            preloadedInterstitial = null
        }
    }

    /**
     * Shows a preloaded Interstitial only at explicit non-destructive break points
     * (e.g., exiting Storage Optimizer after an action), strictly subject to consent & 3-min cooldown.
     */
    fun maybeShowInterstitialOnSafeBreak(activity: Activity, onDone: () -> Unit = {}) {
        val now = System.currentTimeMillis()
        val ad = preloadedInterstitial
        if (!_canRequestAds.value || ad == null || (now - lastInterstitialShownMs) < MIN_INTERSTITIAL_COOLDOWN_MS) {
            preloadInterstitial(activity)
            onDone()
            return
        }

        preloadedInterstitial = null
        lastInterstitialShownMs = now
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                preloadInterstitial(activity)
                onDone()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                preloadInterstitial(activity)
                onDone()
            }
        }
        try {
            ad.show(activity)
        } catch (_: Throwable) {
            onDone()
        }
    }

    private fun syncConsentState(consentInfo: ConsentInformation, appContext: Context) {
        val allowed = consentInfo.canRequestAds()
        _canRequestAds.value = allowed
        _isPrivacyOptionsRequired.value =
            consentInfo.privacyOptionsRequirementStatus == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

        if (allowed && isMobileAdsInitialized.compareAndSet(false, true)) {
            try {
                MobileAds.initialize(appContext) {
                    preloadInterstitial(appContext)
                }
            } catch (_: Throwable) {
                isMobileAdsInitialized.set(false)
            }
        }
    }
}
