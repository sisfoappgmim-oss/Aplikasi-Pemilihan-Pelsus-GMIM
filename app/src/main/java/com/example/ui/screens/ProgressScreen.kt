package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.example.ui.theme.*
import com.example.ui.viewmodel.ElectionViewModel

@Composable
fun ProgressScreen(
    viewModel: ElectionViewModel,
    modifier: Modifier = Modifier
) {
    val koloms by viewModel.kolomList.collectAsState()
    val pemilihs by viewModel.pemilihList.collectAsState()
    val kehadirans by viewModel.kehadiranList.collectAsState()
    val hasils by viewModel.hasilList.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf<StatusPemilihan?>(null) }
    var selectedKolomForModal by remember { mutableStateOf<Kolom?>(null) }

    val filteredKoloms = koloms.filter { k ->
        (searchQuery.isBlank() || k.namaKolom.contains(searchQuery, ignoreCase = true) || k.kodeKolom.contains(searchQuery, ignoreCase = true) || k.namaLokasi.contains(searchQuery, ignoreCase = true)) &&
                (selectedStatusFilter == null || k.statusPemilihan == selectedStatusFilter)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgLight)
            .padding(16.dp)
    ) {
        // Page Title & Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "PROGRES PEMILIHAN SELURUH KOLOM",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = NavyDark
                )
                Text(
                    text = "Pantau status pengisian lokasi, kehadiran, dan hasil perolehan suara Diaken & Penatua",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Surface(
                color = NavyDark,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "${filteredKoloms.size} Kolom Ditampilkan",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search & Status Filters Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari nama kolom atau lokasi...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Status Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedStatusFilter == null,
                onClick = { selectedStatusFilter = null },
                label = { Text("Semua Status", fontSize = 11.sp) }
            )
            StatusPemilihan.values().forEach { st ->
                FilterChip(
                    selected = selectedStatusFilter == st,
                    onClick = { selectedStatusFilter = if (selectedStatusFilter == st) null else st },
                    label = { Text(st.label, fontSize = 11.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Grid of Kolom Cards
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 300.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredKoloms, key = { it.kodeKolom }) { kolom ->
                val progress = viewModel.calculateProgress(kolom.kodeKolom)
                val voters = pemilihs.filter { it.kodeKolom == kolom.kodeKolom }
                val hadir = kehadirans.count { it.kodeKolom == kolom.kodeKolom && it.hadir }
                val hadirPct = if (voters.isNotEmpty()) (hadir * 100) / voters.size else 0

                val hasilDiaken = hasils.find { it.kodeKolom == kolom.kodeKolom && it.jenisJabatan == JenisJabatan.DIAKEN }
                val hasilPenatua = hasils.find { it.kodeKolom == kolom.kodeKolom && it.jenisJabatan == JenisJabatan.PENATUA }

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, BorderColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedKolomForModal = kolom }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Header row: Kolom name + Progress circle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = kolom.namaKolom,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = NavyDark
                                )
                                Text(
                                    text = "Kode: ${kolom.kodeKolom} • Tgl: ${kolom.tanggalPemilihan}",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }

                            // Dynamic Progress Circle
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
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
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    color = when {
                                        progress >= 100 -> SuccessGreen
                                        progress >= 50 -> DiakenGold
                                        else -> PenatuaBlue
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = BorderColor)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Status Pemilihan Badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Status Pemilihan:", fontSize = 11.sp, color = TextSecondary)
                            StatusBadge(kolom.statusPemilihan)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Status Lokasi
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Lokasi Pemilihan:", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                text = if (kolom.namaLokasi.isNotBlank()) kolom.namaLokasi else "Belum Diisi",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (kolom.namaLokasi.isNotBlank()) NavyPrimary else DangerRed,
                                maxLines = 1
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Status Daftar Hadir
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Daftar Hadir:", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                text = "$hadir/${voters.size} Hadir ($hadirPct%)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (hadir > 0) SuccessGreen else DangerRed
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Status Hasil Diaken
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Hasil Diaken:", fontSize = 11.sp, color = TextSecondary)
                            val dStatus = hasilDiaken?.statusHasil ?: StatusHasil.BELUM_ADA
                            Text(
                                text = dStatus.label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (dStatus) {
                                    StatusHasil.TERVERIFIKASI -> SuccessGreen
                                    StatusHasil.MENUNGGU_VERIFIKASI -> PenatuaBlue
                                    StatusHasil.TERSIMPAN -> DiakenGold
                                    else -> TextMuted
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Status Hasil Penatua
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Hasil Penatua:", fontSize = 11.sp, color = TextSecondary)
                            val pStatus = hasilPenatua?.statusHasil ?: StatusHasil.BELUM_ADA
                            Text(
                                text = pStatus.label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (pStatus) {
                                    StatusHasil.TERVERIFIKASI -> SuccessGreen
                                    StatusHasil.MENUNGGU_VERIFIKASI -> PenatuaBlue
                                    StatusHasil.TERSIMPAN -> DiakenGold
                                    else -> TextMuted
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick click prompt
                        Text(
                            text = "Klik kartu untuk rincian & aksi",
                            fontSize = 10.sp,
                            color = NavyAccent,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }
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
