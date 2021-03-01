package io.mehow.nark

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.throwable.shouldHaveMessage

internal class NarkConfiguratorSpec : DescribeSpec({
  describe("Nark configurator") {
    it("can configure only one") {
      NarkConfigurator.configure()

      val exception = shouldThrowExactly<IllegalStateException> { NarkConfigurator.configure() }
      exception shouldHaveMessage "Nark can be configured only once"
    }
  }
})
