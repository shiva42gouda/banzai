#!/usr/bin/env groovy

import java.util.regex.Matcher
import java.util.regex.Pattern

def call(config) {
	
	echo "Environment Selection"
	stage ('Environment Selection'){
		
		if (!config.promoteRepo || !config.stackName) {
			println "'promoteRepo' and 'stackName' are required in your Jenkinsfile when 'promote' = true"
			return
		}
		
		if (config.promoteBranches && env.BRANCH_NAME) {
			Pattern pattern = Pattern.compile(config.promoteBranches)
			println "Inside banch check"
			if (!(env.BRANCH_NAME ==~ pattern)) {
			   println "${env.BRANCH_NAME} does not match the promoteBranches pattern. Skipping Promote"
			   return
			}
		}
		
		env.ENV_OPTION = ''
		timeout(time: 3, unit: 'DAYS') {
			script {
				env.ENV_OPTION = input message: "Select the Environment for Deployment",
						ok: 'Submit',
						parameters: [choice(name: 'Where do you want to deploy the application ?', choices: "QA & PROD\nQA\nPROD\nSkip", description: 'What would you like to do?')]
			}
		}
	}
	if (env.ENV_OPTION == 'Skip') {
		echo "You want to skip deployment!"
		return
	}
	
	if (env.ENV_OPTION.contains('QA')) {
		// Request QA deploy
		echo "Requesting QA deployment"
		stage ('Promote to QA ?'){
			  
			env.DEPLOY_OPTION = ''
			timeout(time: 3, unit: 'DAYS') {
				script {
					env.DEPLOY_OPTION = input message: "Promote to QA ?",
							ok: 'Submit',
							parameters: [choice(name: 'Deployment Request', choices: "Deploy\nSkip", description: 'What would you like to do?')]
				}
			}
		}
		if(env.DEPLOY_OPTION == 'Skip') {
			echo "You want to skip QA deployment!"
			return
		}
		else if(env.DEPLOY_OPTION == 'Deploy') {
			echo "You want to deploy in QA!"		
			runPromote(config, 'qa')
			echo "Deployed to QA!"
		}
	}
	
	if (env.ENV_OPTION.contains('PROD')) {
		// Request PROD deploy
		echo "Requesting PROD deployment"
		stage ('Request PROD Deployment ?'){
			env.DEPLOY_OPTION = ''
			timeout(time: 5, unit: 'DAYS') {
				script {
					env.DEPLOY_OPTION = input message: "Request deployment to PROD ?",
							ok: 'Submit',
							parameters: [choice(name: 'Deployment Request', choices: "Email Roger\nSkip", description: 'What would you like to do?')]
				}
			}
		}
		if(env.DEPLOY_OPTION == 'Skip') {
			echo "You want to skip PROD deployment!"
		}
		else if(env.DEPLOY_OPTION == 'Email Roger') {			
			//submitter: '210026212' //Roger's SSO // ,Roger.Laurence@ge.com
			echo "You want to request PROD deployment!"
			stage ('Promote to PROD ?'){
				// Remove the app name hardcoding
				mail from: "JenkinsAdmin@ge.com",
					 to: config.approverEmail,
					 subject: "${config.stackName} application stack awaiting PROD deployment approval",
					 body: "Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' is waiting for PROD approval.\n\nPlease click the link below to proceed.\n${env.BUILD_URL}input/"
	  
				env.DEPLOY_OPTION = ''
				timeout(time: 7, unit: 'DAYS') {
					script {
						env.DEPLOY_OPTION = input message: "Deploy Config Reviewer to PROD?",
								ok: 'Deploy to PROD!',
								parameters: [choice(name: 'Deployment Action', choices: "Deploy\nSkip", description: 'What would you like to do?')],
								submitter: config.approverSSO //Roger's SSO 210026212
					}
				}
			}
  
			if(env.DEPLOY_OPTION == 'Skip') {
				script.echo "You want to reject PROD deployment!"
			}
			else if(env.DEPLOY_OPTION == 'Deploy') {
				echo "You want to deploy in PROD!"  
				runPromote(config, 'prod')
				echo "Deployed to PROD!"
			}
		}
	}
}
