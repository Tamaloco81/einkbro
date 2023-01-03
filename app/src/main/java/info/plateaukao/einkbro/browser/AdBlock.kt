package info.plateaukao.einkbro.browser

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import info.plateaukao.einkbro.database.RecordDb
import info.plateaukao.einkbro.preference.ConfigManager
import info.plateaukao.einkbro.unit.RecordUnit
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URI
import java.net.URISyntaxException
import java.util.*

class AdBlock(private val context: Context): KoinComponent {
    private val config: ConfigManager by inject()
    private val recordDb: RecordDb by inject()

    fun isWhite(url: String?): Boolean {
        for (domain in whitelist) {
            if (url != null && url.contains(domain!!)) {
                return true
            }
        }
        return false
    }

    fun isAd(url: String): Boolean {
        val domain: String = try {
            getDomain(url).lowercase(locale)
        } catch (u: URISyntaxException) {
            return false
        }
        return hosts.contains(domain) || isAdExtraSites(url)
    }

    private fun isAdExtraSites(url: String): Boolean {
        return config.adSites.any { url.contains(it, true) }
    }

    fun addDomain(domain: String?) {
        recordDb.addDomain(domain, RecordUnit.TABLE_WHITELIST)
        whitelist.add(domain)
    }

    fun removeDomain(domain: String?) {
        recordDb.deleteDomain(domain, RecordUnit.TABLE_WHITELIST)
        whitelist.remove(domain)
    }

    fun clearDomains() {
        recordDb.clearDomains()
        whitelist.clear()
    }

    private fun loadHosts(context: Context) {
        val thread = Thread {
            val manager = context.assets
            try {
                val reader = BufferedReader(InputStreamReader(manager.open(FILE)))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    line?.let { hosts.add(it.lowercase(locale)) }
                }
            } catch (i: IOException) {
                Log.w("browser", "Error loading hosts", i)
            }
        }
        thread.start()
    }

    @Synchronized
    private fun loadDomains() {
        whitelist.clear()
        whitelist.addAll(recordDb.listDomains(RecordUnit.TABLE_WHITELIST))
    }

    companion object {
        private const val FILE = "hosts.txt"
        private val hosts: MutableSet<String> = HashSet()
        private val whitelist: MutableList<String?> = ArrayList()

        @SuppressLint("ConstantLocale")
        private val locale = Locale.getDefault()

        @Throws(URISyntaxException::class)
        private fun getDomain(url: String): String {
            var url = url
            url = url.lowercase(locale)
            val index = url.indexOf('/', 8) // -> http://(7) and https://(8)
            if (index != -1) {
                url = url.substring(0, index)
            }
            val uri = URI(url)
            val domain = uri.host ?: return url
            return if (domain.startsWith("www.")) domain.substring(4) else domain
        }
    }

    init {
        if (hosts.isEmpty()) {
            loadHosts(context)
        }
        loadDomains()
    }
}