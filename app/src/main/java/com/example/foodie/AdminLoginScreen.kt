package com.example.foodie.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodie.components.FoodieLoadingDialog
import com.example.foodie.components.FoodieMessageDialog
import com.example.foodie.ui.theme.FoodieColors
import com.example.foodie.viewmodel.AuthViewModel

@Composable
fun AdminLoginScreen(
    authViewModel: AuthViewModel,
    onBackClick: () -> Unit,
    onLoginSuccess: () -> Unit
) {

    var email by rememberSaveable {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var passwordVisible by remember {
        mutableStateOf(false)
    }

    val admin by authViewModel.admin.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    val error by authViewModel.error.collectAsState()

    LaunchedEffect(admin) {
        if (admin != null) {
            password = ""
            onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FoodieColors.Background)
            .navigationBarsPadding()
            .imePadding()
    ) {

        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(FoodieColors.Green)
                .statusBarsPadding()
                .height(48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(
                onClick = onBackClick,
                enabled = !isLoading
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = Color.White
                )
            }

            Text(
                text = "Đăng nhập nhân viên",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(32.dp))

            Surface(
                color = FoodieColors.SoftGreen,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.size(80.dp)
            ) {

                Box(
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        imageVector =
                        Icons.Outlined.AdminPanelSettings,
                        contentDescription = null,
                        tint = FoodieColors.Green,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Foodie Admin",
                color = FoodieColors.Green,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Hệ thống dành cho nhân viên nhà hàng",
                color = FoodieColors.TextGray,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(28.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                border = BorderStroke(
                    1.dp,
                    FoodieColors.Border
                )
            ) {

                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement =
                    Arrangement.spacedBy(12.dp)
                ) {

                    Text(
                        text = "Đăng nhập",
                        color = FoodieColors.TextDark,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Email",
                        color = FoodieColors.TextDark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading,
                        placeholder = {
                            Text(
                                "Nhập email",
                                fontSize = 13.sp,
                                color = FoodieColors.TextGray
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Email,
                                contentDescription = null,
                                tint = FoodieColors.TextGray
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email
                        ),
                        shape = RoundedCornerShape(12.dp),
                        colors = adminTextFieldColors()
                    )

                    Text(
                        text = "Mật khẩu",
                        color = FoodieColors.TextDark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading,
                        placeholder = {
                            Text(
                                "Nhập mật khẩu",
                                fontSize = 13.sp,
                                color = FoodieColors.TextGray
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = FoodieColors.TextGray
                            )
                        },
                        trailingIcon = {

                            IconButton(
                                onClick = {
                                    passwordVisible =
                                        !passwordVisible
                                }
                            ) {

                                Icon(
                                    imageVector =
                                    if (passwordVisible)
                                        Icons.Outlined.Visibility
                                    else
                                        Icons.Outlined.VisibilityOff,

                                    contentDescription =
                                    if (passwordVisible)
                                        "Ẩn mật khẩu"
                                    else
                                        "Hiện mật khẩu",

                                    tint = FoodieColors.TextGray
                                )
                            }
                        },
                        visualTransformation =
                        if (passwordVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),

                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password
                        ),
                        shape = RoundedCornerShape(12.dp),
                        colors = adminTextFieldColors()
                    )

                    Spacer(Modifier.height(4.dp))

                    Button(
                        onClick = {
                            authViewModel.login(
                                email,
                                password
                            )
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FoodieColors.Green,
                            contentColor = Color.White
                        )
                    ) {

                        Text(
                            text = "Đăng nhập",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Chỉ dành cho nhân viên được cấp tài khoản",
                color = FoodieColors.TextGray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }

    if (isLoading) {

        FoodieLoadingDialog(
            message = "Đang đăng nhập..."
        )
    }

    error?.let { message ->

        FoodieMessageDialog(
            title = "Đăng nhập thất bại",
            message = message,
            isSuccess = false,
            onDismiss = {
                authViewModel.clearError()
            }
        )
    }
}

@Composable
private fun adminTextFieldColors(): TextFieldColors {

    return OutlinedTextFieldDefaults.colors(
        focusedBorderColor = FoodieColors.Green,
        unfocusedBorderColor = FoodieColors.Border,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        focusedTextColor = FoodieColors.TextDark,
        unfocusedTextColor = FoodieColors.TextDark,
        cursorColor = FoodieColors.Green
    )
}