package com.example.data.model

enum class StatusPemilihan(val label: String) {
    BELUM_MULAI("Belum Mulai"),
    PERSIAPAN("Persiapan"),
    SEDANG_BERLANGSUNG("Sedang Berlangsung"),
    MENUNGGU_VERIFIKASI("Menunggu Verifikasi"),
    SELESAI("Selesai")
}

enum class StatusHasil(val label: String) {
    BELUM_ADA("Belum Ada Hasil"),
    MENUNGGU_SELESAI("Menunggu Pemilihan Selesai"),
    TERSIMPAN("Hasil Tersimpan (Draft)"),
    MENUNGGU_VERIFIKASI("Menunggu Verifikasi"),
    TERVERIFIKASI("Terverifikasi & Sah")
}

enum class JenisJabatan(val label: String) {
    DIAKEN("Diaken"),
    PENATUA("Penatua")
}

enum class PeranPengguna(val label: String) {
    SUPER_ADMIN("Super Admin"),
    ADMIN_PEMILIHAN("Admin Pemilihan"),
    PANITIA_KOLOM("Panitia Kolom"),
    VERIFIKATOR("Verifikator"),
    PENGAWAS("Pimpinan / Pengawas")
}

data class Kolom(
    val kodeKolom: String,
    val namaKolom: String,
    val namaLokasi: String,
    val tanggalPemilihan: String, // DD-MM-YYYY
    val jenisPemilihan: String = "Pemilihan Pelsus Kolom GMIM",
    val statusPemilihan: StatusPemilihan = StatusPemilihan.BELUM_MULAI,
    val keterangan: String = ""
)

data class Pemilih(
    val kodePemilih: String,
    val namaPemilih: String,
    val kodeKolom: String,
    val alamat: String = "",
    val nomorAnggota: String = "",
    val statusAktif: Boolean = true
)

data class Calon(
    val kodeCalon: String,
    val namaCalon: String,
    val jenisJabatan: JenisJabatan,
    val kodeKolom: String, // Can be shared or specific
    val nomorUrut: Int,
    val statusCalon: String = "Aktif",
    val cakupan: String = "Khusus Kolom" // "Khusus Kolom" or "Bersama"
)

data class Panitia(
    val kodePanitia: String,
    val namaPanitia: String,
    val jabatanPanitia: String,
    val kodeKolom: String,
    val nomorTelepon: String = "",
    val statusAktif: Boolean = true
)

data class Lokasi(
    val kodeLokasi: String,
    val namaLokasi: String,
    val alamat: String,
    val penanggungJawab: String = ""
)

data class Unsur(
    val kodeUnsur: String,
    val namaUnsur: String,
    val jenisPemilihan: String,
    val keterangan: String = ""
)

data class CatatanKehadiran(
    val id: String,
    val kodePemilih: String,
    val kodeKolom: String,
    val hadir: Boolean,
    val waktuCatatWita: String = "", // HH:mm WITA
    val tanggalCatat: String = "", // DD-MM-YYYY
    val petugas: String = ""
)

data class HasilPerolehan(
    val id: String,
    val kodeKolom: String,
    val jenisJabatan: JenisJabatan,
    val tanggalPemilihan: String,
    val jumlahPemilihTerdaftar: Int,
    val jumlahPemilihHadir: Int,
    val suaraCalon: Map<String, Int> = emptyMap(), // kodeCalon -> count
    val suaraSah: Int = 0,
    val suaraRusak: Int = 0,
    val suratSuaraTidakDigunakan: Int = 0,
    val catatan: String = "",
    val petugas: String = "",
    val statusHasil: StatusHasil = StatusHasil.BELUM_ADA,
    val isFinal: Boolean = false,
    val verifiedBy: String = "",
    val riwayatKoreksi: List<String> = emptyList()
)

data class AuditLog(
    val id: String,
    val timestampWita: String,
    val pengguna: String,
    val peran: PeranPengguna,
    val tindakan: String,
    val detail: String
)

data class AppUser(
    val id: String,
    val username: String,
    val nama: String,
    val peran: PeranPengguna,
    val assignedKolom: String? = null // null means all columns
)

data class ImportPreviewRow(
    val rowNumber: Int,
    val data: Map<String, String>,
    val isValid: Boolean,
    val isDuplicate: Boolean,
    val errors: List<String>
)

data class ImportSummary(
    val sheetType: String,
    val totalRows: Int,
    val validRows: Int,
    val invalidRows: Int,
    val duplicateRows: Int,
    val errorsList: List<String>,
    val previewRows: List<ImportPreviewRow>
)
