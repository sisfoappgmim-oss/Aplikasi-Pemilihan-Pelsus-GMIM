package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.ElectionViewModel

@Composable
fun VoteResultsScreen(
    viewModel: ElectionViewModel,
    modifier: Modifier = Modifier
) {
    val koloms by viewModel.kolomList.collectAsState()
    val calons by viewModel.calonList.collectAsState()
    val hasils by viewModel.hasilList.collectAsState()
    val selectedKolomCode by viewModel.selectedKolomCode.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var activeJabatan by remember { mutableStateOf(JenisJabatan.DIAKEN) }

    val activeKolom = koloms.find { it.kodeKolom == selectedKolomCode } ?: koloms.firstOrNull()
    val activeKolomCode = activeKolom?.kodeKolom ?: ""

    val result = hasils.find { it.kodeKolom == activeKolomCode && it.jenisJabatan == activeJabatan }
    val candidates = calons.filter {
        (it.kodeKolom == activeKolomCode || it.cakupan == "Bersama") && it.jenisJabatan == activeJabatan
    }

    val totalVotes = result?.suaraSah ?: 0
    val isVerified = result?.statusHasil == StatusHasil.TERVERIFIKASI
    val canVerify = currentUser.peran == PeranPengguna.VERIFIKATOR || currentUser.peran == PeranPengguna.SUPER_ADMIN

    // Winner detection or Tie logic
    val candidateVoteList = candidates.map { c ->
        val votes = result?.suaraCalon?.get(c.kodeCalon) ?: 0
        val pct = if (totalVotes > 0) (votes * 100f) / totalVotes else 0f
        Triple(c, votes, pct)
    }.sortedByDescending { it.second }

    val topVotes = candidateVoteList.firstOrNull()?.second ?: 0
    val topCandidates = candidateVoteList.filter { it.second == topVotes && topVotes > 0 }
    val isTie = topCandidates.size > 1

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgLight)
            .padding(16.dp)
    ) {
        // Page Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "HASIL PEROLEHAN SUARA & REKAPITULASI",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = NavyDark
                )
                Text(
                    text = "Grafik batang perbandingan suara, persentase, dan verifikasi sidang majelis",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { viewModel.navigateTo(AppScreen.LAPORAN_BAP, activeKolomCode) },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyDark),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cetak BAP", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Column selector pills
        Text(text = "PILIH KOLOM:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
        Spacer(modifier = Modifier.height(4.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(koloms) { k ->
                val isSelected = k.kodeKolom == activeKolomCode
                Surface(
                    color = if (isSelected) NavyPrimary else Color.White,
                    border = BorderStroke(1.dp, if (isSelected) NavyPrimary else BorderColor),
                    shape = RoundedCornerShape(8.dp),
                    onClick = { viewModel.setSelectedKolom(k.kodeKolom) }
                ) {
                    Text(
                        text = k.namaKolom,
                        color = if (isSelected) Color.White else TextPrimary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Jabatan Toggle: Diaken vs Penatua
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { activeJabatan = JenisJabatan.DIAKEN },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeJabatan == JenisJabatan.DIAKEN) DiakenGold else Color.White,
                    contentColor = if (activeJabatan == JenisJabatan.DIAKEN) Color.White else DiakenGold
                ),
                border = BorderStroke(1.dp, DiakenGold),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Hasil DIAKEN", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            Button(
                onClick = { activeJabatan = JenisJabatan.PENATUA },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeJabatan == JenisJabatan.PENATUA) PenatuaBlue else Color.White,
                    contentColor = if (activeJabatan == JenisJabatan.PENATUA) Color.White else PenatuaBlue
                ),
                border = BorderStroke(1.dp, PenatuaBlue),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Hasil PENATUA", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Status & Verification Banner
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, BorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "${activeJabatan.label} - ${activeKolom?.namaKolom}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = NavyDark
                    )
                    Text(
                        text = "Status: ${result?.statusHasil?.label ?: "Belum Ada Data"} • Petugas: ${result?.petugas ?: "-"}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                if (result != null) {
                    if (isVerified) {
                        Surface(
                            color = SuccessGreenLight,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                                Text("Sah & Terverifikasi", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    } else if (result.isFinal && canVerify) {
                        Button(
                            onClick = { viewModel.verifyVoteResult(activeKolomCode, activeJabatan) },
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Verifikasi Sekarang", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else if (result.isFinal) {
                        Surface(
                            color = PenatuaBlueLight,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Menunggu Verifikasi",
                                color = PenatuaBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    } else {
                        Surface(
                            color = DiakenGoldLight,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Draft (Belum Final)",
                                color = DiakenGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Winner / Tie Banner
        if (result != null && result.suaraSah > 0) {
            Spacer(modifier = Modifier.height(8.dp))
            if (isTie) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DiakenGoldLight),
                    border = BorderStroke(1.dp, DiakenGoldBorder),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Balance, contentDescription = null, tint = DiakenGold)
                        Column {
                            Text("Hasil Perolehan Suara Seri (Draw)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF78350F))
                            Text(
                                "Terdapat ${topCandidates.size} calon dengan perolehan suara tertinggi sama ($topVotes suara). Ikuti tata gereja resmi GMIM untuk pemilihan putaran kedua atau penyelesaian.",
                                fontSize = 11.sp,
                                color = Color(0xFF92400E)
                            )
                        }
                    }
                }
            } else if (isVerified && topCandidates.isNotEmpty()) {
                val winner = topCandidates.first().first
                Card(
                    colors = CardDefaults.cardColors(containerColor = SuccessGreenLight),
                    border = BorderStroke(1.dp, SuccessGreen),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = SuccessGreen)
                        Column {
                            Text("Calon Terpilih ${activeJabatan.label}: ${winner.namaCalon}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF14532D))
                            Text(
                                "Memperoleh suara terbanyak ($topVotes suara, ${String.format("%.1f", (topVotes * 100f) / totalVotes)}%) dan telah disahkan.",
                                fontSize = 11.sp,
                                color = Color(0xFF166534)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Candidate Vote List with Visual Progress Bars
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = "PEROLEHAN SUARA CALON",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
            }

            if (result == null || candidateVoteList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Ballot, contentDescription = null, tint = TextMuted, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Belum ada data perolehan suara untuk kolom ini", color = TextSecondary, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.navigateTo(AppScreen.INPUT_HASIL, activeKolomCode) },
                                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                            ) {
                                Text("Input Suara Sekarang")
                            }
                        }
                    }
                }
            } else {
                items(candidateVoteList) { (calon, votes, pct) ->
                    val isTop = votes == topVotes && topVotes > 0

                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, if (isTop && isVerified) SuccessGreen else BorderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(if (activeJabatan == JenisJabatan.DIAKEN) DiakenGoldLight else PenatuaBlueLight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${calon.nomorUrut}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (activeJabatan == JenisJabatan.DIAKEN) DiakenGold else PenatuaBlue
                                        )
                                    }
                                    Text(
                                        text = calon.namaCalon,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = NavyDark
                                    )
                                    if (isTop && isVerified) {
                                        Icon(Icons.Default.Stars, contentDescription = "Terpilih", tint = SuccessGreen, modifier = Modifier.size(16.dp))
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "$votes Suara",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = NavyPrimary
                                    )
                                    Text(
                                        text = "(${String.format("%.1f", pct)}%)",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Visual Bar
                            LinearProgressIndicator(
                                progress = { (pct / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                color = if (activeJabatan == JenisJabatan.DIAKEN) DiakenGold else PenatuaBlue,
                                trackColor = Color(0xFFF1F5F9)
                            )
                        }
                    }
                }

                // Summary Ballot Count
                item {
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, BorderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Rekapitulasi Suara:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Jumlah Suara Sah:", fontSize = 12.sp, color = TextPrimary)
                                Text("${result.suaraSah}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Jumlah Suara Rusak / Batal:", fontSize = 12.sp, color = DangerRed)
                                Text("${result.suaraRusak}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DangerRed)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Surat Suara Tidak Digunakan:", fontSize = 12.sp, color = TextSecondary)
                                Text("${result.suratSuaraTidakDigunakan}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            if (result.catatan.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                HorizontalDivider(color = BorderColor)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Catatan Panitia: \"${result.catatan}\"", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}
