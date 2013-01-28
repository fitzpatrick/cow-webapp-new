/**
 * Approved for Public Release: 10-4800. Distribution Unlimited.
 * Copyright 2011 The MITRE Corporation,
 * Licensed under the Apache License,
 * Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.wiredwidgets.cow.webapp.client.page;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.wiredwidgets.cow.webapp.client.BpmServiceMain;
import org.wiredwidgets.cow.webapp.client.PageManager;
import org.wiredwidgets.cow.webapp.client.PageManager.Pages;
import org.wiredwidgets.cow.webapp.client.bpm.Activity;
import org.wiredwidgets.cow.webapp.client.bpm.BaseList;
import org.wiredwidgets.cow.webapp.client.bpm.Decision;
import org.wiredwidgets.cow.webapp.client.bpm.Loop;
import org.wiredwidgets.cow.webapp.client.bpm.Parse;
import org.wiredwidgets.cow.webapp.client.bpm.Task;
import org.wiredwidgets.cow.webapp.client.bpm.Template;
import org.wiredwidgets.cow.webapp.client.components.CustomListGrid;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.types.SortDirection;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.grid.CellFormatter;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.CellClickEvent;
import com.smartgwt.client.widgets.grid.events.CellClickHandler;
import com.smartgwt.client.widgets.grid.events.CellDoubleClickEvent;
import com.smartgwt.client.widgets.grid.events.CellDoubleClickHandler;
import com.smartgwt.client.widgets.layout.LayoutSpacer;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tree.Tree;
import com.smartgwt.client.widgets.tree.TreeNode;

public class ViewActiveWorkflows extends PageWidget {
	HashMap<String, String> map;

	public ViewActiveWorkflows() {
		map = new HashMap<String, String>();
		BpmServiceMain.sendGet("/processInstances/active", new AsyncCallback<String>() {
			public void onFailure(Throwable arg0) {
				createPage(new Label("Couldn't access list of active workflows"), PageWidget.PAGE_VIEWACTIVEWORKFLOWS);
			}
			public void onSuccess(String arg0) {
				ArrayList<String> names = Parse.parseTemplateInstances(arg0);
				ArrayList<String> ids = Parse.parseTemplateInstancesIds(arg0);
				for(int i = 0; i < names.size(); i++) {
					map.put(names.get(i), ids.get(i));
				}
				generateBody(names);
			}
		});
	}
	
	protected void generateBody(ArrayList<String> names) {
		if(names.size() == 0) {
			createPage(new Label("No active workflows"), PageWidget.PAGE_VIEWACTIVEWORKFLOWS);
		} else {
			
			final ListGrid grid = new ListGrid(){
				@Override  
	            protected String getBaseStyle(ListGridRecord record, int rowNum, int colNum) {  
	                if (getFieldName(colNum).equals("id")) {  
	                	ListGridRecord r =  record;  
	                    if (r.getAttribute("id").startsWith("a")) {  
	                        return "completed";  
	                    } else if (r.getAttribute("id").startsWith("TST")) {  
	                        return "open";  
	                    } else {  
	                        return super.getBaseStyle(record, rowNum, colNum);  
	                    }  
	                } else {  
	                    return super.getBaseStyle(record, rowNum, colNum);  
	                }  
	            } 
				
			};
			grid.setWidth100();
			grid.setHeight100();
			grid.setShowFilterEditor(true);
			grid.setFilterOnKeypress(true);
			grid.setShowRecordComponents(true);
			grid.setShowRecordComponentsByCell(true);
			grid.addCellDoubleClickHandler(new CellDoubleClickHandler() {
				public void onCellDoubleClick(CellDoubleClickEvent event) {
					if(event.getColNum() > 0) {
						Object[] args= {event.getRecord().getAttribute("id"),true};
						PageManager.getInstance().setPageHistory(Pages.VIEWWORKFLOW, args );
					}
				}
			}); 
			
			grid.addCellClickHandler(new CellClickHandler() {
				public void onCellClick(CellClickEvent event) {
					if(event.getColNum() == 0) {
						Object[] args= {event.getRecord().getAttribute("id"),true};
						SC.ask(event.getRecord().toString(),null);
						PageManager.getInstance().setPageHistory(Pages.VIEWWORKFLOW, args );
					}
					
				
				
				}
			});
			ListGridField id = new ListGridField("id", "ID");   
	        id.setCellFormatter(new CellFormatter() {  
	            public String format(Object value, ListGridRecord record, int rowNum, int colNum) {  
	            	return value.toString(); 
	            }  
	        });
	        ListGridField am = new ListGridField("AM", "AM"); 
	        ListGridField dac = new ListGridField("DAC", "DAC"); 
	        ListGridField doc = new ListGridField("DOC", "DOC"); 
	        ListGridField dt = new ListGridField("DT", "DT"); 
	        ListGridField sido = new ListGridField("SIDO", "SIDO"); 
	        
			grid.setFields(new ListGridField("activeWorkflow", "Active Workflow"), id,am,dac,doc,dt,sido);
			
			BpmServiceMain.sendGet("/processInstances/active", new AsyncCallback<String>() {
				public void onFailure(Throwable arg0) {
				}
				public void onSuccess(String arg0) {
					
					ArrayList<String> names = Parse.parseTemplateInstances(arg0);
					ArrayList<String> ids = Parse.parseTemplateInstancesIds(arg0);
					ListGridRecord[] records = new ListGridRecord[names.size()];
					for(int i = 0; i < names.size(); i++) {
						records[i] = new ListGridRecord();
						records[i].setAttribute("activeWorkflow", names.get(i));
						records[i].setAttribute("id", ids.get(i));
						//
						BpmServiceMain.sendGet("/processInstances/active/" + BpmServiceMain.urlEncode(ids.get(i)) + "/status", new AsyncCallback<String>() {
							public void onFailure(Throwable caught) {
								SC.say("Error. Please ensure that you are connected to the Internet, and that the server is currently online.");
							}
							public void onSuccess(String result) {
								
								Template template = (result == null || result.equals("") ? new Template() : Parse.parseTemplate(result));
								
								ListGridRecord[] rs = grid.getRecords();
								for(int x = 0;x <rs.length; x++){
									ArrayList<Activity> acts = template.getBase().getActivities();
									
									if (rs[x].getAttribute("id").startsWith(template.getKey())){
										
										String attr = null;
										String n = null;
										for(int j = 0; j < acts.size(); j++){
											Object act = acts.get(j);
											if(act instanceof Task) {
												Task t = (Task)act;										
												attr = t.get("assignee");
												n = t.getName();
												

											} else if(act instanceof Loop) {
												Loop l = (Loop)act;
												attr = l.getLoopTask().get("assignee");
												n = l.getName();


											} else if(act instanceof Decision) {
												Decision d = (Decision)act;
												attr = d.getTask().get("assignee");
												n = d.getName();
											}
										}
										if (attr != null){
											rs[x].setAttribute(attr, n);
											
											break;
										}
										
									}
								}
								
							grid.setData(rs);	
							}
						});
						//
						
						
					}
					grid.setData(records);
					grid.sort(2, SortDirection.ASCENDING);
					
				}
			});
			
			
			createPage(grid, PageWidget.PAGE_VIEWACTIVEWORKFLOWS);}
		}
			/*
			VLayout layout = new VLayout();
			layout.setHeight100();
			layout.setWidth100();
			for(int i = 0; i < names.size(); i++) {
				Label l = new Label(names.get(i));
				l.setStyleName("linkLabel");
				l.addClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						Object[] args= {map.get(((Label)event.getSource()).getTitle()), true};
						PageManager.getInstance().setPageHistory(Pages.VIEWWORKFLOW, args );
					}
				});
				layout.addMember(l);
			}
			LayoutSpacer spacer = new LayoutSpacer();
			spacer.setHeight100();
			layout.addMember(spacer);
			createPage(layout, PageWidget.PAGE_VIEWACTIVEWORKFLOWS);
		}
	}*/

	public void refresh() {
		PageManager.getInstance().setPageHistory(Pages.VIEWACTIVEWORKFLOWS,null);
	}

}
