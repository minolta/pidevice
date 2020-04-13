package me.pixka.kt.base.s

import me.pixka.kt.base.r.REPOBase


open class ServiceImpl<T> : ServiceBase<T>() {
    override fun search(search: String, page: Long, limit: Long): List<T>? {
        val r = repo as REPOBase<T>
        return r.search(search, topage(page, limit))
    }

}