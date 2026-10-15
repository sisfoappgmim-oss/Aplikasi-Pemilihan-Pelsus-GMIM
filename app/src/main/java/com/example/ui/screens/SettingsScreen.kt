package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PeranPengguna
import com.example.ui.theme.*
import com.example.ui.viewmodel.ElectionViewModel

@Composable
fun SettingsScreen(
    viewModel: ElectionViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val churchName by viewModel.churchName.collectAsState()
    val jemaatName by viewModel.jemaatName.collectAsState()
    val wilayahName by viewModel.wilayahName.collectAsState()
    val electionYear by viewModel.electionYear.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()

    var editChurch by remember(churchName) { mutableStateOf(churchName) }
    var editJemaat by remember(jemaatName) { mutableStateOf(jemaatName) }
    var editWilayah by remember(wilayahName) { mutableStateOf(wilayahName) }
    var editYear by remember(electionYear) { mutableStateOf(electionYear) }

    var showResetConfirm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgLight)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Page Title
        item {
            Column {
                Text(
                    text = "PENGATURAN SISTEM & KEAMANAN",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = NavyDark
                )
                Text(
                    text = "Konfigurasi jemaat, manajemen hak akses pengguna, backup data, dan riwayat audit",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        // Section 1: Role Switcher Simulation
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, BorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. SIMULASI PERAN DAN HAK AKSES PENGGUNA",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyDark
                    )
                    Text(
                        text = "Peran aktif saat ini: ${currentUser.peran.label} (${currentUser.nama})",
                        fontSize = 11.sp,
                        color = NavyPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    PeranPengguna.values().forEach { role ->
                        val isSelected = currentUser.peran == role
                        Surface(
                            color = if (isSelected) NavyMedium else Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (isSelected) NavyPrimary else BorderColor),
                            onClick = { viewModel.switchRole(role) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = role.label,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isSelected) Color.White else TextPrimary
                                    )
                                    Text(
                                        text = when (role) {
                                            PeranPengguna.SUPER_ADMIN -> "Mengelola seluruh aplikasi, konfigurasi, dan verifikasi akhir"
                                            PeranPengguna.ADMIN_PEMILIHAN -> "Mengelola master data, impor Excel, dan pengaturan pemilihan"
                                            PeranPengguna.PANITIA_KOLOM -> "Mengisi daftar hadir dan mencatat perolehan suara kolom"
                                            PeranPengguna.VERIFIKATOR -> "Memeriksa keabsahan dan memverifikasi hasil suara"
                                            PeranPengguna.PENGAWAS -> "Memantau dashboard, statistik, dan mencetak dokumen BAP"
                                        },
                                        fontSize = 10.sp,
                                        color = if (isSelected) Color(0xFFCBD5E1) else TextSecondary
                                    )
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = DiakenGold, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Church Profile
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, BorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "2. IDENTITAS RESMI GEREJA & JEMAAT",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyDark
                    )
                    Text(
                        text = "Data ini otomatis dicantumkan pada kop surat dan Berita Acara Pemilihan (BAP)",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = editChurch,
                        onValueChange = { editChurch = it },
                        label = { Text("Nama Sinode / Gereja") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = editJemaat,
                        onValueChange = { editJemaat = it },
                        label = { Text("Nama Jemaat") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = editWilayah,
                        onValueChange = { editWilayah = it },
                        label = { Text("Nama Wilayah") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = editYear,
                        onValueChange = { editYear = it },
                        label = { Text("Periode Pelayanan") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            viewModel.updateChurchSettings(editChurch, editJemaat, editWilayah, editYear)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Simpan Identitas Gereja")
                    }
                }
            }
        }

        // Section 3: Backup, Export & Reset
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, BorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "3. BACKUP, EKSPOR DATA & PEMULIHAN",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyDark
                    )
                    Text(
                        text = "Pencadangan data terstruktur dalam format JSON/CSV dan pemulihan data lokal",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Backup Database GMIM Pelsus")
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        """
{
  "app": "PEMILIHAN PELSUS GMIM",
  "jemaat": "$jemaatName",
  "wilayah": "$wilayahName",
  "periode": "$electionYear",
  "backup_time": "${System.currentTimeMillis()}",
  "database_type": "LOCAL_OFFLINE_READY",
  "kolom_count": 12
}
                                        """.trimIndent()
                                    )
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Ekspor Backup JSON"))
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ekspor JSON", fontSize = 11.sp)
                        }

                        Button(
                            onClick = { showResetConfirm = true },
                            colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset Data Demo", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Section 4: Audit Logs (Catatan Riwayat)
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, BorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "4. CATATAN RIWAYAT AKTIVITAS (AUDIT LOG)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyDark
                    )
                    Text(
                        text = "Semua aksi penting (impor, koreksi, kehadiran, finalisasi) dicatat secara permanen",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    auditLogs.take(15).forEach { log ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = log.tindakan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = NavyPrimary
                                )
                                Text(
                                    text = log.timestampWita,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextMuted
                                )
                            }
                            Text(
                                text = "${log.detail} • Oleh ${log.pengguna} (${log.peran.label})",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                            HorizontalDivider(color = BorderColor, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Konfirmasi Muat Ulang Data Demo") },
            text = { Text("Tindakan ini akan mengembalikan data 12 kolom, pemilih, calon, dan hasil pemilihan GMIM ke data awal demonstrasi.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetToDemoData()
                        showResetConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Muat Ulang")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
