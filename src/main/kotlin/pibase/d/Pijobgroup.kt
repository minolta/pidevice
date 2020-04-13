package me.pixka.kt.pibase.d

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import me.pixka.kt.base.d.En
import me.pixka.kt.base.s.DefaultService
import me.pixka.kt.base.s.findByName
import me.pixka.kt.base.s.search
import org.hibernate.annotations.Cache
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import javax.persistence.Cacheable
import javax.persistence.Entity

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@Cacheable
@Cache(usage = org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE)
class Pijobgroup(var name: String? = null) : En()

@Repository
interface PijobgroupRepo : JpaRepository<Pijobgroup, Long>, findByName<Pijobgroup>, search<Pijobgroup> {
    @Query("from Pijobgroup p where p.name like %?1%")
    override fun search(s: String, page: Pageable): List<Pijobgroup>?
}

@Service
class PijobgroupService(val r: PijobgroupRepo) : DefaultService<Pijobgroup>() {


}