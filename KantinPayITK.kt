// ============================================================
//  KantinPay ITK — Sistem E-Wallet Kantin Kampus
//  Mata Kuliah : Pemrograman Berorientasi Objek (TE2514027)
//  Studi Kasus  : Tema 1 — KantinPay ITK
// ============================================================

// ──────────────────────────────────────────────────────────────
//  CLASS: Menu
//  Merepresentasikan satu item menu di stand makanan.
//  Stok bersifat private agar tidak diubah sembarangan.
// ──────────────────────────────────────────────────────────────
class Menu(
    val namaMenu: String,
    val harga: Double,
    stokAwal: Int
) {
    private var stok: Int = stokAwal

    fun getStok(): Int = stok

    /**
     * Kurangi stok setelah transaksi berhasil divalidasi.
     * Hanya bisa dipanggil dari dalam package/class terpercaya.
     */
    fun kurangiStok(jumlah: Int): Boolean {
        return if (stok >= jumlah) {
            stok -= jumlah
            true
        } else {
            false
        }
    }

    fun tampilInfo() {
        println("  [Menu] $namaMenu | Harga: Rp${harga.toInt()} | Stok: $stok")
    }
}

// ──────────────────────────────────────────────────────────────
//  CLASS: StandMakanan
//  Merepresentasikan stand/warung di kantin.
//  Memiliki daftar menu yang dikelolanya.
// ──────────────────────────────────────────────────────────────
class StandMakanan(val namaStand: String) {
    private val daftarMenu: MutableList<Menu> = mutableListOf()

    fun tambahMenu(menu: Menu) {
        daftarMenu.add(menu)
        println("[Stand] Menu '${menu.namaMenu}' ditambahkan ke $namaStand.")
    }

    fun cariMenu(namaMenu: String): Menu? {
        return daftarMenu.find {
            it.namaMenu.equals(namaMenu, ignoreCase = true)
        }
    }

    fun tampilSemuaMenu() {
        println("\n  === Daftar Menu: $namaStand ===")
        if (daftarMenu.isEmpty()) {
            println("  (Tidak ada menu tersedia)")
        } else {
            daftarMenu.forEach { it.tampilInfo() }
        }
    }
}

// ──────────────────────────────────────────────────────────────
//  CLASS: Mahasiswa
//  Merepresentasikan pengguna e-wallet kantin.
//  Saldo dan PIN bersifat PRIVATE — tidak bisa diakses
//  langsung dari luar class (Enkapsulasi Ketat).
// ──────────────────────────────────────────────────────────────
class Mahasiswa(
    val nim: String,
    val nama: String,
    saldoAwal: Double,
    pinRahasia: String
) {
    // DATA SENSITIF — wajib private
    private var saldo: Double = saldoAwal
    private val pin: String = pinRahasia

    fun getSaldo(): Double = saldo

    // ── VALIDASI PIN ──────────────────────────────────────────
    /**
     * Verifikasi PIN yang dimasukkan pengguna.
     * PIN asli tidak pernah dikembalikan ke luar class.
     */
    fun verifikasiPin(inputPin: String): Boolean {
        return pin == inputPin
    }

    // ── TOP UP SALDO ──────────────────────────────────────────
    /**
     * Jalur resmi untuk menambah saldo.
     * Business Rule: jumlah top-up harus > 0.
     */
    fun topUpSaldo(jumlah: Double) {
        if (jumlah <= 0) {
            println("[ERROR] Top-up gagal: Jumlah top-up harus lebih dari Rp0.")
            return
        }
        saldo += jumlah
        println("[INFO] Top-up berhasil. Saldo $nama sekarang: Rp${saldo.toInt()}")
    }

    // ── BELI MENU ─────────────────────────────────────────────
    /**
     * Jalur resmi untuk melakukan transaksi pembelian menu.
     *
     * Business Rules yang divalidasi secara ketat:
     *  1. PIN harus benar
     *  2. Saldo mahasiswa harus mencukupi (>= harga menu)
     *  3. Stok menu harus > 0
     *  4. Saldo tidak boleh minus setelah transaksi
     */
    fun beliMenu(
        stand: StandMakanan,
        namaMenu: String,
        inputPin: String
    ) {
        println("\n--- Proses Pembelian: $nama → '$namaMenu' di ${stand.namaStand} ---")

        // Rule 1: Verifikasi PIN
        if (!verifikasiPin(inputPin)) {
            println("[GAGAL] Transaksi ditolak: PIN tidak valid. Harap masukkan PIN yang benar.")
            return
        }
        println("[✓] PIN terverifikasi.")

        // Cari menu di stand
        val menu = stand.cariMenu(namaMenu)
        if (menu == null) {
            println("[GAGAL] Transaksi ditolak: Menu '$namaMenu' tidak ditemukan di ${stand.namaStand}.")
            return
        }

        // Rule 3: Cek stok menu
        if (menu.getStok() <= 0) {
            println("[GAGAL] Transaksi ditolak: Stok menu '${menu.namaMenu}' telah habis.")
            return
        }
        println("[✓] Stok tersedia: ${menu.getStok()} porsi.")

        // Rule 2: Cek kecukupan saldo
        if (saldo < menu.harga) {
            println(
                "[GAGAL] Transaksi ditolak: Saldo tidak mencukupi. " +
                "Saldo Anda Rp${saldo.toInt()}, harga menu Rp${menu.harga.toInt()}."
            )
            return
        }
        println("[✓] Saldo mencukupi.")

        // Rule 4: Eksekusi transaksi — saldo tidak boleh minus
        val saldobefore = saldo
        saldo -= menu.harga
        menu.kurangiStok(1)

        println("[SUKSES] Pembelian berhasil!")
        println("         Menu    : ${menu.namaMenu}")
        println("         Harga   : Rp${menu.harga.toInt()}")
        println("         Saldo sebelum : Rp${saldobefore.toInt()}")
        println("         Saldo sesudah : Rp${saldo.toInt()}")
        println("         Stok tersisa  : ${menu.getStok()} porsi")
    }

    fun tampilInfo() {
        println("  [Mahasiswa] NIM: $nim | Nama: $nama | Saldo: Rp${saldo.toInt()}")
    }
}


// ──────────────────────────────────────────────────────────────
//  MAIN FUNCTION — Simulasi Lengkap KantinPay ITK
// ──────────────────────────────────────────────────────────────
fun main() {
    println("============================================================")
    println("         KANTINPAY ITK — SIMULASI SISTEM E-WALLET           ")
    println("============================================================")

    // ── INISIALISASI OBJEK ──────────────────────────────────
    println("\n[INISIALISASI] Membuat data awal sistem...")

    val stand1 = StandMakanan("Warung Bu Siti")
    val menuNasiGoreng = Menu("Nasi Goreng Spesial", 15000.0, 5)
    val menuEsTeh      = Menu("Es Teh Manis",         5000.0, 2)
    val menuMieAyam    = Menu("Mie Ayam Bakso",       12000.0, 0) // stok habis!

    stand1.tambahMenu(menuNasiGoreng)
    stand1.tambahMenu(menuEsTeh)
    stand1.tambahMenu(menuMieAyam)

    val mahasiswa1 = Mahasiswa("04221001", "Andi Pratama",   20000.0, "1234")
    val mahasiswa2 = Mahasiswa("04221002", "Budi Santoso",    5000.0, "5678")

    stand1.tampilSemuaMenu()

    println("\n[STATUS AWAL MAHASISWA]")
    mahasiswa1.tampilInfo()
    mahasiswa2.tampilInfo()

    // ════════════════════════════════════════════════════════
    //  BLOK SIMULASI GAGAL
    // ════════════════════════════════════════════════════════
    println("\n\n════════════ SIMULASI GAGAL ════════════")

    // ── GAGAL 1: PIN SALAH ───────────────────────────────
    println("\n[Skenario Gagal #1] Mahasiswa memasukkan PIN yang salah")
    mahasiswa1.beliMenu(stand1, "Nasi Goreng Spesial", "9999")

    // ── GAGAL 2: STOK HABIS ──────────────────────────────
    println("\n[Skenario Gagal #2] Mahasiswa mencoba membeli menu yang stoknya habis")
    mahasiswa1.beliMenu(stand1, "Mie Ayam Bakso", "1234")

    // ── GAGAL 3: SALDO TIDAK CUKUP ───────────────────────
    println("\n[Skenario Gagal #3] Saldo mahasiswa tidak mencukupi harga menu")
    // Budi hanya punya Rp5.000, harga Nasi Goreng Rp15.000
    mahasiswa2.beliMenu(stand1, "Nasi Goreng Spesial", "5678")

    // ── GAGAL 4: MENU TIDAK ADA ──────────────────────────
    println("\n[Skenario Gagal #4] Mahasiswa memesan menu yang tidak ada di stand")
    mahasiswa1.beliMenu(stand1, "Ayam Geprek", "1234")

    // ── GAGAL 5: TOP UP NEGATIF ──────────────────────────
    println("\n[Skenario Gagal #5] Mahasiswa mencoba top-up dengan nilai negatif")
    mahasiswa1.topUpSaldo(-50000.0)

    // ════════════════════════════════════════════════════════
    //  BLOK SIMULASI SUKSES
    // ════════════════════════════════════════════════════════
    println("\n\n════════════ SIMULASI SUKSES ════════════")

    // ── SUKSES 1: Andi beli Nasi Goreng ──────────────────
    println("\n[Skenario Sukses #1] Andi berhasil membeli Nasi Goreng Spesial")
    mahasiswa1.beliMenu(stand1, "Nasi Goreng Spesial", "1234")

    // ── SUKSES 2: Top-up saldo lalu beli lagi ────────────
    println("\n[Skenario Sukses #2] Budi top-up saldo terlebih dahulu, lalu membeli Es Teh")
    mahasiswa2.topUpSaldo(10000.0)
    mahasiswa2.beliMenu(stand1, "Es Teh Manis", "5678")

    // ── SUKSES 3: Andi beli Es Teh sisa stok terakhir ────
    println("\n[Skenario Sukses #3] Andi membeli Es Teh (stok sisa 1)")
    mahasiswa1.beliMenu(stand1, "Es Teh Manis", "1234")

    // ── VALIDASI PASCA-TRANSAKSI: Es Teh seharusnya habis
    println("\n[Skenario Gagal #6] Andi mencoba beli Es Teh lagi — stok sudah habis")
    mahasiswa1.beliMenu(stand1, "Es Teh Manis", "1234")

    // ════════════════════════════════════════════════════════
    //  STATUS AKHIR SISTEM
    // ════════════════════════════════════════════════════════
    println("\n\n============================================================")
    println("               STATUS AKHIR SISTEM                         ")
    println("============================================================")
    println("\n[STATUS AKHIR MAHASISWA]")
    mahasiswa1.tampilInfo()
    mahasiswa2.tampilInfo()
    stand1.tampilSemuaMenu()

    println("\n[INFO] Semua business rules telah tervalidasi dengan benar.")
    println("       Enkapsulasi Kotlin berjalan sempurna.")
    println("============================================================")
}
