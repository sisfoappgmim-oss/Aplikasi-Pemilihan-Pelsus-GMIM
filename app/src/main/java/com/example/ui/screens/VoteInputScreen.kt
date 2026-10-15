package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ElectionViewModel

@Composable
fun VoteInputScreen(
    viewModel: ElectionViewModel,
    modifier: Modifier = Modifier
) {
    val koloms by viewModel.kolomList.collectAsState()
    val pemilihs by viewModel.pemilihList.collectAsState()
    val kehadirans by viewModel.kehadiranList.collectAsState()
    val calons by viewModel.calonList.collectAsState()
    val hasils by viewModel.hasilList.collectAsState()
    val selectedKolomCode by viewModel.selectedKolomCode.collectAsState()
    val selectedJabatan by viewModel.selectedJabatan.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val activeKolom = koloms.find { it.kodeKolom == selectedKolomCode } ?: koloms.firstOrNull()
    val activeKolomCode = activeKolom?.kodeKolom ?: ""

    val votersInKolom = pemilihs.filter { it.kodeKolom == activeKolomCode }
    val hadirCount = kehadirans.count { it.kodeKolom == activeKolomCode && it.hadir }

    // Candidates for active column and selected role
    val filteredCalons = calons.filter {
        (it.kodeKolom == activeKolomCode || it.cakupan == "Bersama") && it.jenisJabatan == selectedJabatan
    }

    // Existing saved result if any
    val existingResult = hasils.find { it.kodeKolom == activeKolomCode && it.jenisJabatan == selectedJabatan }

    // Form states
    var voteInputs by remember(activeKolomCode, selectedJabatan, existingResult) {
        val initialMap = mutableStateMapOf<String, String>()
        filteredCalons.forEach { c ->
            val prevVotes = existingResult?.suaraCalon?.get(c.kodeCalon) ?: 0
            initialMap[c.kodeCalon] = prevVotes.toString()
        }
        mutableStateOf(initialMap)
    }

    var suaraRusakInput by remember(activeKolomCode, selectedJabatan, existingResult) {
        mutableStateOf(existingResult?.suaraRusak?.toString() ?: "0")
    }

    var suaraTidakDigunakanInput by remember(activeKolomCode, selectedJabatan, existingResult) {
        mutableStateOf(existingResult?.suratSuaraTidakDigunakan?.toString() ?: (votersInKolom.size - hadirCount).coerceAtLeast(0).toString())
    }

    var catatanInput by remember(activeKolomCode, selectedJabatan, existingResult) {
        mutableStateOf(existingResult?.catatan ?: "")
    }

    var showCorrectionDialog by remember { mutableStateOf(false) }
    var correctionReasonText by remember { mutableStateOf("") }
    var pendingFinalize by remember { mutableStateOf(false) }

    // Live Calculations
    val parsedCandidateVotes = voteInputs.mapValues { it.value.toIntOrNull() ?: 0 }
    val totalCandidateVotes = parsedCandidateVotes.values.sum()
    val parsedSuaraRusak = suaraRusakInput.toIntOrNull() ?: 0
    val parsedSuaraTidakDigunakan = suaraTidakDigunakanInput.toIntOrNull() ?: 0
    val totalSuaraMasuk = totalCandidateVotes + parsedSuaraRusak

    // Discrepancy checks
    val hasDiscrepancyWithHadir = totalSuaraMasuk != hadirCount
    val discrepancyDiff = totalSuaraMasuk - hadirCount

    // Add candidate dialog
    var showAddCandidateDialog by remember { mutableStateOf(false) }
    var newCandidateName by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgLight)
            .padding(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "FORMULIR INPUT HASIL PEMILIHAN",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = NavyDark
                )
                Text(
                    text = "Pencatatan perolehan suara sah, rusak, dan validasi selisih surat suara",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            // Existing Status Badge
            if (existingResult != null) {
                Surface(
                    color = when (existingResult.statusHasil) {
                        StatusHasil.TERVERIFIKASI -> SuccessGreenLight
                        StatusHasil.MENUNGGU_VERIFIKASI -> PenatuaBlueLight
                        StatusHasil.TERSIMPAN -> DiakenGoldLight
                        else -> Color(0xFFF1F5F9)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = existingResult.statusHasil.label,
                        color = when (existingResult.statusHasil) {
                            StatusHasil.TERVERIFIKASI -> SuccessGreen
                            StatusHasil.MENUNGGU_VERIFIKASI -> PenatuaBlue
                            StatusHasil.TERSIMPAN -> DiakenGold
                            else -> TextSecondary
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
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

        // Position Toggle (Diaken vs Penatua)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { viewModel.setSelectedJabatan(JenisJabatan.DIAKEN) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedJabatan == JenisJabatan.DIAKEN) DiakenGold else Color.White,
                    contentColor = if (selectedJabatan == JenisJabatan.DIAKEN) Color.White else DiakenGold
                ),
                border = BorderStroke(1.dp, DiakenGold),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.VolunteerActivism, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Pemilihan DIAKEN", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            Button(
                onClick = { viewModel.setSelectedJabatan(JenisJabatan.PENATUA) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedJabatan == JenisJabatan.PENATUA) PenatuaBlue else Color.White,
                    contentColor = if (selectedJabatan == JenisJabatan.PENATUA) Color.White else PenatuaBlue
                ),
                border = BorderStroke(1.dp, PenatuaBlue),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Pemilihan PENATUA", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Information banner: Pemilih terdaftar & Hadir
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, BorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Pemilih Terdaftar", fontSize = 11.sp, color = TextSecondary)
                    Text(text = "${votersInKolom.size}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                }
                VerticalDivider(modifier = Modifier.height(28.dp), color = BorderColor)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Pemilih Hadir", fontSize = 11.sp, color = TextSecondary)
                    Text(text = "$hadirCount", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                }
                VerticalDivider(modifier = Modifier.height(28.dp), color = BorderColor)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Total Suara Masuk", fontSize = 11.sp, color = TextSecondary)
                    Text(text = "$totalSuaraMasuk", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (hasDiscrepancyWithHadir) DangerRed else NavyDark)
                }
            }
        }

        // Discrepancy Alert
        if (hasDiscrepancyWithHadir && totalSuaraMasuk > 0) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = DangerRedLight),
                border = BorderStroke(1.dp, DangerRed),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = DangerRed, modifier = Modifier.size(18.dp))
                    Text(
                        text = "Peringatan Selisih: Total suara ($totalSuaraMasuk) ${if (discrepancyDiff > 0) "lebih banyak $discrepancyDiff" else "kurang ${-discrepancyDiff}"} dibanding pemilih hadir ($hadirCount). Periksa kembali surat suara.",
                        fontSize = 11.sp,
                        color = DangerRed,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Candidates List & Vote inputs
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "DAFTAR CALON ${selectedJabatan.label.uppercase()}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                    TextButton(onClick = { showAddCandidateDialog = true }) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tambah Calon Lain", fontSize = 11.sp)
                    }
                }
            }

            if (filteredCalons.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Belum ada calon ${selectedJabatan.label} untuk kolom ini. Klik 'Tambah Calon Lain' untuk mendaftarkan.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                items(filteredCalons) { calon ->
                    val currentVal = voteInputs[calon.kodeCalon] ?: "0"

                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, BorderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (selectedJabatan == JenisJabatan.DIAKEN) DiakenGoldLight else PenatuaBlueLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${calon.nomorUrut}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (selectedJabatan == JenisJabatan.DIAKEN) DiakenGold else PenatuaBlue
                                    )
                                }

                                Column {
                                    Text(
                                        text = calon.namaCalon,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = NavyDark
                                    )
                                    Text(
                                        text = "Kode: ${calon.kodeCalon} • Status: ${calon.statusCalon}",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            // Stepper or Input Field
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        val v = (currentVal.toIntOrNull() ?: 0) - 1
                                        if (v >= 0) {
                                            voteInputs[calon.kodeCalon] = v.toString()
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Kurang")
                                }

                                OutlinedTextField(
                                    value = currentVal,
                                    onValueChange = { input ->
                                        if (input.all { it.isDigit() } || input.isEmpty()) {
                                            voteInputs[calon.kodeCalon] = input
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.width(64.dp)
                                )

                                IconButton(
                                    onClick = {
                                        val v = (currentVal.toIntOrNull() ?: 0) + 1
                                        voteInputs[calon.kodeCalon] = v.toString()
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Tambah")
                                }
                            }
                        }
                    }
                }
            }

            // Suara Rusak / Batal & Surat Suara Tidak Digunakan
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "SURAT SUARA TAMBAHAN", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
            }

            item {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, BorderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Suara Rusak / Batal", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DangerRed)
                                Text("Surat suara dicoblos tidak sah", fontSize = 11.sp, color = TextSecondary)
                            }
                            OutlinedTextField(
                                value = suaraRusakInput,
                                onValueChange = { if (it.all { ch -> ch.isDigit() } || it.isEmpty()) suaraRusakInput = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.width(72.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = BorderColor)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Surat Suara Tidak Digunakan", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyPrimary)
                                Text("Sisa surat suara belum terpakai", fontSize = 11.sp, color = TextSecondary)
                            }
                            OutlinedTextField(
                                value = suaraTidakDigunakanInput,
                                onValueChange = { if (it.all { ch -> ch.isDigit() } || it.isEmpty()) suaraTidakDigunakanInput = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.width(72.dp)
                            )
                        }
                    }
                }
            }

            // Catatan Panitia
            item {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = catatanInput,
                    onValueChange = { catatanInput = it },
                    label = { Text("Catatan Pelaksanaan Panitia (Opsional)") },
                    placeholder = { Text("Contoh: Pemilihan berjalan tertib dan lancar...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    minLines = 2
                )
            }

            // Correction history if any
            if (existingResult?.riwayatKoreksi?.isNotEmpty() == true) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = BorderStroke(1.dp, BorderColor),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Riwayat Koreksi Resmi:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                            existingResult.riwayatKoreksi.forEach { log ->
                                Text("• $log", fontSize = 10.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Action Buttons: Save Draft vs Finalize
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = {
                    val isOldFinal = existingResult?.isFinal == true
                    if (isOldFinal) {
                        pendingFinalize = false
                        showCorrectionDialog = true
                    } else {
                        viewModel.saveVoteResult(
                            kodeKolom = activeKolomCode,
                            jenisJabatan = selectedJabatan,
                            suaraCalon = parsedCandidateVotes,
                            suaraSah = totalCandidateVotes,
                            suaraRusak = parsedSuaraRusak,
                            suaraTidakDigunakan = parsedSuaraTidakDigunakan,
                            catatan = catatanInput,
                            isFinal = false
                        )
                    }
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Simpan Draft", fontSize = 12.sp)
            }

            Button(
                onClick = {
                    val isOldFinal = existingResult?.isFinal == true
                    if (isOldFinal) {
                        pendingFinalize = true
                        showCorrectionDialog = true
                    } else {
                        viewModel.saveVoteResult(
                            kodeKolom = activeKolomCode,
                            jenisJabatan = selectedJabatan,
                            suaraCalon = parsedCandidateVotes,
                            suaraSah = totalCandidateVotes,
                            suaraRusak = parsedSuaraRusak,
                            suaraTidakDigunakan = parsedSuaraTidakDigunakan,
                            catatan = catatanInput,
                            isFinal = true
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = if (selectedJabatan == JenisJabatan.DIAKEN) DiakenGold else PenatuaBlue),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Finalisasi Hasil", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }

    // Modal Koreksi Hasil (wajib isi alasan bila data sudah final)
    if (showCorrectionDialog) {
        AlertDialog(
            onDismissRequest = { showCorrectionDialog = false },
            title = { Text("Prosedur Koreksi Hasil Final") },
            text = {
                Column {
                    Text(
                        "Hasil perolehan suara ini sebelumnya telah difinalisasi. Perubahan wajib menyertakan alasan koreksi resmi untuk audit log panitia:",
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = correctionReasonText,
                        onValueChange = { correctionReasonText = it },
                        placeholder = { Text("Masukkan alasan koreksi...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (correctionReasonText.isBlank()) {
                            viewModel.showMessage("Alasan koreksi wajib diisi", true)
                            return@Button
                        }
                        viewModel.saveVoteResult(
                            kodeKolom = activeKolomCode,
                            jenisJabatan = selectedJabatan,
                            suaraCalon = parsedCandidateVotes,
                            suaraSah = totalCandidateVotes,
                            suaraRusak = parsedSuaraRusak,
                            suaraTidakDigunakan = parsedSuaraTidakDigunakan,
                            catatan = catatanInput,
                            isFinal = pendingFinalize,
                            correctionReason = correctionReasonText
                        )
                        showCorrectionDialog = false
                        correctionReasonText = ""
                    }
                ) {
                    Text("Konfirmasi Koreksi")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCorrectionDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Modal Tambah Calon Lain
    if (showAddCandidateDialog) {
        AlertDialog(
            onDismissRequest = { showAddCandidateDialog = false },
            title = { Text("Tambah Calon ${selectedJabatan.label} Lain") },
            text = {
                Column {
                    Text("Daftarkan calon baru pada $activeKolomCode:", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newCandidateName,
                        onValueChange = { newCandidateName = it },
                        placeholder = { Text("Nama Lengkap Calon...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCandidateName.isNotBlank()) {
                            val nextNo = (filteredCalons.maxOfOrNull { it.nomorUrut } ?: 0) + 1
                            val newC = Calon(
                                kodeCalon = "CLN-${selectedJabatan.name.take(1)}-${activeKolomCode}-$nextNo",
                                namaCalon = newCandidateName.trim(),
                                jenisJabatan = selectedJabatan,
                                kodeKolom = activeKolomCode,
                                nomorUrut = nextNo
                            )
                            viewModel.addCalon(newC)
                            newCandidateName = ""
                            showAddCandidateDialog = false
                        }
                    }
                ) {
                    Text("Simpan Calon")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCandidateDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
