

function CollectionForm(formId, type) {

    RamaddaUtil.defineMembers(this, {
            formId:formId,
            analysisUrl: ramaddaBaseUrl +"/model/" + type +"?",
            dataProcesses: [],
            init: function() {
                var collectionForm = this;
                for(var i=1;i<=2;i++) {
                    collection  = 'collection' + i;
                    this.initCollection(collection);
                }

                var theForm = this;
                //Listen to the form
                $("#"+ this.formId).submit(function( event ) {
                        theForm.handleFormSubmission();
                        event.preventDefault();
                    });                

                //Listen to the button
                $("#" + this.formId+"_submit").button().click(function(event) {
                        theForm.handleFormSubmission();
                        event.preventDefault();
                    });

            },
            handleFormSubmission: function() {
                var url = this.analysisUrl;
                var theForm = this;
                var inputs = $('#' + this.formId +' ::input');
                inputs.each(function() {             
                        var value = $(this).val();
                        if(this.name == "entryselect") {
                            if(!$(this).is(':checked')) {
                                return;
                            }
                        }
                        //A hack for now but 
                        if(this.type == 'radio') {
                            if(!$(this).is(':checked')) {
                                return;
                            }
                        }
                        if(HtmlUtil.valueDefined(value)) {
                            url += "&" + this.name+ "=" + encodeURIComponent(value);
                        }
                    });       

                console.log("url:" + url);
                //add the arg that gives us the image directly back then set the img src
                url += "&givemeimage=true";
                var outputDiv = $('#' + this.formId +"_output");
                if(outputDiv.size()==0) {
                    console.log("no output div");
                }
                //Make the html with the image
                var html = "Results:<br>" + HtmlUtil.image(url,[ATTR_TITLE, "Loading"])
                outputDiv.html(html);
            },
            initCollection: function(collection) {
                var collectionForm = this;
                this.getCollectionSelect(collection).change(function(event) {
                        return collectionForm.collectionChanged(collection);
                    });
                for(var fieldIdx=0;fieldIdx<10;fieldIdx++) {
                    this.initField(collection, fieldIdx);
                }
                var t = this.getCollectionSelect(collection);
                //        alert('t:' + t.size());
                var collectionId  =  t.val();
        
                //If they had one selected 
                if(collectionId != null && collectionId !== "") {
                    //            alert("updating fields:" + collectionId);
                    this.updateFields(collection,  collectionId, 0, true);
                }
            },
            initField:function(collection, fieldIdx) {
                var collectionForm = this;
                this.getFieldSelect(collection, fieldIdx).change(function(event) {
                        return collectionForm.fieldChanged(collection, fieldIdx);
                    });
            },
            addDataProcess: function(dataProcess) {
                this.dataProcesses.push(dataProcess);
            },
           //Gets called when the collection select widget is changed
            collectionChanged: function  (collection, selectId) {
                var collectionId = this.getCollectionSelect(collection).val();
                var fieldIdx = 0;
                if(!collectionId || collectionId == "") {
                    this.clearFields(collection, fieldIdx);
                    return false;
                }
                this.updateFields(collection,  collectionId, fieldIdx, false);
                return false;

            },
           //Get the list of metadata values for the given field and collection
                updateFields:function(collection, collectionId, fieldIdx, fromInit) {
                var url = this.analysisUrl +"json=test&thecollection=" + collectionId+"&field=" + fieldIdx;
                //Assemble the other field values up to the currently selected field
                for(var i=0;i<fieldIdx;i++) {
                    var val = this.getFieldSelect(collection, i).val();
                    if(val!="") {
                        url = url +"&field" + i + "=" + encodeURIComponent(val);
                    }
                }
                var collectionForm = this;
                $.getJSON(url, function(data) {
                        var currentValueIsInNewList = collectionForm.setFieldValues(collection, data, fieldIdx);
                        var hasNextField =  collectionForm.hasField(collection, fieldIdx+1);
                        var nextFieldIndex = fieldIdx+1;
                        if(hasNextField) {
                            if(currentValueIsInNewList)  {
                                collectionForm.updateFields(collection, collectionId, nextFieldIndex, true); 
                            } else {
                                collectionForm.clearFields(collection, nextFieldIndex);
                            }
                        }
                    });

            },
          //Clear the field selects starting at start idx
                clearFields: function(collection, startIdx) {
                for(var idx=startIdx;idx<10;idx++) {
                    //this.getFieldSelect(collection, idx).html("<select><option value=''>--</option></select>");
                    this.getFieldSelect(collection, idx).html("<option value=''>--</option>");
                }
            },
         //Get the select object for the given field
         getFieldSelect: function(collection, fieldIdx) {
               return  $('#' + this.getFieldSelectId(collection, fieldIdx));
            },
            hasField: function(collection, fieldIdx) {
                return  this.getFieldSelect(collection, fieldIdx).size()>0;
            },
           //Get the selected entry id
            getSelectedCollectionId: function(collection) {
                var t = this.getCollectionSelect(collection);
                return t.val();
           },
           //Get the collection selector 
           getCollectionSelect: function(collection) {
                return  $('#' + this.getCollectionSelectId(collection));
           },
         //This matches up with ClimateModelApiHandler.getFieldSelectId
          getFieldSelectId: function(collection, fieldIdx) {
                return  this.getCollectionSelectId(collection) + "_field" + fieldIdx;
            },
           //dom id of the collection select widget
           //This matches up with ClimateModelApiHandler.getCollectionSelectId
            getCollectionSelectId: function(collection) {
                return  this.formId +"_"  + collection;
            },
            setFieldValues: function(collection, data, fieldIdx) {
                var currentValue =    this.getFieldSelect(collection, fieldIdx).val();
                var currentValueIsInNewList = false;
                //var html = "<select>";
                var html = "";
                for(var i=0;i<data.length;i++)  {
                    var objIQ = data[i];
                    var value,label;
                    var type = typeof(objIQ);
                    if (type == 'object') {  
                        // made from TwoFacedObject [ {id:id1,value:value1}, {id:id2,value:value2} ]
                        value = objIQ.id;
                        label = objIQ.label;
                    } else {
                        value = objIQ;
                        label  = value;
                    }
                    if(label == "") {
                        label =  "--";
                    }
                    var extra = "";
                    if(currentValue == value) {
                        extra = " selected ";
                        currentValueIsInNewList = true;
                    }
                    html += "<option value=\'"+value+"\'   " + extra +" >" + label +"</option>";
                }
                //html+="</select>";
                this.getFieldSelect(collection, fieldIdx).html(html);
                return currentValueIsInNewList;
            },
            fieldChanged: function (collection, fieldIdx) {
                var value = this.getFieldSelect(collection, fieldIdx).val();
                if(value == null || value == "") {
                    this.clearFields(collection, fieldIdx+1);
                    return;
                }
                this.updateFields(collection, this.getSelectedCollectionId(collection),  fieldIdx+1);
            }
        });
        this.init();
}


function DataProcess(formId) {
    RamaddaUtil.defineMembers(this, {
            formId:formId
        });

}


var  ARG_CDO_PREFIX = "cdo_";
var ARG_CDO_OPERATION = ARG_CDO_PREFIX+ "operation";
var ARG_CDO_STARTMONTH = ARG_CDO_PREFIX  + "startmonth";
var ARG_CDO_ENDMONTH = ARG_CDO_PREFIX + "endmonth";
var ARG_CDO_MONTHS = ARG_CDO_PREFIX + "months";
var ARG_CDO_STARTYEAR = ARG_CDO_PREFIX + "startyear";
var ARG_CDO_ENDYEAR = ARG_CDO_PREFIX + "endyear";
var ARG_CDO_YEARS = ARG_CDO_PREFIX + "years";
var ARG_CDO_PARAM = ARG_CDO_PREFIX + "param";
var ARG_CDO_LEVEL = ARG_CDO_PREFIX + "level";
var ARG_CDO_STAT = ARG_CDO_PREFIX + "stat";
var ARG_CDO_FROMDATE = ARG_CDO_PREFIX + "fromdate";
var ARG_CDO_TODATE = ARG_CDO_PREFIX + "todate";
var ARG_CDO_PERIOD = ARG_CDO_PREFIX + "period";
var ARG_CDO_AREA = ARG_CDO_PREFIX + "area";
var ARG_CDO_AREA_NORTH = ARG_CDO_AREA + "_north";
var ARG_CDO_AREA_SOUTH = ARG_CDO_AREA + "_south";
var ARG_CDO_AREA_EAST = ARG_CDO_AREA + "_east";
var ARG_CDO_AREA_WEST = ARG_CDO_AREA + "_west";



function CDOTimeSeriesProcess(formId) {
     var SUPER;
     RamaddaUtil.inherit(this, SUPER = new DataProcess(formId));
}

function CDOArealStatisticsProcess(formId) {
     var SUPER;
     RamaddaUtil.inherit(this, SUPER = new DataProcess(formId));
}

function NCLModelPlotDataProcess(formId) {
     var SUPER;
     RamaddaUtil.inherit(this, SUPER = new DataProcess(formId));
}



