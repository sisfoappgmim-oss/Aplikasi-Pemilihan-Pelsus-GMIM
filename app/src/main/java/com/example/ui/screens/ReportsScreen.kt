package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ElectionViewModel

@Composable
fun ReportsScreen(
    viewModel: ElectionViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val koloms by viewModel.kolomList.collectAsState()
    val pemilihs by viewModel.pemilihList.collectAsState()
    val calons by viewModel.calonList.collectAsState()
    val kehadirans by viewModel.kehadiranList.collectAsState()
    val hasils by viewModel.hasilList.collectAsState()
    val churchName by viewModel.churchName.collectAsState()
    val jemaatName by viewModel.jemaatName.collectAsState()
    val wilayahName by viewModel.wilayahName.collectAsState()
    val electionYear by viewModel.electionYear.collectAsState()
    val selectedKolomCode by viewModel.selectedKolomCode.collectAsState()

    var activeReportType by remember { mutableStateOf(0) } // 0: BAP Kolom, 1: Daftar Hadir, 2: Rekapitulasi Suara
    var chosenKolomCode by remember(selectedKolomCode) { mutableStateOf(selectedKolomCode ?: koloms.firstOrNull()?.kodeKolom ?: "KOLOM-01") }

    val activeKolom = koloms.find { it.kodeKolom == chosenKolomCode } ?: koloms.firstOrNull()
    val votersInKolom = pemilihs.filter { it.kodeKolom == chosenKolomCode }
    val hadirCount = kehadirans.count { it.kodeKolom == chosenKolomCode && it.hadir }
    val hadirPct = if (votersInKolom.isNotEmpty()) (hadirCount * 100) / votersInKolom.size else 0

    val hasilDiaken = hasils.find { it.kodeKolom == chosenKolomCode && it.jenisJabatan == JenisJabatan.DIAKEN }
    val hasilPenatua = hasils.find { it.kodeKolom == chosenKolomCode && it.jenisJabatan == JenisJabatan.PENATUA }

    val calonsDiaken = calons.filter { (it.kodeKolom == chosenKolomCode || it.cakupan == "Bersama") && it.jenisJabatan == JenisJabatan.DIAKEN }
    val calonsPenatua = calons.filter { (it.kodeKolom == chosenKolomCode || it.cakupan == "Bersama") && it.jenisJabatan == JenisJabatan.PENATUA }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgLight)
            .padding(16.dp)
    ) {
        // Page Header & Export buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "MODUL LAPORAN & BERITA ACARA (BAP)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = NavyDark
                )
                Text(
                    text = "Penerbitan Berita Acara Pemilihan resmi GMIM format standar cetak A4",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "BAP Pemilihan ${activeKolom?.namaKolom}")
                            putExtra(
                                Intent.EXTRA_TEXT,
                                """
BERITA ACARA PEMILIHAN PELAYAN KHUSUS (PELSUS) GMIM
$churchName - $jemaatName ($wilayahName)
Kolom: ${activeKolom?.namaKolom}
Tanggal: ${activeKolom?.tanggalPemilihan}
Lokasi: ${activeKolom?.namaLokasi}

REKAPITULASI KEHADIRAN:
- Pemilih Terdaftar: ${votersInKolom.size} Jiwa
- Pemilih Hadir: $hadirCount Jiwa ($hadirPct%)

HASIL DIAKEN:
Suara Sah: ${hasilDiaken?.suaraSah ?: 0}, Rusak: ${hasilDiaken?.suaraRusak ?: 0}
${calonsDiaken.joinToString("\n") { "- ${it.namaCalon}: ${hasilDiaken?.suaraCalon?.get(it.kodeCalon) ?: 0} Suara" }}

HASIL PENATUA:
Suara Sah: ${hasilPenatua?.suaraSah ?: 0}, Rusak: ${hasilPenatua?.suaraRusak ?: 0}
${calonsPenatua.joinToString("\n") { "- ${it.namaCalon}: ${hasilPenatua?.suaraCalon?.get(it.kodeCalon) ?: 0} Suara" }}

Status: Disahkan Panitia Pemilihan GMIM
                                """.trimIndent()
                            )
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Bagikan / Cetak BAP"))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyDark),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Bagikan / Ekspor Teks BAP", fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Filters: Document Type & Kolom Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Kolom Dropdown / Selector
            var showKolomDropdown by remember { mutableStateOf(false) }
            Box(modifier = Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = { showKolomDropdown = true },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Pilih Kolom: $chosenKolomCode", fontSize = 12.sp, maxLines = 1)
                }
                DropdownMenu(
                    expanded = showKolomDropdown,
                    onDismissRequest = { showKolomDropdown = false }
                ) {
                    koloms.forEach { k ->
                        DropdownMenuItem(
                            text = { Text(k.namaKolom) },
                            onClick = {
                                chosenKolomCode = k.kodeKolom
                                showKolomDropdown = false
                            }
                        )
                    }
                }
            }

            // Tab selection
            FilterChip(
                selected = activeReportType == 0,
                onClick = { activeReportType = 0 },
                label = { Text("BAP Kolom", fontSize = 11.sp) }
            )
            FilterChip(
                selected = activeReportType == 1,
                onClick = { activeReportType = 1 },
                label = { Text("Daftar Hadir", fontSize = 11.sp) }
            )
            FilterChip(
                selected = activeReportType == 2,
                onClick = { activeReportType = 2 },
                label = { Text("Rekap Suara", fontSize = 11.sp) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Formal Document Preview Box (A4 style paper)
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, BorderColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Kop Gereja GMIM
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = churchName.uppercase(),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = NavyDark,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "${jemaatName.uppercase()} - ${wilayahName.uppercase()}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = NavyPrimary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "KOMISI PEMILIHAN PELAYAN KHUSUS PERIODE $electionYear",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider(thickness = 2.dp, color = NavyDark)
                        HorizontalDivider(thickness = 1.dp, color = NavyDark, modifier = Modifier.padding(top = 2.dp))
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Document Title & Number
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "BERITA ACARA PEMILIHAN PELAYAN KHUSUS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = NavyDark
                        )
                        Text(
                            text = "NOMOR: BAP/${chosenKolomCode}/${jemaatName.replace(" ", "")}/X/2026",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Preamble Text
                item {
                    Text(
                        text = "Pada hari ini Sabtu, tanggal ${activeKolom?.tanggalPemilihan ?: "17-10-2026"}, bertempat di ${activeKolom?.namaLokasi ?: "Pastori"}, telah dilaksanakan Pemilihan Pelayan Khusus (Diaken dan Penatua) ${activeKolom?.namaKolom ?: ""} Jemaat GMIM Sentrum periode pelayanan $electionYear dengan ketentuan sebagai berikut:",
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Justify,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Section I: Kehadiran
                item {
                    Text(
                        text = "I. REKAPITULASI DAFTAR PEMILIH & KEHADIRAN",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = NavyDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    ReportDataRow("1. Jumlah Pemilih Terdaftar dalam DPK", "${votersInKolom.size} Orang")
                    ReportDataRow("2. Jumlah Pemilih yang Hadir Menggunakan Hak Suara", "$hadirCount Orang ($hadirPct%)")
                    ReportDataRow("3. Jumlah Pemilih yang Tidak Hadir", "${votersInKolom.size - hadirCount} Orang")
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Section II: Hasil Diaken
                item {
                    Text(
                        text = "II. PEROLEHAN SUARA CALON DIAKEN",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = DiakenGold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    calonsDiaken.forEach { c ->
                        val votes = hasilDiaken?.suaraCalon?.get(c.kodeCalon) ?: 0
                        ReportDataRow("• No. ${c.nomorUrut} - ${c.namaCalon}", "$votes Suara")
                    }
                    ReportDataRow("Jumlah Suara Sah Diaken", "${hasilDiaken?.suaraSah ?: 0} Suara")
                    ReportDataRow("Jumlah Suara Rusak / Batal", "${hasilDiaken?.suaraRusak ?: 0} Suara")
                    ReportDataRow("Status Pengesahan", hasilDiaken?.statusHasil?.label ?: "Belum Ada")
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Section III: Hasil Penatua
                item {
                    Text(
                        text = "III. PEROLEHAN SUARA CALON PENATUA",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = PenatuaBlue
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    calonsPenatua.forEach { c ->
                        val votes = hasilPenatua?.suaraCalon?.get(c.kodeCalon) ?: 0
                        ReportDataRow("• No. ${c.nomorUrut} - ${c.namaCalon}", "$votes Suara")
                    }
                    ReportDataRow("Jumlah Suara Sah Penatua", "${hasilPenatua?.suaraSah ?: 0} Suara")
                    ReportDataRow("Jumlah Suara Rusak / Batal", "${hasilPenatua?.suaraRusak ?: 0} Suara")
                    ReportDataRow("Status Pengesahan", hasilPenatua?.statusHasil?.label ?: "Belum Ada")
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Section IV: Signatures (Tanda Tangan)
                item {
                    Text(
                        text = "Demikian Berita Acara ini dibuat dan ditandatangani dengan sebenarnya oleh Panitia Pemilihan:",
                        fontSize = 11.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Ketua Panitia Pemilihan,", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(48.dp))
                            Text("Pnt. James Rumagit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("(Tanda Tangan & Cap)", fontSize = 9.sp, color = TextMuted)
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Sekretaris Panitia,", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(48.dp))
                            Text("Dkn. Ruth Sumendap", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("(Tanda Tangan)", fontSize = 9.sp, color = TextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Saksi Kolom
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Petugas / Saksi Kolom:", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(40.dp))
                        Text(hasilDiaken?.petugas?.ifBlank { "Bpk. Hendra Rondonuwu" } ?: "Bpk. Hendra Rondonuwu", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportDataRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, color = TextPrimary)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = NavyDark)
    }
}
