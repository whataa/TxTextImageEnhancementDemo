package tech.linjiang.app.docscandemo

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target.SIZE_ORIGINAL
import com.bumptech.glide.request.transition.Transition
import com.tencentcloudapi.common.exception.TencentCloudSDKException
import kotlinx.android.synthetic.main.activity_main.btn_ok
import kotlinx.android.synthetic.main.activity_main.btn_photo
import kotlinx.android.synthetic.main.activity_main.btn_reset
import kotlinx.android.synthetic.main.activity_main.cb_1
import kotlinx.android.synthetic.main.activity_main.cb_2
import kotlinx.android.synthetic.main.activity_main.cb_202
import kotlinx.android.synthetic.main.activity_main.cb_204
import kotlinx.android.synthetic.main.activity_main.cb_205
import kotlinx.android.synthetic.main.activity_main.cb_207
import kotlinx.android.synthetic.main.activity_main.cb_208
import kotlinx.android.synthetic.main.activity_main.cb_300
import kotlinx.android.synthetic.main.activity_main.cb_301
import kotlinx.android.synthetic.main.activity_main.cb_302
import kotlinx.android.synthetic.main.activity_main.cb_303
import kotlinx.android.synthetic.main.activity_main.cb_304
import kotlinx.android.synthetic.main.activity_main.iv_before
import kotlinx.android.synthetic.main.activity_main.progress_indicator
import kotlinx.android.synthetic.main.activity_main.status

class MainActivity : AppCompatActivity(), CompoundButton.OnCheckedChangeListener {

    companion object {
        private const val TAG = "MainActivity"
        private const val REQUEST_CODE_ALBUM = 1
    }

    private val handler = Handler(Looper.getMainLooper())

    private val specialCB by lazy {
        arrayOf(
            cb_301, cb_302, cb_303, cb_304
        )
    }

    private val checkboxes by lazy {
        mapOf(
            cb_1 to 1, cb_2 to 2, cb_202 to 202, cb_204 to 204, cb_205 to 205, cb_207 to 207, cb_208 to 208,
            cb_300 to 300, cb_301 to 301, cb_302 to 302, cb_303 to 303, cb_304 to 304
        )
    }

    private var curImageUri: Uri? = null

    private val taskTypes = mutableListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkboxes.keys.forEach {
            it.setOnCheckedChangeListener(this)
        }
        btn_reset.setOnClickListener {
            curImageUri?.let {
                Glide.with(this).load(it).apply(RequestOptions.overrideOf(SIZE_ORIGINAL)).into(iv_before)
            }
            checkboxes.keys.forEach { it.isChecked = false }
        }
        btn_ok.setOnClickListener out@{
            if (taskTypes.isEmpty()) {
                "至少选择一项增强类型".toast()
                return@out
            }
            if (curImageUri == null) {
                "请选择图片".toast()
                return@out
            }
            doEnhance()
        }
        btn_photo.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = "android.intent.action.GET_CONTENT"
            intent.addCategory("android.intent.category.OPENABLE")
            startActivityForResult(intent, REQUEST_CODE_ALBUM)
        }
    }

    private fun doEnhance() {
        curImageUri?.let {
            Glide.with(this).load(it).apply(RequestOptions.overrideOf(SIZE_ORIGINAL)).into(iv_before)
        }
        progress_indicator.apply {
            max = taskTypes.size
            progress = 0
        }
        uri2Bitmap {
            var curBitmap = bitmap2Base64(it)
            Thread {
                try {
                    taskTypes.forEachIndexed { i, type ->
                        handler.post {
                            "开始处理：${checkboxes.filter { it.value == type }.map { it.key.text }.first()}".toast()
                        }
                        curBitmap = enhancement(type, curBitmap)
                        handler.post {
                            progress_indicator.progress = i + 1
                            val bitmap = base64AsBitmap(curBitmap)
                            iv_before.setImageBitmap(bitmap)
                        }
                    }
                    "处理完成".toast()
                } catch (t: TencentCloudSDKException) {
                    "错误：(${t.errorCode}) ${t.message}".toast()
                }
            }.start()
        }
    }

    override fun onCheckedChanged(cb: CompoundButton, value: Boolean) {
        if (cb in specialCB && value) {
            cb_300.isChecked = false
        } else if (cb == cb_300 && value) {
            specialCB.forEach { it.isChecked = false }
        }
        val type = checkboxes[cb]!!
        if (value) {
            if (!taskTypes.contains(type)) {
                taskTypes.add(type)
            }
        } else {
            taskTypes.remove(type)
        }
        Log.d(TAG, "onCheckedChanged: $taskTypes")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_ALBUM -> {
                    iv_before.setImageBitmap(null)
                    curImageUri = data?.data
                    Glide.with(this).load(curImageUri).apply(RequestOptions.overrideOf(SIZE_ORIGINAL)).into(iv_before)
                }
            }
        }
    }

    private fun uri2Bitmap(block: (Bitmap) -> Unit) {
        Glide.with(this)
                .asBitmap()
                .load(curImageUri)
                .apply(RequestOptions.overrideOf(SIZE_ORIGINAL))
                .into(object : SimpleTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        block.invoke(resource)
                    }

                })
    }

    private fun String.toast() {
        Log.d(TAG, "toast: $this")
        handler.post {
            status.text = this
            Toast.makeText(this@MainActivity, this, Toast.LENGTH_SHORT).show()
        }
    }
}