package app.philab.common.domain.phi.repository

import app.philab.common.domain.phi.model.PhiTable

interface PhiTableRepository {

    /** Loads (once) and returns the Φ(z) table. */
    suspend fun getTable(): Result<PhiTable>
}
