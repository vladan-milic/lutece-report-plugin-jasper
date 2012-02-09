/*
 * Copyright (c) 2002-2011, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.jasper.service.export;

import fr.paris.lutece.plugins.jasper.business.JasperReportHome;
import fr.paris.lutece.plugins.jasper.service.ILinkJasperReport;
import fr.paris.lutece.plugins.jasper.service.JasperConnectionService;
import fr.paris.lutece.plugins.jasper.service.JasperFileLinkService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.util.JRLoader;

import java.io.File;

import java.sql.Connection;
import java.sql.SQLException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;


public abstract class AbstractDefaultJasperRender implements ILinkJasperReport, Cloneable
{
    protected static final String PROPERTY_FILES_PATH = "jasper.files.path";
    protected static final String PROPERTY_IMAGES_FILES_PATH = "jasper.images.path";
    protected static final String PROPERTY_EXPORT_CHARACTER_ENCODING = "jasper.export.characterEncoding";
    protected static final String PARAMETER_JASPER_VALUE = "value";
    protected static final String REGEX_ID = "^[\\d]+$";

    public byte[] getBuffer( String strReportId, HttpServletRequest request )
    {
        StringBuffer sb = new StringBuffer(  );
        Plugin plugin = PluginService.getPlugin( "jasper" );
        Connection conn = null;
        fr.paris.lutece.plugins.jasper.business.JasperReport report = null;

        try
        {
            report = JasperReportHome.findByPrimaryKey( strReportId, plugin );

            String strPageDesc = report.getUrl(  );
            String strDirectoryPath = AppPropertiesService.getProperty( PROPERTY_FILES_PATH );
            String strAbsolutePath = AppPathService.getWebAppPath(  ) + strDirectoryPath + strPageDesc;

            File reportFile = new File( strAbsolutePath );

            JasperReport jasperReport = (JasperReport) JRLoader.loadObject( reportFile.getPath(  ) );
            List<String> listValues = JasperFileLinkService.INSTANCE.getValues( request );
            Map parameters = new HashMap(  );

            for ( int i = 0; i < listValues.size(  ); i++ )
            {
                parameters.put( PARAMETER_JASPER_VALUE + ( i + 1 ),
                    listValues.get( i ).matches( REGEX_ID ) ? Integer.parseInt( listValues.get( i ) )
                                                            : listValues.get( i ) );
            }

            conn = JasperConnectionService.getConnectionService( report.getPool(  ) ).getConnection(  );

            JasperPrint jasperPrint = JasperFillManager.fillReport( jasperReport, parameters, conn );
            JRExporter exporter = getExporter( request, report );
            exporter.setParameter( JRExporterParameter.JASPER_PRINT, jasperPrint );
            exporter.setParameter( JRExporterParameter.CHARACTER_ENCODING,
                AppPropertiesService.getProperty( PROPERTY_EXPORT_CHARACTER_ENCODING ) );
            exporter.setParameter( JRExporterParameter.OUTPUT_STRING_BUFFER, sb );

            exporter.exportReport(  );

            if ( exporter instanceof JRHtmlExporter )
            {
                ( (JRHtmlExporter) exporter ).reset(  );
            }
        }
        catch ( Exception e )
        {
            AppLogService.error( e );
        }
        finally
        {
            if ( conn != null )
            {
                if ( report != null )
                {
                    try
                    {
                        JasperConnectionService.getConnectionService( report.getPool(  ) ).freeConnection( conn );
                    }
                    catch ( Exception e )
                    {
                        try
                        {
                            conn.close(  );
                        }
                        catch ( SQLException s )
                        {
                            AppLogService.error( s );
                        }
                    }
                }
                else
                {
                    try
                    {
                        conn.close(  );
                    }
                    catch ( SQLException s )
                    {
                        AppLogService.error( s );
                    }
                }
            }
        }

        return sb.toString(  ).getBytes(  );
    }
}