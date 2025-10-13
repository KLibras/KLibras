package com.br.klibras.features.ranking

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.br.klibras.R
import com.br.klibras.core.ui.theme.Copper
import com.br.klibras.shared.AppBottomNavigationBar
import com.br.klibras.core.ui.theme.HighlightYellow
import com.br.klibras.core.ui.theme.Khaki


data class User(val username: String, val points: Int)

@Composable
fun RankingScreen(navController: NavController, users: List<User>) {
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
                text = "Libras",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Ranking",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(users) { index, user ->
                RankingItem(rank = index + 1, user = user)
            }
        }

        Icon(
            imageVector = Icons.Default.ArrowDownward,
            contentDescription = "Rolar para baixo",
            modifier = Modifier.padding(vertical = 25.dp)
        )

        AppBottomNavigationBar(navController = navController)
    }
}

@Composable
fun RankingItem(rank: Int, user: User) {
    val backgroundColor = when (rank) {
        1 -> HighlightYellow
        2 -> Copper
        3 -> Khaki
        else -> MaterialTheme.colorScheme.background
    }

    val border = if (rank > 3) {
        BorderStroke(1.dp, Color.Gray)
    } else {
        null
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
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.width(24.dp))
            Text(
                text = user.username,
                fontSize = 18.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = user.points.toString(),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RankingScreenPreview() {
    val navController = rememberNavController()

    val mockUsers = listOf(
        User("Username", 100),
        User("Username", 99),
        User("Username", 98),
        User("Username", 95),
        User("Username", 95)
    )

    RankingScreen(navController = navController, users = mockUsers)
}