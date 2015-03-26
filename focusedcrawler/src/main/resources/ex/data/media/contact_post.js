// $Id: contact_post.js 1650 2008-09-16 06:43:59Z labsky $
// Post-processing script for the contact extraction ontology

/** Applies final post-processing rules to extracted contact sequence. */
function transformContacts() {
  if(0) {
    LG(ERR,"Transformation skipped.");
    return;
  }

  var ATT_NAM="name";
  var ATT_RES="name_responsible";
  var ATT_VCR="name_vc_responsible";
  var ATT_WEB="name_webmaster";
  var ATT_AUT="name_author";
  var ATT_NSP="name_sponsor";
  var ATT_OSP="organization_sponsor";
  var ATT_EWE="email_webmaster";
  
  var CLS_CON="Contact";
  var CLS_RES="Contact_responsible";
  var CLS_VCR="Contact_vc_responsible";
  var CLS_WEB="Contact_webmaster";
  var CLS_AUT="Contact_author";
  var CLS_SPO="Contact_sponsor";
  
  LG(USR,"Applying post-processing transformation to document "+document.id);
  
  // only transform the best sequence of extrated objects from n-best
  if(document.extractedPaths.size()==0)
  	return;
  var bestSeq=document.extractedPaths.get(0);
  var edits=0;
  
  for(var i=0;i<document.classifications.size();i++) {
    var c=document.classifications.get(i);
    //LG(USR,"CLS "+c);
  }

  for(var i=0;i<bestSeq.size();i++) {
    var o=bestSeq.get(i);
    //LG(USR,i+".OBJ "+o);
  }
  
  // rules for pages classified as "virt" or "vc": virtual consultation pages
  if(document.isClassified("vc", 0.3) || document.isClassified("virt", 0.3)) {
    // Rule 1: if there are no vc responsibles, the first contact name becomes vc responsible 
    var vcResponsibles=bestSeq.getAttributesByName(ATT_VCR);
    if(vcResponsibles.size()==0) {
      var avName=null;
      var contacts=bestSeq.getAttributesByName(ATT_RES);
      if(contacts.size()>0) {
        avName=contacts.get(0);
      }else {
        contacts=bestSeq.getAttributesByName(ATT_NAM);
        if(contacts.size()>0) {
          avName=contacts.get(0);
        }
      }
      if(avName!=null) {
        LG(USR,"Rule1: VC: Transforming 1st "+ATT_RES+" or "+ATT_NAM+" to "+ATT_VCR+": "+avName);
        avName.setAttributeName(ATT_VCR, model); // also changes attribute name for all references
        if(avName.instance!=null) {
          instance.setClassName(CLS_VCR, model);
        }
        edits++;
      }
    }
  }
  // rules for pages classified as "contact" pages
  else if(document.isClassified("contact", 0.3)) {
    // Rule 2: if there are no responsibles, and the page is contact and not vc, then 
    // the first contact name becomes responsible 
    var responsibles=bestSeq.getAttributesByName(ATT_RES);
    if(responsibles.size()==0) {
      var contacts=bestSeq.getAttributesByName(ATT_NAM);
      if(contacts.size()>0) {
        var avName=contacts.get(0);
        LG(USR,"Rule2: Contact: Transforming 1st "+ATT_NAM+" to "+ATT_RES+": "+avName);
        avName.setAttributeName(ATT_RES, model); // also changes attribute name for all references
        var inst=avName.getInstance();
        if(inst!=null) {
          inst.setClassName(CLS_RES, model);
          edits++;
        }
      }
    }
  }
  
  // when an instance contains a specialized name, also specialize its class if not already done by the extraction model or prev rules 
  var contactsInstances=bestSeq.getInstancesByClass(CLS_CON);
  LG(USR,"*** INSTANCES: "+contactsInstances.size());
  for(var i=0;i<contactsInstances.size();i++) {
    var inst=contactsInstances.get(i);
    LG(USR,"*** INST: "+inst);
    if(inst.getAttributesByName(ATT_VCR, model).size()>0) {
      LG(USR,"Rule3: Transforming class "+CLS_CON+" to "+CLS_VCR+": "+inst);
      inst.setClassName(CLS_VCR, model);
      edits++;
    }else if(inst.getAttributesByName(ATT_RES, model).size()>0) {
      LG(USR,"Rule4: Transforming class "+CLS_CON+" to "+CLS_RES+": "+inst);
      inst.setClassName(CLS_RES, model);
      edits++;
    }else if(inst.getAttributesByName(ATT_WEB, model).size()>0) {
      LG(USR,"Rule5: Transforming class "+CLS_CON+" to "+CLS_WEB+": "+inst);
      inst.setClassName(CLS_WEB, model);
      edits++;
    }else if(inst.getAttributesByName(ATT_AUT, model).size()>0) {
      LG(USR,"Rule6: Transforming class "+CLS_CON+" to "+CLS_AUT+": "+inst);
      inst.setClassName(CLS_AUT, model);
      edits++;
    }else if(inst.getAttributesByName(ATT_NSP, model).size()>0 || inst.getAttributesByName(ATT_OSP, model).size()>0) {
      LG(USR,"Rule7: Transforming class "+CLS_CON+" to "+CLS_SPO+": "+inst);
      inst.setClassName(CLS_SPO, model);
      edits++;
    }else if(inst.getAttributesByName(ATT_EWE, model).size()>0) {
      LG(USR,"Rule8: Transforming class "+CLS_CON+" to "+CLS_WEB+": "+inst);
      inst.setClassName(CLS_WEB, model);
      edits++;
    }
    
  }
  
  LG(USR,"Transformation done, applied "+edits+" rules.");
}
