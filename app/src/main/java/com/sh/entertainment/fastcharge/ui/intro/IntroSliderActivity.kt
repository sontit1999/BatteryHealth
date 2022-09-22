package com.sh.entertainment.fastcharge.ui.intro

import android.os.Bundle
import android.widget.ImageView
import androidx.core.view.size
import androidx.viewpager.widget.ViewPager
import com.sh.entertainment.fastcharge.R
import com.sh.entertainment.fastcharge.common.extension.ctx
import com.sh.entertainment.fastcharge.common.extension.openActivity
import com.sh.entertainment.fastcharge.common.extension.setOnSafeClickListener
import com.sh.entertainment.fastcharge.ui.base.BaseActivity
import com.sh.entertainment.fastcharge.ui.main.MainActivity
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator

class IntroSliderActivity : BaseActivity<IntroSliderView, IntroSliderPresenterImp>(),
    IntroSliderView {

    private val pagerIntroduction by lazy { findViewById<ViewPager>(R.id.pager_introduction) }
    private val indicator by lazy { findViewById<DotsIndicator>(R.id.indicator) }
    private val btnNext by lazy { findViewById<ImageView>(R.id.img_next) }

    private val pageChangedListener by lazy {
        object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                updateUI(position)
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initFragment()
    }

    override fun onDestroy() {
        pagerIntroduction.removeOnPageChangeListener(pageChangedListener)
        super.onDestroy()
    }

    override fun initView(): IntroSliderView {
        return this
    }

    override fun initPresenter(): IntroSliderPresenterImp {
        return IntroSliderPresenterImp(ctx)
    }

    override fun getLayoutId(): Int? = R.layout.activity_intro_slider

    override fun initWidgets() {
        // This screen doesn't use toolbar
        hideToolbarBase()

        // Hide navigation bar
        hideNavigationBar()

        // Listeners
        btnNext.setOnSafeClickListener {
            if (pagerIntroduction.currentItem < pagerIntroduction.size - 1) {
                pagerIntroduction.currentItem += 1
            } else {
                // Close Intro page
                finishIntro()
            }
        }

        pagerIntroduction.addOnPageChangeListener(pageChangedListener)
    }

    private fun initFragment() {
        val fragments = listOf(IntroItemFragment.newInstance(0), IntroItemFragment.newInstance(1))
        pagerIntroduction.adapter = IntroSliderPagerAdapter(supportFragmentManager, fragments)
        indicator.setDotsClickable(false)
        indicator.setViewPager(pagerIntroduction)
    }

    private fun finishIntro() {
        // Open Home page
        openActivity(MainActivity::class.java)

        // Close this page
        finish()
    }

    private fun updateUI(position: Int) {
        // Update button
        pagerIntroduction.adapter?.run {
            if (position == count - 1) {
                btnNext.setImageResource(R.drawable.ic_tick)
            } else {
                btnNext.setImageResource(R.drawable.ic_next)
            }
        }
    }
}
