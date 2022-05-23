package org.nitb.orchestrator.utils.subscriptions

import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPSClient
import org.nitb.orchestrator.subscription.SubscriptionReceiver
import org.nitb.orchestrator.subscription.delivery.DeliveryPeriodicalSubscription
import org.nitb.orchestrator.utils.entities.FtpEntity
import java.io.File
import java.io.FileOutputStream
import kotlin.io.path.createTempDirectory

class FtpPeriodicalFileWatcher(
    name: String,
    delay: Long,
    initialDelay: Long = 0,
    receivers: List<SubscriptionReceiver> = listOf(),
    timeout: Long = -1,
    description: String? = null,
    private val ftpServer: String,
    private val ftpPort: Int = 21,
    private val ftpUser: String,
    private val ftpPassword: String,
    private val ftpSsl: Boolean = false,
    private val remoteVerification: Boolean = true,
    private val folders: Array<String> = arrayOf(),
    private val filePatterns: Array<String> = arrayOf(),
    private val deleteNonMatched: Boolean = false,
    private val limit: Int = -1,
    private val recursive: Boolean = false
): DeliveryPeriodicalSubscription<Array<FtpEntity>>(name, delay, initialDelay, receivers, timeout, description) {

    override fun onEvent(sender: String, input: Unit): Array<FtpEntity> {
        val iClient: FTPClient = if (ftpSsl) FTPSClient() else FTPClient()
        iClient.connect(ftpServer, ftpPort)
        iClient.login(ftpUser, ftpPassword)
        iClient.isRemoteVerificationEnabled = remoteVerification
        if (ftpSsl)
            (iClient as FTPSClient).authValue = "TLS"
        iClient.enterLocalPassiveMode()
        iClient.doCommand("PBSZ", "0")
        iClient.doCommand("PROT", "P")
        iClient.enterLocalPassiveMode()
        iClient.setFileType(FTP.BINARY_FILE_TYPE)
        val client = iClient

        val tmpFolder = createTempDirectory("ftp-tmp").toFile()
        val filesByFolder = mutableMapOf<String, MutableList<FtpEntity>>()

        for (folder in folders) {
            val files = client.listFiles(folder)
            val folders = client.listDirectories()

            filesByFolder.computeIfAbsent(folder) { mutableListOf() }.addAll(files.map {
                val localFile = File(tmpFolder, it.name)
                val entity = FtpEntity(it.name, folder, localFile.path)
                FileOutputStream(localFile).use { out ->
                    client.retrieveFile("$folder/${entity.name}", out)
                }
                entity
            }.toList())

        }

        return arrayOf()
    }

}