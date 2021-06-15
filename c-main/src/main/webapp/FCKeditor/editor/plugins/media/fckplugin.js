/*
 * FCKeditor - The text editor for Internet - http://www.fckeditor.net
 * Copyright (C) 2003-2007 Frederico Caldeira Knabben
 *
 * == BEGIN LICENSE ==
 *
 * Licensed under the terms of any of the following licenses at your
 * choice:
 *
 *  - GNU General Public License Version 2 or later (the "GPL")
 *    http://www.gnu.org/licenses/gpl.html
 *
 *  - GNU Lesser General Public License Version 2.1 or later (the "LGPL")
 *    http://www.gnu.org/licenses/lgpl.html
 *
 *  - Mozilla Public License Version 1.1 or later (the "MPL")
 *    http://www.mozilla.org/MPL/MPL-1.1.html
 *
 * == END LICENSE ==
 *
 * Plugin to insert "Medias" in the editor.
 */

// Register the related command.
FCKCommands.RegisterCommand( 'Media', new FCKDialogCommand( 'Media', FCKLang.MediaDlgTitle, FCKPlugins.Items['media'].Path + 'media_frame.jsp?action=selectImage', 800, 600 ) ) ;

// Create the "Plaholder" toolbar button.
var oMediaItem = new FCKToolbarButton( 'Media', FCKLang.MediaBtn ) ;
oMediaItem.IconPath = FCKPlugins.Items['media'].Path + 'media.gif' ;

FCKToolbarItems.RegisterItem( 'Media', oMediaItem ) ;


// The object used for all Media operations.
var FCKMedias = new Object() ;

FCKMedias.Add = function( filePath )
{
	
	var oSpan = FCK.CreateElement( 'SPAN' ) ;
	oSpan.innerHTML = filePath;
}


