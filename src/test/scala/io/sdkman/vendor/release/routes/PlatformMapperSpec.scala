package io.sdkman.vendor.release.routes

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class PlatformMapperSpec extends AnyWordSpec with Matchers {

  "PlatformMapper" should {

    "map MAC_OSX to MAC_X64" in {
      PlatformMapper.mapToStatePlatform("MAC_OSX") shouldBe "MAC_X64"
    }

    "map MAC_ARM64 to MAC_ARM64" in {
      PlatformMapper.mapToStatePlatform("MAC_ARM64") shouldBe "MAC_ARM64"
    }

    "map LINUX_64 to LINUX_X64" in {
      PlatformMapper.mapToStatePlatform("LINUX_64") shouldBe "LINUX_X64"
    }

    "map LINUX_32 to LINUX_X32" in {
      PlatformMapper.mapToStatePlatform("LINUX_32") shouldBe "LINUX_X32"
    }

    "map LINUX_ARM32HF to LINUX_ARM32HF" in {
      PlatformMapper.mapToStatePlatform("LINUX_ARM32HF") shouldBe "LINUX_ARM32HF"
    }

    "map LINUX_ARM32SF to LINUX_ARM32SF" in {
      PlatformMapper.mapToStatePlatform("LINUX_ARM32SF") shouldBe "LINUX_ARM32SF"
    }

    "map LINUX_ARM64 to LINUX_ARM64" in {
      PlatformMapper.mapToStatePlatform("LINUX_ARM64") shouldBe "LINUX_ARM64"
    }

    "map WINDOWS_64 to WINDOWS_X64" in {
      PlatformMapper.mapToStatePlatform("WINDOWS_64") shouldBe "WINDOWS_X64"
    }

    "map UNIVERSAL to UNIVERSAL" in {
      PlatformMapper.mapToStatePlatform("UNIVERSAL") shouldBe "UNIVERSAL"
    }

    "map unknown platform to UNIVERSAL with warning" in {
      PlatformMapper.mapToStatePlatform("UNKNOWN_PLATFORM") shouldBe "UNIVERSAL"
    }

    "map null platform to UNIVERSAL" in {
      PlatformMapper.mapToStatePlatform(null) shouldBe "UNIVERSAL"
    }

    "map empty string to UNIVERSAL" in {
      PlatformMapper.mapToStatePlatform("") shouldBe "UNIVERSAL"
    }
  }
}