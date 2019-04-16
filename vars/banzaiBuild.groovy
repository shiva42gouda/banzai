#!/usr/bin/env groovy

import java.util.regex.Matcher
import java.util.regex.Pattern

// named banzaiBuild to avoid collision with existing 'build' jenkins pipeline plugin
def call(config) {
    stage ('Build') {

      if (config.buildBranches) {
        Pattern pattern = Pattern.compile(config.buildBranches)

        if (!(BRANCH_NAME ==~ pattern)) {
          logger "${BRANCH_NAME} does not match the buildBranches pattern. Skipping Build"
          return
        }
      }

      runScript(config, "buildScriptFile", "buildScript", [BRANCH_NAME])
    }
}