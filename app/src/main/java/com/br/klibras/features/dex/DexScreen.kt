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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.br.klibras.R
import com.br.klibras.core.ui.theme.JosefinSans


val HighlightYellow = Color(0xFFDEBC32)

data class Sign(val name: String)

@Composable
fun DexScreen(knownSigns: List<Sign>) {
    val allPossibleSigns = listOf("Bom dia", "Boa tarde", "Boa noite", "Obrigado")
    val knownSignNames = knownSigns.map { it.name }.toSet()

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

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            allPossibleSigns.forEach { signName ->
                SignItem(
                    signName = signName,
                    isknown = signName in knownSignNames
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = "Rolar para baixo",
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}

@Composable
fun SignItem(signName: String, isknown: Boolean) {
    val backgroundColor = if (isknown) HighlightYellow else MaterialTheme.colorScheme.background
    val border = if (isknown) null else BorderStroke(1.dp, Color.Gray)

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
            if (isknown) {
                Text(
                    text = signName,
                    fontSize = 22.sp,
                    color = Color.Black,
                    fontFamily = JosefinSans,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Sinal bloqueado",
                    modifier = Modifier.size(40.dp),
                    alpha = 0.5f
                )
            }
        }
    }
}

