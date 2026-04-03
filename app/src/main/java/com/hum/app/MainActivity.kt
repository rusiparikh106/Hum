package com.hum.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.firebase.auth.FirebaseAuth
import com.hum.app.ui.navigation.HumNavGraph
import com.hum.app.ui.navigation.Routes
import com.hum.app.ui.theme.HumTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val startRoute = if (auth.currentUser != null) Routes.MAIN else Routes.LOGIN

        setContent {
            HumTheme {
                HumNavGraph(startRoute = startRoute)
            }
        }
    }
}
