package com.example.foodie

sealed class Screen(
    val route: String
) {

    object Tables : Screen("tables")

    object Home : Screen("home")

    object Cart : Screen("cart")

    object Orders : Screen("orders")

    object Assistant : Screen("assistant")

    object AdminLogin : Screen("admin_login")

    object AdminHome : Screen("admin_home")
    object Bill : Screen("bill")
}