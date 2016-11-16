package controllers;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;

import models.User;
import play.data.FormFactory;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;

public class AccessControl extends Controller {
	
	
	 @Inject
	 FormFactory formFactory;
	

    public CompletionStage<Result> login() {
    	System.out.println("login called");
    	User user = formFactory.form(User.class).bindFromRequest().get();
    	User userDB;
    	
    	if(user.userName != null){
    		
    		if (user.password != null) {
    			
    			userDB = User.findByUserName(user.userName);
    			
    			if(userDB != null){
    		    	if(userDB.password.equalsIgnoreCase(user.password)){
    		    		System.out.println("authenticated user");
    		    		session("connected", userDB.userName);
    		    		
    		    	} else {
    		    		System.out.println("password mismatch");
    		    		// return to user with message about mismatched password
    		    		flash("error", Messages.get("login.passwordmismatch"));
    		    		return CompletableFuture.completedFuture(redirect(routes.Application.index()));
    		    	}
    			} else {
    				// user not in database
    				System.out.println("user not in db");
		    		flash("error", Messages.get("login.usernamenotrecognised"));
		    		return CompletableFuture.completedFuture(redirect(routes.Application.index()));
    				
    			}
    			
    		} else {
    			// return to user requesting password to be entered
    			System.out.println("password not entered");
    			flash("error", Messages.get("login.nopassword"));
    			return CompletableFuture.completedFuture(redirect(routes.Application.index()));
    		}
    	} else {
    		//return to user asking for username
    		System.out.println("username not entered");
    		flash("error", Messages.get("login.nousername"));
    		return CompletableFuture.completedFuture(redirect(routes.Application.index()));
    	}
    	
    	
    	return CompletableFuture.completedFuture(redirect(routes.Application.dashboard()));
    	
    }
    
    public CompletionStage<Result> logout() {
    	session().remove("connected");
    	flash("success", Messages.get("logout.success"));
    	return CompletableFuture.completedFuture(redirect(routes.Application.index()));
    }

}