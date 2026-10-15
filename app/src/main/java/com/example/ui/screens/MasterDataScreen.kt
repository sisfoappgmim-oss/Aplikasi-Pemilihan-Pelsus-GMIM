package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import com.example.ui.theme.*
import com.example.ui.viewmodel.ElectionViewModel

@Composable
fun MasterDataScreen(
    viewModel: ElectionViewModel,
    modifier: Modifier = Modifier
) {
    val koloms by viewModel.kolomList.collectAsState()
    val pemilihs by viewModel.pemilihList.collectAsState()
    val calons by viewModel.calonList.collectAsState()
    val panitias by viewModel.panitiaList.collectAsState()
    val lokasis by viewModel.lokasiList.collectAsState()
    val unsurs by viewModel.unsurList.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    // Dialog state for new Kolom
    var showAddKolomDialog by remember { mutableStateOf(false) }
    var newKolomCode by remember { mutableStateOf("") }
    var newKolomName by remember { mutableStateOf("") }
    var newKolomLocation by remember { mutableStateOf("") }

    // Dialog state for new Pemilih
    var showAddPemilihDialog by remember { mutableStateOf(false) }
    var newPemilihName by remember { mutableStateOf("") }
    var newPemilihKolom by remember { mutableStateOf(koloms.firstOrNull()?.kodeKolom ?: "KOLOM-01") }
    var newPemilihAddress by remember { mutableStateOf("") }

    val tabs = listOf("Data Kolom", "Daftar Pemilih", "Data Calon", "Panitia", "Lokasi", "Unsur")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgLight)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "PENGELOLAAN MASTER DATA",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = NavyDark
                )
                Text(
                    text = "Kelola entitas kolom, pemilih, calon, panitia, lokasi, dan unsur dengan proteksi relasi",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Button(
                onClick = {
                    when (selectedTabIndex) {
                        0 -> showAddKolomDialog = true
                        1 -> showAddPemilihDialog = true
                        else -> viewModel.showMessage("Gunakan menu impor Excel atau formulir khusus untuk entitas ini")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NavyDark),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Tambah Data", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.White,
            contentColor = NavyPrimary,
            edgePadding = 0.dp,
            modifier = Modifier.clip(RoundedCornerShape(8.dp))
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = {
                        selectedTabIndex = index
                        searchQuery = ""
                    },
                    text = { Text(title, fontSize = 12.sp, fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Cari data ${tabs[selectedTabIndex]}...", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // List Content based on Selected Tab
        when (selectedTabIndex) {
            0 -> {
                // Kolom List
                val filtered = koloms.filter {
                    searchQuery.isBlank() || it.namaKolom.contains(searchQuery, true) || it.kodeKolom.contains(searchQuery, true)
                }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                    items(filtered) { k ->
                        val vCount = pemilihs.count { it.kodeKolom == k.kodeKolom }
                        val cCount = calons.count { it.kodeKolom == k.kodeKolom }

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
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(k.namaKolom, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyDark)
                                    Text("Kode: ${k.kodeKolom} • Lokasi: ${k.namaLokasi}", fontSize = 11.sp, color = TextSecondary)
                                    Text("Pemilih: $vCount jiwa • Calon: $cCount orang", fontSize = 11.sp, color = NavyPrimary, fontWeight = FontWeight.Medium)
                                }
                                IconButton(onClick = { viewModel.deleteKolom(k.kodeKolom) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = DangerRed)
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                // Pemilih List
                val filtered = pemilihs.filter {
                    searchQuery.isBlank() || it.namaPemilih.contains(searchQuery, true) || it.kodeKolom.contains(searchQuery, true)
                }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                    items(filtered) { p ->
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
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(p.namaPemilih, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyDark)
                                    Text("${p.kodeKolom} • ID: ${p.kodePemilih} • ${p.alamat}", fontSize = 11.sp, color = TextSecondary)
                                    if (p.nomorAnggota.isNotBlank()) {
                                        Text("No Anggota: ${p.nomorAnggota}", fontSize = 10.sp, color = TextMuted)
                                    }
                                }
                                IconButton(onClick = { viewModel.deletePemilih(p.kodePemilih) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = DangerRed)
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                // Calon List
                val filtered = calons.filter {
                    searchQuery.isBlank() || it.namaCalon.contains(searchQuery, true) || it.kodeKolom.contains(searchQuery, true)
                }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                    items(filtered) { c ->
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
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(if (c.jenisJabatan == JenisJabatan.DIAKEN) DiakenGoldLight else PenatuaBlueLight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${c.nomorUrut}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (c.jenisJabatan == JenisJabatan.DIAKEN) DiakenGold else PenatuaBlue
                                        )
                                    }
                                    Column {
                                        Text(c.namaCalon, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyDark)
                                        Text("Jabatan: ${c.jenisJabatan.label} • Kolom: ${c.kodeKolom} • Status: ${c.statusCalon}", fontSize = 11.sp, color = TextSecondary)
                                    }
                                }
                                IconButton(onClick = { viewModel.deleteCalon(c.kodeCalon) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = DangerRed)
                                }
                            }
                        }
                    }
                }
            }
            3 -> {
                // Panitia List
                val filtered = panitias.filter {
                    searchQuery.isBlank() || it.namaPanitia.contains(searchQuery, true) || it.jabatanPanitia.contains(searchQuery, true)
                }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                    items(filtered) { pnt ->
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
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(pnt.namaPanitia, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyDark)
                                    Text("${pnt.jabatanPanitia} • Penugasan: ${pnt.kodeKolom}", fontSize = 11.sp, color = TextSecondary)
                                    Text("Telp: ${pnt.nomorTelepon.ifBlank { "-" }}", fontSize = 10.sp, color = TextMuted)
                                }
                                Icon(Icons.Default.Badge, contentDescription = null, tint = InfoTeal)
                            }
                        }
                    }
                }
            }
            4 -> {
                // Lokasi List
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                    items(lokasis) { lok ->
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, BorderColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(lok.namaLokasi, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyDark)
                                Text("Alamat: ${lok.alamat}", fontSize = 11.sp, color = TextSecondary)
                                Text("Penanggung Jawab: ${lok.penanggungJawab}", fontSize = 10.sp, color = TextMuted)
                            }
                        }
                    }
                }
            }
            else -> {
                // Unsur List
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                    items(unsurs) { uns ->
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, BorderColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(uns.namaUnsur, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyDark)
                                Text("Jenis Pemilihan: ${uns.jenisPemilihan} • Keterangan: ${uns.keterangan}", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Tambah Kolom
    if (showAddKolomDialog) {
        AlertDialog(
            onDismissRequest = { showAddKolomDialog = false },
            title = { Text("Tambah Kolom Baru") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newKolomCode,
                        onValueChange = { newKolomCode = it },
                        label = { Text("Kode Kolom (e.g. KOLOM-13)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newKolomName,
                        onValueChange = { newKolomName = it },
                        label = { Text("Nama Kolom (e.g. Kolom 13 - Yerusalem)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newKolomLocation,
                        onValueChange = { newKolomLocation = it },
                        label = { Text("Lokasi Pemilihan") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newKolomCode.isNotBlank() && newKolomName.isNotBlank()) {
                            viewModel.addKolom(
                                Kolom(
                                    kodeKolom = newKolomCode.trim().uppercase(),
                                    namaKolom = newKolomName.trim(),
                                    namaLokasi = newKolomLocation.trim(),
                                    tanggalPemilihan = "17-10-2026",
                                    statusPemilihan = StatusPemilihan.BELUM_MULAI
                                )
                            )
                            showAddKolomDialog = false
                            newKolomCode = ""
                            newKolomName = ""
                            newKolomLocation = ""
                        } else {
                            viewModel.showMessage("Kode dan nama kolom wajib diisi", true)
                        }
                    }
                ) {
                    Text("Simpan Kolom")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddKolomDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Modal Tambah Pemilih
    if (showAddPemilihDialog) {
        AlertDialog(
            onDismissRequest = { showAddPemilihDialog = false },
            title = { Text("Tambah Pemilih Baru") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newPemilihName,
                        onValueChange = { newPemilihName = it },
                        label = { Text("Nama Lengkap Pemilih") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPemilihAddress,
                        onValueChange = { newPemilihAddress = it },
                        label = { Text("Alamat / Lingkungan") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPemilihName.isNotBlank()) {
                            val pCode = "PML-${newPemilihKolom}-${System.currentTimeMillis() % 10000}"
                            viewModel.addPemilih(
                                Pemilih(
                                    kodePemilih = pCode,
                                    namaPemilih = newPemilihName.trim(),
                                    kodeKolom = newPemilihKolom,
                                    alamat = newPemilihAddress.trim(),
                                    statusAktif = true
                                )
                            )
                            showAddPemilihDialog = false
                            newPemilihName = ""
                            newPemilihAddress = ""
                        } else {
                            viewModel.showMessage("Nama pemilih wajib diisi", true)
                        }
                    }
                ) {
                    Text("Simpan Pemilih")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddPemilihDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
