package com.example.foodie.model

enum class OrderStatus {
    PENDING, //Chờ xác nhận
    CONFIRMED,//Đã xác nhận
    PREPARING,//Đang chế biến
    SERVED,//Đã phục vụ
    CANCELLED  //Đã hủy
}