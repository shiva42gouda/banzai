package com.ge.nola;
import com.ge.nola.BanzaiCfg
import com.ge.nola.BanzaiEvent
import org.jenkinsci.plugins.workflow.cps.CpsClosure2
import org.jenkinsci.plugins.workflow.cps.WorkflowScript

class BanzaiBaseStage {
    WorkflowScript pipeline
    String stageName
    BanzaiCfg cfg
    String validationMessage

    def validate(CpsClosure2 c) {
        validationMessage = c.call()
    }

    def execute(CpsClosure2 c) {
        if (validationMessage) {
            logger validationMessage
            return
        }

        pipeline.stage (stageName) {
            try {
                notify(cfg, [
                    scope: BanzaiEvent.Scope.STAGE,
                    status: BanzaiEvent.Status.PENDING,
                    stage: stageName,
                    message: 'Pending'
                ])
                c.call()
                notify(cfg, [
                    scope: BanzaiEvent.Scope.STAGE,
                    status: BanzaiEvent.Status.SUCCESS,
                    stage: stageName,
                    message: 'Success'
                ])
            } catch (err) {
                echo "Caught: ${err}"
                currentBuild.result = 'FAILURE'
                if (isGithubError(err)) {
                    notify(cfg, [
                        scope: BanzaiEvent.Scope.STAGE,
                        status: BanzaiEvent.Status.FAILURE,
                        stage: stageName,
                        message: 'githubdown'
                    ])
                } else {
                    notify(cfg, [
                        scope: BanzaiEvent.Scope.STAGE,
                        status: BanzaiEvent.Status.FAILURE,
                        stage: stageName,
                        message: 'Failed'
                    ])   
                }
                
                pipeline.error(err.message)
            }
        }
    }
}