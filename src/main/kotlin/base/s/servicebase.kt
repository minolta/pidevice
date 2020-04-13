package me.pixka.kt.base.s

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository


abstract class ServiceBase<T> : S<T> {

    @Autowired
    lateinit var repo: JpaRepository<T, Long>

    fun topage(page: Long = 0, limit: Long = 100): Pageable =  PageRequest.of(page.toInt(), limit.toInt(), Sort.by("id"))
    fun find(id: Long): T? {
        return repo.getOne(id)
    }

    fun all(): List<T>? {
        return repo.findAll()
    }

    fun save(o: T): T {
        return repo.save(o)
    }

}