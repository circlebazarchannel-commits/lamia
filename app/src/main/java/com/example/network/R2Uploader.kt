package com.example.network

import android.content.Context
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.InputStream
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object R2Uploader {

    // Decode base64 to keep credentials obfuscated
    private fun decodeBase64(value: String): String {
        return String(Base64.decode(value, Base64.DEFAULT)).trim()
    }

    private val ACCOUNT_ID = decodeBase64("MDRmY2IzMzRmYTA3YTZhYTQwYTgxNjBiNzc2ZTBkOGQ=")
    private val ACCESS_KEY_ID = decodeBase64("NjhmN2E0NDYxY2VjNTc1Mjk0YTY2YjliZTlkOTkxODNhMzllMjU1YzkwZDU1ZTdkZmY2ZTJhNzgzOTQ5NmI2ZQ==")
    private val SECRET_ACCESS_KEY = decodeBase64("ODliODZkOGY1OTgxMjlkYWUyYmVkMjg1MjdjN2U1ZjI=")
    private val PUBLIC_URL = decodeBase64("aHR0cHM6Ly9wdWItMDRmY2IzMzRmYTA3YTZhYTQwYTgxNjBiNzc2ZTBkOGQucjIuZGV2")
    private const val BUCKET = "media"
    private const val REGION = "auto"
    private const val SERVICE = "s3"

    private fun hmacSha256(key: ByteArray, data: String): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key, "HmacSHA256"))
        return mac.doFinal(data.toByteArray(Charsets.UTF_8))
    }

    private fun sha256(data: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val res = digest.digest(data.toByteArray(Charsets.UTF_8))
        return res.joinToString("") { "%02x".format(it) }
    }

    private fun getSignatureKey(key: String, dateStamp: String, regionName: String, serviceName: String): ByteArray {
        val kDate = hmacSha256(("AWS4" + key).toByteArray(Charsets.UTF_8), dateStamp)
        val kRegion = hmacSha256(kDate, regionName)
        val kService = hmacSha256(kRegion, serviceName)
        val kSigning = hmacSha256(kService, "aws4_request")
        return kSigning
    }

    suspend fun uploadFile(
        context: Context,
        fileUri: Uri,
        ext: String,
        onProgress: (Float) -> Unit
    ): String = withContext(Dispatchers.IO) {
        val filename = "upload_${System.currentTimeMillis()}.$ext"
        val contentType = if (ext.lowercase() == "mp4") "video/mp4" else "image/jpeg"
        
        val contentResolver = context.contentResolver
        var fileLength = 0L
        contentResolver.query(fileUri, null, null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
            if (sizeIndex != -1 && cursor.moveToFirst()) {
                fileLength = cursor.getLong(sizeIndex)
            }
        }
        
        if (fileLength <= 0) {
            contentResolver.openInputStream(fileUri)?.use {
                fileLength = it.available().toLong()
            }
        }

        val gmtFormat = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("GMT")
        }
        val dateStampFormat = SimpleDateFormat("yyyyMMdd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("GMT")
        }

        val now = Date()
        val amzDate = gmtFormat.format(now)
        val dateStamp = dateStampFormat.format(now)

        // Host header
        val host = "$ACCOUNT_ID.r2.cloudflarestorage.com"
        
        // Canonical Request parameters
        val httpMethod = "PUT"
        val canonicalUri = "/$BUCKET/$filename"
        val canonicalQueryString = ""
        
        val canonicalHeaders = "host:$host\n" +
                "x-amz-content-sha256:UNSIGNED-PAYLOAD\n" +
                "x-amz-date:$amzDate\n"
        
        val signedHeaders = "host;x-amz-content-sha256;x-amz-date"
        val payloadHash = "UNSIGNED-PAYLOAD"

        val canonicalRequest = "$httpMethod\n" +
                "$canonicalUri\n" +
                "$canonicalQueryString\n" +
                "$canonicalHeaders\n" +
                "$signedHeaders\n" +
                payloadHash

        val hashedCanonicalRequest = sha256(canonicalRequest)
        val credentialScope = "$dateStamp/$REGION/$SERVICE/aws4_request"
        
        val stringToSign = "AWS4-HMAC-SHA256\n" +
                "$amzDate\n" +
                "$credentialScope\n" +
                hashedCanonicalRequest

        val signingKey = getSignatureKey(SECRET_ACCESS_KEY, dateStamp, REGION, SERVICE)
        val signatureBytes = hmacSha256(signingKey, stringToSign)
        val signature = signatureBytes.joinToString("") { "%02x".format(it) }

        val authHeader = "AWS4-HMAC-SHA256 Credential=$ACCESS_KEY_ID/$credentialScope, SignedHeaders=$signedHeaders, Signature=$signature"

        val uploadUrl = "https://$host/$BUCKET/$filename"

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(300, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        val requestBody = object : RequestBody() {
            override fun contentType() = contentType.toMediaTypeOrNull()
            override fun contentLength() = fileLength

            override fun writeTo(sink: BufferedSink) {
                val inputStream: InputStream = contentResolver.openInputStream(fileUri)
                    ?: throw java.io.FileNotFoundException("Could not open URI: $fileUri")
                val buffer = ByteArray(16384)
                var bytesRead: Int
                var totalBytesRead = 0L
                inputStream.use { input ->
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        sink.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead
                        if (fileLength > 0) {
                            val progress = (totalBytesRead.toFloat() / fileLength).coerceIn(0f, 1f)
                            onProgress(progress)
                        }
                    }
                }
            }
        }

        val request = Request.Builder()
            .url(uploadUrl)
            .put(requestBody)
            .header("Host", host)
            .header("x-amz-date", amzDate)
            .header("x-amz-content-sha256", "UNSIGNED-PAYLOAD")
            .header("Authorization", authHeader)
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "No error body"
                throw Exception("R2 upload failed with code ${response.code}: $errorBody")
            }
        }

        "$PUBLIC_URL/$filename"
    }
}
