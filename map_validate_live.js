

(function(){
 const status=document.getElementById('mapstatus');
 if(typeof L==='undefined'){status.textContent='MAP ENGINE UNAVAILABLE — CHECK INTERNET';return;}
 const map=L.map('map',{zoomControl:false,attributionControl:true,preferCanvas:true,fadeAnimation:false,zoomAnimation:false}).setView([53.795,-1.55],12);
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
 let plannedRouteLoaded=false;
 window.haulwaySetRoute=function(jsonText){try{const raw=JSON.parse(jsonText);const coords=raw.map(c=>[Number(c[1]),Number(c[0])]).filter(c=>Number.isFinite(c[0])&&Number.isFinite(c[1]));if(coords.length<2)throw new Error('Route geometry is empty');map.removeLayer(routeLayer);map.removeLayer(routeCasing);routeCasing=L.polyline(coords,{color:'#062d4e',weight:11,opacity:.96,lineCap:'round',lineJoin:'round'}).addTo(map);routeLayer=L.polyline(coords,{color:'#168fe8',weight:6,opacity:1,lineCap:'round',lineJoin:'round'}).addTo(map);plannedRouteLoaded=true;map.fitBounds(routeLayer.getBounds(),{padding:[45,45]});}catch(e){status.style.display='block';status.textContent='ROUTE DISPLAY ERROR';console.log(e);}};
 let userMarker=null,userCircle=null,firstGpsFix=true;
 const vehicleIcon=L.divIcon({className:'',html:'<div class="haulway-position"></div>',iconSize:[30,38],iconAnchor:[15,19]});
 window.haulwaySetZoom=function(offset){const target=Math.max(5,Math.min(18,12+Number(offset||0)));map.setZoom(target);};
 window.haulwayRecenter=function(){if(userMarker)map.setView(userMarker.getLatLng(),Math.max(map.getZoom(),14),{animate:true});};
 if(navigator.geolocation){navigator.geolocation.watchPosition(function(pos){const ll=[pos.coords.latitude,pos.coords.longitude];if(!userMarker){userMarker=L.marker(ll,{icon:vehicleIcon,zIndexOffset:1000}).addTo(map);userCircle=L.circle(ll,{radius:Math.max(pos.coords.accuracy,10),color:'#168fe8',weight:1,fillColor:'#168fe8',fillOpacity:.10}).addTo(map);}else{userMarker.setLatLng(ll);userCircle.setLatLng(ll).setRadius(Math.max(pos.coords.accuracy,10));}if(firstGpsFix){if(!plannedRouteLoaded)map.setView(ll,14,{animate:false});firstGpsFix=false;}},function(err){console.log('GPS unavailable',err.code);},{enableHighAccuracy:true,maximumAge:5000,timeout:20000});}
 setTimeout(()=>map.invalidateSize(true),250);setTimeout(()=>map.invalidateSize(true),900);window.addEventListener('resize',()=>map.invalidateSize(true));
})();
