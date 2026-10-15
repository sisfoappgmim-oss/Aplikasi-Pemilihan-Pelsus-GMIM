package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.excel.ExcelCsvManager
import com.example.data.model.*
import com.example.data.repository.ElectionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppScreen(val title: String, val group: String = "Utama") {
    BERANDA("Beranda", "Utama"),
    PROGRES_PEMILIHAN("Progres Pemilihan", "Utama"),
    PANITIA("Data Panitia", "Master"),
    MASTER_DATA("Master Data", "Master"),
    LOKASI_PEMILIHAN("Lokasi Pemilihan", "Master"),
    IMPOR_EXCEL("Impor Data Excel", "Master"),
    STATUS_PILIH("Status Pemilihan", "Pelsus"),
    DAFTAR_HADIR("Daftar Hadir", "Pelsus"),
    INPUT_HASIL("Input Hasil Suara", "Pelsus"),
    HASIL_PEMILIHAN("Hasil Pemilihan", "Pelsus"),
    PEMILIHAN_BIPRA("Pemilihan BIPRA", "Kategorial"),
    PEMILIHAN_BPMJ("Pemilihan BPMJ", "Sinodal"),
    PEMILIHAN_LANSIA("Pemilihan Lansia", "Kategorial"),
    LAPORAN_BAP("Laporan & BAP", "Laporan"),
    PENGATURAN("Pengaturan Sistem", "Pengaturan")
}

data class UiSnackbar(
    val message: String,
    val isError: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

class ElectionViewModel(
    private val repository: ElectionRepository = ElectionRepository.getInstance()
) : ViewModel() {

    private val _currentScreen = MutableStateFlow(AppScreen.BERANDA)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _snackbar = MutableStateFlow<UiSnackbar?>(null)
    val snackbar: StateFlow<UiSnackbar?> = _snackbar.asStateFlow()

    private val _selectedKolomCode = MutableStateFlow<String?>("KOLOM-01")
    val selectedKolomCode: StateFlow<String?> = _selectedKolomCode.asStateFlow()

    private val _selectedJabatan = MutableStateFlow(JenisJabatan.DIAKEN)
    val selectedJabatan: StateFlow<JenisJabatan> = _selectedJabatan.asStateFlow()

    // Import State
    private val _currentImportType = MutableStateFlow(ExcelCsvManager.ImportType.MASTER_KOLOM)
    val currentImportType: StateFlow<ExcelCsvManager.ImportType> = _currentImportType.asStateFlow()

    private val _importSummary = MutableStateFlow<ImportSummary?>(null)
    val importSummary: StateFlow<ImportSummary?> = _importSummary.asStateFlow()

    // Repo flows
    val currentUser = repository.currentUser
    val churchName = repository.churchName
    val jemaatName = repository.jemaatName
    val wilayahName = repository.wilayahName
    val electionYear = repository.electionYear
    val kolomList = repository.kolomList
    val pemilihList = repository.pemilihList
    val calonList = repository.calonList
    val panitiaList = repository.panitiaList
    val lokasiList = repository.lokasiList
    val unsurList = repository.unsurList
    val kehadiranList = repository.kehadiranList
    val hasilList = repository.hasilList
    val auditLogs = repository.auditLogs

    fun navigateTo(screen: AppScreen, kolomCode: String? = null) {
        if (kolomCode != null) {
            _selectedKolomCode.value = kolomCode
        }
        _currentScreen.value = screen
    }

    fun setSelectedKolom(code: String) {
        _selectedKolomCode.value = code
    }

    fun setSelectedJabatan(jabatan: JenisJabatan) {
        _selectedJabatan.value = jabatan
    }

    fun showMessage(msg: String, isError: Boolean = false) {
        _snackbar.value = UiSnackbar(msg, isError)
    }

    fun clearSnackbar() {
        _snackbar.value = null
    }

    fun switchRole(role: PeranPengguna, kolom: String? = null) {
        repository.switchUserRole(role, kolom)
        showMessage("Beralih ke peran: ${role.label}")
    }

    fun calculateProgress(kodeKolom: String): Int {
        return repository.calculateColumnProgress(kodeKolom)
    }

    fun toggleAttendance(kodePemilih: String, kodeKolom: String, newHadir: Boolean) {
        repository.setAttendance(kodePemilih, kodeKolom, newHadir)
        showMessage(if (newHadir) "Kehadiran tercatat: HADIR" else "Status kehadiran dibatalkan")
    }

    fun bulkAttendance(kodeKolom: String, hadir: Boolean) {
        repository.setBulkAttendance(kodeKolom, hadir)
        showMessage("Seluruh pemilih di $kodeKolom diset: ${if (hadir) "Hadir" else "Belum Hadir"}")
    }

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
    ) {
        val (success, message) = repository.saveVoteResult(
            kodeKolom,
            jenisJabatan,
            suaraCalon,
            suaraSah,
            suaraRusak,
            suaraTidakDigunakan,
            catatan,
            isFinal,
            correctionReason
        )
        showMessage(message, !success)
    }

    fun verifyVoteResult(kodeKolom: String, jenisJabatan: JenisJabatan) {
        val (success, message) = repository.verifyVoteResult(kodeKolom, jenisJabatan)
        showMessage(message, !success)
    }

    fun deleteKolom(kodeKolom: String) {
        val (success, msg) = repository.deleteKolom(kodeKolom)
        showMessage(msg, !success)
    }

    fun deletePemilih(kodePemilih: String) {
        val (success, msg) = repository.deletePemilih(kodePemilih)
        showMessage(msg, !success)
    }

    fun deleteCalon(kodeCalon: String) {
        val (success, msg) = repository.deleteCalon(kodeCalon)
        showMessage(msg, !success)
    }

    fun addKolom(kolom: Kolom) {
        val success = repository.addKolom(kolom)
        if (success) {
            showMessage("Kolom ${kolom.kodeKolom} berhasil ditambahkan")
        } else {
            showMessage("Kode kolom ${kolom.kodeKolom} sudah terdaftar", true)
        }
    }

    fun addPemilih(pemilih: Pemilih) {
        repository.addPemilih(pemilih)
        showMessage("Pemilih ${pemilih.namaPemilih} berhasil didaftarkan")
    }

    fun addCalon(calon: Calon) {
        repository.addCalon(calon)
        showMessage("Calon ${calon.namaCalon} berhasil didaftarkan")
    }

    fun updateChurchSettings(church: String, jemaat: String, wilayah: String, year: String) {
        repository.updateChurchSettings(church, jemaat, wilayah, year)
        showMessage("Informasi gereja berhasil disimpan")
    }

    fun resetToDemoData() {
        repository.loadInitialData()
        showMessage("Data demonstrasi GMIM berhasil dimuat ulang")
    }

    // Import Handling
    fun setImportType(type: ExcelCsvManager.ImportType) {
        _currentImportType.value = type
        _importSummary.value = null
    }

    fun previewImportText(rawText: String) {
        val type = _currentImportType.value
        val existingIds = when (type) {
            ExcelCsvManager.ImportType.MASTER_KOLOM -> repository.kolomList.value.map { it.kodeKolom }.toSet()
            ExcelCsvManager.ImportType.DAFTAR_PEMILIH -> repository.pemilihList.value.map { it.kodePemilih }.toSet()
            ExcelCsvManager.ImportType.MASTER_CALON -> repository.calonList.value.map { it.kodeCalon }.toSet()
            ExcelCsvManager.ImportType.MASTER_PANITIA -> repository.panitiaList.value.map { it.kodePanitia }.toSet()
            ExcelCsvManager.ImportType.MASTER_UNSUR -> repository.unsurList.value.map { it.kodeUnsur }.toSet()
        }

        val summary = ExcelCsvManager.analyzeImport(rawText, type, existingIds)
        _importSummary.value = summary
        showMessage("Preview siap: ${summary.validRows} valid, ${summary.invalidRows} tidak valid, ${summary.duplicateRows} duplikat")
    }

    fun loadSampleForImport() {
        val type = _currentImportType.value
        val sample = ExcelCsvManager.getSampleData(type)
        previewImportText(sample)
    }

    fun confirmImport() {
        val summary = _importSummary.value ?: return
        val validRows = summary.previewRows.filter { it.isValid && !it.isDuplicate }.map { it.data }
        if (validRows.isEmpty()) {
            showMessage("Tidak ada baris valid untuk diimpor", true)
            return
        }
        val count = repository.executeImport(_currentImportType.value, validRows)
        _importSummary.value = null
        showMessage("Sukses mengimpor $count data ke database lokal!")
    }

    fun cancelImport() {
        _importSummary.value = null
        showMessage("Impor dibatalkan")
    }
}
