package com.br.klibras.features.learn

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.br.klibras.R
import com.br.klibras.core.ui.theme.HighlightYellow
import com.br.klibras.features.main.Screen

@Composable
fun LearningScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))
        TreeSection(navController = navController)
    }
}

@Composable
fun TreeSection(navController: NavController) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        NodeItem(
            icon = R.drawable.introducao_logo,
            label = "Introdução",
            onClick = { navController.navigate("${Screen.GestureLearning.route}/introducao") }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(64.dp)
        ) {
            NodeItem(
                icon = R.drawable.em_preparacao_logo,
                label = "??",
                onClick = { navController.navigate("${Screen.GestureLearning.route}/em_preparacao") }
            )
            NodeItem(
                icon = R.drawable.em_preparacao_logo,
                label = "??",
                onClick = { navController.navigate("${Screen.GestureLearning.route}/em_preparacao") }
            )
        }
    }
}

@Composable
fun NodeItem(icon: Int, label: String, onClick: (() -> Unit)? = null) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = if (onClick != null) Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        ) else Modifier
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .border(
                    width = 2.dp,
                    color = HighlightYellow,
                    shape = CircleShape
                )
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(HighlightYellow),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = icon),
                    contentDescription = label,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}