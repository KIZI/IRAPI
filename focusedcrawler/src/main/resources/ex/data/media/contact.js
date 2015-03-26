// $Id: contact.js 1715 2008-11-12 21:05:15Z labsky $
// Accompanying scripts for the contact extraction ontology

var synonymNames=[['robert','bob'],['joseph','joe'],['charles','charlie','chuck'],['william','bill','will','billy','willy']];

function getSynonyms(w, set) {
    var hash=set.__hash;
    if(hash==undefined) {
        hash={};
        for(var i=0;i<set.length;i++)
            for(var j=0;j<set.length;j++)
                hash[set[i][j]]=set[i];
        set.__hash=hash;
    }
    var synonyms=hash[w.toLowerCase()];
    if(synonyms==undefined)
        synonyms=[w];
    return synonyms;
}

function nameMatchesEmail(name, email) {
    if(!name || !email)
        return 0;
    email=email.substr(0,email.indexOf('@')).toLowerCase();
    email=email.replace(/[,.\-_]/g,"");
    if(email.length==0)
        return 0;
    name=name.toLowerCase();
    var words=name.split(/[. ,]+/);

    // try concatenations of name parts with possible omissions
    // and name parts optionally replaced by initials
    var cands=[""];
    for(var i=0;i<words.length;i++) {
        var w=words[i];
        var ccnt=cands.length;
        for(var k=0;k<ccnt;k++) {
            var base=cands[k];
            if(w.length==0)
                continue;

            var syns=getSynonyms(w, synonymNames);
            for(var j=0;j<syns.length;j++) {
                var w1=syns[j];
                var c=base+w1;
                if(c==email)
                    return 1;
                else if(email.indexOf(c)==0)
                    return 0.75;
                cands[cands.length]=c;
            }

            for(var j=0;j<syns.length;j++) {
                var w1=syns[j];
                var c=base+w1[0];
                if(c==email)
                    return 1;
                else if(email.indexOf(c)==0)
                    return 0.5;
                cands[cands.length]=c;
            }
        }
    }
    // search for a common substring >=3 of one of name parts with email
    var minLen=3;
    if(email.length>=minLen) {
        for(var i=0;i<words.length;i++) {
            if(words[i].length>=minLen) {
                var com=commonSubstr(email, words[i], minLen);
                if(com!=null)
                    return 0.5;
            }
        }
    }
    return 0;
}

function commonSubstr(s1, s2, minLen) {
    var s1ei=s1.length-minLen;
    var s2ei=s2.length-minLen;
    for(var i=0;i<=s1ei;i++) {
        // now search s2 for s1.substr(i,minLen)
        for(var j=0;j<=s2ei;j++) {
            // check if s1.substr(i,minLen) == s2.substr(j,minLen)
            var k=0;
            for(;k<minLen;k++)
                if(s1[i+k]!=s2[j+k])
                    break;
            if(k==minLen)
                return s1.substr(i,minLen);
        }
    }
    return null;
}

function checkPersonName(name) {
    if (name.length<=1)
        return false;
    if ((/branch|company|hospital|england|^new|nombre|email|e-mail|pública|(^|\s)st($|\s)/i).test(name))
        return false;
    if ((/^(Mo|Tu|We|Th|Fr|Sa|Su|Lu|Ma|Mi|Ju|Vi|Sá|Do)(| \w{1,2})$/i).test(name))
        return false;
    if ((/(^|[ ,.])M\s?\.?\s?D\s?\.?$/).test(name))
        return false;
    return true;
}

function checkOrgName(name) {
    if (name.length<=1)
        return false;
    if ((/^(la|el|li|di|de|los|las|centrum|centre|centro|center|hospital)$/i).test(name))
        return false;
    if ((/^(Mo|Tu|We|Th|Fr|Sa|Su|Lu|Ma|Mi|Ju|Vi|Sá|Do)(| \w{1,2})$/i).test(name))
        return false;
    return true;
}

function addSyns(lst) {
    var len=lst.length;
    for(var i=0;i<len;i++) {
      var w=lst[i];
      if(w.length<=1)
        continue;
      var syns=getSynonyms(w, synonymNames);
      if(syns!=null && syns.length>1) {
        for(var j=0;j<syns.length;j++) {
          if(syns[j]!=w) {
            lst[lst.length]=syns[j];
          }
        }
      }
    }
    return lst;
}

/** Checks whether 2 person names may reference each other */
function nameRefersTo(a, b) {
    var n1=a.toLowerCase().split(/[\s&+.,;\-]+/);
    var n2=b.toLowerCase().split(/[\s&+.,;\-]+/);
    addSyns(n1);
    addSyns(n2);
    for(var i=0;i<n1.length;i++) {
      var w1=n1[i];
      if(w1=='dr'||w1=='mr')
        continue;
      for(var j=0;j<n2.length;j++) {
        var w2=n2[j];
        if(w1==w2 && w1.length>2) {
           //LG(TRC,"Wi wi: "+w1+" ~ "+n1+" vs. "+n2);
           return true;
        }else {
           //LG(TRC,"No no: "+w1+" ~ "+n1+" vs. "+n2);
        }
      }
    }
    return false;
}

function returnTrue() { return true; }

function sameTel(a, b) {
    // strip country codes, spaces, dashes, slashes and compare
    return compressTel(a)==compressTel(b);
}

function compressTel(phone) {
    phone=phone.replace(/[()]+/g,'');
    phone=phone.replace(/\s*\+\s*\d{1,3}[\s\-\/]/g,'');
    phone=phone.replace(/[\s\-\/]+/g,'');
    return phone;
}

function checkTel(phone) {
    var cnt=0;
    for(i=0;i<phone.length;i++)
      if((/\d/).test(phone.charAt(i)))
        cnt++;
    return cnt>=3; //allow ext: cnt>6;
}

function matchCount(s,re) { var i=0; while(re.test(s)) {i++}; return i; }
