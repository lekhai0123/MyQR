package com.example.myqr

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import androidx.core.content.FileProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat

class QuickQRActivity : Activity() {

    private lateinit var etAmount: EditText
    private lateinit var ivQr: ImageView
    private val formatter = DecimalFormat("#,###")

    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private var qrRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quick_qr)

        etAmount = findViewById(R.id.et_amount)
        ivQr = findViewById(R.id.iv_qr)

        val rootView = findViewById<FrameLayout>(R.id.root_view)
        rootView.setOnClickListener {
            finish() // Close when tapping outside
        }

        val btnShare = findViewById<ImageButton>(R.id.btn_share)
        btnShare.setOnClickListener {
            shareQRCode()
        }

        var isFormatting = false
        etAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s == null || isFormatting) return
                
                val currentText = s.toString()
                val rawNumber = currentText.replace(Regex("[^0-9]"), "")
                var amount: Long = 0
                val formatted: String
                
                if (rawNumber.isNotEmpty()) {
                    amount = rawNumber.toLong()
                    formatted = formatter.format(amount)
                } else {
                    formatted = ""
                }

                if (currentText != formatted) {
                    isFormatting = true
                    val diff = formatted.length - currentText.length
                    val oldSel = etAmount.selectionStart
                    
                    s.replace(0, s.length, formatted)
                    
                    var newSel = oldSel + diff
                    if (newSel < 0) newSel = 0
                    if (newSel > formatted.length) newSel = formatted.length
                    etAmount.setSelection(newSel)
                    
                    isFormatting = false
                }

                scheduleQRGeneration(amount, 500)
            }
        })

        // Initial generation (Sync to prevent white screen)
        generateQRSync(0)

        // Show soft keyboard immediately
        etAmount.requestFocus()
        etAmount.postDelayed({
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(etAmount, InputMethodManager.SHOW_IMPLICIT)
        }, 100)
    }

    private fun generateQRSync(amount: Long) {
        val payload = buildVietQRString(amount)
        val bitmap = encodeAsBitmap(payload)
        if (bitmap != null) {
            ivQr.setImageBitmap(bitmap)
        }
    }

    private fun shareQRCode() {
        val drawable = ivQr.drawable as? android.graphics.drawable.BitmapDrawable ?: return
        val bitmap = drawable.bitmap

        try {
            val cachePath = File(cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "vietqr_share.png")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()

            val uri = FileProvider.getUriForFile(
                this,
                "$packageName.fileprovider",
                file
            )

            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(android.content.Intent.createChooser(shareIntent, "Share VietQR"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun scheduleQRGeneration(amount: Long, delay: Long) {
        qrRunnable?.let { handler.removeCallbacks(it) }
        qrRunnable = Runnable { generateQRAsync(amount) }
        handler.postDelayed(qrRunnable!!, delay)
    }

    private fun generateQRAsync(amount: Long) {
        Thread {
            val payload = buildVietQRString(amount)
            val bitmap = encodeAsBitmap(payload)
            runOnUiThread {
                if (bitmap != null) {
                    ivQr.setImageBitmap(bitmap)
                }
            }
        }.start()
    }

    private fun buildVietQRString(amount: Long): String {
        val bankBin = "970422"
        val accountNumber = "0767681248"
        val guid = "A000000727"
        val serviceCode = "QRIBFTTA"
        val content = "CHUYEN TIEN NHANH"

        var payload = ""
        payload += formatTag("00", "01")
        payload += formatTag("01", "12")

        var merchantInfo = ""
        merchantInfo += formatTag("00", guid)

        var consumerInfo = ""
        consumerInfo += formatTag("00", bankBin)
        consumerInfo += formatTag("01", accountNumber)

        merchantInfo += formatTag("01", consumerInfo)
        merchantInfo += formatTag("02", serviceCode)

        payload += formatTag("38", merchantInfo)
        payload += formatTag("53", "704")

        if (amount > 0) {
            payload += formatTag("54", amount.toString())
        }

        payload += formatTag("58", "VN")

        if (content.isNotEmpty()) {
            val additionalData = formatTag("08", content)
            payload += formatTag("62", additionalData)
        }

        payload += "6304"
        val crc = calculateCRC16(payload)
        return payload + crc
    }

    private fun formatTag(id: String, value: String): String {
        val length = value.length.toString().padStart(2, '0')
        return "$id$length$value"
    }

    private fun calculateCRC16(data: String): String {
        var crc = 0xFFFF
        for (i in data.indices) {
            crc = crc xor (data[i].code shl 8)
            for (j in 0..7) {
                crc = if ((crc and 0x8000) != 0) {
                    ((crc shl 1) xor 0x1021) and 0xFFFF
                } else {
                    (crc shl 1) and 0xFFFF
                }
            }
        }
        return crc.toString(16).uppercase().padStart(4, '0')
    }

    private fun encodeAsBitmap(str: String): Bitmap? {
        try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(str, BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }
            return bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
