package com.example.foodie.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.foodie.model.Food
import com.example.foodie.ui.theme.FoodieColors.Green
import com.example.foodie.ui.theme.FoodieColors.TextDark
import com.example.foodie.ui.theme.FoodieColors.TextGray
import com.example.foodie.ui.theme.FoodieColors.Border
import com.example.foodie.ui.theme.FoodieColors.SoftGreen
import com.example.foodie.formatPrice

@Composable
fun FoodMenuCard(
    food: Food,
    quantity: Int,
    onAddClick: () -> Unit,
    onDecreaseClick: () -> Unit,
    showDescription: Boolean = true,
    showSubTotal: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Border)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(84.dp)
                    .clip(RoundedCornerShape(12.dp)).background(SoftGreen),
                contentAlignment = Alignment.Center
            ) {
                if (food.imageUrl.isNotBlank()) {
                    SubcomposeAsyncImage(
                        model = food.imageUrl,
                        contentDescription = food.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(Modifier.size(24.dp), color = Green, strokeWidth = 2.dp)
                            }
                        },
                        error = { FoodPlaceholder() }
                    )
                } else FoodPlaceholder()
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(food.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                if (showDescription && food.description.isNotBlank()) {
                    Text(food.description, fontSize = 12.sp, color = TextGray,
                        maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                Text(
                    text = formatPrice(food.price) + if (showSubTotal) " / phần" else "",
                    fontSize = 13.sp,
                    color = if (showSubTotal) TextGray else Green,
                    fontWeight = if (showSubTotal) FontWeight.Normal else FontWeight.Bold
                )
                if (showSubTotal) {
                    Text(formatPrice(food.price * quantity), color = Green,
                        fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically) {
                    if (quantity > 0) {
                        OutlinedButton(onClick = onDecreaseClick,
                            modifier = Modifier.size(30.dp), contentPadding = PaddingValues(0.dp),
                            shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Border),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Green)) {
                            Text("−", fontSize = 14.sp)
                        }
                        Text("$quantity", modifier = Modifier.padding(horizontal = 8.dp), color = TextDark)
                    }
                    Button(onClick = onAddClick, enabled = food.available,
                        modifier = Modifier.size(30.dp), contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Color.White)) {
                        Text("+", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun FoodPlaceholder() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("🍽️", fontSize = 28.sp)
    }
}
