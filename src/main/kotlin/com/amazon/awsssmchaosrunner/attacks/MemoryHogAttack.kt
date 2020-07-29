package com.amazon.awsssmchaosrunner.attacks

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement
import mu.KotlinLogging
import java.time.Duration

private val log = KotlinLogging.logger { }

class MemoryHogAttack constructor(
    override val ssm: AWSSimpleSystemsManagement,
    override val configuration: SSMAttack.Companion.AttackConfiguration
) : SSMAttack {
    private val vmWorkers = 8
    override val documentContent: String
        get() {
            val documentHeader = "---\n" +
                "schemaVersion: '2.2'\n" +
                "description: Hog virtual memory on the instance\n" +
                "mainSteps:\n" +
                "- action: aws:runShellScript\n" +
                "  name: ${this.documentName()}\n" +
                "  inputs:\n" +
                "    runCommand:\n"
            val chaos = "    - sudo yum -y install stress-ng\n" +
                "    - stress-ng --vm $vmWorkers --vm-bytes ${configuration.otherParameters["virtualMemoryPercent"]}% -t ${Duration.parse(configuration.duration).seconds}s\n"
            val scheduledChaosRollback = "    - echo \"sudo yum -y remove stress-ng\" | " +
                "at now + ${Duration.parse(configuration.duration).toMinutes() + 1} minutes\n"
            val documentContent = "$documentHeader$scheduledChaosRollback$chaos"
            log.info("Chaos Document Content:\n$documentContent")

            return documentContent
        }
}