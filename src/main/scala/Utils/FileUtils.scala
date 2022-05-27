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
import java.nio.channels.Channels
import com.google.cloud.storage.BlobInfo


object FileUtils {
    val storage = StorageOptions.getDefaultInstance().getService()

    def getInputStream(stringPath: String): InputStream = {
        val isGS = stringPath.size > 5 && stringPath.indexOf("gs://") == 0
        if(isGS) {
            val content = storage.readAllBytes(getBlobIdFromPath(stringPath))
            new ByteArrayInputStream(content)
        } else {
            val path = Paths.get(URI.create(stringPath))
            Files.newInputStream(path)
        }
    }

    def getOutputStream(stringPath: String): OutputStream = {
        val isGS = stringPath.size > 5 && stringPath.indexOf("gs://") == 0
        if(isGS) {
            val blobId = getBlobIdFromPath(stringPath)
            //val blob = storage.get(blobId)
            val blobInfo = BlobInfo.newBuilder(blobId).build()
            val blob = storage.create(blobInfo)
            Channels.newOutputStream(blob.writer())
        } else {
            val path = Paths.get(URI.create(stringPath))
            if(!Files.exists(path))
                Files.createFile(path)
            Files.newOutputStream(path)
        }
    }

    private def getBlobIdFromPath(stringPath: String) : BlobId = {
        val bucket = stringPath.split("/")(2)
        val file = stringPath.split(s"gs://${bucket}/")(1)
        BlobId.of(bucket, file)
    }
}