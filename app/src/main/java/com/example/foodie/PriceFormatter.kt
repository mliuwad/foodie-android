package com.example.foodie

import java.text.NumberFormat
import java.util.Locale

fun formatPrice(price: Double): String =
    NumberFormat.getNumberInstance(Locale("vi", "VN")).format(price) + "đ"
