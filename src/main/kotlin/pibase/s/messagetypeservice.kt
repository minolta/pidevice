package me.pixka.kt.pibase.s

import me.pixka.kt.base.s.DefaultService
import me.pixka.kt.base.s.Ds
import me.pixka.kt.pibase.d.MessageType
import me.pixka.kt.pibase.d.MessageTypeRepo
import org.springframework.stereotype.Service

@Service
class MessagetypeService(val r: MessageTypeRepo) : DefaultService<MessageType>() {
    fun findOrCreate(n: String): MessageType? {
        var h = findByName(n)
        if (h == null) {
            var nn = MessageType(n)
            return save(nn)
        }
        return h
    }
}