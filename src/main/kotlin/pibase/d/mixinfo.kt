package me.pixka.kt.pibase.d

import me.pixka.kt.base.d.En
import me.pixka.kt.base.s.findByName
import me.pixka.kt.base.s.search
import org.hibernate.annotations.Cache
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import javax.persistence.Cacheable
import javax.persistence.Column
import javax.persistence.Entity

/*
* สำหรับการผสม
* */

@Entity
@Cacheable
@Cache(usage = org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE)

class Mixedinfo(var name: String, @Column(columnDefinition = "text") var description: String? = null,
                var bran: BigDecimal? = null, var dolomi: BigDecimal? = null, var water: BigDecimal? = null,
                var sawdust: BigDecimal? = null, var checksum: BigDecimal? = null) : En() {
    constructor() : this(name = "")

}

@Repository
interface MixedinfoRepo : JpaRepository<Mixedinfo, Long>, search<Mixedinfo>, findByName<Mixedinfo> {
    @Query("from Mixedinfo m where m.name like %?1%")
    override fun search(search: String, topage: Pageable): List<Mixedinfo>?

}
