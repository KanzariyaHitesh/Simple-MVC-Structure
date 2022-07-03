package com.mvc.simple.model


import com.google.gson.annotations.SerializedName

data class LabelListResponse(
        @SerializedName("error")
    var error: Any,
        @SerializedName("payload")
    var payload: List<Payload>,
        @SerializedName("status")
    var status: String
) {
    data class Payload(
        @SerializedName("code")
        var code: String,
        @SerializedName("ed_label_id")
        var labelId: String,
        @SerializedName("value")
        var value: String
    )
}