package lib

import com.gu.mediaservice.lib.auth.{ReadOnly, Internal, Syndication}
import com.gu.mediaservice.model.{Handout, StaffPhotographer}
import controllers.SearchParams
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, FunSpec, Matchers}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class ElasticSearchTest extends FunSpec with BeforeAndAfterAll with Matchers with ElasticSearchHelper with ScalaFutures {

  override def beforeAll {
    ES.ensureAliasAssigned()
    val createTestImages =
      Future.sequence(List(
        createImage(Handout()),
        createImage(StaffPhotographer("Yellow Giraffe", "The Guardian")),
        createImage(Handout()),
        createImageWithSyndicationRights(Handout(), rightsAcquired = true),
        createImageWithSyndicationRights(Handout(), rightsAcquired = false)
      ).map(saveToES))
    Await.ready(createTestImages, 2.seconds)
  }

  describe("ES") {
    it("ES should return only rights acquired pictures for a syndication tier search") {
      val searchParams = SearchParams(tier = Syndication, uploadedBy = Some(testUser))
      val searchResult = ES.search(searchParams)
      whenReady(searchResult) { result =>
        result.total shouldBe 1
      }
    }

    it("ES should return all pictures for internal tier search") {
      val searchParams = SearchParams(tier = Internal, uploadedBy = Some(testUser))
      val searchResult = ES.search(searchParams)
      whenReady(searchResult) { result =>
        result.total shouldBe 5
      }
    }

    it("ES should return all pictures for readonly tier search") {
      val searchParams = SearchParams(tier = ReadOnly, uploadedBy = Some(testUser))
      val searchResult = ES.search(searchParams)
      whenReady(searchResult) { result =>
        result.total shouldBe 5
      }
    }
  }

  override def afterAll {
    Await.ready(cleanTestUserImages(), 2.seconds)
  }
}
