package me.pixka.kt.base.o

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
open class SearchOption(val search: String? = "", val limit: Long? = null,
                        val page: Long? = null,
                        @JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.sss'Z'") val s: Date? = null,
                        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.sss'Z'") val e: Date? = null,
                        val id: Long? = 0
) {

    override fun toString(): String {
        return "search:${search} limit:${limit} page:${page} s:${s} e:${e} id:${id}";
    }
}