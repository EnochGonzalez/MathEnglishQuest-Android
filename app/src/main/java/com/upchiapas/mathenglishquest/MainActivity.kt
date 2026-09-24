package com.upchiapas.mathenglishquest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.upchiapas.mathenglishquest.ui.navigation.AppNavigation
import com.upchiapas.mathenglishquest.ui.theme.MathEnglishQuestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MathEnglishQuestTheme {
                AppNavigation()
            }
        }
    }
}