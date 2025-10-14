package com.br.klibras.features.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.br.klibras.core.ui.theme.JosefinSans
import com.br.klibras.core.ui.theme.KLibrasTheme
import com.br.klibras.features.main.Screen

@Composable
fun AccountScreen(
    navController: NavController,
    username: String = "Username",
    email: String = "Email",
    points: Int = 100,
    conqueredSigns: Int = 1
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 80.dp)
    ) {
        ProfileSection(
            username = username,
            email = email,
            onChangePasswordClick = {
                navController.navigate(Screen.ChangePassword.route)
            },
            onChangeUsernameClick = {
                navController.navigate(Screen.ChangeUsername.route)
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Estatísticas",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = JosefinSans,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        StatisticsSection(points = points, conqueredSigns = conqueredSigns)
    }
}

@Composable
fun ProfileSection(
    username: String,
    email: String,
    onChangePasswordClick: () -> Unit,
    onChangeUsernameClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFDEBC32)),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = username, color = Color.Black, fontSize = 32.sp, fontWeight = FontWeight.Bold, fontFamily = JosefinSans)
            Text(text = email, color = Color.Black, fontSize = 20.sp, fontFamily = JosefinSans)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Button(
                    onClick = onChangePasswordClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Alterar senha", fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onChangeUsernameClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Alterar username", fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun StatisticsSection(points: Int, conqueredSigns: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatisticCard(
            value = points.toString(),
            label = "Pontos",
            modifier = Modifier.weight(1f)
        )
        StatisticCard(
            value = conqueredSigns.toString(),
            label = "Sinais",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatisticCard(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFDEBC32)),
        modifier = modifier
            .height(100.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                color = Color.Black,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = JosefinSans
            )
            Text(
                text = label,
                color = Color.Black,
                fontSize = 16.sp,
                fontFamily = JosefinSans
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AccountScreenPreview() {
    KLibrasTheme(darkTheme = true) {
        AccountScreen(navController = rememberNavController())
    }
}
