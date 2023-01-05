package tech.linjiang.app.docscandemo

import com.tencentcloudapi.common.Credential
import com.tencentcloudapi.common.profile.ClientProfile
import com.tencentcloudapi.common.profile.HttpProfile
import com.tencentcloudapi.ocr.v20181119.OcrClient
import com.tencentcloudapi.ocr.v20181119.models.ImageEnhancementRequest


fun enhancement(type: Int, base64Image: String): String {
    val req = ImageEnhancementRequest().apply {
        imageBase64 = base64Image
        taskType = type.toLong()
        returnImage = "preprocess"
    }
    val resp = OcrClient(makeCredential(), "ap-beijing", makeClientProfile()).ImageEnhancement(req)
    println("enhancement success: ${resp.requestId}")
    return resp.image
}

private fun makeCredential() =
    Credential(BuildConfig.ocrSecretId, BuildConfig.ocrSecretKey)

private fun makeClientProfile() =
    ClientProfile().apply {
        httpProfile = HttpProfile().apply { endpoint = "ocr.tencentcloudapi.com" }
    }
