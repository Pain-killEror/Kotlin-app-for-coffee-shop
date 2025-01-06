package com.example.itrysohard.shadow

import android.content.res.Resources
import android.graphics.BlurMaskFilter
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.util.TypedValue
import android.view.View
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import kotlin.math.roundToInt

fun wrapInCustomShadowWithOffset(
    view: View,
    @ColorRes shadowColor: Int,
    resources: Resources
) {

    val shadowColorValue = ContextCompat.getColor(view.context, shadowColor)

    val shapeDrawable = ShapeDrawable()
    shapeDrawable.setTint(shadowColorValue)

    //You could use this to set padding directly to the shapeDrawable
    // instead of setting it through the wrapper layout in XML
//        val padding = 14.toDp(view.context.resources)
//        val shapeDrawablePadding = Rect()
//        shapeDrawablePadding.left = padding
//        shapeDrawablePadding.right = padding
//        shapeDrawablePadding.top = padding
//        shapeDrawablePadding.bottom = padding
//        shapeDrawable.setPadding(shapeDrawablePadding)
//        val shadowBlur = padding - 4.toDp(resources)

    val shadowBlur = view.paddingBottom - 4.toDp(resources)
    val offset = 4.toDp(resources)
    shapeDrawable.paint.setShadowLayer(
        shadowBlur - offset, //blur
        0f, //dx
        offset, //dy
        getColorWithAlpha(shadowColorValue, 0.8f) //color
    )
    val filter = BlurMaskFilter(offset, BlurMaskFilter.Blur.OUTER)
    view.setLayerType(View.LAYER_TYPE_SOFTWARE, shapeDrawable.paint)
    shapeDrawable.paint.maskFilter = filter





    val radius = 40.toDp(view.context.resources)
    val outerRadius = floatArrayOf(
        radius, radius, //top-left
        radius, radius, //top-right
        radius, radius, //bottom-right
        radius, radius  //bottom-left
    )
    shapeDrawable.shape = RoundRectShape(outerRadius, null, null)

    val drawable = LayerDrawable(arrayOf<Drawable>(shapeDrawable))
    val inset = view.paddingBottom
    drawable.setLayerInset(
        0,
        inset, //left
        inset + offset.toInt(), //top  добавил штуку чтобы сверху тени не было
        inset , //right
        inset  //bottom
    )
    view.background = drawable
}


fun wrapInCustomShadow(
    view: View,
    @ColorRes shadowColor: Int,
    resources: Resources
) {

    val shadowColorValue = ContextCompat.getColor(view.context, shadowColor)
    val shapeDrawable = ShapeDrawable()
    shapeDrawable.setTint(shadowColorValue)

    val shadowBlur = view.paddingBottom - 4.toDp(resources)
    shapeDrawable.paint.setShadowLayer(
        shadowBlur,
        0f,
        0f,
        getColorWithAlpha(shadowColorValue, 0.8f)
    )
    view.setLayerType(View.LAYER_TYPE_SOFTWARE, shapeDrawable.paint)

    val radius = 40.toDp(view.context.resources) // радиус скругления
    val outerRadius = floatArrayOf(
        radius, radius,
        radius, radius,
        radius, radius,
        radius, radius
    )
    shapeDrawable.shape = RoundRectShape(outerRadius, null, null)

    val drawable = LayerDrawable(arrayOf<Drawable>(shapeDrawable))
    val inset = view.paddingBottom
    drawable.setLayerInset(
        0,
        inset,
        inset,
        inset,
        inset
    )
    view.background = drawable
}


private fun getColorWithAlpha(color: Int, ratio: Float): Int {
    val alpha = (Color.alpha(color) * ratio).roundToInt()
    val r = Color.red(color)
    val g = Color.green(color)
    val b = Color.blue(color)
    return Color.argb(alpha, r, g, b)
}

fun Int.toDp(resources: Resources): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), resources.displayMetrics)
}