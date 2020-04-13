//package me.pixka.kt.base.d
//
//import me.pixka.kt.base.s.findByName
//import me.pixka.kt.base.s.search
//import org.springframework.data.domain.Pageable
//import org.springframework.data.jpa.repository.JpaRepository
//import org.springframework.data.jpa.repository.Query
//import org.springframework.stereotype.Repository
//import javax.persistence.Column
//import javax.persistence.Entity
//
//@Entity
//class Dbconfig(var name: String? = null, var value: String? = null,
//               @Column(columnDefinition = "text") var description: String? = null) : En() {
//    constructor() : this(name = null)
//}
//
//@Repository
//interface DbconfigRepo : JpaRepository<Dbconfig, Long>, findByName<Dbconfig>, search<Dbconfig> {
//    @Query("from Dbconfig d where d.name like %?1%")
//    override fun search(search: String, topage: Pageable): List<Dbconfig>?
//}