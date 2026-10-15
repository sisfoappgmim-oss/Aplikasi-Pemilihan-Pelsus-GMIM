package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.KolomDetailDialog
import com.example.ui.components.StatCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.ElectionViewModel

@Composable
fun DashboardScreen(
    viewModel: ElectionViewModel,
    modifier: Modifier = Modifier
) {
    val koloms by viewModel.kolomList.collectAsState()
    val pemilihs by viewModel.pemilihList.collectAsState()
    val kehadirans by viewModel.kehadiranList.collectAsState()
    val hasils by viewModel.hasilList.collectAsState()
    val churchName by viewModel.churchName.collectAsState()
    val jemaatName by viewModel.jemaatName.collectAsState()

    var selectedKolomForModal by remember { mutableStateOf<Kolom?>(null) }

    // Calculated Statistics
    val totalKolom = koloms.size
    val totalPemilih = pemilihs.size
    val totalHadir = kehadirans.count { it.hadir }
    val totalBelumHadir = (totalPemilih - totalHadir).coerceAtLeast(0)
    val attendancePct = if (totalPemilih > 0) (totalHadir * 100) / totalPemilih else 0

    val kolomBelumMulai = koloms.count { it.statusPemilihan == StatusPemilihan.BELUM_MULAI || it.statusPemilihan == StatusPemilihan.PERSIAPAN }
    val kolomBerlangsung = koloms.count { it.statusPemilihan == StatusPemilihan.SEDANG_BERLANGSUNG || it.statusPemilihan == StatusPemilihan.MENUNGGU_VERIFIKASI }
    val kolomSelesai = koloms.count { it.statusPemilihan == StatusPemilihan.SELESAI }

    // Incomplete results count: Koloms where Diaken or Penatua is not yet verified or missing
    val kolomHasilBelumLengkap = koloms.count { k ->
        val hd = hasils.find { it.kodeKolom == k.kodeKolom && it.jenisJabatan == JenisJabatan.DIAKEN }
        val hp = hasils.find { it.kodeKolom == k.kodeKolom && it.jenisJabatan == JenisJabatan.PENATUA }
        hd?.statusHasil != StatusHasil.TERVERIFIKASI || hp?.statusHasil != StatusHasil.TERVERIFIKASI
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgLight)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header Banner
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "DASHBOARD UTAMA PEMILIHAN PELSUS",
                                color = Color(0xFFFCD34D),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$churchName - $jemaatName",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Rekapitulasi terpusat Diaken & Penatua seluruh kolom secara langsung",
                                color = Color(0xFFCBD5E1),
                                fontSize = 12.sp
                            )
                        }

                        Button(
                            onClick = { viewModel.navigateTo(AppScreen.IMPOR_EXCEL) },
                            colors = ButtonDefaults.buttonColors(containerColor = DiakenGold),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Impor Excel", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Section 1: Statistical Cards Grid
        item {
            Text(
                text = "STATISTIK PEMILIHAN (REALTIME DARI DATABASE)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 0.5.sp
            )
        }

        // Row 1: Kolom, Pemilih, Hadir, Belum Hadir
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Total Kolom",
                    value = "$totalKolom",
                    subtitle = "Terdaftar aktif",
                    icon = Icons.Default.ViewAgenda,
                    iconBgColor = NavyMedium.copy(alpha = 0.15f),
                    iconTint = NavyPrimary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Daftar Pemilih",
                    value = "$totalPemilih",
                    subtitle = "Jiwa terverifikasi",
                    icon = Icons.Default.People,
                    iconBgColor = PenatuaBlueLight,
                    iconTint = PenatuaBlue,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Pemilih Hadir",
                    value = "$totalHadir",
                    subtitle = "$attendancePct% partisipasi",
                    icon = Icons.Default.CheckCircle,
                    iconBgColor = SuccessGreenLight,
                    iconTint = SuccessGreen,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Belum Hadir",
                    value = "$totalBelumHadir",
                    subtitle = "${100 - attendancePct}% belum hadir",
                    icon = Icons.Default.AccessTime,
                    iconBgColor = DangerRedLight,
                    iconTint = DangerRed,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Row 2: Status Pelaksanaan Kolom
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Belum Mulai",
                    value = "$kolomBelumMulai",
                    subtitle = "Persiapan lokasi",
                    icon = Icons.Default.HourglassEmpty,
                    iconBgColor = Color(0xFFF1F5F9),
                    iconTint = TextSecondary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Berlangsung",
                    value = "$kolomBerlangsung",
                    subtitle = "Proses pencoblosan",
                    icon = Icons.Default.Autorenew,
                    iconBgColor = DiakenGoldLight,
                    iconTint = DiakenGold,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Kolom Selesai",
                    value = "$kolomSelesai",
                    subtitle = "BAP disahkan",
                    icon = Icons.Default.Verified,
                    iconBgColor = SuccessGreenLight,
                    iconTint = SuccessGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Alert bar for Incomplete Results
        if (kolomHasilBelumLengkap > 0) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DiakenGoldLight),
                    border = BorderStroke(1.dp, DiakenGoldBorder),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = DiakenGold)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Perhatian: $kolomHasilBelumLengkap Kolom Hasil Belum Lengkap/Terverifikasi",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF78350F),
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Pastikan seluruh formulir perolehan suara Diaken dan Penatua telah difinalisasi dan diverifikasi panitia.",
                                color = Color(0xFF92400E),
                                fontSize = 11.sp
                            )
                        }
                        TextButton(onClick = { viewModel.navigateTo(AppScreen.HASIL_PEMILIHAN) }) {
                            Text("Lihat Hasil", fontWeight = FontWeight.Bold, color = DiakenGold)
                        }
                    }
                }
            }
        }

        // Section 2: Ringkasan Status per Kolom (Table / List)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "RINGKASAN STATUS TIAP KOLOM",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 0.5.sp
                )
                TextButton(onClick = { viewModel.navigateTo(AppScreen.PROGRES_PEMILIHAN) }) {
                    Text("Buka Progres Detail", fontSize = 12.sp, color = NavyPrimary)
                }
            }
        }

        items(koloms) { kolom ->
            val kPemilih = pemilihs.filter { it.kodeKolom == kolom.kodeKolom }
            val kHadir = kehadirans.count { it.kodeKolom == kolom.kodeKolom && it.hadir }
            val kPct = if (kPemilih.isNotEmpty()) (kHadir * 100) / kPemilih.size else 0
            val progress = viewModel.calculateProgress(kolom.kodeKolom)

            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, BorderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedKolomForModal = kolom }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = kolom.namaKolom,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = NavyDark
                            )
                            StatusBadge(kolom.statusPemilihan)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Lokasi: ${kolom.namaLokasi.ifBlank { "Belum diset" }} • Hadir: $kHadir/${kPemilih.size} ($kPct%)",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    // Progress circle percentage
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    progress >= 100 -> SuccessGreenLight
                                    progress >= 50 -> DiakenGoldLight
                                    else -> PenatuaBlueLight
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$progress%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = when {
                                progress >= 100 -> SuccessGreen
                                progress >= 50 -> DiakenGold
                                else -> PenatuaBlue
                            }
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    selectedKolomForModal?.let { k ->
        KolomDetailDialog(
            kolom = k,
            viewModel = viewModel,
            onDismiss = { selectedKolomForModal = null }
        )
    }
}

@Composable
fun StatusBadge(status: StatusPemilihan) {
    val (bg, text) = when (status) {
        StatusPemilihan.BELUM_MULAI -> Pair(Color(0xFFF1F5F9), Color(0xFF64748B))
        StatusPemilihan.PERSIAPAN -> Pair(InfoTealLight, InfoTeal)
        StatusPemilihan.SEDANG_BERLANGSUNG -> Pair(DiakenGoldLight, DiakenGold)
        StatusPemilihan.MENUNGGU_VERIFIKASI -> Pair(PenatuaBlueLight, PenatuaBlue)
        StatusPemilihan.SELESAI -> Pair(SuccessGreenLight, SuccessGreen)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = status.label,
            color = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
