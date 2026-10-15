package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.data.model.PeranPengguna
import com.example.ui.theme.*
import com.example.ui.viewmodel.ElectionViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HeaderBar(
    viewModel: ElectionViewModel,
    onMenuToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val jemaatName by viewModel.jemaatName.collectAsState()

    var showRoleMenu by remember { mutableStateOf(false) }

    // Live WITA Clock
    var currentTimeString by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("Asia/Makassar")
        while (true) {
            currentTimeString = sdf.format(Date())
            delay(1000)
        }
    }

    Surface(
        color = NavyDark,
        tonalElevation = 4.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Hamburger + Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onMenuToggle,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Toggle Sidebar",
                        tint = Color.White
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "PEMILIHAN PELSUS GMIM",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            letterSpacing = 0.5.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(DiakenGold)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "KOLOM",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 9.sp
                            )
                        }
                    }
                    Text(
                        text = jemaatName,
                        color = Color(0xFF93C5FD),
                        fontSize = 11.sp
                    )
                }
            }

            // Right: Time + Status + User Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // WITA Clock Badge
                Surface(
                    color = NavyPrimary,
                    shape = RoundedCornerShape(6.dp),
                    border = null,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Waktu WITA",
                            tint = Color(0xFF67E8F9),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = if (currentTimeString.isNotBlank()) "$currentTimeString WITA" else "WITA",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Connection status chip
                Surface(
                    color = Color(0xFF064E3B),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF34D399))
                        )
                        Text(
                            text = "Mode Lokal",
                            color = Color(0xFFA7F3D0),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // User profile & role selector button
                Box {
                    Surface(
                        color = NavyMedium,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showRoleMenu = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(DiakenGold),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentUser.nama.take(1),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column {
                                Text(
                                    text = currentUser.nama,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                                Text(
                                    text = currentUser.peran.label,
                                    color = Color(0xFFFCD34D),
                                    fontSize = 9.sp
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Pilih Peran",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showRoleMenu,
                        onDismissRequest = { showRoleMenu = false }
                    ) {
                        Text(
                            text = "Simulasi Peran Hak Akses:",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                        HorizontalDivider()
                        PeranPengguna.values().forEach { role ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(role.label, fontWeight = if (currentUser.peran == role) FontWeight.Bold else FontWeight.Normal)
                                        Text(
                                            when (role) {
                                                PeranPengguna.SUPER_ADMIN -> "Akses penuh konfigurasi, master data, & verifikasi"
                                                PeranPengguna.ADMIN_PEMILIHAN -> "Kelola kolom, calon, pemilih, & impor"
                                                PeranPengguna.PANITIA_KOLOM -> "Catat kehadiran & input perolehan suara"
                                                PeranPengguna.VERIFIKATOR -> "Validasi & sahkan hasil pemilihan"
                                                PeranPengguna.PENGAWAS -> "Pantau progres, statistik, & cetak BAP"
                                            },
                                            fontSize = 10.sp,
                                            color = TextSecondary
                                        )
                                    }
                                },
                                onClick = {
                                    viewModel.switchRole(role)
                                    showRoleMenu = false
                                },
                                leadingIcon = {
                                    if (currentUser.peran == role) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = SuccessGreen)
                                    } else {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = TextMuted)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
