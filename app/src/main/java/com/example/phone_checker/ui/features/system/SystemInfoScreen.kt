package com.example.phone_checker.ui.features.system

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.phone_checker.ui.components.KeyValueCard
import com.example.phone_checker.ui.components.InfoRowCard
import com.example.phone_checker.ui.theme.PhonecheckerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemInfoScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("System") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SystemInfoContent()
        }
    }
}

@Composable
private fun SystemInfoContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val infoItems = listOf(
            "Android Version" to (Build.VERSION.RELEASE ?: "Unknown"),
            "API Level" to Build.VERSION.SDK_INT.toString(),
            "Security Patch" to (Build.VERSION.SECURITY_PATCH ?: "Unknown"),
            "Build ID" to Build.ID,
            "Incremental" to Build.VERSION.INCREMENTAL,
            "Build Type" to Build.TYPE,
            "Fingerprint" to Build.FINGERPRINT,
            "Manufacturer" to Build.MANUFACTURER,
            "Brand" to Build.BRAND,
            "Model" to Build.MODEL,
            "Device" to Build.DEVICE,
            "Product" to Build.PRODUCT,
            "Hardware" to Build.HARDWARE,
            "Bootloader" to Build.BOOTLOADER
        )
        infoItems.forEach { (label, value) ->
                InfoRowCard(title = label, value = value)
            }
    }
}


@Preview(showBackground = true)
@Composable
fun SystemInfoScreenPreview() {
    PhonecheckerTheme {
        SystemInfoContent()
    }
}
