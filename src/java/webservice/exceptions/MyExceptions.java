/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webservice.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 *
 * @author hadar
 */
public class MyExceptions extends WebApplicationException{
    public MyExceptions(){
       
        super(Response.serverError().build());
    }
   
    public MyExceptions(String message) {
        super( Response.serverError().entity(message).type("text/plain;charset=UTF-8").build());
       
    }
}