/**
  * Copyright 2023 SDKMAN!
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package io.sdkman.vendor.release

import com.typesafe.config.{Config, ConfigFactory}

trait Configuration {

  lazy val config: Config = ConfigFactory.load()

  lazy val serviceHost: String = config.getString("service.host")

  lazy val servicePort: Int = config.getInt("service.port")

  lazy val serviceToken: String = config.getString("service.token")

  lazy val serviceAdminConsumer: String = config.getString("service.adminConsumer")

  private lazy val stateApiProtocol: String = config.getString("state-api.url.protocol")

  private lazy val stateApiHost: String = config.getString("state-api.url.host")

  lazy val stateApiUrl: String = {
    if (stateApiHost == "localhost") {
      s"$stateApiProtocol://$stateApiHost:8080"
    } else {
      s"$stateApiProtocol://$stateApiHost"
    }
  }

  lazy val stateApiBasicAuthUsername: String = config.getString("state-api.basic-auth.username")

  lazy val stateApiBasicAuthPassword: String = config.getString("state-api.basic-auth.password")
}
