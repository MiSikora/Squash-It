package io.mehow.nark

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll

internal class CredentialsSpec : DescribeSpec({
  describe("Credentials") {
    it("do not expose values when printed") {
      checkAll<String, String> { id, secret ->
        val credentials = Credentials(id, secret)

        credentials.toString() shouldBe "Credentials(██)"
      }
    }
  }
})
