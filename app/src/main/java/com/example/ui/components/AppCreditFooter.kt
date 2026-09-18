package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GreenContainer
import com.example.ui.theme.GreenDark
import com.example.ui.theme.GreenPrimary

@Composable
fun AppCreditFooter(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("app_credit_footer_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = GreenContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "FitTrack Daily",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = GreenDark
            )
            Text(
                text = "Eat. Move. Track. Improve.",
                fontSize = 12.sp,
                color = GreenDark.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Footer Credit: Irfan Gulzar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://irfangulzar.com"))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Fallback if no browser
                        }
                    }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .testTag("footer_irfan_gulzar_credit")
            ) {
                Text(
                    text = "Created with ",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = GreenDark
                )
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Love",
                    tint = Color(0xFFE53935),
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = " by ",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = GreenDark
                )
                Text(
                    text = "Irfan Gulzar",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = GreenDark,
                    textDecoration = TextDecoration.Underline
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.OpenInBrowser,
                    contentDescription = "Visit Irfan Gulzar's website",
                    tint = GreenDark,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}
