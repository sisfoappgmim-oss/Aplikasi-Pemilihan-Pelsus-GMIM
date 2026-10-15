package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.ElectionViewModel

@Composable
fun UnderDevScreen(
    screen: AppScreen,
    viewModel: ElectionViewModel,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BgLight)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, BorderColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 500.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = DiakenGoldLight,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Construction,
                        contentDescription = null,
                        tint = DiakenGold,
                        modifier = Modifier
                            .padding(16.dp)
                            .size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = screen.title.uppercase(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyDark
                )

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "STATUS: TAHAP PENGEMBANGAN SISTEM",
                        color = Color(0xFF475569),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = when (screen) {
                        AppScreen.PEMILIHAN_BIPRA -> "Modul Pemilihan Komisi Pelayanan Kategorial BIPRA (Pria/Kaum Bapa, Wanita/Kaum Ibu, Pemuda, Remaja, Anak Sekolah Minggu) saat ini sedang dalam spesifikasi aturan baku sidang sinode dan tata gereja GMIM."
                        AppScreen.PEMILIHAN_BPMJ -> "Modul Pemilihan Badan Pekerja Majelis Jemaat (BPMJ) periode pelayanan mendatang akan dibuka setelah seluruh pemilihan Pelsus Kolom selesai dan terverifikasi secara penuh."
                        AppScreen.PEMILIHAN_LANSIA -> "Modul Pemilihan Kelompok Fungsional Lanjut Usia (Lansia) sedang disinkronkan dengan basis data umur dan kehadiran jemaat."
                        else -> "Modul ini dijadwalkan pada rilis sistem berikutnya."
                    },
                    fontSize = 13.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { viewModel.navigateTo(AppScreen.BERANDA) },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Kembali ke Beranda")
                }
            }
        }
    }
}
