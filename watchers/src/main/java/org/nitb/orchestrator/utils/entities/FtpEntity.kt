package org.nitb.orchestrator.utils.entities

import org.nitb.orchestrator.annotations.NoArgsConstructor
import java.io.Serializable

@NoArgsConstructor
class FtpEntity(
    val name: String,
    val ftpDir: String,
    val location: String
): Serializable {

}