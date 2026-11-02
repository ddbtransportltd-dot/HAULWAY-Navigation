package com.czerman.haulway_navigation

import android.annotation.SuppressLint
import android.location.Geocoder
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import org.json.JSONObject

private val ImageryPanel = Color(0xFF151E26)
private val ImageryBlue = Color(0xFF168FE0)

/**
 * HAULWAY multi-provider street imagery preview.
 * Google Street View is attempted first, followed by MapillaryJS and KartaView.
 * API credentials are injected from local.properties via BuildConfig.
 */
@Composable
fun HaulwayStreetImageryScreen(onBack: () -> Unit) {
    var address by remember { mutableStateOf("Dukinfield, Greater Manchester") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var latitude by remember { mutableStateOf("53.474") }
    var longitude by remember { mutableStateOf("-2.083") }
    var searchRequest by remember { mutableStateOf(0) }
    var requestedProvider by remember { mutableStateOf("AUTO") }
    var lastAppliedSearch by remember { mutableStateOf(0) }

    Column(
        Modifier.fillMaxSize().background(Color(0xFF0B1117)).padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = ImageryPanel)) {
                Text("BACK", color = Color.White)
            }
            Column(Modifier.weight(1f).padding(start = 8.dp)) {
                Text("HAULWAY 360°", color = Color.White, fontSize = 20.sp)
                Text("GOOGLE · MAPILLARY · KARTAVIEW", color = Color(0xFF8DA8BA), fontSize = 10.sp)
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = address, onValueChange = { address = it }, label = { Text("ADDRESS / POSTCODE") },
                modifier = Modifier.weight(1f), singleLine = true,
                colors = imageryFieldColors()
            )
            Button(onClick = {
                val query = address.trim()
                if (query.isNotEmpty()) scope.launch {
                    val found = withContext(Dispatchers.IO) {
                        try { Geocoder(context).getFromLocationName(query, 1)?.firstOrNull() } catch (_: Exception) { null }
                    }
                    if (found != null) {
                        latitude = found.latitude.toString()
                        longitude = found.longitude.toString()
                        searchRequest++
                    }
                }
            }, colors = ButtonDefaults.buttonColors(containerColor = ImageryBlue)) { Text("FIND") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            OutlinedTextField(
                value = latitude, onValueChange = { latitude = it }, label = { Text("LATITUDE") },
                modifier = Modifier.weight(1f), singleLine = true,
                colors = imageryFieldColors()
            )
            OutlinedTextField(
                value = longitude, onValueChange = { longitude = it }, label = { Text("LONGITUDE") },
                modifier = Modifier.weight(1f), singleLine = true,
                colors = imageryFieldColors()
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("AUTO", "GOOGLE", "MAPILLARY", "KARTAVIEW").forEach { provider ->
                Button(
                    onClick = { requestedProvider = provider; searchRequest++ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (requestedProvider == provider) ImageryBlue else ImageryPanel
                    ), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 3.dp, vertical = 7.dp),
                    shape = RoundedCornerShape(7.dp)
                ) { Text(provider, fontSize = 8.sp, color = Color.White, maxLines = 1) }
            }
        }
        Text(
            "Enter an address/postcode or coordinates. AUTO checks Google Street View first, then Mapillary, then KartaView. Coverage varies; a street photo is not always a full 360° panorama.",
            color = Color(0xFFB5C2CC), fontSize = 11.sp
        )
        AndroidView(
            modifier = Modifier.fillMaxSize().weight(1f),
            factory = { context ->
                WebView(context).apply {
                    setBackgroundColor(android.graphics.Color.rgb(9, 15, 21))
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadsImagesAutomatically = true
                    settings.blockNetworkImage = false
                    settings.allowFileAccess = false
                    settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW
                    webChromeClient = WebChromeClient()
                    webViewClient = WebViewClient()
                    loadDataWithBaseURL("https://haulway.local/", imageryHtml(), "text/html", "UTF-8", null)
                }
            },
            update = { webView ->
                if (searchRequest > 0 && searchRequest != lastAppliedSearch) {
                    lastAppliedSearch = searchRequest
                    val lat = latitude.toDoubleOrNull()
                    val lon = longitude.toDoubleOrNull()
                    if (lat != null && lon != null && lat in -90.0..90.0 && lon in -180.0..180.0) {
                        val js = "window.haulwayFindImagery(${lat},${lon},${JSONObject.quote(requestedProvider)});"
                        webView.evaluateJavascript(js, null)
                    } else {
                        webView.evaluateJavascript("window.haulwayShowMessage('Enter valid latitude and longitude');", null)
                    }
                }
            }
        )
    }
}

@Composable
private fun imageryFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = ImageryBlue,
    unfocusedBorderColor = Color(0xFF526575),
    focusedLabelColor = ImageryBlue,
    unfocusedLabelColor = Color(0xFF9AABB8),
    cursorColor = ImageryBlue
)

@SuppressLint("SetJavaScriptEnabled")
private fun imageryHtml(): String {
    val googleKey = JSONObject.quote(BuildConfig.GOOGLE_MAPS_API_KEY)
    val mapillaryToken = JSONObject.quote(BuildConfig.MAPILLARY_ACCESS_TOKEN)
    return """
<!doctype html><html><head><meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no">
<link href="https://unpkg.com/mapillary-js@4.1.2/dist/mapillary.css" rel="stylesheet">
<style>
html,body,#stage{margin:0;width:100%;height:100%;overflow:hidden;background:#09131c;color:#fff;font-family:Arial,sans-serif}
#status{position:absolute;z-index:20;left:12px;right:12px;top:12px;padding:10px 12px;background:#d91b2732;border:1px solid #456071;border-radius:8px;font-size:12px}
#viewer{position:absolute;inset:0;width:100%;height:100%;display:none;background:#081018}
#photo{display:none;position:absolute;inset:0;width:100%;height:100%;object-fit:contain;background:#081018}
#credit{position:absolute;bottom:6px;left:8px;right:8px;font-size:10px;color:#dce8ef;text-shadow:0 1px 3px #000;z-index:25;pointer-events:none}
</style></head><body><div id="stage"><div id="status">Enter coordinates and tap a provider to search street imagery.</div><div id="viewer"></div><img id="photo"><div id="credit"></div></div>
<script src="https://unpkg.com/mapillary-js@4.1.2/dist/mapillary.js"></script>
<script>
const GOOGLE_KEY=$googleKey, MAPILLARY_TOKEN=$mapillaryToken;
let mapillaryViewer=null, currentSearch=0;
function status(t){document.getElementById('status').style.display='block';document.getElementById('status').textContent=t;}
function hideStatus(){document.getElementById('status').style.display='none';}
function clearView(){if(mapillaryViewer){try{mapillaryViewer.remove();}catch(e){} mapillaryViewer=null;}document.getElementById('viewer').style.display='none';document.getElementById('photo').style.display='none';document.getElementById('credit').textContent='';}
window.haulwayShowMessage=status;
function showGoogle(lat,lon,token){
 return new Promise(resolve=>{
  if(!token){resolve(false);return;}
  if(!window.google||!google.maps||!google.maps.StreetViewService){
   if(!window._googleLoading){window._googleLoading=true;let s=document.createElement('script');s.src='https://maps.googleapis.com/maps/api/js?key='+encodeURIComponent(token)+'&v=weekly';s.onload=()=>{window._googleLoading=false;resolve(showGoogle(lat,lon,token));};s.onerror=()=>{window._googleLoading=false;resolve(false);};document.head.appendChild(s);}else{let n=0;let t=setInterval(()=>{n++;if(window.google&&google.maps&&google.maps.StreetViewService){clearInterval(t);resolve(showGoogle(lat,lon,token));}else if(n>40){clearInterval(t);resolve(false);}},250);}return;
  }
  const service=new google.maps.StreetViewService();
  service.getPanorama({location:{lat:lat,lng:lon},radius:100,source:google.maps.StreetViewSource.OUTDOOR},(data,result)=>{
   if(result!==google.maps.StreetViewStatus.OK||!data||!data.location){resolve(false);return;}
   clearView();const node=document.getElementById('viewer');node.style.display='block';
   new google.maps.StreetViewPanorama(node,{position:data.location.latLng,pov:{heading:0,pitch:0},zoom:0,visible:true,linksControl:true,panControl:true,enableCloseButton:false,fullscreenControl:false,motionTracking:false,motionTrackingControl:false});
   document.getElementById('credit').textContent='Google Street View';hideStatus();resolve(true);
  });
 });
}
async function showMapillary(lat,lon,token){
 if(!token||!window.mapillary||!mapillary.Viewer)return false;
 const d=0.003;const bbox=[lon-d,lat-d,lon+d,lat+d].join(',');
 const fields='id,is_pano,captured_at,computed_geometry,compass_angle,sequence,thumb_1024_url';
 const url='https://graph.mapillary.com/images?access_token='+encodeURIComponent(token)+'&fields='+encodeURIComponent(fields)+'&bbox='+bbox+'&limit=30';
 try{const response=await fetch(url);if(!response.ok)return false;const json=await response.json();const images=json.data||[];if(!images.length)return false;
  const pano=images.find(x=>x.is_pano===true)||images[0];clearView();const node=document.getElementById('viewer');node.style.display='block';
  mapillaryViewer=new mapillary.Viewer({accessToken:token,container:node,imageId:String(pano.id)});
  document.getElementById('credit').textContent='Street-level imagery © Mapillary contributors';hideStatus();return true;
 }catch(e){return false;}
}
async function showKartaView(lat,lon){
 try{const url='https://api.openstreetcam.org/2.0/photo/?lat='+lat+'&lng='+lon+'&radius=500';const response=await fetch(url);if(!response.ok)return false;const json=await response.json();const rows=(json.result&&json.result.data)||json.data||[];if(!rows.length)return false;
  const item=rows[0];const src=item.filepath||item.fileUrl||item.fileurl||item.imageUrl||item.url;if(!src)return false;
  clearView();const img=document.getElementById('photo');img.src=src;img.style.display='block';document.getElementById('credit').textContent='Street-level image © KartaView contributors';hideStatus();return true;
 }catch(e){return false;}
}
window.haulwayFindImagery=async function(lat,lon,provider){const run=++currentSearch;clearView();status('Searching street imagery…');const chain=provider==='AUTO'?['GOOGLE','MAPILLARY','KARTAVIEW']:[provider];
 for(const p of chain){if(run!==currentSearch)return;status('Checking '+p+'…');let ok=false;if(p==='GOOGLE')ok=await showGoogle(lat,lon,GOOGLE_KEY);if(p==='MAPILLARY')ok=await showMapillary(lat,lon,MAPILLARY_TOKEN);if(p==='KARTAVIEW')ok=await showKartaView(lat,lon);if(ok){if(run===currentSearch)return;return;}}
 if(run===currentSearch)status('No imagery found at this location. Try another point or provider.');
};
</script></body></html>
""".trimIndent()
}
