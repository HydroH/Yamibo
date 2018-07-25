package com.hydroh.yamibo.io

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Environment
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable
import com.bumptech.glide.load.resource.gif.GifDrawable
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.util.*

class SaveImageTask(private val callback: Callback) :
        AsyncTask<Drawable, Unit, File>() {

    private var e: Exception? = null

    override fun doInBackground(vararg drawables: Drawable): File? {
        if (Environment.MEDIA_MOUNTED != Environment.getExternalStorageState()) {
            e = FileNotFoundException("图片保存失败")
            return null
        }
        val mediaStorageDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Yamibo")
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs() ) {
            e = FileNotFoundException("图片保存失败，请开启存储空间权限")
            return null
        }
        val drawable = drawables.first()
        try {
            return when (drawable) {
                is GlideBitmapDrawable -> {
                    val filename = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date()) + ".png"
                    val mediaFile = File(mediaStorageDir.path + File.separator + filename)
                    val fos = FileOutputStream(mediaFile)

                    val image = drawable.bitmap
                    image.compress(Bitmap.CompressFormat.PNG, 100, fos)
                    fos.close()
                    mediaFile
                }
                is GifDrawable -> {
                    val filename = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date()) + ".gif"
                    val mediaFile = File(mediaStorageDir.path + File.separator + filename)
                    val fos = FileOutputStream(mediaFile)

                    val byteBuffer = drawable.data
                    fos.write(byteBuffer, 0, byteBuffer.size)
                    fos.close()
                    mediaFile
                }
                else -> {
                    e = Exception("未知图片格式")
                    null
                }
            }
        } catch (e: FileNotFoundException) {
            this.e = FileNotFoundException("图片保存失败，请开启存储空间权限")
            return null
        } catch (e: Exception) {
            this.e = Exception("图片保存失败")
            return null
        }
    }

    override fun onPostExecute(result: File?) {
        super.onPostExecute(result)
        if (result != null) {
            callback.onSaveComplete(result)
        } else {
            callback.onError(e ?: Exception("未知错误"))
        }
    }

    interface Callback {
        fun onSaveComplete(filePath: File)
        fun onError(e: Exception)
    }
}