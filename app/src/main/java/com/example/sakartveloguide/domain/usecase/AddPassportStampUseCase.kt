package com.example.sakartveloguide.domain.usecase

import com.example.sakartveloguide.data.local.entity.PassportEntity
import com.example.sakartveloguide.domain.model.TripPath
import com.example.sakartveloguide.domain.repository.PassportRepository
import javax.inject.Inject

class AddPassportStampUseCase @Inject constructor(
    private val repository: PassportRepository
) {
    suspend operator fun invoke(trip: TripPath) {
        val regionId = trip.category.name 
        
        // Clean formatting for the stamp name
        val regionName = trip.category.name
            .replace("_", " ")
            .lowercase()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

        // ...
        val stamp = PassportEntity(
            regionId = regionId,
            regionName = regionName,
            dateUnlocked = System.currentTimeMillis(),
            tripTitle = trip.title.get("en") // EXTRACT PLAIN STRING
        )
// ...

        
        // Now this reference will be resolved!
        repository.addStamp(stamp)
    }
}
