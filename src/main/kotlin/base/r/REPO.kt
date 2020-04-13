package me.pixka.kt.base.r

import org.springframework.data.domain.Pageable

interface REPOBase<T> {

    fun search(search: String? = null, page: Pageable): List<T>?
}


