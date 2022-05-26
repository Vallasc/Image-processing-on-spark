package Utils

import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Paths
import java.net.URI
import java.nio.file.Files


object FileUtils {
    def getInputStream(stringPath: String): InputStream = {
        val path = Paths.get(URI.create(stringPath))
        Files.newInputStream(path)
    }

    def getOutputStream(stringPath: String): OutputStream ={
        val path = Paths.get(URI.create(stringPath))
        if(!Files.exists(path))
            Files.createFile(path)
        Files.newOutputStream(path)
    }
}