package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.ElectionViewModel

@Composable
fun KolomDetailDialog(
    kolom: Kolom,
    viewModel: ElectionViewModel,
    onDismiss: () -> Unit
) {
    val pemilihList by viewModel.pemilihList.collectAsState()
    val kehadiranList by viewModel.kehadiranList.collectAsState()
    val hasilList by viewModel.hasilList.collectAsState()

    val voters = pemilihList.filter { it.kodeKolom == kolom.kodeKolom }
    val hadirCount = kehadiranList.count { it.kodeKolom == kolom.kodeKolom && it.hadir }
    val attendancePct = if (voters.isNotEmpty()) (hadirCount * 100) / voters.size else 0

    val hasilDiaken = hasilList.find { it.kodeKolom == kolom.kodeKolom && it.jenisJabatan == JenisJabatan.DIAKEN }
    val hasilPenatua = hasilList.find { it.kodeKolom == kolom.kodeKolom && it.jenisJabatan == JenisJabatan.PENATUA }

    val progress = viewModel.calculateProgress(kolom.kodeKolom)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, BorderColor),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header with Kolom name and progress badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = kolom.namaKolom,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = NavyDark
                        )
                        Text(
                            text = "Kode: ${kolom.kodeKolom} • Tanggal: ${kolom.tanggalPemilihan}",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    // Circular Progress
                    Box(
                        modifier = Modifier
                            .size(52.dp)
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
                            fontSize = 14.sp,
                            color = when {
                                progress >= 100 -> SuccessGreen
                                progress >= 50 -> DiakenGold
                                else -> PenatuaBlue
                            }
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = BorderColor)

                // Item A: Lokasi Pemilihan
                DetailItemRow(
                    label = "A. Lokasi Pemilihan",
                    value = kolom.namaLokasi.ifBlank { "Belum ditetapkan" },
                    icon = Icons.Default.Place,
                    iconColor = NavyAccent
                )

                // Item B: Jenis Pemilihan
                DetailItemRow(
                    label = "B. Jenis Pemilihan",
                    value = kolom.jenisPemilihan,
                    icon = Icons.Default.Ballot,
                    iconColor = InfoTeal
                )

                // Item C: Status Hasil Diaken
                val statusDiaken = hasilDiaken?.statusHasil ?: StatusHasil.BELUM_ADA
                DetailItemRow(
                    label = "C. Status Hasil Diaken",
                    value = statusDiaken.label + if (hasilDiaken != null) " (Sah: ${hasilDiaken.suaraSah}, Rusak: ${hasilDiaken.suaraRusak})" else "",
                    icon = Icons.Default.Person,
                    iconColor = DiakenGold,
                    isHighlight = true,
                    highlightBg = DiakenGoldLight,
                    highlightText = DiakenGold
                )

                // Item D: Status Hasil Penatua
                val statusPenatua = hasilPenatua?.statusHasil ?: StatusHasil.BELUM_ADA
                DetailItemRow(
                    label = "D. Status Hasil Penatua",
                    value = statusPenatua.label + if (hasilPenatua != null) " (Sah: ${hasilPenatua.suaraSah}, Rusak: ${hasilPenatua.suaraRusak})" else "",
                    icon = Icons.Default.PersonOutline,
                    iconColor = PenatuaBlue,
                    isHighlight = true,
                    highlightBg = PenatuaBlueLight,
                    highlightText = PenatuaBlue
                )

                // Item E: Kehadiran dan Hasil Penghitungan
                DetailItemRow(
                    label = "E. Kehadiran & Rekap",
                    value = "$hadirCount dari ${voters.size} pemilih hadir ($attendancePct%)",
                    icon = Icons.Default.HowToReg,
                    iconColor = SuccessGreen
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.setSelectedKolom(kolom.kodeKolom)
                            viewModel.navigateTo(AppScreen.DAFTAR_HADIR, kolom.kodeKolom)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Isi Hadir", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            viewModel.setSelectedKolom(kolom.kodeKolom)
                            viewModel.navigateTo(AppScreen.INPUT_HASIL, kolom.kodeKolom)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Input Suara", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Tutup", color = TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun DetailItemRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    isHighlight: Boolean = false,
    highlightBg: Color = Color.Transparent,
    highlightText: Color = TextPrimary
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = if (isHighlight) {
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(highlightBg)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            } else {
                Modifier.fillMaxWidth()
            }
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = if (isHighlight) FontWeight.SemiBold else FontWeight.Medium,
                color = if (isHighlight) highlightText else TextPrimary
            )
        }
    }
}
