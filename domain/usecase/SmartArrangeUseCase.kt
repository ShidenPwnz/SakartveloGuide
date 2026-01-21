class SmartArrangeUseCase @Inject constructor() {
    operator fun invoke(startPoint: GeoPoint, locations: List<LocationEntity>): List<LocationEntity> {
        val optimized = mutableListOf<LocationEntity>()
        val remaining = locations.toMutableList()

        var currentLoc = startPoint
        while (remaining.isNotEmpty()) {
            val nearest = remaining.minByOrNull {
                calculateDistance(currentLoc, GeoPoint(it.latitude, it.longitude))
            }

            nearest?.let {
                optimized.add(it)
                currentLoc = GeoPoint(it.latitude, it.longitude)
                remaining.remove(it)
            }
        }

        return optimized
    }
}