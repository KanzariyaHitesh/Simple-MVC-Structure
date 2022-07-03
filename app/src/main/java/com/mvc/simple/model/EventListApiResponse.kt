package com.mvc.simple.model

data class EventListApiResponse(
    val `data`: Data = Data(),
    val success: Boolean = false
) {
    data class Data(
        val agenda_list: List<Agenda> = listOf(),
        val category: List<Category> = listOf(),
        val category_relation: List<CategoryRelation> = listOf(),
        val types: List<Type> = listOf()
    ) {
        data class Agenda(
            val Address_map: String = "",
            val Address_map_id: String = "",
            val Agenda_status: String = "",
            val Documents: String = "",
            val End_date: String = "",
            val End_time: String = "",
            val Event_show_time_zone: String = "",
            val Heading: String = "",
            val Map_title: String = "",
            val Maximum_People: String = "",
            val Speaker_id: String = "",
            val Start_date: String = "",
            val Start_time: String = "",
            val Types: String = "",
            val agenda_id: String = "",
            val chair: String = "",
            val checking_datetime: String = "",
            val description: String = "",
            val document_id: String = "",
            val is_on_demand: String = "",
            val is_saved: String = "",
            val location: String = "",
            val other_types: String = "",
            val presentation_id: String = "",
            val session_image: String = "",
            val session_tracks: String = "",
            val short_desc: String = "",
            val show_checking_in: String = "",
            val sort_order: String = "",
            val speaker: String = "",
            val sponsor: String = "",
            val survey_id: String = "",
            val time_zone: String = "",
            val timestamp: Int = 0,
            val tname: String = "",
            val type_id: String = "",
            val type_ids: List<TypeId> = listOf()
        ) {
            data class TypeId(
                val color: String = "",
                val name: String = ""
            )
        }

        data class Category(
            val Id: String = "",
            val categorie_type: String = "",
            val category_image: String = "",
            val category_name: String = "",
            val created_date: String = "",
            val event_id: String = "",
            val show_check_in_time: String = "",
            val sort_order: String = "",
            val updated_date: String = "",
            val welcome_screen: String = ""
        )

        data class CategoryRelation(
            val Id: String = "",
            val agenda_id: String = "",
            val category_id: String = "",
            val created_date: String = "",
            val updated_date: String = ""
        )

        data class Type(
            val created_date: String = "",
            val event_id: String = "",
            val order_no: String = "",
            val type_color: String = "",
            val type_id: String = "",
            val type_name: String = "",
            val updated_date: String = ""
        )
    }
}