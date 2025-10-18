package com.br.klibras.features.dex

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.br.klibras.R
import com.br.klibras.core.service.api.Sign
import com.br.klibras.core.ui.theme.JosefinSans

val HighlightYellow = Color(0xFFDEBC32)

@Composable
fun DexScreen(viewModel: DexViewModel = viewModel()) {
    val context = LocalContext.current
    val dexState by viewModel.dexState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadKnownSigns(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(56.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "KLibras Logo",
                modifier = Modifier.size(63.dp, 56.dp)
            )
            Text(
                text = "KLibras",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = JosefinSans
            )
        }

        Spacer(modifier = Modifier.height(50.dp))

        Text(
            text = "Sinais conquistados",
            fontFamily = JosefinSans,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(24.dp))

        when (val state = dexState) {
            is DexState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = HighlightYellow)
                }
            }
            is DexState.Success -> {
                DexSignsList(knownSigns = state.knownSigns)
            }
            is DexState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        color = Color.Red,
                        fontFamily = JosefinSans
                    )
                }
            }
        }
    }
}

@Composable
fun DexSignsList(knownSigns: List<Sign>) {
    val maxSlots = 4
    val knownSignNames = knownSigns.map { it.name }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        for (i in 0 until maxSlots) {
            if (i < knownSignNames.size) {
                SignItem(
                    signName = knownSignNames[i],
                    isKnown = true
                )
            } else {
                EmptySignSlot()
            }
        }
    }
}

@Composable
fun SignItem(signName: String, isKnown: Boolean) {
    val backgroundColor = if (isKnown) HighlightYellow else MaterialTheme.colorScheme.background
    val border = if (isKnown) null else BorderStroke(1.dp, Color.Gray)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = border
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = signName,
                fontSize = 22.sp,
                color = Color.Black,
                fontFamily = JosefinSans,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun EmptySignSlot() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        border = BorderStroke(1.dp, Color.Gray)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Slot vazio",
                modifier = Modifier.size(40.dp),
                alpha = 0.5f
            )
        }
    }
}