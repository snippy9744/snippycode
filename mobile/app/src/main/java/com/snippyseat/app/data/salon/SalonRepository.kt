package com.snippyseat.app.data.salon

import com.snippyseat.app.data.network.SnippySeatApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SalonRepository @Inject constructor(
    private val api: SnippySeatApi,
) {
    suspend fun loadHomeFeed(
        lat: Double = DEFAULT_LAT,
        lng: Double = DEFAULT_LNG,
        gender: String? = null,
        category: String? = null,
    ): SalonFeed {
        return runCatching {
            val featured = api.getFeaturedSalons().data.orEmpty().map { it.toSalon(promoted = true) }
            val nearby = api.getNearbySalons(
                lat = lat,
                lng = lng,
                gender = gender,
                category = category,
                sort = "nearest",
            ).data?.items.orEmpty().map { it.toSalon(promoted = it.isFeatured) }

            SalonFeed(featured = featured.ifEmpty { sampleSalons.take(2) }, nearby = nearby.ifEmpty { sampleSalons })
        }.getOrElse {
            SalonFeed(featured = sampleSalons.take(2), nearby = sampleSalons)
        }
    }

    suspend fun searchSalons(
        query: String,
        filters: SalonSearchFilters,
        lat: Double = DEFAULT_LAT,
        lng: Double = DEFAULT_LNG,
    ): List<Salon> {
        return runCatching {
            api.getNearbySalons(
                lat = lat,
                lng = lng,
                radius = filters.distanceKm.toDouble(),
                gender = filters.gender.apiValue,
                category = filters.services.firstOrNull()?.apiValue,
                rating = filters.rating,
                homeService = filters.homeServiceOnly.takeIf { it },
                sort = filters.sort.apiValue,
                query = query.ifBlank { null },
            ).data?.items.orEmpty().map { it.toSalon(promoted = it.isFeatured) }
        }.getOrElse {
            sampleSalons.filter { salon ->
                query.isBlank() ||
                    salon.name.contains(query, ignoreCase = true) ||
                    salon.tags.any { it.contains(query, ignoreCase = true) }
            }
        }
    }

    private fun SalonDto.toSalon(promoted: Boolean): Salon {
        val serviceTags = services.mapNotNull { it.name ?: it.category }.distinct().take(3)
        val price = priceFrom ?: services.mapNotNull { it.price }.minOrNull() ?: 199.0

        return Salon(
            id = id,
            name = name,
            photoUrl = photos.firstOrNull(),
            rating = averageRating,
            reviewCount = totalReviews,
            distanceKm = distanceKm ?: 1.8,
            priceFrom = price.toInt(),
            offersHomeService = offersHomeService,
            isOpen = isOpen,
            isPromoted = promoted || seller?.isFeatured == true,
            tags = serviceTags.ifEmpty { listOf("Haircut", "Shaving", "Facial") },
            address = addressText.orEmpty(),
        )
    }

    private companion object {
        const val DEFAULT_LAT = 19.0760
        const val DEFAULT_LNG = 72.8777
    }
}

enum class GenderFilter(val label: String, val apiValue: String?) {
    ALL("All", null),
    MEN("Men", "MEN"),
    WOMEN("Women", "WOMEN"),
}

enum class ServiceCategoryFilter(val label: String, val apiValue: String?) {
    HAIRCUT("Haircut", "HAIRCUT"),
    SHAVING("Shaving", "SHAVING"),
    COLORING("Coloring", "COLORING"),
    SMOOTHENING("Smoothening", "SMOOTHENING"),
    STRAIGHTENING("Straightening", "STRAIGHTENING"),
    FACIAL("Facial", "FACIAL"),
    THREADING("Threading", "THREADING"),
    WAXING("Waxing", "WAXING"),
    BRIDAL("Bridal", "BRIDAL"),
    KIDS_CUT("Kids Cut", "KIDS_CUT"),
}

enum class SortOption(val label: String, val apiValue: String) {
    NEAREST("Nearest", "nearest"),
    TOP_RATED("Top Rated", "rating"),
    PRICE_LOW_HIGH("Price Low-High", "price"),
}

data class SalonSearchFilters(
    val gender: GenderFilter = GenderFilter.ALL,
    val distanceKm: Float = 10f,
    val rating: Double? = null,
    val services: Set<ServiceCategoryFilter> = emptySet(),
    val homeServiceOnly: Boolean = false,
    val sort: SortOption = SortOption.NEAREST,
)

private val sampleSalons = listOf(
    Salon(
        id = "sample-red-chair",
        name = "Red Chair Studio Bandra",
        photoUrl = "https://images.unsplash.com/photo-1521590832167-7bcbfaa6381f",
        rating = 4.8,
        reviewCount = 126,
        distanceKm = 1.4,
        priceFrom = 199,
        offersHomeService = true,
        isOpen = true,
        isPromoted = true,
        tags = listOf("Haircut", "Beard", "Facial"),
        address = "Linking Road, Bandra West",
    ),
    Salon(
        id = "sample-glow-snip",
        name = "Glow & Snip Indiranagar",
        photoUrl = "https://images.unsplash.com/photo-1560066984-138dadb4c035",
        rating = 4.6,
        reviewCount = 84,
        distanceKm = 2.2,
        priceFrom = 299,
        offersHomeService = false,
        isOpen = true,
        isPromoted = true,
        tags = listOf("Haircut", "Threading", "Bridal"),
        address = "100 Feet Road, Indiranagar",
    ),
    Salon(
        id = "sample-urban-trim",
        name = "Urban Trim Lounge",
        photoUrl = "https://images.unsplash.com/photo-1503951914875-452162b0f3f1",
        rating = 4.4,
        reviewCount = 57,
        distanceKm = 3.7,
        priceFrom = 149,
        offersHomeService = true,
        isOpen = false,
        isPromoted = false,
        tags = listOf("Shaving", "Haircut", "Head Massage"),
        address = "HSR Layout, Bengaluru",
    ),
)
