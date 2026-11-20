package com.example.ui.components

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.BuildConfig
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView

/**
 * Non-intrusive anchored adaptive AdMob Banner placed ONLY on safe browse screens.
 * Remains invisible until an ad successfully loads (zero empty boxes offline).
 */
@Composable
fun AdMobBannerBar(
    canRequestAds: Boolean,
    modifier: Modifier = Modifier,
    adUnitId: String = BuildConfig.ADMOB_BANNER_UNIT_ID
) {
    if (!canRequestAds || adUnitId.isBlank()) return

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isAdLoaded by remember(adUnitId) { mutableStateOf(false) }

    val adView = remember(context, adUnitId) {
        try {
            val metrics = context.resources.displayMetrics
            val adWidthDp = (metrics.widthPixels / metrics.density).toInt().coerceIn(300, 420)
            AdView(context).apply {
                this.adUnitId = adUnitId
                setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidthDp))
                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        isAdLoaded = true
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        isAdLoaded = false
                    }
                }
                loadAd(AdRequest.Builder().build())
            }
        } catch (_: Throwable) {
            null
        }
    }

    if (adView == null) return

    DisposableEffect(lifecycleOwner, adView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> adView.resume()
                Lifecycle.Event.ON_PAUSE -> adView.pause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            adView.destroy()
        }
    }

    AnimatedVisibility(
        visible = isAdLoaded,
        enter = fadeIn(),
        modifier = modifier
            .fillMaxWidth()
            .testTag("admob_banner_container")
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 18.dp,
            specularHighlight = false
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ad",
                        color = TextMuted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0x22FFFFFF))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
                AndroidView(
                    factory = {
                        (adView.parent as? ViewGroup)?.removeView(adView)
                        adView
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Liquid Glass styled Google AdMob Native Advanced Ad Card for Storage Optimizer / Dashboard feeds.
 * Clearly marks "Sponsored" badge per AdMob policies and never mimics file action controls.
 */
@Composable
fun LiquidNativeAdCard(
    canRequestAds: Boolean,
    modifier: Modifier = Modifier,
    adUnitId: String = BuildConfig.ADMOB_NATIVE_UNIT_ID
) {
    if (!canRequestAds || adUnitId.isBlank()) return

    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current
    var loadedAd by remember(adUnitId) { mutableStateOf<NativeAd?>(null) }

    DisposableEffect(context, adUnitId) {
        var isDisposed = false
        try {
            val loader = AdLoader.Builder(context, adUnitId)
                .forNativeAd { nativeAd ->
                    if (isDisposed) {
                        nativeAd.destroy()
                    } else {
                        loadedAd?.destroy()
                        loadedAd = nativeAd
                    }
                }
                .withNativeAdOptions(NativeAdOptions.Builder().build())
                .build()
            loader.loadAd(AdRequest.Builder().build())
        } catch (_: Throwable) {
            // Skip gracefully on offline or unsupported environments
        }

        onDispose {
            isDisposed = true
            loadedAd?.destroy()
            loadedAd = null
        }
    }

    val nativeAd = loadedAd ?: return

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("admob_native_container"),
        cornerRadius = 22.dp,
        accentGlowColor = NeonCyan
    ) {
        AndroidView(
            factory = { ctx ->
                buildNativeAdView(ctx, isDark)
            },
            update = { adView ->
                bindNativeAd(adView, nativeAd, isDark)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        )
    }
}

private fun dpToPx(context: Context, dp: Int): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp.toFloat(),
        context.resources.displayMetrics
    ).toInt()
}

private fun buildNativeAdView(context: Context, isDark: Boolean): NativeAdView {
    val root = NativeAdView(context).apply {
        layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    val container = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    val headerRow = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
    }

    val iconView = ImageView(context).apply {
        id = android.R.id.icon
        val size = dpToPx(context, 42)
        layoutParams = LinearLayout.LayoutParams(size, size).apply {
            marginEnd = dpToPx(context, 10)
        }
        scaleType = ImageView.ScaleType.CENTER_CROP
    }

    val titleCol = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
    }

    val badge = TextView(context).apply {
        text = "Sponsored"
        textSize = 10f
        setTypeface(null, Typeface.BOLD)
        setTextColor(0xFF00E5FF.toInt())
    }

    val headline = TextView(context).apply {
        id = android.R.id.title
        textSize = 14f
        setTypeface(null, Typeface.BOLD)
        maxLines = 1
        ellipsize = TextUtils.TruncateAt.END
        setTextColor(if (isDark) 0xFFF4F7FE.toInt() else 0xFF0F172A.toInt())
    }

    titleCol.addView(badge)
    titleCol.addView(headline)

    val ctaButton = Button(context).apply {
        id = android.R.id.button1
        textSize = 11f
        isAllCaps = false
        setTypeface(null, Typeface.BOLD)
        setTextColor(0xFF07090E.toInt())
        background = GradientDrawable().apply {
            setColor(0xFF00E5FF.toInt())
            cornerRadius = dpToPx(context, 12).toFloat()
        }
        val padH = dpToPx(context, 12)
        val padV = dpToPx(context, 6)
        setPadding(padH, padV, padH, padV)
        minHeight = 0
        minimumHeight = dpToPx(context, 36)
    }

    headerRow.addView(iconView)
    headerRow.addView(titleCol)
    headerRow.addView(ctaButton)

    val bodyView = TextView(context).apply {
        id = android.R.id.summary
        textSize = 12f
        maxLines = 2
        ellipsize = TextUtils.TruncateAt.END
        setTextColor(if (isDark) 0xFFB0BCD0.toInt() else 0xFF475569.toInt())
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = dpToPx(context, 8)
        }
    }

    val mediaView = MediaView(context).apply {
        id = android.R.id.content
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dpToPx(context, 130)
        ).apply {
            topMargin = dpToPx(context, 10)
        }
    }

    container.addView(headerRow)
    container.addView(bodyView)
    container.addView(mediaView)
    root.addView(container)

    root.iconView = iconView
    root.headlineView = headline
    root.bodyView = bodyView
    root.callToActionView = ctaButton
    root.mediaView = mediaView

    return root
}

private fun bindNativeAd(adView: NativeAdView, nativeAd: NativeAd, isDark: Boolean) {
    (adView.headlineView as? TextView)?.apply {
        text = nativeAd.headline ?: ""
        setTextColor(if (isDark) 0xFFF4F7FE.toInt() else 0xFF0F172A.toInt())
    }
    (adView.bodyView as? TextView)?.apply {
        val bodyText = nativeAd.body
        text = bodyText ?: ""
        visibility = if (bodyText.isNullOrBlank()) android.view.View.GONE else android.view.View.VISIBLE
        setTextColor(if (isDark) 0xFFB0BCD0.toInt() else 0xFF475569.toInt())
    }
    (adView.iconView as? ImageView)?.apply {
        val drawable = nativeAd.icon?.drawable
        setImageDrawable(drawable)
        visibility = if (drawable != null) android.view.View.VISIBLE else android.view.View.GONE
    }
    (adView.callToActionView as? Button)?.apply {
        val cta = nativeAd.callToAction
        text = cta ?: "Learn More"
        visibility = if (cta.isNullOrBlank()) android.view.View.GONE else android.view.View.VISIBLE
    }
    adView.mediaView?.let { mv ->
        val content = nativeAd.mediaContent
        mv.mediaContent = content
        mv.visibility = if (content != null) android.view.View.VISIBLE else android.view.View.GONE
    }
    adView.setNativeAd(nativeAd)
}
