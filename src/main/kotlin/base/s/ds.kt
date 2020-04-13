package me.pixka.kt.base.s

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository

interface S<T> {
    fun search(search: String = "", page: Long = 0, limit: Long = 100): List<T>?
}

abstract class Ds<T>() : S<T> {
    @Autowired
     lateinit var repo: JpaRepository<T, Long>

//    constructor() : this(repo = null)

    fun all() = repo?.findAll()
    fun save(o: T): T? = repo.save(o)
//    fun save(os: List<T>) = repo?.save(os)
    fun delete(item: T) = repo.delete(item)
    fun search(search: String = ""): List<T>? = search(search, 0, 100)
    fun topage(page: Long = 0, limit: Long = 100): Pageable = PageRequest.of(page.toInt(), limit.toInt(), Sort.by("id"))
    fun find(id: Long?): T? {
        var o = repo.findById(id)
        if(o.isPresent)
            return o.get()
        return null
    }

    fun lists(limit: Long = 50) = repo!!.findAll(topage(0, limit))?.content as List<T>


}