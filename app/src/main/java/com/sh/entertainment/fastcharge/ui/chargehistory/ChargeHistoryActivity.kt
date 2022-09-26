package com.sh.entertainment.fastcharge.ui.chargehistory

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
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
import com.sh.entertainment.fastcharge.common.util.AdsManager
import com.sh.entertainment.fastcharge.databinding.ActivityChargeHistoryBinding
import com.sh.entertainment.fastcharge.ui.base.BaseActivityBinding
import java.lang.String
import java.text.DateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.Array
import kotlin.Float
import kotlin.Int
import kotlin.Long
import kotlin.math.roundToInt

class ChargeHistoryActivity : BaseActivityBinding<ActivityChargeHistoryBinding>() {
    private lateinit var chargeAdapter: ChartAdapter
    private lateinit var vpPager: ViewPager
    private lateinit var chart: PieChart

    var imgColor: ImageView? = null
    var indicator: TabLayout? = null
    var tvDate: TextView? = null

    override val layoutId = R.layout.activity_charge_history

    override fun initializeView() {
        initView()
        registerListener()
    }

    override fun initializeData() {
        AdsManager.showNativeAd(this, dataBinding.nativeAdView, AdsManager.NATIVE_AD_KEY)
    }

    override fun onClick() {

    }

    private fun initView() {
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
                imgColor!!.visibility = View.VISIBLE
                setPercent(highlight.x)
            }

            override fun onNothingSelected() {
                imgColor!!.visibility = View.INVISIBLE
                dataBinding.tvCount.text =
                    String.valueOf(SharePreferenceUtils.getInstance(this@ChargeHistoryActivity).chargeNormal)
            }
        })
        viewPagerListenItem()
    }

    @SuppressLint("SetTextI18n")
    fun setPercent(f: Float) {
        val chargeNormal = SharePreferenceUtils.getInstance(this).chargeNormal.toFloat()
        val chargeHealthy = SharePreferenceUtils.getInstance(this).chargeHealthy.toFloat()
        val chargeOver = SharePreferenceUtils.getInstance(this).chargeOver.toFloat()
        val f2 = chargeNormal + chargeHealthy + chargeOver
        when (f) {
            0.0f -> {
                imgColor!!.setBackgroundResource(R.drawable.shape_status_normal)
                dataBinding.tvCount.text =
                    (chargeNormal / f2 * 100.0f).roundToInt().toString() + "%"
            }
            1.0f -> {
                dataBinding.tvCount.text =
                    (chargeHealthy / f2 * 100.0f).roundToInt().toString() + "%"
                imgColor!!.setBackgroundResource(R.drawable.shape_status_healthy)
            }
            else -> {
                dataBinding.tvCount.text = (chargeOver / f2 * 100.0f).roundToInt().toString() + "%"
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
        dataBinding.lrBack.setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        intData()
    }

    @SuppressLint("SetTextI18n")
    fun intData() {
        if (SharePreferenceUtils.getInstance(this).chargeNormal != 0L) {
            dataBinding.tvNormal.text = String.valueOf(
                SharePreferenceUtils.getInstance(this).chargeNormal
            )
        }
        if (SharePreferenceUtils.getInstance(this).chargeOver != 0L) {
            dataBinding.tvOver.text = String.valueOf(
                SharePreferenceUtils.getInstance(this).chargeOver
            )
        }
        if (SharePreferenceUtils.getInstance(this).chargeHealthy != 0L) {
            dataBinding.tvHealthy.text = String.valueOf(
                SharePreferenceUtils.getInstance(this).chargeHealthy
            )
        }
        if (SharePreferenceUtils.getInstance(this).chargeFull != null) {
            dataBinding.tvLastFull.text = SharePreferenceUtils.getInstance(this).chargeFull
        }
        dataBinding.tvChargeType.text = SharePreferenceUtils.getInstance(this).chargeType
        dataBinding.tvTimeCharge.text =
            formatToDigitalClock(SharePreferenceUtils.getInstance(this).timeCharge)
        dataBinding.tvQuantity.text =
            SharePreferenceUtils.getInstance(this).chargeQuantity.toString() + "%"
        setData()
    }

    @SuppressLint("DefaultLocale")
    private fun formatToDigitalClock(miliSeconds: Long): kotlin.String {
        val hours = TimeUnit.MILLISECONDS.toHours(miliSeconds).toInt() % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(miliSeconds).toInt() % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(miliSeconds).toInt() % 60
        return when {
            hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, seconds)
            minutes > 0 -> String.format("%02d:%02d", minutes, seconds)
            seconds > 0 -> String.format("00:%02d", seconds)
            else -> "00:00"
        }
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