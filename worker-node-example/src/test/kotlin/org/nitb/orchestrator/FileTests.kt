package org.nitb.orchestrator

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import org.junit.jupiter.api.Test
import org.nitb.orchestrator.subscriptions.ftp.GameInfo
import java.io.FileInputStream

class FileTests {

    @Test
    fun testXml() {
        val mapper = XmlMapper()

        lateinit var gameInfo: GameInfo

        FileInputStream("D:\\docker-volumes\\ftpserver\\data\\INPUT\\A\\example.xml").use {
            gameInfo = mapper.readValue(it, GameInfo::class.java)
        }

        println(gameInfo.toString())
    }

}