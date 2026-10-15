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
import com.example.data.model.CatatanKehadiran
import com.example.data.model.Pemilih
import com.example.ui.theme.*
import com.example.ui.viewmodel.ElectionViewModel

@Composable
fun AttendanceScreen(
    viewModel: ElectionViewModel,
    modifier: Modifier = Modifier
) {
    val koloms by viewModel.kolomList.collectAsState()
    val pemilihs by viewModel.pemilihList.collectAsState()
    val kehadirans by viewModel.kehadiranList.collectAsState()
    val selectedKolomCode by viewModel.selectedKolomCode.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var filterStatus by remember { mutableStateOf<Boolean?>(null) } // null = all, true = hadir, false = belum hadir
    var selectedJabatanTab by remember { mutableStateOf(0) } // 0 = Kehadiran Terpadu / Bersama, 1 = Diaken, 2 = Penatua

    val activeKolom = koloms.find { it.kodeKolom == selectedKolomCode } ?: koloms.firstOrNull()
    val activeKolomCode = activeKolom?.kodeKolom ?: ""

    // Filter voters in active column
    val columnVoters = pemilihs.filter { it.kodeKolom == activeKolomCode }
    val hadirMap = remember(kehadirans, activeKolomCode) {
        kehadirans.filter { it.kodeKolom == activeKolomCode }.associateBy { it.kodePemilih }
    }

    val filteredVoters = columnVoters.filter { voter ->
        val record = hadirMap[voter.kodePemilih]
        val isHadir = record?.hadir == true
        val matchesSearch = searchQuery.isBlank() || voter.namaPemilih.contains(searchQuery, ignoreCase = true) || voter.kodePemilih.contains(searchQuery, ignoreCase = true) || voter.alamat.contains(searchQuery, ignoreCase = true)
        val matchesStatus = filterStatus == null || (isHadir == filterStatus)
        matchesSearch && matchesStatus
    }

    val totalVoters = columnVoters.size
    val hadirCount = columnVoters.count { hadirMap[it.kodePemilih]?.hadir == true }
    val belumHadirCount = totalVoters - hadirCount
    val attendancePct = if (totalVoters > 0) (hadirCount * 100) / totalVoters else 0

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
                    text = "DAFTAR HADIR PEMILIHAN PELSUS",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = NavyDark
                )
                Text(
                    text = "Pencatatan presensi pemilih kolom dengan verifikasi waktu WITA otomatis",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = { viewModel.bulkAttendance(activeKolomCode, false) },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Reset Hadir", fontSize = 11.sp, color = DangerRed)
                }
                Button(
                    onClick = { viewModel.bulkAttendance(activeKolomCode, true) },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Semua Hadir", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Column Selector (Scrollable horizontally)
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
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = k.namaKolom,
                            color = if (isSelected) Color.White else TextPrimary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tally Card: Terdaftar, Hadir, Belum Hadir, Persentase
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, BorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                AttendanceSummaryItem(label = "Total Terdaftar", count = "$totalVoters", color = NavyPrimary)
                VerticalDivider(modifier = Modifier.height(36.dp), color = BorderColor)
                AttendanceSummaryItem(label = "Hadir", count = "$hadirCount", color = SuccessGreen)
                VerticalDivider(modifier = Modifier.height(36.dp), color = BorderColor)
                AttendanceSummaryItem(label = "Belum Hadir", count = "$belumHadirCount", color = DangerRed)
                VerticalDivider(modifier = Modifier.height(36.dp), color = BorderColor)
                AttendanceSummaryItem(label = "Partisipasi", count = "$attendancePct%", color = DiakenGold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tabs: Kehadiran Terpadu / Diaken / Penatua
        TabRow(
            selectedTabIndex = selectedJabatanTab,
            containerColor = Color.White,
            contentColor = NavyPrimary,
            modifier = Modifier.clip(RoundedCornerShape(8.dp))
        ) {
            Tab(
                selected = selectedJabatanTab == 0,
                onClick = { selectedJabatanTab = 0 },
                text = { Text("Presensi Bersama (Resmi)", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedJabatanTab == 1,
                onClick = { selectedJabatanTab = 1 },
                text = { Text("Panel Diaken", fontSize = 12.sp, color = DiakenGold) }
            )
            Tab(
                selected = selectedJabatanTab == 2,
                onClick = { selectedJabatanTab = 2 },
                text = { Text("Panel Penatua", fontSize = 12.sp, color = PenatuaBlue) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search & Filter Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari nama pemilih atau alamat...", fontSize = 12.sp) },
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

            // Filter status chips
            FilterChip(
                selected = filterStatus == null,
                onClick = { filterStatus = null },
                label = { Text("Semua", fontSize = 11.sp) }
            )
            FilterChip(
                selected = filterStatus == true,
                onClick = { filterStatus = if (filterStatus == true) null else true },
                label = { Text("Hadir", fontSize = 11.sp) }
            )
            FilterChip(
                selected = filterStatus == false,
                onClick = { filterStatus = if (filterStatus == false) null else false },
                label = { Text("Belum", fontSize = 11.sp) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Voters List
        if (filteredVoters.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.PersonOff, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Tidak ada pemilih ditemukan untuk kriteria ini", color = TextSecondary, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredVoters, key = { it.kodePemilih }) { voter ->
                    val record = hadirMap[voter.kodePemilih]
                    val isHadir = record?.hadir == true

                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isHadir) Color(0xFFF0FDF4) else Color.White),
                        border = BorderStroke(1.dp, if (isHadir) Color(0xFF86EFAC) else BorderColor),
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
                                        .background(if (isHadir) SuccessGreenLight else Color(0xFFF1F5F9)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isHadir) Icons.Default.Check else Icons.Default.Person,
                                        contentDescription = null,
                                        tint = if (isHadir) SuccessGreen else TextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = voter.namaPemilih,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = NavyDark
                                    )
                                    Text(
                                        text = "ID: ${voter.kodePemilih} • ${voter.alamat}",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                    if (isHadir && record != null) {
                                        Text(
                                            text = "Tercatat: ${record.waktuCatatWita} WITA oleh ${record.petugas}",
                                            fontSize = 10.sp,
                                            color = SuccessGreen,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            // Toggle Presence Button
                            Button(
                                onClick = {
                                    viewModel.toggleAttendance(voter.kodePemilih, activeKolomCode, !isHadir)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isHadir) SuccessGreen else Color(0xFFE2E8F0),
                                    contentColor = if (isHadir) Color.White else TextPrimary
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = if (isHadir) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isHadir) "Hadir" else "Isi Hadir",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AttendanceSummaryItem(label: String, count: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, fontSize = 11.sp, color = TextSecondary)
    }
}
