/*
 * ====================================================================
 *
 * Copyright (C) 2015 GeoSolutions S.A.S.
 * http://www.geo-solutions.it
 *
 * GPLv3 + Classpath exception
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. 
 *
 * ====================================================================
 *
 * This software consists of voluntary contributions made by developers
 * of GeoSolutions.  For more information on GeoSolutions, please see
 * <http://www.geo-solutions.it/>.
 *
 */
package it.geosolutions.dynamicstyler;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class GetStyle extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private Configuration cfg = new Configuration();
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        List<TemplateLoader> loaders = new ArrayList<TemplateLoader>();
        
        addTemplateLoader(loaders, config.getInitParameter("stylesPath"));
        addTemplateLoader(loaders, System.getProperty("stylesPath"));
        loaders.add(new ClassTemplateLoader(getClass(), "/"));
        
        MultiTemplateLoader loader = new MultiTemplateLoader(loaders.toArray(new TemplateLoader[] {}));
        cfg.setTemplateLoader(loader);
    }

    private void addTemplateLoader(List<TemplateLoader> loaders, String stylesPath)
            throws ServletException {
        if(stylesPath != null && !stylesPath.trim().isEmpty()) {
            try {
                loaders.add(new FileTemplateLoader(new File(stylesPath)));
            } catch (IOException e) {
                throw new ServletException("Unable to create TemplateLoader for " + stylesPath);
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {
        String styleName = req.getParameter("style");
        
        if(styleName != null) {
            Template template = cfg.getTemplate(styleName + ".sld");
            Map<String, String> params = new HashMap<String, String>();
            Enumeration parameterNames = req.getParameterNames();
            while(parameterNames.hasMoreElements()) {
                String paramName = parameterNames.nextElement().toString();
                params.put(paramName.toUpperCase(), req.getParameter(paramName));
            }
            try {
                template.process(params, resp.getWriter());
            } catch (TemplateException e) {
                sendError(resp ,500, "Error reading " + styleName + ": " + e.getMessage());
            }
            
        } else {
            sendError(resp ,500, "style parameter is mandatory");
        }
    }

    private void sendError(HttpServletResponse resp, int code, String error) throws IOException {
        resp.setStatus(code);
        resp.setContentLength(error.length());
        resp.setContentType("text/plain");
        writeResponse(resp, error);
    }

    private void writeResponse(HttpServletResponse resp, String content) throws IOException {
        PrintWriter writer = resp.getWriter();
        writer.print(content);
        writer.close();
    }
    
}
