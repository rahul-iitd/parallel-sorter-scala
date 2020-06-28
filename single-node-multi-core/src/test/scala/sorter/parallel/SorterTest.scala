package sorter.parallel

import java.io.{File, PrintWriter}
import java.nio.file.Files
import org.scalacheck.{Gen, Prop, Properties}
import scala.io.Source
import scala.util.Random

object SorterSpec extends Properties("Sorter") {
  private val countGen = Gen.choose(0, 1000)
  private val parallelismGen = Gen.choose(1, 4)
  private val chunkSizeGen = Gen.choose(1, 1100)

  property("sortNonEmpty") =
    Prop.forAll(countGen, parallelismGen, chunkSizeGen) {
      (numOfInts, numOfWorkers, intsPerWorker) =>
      val ints = 1.to(numOfInts).map {_ => Random.nextLong }.toList
      val inFile = File.createTempFile("sort", "test")
      val tempDir = Files.createTempDirectory("snmc").toFile
      val writer = new PrintWriter(inFile)
      try
        ints.map(_.toString).foreach(writer.println)
      finally
        writer.close
      val outFile = File.createTempFile("sort", "test")
      val sorter = new Sorter(numOfWorkers, intsPerWorker, tempDir.toString)
      sorter.sort(inFile.getAbsolutePath, outFile.getAbsolutePath)
      val result = Source.fromFile(outFile).getLines.map(_.toLong).toList
      inFile.delete
      outFile.delete
      tempDir.delete

      ints.sorted == result
  }
}
