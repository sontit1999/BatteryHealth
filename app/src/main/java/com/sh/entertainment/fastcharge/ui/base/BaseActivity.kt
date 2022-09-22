package com.sh.entertainment.fastcharge.ui.base

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.appcompat.widget.Toolbar
import com.sh.entertainment.fastcharge.R
import com.sh.entertainment.fastcharge.common.extension.contentView
import com.sh.entertainment.fastcharge.common.extension.ctx
import com.sh.entertainment.fastcharge.common.extension.gone
import com.sh.entertainment.fastcharge.common.util.CommonUtil
import com.sh.entertainment.fastcharge.common.util.PermissionUtil

abstract class BaseActivity<V : BaseView, P : BasePresenterImp<V>> : AppCompatActivity(), BaseView {

    private val toolbarBase by lazy { findViewById<Toolbar>(R.id.toolbar_base) }
    private val frlBase by lazy { findViewById<FrameLayout>(R.id.frl_base) }

    protected val self by lazy { this }
    protected lateinit var presenter: P

    private val broadcastReceiverExitApp by lazy { BroadcastReceiverExitApp() }

    override fun onCreate(savedInstanceState: Bundle?) {
        /*if (PermissionUtil.isApi21orHigher()) {
            translucentStatusBar()
        }*/
        // Fix bug: load vector for API below 21
        if (!PermissionUtil.isApi21orHigher()) {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
        super.onCreate(savedInstanceState)
        enableShowOnLockScreen()
        presenter = initPresenter()
        presenter.attachView(initView())
        getLayoutId()?.run {
            setContentView(R.layout.activity_base)
            layoutInflater.inflate(this, frlBase)

            // Set up toolbar
            applyToolbar()

            // Close keyboard when user touch outside
            CommonUtil.closeKeyboardWhileClickOutSide(self, contentView)
        }

        /* Base methods */
        initWidgets()

        // Listeners
        // Register this broadcast receiver for exiting app
        registerReceiver(broadcastReceiverExitApp, IntentFilter(com.sh.entertainment.fastcharge.common.Constants.ACTION_EXIT_APP))
    }

    override fun onDestroy() {
        presenter.detachView()
        unregisterReceiver(broadcastReceiverExitApp)
        super.onDestroy()
    }

    /*
    * return view
    * */
    abstract fun initView(): V

    /*
    * Return presenter
    * */
    abstract fun initPresenter(): P

    /*
    * Return activity's layout id
    * */
    abstract fun getLayoutId(): Int?

    /*
    * Set up widgets such as EditText, TextView, RecyclerView, etc
    * */
    abstract fun initWidgets()

    protected fun translucentStatusBar() {
//        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    }

    protected fun hideNavigationBar() {
        if (PermissionUtil.isApi30orHigher()) {
            window.setDecorFitsSystemWindows(true)
        } else {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
    }

    protected fun enableShowOnLockScreen() {
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )
    }

    protected fun applyToolbar(toolbar: Toolbar = toolbarBase) {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    // Hide base toolbar
    protected fun hideToolbarBase() {
        toolbarBase.gone()
    }

    // Using toolbar
    protected fun showTitle(title: Any? = null, toolbar: Toolbar = toolbarBase) {
        // Set title
        val result = when (title) {
            is CharSequence -> title.toString()
            is String -> title
            is Int -> getString(title)
            else -> title.toString()
        }
        toolbar.title = result
    }

    // Show Back icon
    protected fun enableHomeAsUp(toolbar: Toolbar = toolbarBase, up: () -> Unit) {
        toolbar.run {
            navigationIcon = DrawerArrowDrawable(ctx).apply { progress = 1f }
            setNavigationOnClickListener { up() }
        }
    }

    inner class BroadcastReceiverExitApp : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action?.equals(com.sh.entertainment.fastcharge.common.Constants.ACTION_EXIT_APP) == true) {
                self.finish()
            }
        }
    }
}
