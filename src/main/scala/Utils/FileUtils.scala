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
import java.io.FileInputStream
import java.io.File
import java.io.FileOutputStream


object FileUtils {
    val storage = StorageOptions.getDefaultInstance().getService()

    def getInputStream(stringPath: String): InputStream = {
        val isGS = stringPath.size > 5 && stringPath.indexOf("gs://") == 0
        if(isGS)
            new ByteArrayInputStream(storage.readAllBytes(getBlobIdFromPath(stringPath)))
        else
            new FileInputStream(new File(stringPath))
    }

    def getOutputStream(stringPath: String): OutputStream = {
        val isGS = stringPath.size > 5 && stringPath.indexOf("gs://") == 0
        if(isGS) {
            val blobId = getBlobIdFromPath(stringPath)
            val blobInfo = BlobInfo.newBuilder(blobId).build()
            val blob = storage.create(blobInfo)
            Channels.newOutputStream(blob.writer())
        } else {
            val file = new File(stringPath)
            file.createNewFile()
            new FileOutputStream(file, false)
        }
    }

    private def getBlobIdFromPath(stringPath: String) : BlobId = {
        val bucket = stringPath.split("/")(2)
        val file = stringPath.split(s"gs://${bucket}/")(1)
        BlobId.of(bucket, file)
    }
}