package Schach

import Schach.aview.TuiITSpec
import Schach.controller.controllerComponent.controllerBaseImpl.ControllerITSpec
import com.dimafeng.testcontainers.{DockerComposeContainer, ExposedService, ForAllTestContainer}
import org.scalatest.Suite
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.testcontainers.containers.wait.strategy.Wait

import java.io.File
import java.time.Duration
import java.time.temporal.ChronoUnit

class ITRunner extends AnyWordSpec with Matchers with ForAllTestContainer {
  override val container: DockerComposeContainer = DockerComposeContainer(
    new File("src/it/resources/docker-compose-test.yml"),
    exposedServices = Seq(
      ExposedService("mongodb", 27017, Wait.forListeningPort()),
      ExposedService("model", 8082, Wait.forListeningPort().withStartupTimeout(Duration.of(2, ChronoUnit.MINUTES))),
      ExposedService("persistence", 8081, Wait.forListeningPort().withStartupTimeout(Duration.of(2, ChronoUnit.MINUTES)))
    )
  )

  override def nestedSuites: IndexedSeq[Suite] = Vector(
    new ControllerITSpec(container),
    new TuiITSpec(container)
  )
}
