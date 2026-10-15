package com.example

import com.example.data.excel.ExcelCsvManager
import com.example.data.model.JenisJabatan
import com.example.data.repository.ElectionRepository
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testHeaderNormalization() {
        assertEquals("kode_kolom", ExcelCsvManager.normalizeHeader("Kode Kolom"))
        assertEquals("kode_kolom", ExcelCsvManager.normalizeHeader("KODE_KOLOM"))
        assertEquals("kode_kolom", ExcelCsvManager.normalizeHeader("  kode_kolom  "))
        assertEquals("nomor_anggota_opsional", ExcelCsvManager.normalizeHeader("Nomor Anggota (Opsional)"))
    }

    @Test
    fun testDelimitedCsvParser() {
        val csv = """
            kode_kolom,nama_kolom,nama_lokasi
            KOLOM-01,"Kolom 1, Getsemani",Gedung Serbaguna
        """.trimIndent()

        val parsed = ExcelCsvManager.parseDelimitedText(csv)
        assertEquals(2, parsed.size)
        assertEquals(listOf("kode_kolom", "nama_kolom", "nama_lokasi"), parsed[0])
        assertEquals("Kolom 1, Getsemani", parsed[1][1])
    }

    @Test
    fun testAnalyzeImportValidation() {
        val sample = """
            kode_kolom,nama_kolom,nama_lokasi
            KOLOM-01,Kolom 1,Lokasi A
            ,Kolom Tanpa Kode,Lokasi B
            KOLOM-01,Kolom Duplikat,Lokasi C
        """.trimIndent()

        val summary = ExcelCsvManager.analyzeImport(
            rawContent = sample,
            importType = ExcelCsvManager.ImportType.MASTER_KOLOM,
            existingIds = emptySet()
        )

        assertEquals(3, summary.totalRows)
        assertEquals(1, summary.validRows)
        assertEquals(2, summary.invalidRows)
        assertEquals(1, summary.duplicateRows)
    }

    @Test
    fun testVoteValidation() {
        val repo = ElectionRepository.getInstance()

        // Negative votes should fail
        val (negSuccess, _) = repo.saveVoteResult(
            kodeKolom = "KOLOM-01",
            jenisJabatan = JenisJabatan.DIAKEN,
            suaraCalon = mapOf("C1" to -2),
            suaraSah = -2,
            suaraRusak = 0,
            suaraTidakDigunakan = 0,
            catatan = "",
            isFinal = false
        )
        assertFalse(negSuccess)

        // Mismatched candidate sum should fail
        val (mismatchSuccess, _) = repo.saveVoteResult(
            kodeKolom = "KOLOM-01",
            jenisJabatan = JenisJabatan.DIAKEN,
            suaraCalon = mapOf("C1" to 5),
            suaraSah = 8, // does not match sum of 5
            suaraRusak = 0,
            suaraTidakDigunakan = 0,
            catatan = "",
            isFinal = false
        )
        assertFalse(mismatchSuccess)
    }

    @Test
    fun testProgressCalculation() {
        val repo = ElectionRepository.getInstance()
        val progress = repo.calculateColumnProgress("KOLOM-01")
        assertTrue("Progress should be between 0 and 100", progress in 0..100)
    }
}
