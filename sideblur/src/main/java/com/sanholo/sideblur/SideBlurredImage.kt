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

        // Resize the bitmap to set center image keeping aspect ratio
        val centerImage = resize(bitmap, destWidthPx, destHeightPx)

        // Calculate frame and image aspect ratio
        val frameRatio = if (destWidthPx > destHeightPx) {
            destWidthPx.toFloat() / destHeightPx
        } else {
            destHeightPx / destWidthPx.toFloat()
        }
        val imageRatio = if (centerImage.width > centerImage.height) {
            centerImage.width.toFloat() / centerImage.height
        } else {
            centerImage.height / centerImage.width.toFloat()
        }

        // Crop the bitmap to set background image keeping aspect ratio
        val cropped = if (destWidthPx > destHeightPx) {
            if (centerImage.width > centerImage.height) {
                if (imageRatio > frameRatio) {
                    centerCrop(centerImage, (centerImage.height * frameRatio).toInt(),
                        centerImage.height)
                } else {
                    centerCrop(centerImage, centerImage.width,
                        (centerImage.width / frameRatio).toInt())
                }
            } else {
                centerCrop(centerImage, centerImage.width,
                    (centerImage.width / frameRatio).toInt())
            }
        } else if (destWidthPx == destHeightPx) {
            if (centerImage.width > centerImage.height) {
                centerCrop(centerImage, centerImage.height, centerImage.width)
            } else {
                centerCrop(centerImage, centerImage.width, centerImage.width)
            }
        } else {
            if (centerImage.width > centerImage.height) {
                centerCrop(centerImage, (centerImage.height / frameRatio).toInt(),
                    centerImage.height)
            } else {
                if (imageRatio > frameRatio) {
                    centerCrop(centerImage, centerImage.width,
                        (centerImage.width * frameRatio).toInt())
                } else {
                    centerCrop(centerImage, (centerImage.height / frameRatio).toInt(),
                        centerImage.height)
                }
            }
        }

        // Scale and Blur background image
        val scaled = resize(cropped, destWidthPx, destHeightPx)
        val background = blur(context, scaled, radius)

        // Put the actual image on top of the blurred background image
        val canvas = Canvas(background)
        val centerX = (background.width - centerImage.width).toFloat() / 2
        val centerY = (background.height - centerImage.height).toFloat() / 2
        canvas.drawBitmap(centerImage, centerX, centerY, null)

        // Recycle unused bitmaps
        cropped.recycle()
        centerImage.recycle()

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

    private fun centerCrop(source: Bitmap, destWidth: Int, destHeight: Int): Bitmap {
        val width = source.width
        val height = source.height

        if (width < destWidth && height < destHeight) return source

        var x = 0
        var y = 0
        if (width > destWidth) x = (width - destWidth) / 2
        if (height > destHeight) y = (height - destHeight) / 2

        var smallestWidth = destWidth
        var smallestHeight = destHeight
        if (destWidth > width) smallestWidth = width
        if (destHeight > height) smallestHeight = height

        return Bitmap.createBitmap(source, x, y, smallestWidth, smallestHeight)
    }

    private fun resize(source: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        if (targetHeight <= 0 || targetWidth <= 0) return source

        val width = source.width
        val height = source.height
        val sourceRatio = width.toFloat() / height.toFloat()
        val targetRatio = targetWidth.toFloat() / targetHeight.toFloat()
        var scaledWidth = targetWidth
        var scaledHeight = targetHeight
        if (targetRatio > sourceRatio) {
            scaledWidth = (targetHeight.toFloat() * sourceRatio).toInt()
        } else {
            scaledHeight = (targetWidth.toFloat() / sourceRatio).toInt()
        }
        return Bitmap.createScaledBitmap(source, scaledWidth, scaledHeight, true)
    }
}