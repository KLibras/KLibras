package com.br.klibras.features.ranking

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.br.klibras.R
import com.br.klibras.core.service.api.User
import com.br.klibras.core.ui.theme.Copper
import com.br.klibras.core.ui.theme.HighlightYellow
import com.br.klibras.core.ui.theme.Khaki

@Composable
fun RankingScreen(
    rankingViewModel: RankingViewModel = viewModel()
) {
    val uiState by rankingViewModel.uiState.collectAsState()

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
                contentDescription = "Libras Logo",
                modifier = Modifier.size(63.dp, 56.dp)
            )
            Text(
                text = "KLibras",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            )
        }

        Spacer(modifier = Modifier.height(50.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Ranking ",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.width(8.dp))

        }

        Spacer(modifier = Modifier.height(24.dp))

        when {
            uiState.isLoading && uiState.users.isEmpty() -> {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.error!!,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
            uiState.users.isEmpty() -> {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhum usuário no ranking ainda",
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(uiState.users) { index, user ->
                        val rank = index + 1
                        RankingItem(
                            rank = rank,
                            user = user,
                            isCurrentUser = rank == uiState.currentUserRank
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = "Rolar para baixo",
                    modifier = Modifier.padding(vertical = 25.dp),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
fun RankingItem(rank: Int, user: User, isCurrentUser: Boolean = false) {
    val backgroundColor = when (rank) {
        1 -> HighlightYellow
        2 -> Copper
        3 -> Khaki
        else -> MaterialTheme.colorScheme.background
    }

    val border = when {
        isCurrentUser -> BorderStroke(2.dp, Color(0xFFDEBC32))
        rank > 3 -> BorderStroke(1.dp, Color.Gray)
        else -> null
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = border
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 30.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$rank",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = if (isCurrentUser) Color(0xFFDEBC32) else Color.Unspecified
            )
            Spacer(modifier = Modifier.width(24.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.username,
                    fontSize = 18.sp,
                    fontWeight = if (isCurrentUser) FontWeight.Bold else FontWeight.Normal,
                    color = if (isCurrentUser) Color(0xFFDEBC32) else Color.Unspecified
                )
                if (isCurrentUser) {
                    Text(
                        text = "Você",
                        fontSize = 12.sp,
                        color = Color(0xFFDEBC32),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Text(
                text = "${user.points} pts",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = if (isCurrentUser) Color(0xFFDEBC32) else Color.Unspecified
            )
        }
    }
}