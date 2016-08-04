/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bonitasoft.commentshandler;

import com.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.comment.model.SComment;
import org.bonitasoft.engine.core.process.comment.model.SSystemComment;
import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.events.model.SHandler;
import org.bonitasoft.engine.events.model.SHandlerExecutionException;
import org.bonitasoft.engine.exception.BonitaHomeConfigurationException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.identity.SUserNotFoundException;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 *
 * @author Domenico
 */
public class CommentsHandler implements SHandler<SEvent>{
    private Logger logger = Logger.getLogger("org.bonitasoft");

    @Override
    public void execute(SEvent t) throws SHandlerExecutionException {
        SComment comment=(SComment) t.getObject();
        if (comment.getContent().contains("is now assigned to")){
            int idx=comment.getContent().indexOf("is now assigned to");
            if (idx==-1){
                return;
            }
            String username=comment.getContent().substring(idx + "is now assigned to".length()).trim();
            String initial= comment.getContent().substring(0, comment.getContent().indexOf(username));
            logger.info("Username:" + username);
            ServiceAccessorFactory serviceAccessorFactory = ServiceAccessorFactory.getInstance();
            try {
                TenantServiceAccessor tenantServiceAccessor= serviceAccessorFactory.createTenantServiceAccessor(1L);
                SUser user = null;
                try {
                    user = tenantServiceAccessor.getIdentityService().getUserByUserName(username);
                } catch (SUserNotFoundException sUserNotFoundException) {
                    return;
                }
                tenantServiceAccessor.getCommentService().addSystemComment(comment.getProcessInstanceId(),initial.concat(user.getFirstName()).concat(" ").concat(user.getLastName()));
                tenantServiceAccessor.getCommentService().delete(comment);
            } catch (Exception ex) {
                throw new SHandlerExecutionException(new SBonitaException(ex.toString()) {
                });
            } 
            
        }
    }

    @Override
    public boolean isInterested(SEvent t) {        
        return (t.getObject() instanceof SSystemComment);
    }

    @Override
    public String getIdentifier() {
        return "CommentsHandler";
    }
    
}
