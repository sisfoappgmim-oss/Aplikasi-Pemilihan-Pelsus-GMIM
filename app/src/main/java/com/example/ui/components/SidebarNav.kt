package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.ElectionViewModel

data class NavItem(
    val screen: AppScreen,
    val icon: ImageVector,
    val isDev: Boolean = false,
    val badge: String? = null
)

data class NavSection(
    val title: String,
    val items: List<NavItem>
)

@Composable
fun SidebarNav(
    viewModel: ElectionViewModel,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val scrollState = rememberScrollState()

    val sections = listOf(
        NavSection(
            title = "UTAMA",
            items = listOf(
                NavItem(AppScreen.BERANDA, Icons.Default.Dashboard),
                NavItem(AppScreen.PROGRES_PEMILIHAN, Icons.Default.PieChart)
            )
        ),
        NavSection(
            title = "PEMILIHAN PELSUS KOLOM",
            items = listOf(
                NavItem(AppScreen.STATUS_PILIH, Icons.Default.Ballot),
                NavItem(AppScreen.DAFTAR_HADIR, Icons.Default.HowToReg),
                NavItem(AppScreen.INPUT_HASIL, Icons.Default.EditNote),
                NavItem(AppScreen.HASIL_PEMILIHAN, Icons.Default.Assessment)
            )
        ),
        NavSection(
            title = "MASTER DATA & DOKUMEN",
            items = listOf(
                NavItem(AppScreen.IMPOR_EXCEL, Icons.Default.UploadFile, badge = "Utama"),
                NavItem(AppScreen.MASTER_DATA, Icons.Default.Storage),
                NavItem(AppScreen.PANITIA, Icons.Default.Badge),
                NavItem(AppScreen.LOKASI_PEMILIHAN, Icons.Default.LocationOn),
                NavItem(AppScreen.LAPORAN_BAP, Icons.Default.Print)
            )
        ),
        NavSection(
            title = "KATEGORIAL & SINODAL",
            items = listOf(
                NavItem(AppScreen.PEMILIHAN_BIPRA, Icons.Default.Groups, isDev = true),
                NavItem(AppScreen.PEMILIHAN_BPMJ, Icons.Default.AccountBalance, isDev = true),
                NavItem(AppScreen.PEMILIHAN_LANSIA, Icons.Default.Elderly, isDev = true)
            )
        ),
        NavSection(
            title = "SISTEM",
            items = listOf(
                NavItem(AppScreen.PENGATURAN, Icons.Default.Settings)
            )
        )
    )

    Column(
        modifier = modifier
            .width(260.dp)
            .fillMaxHeight()
            .background(NavyPrimary)
            .verticalScroll(scrollState)
            .padding(vertical = 16.dp, horizontal = 12.dp)
    ) {
        // App Brand Box
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp, start = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DiakenGold),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Church,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column {
                Text(
                    text = "PELSUS GMIM",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Sistem Kolom Terpadu",
                    color = Color(0xFF93C5FD),
                    fontSize = 11.sp
                )
            }
        }

        HorizontalDivider(color = NavyMedium, thickness = 1.dp, modifier = Modifier.padding(bottom = 12.dp))

        // Navigation Sections
        sections.forEach { section ->
            Text(
                text = section.title,
                color = Color(0xFF64748B),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
            )

            section.items.forEach { item ->
                val isSelected = currentScreen == item.screen
                val bgColor = when {
                    isSelected -> NavyMedium
                    else -> Color.Transparent
                }
                val textColor = when {
                    isSelected -> Color.White
                    item.isDev -> Color(0xFF94A3B8)
                    else -> Color(0xFFE2E8F0)
                }
                val iconTint = when {
                    isSelected -> DiakenGold
                    item.isDev -> Color(0xFF64748B)
                    else -> Color(0xFF93C5FD)
                }

                Surface(
                    color = bgColor,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onNavigate(item.screen) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.screen.title,
                                tint = iconTint,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = item.screen.title,
                                color = textColor,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }

                        if (item.badge != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(DiakenGold)
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = item.badge,
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else if (item.isDev) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF334155))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Dev",
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.weight(1f))

        // Footer version info
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .background(NavyDark, RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            Column {
                Text(
                    text = "GMIM SI-PELSUS v1.0",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Zona WITA (Asia/Makassar)",
                    color = Color(0xFF94A3B8),
                    fontSize = 9.sp
                )
            }
        }
    }
}
