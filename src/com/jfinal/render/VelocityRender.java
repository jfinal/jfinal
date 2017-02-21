/**
 * Copyright (c) 2011-2012, James Zhan 詹波 (jfinal@126.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jfinal.render;

import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Map.Entry;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import javax.servlet.ServletContext;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

/**
 * VelocityRender.
 */
@SuppressWarnings({"serial", "unchecked"})
class VelocityRender extends Render {
	
	private transient static final String encoding = getEncoding();
	private transient static final String contentType = "text/html;charset=" + encoding;
	private transient static final Properties properties = new Properties();
	
	private transient static boolean notInit = true;
	
	public VelocityRender(String view) {
		this.view = view;
	}
	
	/*
	static {
		String webPath = RenderFactory.getServletContext().getRealPath("/");
		
		Properties properties = new Properties();
		properties.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH, webPath);
		properties.setProperty(Velocity.ENCODING_DEFAULT, encoding); 
		properties.setProperty(Velocity.INPUT_ENCODING, encoding); 
		properties.setProperty(Velocity.OUTPUT_ENCODING, encoding); 
		
		Velocity.init(properties);	// Velocity.init("velocity.properties");	// setup
	}*/
	
	static void init(ServletContext servletContext) {
		String webPath = servletContext.getRealPath("/");
		properties.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH, webPath);
		properties.setProperty(Velocity.ENCODING_DEFAULT, encoding); 
		properties.setProperty(Velocity.INPUT_ENCODING, encoding); 
		properties.setProperty(Velocity.OUTPUT_ENCODING, encoding);
	}
	
	static void setProperties(Properties properties) {
		Set<Entry<Object, Object>> set = properties.entrySet();
		for (Iterator<Entry<Object, Object>> it=set.iterator(); it.hasNext();) {
			Entry<Object, Object> e = it.next();
			VelocityRender.properties.put(e.getKey(), e.getValue());
		}
	}
	
	public void render() {
		 if (notInit) {
			 Velocity.init(properties);	// Velocity.init("velocity.properties");	// setup
			 notInit = false;
		 }
		
		Writer writer = null;
        try {
            /*
             *  Make a context object and populate with the data.  This
             *  is where the Velocity engine gets the data to resolve the
             *  references (ex. $list) in the template
             */
            VelocityContext context = new VelocityContext();
            
    		// Map root = new HashMap();
    		for (Enumeration<String> attrs=request.getAttributeNames(); attrs.hasMoreElements();) {
    			String attrName = attrs.nextElement();
    			context.put(attrName, request.getAttribute(attrName));
    		}
    		
            /*
             *  get the Template object.  This is the parsed version of your
             *  template input file.  Note that getTemplate() can throw
             *   ResourceNotFoundException : if it doesn't find the template
             *   ParseErrorException : if there is something wrong with the VTL
             *   Exception : if something else goes wrong (this is generally
             *        indicative of as serious problem...)
             */
            Template template = Velocity.getTemplate(view);
            
            /*
             *  Now have the template engine process your template using the
             *  data placed into the context.  Think of it as a  'merge'
             *  of the template and the data to produce the output stream.
             */
           response.setContentType(contentType);
           writer = response.getWriter();	// BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));
            
           template.merge(context, writer);
           writer.flush();	// flush and cleanup
        }
        catch(ResourceNotFoundException e) {
        	throw new RenderException("Example : error : cannot find template " + view, e);
        }
        catch( ParseErrorException e) {
            throw new RenderException("Example : Syntax error in template " + view + ":" + e, e);
        }
        catch(Exception e ) {
            throw new RenderException(e);
        }
        finally {
        	// if (writer != null)
        		try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        }
	}
}




