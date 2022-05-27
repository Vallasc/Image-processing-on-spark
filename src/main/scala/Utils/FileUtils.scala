package Utils

import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Paths
import java.net.URI
import java.nio.file.Files
import com.google.cloud.storage.StorageOptions
import com.google.cloud.storage.Blob
import com.google.cloud.storage.BlobId
import java.io.ByteArrayInputStream


object FileUtils {
    val storage = StorageOptions.getDefaultInstance().getService()

    def getInputStream(stringPath: String, isGS: Boolean = false): InputStream = {
        if(isGS) {
            val blobId = BlobId.of("scala-image-processing-bucket", "data/nike_noisy.png")
            val content = storage.readAllBytes(blobId)
            new ByteArrayInputStream(content)
        } else {
            val path = Paths.get(URI.create(stringPath))
            Files.newInputStream(path)
        }
    }

    def getOutputStream(stringPath: String): OutputStream ={
        val path = Paths.get(URI.create(stringPath))
        if(!Files.exists(path))
            Files.createFile(path)
        Files.newOutputStream(path)
    }
}