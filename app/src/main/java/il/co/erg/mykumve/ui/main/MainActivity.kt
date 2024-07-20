package il.co.erg.mykumve.ui.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Build
import android.view.Window
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.commit
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import il.co.erg.mykumve.R
import il.co.erg.mykumve.ui.login.LoginManager
import il.co.erg.mykumve.util.UserManager



class MainActivity : AppCompatActivity() {
    private val viewModel by viewModels<MainViewModel>()
    companion object {
        const val DEBUG_MODE = true // Set to true to enable debug options
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().apply {
            setKeepOnScreenCondition{
                !viewModel.isReady.value
            }

        }
        setContentView(R.layout.activity_main)
        initializeComponents()

        //Changing the color of the status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, R.color.statusBarColor)
        }

        // Check if the user is logged in and navigate accordingly
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController


        if (UserManager.isLoggedIn()) {
            navController.navigate(R.id.mainScreenManager)
        } else {
            navController.navigate(R.id.loginManager)
        }

    }


    private fun initializeComponents(){
        // Initialize UserManager
        UserManager.init(this)
    }
}