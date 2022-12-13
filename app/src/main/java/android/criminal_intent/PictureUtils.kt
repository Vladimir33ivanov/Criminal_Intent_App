package android.criminal_intent

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point

fun getScaledBitmap(path: String, activity: Activity): Bitmap {
    val size = Point()
    activity.windowManager.defaultDisplay.getSize(size)

    return getScaledBitmap(path, size.x, size.y)
}

fun getScaledBitmap(path: String, destWith: Int, destHeight: Int): Bitmap{
    //Чтение размеров изображения дисков
    var options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(path, options)

    val srcWidth = options.outWidth.toFloat()
    val srcHeight = options.outHeight.toFloat()

    //Узнать на сколько можно уменьшить
    var inSampleSize = 1
    if(srcHeight > destHeight || srcWidth > destWith){
        val heightScale = srcHeight/destHeight
        val widthScale = srcWidth/destWith

        val sampleScale = if (heightScale > widthScale){
            heightScale
        }else{
            widthScale
        }
        inSampleSize = Math.round(sampleScale)
    }

    options = BitmapFactory.Options()
    options.inSampleSize = inSampleSize

    //Чтение и создание растрового изображения
    return BitmapFactory.decodeFile(path, options)
}