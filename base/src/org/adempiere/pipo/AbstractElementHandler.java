/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2006 Adempiere, Inc. All Rights Reserved.                *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 *      
 * Copyright (C) 2005 Robert Klein. robeklein@hotmail.com                     * 
 * Contributor(s): Low Heng Sin hengsin@avantz.com                            *
 *****************************************************************************/
package org.adempiere.pipo;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.xml.transform.sax.TransformerHandler;

import org.compiere.model.MSequence;
import org.compiere.model.PO;
import org.compiere.model.POInfo;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public abstract class AbstractElementHandler implements ElementHandler {

	protected CLogger log = CLogger.getCLogger("PackIn");
	
	/**
	 * Get ID from Name for a table.
	 * TODO: substitute with PO.getAllIDs
	 *
	 * @param tableName
	 * @param name
	 * 
	 */
	public int get_ID (Properties ctx, String tableName, String name) {
		return IDFinder.get_ID(tableName, name, getClientId(ctx), getTrxName(ctx));
	}

	/**
	 * Get ID from column value for a table.
	 *
	 * @param tableName
	 * @param columName
	 * @param name
	 */
	public int get_IDWithColumn (Properties ctx, String tableName, String columnName, Object value) {
		return IDFinder.get_IDWithColumn(tableName, columnName, value, getClientId(ctx), getTrxName(ctx));
	}
	
	/**
     *	Write results to log and records in history table
     *
     *      @param success
     * 		@param tableName
     * 		@param objectType
     * 		@param objectID
     * 		@param objectStatus
     * 		@throws SAXException
     *       	
     */
    public int record_log (Properties ctx, int success, String objectName,String objectType, int objectID,
    		int objectIDBackup, String objectStatus, String tableName, int AD_Table_ID) throws SAXException{    	
    	String recordLayout;
    	int id = 0;
    	TransformerHandler hd_document = getDocument(ctx);
		AttributesImpl attsOut = getDocumentAttributes(ctx);
    	if (success == 1){    		
    		//hd_documemt.startElement("","","Successfull",attsOut);
    		recordLayout = "Type:"+objectType + "  -   Name:"+objectName + "  -  ID:"+objectID +"  -  Action:"+objectStatus+"  -  Success";
    		
    		hd_document.startElement("","","Success",attsOut);
    		hd_document.characters(recordLayout.toCharArray(),0,recordLayout.length());
    		hd_document.endElement("","","Success");
    		//hd_documemt.endElement("","","Successfull");
    		
    		//String sql2 = "SELECT MAX(AD_PACKAGE_IMP_DETAIL_ID) FROM AD_PACKAGE_IMP_DETAIL";
    		//int id = DB.getSQLValue(m_trxName, sql2)+1;
    		
    		id = MSequence.getNextID (Env.getAD_Client_ID(ctx), "AD_Package_Imp_Detail", getTrxName(ctx));
    		
    		StringBuffer sqlB = new StringBuffer ("Insert INTO AD_Package_Imp_Detail" 
    				+   "(AD_Client_ID, AD_Org_ID, CreatedBy, UpdatedBy, " 
    				+   "AD_PACKAGE_IMP_DETAIL_ID, AD_PACKAGE_IMP_ID, TYPE, NAME," 
    				+   " ACTION, SUCCESS, AD_ORIGINAL_ID, AD_BACKUP_ID, TABLENAME, AD_TABLE_ID)"
    				+	"VALUES("
    				+	" "+ Env.getAD_Client_ID(ctx)
    				+	", "+ Env.getAD_Org_ID(ctx)
    				+	", "+ Env.getAD_User_ID(ctx)
    				+	", "+ Env.getAD_User_ID(ctx)
    				+	", " + id 
    				+	", " + getPackageImpId(ctx)
    				+	", '" + objectType
    				+	"', '" + objectName
    				+	"', '" + objectStatus
    				+	"', 'Success'"
    				+	", "+objectID
    				+	", "+objectIDBackup
    				+	", '"+tableName
    				+	"', "+AD_Table_ID
    				+")");
    		int no = DB.executeUpdate (sqlB.toString(), getTrxName(ctx));
    		if (no == -1)
    			log.info("Insert to import detail failed");
    		
    	}
    	else{
    		String PK_Status = "Completed with errors";
    		hd_document.startElement("","","Failure",attsOut);    	
    		recordLayout = "Type:"+objectType + "  -   Name:"+tableName + "  -  ID:"+objectID +"  -  Action:"+objectStatus+"  -  Failure";
    		//hd_documemt.startElement("","","Success",attsOut);
    		hd_document.characters(recordLayout.toCharArray(),0,recordLayout.length());
    		//hd_documemt.endElement("","","Success");		
    		hd_document.endElement("","","Failure");
    		
    		//String sql2 = "SELECT MAX(AD_PACKAGE_IMP_DETAIL_ID) FROM AD_PACKAGE_IMP_DETAIL";
    		//int id = DB.getSQLValue(m_trxName,sql2)+1; 
    		
    		id = MSequence.getNextID (Env.getAD_Client_ID(ctx), "AD_Package_Imp_Detail", getTrxName(ctx));
    		
    		StringBuffer sqlB = new StringBuffer ("Insert INTO AD_Package_Imp_Detail" 
    				+   "(AD_Client_ID, AD_Org_ID, CreatedBy, UpdatedBy, " 
    				+   "AD_PACKAGE_IMP_DETAIL_ID, AD_PACKAGE_IMP_ID, TYPE, NAME," 
    				+   " ACTION, SUCCESS, AD_ORIGINAL_ID, AD_BACKUP_ID, TABLENAME, AD_TABLE_ID)"
    				+	"VALUES("
    				+	" "+ Env.getAD_Client_ID(ctx)
    				+	", "+ Env.getAD_Org_ID(ctx)
    				+	", "+ Env.getAD_User_ID(ctx)
    				+	", "+ Env.getAD_User_ID(ctx)
    				+	", " + id 
    				+	", " + getPackageImpId(ctx)
    				+	", '" + objectType
    				+	"', '" + objectName
    				+	"', '" + objectStatus
    				+	"', 'Failure'" 
    				+	", "+objectID
    				+	", "+objectIDBackup
    				+	", '"+tableName
    				+	"', "+AD_Table_ID
    				+")");
    		int no = DB.executeUpdate (sqlB.toString(), getTrxName(ctx));
    		if (no == -1)
    			log.info("Insert to import detail failed");
    	}
    	
    	String Object_Status = "Status not set";
    	return id;  
    }
    
    /**
	 * Get ID from Name for a table with a Master reference.
	 *
	 * @param tableName
	 * @param name
	 * @param tableNameMaster
	 * @param nameMaster
	 */
	public int get_IDWithMaster (Properties ctx, String tableName, String name, String tableNameMaster, String nameMaster) {
		return IDFinder.get_IDWithMaster(tableName, name, tableNameMaster, nameMaster, getTrxName(ctx));
	}

    /**
     * Get ID from Name for a table with a Master reference.
     *
     * @param tableName
     * @param name
     * @param tableNameMaster
     * @param nameMaster
     */    
    
	public int get_IDWithMasterAndColumn (Properties ctx, String tableName, String columnName, String name, String tableNameMaster, int masterID) {
		return IDFinder.get_IDWithMasterAndColumn(tableName, columnName, name, tableNameMaster, masterID, 
				getTrxName(ctx));
	}

	/**
	 * Get ID from Name for a table with a Master reference ID.
	 *
	 * @param tableName
	 * @param name
	 * @param tableNameMaster
	 * @param masterID
	 */    
	public int get_IDWithMaster (Properties ctx, String tableName, String name, String tableNameMaster, int masterID) {
		return IDFinder.get_IDWithMaster(tableName, name, tableNameMaster, masterID, getTrxName(ctx));
	}

	/**
	 * Get ID from Name for a table.
	 * TODO: substitute with PO.getAllIDs
	 *
	 * @param tableName
	 * @param name
	 */
	public int getIDbyName (Properties ctx, String tableName, String name) {
		return IDFinder.getIDbyName(tableName, name, getClientId(ctx), getTrxName(ctx));
	}
	
    /**
     *	Make backup copy of record.
     *
     *      @param tablename
     *  	
     *  	
     *       	
     */
    public int copyRecord(Properties ctx, String tableName,PO from){
	// Create new record
    	int idBackup = 0;
    	String colValue=null;
    	int tableID = get_IDWithColumn(ctx, "AD_Table", "TableName", tableName);    	
		POInfo poInfo = POInfo.getPOInfo(ctx, tableID);
		for (int i = 0; i < poInfo.getColumnCount(); i++){
			String colName = poInfo.getColumnName(i);
			colValue=null;
			
			    int columnID =get_IDWithMasterAndColumn (ctx, "AD_Column", "ColumnName", poInfo.getColumnName(i), "AD_Table", tableID);
			    StringBuffer sqlD = new StringBuffer("SELECT AD_Reference_ID FROM AD_COLUMN WHERE AD_Column_ID = '"+columnID+"'");
	    		int referenceID = DB.getSQLValue(getTrxName(ctx),sqlD.toString());
	    		
	    		idBackup = MSequence.getNextID (getClientId(ctx), "AD_Package_Imp_Backup", getTrxName(ctx));
	    		
	    		sqlD = new StringBuffer("SELECT MAX(AD_PACKAGE_IMP_DETAIL_ID) FROM AD_PACKAGE_IMP_DETAIL");
	    		int idDetail = DB.getSQLValue(getTrxName(ctx),sqlD.toString())+1;
	    		
	    		if (referenceID == 10 || referenceID == 14 || referenceID == 34 || referenceID == 17)
	    			if (from.get_Value(i)!= null)
	    				colValue = from.get_Value(i).toString().replaceAll("'","''");	    		
				else if (referenceID == 20|| referenceID == 28)
					if (from.get_Value(i)!= null)	    				    				
	    				colValue = from.get_Value(i).toString().replaceAll("'","''");
				else
					;//Ignore
	    			    		
	    		StringBuffer sqlB = new StringBuffer ("Insert INTO AD_Package_Imp_Backup" 
	    				+   "(AD_Client_ID, AD_Org_ID, CreatedBy, UpdatedBy, " 
	    				+   "AD_PACKAGE_IMP_BACKUP_ID, AD_PACKAGE_IMP_DETAIL_ID, AD_PACKAGE_IMP_ID," 
	    				+	" AD_TABLE_ID, AD_COLUMN_ID, AD_REFERENCE_ID, COLVALUE)"
	    				+	"VALUES("
	    				+	" "+ Env.getAD_Client_ID(ctx)
	    				+	", "+ Env.getAD_Org_ID(ctx)
	    				+	", "+ Env.getAD_User_ID(ctx)
	    				+	", "+ Env.getAD_User_ID(ctx)
						+	", " + idBackup
						+	", " + idDetail
	    				+	", " + getPackageImpId(ctx)
	    				+	", " + tableID
	    				+	", " + columnID
	    				+	", " + referenceID
	    				+	", '" + (colValue != null ? colValue : from.get_Value(i))
	    				+"')");
	    		
	    		int no = DB.executeUpdate (sqlB.toString(), getTrxName(ctx));
	    		if (no == -1)
					log.info("Insert to import backup failed");
	    		//}
		}		
		return idBackup;
    }
    
    /**
     *	Open input file for processing
     *
     * 	@param String file with path
     * 	
     */
    public FileInputStream OpenInputfile (String filePath) {
    	
    	FileInputStream fileTarget = null;
    	
    	try {    	
    		fileTarget = new FileInputStream(filePath);
    	}
    	catch (FileNotFoundException e ) {
    		System.out.println("Can't find file ");
    		
    		return null;
    	}
    	return fileTarget;
    }
    
    /**
     *	Open output file for processing
     *
     * 	@param String file with path
     * 	
     */
    public OutputStream OpenOutputfile (String filePath) {
    	
    	OutputStream fileTarget = null;
    	
    	try {    	
    		fileTarget = new FileOutputStream(filePath);
    	}
    	catch (FileNotFoundException e ) {
    		System.out.println("Can't find file ");
    		
    		return null;
    	}
    	return fileTarget;
    }
    
    /**
     *	Copyfile
     *
     * 	@param String file with path
     * 	
     */
    public int copyFile (InputStream source,OutputStream target) {
    
    	 int byteCount = 0;
    	 int success = 0;
	        try {
	           while (true) {
	              int data = source.read();
	              if (data < 0)
	                 break;
	              target.write(data);
	              byteCount++;
	           }
	           source.close();
	           target.close();
	          
	           
	           System.out.println("Successfully copied " + byteCount + " bytes.");
	        }
	        catch (Exception e) {
	           System.out.println("Error occurred while copying.  "+ byteCount + " bytes copied.");
	           System.out.println(e.toString());
	           
	           success = -1;
	        }
	    return success;
    }
    
    protected int getClientId(Properties ctx) {
    	return Env.getContextAsInt(ctx, "AD_Client_ID");
    }
    
    protected int getPackageImpId(Properties ctx) {
    	return Env.getContextAsInt(ctx, "AD_Package_Imp_ID");
    }
    
    protected String getUpdateMode(Properties ctx) {
    	return Env.getContext(ctx, "UpdateMode");
    }
    
    protected String getTrxName(Properties ctx) {
    	return Env.getContext(ctx, "TrxName");
    }
    
    protected TransformerHandler getDocument(Properties ctx) {
    	return (TransformerHandler)ctx.get("Document");
    }
    
    protected AttributesImpl getDocumentAttributes(Properties ctx) {
    	return (AttributesImpl)ctx.get("DocumentAttributes");
    }
    
    protected String getPackageDirectory(Properties ctx) {
    	return Env.getContext(ctx, "PackageDirectory");
    }
}
