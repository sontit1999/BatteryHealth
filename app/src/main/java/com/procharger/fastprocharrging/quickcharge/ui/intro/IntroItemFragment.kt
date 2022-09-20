package com.procharger.fastprocharrging.quickcharge.ui.intro

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.procharger.fastprocharrging.quickcharge.R
import com.procharger.fastprocharrging.quickcharge.common.extension.ctx
import com.procharger.fastprocharrging.quickcharge.ui.base.BaseFragment

private const val ARG_POSITION = "arg_position"

class IntroItemFragment : BaseFragment<IntroItemView, IntroItemPresenterImp>(), IntroItemView {

    private lateinit var imgIntro: ImageView
    private lateinit var lblIntroTitle: TextView
    private lateinit var lblIntroMessage: TextView

    private val arrImage by lazy { intArrayOf(R.drawable.img_intro_1, R.drawable.img_intro_2) }
    private val arrTitle by lazy { resources.getStringArray(R.array.intro_titles) }
    private val arrMessage by lazy { resources.getStringArray(R.array.intro_messages) }

    private var position = 0

    companion object {
        @JvmStatic
        fun newInstance(position: Int) = IntroItemFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_POSITION, position)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            position = it.getInt(ARG_POSITION)
        }
    }

    override fun initView(): IntroItemView = this

    override fun initPresenter(): IntroItemPresenterImp = IntroItemPresenterImp(ctx!!)

    override fun getLayoutId(): Int {
        return R.layout.fragment_intro_item
    }

    override fun initWidgets(rootView: View) {
        rootView.run {
            imgIntro = findViewById(R.id.img_intro)
            lblIntroTitle = findViewById(R.id.lbl_intro_title)
            lblIntroMessage = findViewById(R.id.lbl_intro_message)
        }

        // Fill data
        if (position < arrImage.size) {
            imgIntro.setImageResource(arrImage[position])
        }
        if (position < arrTitle.size) {
            lblIntroTitle.text = arrTitle[position]
        }
        if (position < arrMessage.size) {
            lblIntroMessage.text = arrMessage[position]
        }
    }
}
