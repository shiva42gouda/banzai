![Banzai](https://i.imgur.com/QKdnoZ4.png)

Banzai
========
Banzai started as one team's solution to CICD and has grown to a full-featured CICD offering. All of Banazai's features are implemented with genericity and flexibility in mind so if something does not meet your needs please open a Git issue and let us know!

* [Configuration Overview](#configuration-overview)
* [BanzaiCfg](#banzaicfg)
  * [appName](#appName)
  * [sshCreds](#sshCreds)
  * [timeout](#timeout)
  * [throttle](#throttle)
  * [filterSecrets](#filterSecrets)
  * [skipScm](#skipSCM)
  * [debug](#debug)
  * [gitTokenId](#gitTokenId)
  * [proxy](#proxy)
  * [noProxy](noproxy)
  * [cleanWorkspace](#cleanworkspace)
  * [build](#build)
  * [publish](#publish)
  * [deploy](#deploy)
  * [itegrationTests](#integrationTests)
  * [tools](#tools)
  * [flowdock](#flowdock)
  * [email](email)
  * [notifications](#notifications)
  * [vulnerabilityScans](#vulnerabilityScans)
  * [vulnerabilityAbortOnError](#vulnerabilityAbortOnError)
  * [qualityScans](#qualityScans)
  * [qualityAbortOnError](#qualityAbortOnError)
  * [downstreamBuilds](#downstreamBuilds)
  * [gitOpsTrigger and gitOps](#gitopstrigger-and-gitops)
    * [GitOps Configuration](#gitops-configuration)
  * [stages](#stages)
  * [hooks](#hooks)
* [Coverity](#coverity)
* [BanzaiUserData](#banzaiuserdata)
* [Notifications and Events](#notifications-and-events)
* [Proxies](#proxies)

## Configuration Overview
Basic Jenkinsfile
```
banzai([
  appName: 'my-app',
  build: [ /.*/: [:] ],                                                  // build all branches (calls build.sh)
  publish: [ /tag\-(.*)|develop/: [script : 'scripts/my-publish.sh'] ],  // publish tags and develop branch changes. Execute scripts/my-publish.sh instead of the default publish.sh
  deploy: [ /tag\-(.*)|develop/: [:] ],                                  // deploy tags and develop branch changes (calls deploy.sh)
])
```

Exhaustive List of BanzaiCfg properties
```
@Library('Banzai@1.0.0') _ // only necessary if configured as a 'Global Pipeline Library'. IMPORTANT: the _ is required after @Library. 
banzai([
    appName: 'config-reviewer-server',
    sshCreds: ['cred1', 'cred2'],
    timeout: 30,
    throttle: ['my-project'],
    filterSecrets: [
      /develop/: [
          file: 'settings.xml',
          label: 'myPass',
          secretId: 'my-jenkins-secret-id'
      ]
    ],
    skipSCM: true,
    debug: false,
    gitTokenId: 'sweeney-git-token',
    proxy: [
      host: 'proxyhost',
      port: '80'
    ],
    cleanWorkspace: [
      pre: true,
      post: true
    ],
    build: [
      /.*/ : [:]
    ],
    publish: [
      /.*/ : [
        shell: 'scripts/my-publish-script.sh'
      ]
    ],
    deploy: [
      /.*/ : [
        shell: 'scripts/my-deploy-script.sh'
      ]
    ],
    integrationTests: [
      /.*/ : [
        shell: 'scripts/my-it-script.sh'
      ]
    ],
    tools: [
      jdk: 'jdk 10.0.1',
      nodejs: '8'
    ],
    flowdock: [
      banzaiFlow: [
        credId: 'banzai-flowtoken',
        author: [
          name: 'Banzai',
          avatarUrl: 'https://github.com/avatars/u/55576?s=400&u=700c7e70356d1f5a679908c1d7c7e5bf8e2beab6',
          email: 'banzai@banzai.com'
        ]
      ]
    ],
    email: [
      addresses: [
        tom: 'tom@jerry.com',
        banzai: 'banzai@banzai.com'
      ],
      groups: [
        everyone: ['tom', 'banzai'],
      ]
    ],
    notifications: [
      flowdock: [
        /.*/: [
          'banzaiFlow': ['.*']
        ]
      ],
      email: [
        /.*/: [
          groups: [
            'everyone': ['PIPELINE:(FAILURE|SUCCESS)']
          ],
          individuals: [
            'tom': [
              'PIPELINE:PENDING',
              'VULNERABILITY:.*',
              'QUALITY:.*'
            ]
          ]
        ]
      ]
    ],
    vulnerabilityScans = [
      /develop|master/: [
        [
          type: 'checkmarx',
          credId: 'checkmarx-cred-id',
          preset: '17',
          teamUUID: 'your-checkmarx-team-uuid',
          abortOnError: false
        ],
        [
          type: 'coverity',
          credId: 'coverity-auth-key-file',
          toolId: 'coverity-2018.12',
          serverHost: 'my.coverity.server.com',
          serverPort: '443',
          buildCmd: 'mvn -s ./settings.xml clean install -U',
          projectName: 'your-coverity-project-name',
          abortOnError: true
        ]
      ]
    ],
    vulnerabilityAbortOnError = false,
    qualityScans: [
      /develop|master/: [
        [
          type: 'sonar',
          serverUrl: 'https://my-sonar.com'
          credId: 'sonar-auth-token-id'
        ]
      ]
    ],
    qualityAbortOnError = false,
    downstreamBuilds: [
      /develop/: [
        [
          id: 'my-job',
          job: '/YOUR_PROJECT_FOLDER/Build/your-project/branch',
          optional: true,
          wait: true
        ],
        [
          id: 'my-parallel-job',
          job: '/YOUR_PROJECT_FOLDER/Build/your-project/branch',
          parallel: true
        ],
        [
          id: 'my-parallel-job-2',
          job: '/YOUR_PROJECT_FOLDER/Build/your-project/branch',
          parallel: true,
          propagate: true
        ],
        [
          id: 'my-serial-job',
          job: '/YOUR_PROJECT_FOLDER/Build/your-project/branch'
        ]
      ]
    ],
    gitOpsTrigger: [
      /develop|tag-*/ : [
        jenkinsJob: '/Banzai/GitOps/master',
        stackId: 'dib'
      ]
    ],
    gitOps: [
      autoDeploy: [
        /develop/ : 'dev'
      ],
      envs: [
        'dev' : [:],
        'qa' : [
            approvers: ['<jenkins-id>'],
            watchers: ['<jenkins-id>']
        ]
      ]
    ],
    stages: [
      [ name: 'build' ],
      [
        name: 'My Arbitrary Stage',
        steps: [
          /.*/: [
            [
              groovy: { logger "YO I RAN A CLOSURE FROM A CUSTOM STAGE!" }
            ], 
            [
              shell: 'customStageScript.sh'
            ]
          ]
        ]
      ]
    ]
  ]
])
```

## BanzaiCfg
The BanzaiCfg is the object passed to the `banzai()` entrypoint in your Jenkinsfile. The Map that you pass in is mapped to typed [BanzaiCfg](src/com/github/banzaicicd/cfg/BanzaiCfg.groovy) objects. The BanzaiCfg properties are referenced throughout the following documentation.

### appName
**String** <span style="color:red">*</span>  
Used throughout the pipeline in various contexts to indentify the name of the app/service/lib/etc being processed.

### sshCreds
**List\<String>**  
A list of id's that map to Jenkins Credentials of type 'SSH Username with private key'. When configured, the ssh credentials will be available for the duration of the pipeline run.

### timeout
**int** *default: 30*  
Time in minutes the pipeline must complete with-in before being aborted

### throttle
**List\<String>**  
The `throttle` property leverages the [Throttle Concurrent Builds Plugin](https://github.com/jenkinsci/throttle-concurrent-builds-plugin) to provide a way of restricting the number of concurrent builds belonging to a particular 'throttle category' at any given time. This is useful when you have builds that require a large number of resources.
1. Install the [Throttle Concurrent Builds Plugin](https://github.com/jenkinsci/throttle-concurrent-builds-plugin), 
2. Configure the plugin (create a throttle group)
3. Update your BanzaiCfg
```
throttle = ['my-throttle-group']
```

### filterSecrets
**[BanzaiFilterSecretsCfg](src/com/github/banzaicicd/cfg/BanzaiFilterSecretsCfg.groovy)**  
If your pipeline requires secret values exist in files but you do not want to store them in version control, `filterSecrets` can help.

1. Add a credential to Jenkins of type 'secret'. (remember the id)
2. Update the file in your repository that you would like to have the secret injected into.
```
properties:
  username: 'myuser'
  password: '${banzai:myPass}'
```
3. Update the BanzaiCfg to include the `filterSecrets` property
```
filterSecrets: [
  /develop/: [
      file: 'settings.xml',   // the filePath to filter relative to the jenkins WORKSPACE root
      label: 'myPass',        // should appear in the file in the format ${banzai:myPass}
      secretId: 'my-secret'   // the id of the secret on Jenkins
  ]
]
```

### skipSCM
**Boolean** <i>default: false</i>  
When true, will skip cloning down the repsitory which triggered the pipeline.

### debug
**Boolean** <i>default: false</i>  
When true, adds additional logs to Jenkins console

### gitTokenId
**String** <span style="color:red">*</span>  
The id of a Jenkins Credential of type 'secret' containing a Github Personal Access Token. Currently used for updating PR statuses and by the [downstreamBuilds](#downstreamBuilds) feature. The token must include at a minimum the entire `repo` scope. 

### proxy
**[BanzaiProxyCfg](src/com/github/banzaicicd/cfg/BanzaiProxyCfg.groovy)**  
By default, Banzai will automatically populate the [BanzaiProxyCfg](src/com/github/banzaicicd/cfg/BanzaiProxyCfg.groovy) with the values set in the `Manage Jenkins -> Manage Plugins -> Advanced` of your Jenkins instance and if these are not available fall back to `env.http_proxy`, `env.HTTP_PROXY`, `env.no_proxy` and `env.NO_PROXY` in the environment. If for some reason you would like to override your Jenkins proxy settings you may define the `proxy` property of the BanzaiCfg yourself.
ex)
```
proxy: [
  host: 'proxyhost',
  port: '80'
]
```

### noProxy
**String**  
A comma-delimted list of hosts that the proxies should not apply to. Leave empty if you would like the pipeline to default to available `env.no_proxy` and `env.NO_PROXY` env vars.

### cleanWorkspace
**[BanzaiCleanWorkspaceCfg](src/com/github/banzaicicd/cfg/BanzaiCleanWorkspaceCfg.groovy)**  
Ensure a cleaned WORKSPACE prior to pipeline run or clean up the WORKSPACE after a pipeline run.  
ex)
```
cleanWorkspace : [
  pre: true,
  post: true
]
```

### build
**Map<String,[BanzaiStepCfg](src/com/github/banzaicicd/cfg/BanzaiStepCfg.groovy)>**  
Configures the built-in 'Build' stage of Banzai. The config is branch-based meaning that the keys of the supplied Map should be regex patterns matching the branch that each [BanzaiStepCfg](src/com/github/banzaicicd/cfg/BanzaiStepCfg.groovy) should apply to. To accept the defaults pass an empty object (`[:]`) as your [BanzaiStepCfg](src/com/github/banzaicicd/cfg/BanzaiStepCfg.groovy).
ex)
```
build: [
  /.*/ : [:]  // defaults to [ shell: 'build.sh' ]
],
```

### publish
**Map<String,[BanzaiStepCfg](src/com/github/banzaicicd/cfg/BanzaiStepCfg.groovy)>**  
Configures the built-in 'Publish' stage of Banzai. The config is branch-based meaning that the keys of the supplied Map should be regex patterns matching the branch that each [BanzaiStepCfg](src/com/github/banzaicicd/cfg/BanzaiStepCfg.groovy) should apply to. To accept the defaults pass an empty object (`[:]`) as your [BanzaiStepCfg](src/com/github/banzaicicd/cfg/BanzaiStepCfg.groovy).
ex)
```
publish: [
  /.*/ : [:]  // defaults to [ shell: 'publish.sh' ]
],
```

### deploy
**Map<String,[BanzaiStepCfg](src/com/github/banzaicicd/cfg/BanzaiStepCfg.groovy)>**  
Configures the built-in 'Deploy' stage of Banzai. The config is branch-based meaning that the keys of the supplied Map should be regex patterns matching the branch that each [BanzaiStepCfg](src/com/github/banzaicicd/cfg/BanzaiStepCfg.groovy) should apply to. To accept the defaults pass an empty object (`[:]`) as your [BanzaiStepCfg](src/com/github/banzaicicd/cfg/BanzaiStepCfg.groovy).
ex)
```
deploy: [
  /.*/ : [:]  // defaults to [ shell: 'deploy.sh' ]
],
```

### integrationTests
**Map<String,[BanzaiIntegrationTestsCfg](src/com/github/banzaicicd/cfg/BanzaiIntegrationTestsCfg.groovy)>**  
Extends the [BanzaiStepCfg](src/com/github/banzaicicd/cfg/BanzaiStepCfg.groovy) and adds additional properties for `xvfb` and `xvfbScreen`. *note xvfb features require that the [Xvfb Plugin](https://wiki.jenkins.io/display/JENKINS/Xvfb+Plugin) is installed on the Jenkins instance.*

### tools
**[BanzaiToolsCfg](src/com/github/banzaicicd/cfg/BanzaiToolsCfg.groovy)**  
The `tools` property allows you to target specific items from your Jenkins Global Tool Configuration ie) `jdk`, `nodejs`. Tools configured via the [BanzaiToolsCfg](src/com/github/banzaicicd/cfg/BanzaiToolsCfg.groovy) object will be in scope for the duration of the pipeline run. 

### flowdock
**Map<String, [BanzaiFlowdockCfg](src/com/github/banzaicicd/cfg/BanzaiFlowdockCfg.groovy)>**  
A branch-based configuration providing flowdock cfgs that are available for reference in the [notifications](#notifications) cfg

### email
**Map<String, [BanzaiEmailCfg](BanzaiEmailCfg)>**  
A branch-based configuration providing individual emails and groups that are available for reference in the [notifications](#notifications) cfg

### notifications
**[BanzaiNotificationsCfg](src/com/github/banzaicicd/cfg/BanzaiNotificationsCfg.groovy)**  
Determines when/how notifications are sent and who recieves those notifications. the `notifications` property works in tandem with the [email](src/com/github/banzaicicd/cfg/BanzaiEmailCfg.groovy) and [flowdock](/src/com/github/banzaicicd/cfg/BanzaiFlowdockCfg.groovy) properties. See [Notifications and Events](#notifications-and-events) for more.  
ex)
```
flowdock: [
  banzaiFlow: [
    credId: 'banzai-flowtoken',
    author: [
      name: 'Banzai',
      avatarUrl: 'https://github.com/avatars/u/55576?s=400&u=700c7e70356d1f5a679908c1d7c7e5bf8e2beab6',
      email: 'banzai@banzai.com'
    ]
  ]
],
email: [
  addresses: [
    tom: 'tom@jerry.com',
    banzai: 'banzai@banzai.com'
  ],
  groups: [
    everyone: ['tom', 'banzai'],
  ]
],
notifications: [
  flowdock: [
    /.*/: [ // this config applies to all branches
      'banzaiFlow': ['.*'] // all branches will use the flowdock 'banzaiFlow' config and publish all notification events.
    ]
  ],
  email: [
    /.*/: [ // this config applies to all branches
      groups: [
        'everyone': ['PIPELINE:(FAILURE|SUCCESS)'] // everyone will get an email for pipeline success and failure
      ],
      individuals: [
        'tom': ['PIPELINE:PENDING'] // only tom will get emails about pending pipelines
      ]
    ]
  ]
]
```

### vulnerabilityScans
**Map<String, List<[BanzaiVulnerabilityCfg](src/com/github/banzaicicd/cfg/BanzaiVulnerabilityCfg.groovy)>>**  
Banzai supports `checkmarx` and `coverity` Vulnerability Scans. The config is branch-based meaning that the keys of the supplied Map should be regex patterns matching the branch that each [BanzaiVulnerabilityCfg](src/com/github/banzaicicd/cfg/BanzaiVulnerabilityCfg.groovy) should apply to.
ex)
```
vulnerabilityScans = [
  /develop|master/: [
    [
      type: 'checkmarx',
      credId: 'ge-checkmarx',               // jenkins credential containing user/pass for checkmarx
      preset: '17',                         // defaults to '17'
      teamUUID: 'your-checkmarx-team-uuid'
    ],
    [
      type: 'coverity',
      credId: 'coverity-auth-key-file',     // jenkins credId of type 'file' representing your users authentication key (exported from coverity server UI)
      toolId: 'coverity-2018.12',           // the id given to Coverity i the Jenkins Global Tool installation
      serverHost: 'my.coverity.server.com',
      serverPort: '443',
      buildCmd: 'mvn -s ./settings.xml clean install -U', // the build command coverity should wrap. alternatively, you can leverage banzai BanzaiUserData. see BanzaiUserData section of README
      projectName: 'your-coverity-project-name'
    ]
  ]
]
```

### vulnerabilityAbortOnError
**Boolean**  
Aborts the pipeline if any of the Vulnerability Scans throw an error during execution

### qualityScans
**Map<String, List<[BanzaiQualityCfg](src/com/github/banzaicicd/cfg/BanzaiQualityCfg.groovy)>>**  
Banzai supports `sonar` Quality Scans. The config is branch-based meaning that the keys of the supplied Map should be regex patterns matching the branch that each [BanzaiQualityCfg](src/com/github/banzaicicd/cfg/BanzaiQualityCfg.groovy) should apply to.
ex)
```
qualityScans: [
  /develop|master/: [
    [
      type: 'sonar',
      serverUrl: 'https://my.sonar.server.com'
      credId: 'sonar-auth-token-id'        // jenkins credential (of type secret) containing a sonar server auth token
    ]
  ]
]
```

### qualityAbortOnError
**Boolean**  
Aborts the pipeline if any of the Quality Scans throw an error during execution

### downstreamBuilds
**Map<String, List<[BanzaiDownstreamBuildCfg](src/com/github/banzaicicd/cfg/BanzaiDownstreamBuildCfg.groovy)>>**  
Banzai allows you to execute additional builds on completion of a pipeline. These 'Downstream Builds' can be optional or required. In the event that they are required they will always run upon success of a build. If they are marked as 'optional' the Pull Request must contain a label in the format `build:<buildId>`. 
ex) running a downstream job where the parent job depends on the result.
```
downstreamBuilds: [
  /develop/: [
    [
      id: 'my-job',
      job: '/YOUR_PROJECT_FOLDER/Build/your-project/branch',
      propagate: true
    ]
  }
```
#### Parallel Downsteam Build Behavior
When Banzai encounters 1 or more BanzaiDownstreamBuildCfg's that contain `parallel: true` listed sequentially, Banzai will execute those builds in parallel and not wait for them to complete. There are 3 scenarios where the parent build will wait for parallel builds to complete:
1. the parallel build also has `wait: true`
2. the parallel build also has `propagate: true`
3. there is one or more non-parallel builds defined after the parallel build(s) that need to be executed once the parallel build(s) complete.
ex)
The following example is configured as follows
1. 'my-job' would only run if the pull-request that kicked off the parent job included the label 'build:my-job' because `optional: true`. If it runs it would block the parent job until it completes because `wait: true`.
2. 'my-parallel-job' and 'my-parallel-job-2' would execute in parallel. 'my-parallel-job-2' includes `propagate: true` and therefor blocks the parent job as it depends on the result of 'my-parallel-job-2'
3. 'my-serial-job' would run after both parallel jobs complete.
```
downstreamBuilds: [
  /develop/: [
    [
      id: 'my-job',
      job: '/YOUR_PROJECT_FOLDER/Build/your-project/branch',
      optional: true,
      wait: true
    ],
    [
      id: 'my-parallel-job',
      job: '/YOUR_PROJECT_FOLDER/Build/your-project/branch',
      parallel: true
    ],
    [
      id: 'my-parallel-job-2',
      job: '/YOUR_PROJECT_FOLDER/Build/your-project/branch',
      parallel: true,
      propagate: true
    ],
    [
      id: 'my-serial-job',
      job: '/YOUR_PROJECT_FOLDER/Build/your-project/branch'
    ]
  ]
  ],
```

*Note: Downstream Builds requires that `gitTokenId` is defined*

### gitOpsTrigger and gitOps
****
Banzai supports [GitOps-style](https://www.xenonstack.com/insights/what-is-gitops/) deployments. GitOps allows you to back your environments and handle their deployments from a single repository as well as decouple CI from CD (good for security). Once configured, your Service repositories will complete all CI Banzai Pipeline steps. If Banzai determines that a new version has been created or a deployment should take place it will trigger a new Banzai Pipeline that builds your GitOps repository. The GitOps repository is responsible for recording all versions of each Service and updating your 'Stacks' with the correct versions in each environment. [You can view an example GitOps repo here](https://githubcom/banzai-cicd/GitOps). For our purposes, a 'Stack' is merely a collection of indvidual Services that should be deployed together.

There are 2 methods of deployment via Banzai GitOps.
1. automatic deployment
  - the .banzai of your GitOps repo can be configured to automatically trigger a deployment based on the git branch of the upstream Service that triggered the GitOps job
2. manual deployment
  1. A user manually runs 'Build With Parameters' on the GitOps master job manually from with-in Jenkins. 
  2. The user will be presented with a series of user-input stages.
  3. The user can choose between 3 different styles of deployment
    1. 'Select Specific Versions' - The user will provide the version for each Service that should be deployed
    2. 'Promote Stack From Another Environment' - The versions from one environment will be copied to another
    3. 'Rollback Stack' - select a previous deployment to re-assign to an environment

#### GitOps Configuration

1. update the .banzai file in the repository of each Service that you would like to trigger a GitOps job with an instance of **Map<String, [BanzaiGitOpsTriggerCfg](src/com/github/banzaicicd/cfg/BanzaiGitOpsTriggerCfg.groovy)>**  

ex)
```
# ensure that 'deploy' is removed then add:

gitOpsTrigger: [
  /develop|tag-*/ : [
    jenkinsJob: '/Banzai/GitOps/master', # the path to the GitOps master job in jenkins
    stackId: 'dib'                       # the GitOps 'stack' that this service is a member of
  ]
]
```
2. At some point during your pipeline, write a `BanzaiUserData.[yaml/json]` (see [BanzaiUserData](#banzaiuserdata) for more) to the root of your project WORKSPACE like the following
```
"gitOps": {
  "versions": {
      "test-maven" : {          // add an entry for each service in a GitOps stack that should update its version
          "version": "1.0.0",
          "meta": {
              "some": "example meta"
          }
      }
  }
}
```
This information will be passed to your GitOps pipeline so that it is aware of what services should be updated

3. Create a GitOps repo. You can use the [GitOps-starter](https://github.com/banzai-cicdtarter) to speed things up.  
**Your GitOps Repo Must Contain**  
- `envs` - directory with sub-directories for each environment
- `services` - directory (this is where the available versions of each service will be stored)
- `.banzai` - file with a `gitOps` section
- `deployScript.sh` - will be called for each deployment as passed arguments containing the stack and service versions to deploy

3a. ensure your .banzai file in the GitOps repo includes an instance of **[BanzaiGitOpsCfg](src/com/github/banzaicicd/cfg/BanzaiGitOpsCfg.groovy)**  
ex) *in the following example, GitOps will automatically re-deploy any project thats 'develop' branch triggered the GitOps build to the 'dev' environment*
```
gitOps: [
    autoDeploy: [
        /develop/ : 'dev'  // <-- if the triggering project's branch matches 'develop' then re-deploy the 'dev' env.
    ],
    envs: [
        'dev' : [:],
        'qa' : [
            approvers: ['212589146'],
            watchers: ['212589146']
        ]
    ]
]
```

### stages
**List<[BanzaiStageCfg](src/com/github/banzaicicd/cfg/BanzaiStageCfg.groovy)>**  
If the Banzai-provided build,publish,deploy,integrationTests stages do not satisfy your pipeline needs you can compose your own via the `stages` BanzaiCfg property. The `stages` property can contain a mixuture of User-provided and Banzai-provided stages (such as the built-in build/publish). Because of this, the `name` property of the [BanazaiStageCfg](src/com/github/banzaicicd/cfg/BanzaiStageCfg.groovy) is reserved for `build|deploy|publish|integrationTests|scans:vulnerability|scans:quality`.  

Banzai will perform basic functions such as notifications and error handling for you during your User-provided stage execution. Be aware, Stages and boilerplate that exist for supporting SCM, Secrets Filtering, GitOps, proxy etc will still evaluate as normal. To re-iterate, when you leverage the `stages` property you will only be overriding the following Stages `vulnerabilityScans, qualityScans, build, publish, deploy, integrationTests`  

ex) the following example contains a mix of Banzai-provided Stages and User-provided Stages. Note, when calling a Banzai-provided stage you should still configure that stage using it's existing [BanzaiCfg](src/com/github/banzaicicd/cfg/BanzaiCfg.groovy) property.
```
build: [
      /.*/ : [ shell: 'scripts/build.sh' ]
],
stages: [
  [ name: 'build' ],                  // call existing Banzai-provided build stage
  [
    name: 'My Arbitrary Stage',       // provide a custom Banzai Stage name
    steps: [
      /.*/: [                         // steps for a custom stage can be configured per branch via regex
        [
          groovy: { logger "YO I RAN A CLOSURE FROM A CUSTOM STAGE!" }  // ex of running a groovy closure
        ], 
        [
          shell: 'customStageScript.sh'  // ex running a shell script
        ]
      ]
    ]
  ]
]
```

### hooks
**[BanzaiHooksCfg](src/com/github/banzaicicd/cfg/BanzaiHooksCfg.groovy)**  
Hooks are designed to provide an opportunity at certain points during the pipeline execution to run arbitrarty code. Currently, the only hooks supported are the `hooks.stages.pre` and `hooks.stages.post`. These hooks wrap the main pipeline stages (`pre` runs just after `scm` but before `filterSecrets`. `post` runs after `gitOptsTrigger` and before `downstreamBuilds`)  
ex) *note: the closures that you provide recieve an instance of your entire BanzaiCfg as an argument.
```
hooks: [
    stages: [
        pre: { cfg ->
            logger 'I ran before the pipeline!'
        },
        post: { cfg ->
            logger 'I ran after the pipeline!'
        }
    ]
]
```

## Coverity
Coverity functionality requires the Coverity ~2.0.0 Plugin to be installed on the host Jenkins https://synopsys.atlassian.net/wiki/spaces/INTDOCS/pages/623018/Synopsys+Coverity+for+Jenkins#SynopsysCoverityforJenkins-Version2.0.0. Also, if your Jenkins has a proxy configured it MUST support the `https` protocol for Coverity to work.

## Custom Stages
The `stages` property of the BanzaiCfg allows you to override the order of existing Banzai Stages as well as provide custom Stages of your own. 

## BanzaiUserData
BanzaiUserData serves 2 purposes
1. Pass variables from a user-provided script in 1 stage to a user-provided script in another
2. Supply values to a Banzai-provided Stage that aren't known until a user-provided script runs

In order to persist BanzaiUserData you simply write a `BanzaiUserData.[yaml/json]` file to the root of the project workspace during the execution of a user-provided script. The file will be ingested by Banzai and deleted so you do not have to be concerned with any sort of file collision if you write more UserData in a subsequent stage. BanzaiUserData will be passed as the 1st argument to all user-provided scripts in json format. While you may store arbitraty data in the BanzaiUserData object, there are some fields which are read by Banzai-provided stages. The following fields are reserved

```
{
    "coverity": {
        "buildCmd": "mvn clean install"
    },
    "gitOps": {
      "versions": {
          "test-maven" : {
              "version": "1.0.0",
              "meta": {
                  "some": "example meta"
              }
          }
      }
    }
}
```

## Notifications and Events
There are 3 components to configuring notifications.
1. The [BanzaiEvent](src/com/github/banzaicicd/BanzaiEvent.groovy)
2. The Notification Method, ie) [email](#email), [flowdock](#flowdock)
3. The [notifications](#notifications) cfg which associates the [BanzaiEvent](src/com/github/banzaicicd/BanzaiEvent.groovy) with the desired Notification Method

The decoupling of the [BanzaiEvent](src/com/github/banzaicicd/BanzaiEvent.groovy) from the specific notification in-tandem with each being configuratble at the branch-level allows for a high-level of flexibility.  

ex) *The following example would send emails to 'steve' when a Pipeline execution reports FAILURE or SUCCESS for all branches. It would also send notification to the flowdock configuration with the id 'myFlowCfg' for all BanzaiEvents when the branch is 'develop'*
```
flowdock: [
  myFlowCfg: [
    credId: 'banzai-flowtoken',
    author: [
      name: 'Banzai',
      avatarUrl: 'https://github.com/avatars/u/55576?s=400&u=700c7e70356d1f5a679908c1d7c7e5bf8e2beab6',
      email: 'banzai@banzai.com'
    ]
  ]
],
email: [
  addresses: [
    steve: 'steve@email.com'
  ],
  groups: [
    everyone: ['steve'],
  ]
],
notifications: [
  flowdock : [
    /develop/ : [
      'myFlowCfg' : ['.*']
    ]
  ],
  email : [
    individuals: [
      'steve' : [
        'PIPELINE:(FAILURE|SUCCESS)'
      ]
    ]
  ]
]
```
As shown above, event's can be bound to in the format '${BanzaiEvent.SCOPE}:${BanzaiEvent.STATUS}'. Please see the [BanzaiEvent](src/com/github/banzaicicd/BanzaiEvent.groovy) to determine the available event combinations. *Note* for `BanzaiEvent.scopes.VULNERABILTY` and `BanzaiEvent.scopes.QUALITY` only the statues `SUCCESS` and `FAILURE` are reported. For more examples of notification cofigurations please see [TestMavenBuild](https://github.com/banzai-cicd/TestMavenBuild/blob/master/.banzai) and [TestDownstreamBuild](https://github.com/banzai-cicd/TestDownstreamBuild/blob/master/.banzai)

## Proxies
Operating a Jenkins pipeline behind a corporate firewall can be somewhat tricky due to the different levels in-which a proxy must be configured. For instance, `Manage Jenkins -> Manage Plugins -> Advanced` allows you to set the proxy with-in the Jenkins instance and most plugins used by the pipeline will honor this setting. However, there are instances in Banzai where features are implemented via shell commands and