package me.pixka.kt.pidevice.d

import me.pixka.kt.base.d.En
import me.pixka.kt.pibase.d.PiDevice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import java.util.*
import javax.persistence.Entity
import javax.persistence.ManyToOne

@Entity
class ErrorlogII(var error: String? = null, var errordeate: Date? = null, @ManyToOne var piDevice: PiDevice
                 , var toserver: Boolean? = false

) : En()


@Repository
interface ErrorlogRepoII : JpaRepository<ErrorlogII, Long> {
    fun findByToserver(f: Boolean): List<ErrorlogII>?
}


@Service
class ErrorlogServiceII(val repo: ErrorlogRepoII) {
    fun save(o: ErrorlogII) = repo.save(o)
    fun nottoserver() = repo.findByToserver(false)

}