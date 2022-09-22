package com.sh.entertainment.fastcharge.ui.chargehistory

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.MPPointF
import com.google.android.material.tabs.TabLayout
import com.sh.entertainment.fastcharge.R
import java.lang.String
import java.text.DateFormat
import java.util.*
import kotlin.Array
import kotlin.Float
import kotlin.Int
import kotlin.Long
import kotlin.getValue
import kotlin.lazy
import kotlin.math.roundToInt

class ChargeHistoryActivity : AppCompatActivity() {
    private lateinit var chargeAdapter: ChartAdapter
    private lateinit var vpPager: ViewPager
    private lateinit var chart: PieChart

    var imgColor: ImageView? = null
    var indicator: TabLayout? = null
    var tvChargeType: TextView? = null
    var tvCount: TextView? = null
    var tvDate: TextView? = null
    var tvHealthy: TextView? = null
    var tvLastFull: TextView? = null
    var tvNormal: TextView? = null
    var tvOver: TextView? = null
    var tvQuantity: TextView? = null
    var tvTimeCharge: TextView? = null
    private val btnBack by lazy { findViewById<LinearLayout>(R.id.lr_back) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_charge_history)
        initView()
        registerListener()
    }

    private fun initView() {
        tvNormal = findViewById<View>(R.id.tvNormal) as TextView
        tvOver = findViewById<View>(R.id.tvOver) as TextView
        tvHealthy = findViewById<View>(R.id.tvHealthy) as TextView
        tvLastFull = findViewById<View>(R.id.tvLastFull) as TextView
        tvChargeType = findViewById<View>(R.id.tvChargeType) as TextView
        tvTimeCharge = findViewById<View>(R.id.tvTimeCharge) as TextView
        tvQuantity = findViewById<View>(R.id.tvQuantity) as TextView
        tvCount = findViewById<View>(R.id.tvCount) as TextView
        imgColor = findViewById<View>(R.id.imgColor) as ImageView
        tvDate = findViewById<View>(R.id.tvDate) as TextView
        vpPager = (findViewById<View>(R.id.viewPager) as ViewPager)

        chargeAdapter = ChartAdapter(supportFragmentManager)
        vpPager.adapter = chargeAdapter
        vpPager.currentItem = 2

        val tabLayout = findViewById<View>(R.id.indicator) as TabLayout
        indicator = tabLayout
        tabLayout.setupWithViewPager(vpPager, true)

        initChart()
        chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(entry: Entry, highlight: Highlight) {
                if (entry != null) {
                    Log.i(
                        "VAL SELECTED",
                        "Value: " + entry.y + ", index: " + highlight.x + ", DataSet index: " + highlight.dataSetIndex
                    )
                    imgColor!!.visibility = View.VISIBLE
                    setPercent(highlight.x)
                }
            }

            override fun onNothingSelected() {
                imgColor!!.visibility = View.INVISIBLE
                tvCount!!.text = String.valueOf(SharePreferenceUtils.getInstance(this@ChargeHistoryActivity).chargeNormal)
            }
        })
        viewPagerListenItem()
    }

    fun setPercent(f: Float) {
        val chargeNormal = SharePreferenceUtils.getInstance(this).chargeNormal.toFloat()
        val chargeHealthy = SharePreferenceUtils.getInstance(this).chargeHealthy.toFloat()
        val chargeOver = SharePreferenceUtils.getInstance(this).chargeOver.toFloat()
        val f2 = chargeNormal + chargeHealthy + chargeOver
        when (f) {
            0.0f -> {
                imgColor!!.setBackgroundResource(R.drawable.shape_status_normal)
                val textView = tvCount
                textView!!.text = (chargeNormal / f2 * 100.0f).roundToInt().toString() + "%"
            }
            1.0f -> {
                val textView2 = tvCount
                textView2!!.text = (chargeHealthy / f2 * 100.0f).roundToInt().toString() + "%"
                imgColor!!.setBackgroundResource(R.drawable.shape_status_healthy)
            }
            else -> {
                val textView3 = tvCount
                textView3!!.text = (chargeOver / f2 * 100.0f).roundToInt().toString() + "%"
                imgColor!!.setBackgroundResource(R.drawable.shape_status_over)
            }
        }
    }

    private fun initChart() {
        val pieChart = findViewById<View>(R.id.chartPie) as PieChart
        chart = pieChart
        pieChart.setUsePercentValues(false)
        chart.description.isEnabled = false
        chart.isDrawHoleEnabled = true
        chart.setHoleColor(0)
        chart.setTransparentCircleColor(0)
        chart.setTransparentCircleAlpha(110)
        chart.holeRadius = 58.0f
        chart.transparentCircleRadius = 61.0f
        chart.setDrawCenterText(true)
        chart.rotationAngle = 0.0f
        chart.isRotationEnabled = true
        chart.isHighlightPerTapEnabled = true
        chart.description = null
        chart.legend.isEnabled = false
    }

    fun viewPagerListenItem() {
        if (tvDate == null) return
        this.tvDate!!.text = DateFormat.getDateInstance().format(Calendar.getInstance().time)
        vpPager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrollStateChanged(i: Int) {
                Log.e("fff", "")
            }

            override fun onPageScrolled(i: Int, f: Float, i2: Int) {
                Log.e("fff", "")
            }

            override fun onPageSelected(i: Int) {
                if (i == 0) {
                    val instance = Calendar.getInstance()
                    instance.add(5, -2)
                    tvDate!!.text = DateFormat.getDateInstance().format(instance.time)
                }
                if (i == 1) {
                    val instance2 = Calendar.getInstance()
                    instance2.add(5, -1)
                    tvDate!!.text = DateFormat.getDateInstance().format(instance2.time)
                }
                if (i == 2) {
                    tvDate!!.text = DateFormat.getDateInstance().format(Calendar.getInstance().time)
                }
            }
        })
    }

    private fun registerListener() {
        btnBack.setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        intData()
    }

    @SuppressLint("SetTextI18n")
    fun intData() {
        if (SharePreferenceUtils.getInstance(this).chargeNormal != 0L) {
            tvNormal!!.text = String.valueOf(
                SharePreferenceUtils.getInstance(this).chargeNormal
            )
        }
        if (SharePreferenceUtils.getInstance(this).chargeOver != 0L) {
            tvOver!!.text = String.valueOf(
                SharePreferenceUtils.getInstance(this).chargeOver
            )
        }
        if (SharePreferenceUtils.getInstance(this).chargeHealthy != 0L) {
            tvHealthy!!.text = String.valueOf(
                SharePreferenceUtils.getInstance(this).chargeHealthy
            )
        }
        if (SharePreferenceUtils.getInstance(this).chargeFull != null) {
            tvLastFull!!.text = SharePreferenceUtils.getInstance(this).chargeFull
        }
        tvChargeType!!.text = SharePreferenceUtils.getInstance(this).chargeType
        tvTimeCharge!!.text =
            formatHourMinutune(SharePreferenceUtils.getInstance(this).timeCharge)
        val textView = tvQuantity
        textView!!.text = SharePreferenceUtils.getInstance(this).chargeQuantity.toString() + "%"
        setData()
    }

    fun formatHourMinutune(j: Long): kotlin.String {
        val str: kotlin.String
        val j2 = j / 1000 % 60
        val j3 = j / 60 * 1000 % 60
        val j4 = j / 3600000
        val sb = StringBuilder()
        var str2 = "00"
        str = if (j4 == 0L) {
            str2
        } else if (j4 < 10) {
            "0$j4"
        } else {
            j4.toString()
        }
        sb.append(str)
        sb.append(":")
        if (j3 != 0L) {
            str2 = if (j3 < 10) {
                "0$j3"
            } else {
                j3.toString()
            }
        }
        sb.append(str2)
        return sb.toString()
    }

    private fun setData() {
        var f: Float
        val arrayList: ArrayList<PieEntry> = ArrayList()
        SharePreferenceUtils.getInstance(this).chargeNormal.toDouble().roundToInt()
        val chargeNormal = SharePreferenceUtils.getInstance(this).chargeNormal.toFloat()
        val chargeHealthy = SharePreferenceUtils.getInstance(this).chargeHealthy.toFloat()
        val chargeOver = SharePreferenceUtils.getInstance(this).chargeOver.toFloat()
        if (chargeNormal == 0.0f && chargeHealthy == 0.0f && chargeOver == 0.0f) {
            arrayList.add(PieEntry(40.0f, 0))
            arrayList.add(PieEntry(1.0f, 1))
            arrayList.add(PieEntry(1.0f, 2))
            f = chargeNormal
        } else {
            f = if (chargeNormal < chargeHealthy) chargeHealthy else chargeNormal
            if (f < chargeOver) {
                f = chargeOver
            }
        }
        val f2 = f / 40.0f
        if (chargeNormal <= f2) {
            arrayList.add(PieEntry(f2, 0))
        } else {
            arrayList.add(PieEntry(chargeNormal, 0))
        }
        if (chargeHealthy <= f2) {
            arrayList.add(PieEntry(f2, 0))
        } else {
            arrayList.add(PieEntry(chargeHealthy, 0))
        }
        if (chargeOver <= f2) {
            arrayList.add(PieEntry(f2, 0))
        } else {
            arrayList.add(PieEntry(chargeOver, 0))
        }
        val pieDataSet = PieDataSet(arrayList, "")
        pieDataSet.setDrawIcons(false)
        pieDataSet.sliceSpace = 3.0f
        pieDataSet.iconsOffset = MPPointF(0.0f, 40.0f)
        pieDataSet.selectionShift = 5.0f
        val arrayList2: ArrayList<Int> = ArrayList()
        arrayList2.add(Integer.valueOf(ContextCompat.getColor(this, R.color.color_normal)))
        arrayList2.add(Integer.valueOf(ContextCompat.getColor(this, R.color.color_healthy)))
        arrayList2.add(Integer.valueOf(ContextCompat.getColor(this, R.color.color_over)))
        pieDataSet.colors = arrayList2
        pieDataSet.xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        val pieData = PieData(pieDataSet)
        pieData.setValueTextSize(11.0f)
        pieData.setValueTextColor(-1)
        pieData.setDrawValues(false)
        chart.data = pieData
        chart.setDrawSliceText(false)
        chart.highlightValues(null as Array<Highlight?>?)
        chart.invalidate()
    }

}