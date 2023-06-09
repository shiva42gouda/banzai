#!/usr/bin/env groovy

import com.github.banzaicicd.cfg.BanzaiCfg
import com.github.banzaicicd.BanzaiEvent
import com.github.banzaicicd.cfg.BanzaiNotificationsEmailCfg

void call(BanzaiCfg cfg, BanzaiEvent event) {
    if (!cfg.email 
        || (!cfg.email.addresses || !cfg.email.groups)
        || !cfg.notifications 
        || !cfg.notifications.email) {
        return
    }
    // determine if there is an email config for this branch
    BanzaiNotificationsEmailCfg emailCfg = findValueInRegexObject(cfg.notifications.email, BRANCH_NAME)
    if (emailCfg == null) {
        return 
    }
    logger "email configuration for branch '${BRANCH_NAME}' detected"
    /*
        determine if there are groups or individuals configured with a regex
        pattern matching this event
    */
    String currentEvent = event.getEventLabel()
    Set<String> addresses = []
    if (emailCfg.groups) {
        // find groupIds
        Set<String> groupIds = emailCfg.groups.keySet().findAll { groupId ->
             emailCfg.groups[groupId].find { regex -> currentEvent ==~ regex }
        }
        
        groupIds.each { groupId ->
            List<String> emailIds = cfg.email.groups[groupId]
            emailIds.each { addresses.add(cfg.email.addresses[it]) }
        }
    }
    if (emailCfg.individuals) {
        Set<String> emailIds = emailCfg.individuals.keySet().findAll { emailId ->
            emailCfg.individuals[emailId].find { regex -> currentEvent ==~ regex }
        }

        emailIds.each { addresses.add(cfg.email.addresses[it]) }
    }

    if (addresses.size() > 0) {
        String subject = "${env.JOB_NAME} ${event.scope} ${event.status}"
        String body = "Message: ${event.message}"
        sendEmail(cfg.email.admin, addresses.join(','), subject, body, event.attachmentPattern)
    }
}