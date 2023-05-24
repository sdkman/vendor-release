/**
  * Copyright 2020 Marco Vermeulen
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

import com.typesafe.config.ConfigFactory

trait Configuration {

  lazy val config = ConfigFactory.load()

  lazy val serviceHost = config.getString("service.host")

  lazy val servicePort = config.getInt("service.port")

  lazy val serviceToken = config.getString("service.token")

  lazy val serviceAdminConsumer = config.getString("service.adminConsumer")

  lazy val jdbcUrl = config.getString("jdbc.url")

  lazy val jdbcUser = config.getString("jdbc.username")

  lazy val jdbcPassword = config.getString("jdbc.password")
}
