package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.excel.ExcelCsvManager
import com.example.ui.theme.*
import com.example.ui.viewmodel.ElectionViewModel

@Composable
fun ExcelImportScreen(
    viewModel: ElectionViewModel,
    modifier: Modifier = Modifier
) {
    val currentType by viewModel.currentImportType.collectAsState()
    val importSummary by viewModel.importSummary.collectAsState()

    var rawInputText by remember(currentType) { mutableStateOf("") }
    var showTemplateDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgLight)
            .padding(16.dp)
    ) {
        // Page Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "MODUL IMPOR DATA EXCEL & CSV",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = NavyDark
                )
                Text(
                    text = "Fitur utama: Impor data terstruktur, validasi otomatis, deteksi duplikat, dan preview sebelum simpan",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Button(
                onClick = { showTemplateDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = NavyDark),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Template Excel Resmi", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Step 1: Select Sheet Type
        Text(text = "1. PILIH SHEET / JENIS DATA YANG AKAN DIIMPOR:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(ExcelCsvManager.ImportType.values()) { type ->
                val isSelected = currentType == type
                Surface(
                    color = if (isSelected) NavyPrimary else Color.White,
                    border = BorderStroke(1.dp, if (isSelected) NavyPrimary else BorderColor),
                    shape = RoundedCornerShape(8.dp),
                    onClick = {
                        viewModel.setImportType(type)
                        rawInputText = ""
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = when (type) {
                                ExcelCsvManager.ImportType.MASTER_KOLOM -> Icons.Default.ViewAgenda
                                ExcelCsvManager.ImportType.DAFTAR_PEMILIH -> Icons.Default.People
                                ExcelCsvManager.ImportType.MASTER_CALON -> Icons.Default.HowToVote
                                ExcelCsvManager.ImportType.MASTER_PANITIA -> Icons.Default.Badge
                                ExcelCsvManager.ImportType.MASTER_UNSUR -> Icons.Default.Layers
                            },
                            contentDescription = null,
                            tint = if (isSelected) Color.White else NavyPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = type.title,
                            color = if (isSelected) Color.White else TextPrimary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Step 2: Input Data / Load Sample
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, BorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "2. INPUT / PASTE DATA (${currentType.title}):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = {
                                val sample = ExcelCsvManager.getSampleData(currentType)
                                rawInputText = sample
                                viewModel.previewImportText(sample)
                            },
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(14.dp), tint = InfoTeal)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Muat Contoh Data GMIM", fontSize = 11.sp, color = InfoTeal)
                        }

                        Button(
                            onClick = {
                                if (rawInputText.isNotBlank()) {
                                    viewModel.previewImportText(rawInputText)
                                } else {
                                    viewModel.showMessage("Harap masukkan atau muat data terlebih dahulu", true)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Preview, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Analisis & Preview", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = rawInputText,
                    onValueChange = { rawInputText = it },
                    placeholder = {
                        Text(
                            "Tempelkan isi baris CSV / Excel di sini, atau klik tombol 'Muat Contoh Data GMIM' di atas.\n\nStruktur Header yang didukung:\n${currentType.headers.joinToString(", ")}",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, fontFamily = FontFamily.Monospace),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Step 3: Preview Table & Verification Summary
        if (importSummary != null) {
            val sum = importSummary!!
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, BorderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Summary counts banner
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "3. HASIL ANALISIS & PRATINJAU DATA",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyDark
                            )
                            Text(
                                text = "Menampilkan ${sum.previewRows.size} baris data yang dipetakan",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        // Counts pills
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            BadgeCount(label = "Valid", count = "${sum.validRows}", bg = SuccessGreenLight, text = SuccessGreen)
                            BadgeCount(label = "Invalid", count = "${sum.invalidRows}", bg = DangerRedLight, text = DangerRed)
                            BadgeCount(label = "Duplikat", count = "${sum.duplicateRows}", bg = DiakenGoldLight, text = DiakenGold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Error log summary if any
                    if (sum.errorsList.isNotEmpty()) {
                        Surface(
                            color = DangerRedLight,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = "Ditemukan ${sum.errorsList.size} Masalah Validasi:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DangerRed
                                )
                                sum.errorsList.take(3).forEach { err ->
                                    Text(text = "• $err", fontSize = 10.sp, color = DangerRed)
                                }
                                if (sum.errorsList.size > 3) {
                                    Text(
                                        text = "+ ${sum.errorsList.size - 3} kesalahan lainnya...",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = DangerRed
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Data Table (Horizontal and vertical scroll)
                    val hScroll = rememberScrollState()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .horizontalScroll(hScroll)
                    ) {
                        LazyColumn(modifier = Modifier.fillMaxWidth()) {
                            // Header row
                            item {
                                Row(
                                    modifier = Modifier
                                        .background(NavyDark, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                        .padding(8.dp)
                                ) {
                                    TableCell(text = "No", width = 45.dp, isHeader = true)
                                    TableCell(text = "Status Baris", width = 110.dp, isHeader = true)
                                    currentType.headers.forEach { h ->
                                        TableCell(text = h, width = 130.dp, isHeader = true)
                                    }
                                }
                            }

                            items(sum.previewRows) { row ->
                                val rowBg = when {
                                    row.isDuplicate -> DiakenGoldLight.copy(alpha = 0.5f)
                                    !row.isValid -> DangerRedLight.copy(alpha = 0.5f)
                                    else -> Color.White
                                }

                                Row(
                                    modifier = Modifier
                                        .background(rowBg)
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TableCell(text = "${row.rowNumber}", width = 45.dp)
                                    Row(
                                        modifier = Modifier.width(110.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = when {
                                                !row.isValid -> Icons.Default.Cancel
                                                row.isDuplicate -> Icons.Default.Warning
                                                else -> Icons.Default.CheckCircle
                                            },
                                            contentDescription = null,
                                            tint = when {
                                                !row.isValid -> DangerRed
                                                row.isDuplicate -> DiakenGold
                                                else -> SuccessGreen
                                            },
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = when {
                                                !row.isValid -> "Tidak Valid"
                                                row.isDuplicate -> "Duplikat"
                                                else -> "Valid"
                                            },
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when {
                                                !row.isValid -> DangerRed
                                                row.isDuplicate -> DiakenGold
                                                else -> SuccessGreen
                                            }
                                        )
                                    }

                                    currentType.headers.forEach { h ->
                                        TableCell(text = row.data[h] ?: "-", width = 130.dp)
                                    }
                                }
                                HorizontalDivider(color = BorderColor)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Bottom Confirmation Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.cancelImport() },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Batalkan Impor", color = DangerRed, fontSize = 12.sp)
                        }

                        Button(
                            onClick = { viewModel.confirmImport() },
                            enabled = sum.validRows > 0,
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Konfirmasi Impor (${sum.validRows} Data Valid)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        } else {
            // Placeholder info box when no preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = TextMuted, modifier = Modifier.size(56.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Pilih jenis data dan masukkan teks atau muat contoh untuk memulai preview", color = TextSecondary, fontSize = 13.sp)
                    Text("Aplikasi tidak akan mengubah database sebelum Anda meninjau baris data.", color = TextMuted, fontSize = 11.sp)
                }
            }
        }
    }

    // Official Template Modal
    if (showTemplateDialog) {
        val tmpl = ExcelCsvManager.getEmptyTemplate(currentType)
        AlertDialog(
            onDismissRequest = { showTemplateDialog = false },
            title = { Text("Template Resmi: ${currentType.title}") },
            text = {
                Column {
                    Text("Salin struktur kolom berikut untuk file Excel / CSV Anda:", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = tmpl,
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Tips: Anda dapat mengunduh atau menyalin contoh data langsung dengan tombol 'Muat Contoh Data GMIM'.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    rawInputText = ExcelCsvManager.getSampleData(currentType)
                    viewModel.previewImportText(rawInputText)
                    showTemplateDialog = false
                }) {
                    Text("Gunakan Contoh Ini")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTemplateDialog = false }) {
                    Text("Tutup")
                }
            }
        )
    }
}

@Composable
fun TableCell(text: String, width: androidx.compose.ui.unit.Dp, isHeader: Boolean = false) {
    Text(
        text = text,
        fontSize = if (isHeader) 11.sp else 10.sp,
        fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
        color = if (isHeader) Color.White else TextPrimary,
        maxLines = 1,
        modifier = Modifier.width(width)
    )
}

@Composable
fun BadgeCount(label: String, count: String, bg: Color, text: Color) {
    Surface(color = bg, shape = RoundedCornerShape(4.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = label, fontSize = 10.sp, color = text)
            Text(text = count, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = text)
        }
    }
}
