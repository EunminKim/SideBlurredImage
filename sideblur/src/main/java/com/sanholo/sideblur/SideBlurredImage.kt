package com.sanholo.sideblur

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.renderscript.Allocation
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur

object SideBlurredImage {

    fun convert(context: Context, bitmap: Bitmap, destWidthPx: Int, destHeightPx: Int,
                radius: Float): Bitmap {
        if (radius <= 0 || radius > 25) return bitmap

        val centerImage = resize(bitmap, destWidthPx, destHeightPx)

        val frameRatio = if (centerImage.width > centerImage.height) {
            destWidthPx.toFloat() / destHeightPx
        } else {
            destHeightPx / destWidthPx.toFloat()
        }
        val cropped = if (destWidthPx > destHeightPx) {
            val imageRatio = centerImage.width.toFloat() /centerImage.height
            if (centerImage.width > centerImage.height) {
                if (imageRatio > frameRatio) {
                    cropCenterBitmap(centerImage, (centerImage.height * frameRatio).toInt(), centerImage.height)
                } else {
                    cropCenterBitmap(centerImage, centerImage.width, (centerImage.width / frameRatio).toInt())
                }
            } else {
                cropCenterBitmap(centerImage, centerImage.width, (centerImage.width * frameRatio).toInt())
            }
        } else if (destWidthPx == destHeightPx) {
            if (centerImage.width > centerImage.height) {
                cropCenterBitmap(centerImage, centerImage.height, centerImage.width)
            } else {
                cropCenterBitmap(centerImage, centerImage.width, centerImage.width)
            }
        } else {
            val imageRatio = centerImage.height / centerImage.width.toFloat()
            if (centerImage.width > centerImage.height) {
                cropCenterBitmap(centerImage, (centerImage.height * frameRatio).toInt(), centerImage.height)
            } else {
                if (imageRatio > frameRatio) {
                    cropCenterBitmap(centerImage, centerImage.width, (centerImage.width * frameRatio).toInt())
                } else {
                    cropCenterBitmap(centerImage, (centerImage.height / frameRatio).toInt(), centerImage.height)
                }
            }
        }
        val scaled = resize(cropped, destWidthPx, destHeightPx)
        val background = blur(context, scaled, radius)

        val canvas = Canvas(background)
        val centreX = (background.width - centerImage.width).toFloat() / 2
        val centreY = (background.height - centerImage.height).toFloat() / 2
        canvas.drawBitmap(centerImage, centreX, centreY, null)

        return background
    }

    private fun blur(context: Context, bitmap: Bitmap, radius: Float): Bitmap {
        val rs = RenderScript.create(context)
        val overlayAlloc = Allocation.createFromBitmap(rs, bitmap)
        val blur = ScriptIntrinsicBlur.create(rs, overlayAlloc.element)
        blur.setInput(overlayAlloc)
        blur.setRadius(radius)
        blur.forEach(overlayAlloc)
        overlayAlloc.copyTo(bitmap)
        return bitmap
    }

    private fun cropCenterBitmap(src: Bitmap, w: Int, h: Int): Bitmap {
        val width = src.width
        val height = src.height

        if (width < w && height < h) return src

        var x = 0
        var y = 0
        if (width > w) x = (width - w) / 2
        if (height > h) y = (height - h) / 2

        var cw = w
        var ch = h
        if (w > width) cw = width;
        if (h > height) ch = height;

        return Bitmap.createBitmap(src, x, y, cw, ch);
    }

    private fun resize(image: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        if (maxHeight > 0 && maxWidth > 0) {
            val width = image.width
            val height = image.height
            val ratioBitmap = width.toFloat() / height.toFloat()
            val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()
            var finalWidth = maxWidth
            var finalHeight = maxHeight
            if (ratioMax > ratioBitmap) {
                finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
            } else {
                finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
            }
            return Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true)
        }
        return image
    }
}