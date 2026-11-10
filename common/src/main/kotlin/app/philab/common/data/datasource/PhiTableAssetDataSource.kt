package app.philab.common.data.datasource

import android.content.Context
import app.philab.common.di.qualifier.IoDispatcher
import app.philab.common.domain.phi.model.PhiTable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PhiTableAssetDataSource @Inject constructor(
    private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun load(): PhiTable = withContext(ioDispatcher) {
        val text = context.assets.open(ASSET_NAME).bufferedReader(Charsets.UTF_8).use { it.readText() }
        PhiTableJsonParser.parse(text)
    }

    companion object {
        const val ASSET_NAME = "phi_table.json"
    }
}
