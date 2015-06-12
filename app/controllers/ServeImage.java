package controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.commons.io.*;
import play.mvc.Controller;
import play.mvc.Result;

public class ServeImage extends Controller
{
    public static Result at(String filename)
    {
    	System.out.println("ServeImage............");
        response().setContentType("image");     
        byte[] i_file = null;

        try
        {
            i_file = IOUtils.toByteArray(new FileInputStream(new File("public/images/uploads/"+filename)));
        } catch (FileNotFoundException e)
        {
        	System.out.println("returning 404....");
            // return 404
        } catch (IOException e)
        {
        	System.out.println("Again returning 404....");
            // return 404           
        }

        return ok(i_file);
    }   
}