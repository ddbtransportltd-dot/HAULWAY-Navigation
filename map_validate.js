
<!doctype html>
<html><head><meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/leaflet@1.9.4/dist/leaflet.css">
<style>
html,body,#map{height:100%;width:100%;margin:0;background:#17232b;overflow:hidden}
#map{position:absolute;inset:0}
#mapstatus{position:absolute;z-index:1200;left:50%;top:50%;transform:translate(-50%,-50%);padding:12px 16px;border:1px solid rgba(0,168,232,.8);border-radius:9px;background:rgba(8,16,22,.94);color:#fff;font:600 12px Arial;letter-spacing:.3px;text-align:center;pointer-events:none;max-width:82%;box-shadow:0 5px 24px rgba(0,0,0,.4)}
.leaflet-control-attribution{font-size:9px!important;background:rgba(8,16,22,.72)!important;color:#dce5eb!important}
.leaflet-control-attribution a{color:#6ec8ff!important}.leaflet-control-zoom{display:none}
.leaflet-tile{filter:none!important}
.haulway-position{width:30px;height:38px;display:flex;align-items:center;justify-content:center;filter:drop-shadow(0 2px 3px rgba(0,0,0,.65))}
.haulway-position:before{content:'';width:0;height:0;border-left:14px solid transparent;border-right:14px solid transparent;border-bottom:34px solid #087dcc;position:absolute}
.haulway-position:after{content:'';width:0;height:0;border-left:7px solid transparent;border-right:7px solid transparent;border-bottom:18px solid #fff;position:absolute;top:9px}
</style></head><body><div id="map"></div><div id="mapstatus">LOADING ROAD MAP…</div>
<script src="https://cdn.jsdelivr.net/npm/leaflet@1.9.4/dist/leaflet.js" onerror="this.onerror=null;this.src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'"></script>
<script>
(function(){
 const status=document.getElementById('mapstatus');
 if(typeof L==='undefined'){status.textContent='MAP ENGINE UNAVAILABLE — CHECK INTERNET';return;}
 const map=L.map('map',{zoomControl:false,attributionControl:true,preferCanvas:true,fadeAnimation:false,zoomAnimation:false}).setView([53.4808,-2.2426],11);
 let activeProvider=0, providerTimer=null, tileErrors=0, tilesLoaded=0, baseLayer=null, roadsLayer=null;
 const providers=[
  {name:'Carto Voyager road map',base:'https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png',subdomains:'abcd',attribution:'© OpenStreetMap contributors © CARTO'},
  {name:'Satellite + road labels',base:'https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}',roads:'https://server.arcgisonline.com/ArcGIS/rest/services/Reference/World_Transportation/MapServer/tile/{z}/{y}/{x}',attribution:'Tiles © Esri — Sources: Esri, Maxar, Earthstar Geographics'},
  {name:'OpenStreetMap',base:'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',subdomains:'abc',attribution:'© OpenStreetMap contributors'},
  {name:'OSM France',base:'https://{s}.tile.openstreetmap.fr/osmfr/{z}/{x}/{y}.png',subdomains:'abc',attribution:'© OpenStreetMap contributors · OSM France'}
 ];
 function clearLayers(){if(baseLayer)map.removeLayer(baseLayer);if(roadsLayer){map.removeLayer(roadsLayer);roadsLayer=null;}}
 function useProvider(index){
  if(index>=providers.length){status.style.display='block';status.textContent='MAP TILES UNAVAILABLE — CHECK INTERNET';return;}
  activeProvider=index;tilesLoaded=0;tileErrors=0;clearLayers();
  const p=providers[index];status.style.display='block';status.textContent='LOADING '+p.name.toUpperCase()+'…';
  const opts={maxZoom:19,keepBuffer:3,updateWhenIdle:true,crossOrigin:true,attribution:p.attribution};if(p.subdomains)opts.subdomains=p.subdomains;
  baseLayer=L.tileLayer(p.base,opts);
  let overlayErrors=0,overlayLoaded=0;
  function loaded(){if(tilesLoaded>=3 && (!p.roads || overlayLoaded>=2)){status.style.display='none';if(providerTimer){clearTimeout(providerTimer);providerTimer=null;}}}
  baseLayer.on('tileload',function(){tilesLoaded++;loaded();});
  baseLayer.on('tileerror',function(){tileErrors++;maybeFallback();});
  baseLayer.addTo(map);
  if(p.roads){
   roadsLayer=L.tileLayer(p.roads,{maxZoom:19,keepBuffer:3,updateWhenIdle:true,crossOrigin:true,opacity:.88,attribution:'Roads & labels © Esri'});
   roadsLayer.on('tileload',function(){overlayLoaded++;loaded();});
   roadsLayer.on('tileerror',function(){overlayErrors++;maybeFallback();});
   roadsLayer.addTo(map);
  }
  function maybeFallback(){if((tilesLoaded<3 && tileErrors>=3)||(p.roads && overlayErrors>=3 && overlayLoaded<2)){if(!providerTimer){providerTimer=setTimeout(()=>{providerTimer=null;if(activeProvider===index&&((tilesLoaded<3)||(p.roads&&overlayLoaded<2)))useProvider(index+1);},700);}}}
  if(providerTimer)clearTimeout(providerTimer);
  providerTimer=setTimeout(()=>{providerTimer=null;if(activeProvider===index&&((tilesLoaded<3)||(p.roads&&overlayLoaded<2)))useProvider(index+1);},8000);
 }
 useProvider(0);
 let routeLayer=L.polyline([],{color:'#168fe8',weight:6,opacity:1,lineCap:'round',lineJoin:'round'}).addTo(map);
 let routeCasing=L.polyline([],{color:'#062d4e',weight:11,opacity:.96,lineCap:'round',lineJoin:'round'}).addTo(map);
 let travelledLayer=L.polyline([],{color:'#91a4b2',weight:5,opacity:.9,lineCap:'round',lineJoin:'round'}).addTo(map);
 let routeCoords=[],plannedRouteLoaded=false,autoFollow=true,lastVehicle=null;
 window.haulwaySetRoute=function(jsonText){try{const raw=JSON.parse(jsonText);const coords=raw.map(c=>[Number(c[1]),Number(c[0])]).filter(c=>Number.isFinite(c[0])&&Number.isFinite(c[1]));if(coords.length<2)throw new Error('Route geometry is empty');routeCoords=coords;routeCasing.setLatLngs(coords);routeLayer.setLatLngs(coords);travelledLayer.setLatLngs([]);plannedRouteLoaded=true;map.fitBounds(routeLayer.getBounds(),{padding:[45,45]});}catch(e){status.style.display='block';status.textContent='ROUTE DISPLAY ERROR';console.log(e);}};
 let userMarker=null,userCircle=null,firstGpsFix=true;
 const vehicleIcon=L.divIcon({className:'',html:'<div class="haulway-position"></div>',iconSize:[30,38],iconAnchor:[15,19]});
 window.haulwaySetZoom=function(offset){const target=Math.max(5,Math.min(18,12+Number(offset||0)));map.setZoom(target);};
 window.haulwayRecenter=function(){autoFollow=true;if(userMarker)map.setView(userMarker.getLatLng(),Math.max(map.getZoom(),15),{animate:true});};
 map.on('dragstart',function(){autoFollow=false;});
 function updateVehicle(lat,lon,accuracy,bearing){const ll=[lat,lon];lastVehicle=ll;if(!userMarker){userMarker=L.marker(ll,{icon:vehicleIcon,zIndexOffset:1000}).addTo(map);userCircle=L.circle(ll,{radius:Math.max(accuracy||10,10),color:'#168fe8',weight:1,fillColor:'#168fe8',fillOpacity:.10}).addTo(map);}else{userMarker.setLatLng(ll);userCircle.setLatLng(ll).setRadius(Math.max(accuracy||10,10));}const el=userMarker.getElement();if(el&&Number.isFinite(bearing)){const inner=el.querySelector('.haulway-position');if(inner)inner.style.transform='rotate('+bearing+'deg)';}if(routeCoords.length>1){let best=0,bestD=Infinity;for(let i=0;i<routeCoords.length;i++){const d=map.distance(ll,routeCoords[i]);if(d<bestD){bestD=d;best=i;}}travelledLayer.setLatLngs(routeCoords.slice(0,best+1));}if(autoFollow&&(firstGpsFix||!plannedRouteLoaded)){map.setView(ll,Math.max(map.getZoom(),15),{animate:!firstGpsFix});}else if(autoFollow){map.panTo(ll,{animate:true,duration:.35});}firstGpsFix=false;}
 window.haulwaySetVehicleLocation=function(lat,lon,bearing){if(Number.isFinite(lat)&&Number.isFinite(lon))updateVehicle(lat,lon,18,bearing);};
 if(navigator.geolocation){navigator.geolocation.watchPosition(function(pos){updateVehicle(pos.coords.latitude,pos.coords.longitude,pos.coords.accuracy,pos.coords.heading||0);},function(err){console.log('GPS unavailable',err.code);},{enableHighAccuracy:true,maximumAge:5000,timeout:20000});}
 setTimeout(()=>map.invalidateSize(true),250);setTimeout(()=>map.invalidateSize(true),900);window.addEventListener('resize',()=>map.invalidateSize(true));
})();
</script></body></html>
