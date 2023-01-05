package tech.linjiang.app.docscandemo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.IOException

fun base64AsBitmap(base64: String): Bitmap {
    val bytes = Base64.decode(base64, Base64.DEFAULT);
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}

fun bitmap2Base64(bitmap: Bitmap): String {
    var result: String? = null
    var baos: ByteArrayOutputStream? = null
    try {
        baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        baos.flush()
        baos.close()
        val bitmapBytes: ByteArray = baos.toByteArray()
        result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT)
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        try {
            if (baos != null) {
                baos.flush()
                baos.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    return result ?: throw RuntimeException("")
}
