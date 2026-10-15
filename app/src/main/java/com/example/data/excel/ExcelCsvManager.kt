package com.example.data.excel

import com.example.data.model.*
import java.util.UUID

object ExcelCsvManager {

    enum class ImportType(val title: String, val headers: List<String>) {
        MASTER_KOLOM(
            "MASTER_KOLOM",
            listOf("kode_kolom", "nama_kolom", "nama_lokasi", "tanggal_pemilihan", "jenis_pemilihan", "status_pemilihan", "keterangan")
        ),
        DAFTAR_PEMILIH(
            "DAFTAR_PEMILIH",
            listOf("nomor", "kode_pemilih", "nama_pemilih", "kode_kolom", "alamat", "nomor_anggota_opsional", "status_aktif")
        ),
        MASTER_CALON(
            "MASTER_CALON",
            listOf("kode_calon", "nama_calon", "jenis_jabatan", "kode_kolom", "nomor_urut", "status_calon")
        ),
        MASTER_PANITIA(
            "MASTER_PANITIA",
            listOf("kode_panitia", "nama_panitia", "jabatan_panitia", "kode_kolom", "nomor_telepon_opsional", "status_aktif")
        ),
        MASTER_UNSUR(
            "MASTER_UNSUR",
            listOf("kode_unsur", "nama_unsur", "jenis_pemilihan", "keterangan")
        )
    }

    fun normalizeHeader(raw: String): String {
        return raw.trim()
            .lowercase()
            .replace(" ", "_")
            .replace("-", "_")
            .replace(".", "")
            .replace("(", "")
            .replace(")", "")
    }

    fun parseDelimitedText(content: String): List<List<String>> {
        val lines = content.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return emptyList()

        // Detect delimiter: comma, semicolon, tab, or pipe
        val firstLine = lines.first()
        val delimiter = when {
            firstLine.contains(";") -> ';'
            firstLine.contains("\t") -> '\t'
            firstLine.contains("|") -> '|'
            else -> ','
        }

        return lines.map { line ->
            parseLine(line, delimiter)
        }
    }

    private fun parseLine(line: String, delimiter: Char): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false

        for (ch in line) {
            when {
                ch == '\"' -> inQuotes = !inQuotes
                ch == delimiter && !inQuotes -> {
                    result.add(current.toString().trim())
                    current.clear()
                }
                else -> current.append(ch)
            }
        }
        result.add(current.toString().trim())
        return result
    }

    fun analyzeImport(
        rawContent: String,
        importType: ImportType,
        existingIds: Set<String>
    ): ImportSummary {
        val rows = parseDelimitedText(rawContent)
        if (rows.isEmpty()) {
            return ImportSummary(
                sheetType = importType.name,
                totalRows = 0,
                validRows = 0,
                invalidRows = 0,
                duplicateRows = 0,
                errorsList = listOf("File atau teks kosong"),
                previewRows = emptyList()
            )
        }

        val headerRow = rows.first()
        val normalizedHeaders = headerRow.map { normalizeHeader(it) }

        // Map expected headers to actual indices
        val headerMap = mutableMapOf<String, Int>()
        for (expected in importType.headers) {
            val normExpected = normalizeHeader(expected)
            val index = normalizedHeaders.indexOfFirst {
                it == normExpected || it.contains(normExpected) || normExpected.contains(it)
            }
            if (index >= 0) {
                headerMap[expected] = index
            }
        }

        val dataRows = rows.drop(1)
        val previewRows = mutableListOf<ImportPreviewRow>()
        val seenCodesInBatch = mutableSetOf<String>()
        val globalErrors = mutableListOf<String>()

        var validCount = 0
        var invalidCount = 0
        var duplicateCount = 0

        dataRows.forEachIndexed { idx, rowCells ->
            val rowNum = idx + 2 // 1-indexed including header
            val rowMap = mutableMapOf<String, String>()
            val rowErrors = mutableListOf<String>()

            importType.headers.forEach { headerKey ->
                val colIdx = headerMap[headerKey]
                val value = if (colIdx != null && colIdx < rowCells.size) rowCells[colIdx] else ""
                rowMap[headerKey] = value
            }

            // Validation rules based on import type
            var isDuplicate = false
            var isValid = true

            when (importType) {
                ImportType.MASTER_KOLOM -> {
                    val code = rowMap["kode_kolom"] ?: ""
                    val name = rowMap["nama_kolom"] ?: ""
                    if (code.isBlank()) rowErrors.add("kode_kolom wajib diisi")
                    if (name.isBlank()) rowErrors.add("nama_kolom wajib diisi")
                    if (code.isNotBlank()) {
                        if (existingIds.contains(code) || seenCodesInBatch.contains(code)) {
                            isDuplicate = true
                            rowErrors.add("Duplikat kode_kolom: $code")
                        } else {
                            seenCodesInBatch.add(code)
                        }
                    }
                }
                ImportType.DAFTAR_PEMILIH -> {
                    val code = rowMap["kode_pemilih"] ?: ""
                    val name = rowMap["nama_pemilih"] ?: ""
                    val colCode = rowMap["kode_kolom"] ?: ""
                    if (name.isBlank()) rowErrors.add("nama_pemilih wajib diisi")
                    if (colCode.isBlank()) rowErrors.add("kode_kolom wajib diisi")
                    val checkCode = if (code.isNotBlank()) code else "AUTO_${name}_${colCode}"
                    if (existingIds.contains(checkCode) || seenCodesInBatch.contains(checkCode)) {
                        isDuplicate = true
                        rowErrors.add("Duplikat kode_pemilih: $checkCode")
                    } else {
                        seenCodesInBatch.add(checkCode)
                    }
                }
                ImportType.MASTER_CALON -> {
                    val code = rowMap["kode_calon"] ?: ""
                    val name = rowMap["nama_calon"] ?: ""
                    val jabatan = rowMap["jenis_jabatan"]?.uppercase() ?: ""
                    if (code.isBlank()) rowErrors.add("kode_calon wajib diisi")
                    if (name.isBlank()) rowErrors.add("nama_calon wajib diisi")
                    if (jabatan != "DIAKEN" && jabatan != "PENATUA") {
                        rowErrors.add("jenis_jabatan harus DIAKEN atau PENATUA (ditemukan: $jabatan)")
                    }
                    if (code.isNotBlank()) {
                        if (existingIds.contains(code) || seenCodesInBatch.contains(code)) {
                            isDuplicate = true
                            rowErrors.add("Duplikat kode_calon: $code")
                        } else {
                            seenCodesInBatch.add(code)
                        }
                    }
                }
                ImportType.MASTER_PANITIA -> {
                    val code = rowMap["kode_panitia"] ?: ""
                    val name = rowMap["nama_panitia"] ?: ""
                    if (code.isBlank()) rowErrors.add("kode_panitia wajib diisi")
                    if (name.isBlank()) rowErrors.add("nama_panitia wajib diisi")
                    if (code.isNotBlank()) {
                        if (existingIds.contains(code) || seenCodesInBatch.contains(code)) {
                            isDuplicate = true
                            rowErrors.add("Duplikat kode_panitia: $code")
                        } else {
                            seenCodesInBatch.add(code)
                        }
                    }
                }
                ImportType.MASTER_UNSUR -> {
                    val code = rowMap["kode_unsur"] ?: ""
                    val name = rowMap["nama_unsur"] ?: ""
                    if (code.isBlank()) rowErrors.add("kode_unsur wajib diisi")
                    if (name.isBlank()) rowErrors.add("nama_unsur wajib diisi")
                    if (code.isNotBlank()) {
                        if (existingIds.contains(code) || seenCodesInBatch.contains(code)) {
                            isDuplicate = true
                            rowErrors.add("Duplikat kode_unsur: $code")
                        } else {
                            seenCodesInBatch.add(code)
                        }
                    }
                }
            }

            isValid = rowErrors.isEmpty()
            if (isDuplicate) duplicateCount++
            if (isValid) validCount++ else invalidCount++

            if (rowErrors.isNotEmpty()) {
                globalErrors.add("Baris $rowNum: ${rowErrors.joinToString("; ")}")
            }

            previewRows.add(
                ImportPreviewRow(
                    rowNumber = rowNum,
                    data = rowMap,
                    isValid = isValid,
                    isDuplicate = isDuplicate,
                    errors = rowErrors
                )
            )
        }

        return ImportSummary(
            sheetType = importType.name,
            totalRows = dataRows.size,
            validRows = validCount,
            invalidRows = invalidCount,
            duplicateRows = duplicateCount,
            errorsList = globalErrors,
            previewRows = previewRows
        )
    }

    fun getEmptyTemplate(type: ImportType): String {
        val headers = type.headers.joinToString(",")
        return headers + "\n"
    }

    fun getSampleData(type: ImportType): String {
        return when (type) {
            ImportType.MASTER_KOLOM -> """
kode_kolom,nama_kolom,nama_lokasi,tanggal_pemilihan,jenis_pemilihan,status_pemilihan,keterangan
KOLOM-01,Kolom 1 - Getsemani,Gedung Pertemuan Kolom 1,17-10-2026,Pemilihan Pelsus Kolom GMIM,Belum Mulai,Jadwal Pagi 09:00 WITA
KOLOM-02,Kolom 2 - Sion,Pastori 2 GMIM,17-10-2026,Pemilihan Pelsus Kolom GMIM,Belum Mulai,Jadwal Pagi 09:30 WITA
KOLOM-03,Kolom 3 - Betania,Rumah Kel. Tumiwa-Kambey,17-10-2026,Pemilihan Pelsus Kolom GMIM,Belum Mulai,Jadwal Siang 13:00 WITA
KOLOM-04,Kolom 4 - Eben Haezer,Gedung Serbaguna GMIM,17-10-2026,Pemilihan Pelsus Kolom GMIM,Belum Mulai,Jadwal Siang 13:30 WITA
KOLOM-05,Kolom 5 - Moria,Rumah Kel. Sondakh-Palar,17-10-2026,Pemilihan Pelsus Kolom GMIM,Belum Mulai,Jadwal Sore 16:00 WITA
            """.trimIndent()

            ImportType.DAFTAR_PEMILIH -> """
nomor,kode_pemilih,nama_pemilih,kode_kolom,alamat,nomor_anggota_opsional,status_aktif
1,PML-001,Meike Sondakh,KOLOM-01,Lingkungan 1 RT 02,GMIM-2026-001,true
2,PML-002,Johanis Palar,KOLOM-01,Lingkungan 1 RT 01,GMIM-2026-002,true
3,PML-003,Stenly Rantung,KOLOM-01,Lingkungan 1 RT 03,GMIM-2026-003,true
4,PML-004,Deisy Tumiwa,KOLOM-01,Lingkungan 1 RT 02,GMIM-2026-004,true
5,PML-005,Frangky Pangalila,KOLOM-01,Lingkungan 1 RT 04,GMIM-2026-005,true
6,PML-006,Merry Lumentut,KOLOM-02,Lingkungan 2 RT 01,GMIM-2026-006,true
7,PML-007,Ferry Mandagi,KOLOM-02,Lingkungan 2 RT 02,GMIM-2026-007,true
8,PML-008,Grace Wowor,KOLOM-02,Lingkungan 2 RT 03,GMIM-2026-008,true
9,PML-009,Rudi Karundeng,KOLOM-03,Lingkungan 3 RT 01,GMIM-2026-009,true
10,PML-010,Vanda Sumual,KOLOM-03,Lingkungan 3 RT 02,GMIM-2026-010,true
            """.trimIndent()

            ImportType.MASTER_CALON -> """
kode_calon,nama_calon,jenis_jabatan,kode_kolom,nomor_urut,status_calon
CLN-D01,Johan Poluan,DIAKEN,KOLOM-01,1,Aktif
CLN-D02,Martha Lengkey,DIAKEN,KOLOM-01,2,Aktif
CLN-P01,Daud Manopo,PENATUA,KOLOM-01,1,Aktif
CLN-P02,Ester Kalalo,PENATUA,KOLOM-01,2,Aktif
CLN-D03,Yance Rumengan,DIAKEN,KOLOM-02,1,Aktif
CLN-P03,Grace Paat,PENATUA,KOLOM-02,1,Aktif
CLN-D04,Steven Warouw,DIAKEN,KOLOM-03,1,Aktif
CLN-P04,Donald Supit,PENATUA,KOLOM-03,1,Aktif
            """.trimIndent()

            ImportType.MASTER_PANITIA -> """
kode_panitia,nama_panitia,jabatan_panitia,kode_kolom,nomor_telepon_opsional,status_aktif
PNT-01,Pnt. James Rumagit,Ketua Panitia Pemilihan,SEMUA,081244556677,true
PNT-02,Dkn. Ruth Sumendap,Sekretaris Panitia,SEMUA,081388990011,true
PNT-03,Bpk. Hendra Rondonuwu,Koordinator Kolom 1,KOLOM-01,085211223344,true
PNT-04,Ibu Shirley Wenas,Petugas Kehadiran Kolom 1,KOLOM-01,085377889900,true
PNT-05,Bpk. Royke Pangemanan,Koordinator Kolom 2,KOLOM-02,082199887766,true
            """.trimIndent()

            ImportType.MASTER_UNSUR -> """
kode_unsur,nama_unsur,jenis_pemilihan,keterangan
UNS-01,Pelayan Khusus Kolom,Pemilihan Kolom,Diaken dan Penatua Kolom
UNS-02,BIPRA Pria/Kaum Bapa (P/KB),Pemilihan Kategorial,BIPRA Jemaat
UNS-03,BIPRA Wanita/Kaum Ibu (W/KI),Pemilihan Kategorial,BIPRA Jemaat
UNS-04,BIPRA Pemuda,Pemilihan Kategorial,BIPRA Jemaat
UNS-05,BIPRA Remaja,Pemilihan Kategorial,BIPRA Jemaat
UNS-06,BIPRA Anak Sekolah Minggu (ASM),Pemilihan Kategorial,BIPRA Jemaat
UNS-07,Badan Pekerja Majelis Jemaat (BPMJ),Pemilihan BPMJ,Ketua/Wakil/Sekretaris/Bendahara/Anggota
UNS-08,Kelompok Fungsional Lansia,Pemilihan Lansia,Pengurus Lansia Jemaat
            """.trimIndent()
        }
    }
}
