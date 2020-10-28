/*
 * Copyright (c) 2002-2018, Mairie de Paris
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
package fr.paris.lutece.plugins.jasper.service.purge;

import java.io.File;

import org.apache.commons.lang3.StringUtils;

import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

/**
 *
 * ImagePurgeService
 *
 */
public class ImagePurgeService
{
    private static final String PROPERTY_IMAGES_FILES_PATH = "jasper.images.path";
    protected static final String PROPERTY_IMAGES_ROOT_PATH = "jasper.images.root.path";


    private ImagePurgeService( )
    {
    }

    public static void purgeFiles( )
    {
        String strRootImagesPath = AppPropertiesService.getProperty( PROPERTY_IMAGES_ROOT_PATH );
        String strRootImagesDirectory = StringUtils.isNoneBlank( strRootImagesPath ) ? strRootImagesPath : AppPathService.getWebAppPath( );
        String strDirectoryPath = AppPropertiesService.getProperty( PROPERTY_IMAGES_FILES_PATH );
        String strRootPath = strRootImagesDirectory + strDirectoryPath;
        File folder = new File( strRootPath );
        deleteFolderWithContent( folder );

        File newFolder = new File( strRootPath );
        newFolder.mkdir( );
    }

    public static boolean deleteFolderWithContent( File folder )
    {
        if ( folder.isDirectory( ) )
        {
            String [ ] files = folder.list( );

            for ( int i = 0; i < files.length; i++ )
            {
                boolean success = deleteFolderWithContent( new File( folder, files [i] ) );

                if ( !success )
                {
                    return false;
                }
            }
        }

        return folder.delete( );
    }
}
