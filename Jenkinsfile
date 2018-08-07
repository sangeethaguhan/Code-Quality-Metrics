node {
	stage 'build'
	    
		checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [], gitTool: 'Default', submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'ci247', url: 'git@github.home.247-inc.net:JenkinsPlugins/quality-metrics-portlet.git']]])



	stage 'integration-test'
		echo 'tested'
}
	
