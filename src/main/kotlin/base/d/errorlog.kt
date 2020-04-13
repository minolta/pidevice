package me.pixka.kt.base.d
import me.pixka.kt.base.d.En
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity

@Entity
class Errorlog(var location: String? = null, var line: String? = null,
               @Column(columnDefinition = "text") var message: String? = null) : En() {
    constructor() : this(location = null)
}

@Repository
interface ErrorlogRepo : JpaRepository<Errorlog, Long> {
    @Query("from Errorlog el where (el.location like %?1% or el.line like %?1% or el.message like %?1%)  and (el.adddate >= ?2 and el.adddate <=?3)")
    fun searchbyDate(search: String, s: Date?, e: Date?, topage: Pageable?): List<Errorlog>?

}