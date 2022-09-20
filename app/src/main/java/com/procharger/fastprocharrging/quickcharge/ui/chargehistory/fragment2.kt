package com.procharger.fastprocharrging.quickcharge.ui.chargehistory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.formatter.IFillFormatter
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.Utils
import com.procharger.fastprocharrging.quickcharge.R
import java.lang.Boolean
import java.util.*

class fragment2 : Fragment() {
    var chart: LineChart? = null
    var page = 0
    var set2: LineDataSet? = null
    var title: String? = null
    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        page = arguments!!.getInt("someInt", 0)
        title = arguments!!.getString("someTitle")
    }

    override fun onCreateView(
        layoutInflater: LayoutInflater,
        viewGroup: ViewGroup?,
        bundle: Bundle?
    ): View? {
        val inflate: View = layoutInflater.inflate(R.layout.fragment_today, viewGroup, false)
        chart = inflate.findViewById<View>(R.id.today_chart) as LineChart
        initHistoryChart()
        return inflate
    }

    inner class MyXaxisValueFormater(private val mValues: Array<String>) :
        IAxisValueFormatter {
        override fun getFormattedValue(f: Float, axisBase: AxisBase): String {
            return mValues[f.toInt()]
        }
    }

    fun initHistoryChart() {
        Boolean.valueOf(true)
        val arrayList: ArrayList<Entry> = ArrayList()
        val instance = Calendar.getInstance()
        instance.add(5, -1)
        val i = instance[5]
        var i2 = 0
        for (i3 in 0..23) {
            val level = HistoryPref.getLevel(context!!, HistoryPref.getKeyFromTime(i, i3))
            if (i3 != 0) {
                if (level != -1) {
                    if (i3 % 2 == 0) {
                        arrayList.add(
                            Entry(
                                i3.toFloat(),
                                level.toFloat()
                            )
                        )
                    }
                } else if (i2 != 0 && i3 % 2 == 0) {
                    arrayList.add(
                        Entry(
                            i3.toFloat(),
                            i2.toFloat()
                        )
                    )
                }
            } else if (level != -1) {
                arrayList.add(Entry(0.0f, level.toFloat()))
            } else {
                arrayList.add(Entry(0.0f, 0.0f))
            }
            i2 = level
        }
        val instance2 = Calendar.getInstance()
        instance2.add(11, 1)
        val level2 = HistoryPref.getLevel(context!!, HistoryPref.getKeyFromTime(instance2[5], 0))
        if (level2 != -1) {
            arrayList.add(Entry(24.0f, level2.toFloat()))
        } else {
            arrayList.add(Entry(24.0f, 0.0f))
        }
        if (i2 != 0) {
            val lineDataSet = LineDataSet(arrayList, "")
            set2 = lineDataSet
            lineDataSet.fillAlpha = 100
            set2!!.color = -6579542
            set2!!.lineWidth = 1.0f
            set2!!.setDrawValues(false)
            set2!!.setCircleColor(ContextCompat.getColor(context!!, R.color.point_chart))
            set2!!.setDrawFilled(true)
            set2!!.fillColor = -9339154
            set2!!.fillFormatter =
                IFillFormatter { iLineDataSet, lineDataProvider -> chart!!.axisLeft.axisMinimum }
            if (Utils.getSDKInt() >= 18) {
                set2!!.fillDrawable = ContextCompat.getDrawable(context!!, R.drawable.fill_green)
            } else {
                set2!!.fillColor = ViewCompat.MEASURED_STATE_MASK
            }
            val arrayList2: ArrayList<LineDataSet> = ArrayList()
            arrayList2.add(set2!!)
            chart!!.data = LineData(arrayList2 as List<ILineDataSet?>)
            val xAxis = chart!!.xAxis
            xAxis.textColor = ContextCompat.getColor(context!!, R.color.title_dark)
            xAxis.axisMaximum = 24.0f
            xAxis.axisMinimum = 0.0f
            xAxis.labelCount = 6
            xAxis.valueFormatter = MyXaxisValueFormater(
                arrayOf(
                    "00:00",
                    "",
                    "",
                    "",
                    "04:00",
                    "",
                    "",
                    "",
                    "08:00",
                    "",
                    "",
                    "",
                    "12:00",
                    "",
                    "",
                    "",
                    "16:00",
                    "",
                    "",
                    "",
                    "20:00",
                    "",
                    "",
                    "",
                    "24:00"
                )
            )
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            val axisLeft = chart!!.axisLeft
            axisLeft.textColor = ContextCompat.getColor(context!!, R.color.title_dark)
            axisLeft.axisMaximum = 100.0f
            axisLeft.axisMinimum = 0.0f
            axisLeft.labelCount = 6
            chart!!.axisRight.isEnabled = false
            chart!!.xAxis.setDrawGridLines(false)
            axisLeft.valueFormatter = MyXaxisValueFormater(
                arrayOf(
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "20%",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "40%",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "60%",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "80%",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "100%"
                )
            )
            axisLeft.enableGridDashedLine(10.0f, 10.0f, 0.0f)
            chart!!.description = null as Description?
            chart!!.xAxis.setDrawAxisLine(false)
            axisLeft.setDrawAxisLine(false)
            chart!!.legend.isEnabled = false
            chart!!.setTouchEnabled(false)
        }
    }

    override fun onResume() {
        super.onResume()
        initHistoryChart()
    }

    companion object {
        fun newInstance(i: Int, str: String?): fragment2 {
            val fragment2 = fragment2()
            val bundle = Bundle()
            bundle.putInt("someInt", i)
            bundle.putString("someTitle", str)
            fragment2.arguments = bundle
            return fragment2
        }
    }
}
