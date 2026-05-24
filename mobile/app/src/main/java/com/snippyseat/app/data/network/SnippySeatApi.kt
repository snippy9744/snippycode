package com.snippyseat.app.data.network

import com.snippyseat.app.data.auth.GoogleAuthRequest
import com.snippyseat.app.data.auth.LoginResponse
import com.snippyseat.app.data.auth.RoleSelectionRequest
import com.snippyseat.app.data.auth.SendOtpRequest
import com.snippyseat.app.data.auth.SendOtpResponse
import com.snippyseat.app.data.auth.VerifyOtpRequest
import com.snippyseat.app.data.booking.BookingCreateRequest
import com.snippyseat.app.data.booking.BookingCreateResponse
import com.snippyseat.app.data.booking.SalonDetailDto
import com.snippyseat.app.data.booking.SalonReviewDto
import com.snippyseat.app.data.booking.SalonStaffDto
import com.snippyseat.app.data.booking.TimeSlotDto
import com.snippyseat.app.data.salon.NearbySalonsResponse
import com.snippyseat.app.data.salon.SalonDto
import com.snippyseat.app.data.user.UpdateUserRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.Query

interface SnippySeatApi {
    @POST("auth/send-otp")
    suspend fun sendOtp(@Body request: SendOtpRequest): ApiEnvelope<SendOtpResponse>

    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): ApiEnvelope<LoginResponse>

    @POST("auth/google")
    suspend fun googleAuth(@Body request: GoogleAuthRequest): ApiEnvelope<LoginResponse>

    @POST("auth/select-role")
    suspend fun selectRole(@Body request: RoleSelectionRequest): ApiEnvelope<LoginResponse>

    @PATCH("users/me")
    suspend fun updateMe(@Body request: UpdateUserRequest): ApiEnvelope<Unit>

    @GET("salons/featured")
    suspend fun getFeaturedSalons(): ApiEnvelope<List<SalonDto>>

    @GET("salons")
    suspend fun getNearbySalons(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("radius") radius: Double? = null,
        @Query("gender") gender: String? = null,
        @Query("category") category: String? = null,
        @Query("rating") rating: Double? = null,
        @Query("homeService") homeService: Boolean? = null,
        @Query("sort") sort: String? = null,
        @Query("q") query: String? = null,
    ): ApiEnvelope<NearbySalonsResponse>

    @GET("salons/{id}")
    suspend fun getSalonDetail(@Path("id") salonId: String): ApiEnvelope<SalonDetailDto>

    @GET("salons/{id}/staff")
    suspend fun getSalonStaff(@Path("id") salonId: String): ApiEnvelope<List<SalonStaffDto>>

    @GET("salons/{id}/reviews")
    suspend fun getSalonReviews(@Path("id") salonId: String): ApiEnvelope<List<SalonReviewDto>>

    @GET("salons/{id}/slots")
    suspend fun getSalonSlots(
        @Path("id") salonId: String,
        @Query("date") date: String,
        @Query("staffId") staffId: String? = null,
    ): ApiEnvelope<List<TimeSlotDto>>

    @POST("bookings")
    suspend fun createBooking(@Body request: BookingCreateRequest): ApiEnvelope<BookingCreateResponse>
}
