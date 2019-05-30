// ################################################################################## \\
// #########     Supervisao de Arquitetura e Prospeccao Tecnologica         ######### \\
// #########     Script para Integração Contínua (Git, Nexus e Jenkins)     ######### \\
// ################################################################################## \\


// ######### Global Environment Variables Definitions ######### \\

	// ######### Global Definitions ######### \\
	def MAVEN_INSTALLATION = null

	// ######### Project Definitions ######### \\
	// ### Maven ###
	def POM = null

	// ### Mail ###
	def MAIL_DEPLOYMENT_APPROVAL_SUBJECT = null
	def MAIL_DEPLOYMENT_APPROVAL_CONTENT = null
	def MAIL_DEPLOYMENT_SUCCESSFUL_SUBJECT = null
	def MAIL_DEPLOYMENT_SUCCESSFUL_CONTENT = null
	def MAIL_DEPLOYMENT_FAILURE_SUBJECT = null
	def MAIL_DEPLOYMENT_FAILURE_CONTENT = null
	def PROJECT_ADMIN_USERS = null;

// ######### Script ######### \\

stage('Preparation') {
	node {
		APPROVAL_TIMEOUT_IN_DAYS = 3
		MAVEN_INSTALLATION = 'Maven for jenkins'

		checkout scm
		POM = readMavenPom file: 'pom.xml'
		
		// Users who approve the deployment (to separate multiple users, do not use white spaces, just commas) 
		PROJECT_ADMIN_USERS = 'renato-rsilva'
		
		MAIL_DEPLOYMENT_APPROVAL_SUBJECT = "$env.JOB_NAME: Solicitacao de Aprovacao de Deployment no Nexus"
		MAIL_DEPLOYMENT_APPROVAL_CONTENT = '<h3>Nova solicitacao de publicacao disponivel para o artefato ' + POM.artifactId + ' (' + POM.version + ')</h3><p>Por favor <a href=' + "$env.BUILD_URL" + '>acesse o Jenkins</a> e realize a aprovacao manualmente.<br/><b>ATENCAO: Veja primeiro se os indices de qualidade estao aderentes ao projeto.</b></p>'
		MAIL_DEPLOYMENT_SUCCESSFUL_SUBJECT = "$env.JOB_NAME: Nova versao do artefato no servidor Nexus"
		MAIL_DEPLOYMENT_SUCCESSFUL_CONTENT = '<h3>Nova versao do artefato ' + POM.artifactId + ' (' + POM.version + ') foi publicado com sucesso no Nexus.</h3>' 
		MAIL_DEPLOYMENT_FAILURE_SUBJECT = "$env.JOB_NAME: Erro ao publicar artefato no servidor Nexus"
		MAIL_DEPLOYMENT_FAILURE_CONTENT = '<h3>Erro ao publicar o artefato ' + POM.artifactId + ' (' + POM.version + ') no Nexus.</h3><p>Por favor verifique os detalhes do erro no log anexo.</p>'				
	}
}

stage('Packaging') {
    node {
		 try {
			withMaven(maven: "$MAVEN_INSTALLATION") {
    			sh "mvn clean package -U -DskipTests"    			
	    	}			
        } catch (e) {           
            currentBuild.result = 'FAILURE'
            sendMailDeploymentFailure("$MAIL_DEPLOYMENT_FAILURE_SUBJECT", "$MAIL_DEPLOYMENT_FAILURE_CONTENT", "$PROJECT_ADMIN_USERS")
            throw e
        } 
    }
}

stage('Deployment') {	
    node {
       try {
			withMaven(maven: "$MAVEN_INSTALLATION") {
    			sh "mvn clean deploy -Dsettings.security=/dados/repositorioMaven/settings-security.xml -DskipTests"   			
	    	}
			sendMailDeploymentSuccessful("$MAIL_DEPLOYMENT_SUCCESSFUL_SUBJECT", "$MAIL_DEPLOYMENT_SUCCESSFUL_CONTENT", "$PROJECT_ADMIN_USERS")
			currentBuild.result = 'SUCCESS'
        } catch (e) {           
            currentBuild.result = 'FAILURE'
            sendMailDeploymentFailure("$MAIL_DEPLOYMENT_FAILURE_SUBJECT", "$MAIL_DEPLOYMENT_FAILURE_CONTENT", "$PROJECT_ADMIN_USERS")
            throw e
        } finally {           
            cleanWs cleanWhenFailure: false
        }
   }
}

def sendMailDeploymentApproval(String subject, String content, emailListRecipient) {
	sendMail(subject, content, emailListRecipient);
}

def sendMailDeploymentSuccessful(String subject, String content, emailListRecipient) { 		
	sendMail(subject, content, emailListRecipient);
}

def sendMailDeploymentFailure(String subject, String content, emailListRecipient) {
	sendMail(subject, content, emailListRecipient);
}

def sendMail(String subject, String body, emailListRecipient) {
	 emailext ( 
			subject: subject, 
			attachmentsPattern: '**/report.html',
			mimeType: 'text/html',
			recipientProviders: [[$class: 'DevelopersRecipientProvider'], [$class: 'RequesterRecipientProvider']],
			body: body + "<h5>Informacoes do JOB:</h5><p>$env.JOB_NAME [$env.BUILD_NUMBER]<br/><a href='$env.BUILD_URL'>$env.BUILD_URL</a></p>",
			to: emailListRecipient
	);
}