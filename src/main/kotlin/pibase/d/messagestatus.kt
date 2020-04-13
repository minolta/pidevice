package me.pixka.kt.pibase.d

import me.pixka.kt.base.d.En
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import javax.persistence.Entity

@Entity
class Messagestatus(var name: String? = null) : En()


@Repository
interface MessagestatusRepo : JpaRepository<Messagestatus, Long>