package com.entertainment.basemvvmproject.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.MediaStore
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

fun UUID.toLongUUID(): Long = this.mostSignificantBits and Long.MAX_VALUE

fun String.vietnameseToEnglish(): String {
    var str = this
    arrayOf(
        'à', 'á', 'ạ', 'ả', 'ã', 'â', 'ầ', 'ấ', 'ậ', 'ẩ', 'ẫ', 'ă', 'ằ', 'ắ', 'ặ', 'ẳ', 'ẵ'
    ).forEach {
        str = str.replace(it, 'a')
    }

    arrayOf('è', 'é', 'ẹ', 'ẻ', 'ẽ', 'ê', 'ề', 'ế', 'ệ', 'ể', 'ễ').forEach {
        str = str.replace(it, 'e')
    }

    arrayOf('ì', 'í', 'ị', 'ỉ', 'ĩ').forEach {
        str = str.replace(it, 'i')
    }

    arrayOf(
        'ò', 'ó', 'ọ', 'ỏ', 'õ', 'ô', 'ồ', 'ố', 'ộ', 'ổ', 'ỗ', 'ơ', 'ờ', 'ớ', 'ợ', 'ở', 'ỡ'
    ).forEach {
        str = str.replace(it, 'o')
    }

    arrayOf('ù', 'ú', 'ụ', 'ủ', 'ũ', 'ư', 'ừ', 'ứ', 'ự', 'ử', 'ữ').forEach {
        str = str.replace(it, 'u')
    }

    arrayOf('ỳ', 'ý', 'ỵ', 'ỷ', 'ỹ').forEach {
        str = str.replace(it, 'y')
    }

    arrayOf('đ').forEach {
        str = str.replace(it, 'd')
    }

    arrayOf(
        'À', 'Á', 'Ạ', 'Ả', 'Ã', 'Â', 'Ầ', 'Ấ', 'Ậ', 'Ẩ', 'Ẫ', 'Ă', 'Ằ', 'Ắ', 'Ặ', 'Ẳ', 'Ẵ'
    ).forEach {
        str = str.replace(it, 'A')
    }

    arrayOf('È', 'É', 'Ẹ', 'Ẻ', 'Ẽ', 'Ê', 'Ề', 'Ế', 'Ệ', 'Ể', 'Ễ').forEach {
        str = str.replace(it, 'E')
    }

    arrayOf('Ì', 'Í', 'Ị', 'Ỉ', 'Ĩ').forEach {
        str = str.replace(it, 'I')
    }

    arrayOf(
        'Ò', 'Ó', 'Ọ', 'Ỏ', 'Õ', 'Ô', 'Ồ', 'Ố', 'Ộ', 'Ổ', 'Ỗ', 'Ơ', 'Ờ', 'Ớ', 'Ợ', 'Ở', 'Ỡ'
    ).forEach {
        str = str.replace(it, 'O')
    }


    arrayOf('Ù', 'Ú', 'Ụ', 'Ủ', 'Ũ', 'Ư', 'Ừ', 'Ứ', 'Ự', 'Ử', 'Ữ').forEach {
        str = str.replace(it, 'U')
    }

    arrayOf('Ỳ', 'Ý', 'Ỵ', 'Ỷ', 'Ỹ').forEach {
        str = str.replace(it, 'Y')
    }

    arrayOf('Đ').forEach {
        str = str.replace(it, 'D')
    }

    return str
}

fun Long.toDateFormat(pattern: String): String {
    val date = Date(this)
    return SimpleDateFormat(pattern).format(date)
}

fun Long.toDateFormat(): String {
    return this.toDateFormat("HH:mm:ss dd/MM/yyyy")
}

fun Long.toDateFormatFilter(): String {
    return this.toDateFormat("HH:mm dd/MM/yyyy")
}

fun InputStream.toFile(file: File) {
    file.outputStream().use { this.copyTo(it) }
}

fun Long.millisecondToString(): String {
    val time = this
    return when (time) {
        in (0 until 59_999) -> "%ss".format(time / 1000)

        in (60_000 until 3_599_999) -> {
            val minute = time / 60_000
            val second = time % 60_000
            "%sm".format(minute) + second.millisecondToString()
        }

        in (3_600_000 until 86_400_000) -> {
            val hour = time / 3_600_000
            val minute = time % 3_600_000
            "%sh".format(hour) + minute.millisecondToString()
        }

        else -> ""

    }
}

fun Long.getEndTimeOfDay(): Long {
    val startTime = this.getStartTimeOfDay()
    return startTime + 86_400_000 - 1
}

fun Long.getYear(): Int {
    val c = Calendar.getInstance()
    c.timeInMillis = this
    return c.get(Calendar.YEAR)
}

fun Long.getMonth(): Int {
    val c = Calendar.getInstance()
    c.timeInMillis = this
    return c.get(Calendar.MONTH)
}

fun Long.getDay(): Int {
    val c = Calendar.getInstance()
    c.timeInMillis = this
    return c.get(Calendar.DAY_OF_MONTH)
}

fun Long.getHour(): Int {
    val c = Calendar.getInstance()
    c.timeInMillis = this
    return c.get(Calendar.HOUR_OF_DAY)
}

fun Long.getMinute(): Int {
    val c = Calendar.getInstance()
    c.timeInMillis = this
    return c.get(Calendar.MINUTE)
}

fun String.getTime(): Long {
    val sdf = SimpleDateFormat("HH:mm dd/MM/yyyy")
    val date = sdf.parse(this)
    return date.time
}


fun Calendar.isSameDay(otherDay: Calendar): Boolean {
    val currentDay = this
    return (currentDay.get(Calendar.DAY_OF_YEAR) == otherDay.get(Calendar.DAY_OF_YEAR)
            && currentDay.get(Calendar.YEAR) == otherDay.get(Calendar.YEAR))
}


//region View Extension
fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

fun EditText.maxLength(length: Int) {
    val maxLengthFilter = InputFilter.LengthFilter(length)
    this.filters = arrayOf(maxLengthFilter)
}

//endregion View Extension

fun Any.toString(): String {
    return Gson().toJson(this)
}

fun View.enable() {
    this.isEnabled = true
}

fun View.disable() {
    this.isEnabled = false
}

fun Context.getCurrentLocale(): Locale {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        this.resources.configuration.locales.get(0)
    } else {
        this.resources.configuration.locale
    }
}

fun String.getFirstChars(numberOfChar: Int): String {

    val stringWords = this.split("\\s+".toRegex()).map { word ->
        word.replace("""^[,\.]|[,\.]$""".toRegex(), "")
    }
    val firstChars = stringWords.map { it.take(1) }.take(numberOfChar)
    return firstChars.joinToString(",").filterNot { it == ',' }
}


/**
 * Get OH 0m 0s of current Date
 */
fun Calendar.currentDateStartTime(): Calendar {
    val startDate: Calendar
    Calendar.getInstance().apply {
        val year = get(Calendar.YEAR)
        val monthOfYear = get(Calendar.MONTH)
        val dayOfMonth = get(Calendar.DAY_OF_MONTH)
        startDate = GregorianCalendar(year, monthOfYear, dayOfMonth)
    }
    return startDate
}

fun Long.getStartTimeOfDay(): Long {
    val startDate: Calendar
    Calendar.getInstance().apply {
        timeInMillis = this@getStartTimeOfDay
        val year = get(Calendar.YEAR)
        val monthOfYear = get(Calendar.MONTH)
        val dayOfMonth = get(Calendar.DAY_OF_MONTH)
        startDate = GregorianCalendar(year, monthOfYear, dayOfMonth)
    }
    return startDate.timeInMillis
}


fun Calendar.previousDateStartTime(numberOfDay: Int): Calendar {
    val startDate: Calendar
    val previousDate = System.currentTimeMillis() - numberOfDay * 86_400_000
    Calendar.getInstance().apply {
        timeInMillis = previousDate
        val year = get(Calendar.YEAR)
        val monthOfYear = get(Calendar.MONTH)
        val dayOfMonth = get(Calendar.DAY_OF_MONTH)
        startDate = GregorianCalendar(year, monthOfYear, dayOfMonth)
    }
    return startDate
}

fun Calendar.followingDateStartTime(numberOfDay: Int): Calendar {
    val startDate: Calendar
    val previousDate = System.currentTimeMillis() + numberOfDay * 86_400_000
    Calendar.getInstance().apply {
        timeInMillis = previousDate
        val year = get(Calendar.YEAR)
        val monthOfYear = get(Calendar.MONTH)
        val dayOfMonth = get(Calendar.DAY_OF_MONTH)
        startDate = GregorianCalendar(year, monthOfYear, dayOfMonth)
    }
    return startDate
}


fun Uri.toBitmap(context: Context): Bitmap? {
    var bitmap: Bitmap? = null
    try {
        val parcelFileDescriptor = context.contentResolver.openFileDescriptor(this, "r")
        val fileDescriptor = parcelFileDescriptor?.fileDescriptor
        bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor?.close()
    } catch (e: IOException) {
    }
    return bitmap
}

fun Bitmap.toUri(context: Context, delete: Boolean = false): Uri? {
    val bytes = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
    val path: String? = MediaStore.Images.Media.insertImage(
        context.contentResolver,
        this,
        "IMG${System.currentTimeMillis()}",
        null
    )
    path?.let { it ->
        val data = Uri.parse(it)
        if (delete) {
            Uri.parse(it).deleteExternalStorage(context)
        }
        return data
    }
    return null

}

fun Uri.deleteExternalStorage(context: Context) {
    try {
        val check = context.contentResolver.delete(this, null, null)
    } catch (e: SecurityException) {
    }
}

fun String.getBitmapFromFilePath(): Bitmap? {
    var bitmap: Bitmap? = null
    try {
        val f = File(this)
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        bitmap = BitmapFactory.decodeStream(FileInputStream(f), null, options)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return bitmap
}

fun String.pathToBitmap(): Bitmap? {
    return try {
        val url = URL(this)
        val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
        connection.doInput = true
        connection.connect()
        val input: InputStream = connection.inputStream
        BitmapFactory.decodeStream(input)
    } catch (e: Exception) {
        Log.e(
            "awesome",
            "Error in getting notification image: " + e.localizedMessage
        )
        null
    }
}

fun InputStream.toBitmap(): Bitmap? {
    return try {
        BitmapFactory.decodeStream(this)
    } catch (e: java.lang.Exception) {
        null
    }
}


fun String.toRequestBody(): RequestBody {
    return this.toRequestBody("multipart/form-data".toMediaTypeOrNull())
}

fun List<String>.toRequestBody(key: String): HashMap<String, RequestBody> {
    val result = HashMap<String, RequestBody>()
    this.forEachIndexed { index, s ->
        val mapKey = "$key[$index]"
        result[mapKey] = s.toRequestBody()
    }
    return result

}
//endregion Multipart Image

fun Map<String, String>.mapToString(): String? {
    val stringBuilder = StringBuilder()
    for (key in this.keys) {
        if (stringBuilder.isNotEmpty()) {
            stringBuilder.append("&")
        }
        val value = this[key]
        try {
            stringBuilder.append(if (key != null) URLEncoder.encode(key, "UTF-8") else "")
            stringBuilder.append("=")
            stringBuilder.append(if (value != null) URLEncoder.encode(value, "UTF-8") else "")
        } catch (e: UnsupportedEncodingException) {
            throw RuntimeException("This method requires UTF-8 encoding support", e)
        }
    }
    return stringBuilder.toString()
}

fun String.toMap(): Map<String, String> {
    val map: MutableMap<String, String> = java.util.HashMap()
    val nameValuePairs = this.split("&".toRegex()).toTypedArray()
    for (nameValuePair in nameValuePairs) {
        val nameValue = nameValuePair.split("=".toRegex()).toTypedArray()
        try {
            map[URLDecoder.decode(nameValue[0], "UTF-8")] =
                if (nameValue.size > 1) URLDecoder.decode(
                    nameValue[1], "UTF-8"
                ) else ""
        } catch (e: UnsupportedEncodingException) {
            throw RuntimeException("This method requires UTF-8 encoding support", e)
        }
    }
    return map
}

fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }
    })
}

@SuppressLint("ClickableViewAccessibility", "UseCompatLoadingForDrawables")
fun EditText.onClickDrawableRight() {
    var isShowingPass: Boolean = true
    this.setOnTouchListener(View.OnTouchListener { v, event ->
        val DRAWABLE_RIGHT = 2
        if (event.action == MotionEvent.ACTION_UP) {
            if (event.rawX >= this.right - this.compoundDrawables[DRAWABLE_RIGHT].bounds.width()
            ) {
                // your action here
                if (isShowingPass) {
                    this.transformationMethod = HideReturnsTransformationMethod.getInstance()
                } else {
                    this.transformationMethod = PasswordTransformationMethod.getInstance()
                }
                isShowingPass = !isShowingPass
                return@OnTouchListener true
            }
        }
        false
    })
}

fun String.toBitmap(context: Context): Bitmap? {
    val uri = Uri.fromFile(File(this))
    var `in`: InputStream? = null
    try {
        val IMAGE_MAX_SIZE = 1200000 // 1.2MP
        `in` = context.contentResolver?.openInputStream(uri)

        // Decode image size
        var o = BitmapFactory.Options()
        o.inJustDecodeBounds = true
        BitmapFactory.decodeStream(`in`, null, o)
        `in`?.close()
        var scale = 1
        while (o.outWidth * o.outHeight * (1 / Math.pow(scale.toDouble(), 2.0)) >
            IMAGE_MAX_SIZE
        ) {
            scale++
        }

        var b: Bitmap? = null
        `in` = context.contentResolver?.openInputStream(uri)
        if (scale > 1) {
            scale--
            // scale to max possible inSampleSize that still yields an image
            // larger than target
            o = BitmapFactory.Options()
            o.inSampleSize = scale
            b = BitmapFactory.decodeStream(`in`, null, o)

            // resize to desired dimensions
            val height = b!!.height
            val width = b.width
            val y = Math.sqrt(
                IMAGE_MAX_SIZE
                        / (width.toDouble() / height)
            )
            val x = (y / height) * width
            val scaledBitmap = Bitmap.createScaledBitmap(
                (b), x.toInt(),
                y.toInt(), true
            )
            b.recycle()
            b = scaledBitmap
            System.gc()
        } else {
            b = BitmapFactory.decodeStream(`in`)
        }
        `in`?.close()
        return b
    } catch (e: IOException) {
        return null
    }
}

//region Context Ex
fun Context.vibrate(milliseconds: Long = 500) {
    val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    // Check whether device/hardware has a vibrator
    val canVibrate: Boolean = vibrator.hasVibrator()

    if (canVibrate) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // void vibrate (VibrationEffect vibe)
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    milliseconds,
                    // The default vibration strength of the device.
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            // This method was deprecated in API level 26
            vibrator.vibrate(milliseconds)
        }
    }
}
//endregion Context Ex

fun Activity.gotoActivity(activity: Class<*>) {
    this.startActivity(Intent(this, activity))
}
