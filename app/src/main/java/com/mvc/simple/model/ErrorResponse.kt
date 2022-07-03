package com.mvc.simple.model
import com.google.gson.annotations.SerializedName

data class ErrorResponse(
    @SerializedName("error")
    val error: Error,
    @SerializedName("payload")
    val payload: Any,
    @SerializedName("status")
    val status: String
) {
    data class Error(
        @SerializedName("code")
        val code: Int,
        @SerializedName("message")
        var message: String,
        @SerializedName("name")
        val name: String
    )
}