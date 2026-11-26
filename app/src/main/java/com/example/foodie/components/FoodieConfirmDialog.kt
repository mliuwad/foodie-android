package com.example.foodie.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodie.ui.theme.FoodieColors

@Composable
fun FoodieConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "Xác nhận",
    dismissText: String = "Hủy",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,

        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White,
        tonalElevation = 0.dp,

        titleContentColor = FoodieColors.TextDark,
        textContentColor = FoodieColors.TextGray,

        title = {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        },

        text = {
            Text(
                text = message,
                fontSize = 14.sp,
                lineHeight = 21.sp
            )
        },

        confirmButton = {
            Button(
                onClick = onConfirm,
                modifier = Modifier.heightIn(min = 44.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FoodieColors.Green,
                    contentColor = Color.White
                )
            ) {
                Text(confirmText, fontSize = 13.sp)
            }
        },

        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = FoodieColors.TextGray
                )
            ) {
                Text(dismissText, fontSize = 13.sp)
            }
        }
    )
}