package cstjean.mobile2.classementlnh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cstjean.mobile2.classementlnh.ui.navigation.LnhNavGraph
import cstjean.mobile2.classementlnh.ui.theme.ProjetDepartClassementLNHTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ProjetDepartClassementLNHTheme {
                LnhNavGraph()
            }
        }
    }
}
