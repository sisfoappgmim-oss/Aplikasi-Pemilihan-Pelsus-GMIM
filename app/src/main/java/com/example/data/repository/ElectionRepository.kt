package com.example.data.repository

import com.example.data.excel.ExcelCsvManager
import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

class ElectionRepository private constructor() {

    companion object {
        @Volatile
        private var instance: ElectionRepository? = null

        fun getInstance(): ElectionRepository {
            return instance ?: synchronized(this) {
                instance ?: ElectionRepository().also { instance = it }
            }
        }
    }

    private val witaTimeZone = TimeZone.getTimeZone("Asia/Makassar")

    private fun getCurrentWitaTime(): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.timeZone = witaTimeZone
        return sdf.format(Date())
    }

    private fun getCurrentWitaDateTime(): String {
        val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        sdf.timeZone = witaTimeZone
        return "${sdf.format(Date())} WITA"
    }

    private val _currentUser = MutableStateFlow(
        AppUser(
            id = "USR-001",
            username = "superadmin",
            nama = "Pnt. James Rumagit",
            peran = PeranPengguna.SUPER_ADMIN,
            assignedKolom = null
        )
    )
    val currentUser: StateFlow<AppUser> = _currentUser.asStateFlow()

    private val _churchName = MutableStateFlow("GEREJA MASEHI INJILI di MINAHASA (GMIM)")
    val churchName: StateFlow<String> = _churchName.asStateFlow()

    private val _jemaatName = MutableStateFlow("Jemaat EFRATA SENTRUM")
    val jemaatName: StateFlow<String> = _jemaatName.asStateFlow()

    private val _wilayahName = MutableStateFlow("Wilayah Manado Sentrum")
    val wilayahName: StateFlow<String> = _wilayahName.asStateFlow()

    private val _electionYear = MutableStateFlow("2026 - 2030")
    val electionYear: StateFlow<String> = _electionYear.asStateFlow()

    private val _kolomList = MutableStateFlow<List<Kolom>>(emptyList())
    val kolomList: StateFlow<List<Kolom>> = _kolomList.asStateFlow()

    private val _pemilihList = MutableStateFlow<List<Pemilih>>(emptyList())
    val pemilihList: StateFlow<List<Pemilih>> = _pemilihList.asStateFlow()

    private val _calonList = MutableStateFlow<List<Calon>>(emptyList())
    val calonList: StateFlow<List<Calon>> = _calonList.asStateFlow()

    private val _panitiaList = MutableStateFlow<List<Panitia>>(emptyList())
    val panitiaList: StateFlow<List<Panitia>> = _panitiaList.asStateFlow()

    private val _lokasiList = MutableStateFlow<List<Lokasi>>(emptyList())
    val lokasiList: StateFlow<List<Lokasi>> = _lokasiList.asStateFlow()

    private val _unsurList = MutableStateFlow<List<Unsur>>(emptyList())
    val unsurList: StateFlow<List<Unsur>> = _unsurList.asStateFlow()

    private val _kehadiranList = MutableStateFlow<List<CatatanKehadiran>>(emptyList())
    val kehadiranList: StateFlow<List<CatatanKehadiran>> = _kehadiranList.asStateFlow()

    private val _hasilList = MutableStateFlow<List<HasilPerolehan>>(emptyList())
    val hasilList: StateFlow<List<HasilPerolehan>> = _hasilList.asStateFlow()

    private val _auditLogs = MutableStateFlow<List<AuditLog>>(emptyList())
    val auditLogs: StateFlow<List<AuditLog>> = _auditLogs.asStateFlow()

    init {
        loadInitialData()
    }

    fun switchUserRole(peran: PeranPengguna, assignedKolom: String? = null) {
        val user = when (peran) {
            PeranPengguna.SUPER_ADMIN -> AppUser("USR-01", "superadmin", "Pnt. James Rumagit (Super Admin)", peran, null)
            PeranPengguna.ADMIN_PEMILIHAN -> AppUser("USR-02", "admin", "Dkn. Ruth Sumendap (Admin)", peran, null)
            PeranPengguna.PANITIA_KOLOM -> AppUser("USR-03", "panitia1", "Bpk. Hendra Rondonuwu (Panitia Kolom 1)", peran, assignedKolom ?: "KOLOM-01")
            PeranPengguna.VERIFIKATOR -> AppUser("USR-04", "verifikator", "Pdt. Stefanus Wowor, M.Th (Verifikator)", peran, null)
            PeranPengguna.PENGAWAS -> AppUser("USR-05", "pengawas", "Pnt. DR. Royke Pangalila (Pengawas)", peran, null)
        }
        _currentUser.value = user
        addAuditLog("Ganti Pengguna", "Beralih ke peran ${peran.label} (${user.nama})")
    }

    fun addAuditLog(tindakan: String, detail: String) {
        val log = AuditLog(
            id = "LOG-" + UUID.randomUUID().toString().take(8).uppercase(),
            timestampWita = getCurrentWitaDateTime(),
            pengguna = _currentUser.value.nama,
            peran = _currentUser.value.peran,
            tindakan = tindakan,
            detail = detail
        )
        _auditLogs.value = listOf(log) + _auditLogs.value
    }

    fun updateChurchSettings(church: String, jemaat: String, wilayah: String, year: String) {
        _churchName.value = church
        _jemaatName.value = jemaat
        _wilayahName.value = wilayah
        _electionYear.value = year
        addAuditLog("Pengaturan Gereja", "Memperbarui data jemaat: $jemaat, Wilayah $wilayah")
    }

    // --- Dynamic Column Progress Calculation ---
    fun calculateColumnProgress(kodeKolom: String): Int {
        val kolom = _kolomList.value.find { it.kodeKolom == kodeKolom } ?: return 0
        var progress = 0

        // 1. Lokasi & Panitia ready: +20%
        val hasLokasi = kolom.namaLokasi.isNotBlank()
        if (hasLokasi) progress += 20

        // 2. Kehadiran: +20% if some voters recorded
        val votersInKolom = _pemilihList.value.filter { it.kodeKolom == kodeKolom }
        val hadirCount = _kehadiranList.value.count { it.kodeKolom == kodeKolom && it.hadir }
        if (votersInKolom.isNotEmpty() && hadirCount > 0) {
            progress += 20
        }

        // 3. Diaken Results: +30% if submitted, +5% if verified
        val hasilDiaken = _hasilList.value.find { it.kodeKolom == kodeKolom && it.jenisJabatan == JenisJabatan.DIAKEN }
        if (hasilDiaken != null && (hasilDiaken.statusHasil == StatusHasil.TERSIMPAN || hasilDiaken.statusHasil == StatusHasil.MENUNGGU_VERIFIKASI || hasilDiaken.statusHasil == StatusHasil.TERVERIFIKASI)) {
            progress += 25
            if (hasilDiaken.statusHasil == StatusHasil.TERVERIFIKASI) progress += 5
        }

        // 4. Penatua Results: +30% if submitted, +5% if verified
        val hasilPenatua = _hasilList.value.find { it.kodeKolom == kodeKolom && it.jenisJabatan == JenisJabatan.PENATUA }
        if (hasilPenatua != null && (hasilPenatua.statusHasil == StatusHasil.TERSIMPAN || hasilPenatua.statusHasil == StatusHasil.MENUNGGU_VERIFIKASI || hasilPenatua.statusHasil == StatusHasil.TERVERIFIKASI)) {
            progress += 25
            if (hasilPenatua.statusHasil == StatusHasil.TERVERIFIKASI) progress += 5
        }

        return progress.coerceIn(0, 100)
    }

    // --- Attendance Operations ---
    fun setAttendance(kodePemilih: String, kodeKolom: String, hadir: Boolean) {
        val current = _kehadiranList.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.kodePemilih == kodePemilih && it.kodeKolom == kodeKolom }
        val timeNow = getCurrentWitaTime()
        val userNow = _currentUser.value.nama

        if (existingIndex >= 0) {
            current[existingIndex] = current[existingIndex].copy(
                hadir = hadir,
                waktuCatatWita = timeNow,
                petugas = userNow
            )
        } else {
            current.add(
                CatatanKehadiran(
                    id = "ATT-" + UUID.randomUUID().toString().take(8),
                    kodePemilih = kodePemilih,
                    kodeKolom = kodeKolom,
                    hadir = hadir,
                    waktuCatatWita = timeNow,
                    tanggalCatat = "17-10-2026",
                    petugas = userNow
                )
            )
        }
        _kehadiranList.value = current

        // Update Kolom status if was Belum Mulai
        val kList = _kolomList.value.toMutableList()
        val kIdx = kList.indexOfFirst { it.kodeKolom == kodeKolom }
        if (kIdx >= 0 && kList[kIdx].statusPemilihan == StatusPemilihan.BELUM_MULAI) {
            kList[kIdx] = kList[kIdx].copy(statusPemilihan = StatusPemilihan.SEDANG_BERLANGSUNG)
            _kolomList.value = kList
        }

        addAuditLog("Catat Kehadiran", "Pemilih $kodePemilih di $kodeKolom diset: ${if (hadir) "Hadir" else "Tidak Hadir"}")
    }

    fun setBulkAttendance(kodeKolom: String, hadir: Boolean) {
        val pemilihInKolom = _pemilihList.value.filter { it.kodeKolom == kodeKolom }
        val current = _kehadiranList.value.toMutableList()
        val timeNow = getCurrentWitaTime()
        val userNow = _currentUser.value.nama

        pemilihInKolom.forEach { p ->
            val idx = current.indexOfFirst { it.kodePemilih == p.kodePemilih && it.kodeKolom == kodeKolom }
            if (idx >= 0) {
                current[idx] = current[idx].copy(hadir = hadir, waktuCatatWita = timeNow, petugas = userNow)
            } else {
                current.add(
                    CatatanKehadiran(
                        id = "ATT-" + UUID.randomUUID().toString().take(8),
                        kodePemilih = p.kodePemilih,
                        kodeKolom = kodeKolom,
                        hadir = hadir,
                        waktuCatatWita = timeNow,
                        tanggalCatat = "17-10-2026",
                        petugas = userNow
                    )
                )
            }
        }
        _kehadiranList.value = current
        addAuditLog("Kehadiran Massal", "Menandai ${pemilihInKolom.size} pemilih di $kodeKolom sebagai ${if (hadir) "Hadir" else "Belum Hadir"}")
    }

    // --- Vote Results Operations ---
    fun saveVoteResult(
        kodeKolom: String,
        jenisJabatan: JenisJabatan,
        suaraCalon: Map<String, Int>,
        suaraSah: Int,
        suaraRusak: Int,
        suaraTidakDigunakan: Int,
        catatan: String,
        isFinal: Boolean,
        correctionReason: String = ""
    ): Pair<Boolean, String> {
        val votersInKolom = _pemilihList.value.count { it.kodeKolom == kodeKolom }
        val hadirInKolom = _kehadiranList.value.count { it.kodeKolom == kodeKolom && it.hadir }

        // Negative check
        if (suaraSah < 0 || suaraRusak < 0 || suaraTidakDigunakan < 0) {
            return Pair(false, "Jumlah suara tidak boleh bernilai negatif.")
        }
        for ((_, votes) in suaraCalon) {
            if (votes < 0) return Pair(false, "Suara calon tidak boleh negatif.")
        }

        val totalVotesForCandidates = suaraCalon.values.sum()
        if (totalVotesForCandidates != suaraSah) {
            return Pair(false, "Total perolehan calon ($totalVotesForCandidates) tidak sama dengan Jumlah Suara Sah ($suaraSah).")
        }

        val currentList = _hasilList.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.kodeKolom == kodeKolom && it.jenisJabatan == jenisJabatan }

        val newStatus = if (isFinal) StatusHasil.MENUNGGU_VERIFIKASI else StatusHasil.TERSIMPAN
        val correctionLogs = mutableListOf<String>()

        if (existingIndex >= 0) {
            val old = currentList[existingIndex]
            if (old.isFinal && !isFinal) {
                return Pair(false, "Hasil yang telah difinalisasi tidak dapat diubah kembali menjadi draft tanpa prosedur koreksi.")
            }
            if (old.isFinal && correctionReason.isBlank()) {
                return Pair(false, "Perubahan hasil yang sudah final wajib menyertakan alasan koreksi resmi.")
            }
            if (correctionReason.isNotBlank()) {
                correctionLogs.addAll(old.riwayatKoreksi)
                correctionLogs.add("[${getCurrentWitaDateTime()}] Koreksi oleh ${_currentUser.value.nama}: $correctionReason")
            }

            currentList[existingIndex] = old.copy(
                suaraCalon = suaraCalon,
                suaraSah = suaraSah,
                suaraRusak = suaraRusak,
                suratSuaraTidakDigunakan = suaraTidakDigunakan,
                catatan = catatan,
                petugas = _currentUser.value.nama,
                statusHasil = newStatus,
                isFinal = isFinal,
                jumlahPemilihTerdaftar = votersInKolom,
                jumlahPemilihHadir = hadirInKolom,
                riwayatKoreksi = correctionLogs
            )
        } else {
            currentList.add(
                HasilPerolehan(
                    id = "RES-" + UUID.randomUUID().toString().take(8),
                    kodeKolom = kodeKolom,
                    jenisJabatan = jenisJabatan,
                    tanggalPemilihan = "17-10-2026",
                    jumlahPemilihTerdaftar = votersInKolom,
                    jumlahPemilihHadir = hadirInKolom,
                    suaraCalon = suaraCalon,
                    suaraSah = suaraSah,
                    suaraRusak = suaraRusak,
                    suratSuaraTidakDigunakan = suaraTidakDigunakan,
                    catatan = catatan,
                    petugas = _currentUser.value.nama,
                    statusHasil = newStatus,
                    isFinal = isFinal
                )
            )
        }
        _hasilList.value = currentList

        addAuditLog(
            if (isFinal) "Finalisasi Hasil ${jenisJabatan.label}" else "Simpan Draft Hasil ${jenisJabatan.label}",
            "Kolom: $kodeKolom, Suara Sah: $suaraSah, Rusak: $suaraRusak" + if (correctionReason.isNotBlank()) " (Alasan: $correctionReason)" else ""
        )

        checkAndUpdateKolomOverallStatus(kodeKolom)
        return Pair(true, "Hasil pemilihan ${jenisJabatan.label} berhasil disimpan${if (isFinal) " dan siap diverifikasi" else " sebagai draft"}.")
    }

    fun verifyVoteResult(kodeKolom: String, jenisJabatan: JenisJabatan): Pair<Boolean, String> {
        if (_currentUser.value.peran != PeranPengguna.VERIFIKATOR && _currentUser.value.peran != PeranPengguna.SUPER_ADMIN) {
            return Pair(false, "Hanya Verifikator atau Super Admin yang berhak memverifikasi hasil.")
        }
        val currentList = _hasilList.value.toMutableList()
        val idx = currentList.indexOfFirst { it.kodeKolom == kodeKolom && it.jenisJabatan == jenisJabatan }
        if (idx < 0) return Pair(false, "Hasil belum dimasukkan.")

        currentList[idx] = currentList[idx].copy(
            statusHasil = StatusHasil.TERVERIFIKASI,
            verifiedBy = _currentUser.value.nama
        )
        _hasilList.value = currentList
        addAuditLog("Verifikasi Hasil", "Hasil ${jenisJabatan.label} $kodeKolom diverifikasi oleh ${_currentUser.value.nama}")
        checkAndUpdateKolomOverallStatus(kodeKolom)
        return Pair(true, "Hasil ${jenisJabatan.label} $kodeKolom berhasil diverifikasi dan disahkan.")
    }

    private fun checkAndUpdateKolomOverallStatus(kodeKolom: String) {
        val hasilD = _hasilList.value.find { it.kodeKolom == kodeKolom && it.jenisJabatan == JenisJabatan.DIAKEN }
        val hasilP = _hasilList.value.find { it.kodeKolom == kodeKolom && it.jenisJabatan == JenisJabatan.PENATUA }

        val isBothVerified = hasilD?.statusHasil == StatusHasil.TERVERIFIKASI && hasilP?.statusHasil == StatusHasil.TERVERIFIKASI
        val isBothWaiting = hasilD?.isFinal == true && hasilP?.isFinal == true

        val kList = _kolomList.value.toMutableList()
        val idx = kList.indexOfFirst { it.kodeKolom == kodeKolom }
        if (idx >= 0) {
            val newStatus = when {
                isBothVerified -> StatusPemilihan.SELESAI
                isBothWaiting -> StatusPemilihan.MENUNGGU_VERIFIKASI
                else -> StatusPemilihan.SEDANG_BERLANGSUNG
            }
            kList[idx] = kList[idx].copy(statusPemilihan = newStatus)
            _kolomList.value = kList
        }
    }

    // --- Master Data Operations with Integrity Guards ---
    fun addKolom(kolom: Kolom): Boolean {
        if (_kolomList.value.any { it.kodeKolom == kolom.kodeKolom }) return false
        _kolomList.value = _kolomList.value + kolom
        addAuditLog("Tambah Kolom", "Menambahkan kolom ${kolom.kodeKolom} (${kolom.namaKolom})")
        return true
    }

    fun updateKolom(kolom: Kolom) {
        _kolomList.value = _kolomList.value.map { if (it.kodeKolom == kolom.kodeKolom) kolom else it }
        addAuditLog("Ubah Kolom", "Memperbarui kolom ${kolom.kodeKolom}")
    }

    fun deleteKolom(kodeKolom: String): Pair<Boolean, String> {
        val hasAttendance = _kehadiranList.value.any { it.kodeKolom == kodeKolom }
        val hasResults = _hasilList.value.any { it.kodeKolom == kodeKolom }
        if (hasAttendance || hasResults) {
            return Pair(false, "Kolom $kodeKolom tidak dapat dihapus karena memiliki riwayat daftar hadir atau hasil pemilihan. Gunakan fitur penonaktifan.")
        }
        _kolomList.value = _kolomList.value.filter { it.kodeKolom != kodeKolom }
        addAuditLog("Hapus Kolom", "Menghapus kolom $kodeKolom")
        return Pair(true, "Kolom $kodeKolom berhasil dihapus.")
    }

    fun addPemilih(pemilih: Pemilih): Boolean {
        _pemilihList.value = _pemilihList.value + pemilih
        addAuditLog("Tambah Pemilih", "Menambahkan pemilih ${pemilih.namaPemilih} di ${pemilih.kodeKolom}")
        return true
    }

    fun deletePemilih(kodePemilih: String): Pair<Boolean, String> {
        val hasAttendance = _kehadiranList.value.any { it.kodePemilih == kodePemilih && it.hadir }
        if (hasAttendance) {
            return Pair(false, "Pemilih $kodePemilih tidak dapat dihapus karena sudah tercatat dalam daftar hadir. Nonaktifkan status pemilih.")
        }
        _pemilihList.value = _pemilihList.value.filter { it.kodePemilih != kodePemilih }
        addAuditLog("Hapus Pemilih", "Menghapus pemilih $kodePemilih")
        return Pair(true, "Pemilih berhasil dihapus.")
    }

    fun addCalon(calon: Calon): Boolean {
        _calonList.value = _calonList.value + calon
        addAuditLog("Tambah Calon", "Menambahkan calon ${calon.namaCalon} (${calon.jenisJabatan.label})")
        return true
    }

    fun deleteCalon(kodeCalon: String): Pair<Boolean, String> {
        val hasVotes = _hasilList.value.any { (it.suaraCalon[kodeCalon] ?: 0) > 0 }
        if (hasVotes) {
            return Pair(false, "Calon $kodeCalon sudah memiliki perolehan suara dalam hasil pemilihan dan tidak dapat dihapus.")
        }
        _calonList.value = _calonList.value.filter { it.kodeCalon != kodeCalon }
        addAuditLog("Hapus Calon", "Menghapus calon $kodeCalon")
        return Pair(true, "Calon berhasil dihapus.")
    }

    // --- Import Execution ---
    fun executeImport(type: ExcelCsvManager.ImportType, rows: List<Map<String, String>>): Int {
        var count = 0
        when (type) {
            ExcelCsvManager.ImportType.MASTER_KOLOM -> {
                val current = _kolomList.value.toMutableList()
                rows.forEach { map ->
                    val code = map["kode_kolom"] ?: ""
                    val name = map["nama_kolom"] ?: ""
                    if (code.isNotBlank() && name.isNotBlank()) {
                        val existingIdx = current.indexOfFirst { it.kodeKolom == code }
                        val status = when (map["status_pemilihan"]) {
                            "Sedang Berlangsung" -> StatusPemilihan.SEDANG_BERLANGSUNG
                            "Menunggu Verifikasi" -> StatusPemilihan.MENUNGGU_VERIFIKASI
                            "Selesai" -> StatusPemilihan.SELESAI
                            "Persiapan" -> StatusPemilihan.PERSIAPAN
                            else -> StatusPemilihan.BELUM_MULAI
                        }
                        val item = Kolom(
                            kodeKolom = code,
                            namaKolom = name,
                            namaLokasi = map["nama_lokasi"] ?: "",
                            tanggalPemilihan = map["tanggal_pemilihan"]?.ifBlank { "17-10-2026" } ?: "17-10-2026",
                            jenisPemilihan = map["jenis_pemilihan"]?.ifBlank { "Pemilihan Pelsus Kolom GMIM" } ?: "Pemilihan Pelsus Kolom GMIM",
                            statusPemilihan = status,
                            keterangan = map["keterangan"] ?: ""
                        )
                        if (existingIdx >= 0) {
                            current[existingIdx] = item
                        } else {
                            current.add(item)
                        }
                        count++
                    }
                }
                _kolomList.value = current
            }
            ExcelCsvManager.ImportType.DAFTAR_PEMILIH -> {
                val current = _pemilihList.value.toMutableList()
                rows.forEachIndexed { i, map ->
                    val code = (map["kode_pemilih"] ?: "").ifBlank { "PML-${System.currentTimeMillis() % 100000}-$i" }
                    val name = map["nama_pemilih"] ?: ""
                    val col = map["kode_kolom"] ?: ""
                    if (name.isNotBlank() && col.isNotBlank()) {
                        val item = Pemilih(
                            kodePemilih = code,
                            namaPemilih = name,
                            kodeKolom = col,
                            alamat = map["alamat"] ?: "",
                            nomorAnggota = map["nomor_anggota_opsional"] ?: "",
                            statusAktif = map["status_aktif"]?.lowercase() != "false"
                        )
                        current.add(item)
                        count++
                    }
                }
                _pemilihList.value = current
            }
            ExcelCsvManager.ImportType.MASTER_CALON -> {
                val current = _calonList.value.toMutableList()
                rows.forEach { map ->
                    val code = map["kode_calon"] ?: ""
                    val name = map["nama_calon"] ?: ""
                    val jabStr = map["jenis_jabatan"]?.uppercase() ?: ""
                    val jab = if (jabStr == "DIAKEN") JenisJabatan.DIAKEN else JenisJabatan.PENATUA
                    val col = map["kode_kolom"] ?: ""
                    val no = map["nomor_urut"]?.toIntOrNull() ?: 1
                    if (code.isNotBlank() && name.isNotBlank()) {
                        current.add(
                            Calon(
                                kodeCalon = code,
                                namaCalon = name,
                                jenisJabatan = jab,
                                kodeKolom = col,
                                nomorUrut = no,
                                statusCalon = map["status_calon"]?.ifBlank { "Aktif" } ?: "Aktif"
                            )
                        )
                        count++
                    }
                }
                _calonList.value = current
            }
            ExcelCsvManager.ImportType.MASTER_PANITIA -> {
                val current = _panitiaList.value.toMutableList()
                rows.forEach { map ->
                    val code = map["kode_panitia"] ?: ""
                    val name = map["nama_panitia"] ?: ""
                    if (code.isNotBlank() && name.isNotBlank()) {
                        current.add(
                            Panitia(
                                kodePanitia = code,
                                namaPanitia = name,
                                jabatanPanitia = map["jabatan_panitia"] ?: "",
                                kodeKolom = map["kode_kolom"] ?: "SEMUA",
                                nomorTelepon = map["nomor_telepon_opsional"] ?: "",
                                statusAktif = true
                            )
                        )
                        count++
                    }
                }
                _panitiaList.value = current
            }
            ExcelCsvManager.ImportType.MASTER_UNSUR -> {
                val current = _unsurList.value.toMutableList()
                rows.forEach { map ->
                    val code = map["kode_unsur"] ?: ""
                    val name = map["nama_unsur"] ?: ""
                    if (code.isNotBlank() && name.isNotBlank()) {
                        current.add(
                            Unsur(
                                kodeUnsur = code,
                                namaUnsur = name,
                                jenisPemilihan = map["jenis_pemilihan"] ?: "Pemilihan Kolom",
                                keterangan = map["keterangan"] ?: ""
                            )
                        )
                        count++
                    }
                }
                _unsurList.value = current
            }
        }
        addAuditLog("Impor Data Excel", "Berhasil mengimpor $count data pada sheet ${type.name}")
        return count
    }

    // --- Demo Data Initializer ---
    fun loadInitialData() {
        val koloms = mutableListOf<Kolom>()
        val pemilihs = mutableListOf<Pemilih>()
        val calons = mutableListOf<Calon>()
        val panitias = mutableListOf<Panitia>()
        val lokasis = mutableListOf<Lokasi>()
        val unsurs = mutableListOf<Unsur>()
        val kehadirans = mutableListOf<CatatanKehadiran>()
        val hasils = mutableListOf<HasilPerolehan>()

        // 12 Realistic GMIM Kolom
        val namaAlkitabiah = listOf(
            "Getsemani", "Sion", "Betania", "Eben Haezer", "Moria",
            "Karmel", "Nazaret", "Hermon", "Betel", "Tiberias",
            "Golgotta", "Galilea"
        )

        for (i in 1..12) {
            val code = String.format("KOLOM-%02d", i)
            val name = "Kolom $i - ${namaAlkitabiah[(i - 1) % namaAlkitabiah.size]}"
            val location = "Rumah Kel. ${when (i) {
                1 -> "Tumiwa - Kambey"
                2 -> "Palar - Sondakh"
                3 -> "Rondonuwu - Wenas"
                4 -> "Manopo - Lengkey"
                5 -> "Pangalila - Wowor"
                6 -> "Karundeng - Sumual"
                7 -> "Warouw - Supit"
                8 -> "Mandagi - Poluan"
                9 -> "Rumengan - Paat"
                10 -> "Lumentut - Kalalo"
                11 -> "Rantung - Pangemanan"
                else -> "Rumagit - Sumendap"
            }}"

            val status = when (i) {
                1 -> StatusPemilihan.SELESAI
                2 -> StatusPemilihan.MENUNGGU_VERIFIKASI
                3, 4, 5 -> StatusPemilihan.SEDANG_BERLANGSUNG
                6, 7 -> StatusPemilihan.PERSIAPAN
                else -> StatusPemilihan.BELUM_MULAI
            }

            koloms.add(
                Kolom(
                    kodeKolom = code,
                    namaKolom = name,
                    namaLokasi = location,
                    tanggalPemilihan = "17-10-2026",
                    jenisPemilihan = "Pemilihan Pelsus Kolom GMIM",
                    statusPemilihan = status,
                    keterangan = "Pemilihan Pelsus Periode Pelayanan 2026 - 2030"
                )
            )

            lokasis.add(
                Lokasi(
                    kodeLokasi = "LOK-$i",
                    namaLokasi = location,
                    alamat = "Lingkungan $i, GMIM Sentrum",
                    penanggungJawab = "Penatua Kolom $i"
                )
            )

            // Add Voters for each column (8 to 15 per column)
            val voterNames = listOf(
                "Frangky Pangalila", "Meike Sondakh", "Johanis Palar", "Deisy Tumiwa",
                "Stenly Rantung", "Merry Lumentut", "Ferry Mandagi", "Grace Wowor",
                "Rudi Karundeng", "Vanda Sumual", "Johan Poluan", "Martha Lengkey"
            )

            voterNames.forEachIndexed { vIdx, vName ->
                val pCode = "PML-${code}-${vIdx + 1}"
                pemilihs.add(
                    Pemilih(
                        kodePemilih = pCode,
                        namaPemilih = "$vName ($code)",
                        kodeKolom = code,
                        alamat = "Lingkungan $i, Lorong Betel RT 0${(vIdx % 3) + 1}",
                        nomorAnggota = "GMIM-2026-${i * 100 + vIdx}",
                        statusAktif = true
                    )
                )

                // Populate some attendance for columns 1 to 5
                if (i in 1..5 && vIdx < 8) {
                    kehadirans.add(
                        CatatanKehadiran(
                            id = "ATT-$pCode",
                            kodePemilih = pCode,
                            kodeKolom = code,
                            hadir = true,
                            waktuCatatWita = "09:${10 + vIdx * 3}",
                            tanggalCatat = "17-10-2026",
                            petugas = "Dkn. Ruth Sumendap"
                        )
                    )
                }
            }

            // Add Candidates (Diaken & Penatua) for each column
            val cDiaken1 = Calon("CLN-D-${code}-1", "Johan Poluan", JenisJabatan.DIAKEN, code, 1, "Aktif")
            val cDiaken2 = Calon("CLN-D-${code}-2", "Martha Lengkey", JenisJabatan.DIAKEN, code, 2, "Aktif")
            val cPenatua1 = Calon("CLN-P-${code}-1", "Daud Manopo", JenisJabatan.PENATUA, code, 1, "Aktif")
            val cPenatua2 = Calon("CLN-P-${code}-2", "Ester Kalalo", JenisJabatan.PENATUA, code, 2, "Aktif")

            calons.addAll(listOf(cDiaken1, cDiaken2, cPenatua1, cPenatua2))

            // Pre-seed results for Kolom 1 (Selesai & Terverifikasi)
            if (i == 1) {
                hasils.add(
                    HasilPerolehan(
                        id = "RES-D-KOLOM-01",
                        kodeKolom = "KOLOM-01",
                        jenisJabatan = JenisJabatan.DIAKEN,
                        tanggalPemilihan = "17-10-2026",
                        jumlahPemilihTerdaftar = 12,
                        jumlahPemilihHadir = 8,
                        suaraCalon = mapOf(cDiaken1.kodeCalon to 5, cDiaken2.kodeCalon to 3),
                        suaraSah = 8,
                        suaraRusak = 0,
                        suratSuaraTidakDigunakan = 4,
                        catatan = "Pemilihan berjalan damai, tertib, dan demokratis.",
                        petugas = "Dkn. Ruth Sumendap",
                        statusHasil = StatusHasil.TERVERIFIKASI,
                        isFinal = true,
                        verifiedBy = "Pdt. Stefanus Wowor, M.Th"
                    )
                )
                hasils.add(
                    HasilPerolehan(
                        id = "RES-P-KOLOM-01",
                        kodeKolom = "KOLOM-01",
                        jenisJabatan = JenisJabatan.PENATUA,
                        tanggalPemilihan = "17-10-2026",
                        jumlahPemilihTerdaftar = 12,
                        jumlahPemilihHadir = 8,
                        suaraCalon = mapOf(cPenatua1.kodeCalon to 6, cPenatua2.kodeCalon to 2),
                        suaraSah = 8,
                        suaraRusak = 0,
                        suratSuaraTidakDigunakan = 4,
                        catatan = "Sah berdasarkan suara terbanyak.",
                        petugas = "Dkn. Ruth Sumendap",
                        statusHasil = StatusHasil.TERVERIFIKASI,
                        isFinal = true,
                        verifiedBy = "Pdt. Stefanus Wowor, M.Th"
                    )
                )
            }

            // Pre-seed results for Kolom 2 (Menunggu verifikasi)
            if (i == 2) {
                hasils.add(
                    HasilPerolehan(
                        id = "RES-D-KOLOM-02",
                        kodeKolom = "KOLOM-02",
                        jenisJabatan = JenisJabatan.DIAKEN,
                        tanggalPemilihan = "17-10-2026",
                        jumlahPemilihTerdaftar = 12,
                        jumlahPemilihHadir = 8,
                        suaraCalon = mapOf(cDiaken1.kodeCalon to 4, cDiaken2.kodeCalon to 3),
                        suaraSah = 7,
                        suaraRusak = 1,
                        suratSuaraTidakDigunakan = 4,
                        catatan = "1 surat suara dinyatakan rusak karena dicoblos ganda.",
                        petugas = "Bpk. Hendra Rondonuwu",
                        statusHasil = StatusHasil.MENUNGGU_VERIFIKASI,
                        isFinal = true
                    )
                )
                hasils.add(
                    HasilPerolehan(
                        id = "RES-P-KOLOM-02",
                        kodeKolom = "KOLOM-02",
                        jenisJabatan = JenisJabatan.PENATUA,
                        tanggalPemilihan = "17-10-2026",
                        jumlahPemilihTerdaftar = 12,
                        jumlahPemilihHadir = 8,
                        suaraCalon = mapOf(cPenatua1.kodeCalon to 5, cPenatua2.kodeCalon to 3),
                        suaraSah = 8,
                        suaraRusak = 0,
                        suratSuaraTidakDigunakan = 4,
                        catatan = "Menunggu verifikasi sidang majelis.",
                        petugas = "Bpk. Hendra Rondonuwu",
                        statusHasil = StatusHasil.MENUNGGU_VERIFIKASI,
                        isFinal = true
                    )
                )
            }
        }

        // Panitia
        panitias.add(Panitia("PNT-01", "Pnt. James Rumagit", "Ketua Komisi Pemilihan", "SEMUA", "081244556677", true))
        panitias.add(Panitia("PNT-02", "Dkn. Ruth Sumendap", "Sekretaris Pemilihan", "SEMUA", "081388990011", true))
        panitias.add(Panitia("PNT-03", "Bpk. Hendra Rondonuwu", "Koordinator Kolom 1", "KOLOM-01", "085211223344", true))
        panitias.add(Panitia("PNT-04", "Ibu Shirley Wenas", "Petugas Kehadiran Kolom 1", "KOLOM-01", "085377889900", true))
        panitias.add(Panitia("PNT-05", "Bpk. Royke Pangemanan", "Koordinator Kolom 2", "KOLOM-02", "082199887766", true))

        // Unsurs
        unsurs.add(Unsur("UNS-01", "Pelayan Khusus Kolom", "Pemilihan Kolom", "Diaken dan Penatua Kolom 1 - 12"))
        unsurs.add(Unsur("UNS-02", "BIPRA Pria/Kaum Bapa (P/KB)", "Pemilihan Kategorial", "BIPRA P/KB Jemaat"))
        unsurs.add(Unsur("UNS-03", "BIPRA Wanita/Kaum Ibu (W/KI)", "Pemilihan Kategorial", "BIPRA W/KI Jemaat"))
        unsurs.add(Unsur("UNS-04", "BIPRA Pemuda", "Pemilihan Kategorial", "BIPRA Pemuda Jemaat"))
        unsurs.add(Unsur("UNS-05", "BIPRA Remaja", "Pemilihan Kategorial", "BIPRA Remaja Jemaat"))
        unsurs.add(Unsur("UNS-06", "BIPRA Anak Sekolah Minggu (ASM)", "Pemilihan Kategorial", "BIPRA ASM Jemaat"))
        unsurs.add(Unsur("UNS-07", "Badan Pekerja Majelis Jemaat (BPMJ)", "Pemilihan BPMJ", "BPMJ Periode 2026 - 2030"))
        unsurs.add(Unsur("UNS-08", "Kelompok Fungsional Lansia", "Pemilihan Lansia", "Pengurus Fungsional Lansia Jemaat"))

        _kolomList.value = koloms
        _pemilihList.value = pemilihs
        _calonList.value = calons
        _panitiaList.value = panitias
        _lokasiList.value = lokasis
        _unsurList.value = unsurs
        _kehadiranList.value = kehadirans
        _hasilList.value = hasils

        addAuditLog("Sistem", "Inisialisasi data pemilihan GMIM berhasil dimuat (${koloms.size} kolom, ${pemilihs.size} pemilih terdaftar)")
    }
}
