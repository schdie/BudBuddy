package com.benegedeniz.budsdynamiceq.ui.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.benegedeniz.budsdynamiceq.ui.buds.BudsScreen
import com.benegedeniz.budsdynamiceq.ui.buds.BudsViewModel

@Composable
fun MainScreen() {
    val budsViewModel: BudsViewModel = viewModel()
    BudsScreen(
        viewModel = budsViewModel,
        modifier = Modifier.fillMaxSize()
    )
}