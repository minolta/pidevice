package me.pixka.kt.base.s

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository


open class DefaultService<T> {

    @Autowired
    lateinit var repo: JpaRepository<T, Long>

    fun delete(o: T) {
        repo.delete(o)
    }

    fun save(o: T): T {
        return repo.save(o)
    }

    fun findByName(n: String): T? {
        try {
            var i = repo as findByName<T>
            return i.findByName(n)
        } catch (e: Exception) {
            throw e
        }
    }

    fun topage(page: Long = 0, limit: Long = 100): Pageable = PageRequest.of(page.toInt(), limit.toInt(), Sort.by("id"))
    fun search(s: String, page: Long, limit: Long): List<T>? {
        try {
            var i = repo as search<T>
            var p = PageRequest.of(page.toInt(), limit.toInt(), Sort.by("id"))
            return i.search(s, p)
        } catch (e: Exception) {
            throw e
        }
    }

    fun find(id: Long): T {
        var o = repo.findById(id)
        if (o.isPresent)
            return o.get()

        throw Exception("Not found")
    }


}

interface findByName<T> {
    fun findByName(n: String): T?
}

interface search<T> {
    fun search(s: String, page: Pageable): List<T>?
}

interface findOrCreate<T> {
    fun findOrCreate(n: String): T
}