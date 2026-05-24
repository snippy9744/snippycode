package com.snippyseat.app.data.salon

import com.squareup.moshi.Json

data class NearbySalonsResponse(
    val items: List<SalonDto> = emptyList(),
)

data class SalonDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val addressText: String? = null,
    val photos: List<String> = emptyList(),
    val averageRating: Double = 0.0,
    val totalReviews: Int = 0,
    val offersHomeService: Boolean = false,
    val isFeatured: Boolean = false,
    val distanceKm: Double? = null,
    val priceFrom: Double? = null,
    val isOpen: Boolean = true,
    val services: List<ServiceDto> = emptyList(),
    val seller: SellerDto? = null,
)

data class ServiceDto(
    val id: String? = null,
    val name: String? = null,
    val category: String? = null,
    val gender: String? = null,
    val price: Double? = null,
)

data class SellerDto(
    val id: String? = null,
    val shopName: String? = null,
    val isFeatured: Boolean = false,
    @Json(name = "sellerType")
    val sellerTypeRaw: String? = null,
)

data class Salon(
    val id: String,
    val name: String,
    val photoUrl: String?,
    val rating: Double,
    val reviewCount: Int,
    val distanceKm: Double,
    val priceFrom: Int,
    val offersHomeService: Boolean,
    val isOpen: Boolean,
    val isPromoted: Boolean,
    val tags: List<String>,
    val address: String,
)

data class SalonFeed(
    val featured: List<Salon>,
    val nearby: List<Salon>,
)
